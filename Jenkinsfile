@Library('ICANN_LIB') _

properties([[$class: 'BuildDiscarderProperty', strategy: [$class: 'LogRotator', artifactDaysToKeepStr: '14', artifactNumToKeepStr: '10', daysToKeepStr: '14', numToKeepStr: '10']]])

node('docker') {

    def utils = new icann.Utilities()

    try {
        utils.notifyBuild("STARTED",'ts-eng-builds')

        stage('Environment') {
             checkout scm
        }

        stage('Run Tests') {
            utils.mvn(args : 'clean install', jdkVersion: 'jdk11')
        }

        stage('SonarQube Analysis') {
            withSonarQubeEnv('ICANN') {
                utils.mvn(sonarAnalysis: true)
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
              utils.mvn(args: 'clean package spring-boot:repackage -Dmaven.test.skip=true', jdkVersion: 'jdk11')
            sh "cd ${WORKSPACE}/ && mvn -Ddocker.username=dockerpull -Ddocker.password=${utils.getUserPassword("dockerpull")} docker:build -Dmaven.test.skip=true"
              utils.pushImageToRegistryTrunkBased(DTRRepo : 'rdapconformancefe', dockerImageName : 'icann/rdapconformancefe')
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
