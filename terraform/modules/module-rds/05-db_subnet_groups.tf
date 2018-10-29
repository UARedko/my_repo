# RDS Subnet Group(s)
resource "aws_db_subnet_group" "appdb" {
  name       = "${var.owner}-${var.env}-${var.platform}-${var.type}-${var.subtype}"
  description = "${var.owner}-${var.env}-${var.platform} ${var.type} AppDB Subnet Group"
  subnet_ids = ["${aws_subnet.appdb.*.id}"]

  lifecycle {
    ignore_changes = ["name"]
  }

  tags {
    Name        = "${var.owner}_${var.env}_${var.platform}_${var.type}_${var.subtype}"
    Owner       = "${var.owner}"
    Environment = "${var.env}"
    Platform    = "${var.platform}"
    Type        = "${var.type}"
    Subtype     = "${var.subtype}"
  }
}
