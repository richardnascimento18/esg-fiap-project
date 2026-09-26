resource "azurerm_resource_group" "shared" {
  name     = "rg-ecocity-shared"
  location = var.location
  tags     = merge(local.common_tags, { environment = "shared" })
}

resource "azurerm_log_analytics_workspace" "shared" {
  name                = "log-ecocity-shared"
  location            = azurerm_resource_group.shared.location
  resource_group_name = azurerm_resource_group.shared.name
  sku                 = "PerGB2018"
  retention_in_days   = 30
  daily_quota_gb      = 1
  tags                = merge(local.common_tags, { environment = "shared" })
}

resource "random_id" "mongo" {
  byte_length = 4
}

resource "random_password" "mongo_admin" {
  length  = 32
  special = false # Keep generated credentials URI-safe; entropy remains high.
}

resource "azurerm_mongo_cluster" "shared" {
  name                   = "mongo-ecocity-${random_id.mongo.hex}"
  location               = azurerm_resource_group.shared.location
  resource_group_name    = azurerm_resource_group.shared.name
  administrator_username = "ecocityadmin"
  administrator_password = random_password.mongo_admin.result
  authentication_methods = ["NativeAuth"]
  compute_tier           = "Free"
  version                = "8.0"
  storage_size_in_gb     = 32
  shard_count            = 1
  high_availability_mode = "Disabled"
  public_network_access  = "Enabled"
  tags                   = merge(local.common_tags, { environment = "shared" })
}

# Azure DocumentDB represents the Azure-services exception as the single
# 0.0.0.0 address. It is distinct from the all-Internet 0.0.0.0–255.255.255.255
# range. It still admits services in other subscriptions, so native auth is vital.
resource "azurerm_mongo_cluster_firewall_rule" "azure_services" {
  name             = "allow-azure-services"
  mongo_cluster_id = azurerm_mongo_cluster.shared.id
  start_ip_address = "0.0.0.0"
  end_ip_address   = "0.0.0.0"
}
