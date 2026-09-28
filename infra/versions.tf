terraform {
  required_version = ">= 1.15.0, < 2.0.0"

  required_providers {
    azapi = {
      source  = "Azure/azapi"
      version = "= 2.12.0"
    }
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "~> 4.0"
    }
    random = {
      source  = "hashicorp/random"
      version = "~> 3.0"
    }
    time = {
      source  = "hashicorp/time"
      version = "~> 0.13"
    }
  }
}
