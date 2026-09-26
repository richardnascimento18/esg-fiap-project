variable "environment" {
  type = string
  validation {
    condition     = contains(["staging", "production"], var.environment)
    error_message = "Environment must be staging or production."
  }
}

variable "resource_group_name" { type = string }
variable "location" { type = string }
variable "tenant_id" { type = string }
variable "provisioning_principal_id" { type = string }
variable "log_analytics_workspace_id" { type = string }
variable "mongo_connection_uri" {
  type      = string
  sensitive = true
}
variable "mongo_database" { type = string }
variable "image" { type = string }
variable "tags" { type = map(string) }
