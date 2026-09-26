terraform {
  backend "azurerm" {
    resource_group_name  = "rg-ecocity-tfstate"
    storage_account_name = "stecocitytf084dd9b3"
    container_name       = "tfstate"
    key                  = "ecocity.terraform.tfstate"
    use_azuread_auth     = true
    use_cli              = true
  }
}
