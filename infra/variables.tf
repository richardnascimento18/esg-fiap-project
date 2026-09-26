variable "subscription_id" {
  description = "Azure for Students subscription to deploy into."
  type        = string
}

variable "location" {
  description = "Application resource region; DocumentDB Free Tier and subscription policy allow Canada Central."
  type        = string
  default     = "canadacentral"

  validation {
    condition     = var.location == "canadacentral"
    error_message = "All application resources must remain in canadacentral."
  }
}

variable "bootstrap_image_digest" {
  description = "Tested GHCR production manifest digest used only to bootstrap new apps. Future image promotion belongs to CD."
  type        = string
  default     = "sha256:a2c140d39ddb69dceabf50843dee5500f3ffe2f25d4efce2d3650f2743ef0569"

  validation {
    condition     = can(regex("^sha256:[0-9a-f]{64}$", var.bootstrap_image_digest))
    error_message = "Use an immutable sha256 GHCR digest."
  }
}
