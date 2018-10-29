module "ecr" {
  source = "git::ssh://git@bitbucket.org/tonicforhealth/terraform-module-ecr.git?ref=master"

  account_id_prod = "${var.account_id_prod}"
  tag_life_period = "${var.tag_life_period}"

  ecr_names = ["authorization-app",
               "marketing-site-app",
               "myjournal-web",
               "mytonic-app",
               "webp-app",
               "oauth-app",
               "xealth-patient-profile-app",
               "xealth-patient-profile-spa-app",
               "sd-proc",
               "sd-web",
               "sd-app",
               "ssp-app",
               "workflow-reports-web",
               "workflow-reports-proc",
               "xealth-api-app",
               "workflow-reports-app",
               "myjournal-app",
               "media-server-app",
               "payments-app",
               "mongodb",
               "studies-patient-data-aggregator-app",
               "apache-drill",
               "centrifugo",
               "parameter-migration",
               "integrated-patient-profile-app",
               "integrated-patient-profile-app-base",
               "integrated-patient-profile-web",
               "survey-list",
               "appointments-dashboard-web",
               "corepoint-adapter"
               ]
}