# Add data to Consul
resource "consul_key_prefix" "appdb" {
  path_prefix = "aws/vpc/${var.owner}/${var.env}/${var.platform}/${var.type}/rds/${var.subtype}/"

  subkeys = {
    "address"           = "${aws_db_instance.appdb.address}"
    "allocated_storage" = "${aws_db_instance.appdb.allocated_storage}"
    "availability_zone" = "${aws_db_instance.appdb.availability_zone}"
    "db_name"           = "${aws_db_instance.appdb.name}"
    "endpoint"          = "${aws_db_instance.appdb.endpoint}"
    "engine"            = "${aws_db_instance.appdb.engine}"
    "engine_version"    = "${aws_db_instance.appdb.engine_version}"
    "kms_key_id"        = "${aws_db_instance.appdb.kms_key_id}"
    "port"              = "${aws_db_instance.appdb.port}"
    "storage_encrypted" = "${aws_db_instance.appdb.storage_encrypted}"
    "storage_type"      = "${aws_db_instance.appdb.storage_type}"
  }
}
