module "mytonic_appdb" {
  source = "../modules/module-rds"

  owner    = "${var.owner}"
  env      = "${var.env}"
  platform = "${var.platform}"
  type     = "mytonic"

  region   = "${var.region}"
  azs      = "${var.azs}"
  vpc_id   = "${module.vpc.vpc_id}"
  vpc_cidr = "${var.vpc_cidr}"

  subnet_count        = 3
  subnet_cidr_step    = 176
  subnet_route_table  = "${module.vpc.rt_internal}"

  sg_appdb_ingress = [
    "${var.sg_tonic_vpn}",
    "${module.mytonic_web.sg_web}",
    "${module.mytonic_api.sg_web}",
    "${module.mytonic_proc.sg_proc}",
    "${module.intg_proc.sg_proc}",
    "${module.mytonic_suport.sg_support}",
    "${module.authorization_api.sg_authorization_api}",
    "${module.sd_web.sg_web}",
    "${module.sd_proc.sg_proc}",
    "${module.oauth_api.sg_api}",
    "${module.myjournal_web.sg_web}",
    "sg-6c4d131c", # payments-web
    "${var.sg_mgmt_default}"
  ]

  db_engine_version      = "5.6.35"
  db_type                = "db.m4.large"
  db_storage_size        = 16
  db_storage_type        = "gp2"
  db_kms_key_id          = "${module.vpc.kms_rds_key_id}"
  db_username            = "${data.vault_generic_secret.db_credentials.data["mytonic_user"]}"
  db_password            = "${data.vault_generic_secret.db_credentials.data["mytonic_pwd"]}"
  db_multi_az            = false

  db_backup_retention_period = 30
  db_backup_window           = "07:00-08:00"
  db_maintenance_window      = "wed:03:48-wed:04:18"
}
