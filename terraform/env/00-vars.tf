variable "owner"    {}
variable "env"      {}
variable "platform" {}
variable "domain"   {}

variable "consul_address"    {}
variable "consul_datacenter" {}
variable "consul_token"      {}

variable "owner_id" {}
variable "region"   {}
variable "azs"      { type = "map" }

variable "vpc_cidr"    {}
variable "vpc_tenancy" {}

variable "mgmt_vpc_cidr"    {}
variable "mgmt_vpc_peering" {}

variable "logs_storage_peering" {}
variable "log_storage_vpc_cidr" {}

variable "sg_intg_old" { }

variable "fipa01" {}
variable "fipa02" {}

variable "sg_tonic_vpn"               {}
variable "sg_mgmt_default"            {}
variable "sg_mgmt_automation_ansible" {}
variable "sg_mgmt_monitoring_zabbix"  {}

variable "payments_cidr"        {}
variable "payments_vpc_peer_id" {}

variable "vault_token"   { default = "" }
variable "vault_address" { default = "http://vault.tonicfordev.com:8200"}
