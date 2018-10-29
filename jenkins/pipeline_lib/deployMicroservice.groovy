/**
 * Setup Infrastructure with Terraform pipeline template
 */

def call(Map tplParams) {
  pipeline {
    agent {
        label 'tools'
    }

    options {
      timeout(time: 20, unit: 'MINUTES')
      buildDiscarder(logRotator(numToKeepStr: '60'))
      disableConcurrentBuilds()
      ansiColor('xterm')
    }
   
    parameters {
      string(      name: 'VERSION',        defaultValue: '', description: 'f.e.:latest')
      booleanParam(name: 'UPDATE_AWS_SM',  defaultValue: true, description: '')
      booleanParam(name: 'DEPLOY',         defaultValue: true, description: '')      
      booleanParam(name: 'RUN_MIGRATIONS', defaultValue: true, description: '')

    }

    environment {
      AWS_CREDENTIALS    = "${tplParams.AWS_CREDENTIALS}"
      AWS_DEFAULT_REGION = "${tplParams.AWS_DEFAULT_REGION}"
      AWS_ACCOUNT_ID     = "${tplParams.AWS_ACCOUNT_ID}"
      APP_NAME           = "${tplParams.APP_NAME}"
      ENVIRONMENT_DIR    = "${tplParams.ENVIRONMENT_DIR}"
      DEPLOY_VERSION     = "${params.VERSION}"
    }

    stages {
      stage('Update Secrects Manager version'){
        when {
          expression {
            return ( params.UPDATE_AWS_SM )
          }
        }        
        steps{
          dir('code') {
            withAWS(region: AWS_DEFAULT_REGION, credentials: AWS_CREDENTIALS){
              checkoutAnsibleBranch(BRANCH: 'master')
              runAnsiblePlaybook(
                PLAYBOOK:          'foundational_services/aws_secret',
                account_shorthand: 'DevQA',
                  env:             'FSDevQA',
                  service:         'FS',
                  CREDENTIALS_ID:  'ansible_ssh',
                  EXTRA_VARS:      [SECRET_VALUES: "{\"image\":\"${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_DEFAULT_REGION}.amazonaws.com/${APP_NAME}\", \"tag\":\"${DEPLOY_VERSION}\"}",
                                    SECRET_PATH:   "product/${ENVIRONMENT_DIR}/${APP_NAME}/docker_image"],
              )
            }          
          }
        }
      }

      stage('Clone repo'){
        when {
          expression {
            return ( params.DEPLOY )
          }
        }        
        steps {
          dir('code') {
            git url: tplParams.REPOSITORY, branch: tplParams.BRANCH, credentialsId: tplParams.CREDENTIALS_ID
          }
        }
      }

      stage('Terraform get'){
        when {
          expression {
            return ( params.DEPLOY )
          }
        }         
        steps {
          dir('code') {
            withAWS(region: AWS_DEFAULT_REGION, credentials: AWS_CREDENTIALS){
              sh 'terraform --version'
              sh 'terraform get -update=true'
            }
          }
        }
      }

      stage('Terraform init'){
        when {
          expression {
            return ( params.DEPLOY )
          }
        }         
        steps {
          dir('code') {
            ansiColor('xterm'){
              script{
                withAWS(region: AWS_DEFAULT_REGION, credentials: AWS_CREDENTIALS){
                  sh "terraform init -backend-config=envs/${ENVIRONMENT_DIR}/backend.conf"
                }
              }
            }
          }
        }
      }

      stage('Terraform plan'){
        when {
          expression {
            return ( params.DEPLOY )
          }
        }         
        steps {
          dir('code'){
            script {
              withAWS(region: AWS_DEFAULT_REGION, credentials: AWS_CREDENTIALS){
                sh "terraform plan -var-file=envs/${ENVIRONMENT_DIR}/terraform.tfvars -target=module.${APP_NAME} -out=../terraform_plan_${BUILD_NUMBER}.state | \
                tee ../terraform_plan_${BUILD_NUMBER}.log"
              }
            }
          }
        }
      }
    
      stage('Confirmation'){
        when {
          expression {
            return ( !tplParams.DISABLE_APPLY_APPROVAL && params.DEPLOY )
          }
        }
        steps { 
          notifySlack(
            STATUS:     'notify',
            CUSTOM_MSG: 'Terraform infrastructure changes were requested by your commit. Please check changes and approve or deny them!'
          )           
          input message: 'Are your ready for "terraform apply"?'
        }
      }

      stage('Terraform apply') {
        when {
          expression {
            return ( params.DEPLOY )
          }
        }            
        steps {
          dir('code') {
            withAWS(region: AWS_DEFAULT_REGION, credentials: AWS_CREDENTIALS){
              sh "terraform apply ../terraform_plan_${BUILD_NUMBER}.state"
            }
          }
        }
      }
      stage('Run migrations') {
        when {
          expression {
            return ( params.RUN_MIGRATIONS )
          }
        }            
        steps {
          dir('code') {
            script {
            withAWS(region: AWS_DEFAULT_REGION, credentials: AWS_CREDENTIALS){
              ECS_CLUSTER    = sh(returnStdout: true, script: "terraform output ecs-cluster-name").trim().toString()
              def MIGRATION_TASK
              try {
                MIGRATION_TASK = sh(returnStdout: true, script: "terraform output ${APP_NAME}-migration-task").trim().toString()
              } catch(Exception e) {
                println "No migration task was found in terrafrom output. Exception ${e}."
              }
              if (MIGRATION_TASK) {
              checkoutAnsibleBranch(BRANCH: 'master')  
              runAnsiblePlaybook(
                PLAYBOOK:          'product/ecs_task_execution',
                account_shorthand: 'DevQA',
                env:               'FSDevQA',
                service:           'FS',
                CREDENTIALS_ID:    'ansible_ssh',
                EXTRA_VARS: [
                  CLUSTER: ECS_CLUSTER,
                  TASK_DEFINITION: MIGRATION_TASK,
                ]
              )} else {
                 println "Skip migration because migration task was found in terrafrom output"
              }              
            }
          }
          }
        }
      }      
    }

    post {
      success {
        notifySlack(STATUS: 'success')   
      }
      failure {
        notifySlack(STATUS: 'failure')   
      }
      cleanup { 
        cleanWs()
      }            
    } 
  }
}