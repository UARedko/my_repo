/**
 * Build Docker image from Git repository pipeline template
 */

def call(Map templateParams) {
  pipeline {

    agent { label 'tools' }

    options {
      timeout(time: 60, unit: 'MINUTES')
      buildDiscarder(logRotator(numToKeepStr: '100'))
    }

    triggers {
      githubPush()
    }

    parameters {
      string(name: 'REPOSITORY',     defaultValue: templateParams.REPOSITORY, description: 'f.e.: git@github.com:TrackRbyPheHalo/FoundationalServices.git')
      string(name: 'BRANCH',         defaultValue: templateParams.BRANCH,     description: 'GitHub branch')
      string(name: 'APP_NAME',       defaultValue: templateParams.APP_NAME,   description: 'f.e: my_app')
      string(name: 'DOCKERFILE_DIR', defaultValue: templateParams.DOCKERFILE_DIR, description: 'Path to the Dockerfile dir')

      string(name: 'ECR_REPO',        defaultValue: templateParams.ECR_REPO,        description: 'f.e.: 3514976908.dkr.ecr.us-west-2.amazonaws.com')
      string(name: 'DOCKER_HUB_REPO', defaultValue: templateParams.DOCKER_HUB_REPO, description: 'f.e.: project_x')
      string(name: 'ECR_REGION',      defaultValue: templateParams.ECR_REGION,      description: 'ex: us-west-2')      
    }

    environment {
      AWS_DEFAULT_REGION    = "${params.ECR_REGION}"
      DOCKER_HUB_REPO       = "${params.DOCKER_HUB_REPO}"
      DOCKER_HUB_CRED       = credentials('dockerhub_thetrackr')
      AWS_CREDENTIALS       = "${templateParams.AWS_CREDENTIALS}"

    }

    stages {
      stage ('Login to ECR') {
        steps { 
          withAWS(credentials: AWS_CREDENTIALS) {
            sh '$(aws ecr get-login --no-include-email)' 
          }
        }
      }

      stage ('Login to Docker Hub') {
        steps { sh "docker login --username ${DOCKER_HUB_CRED_USR} --password ${DOCKER_HUB_CRED_PSW}" }
      }

      stage ('Checkout') {
        steps {
          dir('code') {
            checkout([
              $class: 'GitSCM', branches: [[ name: params.BRANCH ]],
              userRemoteConfigs: [[ url: params.REPOSITORY, credentialsId: templateParams.CREDENTIALS_ID ]]
            ])
          }
        }
      }
      
      stage('Build/Push docker image'){
        steps{
          withAWS(region: AWS_DEFAULT_REGION, credentials: AWS_CREDENTIALS){
          dir("code/${params.DOCKERFILE_DIR}") {
            buildDockerStep(
              APP_NAME: params.APP_NAME,
              BRANCH: params.BRANCH,
              ECR_REGISTRY: params.ECR_REPO ,
              DOCKER_HUB_REGISTRY: DOCKER_HUB_REPO,
              PROMOTE: true
            )
          }
        }
        }
      }
    }
    post {
      failure {
        notifySlack(STATUS: 'failure')
      }
      cleanup { 
        cleanWs()
      }      
    }
  }
}
