output "tenant_id" {
  value = data.azurerm_client_config.current.tenant_id
}

output "subscription_id" {
  value = data.azurerm_client_config.current.subscription_id
}

output "staging_deployment_client_id" {
  value = module.staging.deployment_client_id
}

output "production_deployment_client_id" {
  value = module.production.deployment_client_id
}

output "staging_container_app_name" {
  value = module.staging.container_app_name
}

output "production_container_app_name" {
  value = module.production.container_app_name
}

output "staging_resource_group" {
  value = azurerm_resource_group.staging.name
}

output "production_resource_group" {
  value = azurerm_resource_group.production.name
}

output "staging_fqdn" {
  value = module.staging.fqdn
}

output "production_fqdn" {
  value = module.production.fqdn
}
