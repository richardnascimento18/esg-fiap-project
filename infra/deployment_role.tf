# One reusable role definition; permissions exist only where assigned below.
# The two app-scoped assignments are in the environment module.
resource "azurerm_role_definition" "app_deploy" {
  name        = "EcoCity Container App Image Deployer"
  scope       = "/subscriptions/${var.subscription_id}"
  description = "Inspect and update an existing EcoCity Container App and inspect its revisions."

  permissions {
    actions = [
      "Microsoft.App/containerApps/read",
      "Microsoft.App/containerApps/write",
      "Microsoft.App/containerApps/revisions/read",
    ]
    # Exact allowlist above prevents these actions; record the exclusions too.
    not_actions = [
      "Microsoft.App/containerApps/listSecrets/action",
      "Microsoft.App/containerApps/delete",
    ]
  }

  assignable_scopes = ["/subscriptions/${var.subscription_id}"]
}
