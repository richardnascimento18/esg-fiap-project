locals {
  common_tags = {
    project    = "ecocity-esg"
    managed-by = "terraform"
    purpose    = "academic-devops"
  }

  image = "ghcr.io/richardnascimento18/esg-fiap-project@${var.bootstrap_image_digest}"
}
