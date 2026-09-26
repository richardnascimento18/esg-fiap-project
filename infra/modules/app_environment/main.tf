locals {
  short_name = var.environment == "staging" ? "stg" : "prod"
  secrets = {
    mongo-uri       = var.mongo_connection_uri
    editor-password = random_password.editor.result
    admin-password  = random_password.admin.result
  }
}

resource "random_id" "vault" {
  byte_length = 4
}

resource "random_password" "editor" {
  length  = 32
  special = true
}

resource "random_password" "admin" {
  length  = 32
  special = true
}

resource "azurerm_user_assigned_identity" "runtime" {
  name                = "id-ecocity-${local.short_name}-runtime"
  location            = var.location
  resource_group_name = var.resource_group_name
  tags                = var.tags
}

resource "azurerm_user_assigned_identity" "deploy" {
  name                = "id-ecocity-${local.short_name}-github"
  location            = var.location
  resource_group_name = var.resource_group_name
  tags                = var.tags
}

resource "azurerm_federated_identity_credential" "github" {
  name      = "github-${var.environment}-environment"
  parent_id = azurerm_user_assigned_identity.deploy.id
  issuer    = "https://token.actions.githubusercontent.com"
  audience  = ["api://AzureADTokenExchange"]
  subject   = "repo:richardnascimento18/esg-fiap-project:environment:${var.environment}"
}

resource "azurerm_key_vault" "app" {
  name                       = "kv-ecocity-${local.short_name}-${random_id.vault.hex}"
  location                   = var.location
  resource_group_name        = var.resource_group_name
  tenant_id                  = var.tenant_id
  sku_name                   = "standard"
  soft_delete_retention_days = 7
  purge_protection_enabled   = false
  rbac_authorization_enabled = true
  tags                       = var.tags
}

resource "azurerm_role_assignment" "runtime_secrets" {
  scope                = azurerm_key_vault.app.id
  role_definition_name = "Key Vault Secrets User"
  principal_id         = azurerm_user_assigned_identity.runtime.principal_id
  principal_type       = "ServicePrincipal"
}

resource "azurerm_role_assignment" "provisioning_secrets" {
  scope                = azurerm_key_vault.app.id
  role_definition_name = "Key Vault Secrets Officer"
  principal_id         = var.provisioning_principal_id
}

# Azure RBAC data-plane grants can lag behind role assignment completion.
resource "time_sleep" "vault_rbac" {
  create_duration = "60s"
  depends_on = [
    azurerm_role_assignment.provisioning_secrets,
    azurerm_role_assignment.runtime_secrets,
  ]
}

resource "azurerm_key_vault_secret" "app" {
  for_each     = local.secrets
  name         = each.key
  value        = each.value
  key_vault_id = azurerm_key_vault.app.id
  depends_on   = [time_sleep.vault_rbac]
}

resource "azurerm_container_app_environment" "app" {
  name                       = "cae-ecocity-${local.short_name}"
  location                   = var.location
  resource_group_name        = var.resource_group_name
  log_analytics_workspace_id = var.log_analytics_workspace_id
  tags                       = var.tags
}

resource "azurerm_container_app" "app" {
  name                         = "ca-ecocity-${local.short_name}"
  container_app_environment_id = azurerm_container_app_environment.app.id
  resource_group_name          = var.resource_group_name
  revision_mode                = "Single"
  tags                         = var.tags

  identity {
    type         = "UserAssigned"
    identity_ids = [azurerm_user_assigned_identity.runtime.id]
  }

  dynamic "secret" {
    for_each = azurerm_key_vault_secret.app
    content {
      name                = secret.key
      identity            = azurerm_user_assigned_identity.runtime.id
      key_vault_secret_id = secret.value.versionless_id
    }
  }

  ingress {
    external_enabled           = true
    target_port                = 8080
    allow_insecure_connections = false
    traffic_weight {
      percentage      = 100
      latest_revision = true
    }
  }

  template {
    min_replicas = 0
    max_replicas = 1

    container {
      name   = "ecocity-esg"
      image  = var.image
      cpu    = 0.5
      memory = "1Gi"

      env {
        name  = "SPRING_DATA_MONGODB_DATABASE"
        value = var.mongo_database
      }
      env {
        name        = "SPRING_DATA_MONGODB_URI"
        secret_name = "mongo-uri"
      }
      env {
        name  = "APP_EDITOR_USERNAME"
        value = "editor"
      }
      env {
        name        = "APP_EDITOR_PASSWORD"
        secret_name = "editor-password"
      }
      env {
        name  = "APP_ADMIN_USERNAME"
        value = "admin"
      }
      env {
        name        = "APP_ADMIN_PASSWORD"
        secret_name = "admin-password"
      }
      env {
        name  = "APP_LICENSE_ALERT_CRON"
        value = "-"
      }

      startup_probe {
        transport               = "HTTP"
        path                    = "/actuator/health/liveness"
        port                    = 8080
        initial_delay           = 10
        interval_seconds        = 10
        timeout                 = 5
        failure_count_threshold = 18
      }

      liveness_probe {
        transport               = "HTTP"
        path                    = "/actuator/health/liveness"
        port                    = 8080
        initial_delay           = 45
        interval_seconds        = 30
        timeout                 = 5
        failure_count_threshold = 3
      }

      readiness_probe {
        transport               = "HTTP"
        path                    = "/actuator/health/readiness"
        port                    = 8080
        interval_seconds        = 15
        timeout                 = 5
        failure_count_threshold = 4
      }
    }
  }

  # The image is promoted by future CD; Terraform still owns every other
  # template field, including probes, resources, scaling, and env vars.
  lifecycle {
    ignore_changes = [template[0].container[0].image]
  }

  depends_on = [time_sleep.vault_rbac]
}

resource "azurerm_role_assignment" "deploy_app" {
  scope              = azurerm_container_app.app.id
  role_definition_id = var.deployment_role_definition_id
  principal_id       = azurerm_user_assigned_identity.deploy.principal_id
  principal_type     = "ServicePrincipal"
}
