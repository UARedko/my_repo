@Library('sharedLibrary') _

deployMicroservice(
  APP_NAME:           'cloud-locations',
  CREDENTIALS_ID:     'github_tracker',
  BRANCH:             'master',
  ENVIRONMENT_DIR:    'qa',
  AWS_CREDENTIALS:    'devqa_account_aws_credentials',
  AWS_DEFAULT_REGION: 'us-east-1',
  REPOSITORY:         'https://github.com/TrackRbyPhoneHalo/it-fs-terraform-env-product.git',
  AWS_ACCOUNT_ID:     '363309569879',
  DISABLE_APPLY_APPROVAL: true,
)
