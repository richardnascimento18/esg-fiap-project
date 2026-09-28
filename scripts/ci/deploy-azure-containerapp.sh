#!/usr/bin/env bash
set -Eeuo pipefail

environment=${1:?Pass the GitHub environment}
resource_group=${2:?Pass the resource group}
app=${3:?Pass the Container App name}
image=${4:?Pass the immutable GHCR image}
source_sha=${5:?Pass the staging source commit}

[[ "$environment" == staging || "$environment" == production ]]
[[ "$image" =~ ^ghcr\.io/[a-z0-9._/-]+@sha256:[0-9a-f]{64}$ ]]
[[ "$source_sha" =~ ^[0-9a-f]{40}$ ]]

# az rest uses the Azure CLI session established by azure/login. This fixed API
# version and a full template PATCH require Container App read/write only.
api_version=2025-01-01
subscription_id=$(az account show --query id --output tsv)
[[ "$subscription_id" =~ ^[0-9a-fA-F-]{36}$ ]] || { echo 'Azure OIDC login did not provide a subscription.' >&2; exit 1; }
resource_id="/subscriptions/$subscription_id/resourceGroups/$resource_group/providers/Microsoft.App/containerApps/$app"
url="https://management.azure.com$resource_id?api-version=$api_version"
umask 077
workdir=$(mktemp -d)
trap 'rm -rf "$workdir"' EXIT

az rest --method get --url "$url" --output json > "$workdir/before.json"
jq -e --arg image "$image" '
  if (.location | type) == "string" and (.location | length) > 0 and
     (.properties.template.containers | length) == 1 and
     .properties.template.containers[0].name == "ecocity-esg"
  then {location: .location, properties: {template: (.properties.template | .containers[0].image = $image)}}
  else error("Expected a location and exactly one ecocity-esg container in the current template") end
' "$workdir/before.json" > "$workdir/patch.json"

# The entire current template is carried forward, including env, probes,
# resources, volumes, and scale settings. Configuration and identity are omitted
# from the JSON Merge Patch, so they stay untouched.
az rest --method patch --url "$url" \
  --headers 'Content-Type=application/json' \
  --body "@$workdir/patch.json" --output none

provisioning_state=unknown
actual_image=unknown
fqdn=
for _ in {1..60}; do
  az rest --method get --url "$url" --output json > "$workdir/after.json"
  provisioning_state=$(jq -r '.properties.provisioningState // "unknown"' "$workdir/after.json")
  actual_image=$(jq -r '.properties.template.containers[] | select(.name == "ecocity-esg") | .image' "$workdir/after.json")
  if [[ "$provisioning_state" == Succeeded && "$actual_image" == "$image" ]]; then
    fqdn=$(jq -r '.properties.configuration.ingress.fqdn // empty' "$workdir/after.json")
    break
  fi
  if [[ "$provisioning_state" == Failed || "$provisioning_state" == Canceled ]]; then
    echo "Container App provisioning ended in $provisioning_state." >&2
    exit 1
  fi
  sleep 5
done
[[ "$provisioning_state" == Succeeded && "$actual_image" == "$image" ]] || {
  echo "Container App did not reach Succeeded with the expected image: state=$provisioning_state image=$actual_image" >&2
  exit 1
}
[[ "$fqdn" =~ ^[a-zA-Z0-9.-]+$ ]] || { echo 'Container App ingress FQDN is missing or invalid.' >&2; exit 1; }
base="https://$fqdn"

readiness=000
for _ in {1..60}; do
  readiness=$(curl --silent --show-error --max-time 10 --output /dev/null \
    --write-out '%{http_code}' "$base/actuator/health/readiness" || true)
  [[ "$readiness" == 200 ]] && break
  sleep 5
done
[[ "$readiness" == 200 ]] || { echo "Readiness returned HTTP $readiness after bounded retries." >&2; exit 1; }

liveness=$(curl --silent --show-error --max-time 10 --output /dev/null \
  --write-out '%{http_code}' "$base/actuator/health/liveness")
[[ "$liveness" == 200 ]] || { echo "Liveness returned HTTP $liveness." >&2; exit 1; }

unauthenticated=$(curl --silent --show-error --max-time 10 --output /dev/null \
  --write-out '%{http_code}' --request POST \
  --header 'Content-Type: application/json' --data '{}' \
  "$base/api/v1/waste-collections")
[[ "$unauthenticated" == 401 ]] || {
  echo "Unauthenticated protected POST returned HTTP $unauthenticated." >&2
  exit 1
}

printf '### Azure Container App deployment\n\n- Environment: `%s`\n- Staging source commit: `%s`\n- Immutable image: `%s`\n- Expected digest: `%s`\n- Actual Azure image: `%s`\n- Provisioning state: `%s`\n- Readiness: HTTP %s\n- Liveness: HTTP %s\n- Unauthenticated write: HTTP %s\n- Deployed-image equality: **true**\n' \
  "$environment" "$source_sha" "$image" "${image##*@}" "$actual_image" \
  "$provisioning_state" "$readiness" "$liveness" "$unauthenticated" >> "$GITHUB_STEP_SUMMARY"
