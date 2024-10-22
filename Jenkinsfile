#!groovy
@Library('ICANN_LIB') _

properties([[$class: 'BuildDiscarderProperty', strategy: [$class: 'LogRotator', artifactDaysToKeepStr: '14', artifactNumToKeepStr: '10', daysToKeepStr: '14', numToKeepStr: '10']]])

GString createChartVersion(chartFolder, utils) {
    GString chartVersionCommand = "grep -m1 '^[[:space:]]*version:' ${chartFolder}/Chart.yaml | awk -F ':' '{print \$2}' | sed 's/^[[:space:]]*//'"
    def chartVersion = sh(script: chartVersionCommand, returnStdout: true).trim()
    def tag = utils.generateDockerTag()
    GString commitVersion = "${chartVersion}-${tag}"
    return commitVersion
}

def customPushHelmChart(Map map = [:]) {
    withCredentials([usernamePassword(credentialsId: 'artifactory', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
        sh "helm push-artifactory ${map['directory']} https://artifactory.icann.org/artifactory/icann-helm-local --path ${map['artifactoryPath']} --skip-reindex --username $USERNAME --password $PASSWORD --version ${map['customVersion']}"
    }
}

node('docker') {

    def utils = new icann.Utilities()

    try {
        utils.notifyBuild("STARTED",'ts-eng-builds')

        stage('Environment') {
             checkout scm
        }

        stage('Run Tests') {
            utils.mvn(args : 'clean install', jdkVersion: 'jdk21')
        }

        stage('SonarQube Analysis') {
            withSonarQubeEnv('ICANN') {
                utils.mvn(args: 'sonar:sonar -Dsonar.java.source=21 -Dsonar.java.target=21', jdkVersion: 'jdk21')
            }
        }

        stage('Publish C4') {
            if("${env.BRANCH_NAME}" != 'master' ) {
               echo "Feature branch not publishing c4 models"
               return
            }
            if(utils.hasFolderChanged("docs/diagrams")) {
              utils.buildC4Diagram(path: 'docs/diagrams', pillar: 'ts')
            }
        }

        stage('Docker Image') {
            withEnv(["IMAGE_BUILDER=docker"]) {
              utils.mvn(args: 'clean package spring-boot:repackage -Dmaven.test.skip=true', jdkVersion: 'jdk21')
            sh "cd ${WORKSPACE}/ && mvn -Ddocker.username=dockerpull -Ddocker.password=${utils.getUserPassword("dockerpull")} docker:build -Dmaven.test.skip=true"
              utils.pushImageToRegistryTrunkBased(DTRRepo : 'rdapconformancefe', dockerImageName : 'icann/rdapconformancefe')
            }
        }

        stage('Trigger Webhook') {
            try {
                if ("${env.BRANCH_NAME}" == 'master') {
                    imageTag = utils.generateDockerTag('dev');
                    artifacts = [
                        [
                            "type"     : "docker/image",
                            "name"     : "container-registry-dev.icann.org/icann/rdapconformancefe",
                            "version"  : imageTag,
                            "reference": "container-registry-dev.icann.org/icann/rdapconformancefe:${imageTag}"
                        ]
                    ]

                    utils.spinnakerTrigger(webhook: 'rdapconformance-qa', artifacts: artifacts)
                } else {
                     echo "not master branch - skipping"
                }
            } catch (e) {
                utils.sendNotification(slackChannel: 'ts-eng-builds', sendSlackMessage: true, buildStatus: 'FAILED')
                throw e
            }
        }

        stage('Helm Charts') {
            if("${env.BRANCH_NAME}" != 'master' ) {
               echo "Feature branch not deploying helm charts"
               return
            }
            if(utils.hasFolderChanged("helm/rdap-conformance-tool-fe")) {
               GString chartVersion = createChartVersion("helm/rdap-conformance-tool-fe", utils)
               echo "Deploying helm chart ${chartVersion}"
               customPushHelmChart(directory: "helm/rdap-conformance-tool-fe", artifactoryPath: "icann/rdap-conformance-tool-fe", customVersion: chartVersion)
            }
        }

    } catch(e) {
        currentBuild.result = "FAILURE"
        throw e

    } finally {
        step([$class: 'Publisher'])
        utils.notifyBuild(currentBuild.result,'ts-eng-builds')
    }
}
