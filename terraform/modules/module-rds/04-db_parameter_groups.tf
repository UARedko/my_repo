# RDS Parameter Group(s)
resource "aws_db_parameter_group" "appdb" {
  name        = "${var.owner}-${var.env}-${var.platform}-${var.type}-${var.subtype}"
  description = "${var.owner}-${var.env}-${var.platform} ${var.type} AppDB Parameter Group"
  family      = "${var.db_parameter_group_family}"

  lifecycle {
    ignore_changes = ["name", "description"]
  }

//  parameter {
//    name  = "character_set_server"
//    value = "utf8"
//  }
//
//  parameter {
//    name  = "character_set_client"
//    value = "utf8"
//  }

  # parameter {
  #   name  = "log_bin_trust_function_creators"
  #   value = "${var.db_parameter_log_bin_trust_function_creators}"
  # }

  # parameter {
  #   name  = "slow_query_log"
  #   value = "${var.db_parameter_slow_query_log}"
  # }

  # parameter {
  #   name  = "general_log"
  #   value = "${var.db_parameter_general_log}"
  # }

  tags {
    Name        = "${var.owner}_${var.env}_${var.platform}_${var.type}_${var.subtype}"
    Owner       = "${var.owner}"
    Environment = "${var.env}"
    Platform    = "${var.platform}"
    Type        = "${var.type}"
    Subtype     = "${var.subtype}"
  }
}
