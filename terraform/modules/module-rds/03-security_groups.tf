# Security group(s)
resource "aws_security_group" "appdb" {
  name        = "${var.owner}_${var.env}_${var.platform}_${var.type}_${var.subtype}"
  description = "Allow access to ${var.owner}-${var.env}-${var.platform} ${var.type} RDS instance(s)"
  vpc_id      = "${var.vpc_id}"

  lifecycle {
    ignore_changes = ["description"]
  }

  ingress {
    from_port       = 1433
    to_port         = 1433
    protocol        = "tcp"
    security_groups = ["${var.sg_appdb_ingress}"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
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
