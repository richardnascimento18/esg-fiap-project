#!/usr/bin/env bash
set -Eeuo pipefail

image_ref=${1:?Pass the GHCR image by immutable digest}
[[ "$image_ref" =~ ^ghcr\.io/[a-z0-9._/-]+@sha256:[0-9a-f]{64}$ ]] || {
  echo 'Expected a GHCR image reference with a sha256 digest.' >&2
  exit 2
}

# This job runs on a fresh runner. Pulling by digest here cannot reuse the
# image built in the separate publication job.
docker pull "$image_ref"

suffix="${GITHUB_RUN_ID:-local}-${GITHUB_RUN_ATTEMPT:-1}"
network="ecocity-smoke-$suffix"
mongo="ecocity-mongo-$suffix"
api="ecocity-api-$suffix"
cleanup() {
  docker rm -f "$api" "$mongo" >/dev/null 2>&1 || true
  docker network rm "$network" >/dev/null 2>&1 || true
}
trap cleanup EXIT

docker network create "$network" >/dev/null
docker run -d --name "$mongo" --network "$network" --network-alias mongo mongo:7.0 >/dev/null

# These credentials exist only for this disposable CI container.
export APP_EDITOR_USERNAME=ci-editor
export APP_EDITOR_PASSWORD="$(openssl rand -hex 20)"
export APP_ADMIN_USERNAME=ci-admin
export APP_ADMIN_PASSWORD="$(openssl rand -hex 20)"
docker run -d --name "$api" --network "$network" -p 127.0.0.1::8080 \
  -e SPRING_DATA_MONGODB_URI=mongodb://mongo:27017/ecocity_esg \
  -e APP_EDITOR_USERNAME -e APP_EDITOR_PASSWORD \
  -e APP_ADMIN_USERNAME -e APP_ADMIN_PASSWORD \
  -e APP_LICENSE_ALERT_CRON=- "$image_ref" >/dev/null

port=$(docker port "$api" 8080/tcp | sed -n 's/.*://p' | head -n 1)
base="http://127.0.0.1:$port"
ready=false
for _ in $(seq 1 90); do
  code=$(curl -sS -o /dev/null -w '%{http_code}' "$base/actuator/health/readiness" || true)
  if [[ "$code" == 200 ]]; then
    ready=true
    break
  fi
  if [[ $(docker inspect -f '{{.State.Running}}' "$api") != true ]]; then
    docker logs "$api" >&2
    exit 1
  fi
  sleep 2
done
if [[ "$ready" != true ]]; then
  docker logs "$api" >&2
  echo 'Readiness never reached HTTP 200.' >&2
  exit 1
fi

payload='{"district":"Centro","wasteType":"RECYCLABLE","weightKg":10,"recyclingRatePercentage":50,"collectionDate":"2026-01-01T00:00:00Z","collectorTeam":"CI","properlyDisposed":true}'
unauthenticated=$(curl -sS -o /dev/null -w '%{http_code}' -X POST \
  -H 'Content-Type: application/json' -d "$payload" "$base/api/v1/waste-collections")
[[ "$unauthenticated" == 401 ]] || {
  echo "Expected unauthenticated write HTTP 401; got $unauthenticated." >&2
  exit 1
}

authenticated=$(curl -sS -o /dev/null -w '%{http_code}' -X POST \
  -u "$APP_EDITOR_USERNAME:$APP_EDITOR_PASSWORD" \
  -H 'Content-Type: application/json' -d "$payload" "$base/api/v1/waste-collections")
[[ "$authenticated" == 201 ]] || {
  echo "Expected authenticated create HTTP 201; got $authenticated." >&2
  docker logs "$api" >&2
  exit 1
}

echo "Validated $image_ref from GHCR: readiness 200, unauthenticated write 401, authenticated create 201."
