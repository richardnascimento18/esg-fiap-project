locals {
  common_tags = {
    project    = "ecocity-esg"
    managed-by = "terraform"
    purpose    = "academic-devops"
  }

  image = "ghcr.io/richardnascimento18/esg-fiap-project@${var.bootstrap_image_digest}"

  github_oidc_repository_subject = "richardnascimento18@105979997/esg-fiap-project@1360283715"
}
