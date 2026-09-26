output "deployment_client_id" {
  value = azurerm_user_assigned_identity.deploy.client_id
}

output "container_app_name" {
  value = azurerm_container_app.app.name
}

output "fqdn" {
  value = azurerm_container_app.app.ingress[0].fqdn
}
