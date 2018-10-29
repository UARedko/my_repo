resource "aws_ecr_repository" "ecr" {
  count = "${length(var.ecr_names)}"
  name = "${var.ecr_names[count.index]}"
}

resource "aws_ecr_repository_policy" "prod_pull_policy" {
  count = "${length(var.ecr_names)}"
  repository = "${element(aws_ecr_repository.ecr.*.name, count.index)}"

  policy = <<EOF
{
    "Version": "2008-10-17",
    "Statement": [
        {
            "Sid": "Prod",
            "Effect": "Allow",
            "Principal": {
                "AWS": "arn:aws:iam::${var.account_id_prod}:root"
            },
            "Action": [
                "ecr:GetDownloadUrlForLayer",
                "ecr:BatchGetImage",
                "ecr:BatchCheckLayerAvailability"
            ]
        }
    ]
}
EOF
}

resource "aws_ecr_lifecycle_policy" "clean_images" {
  count = "${length(var.ecr_names)}"
  repository = "${element(aws_ecr_repository.ecr.*.name, count.index)}"

  policy = <<EOF
{
    "rules": [
        {
            "rulePriority": 1,
            "description": "Expire images older than ${var.tag_life_period} days",
            "selection": {
                "tagStatus": "untagged",
                "countType": "sinceImagePushed",
                "countUnit": "days",
                "countNumber": ${var.tag_life_period}
            },
            "action": {
                "type": "expire"
            }
        }
    ]
}
EOF
}
