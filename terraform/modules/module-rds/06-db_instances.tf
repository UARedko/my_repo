# RDS Instance(s)
resource "aws_db_instance" "appdb" {
  # name                       = "${var.owner}_${var.env}_${var.platform}_${var.type}_${var.subtype}"
  identifier                 = "${var.owner}-${var.env}-${var.platform}-${var.type}-${var.subtype}"
  engine                     = "${var.db_engine}"
  engine_version             = "${var.db_engine_version}"
  instance_class             = "${var.db_type}"
  snapshot_identifier        = "${var.db_snapshot_identifier}"
  allocated_storage          = "${var.db_storage_size}"
  storage_type               = "${var.db_storage_type}"
  storage_encrypted          = true
  kms_key_id                 = "${var.db_kms_key_id}"
  multi_az                   = "${var.db_multi_az}"
  publicly_accessible        = false
  vpc_security_group_ids     = ["${aws_security_group.appdb.id}"]
  db_subnet_group_name       = "${aws_db_subnet_group.appdb.id}"
  parameter_group_name       = "${aws_db_parameter_group.appdb.id}"
  apply_immediately          = true
  auto_minor_version_upgrade = true
  backup_retention_period    = "${var.db_backup_retention_period}"
  backup_window              = "${var.db_backup_window}"
  maintenance_window         = "${var.db_maintenance_window}"
  username                   = "${var.db_username}"
  password                   = "${var.db_password}"
  skip_final_snapshot        = false
  final_snapshot_identifier  = "${var.owner}-${var.env}-${var.platform}-${var.type}-${var.subtype}-final-snapshot"
  copy_tags_to_snapshot      = true
  license_model              = "${var.db_license_model}"

  lifecycle {
    ignore_changes = ["snapshot_identifier", "name", "identifier"]
  }

  tags {
    Name        = "${var.owner}_${var.env}_${var.platform}_${var.type}_${var.subtype}"
    Owner       = "${var.owner}"
    Environment = "${var.env}"
    Platform    = "${var.platform}"
    Identifier  = "${var.owner}-${var.env}-${var.platform}"
    Type        = "${var.type}"
    Subtype     = "${var.subtype}"
  }
}
