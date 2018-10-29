variable "owner"    { default = "tonic" }
variable "env"      { default = "prod"  }
variable "platform" { default = "main"  }
variable "type"     { }
variable "subtype"  { default = "appdb" }

variable "region"   { }
variable "azs"      { type = "map" }
variable "vpc_id"   { }
variable "vpc_cidr" { }

variable "subnet_count"        { default = 3 }
variable "subnet_cidr_newbits" { default = 12 }
variable "subnet_cidr_factor"  { default = 16 }
variable "subnet_cidr_step"    { }
variable "subnet_route_table"  { }

variable "sg_appdb_ingress" { type = "list" }

variable "db_parameter_group_family"                    { default = "mysql5.6" }
variable "db_parameter_log_bin_trust_function_creators" { default = "1" }
variable "db_parameter_slow_query_log"                  { default = "1" }
variable "db_parameter_general_log"                     { default = "0" }

variable "db_engine"              { default = "mysql" }
variable "db_engine_version"      { default = "5.6.35" }
variable "db_type"                { default = "db.t2.small" }
variable "db_snapshot_identifier" { default = "" }
variable "db_storage_size"        { default = 5 }
variable "db_storage_type"        { default = "gp2" }
variable "db_kms_key_id"          { }
variable "db_username"            { default = "root" }
variable "db_password"            { }
variable "db_multi_az"            { default = true }

variable "db_backup_retention_period" { default = 31 }
variable "db_backup_window" { default = "04:20-04:50" }
variable "db_maintenance_window" { default = "Mon:05:00-Mon:06:00" }

variable "db_license_model" { default = "general-public-license" }
