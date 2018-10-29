variable "ecr_names" { type    = "list"
                       default = ["ecr-list"
                                  ]
                                }

variable "account_id_prod" { default = "" }
variable "tag_life_period" { default = 180 }
