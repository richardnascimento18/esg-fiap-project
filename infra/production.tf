resource "azurerm_resource_group" "production" {
  name     = "rg-ecocity-prod"
  location = var.location
  tags     = merge(local.common_tags, { environment = "production" })
}

module "production" {
  source                        = "./modules/app_environment"
  environment                   = "production"
  resource_group_name           = azurerm_resource_group.production.name
  location                      = azurerm_resource_group.production.location
  tenant_id                     = data.azurerm_client_config.current.tenant_id
  provisioning_principal_id     = data.azurerm_client_config.current.object_id
  log_analytics_workspace_id    = azurerm_log_analytics_workspace.shared.id
  mongo_connection_uri          = azurerm_mongo_cluster.shared.connection_strings[0].value
  mongo_database                = "ecocity_production"
  image                         = local.image
  deployment_role_definition_id = azurerm_role_definition.app_deploy.role_definition_resource_id
  tags                          = merge(local.common_tags, { environment = "production" })

  depends_on = [azurerm_mongo_cluster_firewall_rule.azure_services]
}
