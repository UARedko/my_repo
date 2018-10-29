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

          try {
            docker.image('postgres').withRun('--net=host -p 5432:5432') { container ->
              APP_IMAGE.inside("-u root -w='/application' --net=host") {
                sh 'ruby --version && bundle exec rake db:create && bundle exec rake db:migrate'
                sh "rspec --format RspecJunitFormatter spec"
                sh "rspec --format RspecJunitFormatter --out reports/rspec.xml spec"
                junit "reports/rspec.xml"
                sh "cat reports/rspec.xml"
                sh "bundle exec rubocop --require rubocop/formatter/checkstyle_formatter --format RuboCop::Formatter::CheckstyleFormatter --no-color --out reports/checkstyle-result.xml || true"
                step([$class: 'hudson.plugins.checkstyle.CheckStylePublisher', pattern: 'reports/checkstyle-result.xml'])
                sh "brakeman -o reports/brakeman-output.json --no-progress --no-exit-on-warn"
                publishBrakeman 'reports/brakeman-output.json'
                //Temporrary remove for dev team
                sh "bundle audit check --update"
                archiveArtifacts artifacts: "reports/rspec.xml", fingerprint: true

                if (APP_NAME == 'cloud-entity') {
                  sh "/usr/local/bundle/bin/puma -v -p 9001 &"
                  sh 'sleep 10; curl -kv http://localhost:9001/swagger_doc | python -m json.tool > swagger.json'
                  archiveArtifacts artifacts: "swagger.json", fingerprint: true
                }
              }
            }
            } catch(Exception e) {
              println "${e}"
            }
            APP_IMAGE.push()
            sh "docker tag ${ECR_IMAGE_NAME} ${DOCKER_HUB_IMAGE_NAME} && docker push ${DOCKER_HUB_IMAGE_NAME}"
          }
        }
      }

      stage("Deploy") {
        when {
          expression {
            return ( BRANCH_NAME =~ /(.*)(develop|CI)(.*)/ || params.DEPLOY_ENV)
          }
        }
        steps{
          script {
            def DEPLOY_ENV
            if (BRANCH_NAME == 'develop') {
              DEPLOY_ENV = 'dev'
            } else {
              DEPLOY_ENV = 'qa'
            }

            if (params.DEPLOY_ENV) {DEPLOY_ENV = params.DEPLOY_ENV}
            
            def APP_TAG = BRANCH_NAME.replaceAll('/', '__')

            if (BRANCH_NAME == 'master') {
              APP_TAG='latest'
            }
            build job: "application_delivery/${DEPLOY_ENV}/deploy_${APP_NAME}", 
                  parameters: [
                    string(name: 'VERSION', value: APP_TAG),
                  ]
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
