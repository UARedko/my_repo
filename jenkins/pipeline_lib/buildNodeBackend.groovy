/**
 * Build Docker image cloud-entity app
 */

def call(Map templateParams) {
  pipeline {

    agent { label 'tools' }

    options {
      timeout(time: 60, unit: 'MINUTES')
      buildDiscarder(logRotator(numToKeepStr: '30'))
      disableConcurrentBuilds()
      ansiColor('xterm')
    }

    triggers {
      githubPush()
    }

    parameters {
      string(name: 'ECR_REPO',        defaultValue: templateParams.ECR_REPO,        description: 'f.e.: 3514976908.dkr.ecr.us-west-2.amazonaws.com')
      string(name: 'AWS_REGION',      defaultValue: templateParams.ECR_REGION,      description: 'f.e.: us-west-2')
      string(name: 'DOCKER_HUB_REPO', defaultValue: templateParams.DOCKER_HUB_REPO, description: 'thetrackr')
      choice(name: 'DEPLOY_ENV', 
             choices:
             '\n'+ 
             'qa\n'+
             'dev'
             )
    }

    environment {
      APP_NAME              = "${templateParams.APP_NAME}"
      AWS_ACCESS_KEY_ID     = credentials('AWS_ACCESS_KEY_ID')
      AWS_SECRET_ACCESS_KEY = credentials('AWS_SECRET_ACCESS_KEY')
      AWS_DEFAULT_REGION    = "${params.AWS_REGION}"
      ECR_REPO              = "${params.ECR_REPO}"
      DOCKER_HUB_REPO       = "${params.DOCKER_HUB_REPO}"
      DOCKER_HUB_CRED       = credentials('dockerhub_thetrackr')
      RACK_ENV              = 'test'
      AWS_ACCOUNT_SHORTHAND = "${templateParams.AWS_ACCOUNT_SHORTHAND}"
    }

    stages {
      stage ('Login to ECR') {
        steps {
          sh '$(aws ecr get-login --no-include-email)'
        }
      }

      stage ('Release notes') {
        steps {
          releaseNotes()
        }
      }

      stage ('Login to Docker Hub') {
        steps {
          sh "docker login --username ${DOCKER_HUB_CRED_USR} --password ${DOCKER_HUB_CRED_PSW}"
        }
      }

      stage("Build docker image") {
        steps{
          script {
            def APP_TAG = BRANCH_NAME.replaceAll('/', '__')

            if (BRANCH_NAME == 'master') {
              APP_TAG='latest'
            }

            echo "BRANCH_NAME: ${BRANCH_NAME}; TAG: ${APP_TAG}"

            def ECR_IMAGE_NAME="${ECR_REPO}/${APP_NAME}:${APP_TAG}"
            def DOCKER_HUB_IMAGE_NAME="${DOCKER_HUB_REPO}/${APP_NAME}:${APP_TAG}"
            def APP_IMAGE=docker.build(ECR_IMAGE_NAME)

            APP_IMAGE.push()
            sh "docker tag ${ECR_IMAGE_NAME} ${DOCKER_HUB_IMAGE_NAME} && docker push ${DOCKER_HUB_IMAGE_NAME}"
          }
        }
      }

      stage("Deploy") {
        when {
          expression {
            return ( BRANCH_NAME =~ /(.*)(master|is|IS)(.*)/ || params.DEPLOY_ENV)
          }
        }        
        steps{
          script {
            def DEPLOY_ENV
            if (BRANCH_NAME == 'master') {
              DEPLOY_ENV = 'dev'
            } else {
              DEPLOY_ENV = 'qa'
            }

            if (params.DEPLOY_ENV) {DEPLOY_ENV = params.DEPLOY_ENV}

            def APP_TAG = BRANCH_NAME.replaceAll('/', '__')

            if (BRANCH_NAME == 'master') {
              APP_TAG='latest'
            }

            if (DEPLOY_ENV) {
              try {
                build job: "application_delivery/${DEPLOY_ENV}/deploy_${APP_NAME}", 
                      parameters: [
                        string(name: 'VERSION', value: APP_TAG),
                      ]
              } catch (Exception e) {
                println "Failed to deploy service! Reason ${e.message}"
              }
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
        sh "sudo chmod -R 777 ${WORKSPACE}"
        cleanWs()
      }
    }
  }
}
