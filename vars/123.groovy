pipeline {
    agent {
        node {
            label 'AGENT-1'
        }
    }
    environment { 
        packageVersion = '' 
        // nexusurl is maintained in pipelineGlobals 
        // nexusURL = '172.31.6.198:8081'      
    }
    options {
        ansiColor('xterm')
        timeout(time: 1, unit: 'HOURS')
        disableConcurrentBuilds()
    }

    // build
    stages {
        
        stage('Install dependencies') {
            steps {
                sh """
                    npm install
                """
            }
        }
        stage('Unit tests') {
            steps {
                sh """
                    echo "unit tests will run here"
                """
            }
        }
        stage('Sonar Scan') {
            steps {
                sh """
                    echo "to run scan need to use this coomand sonar-scanner"
                    echo "usually command here is sonar-scanner"
                    echo "sonar scan will run here"
                """
            }
        }
        stage('Build') {
            steps {
                sh """
                    ls -la
                    zip -q -r ${component}.zip ./* -x ".git" -x "*.zip"
                    ls -ltr
                """
            }
        }
        stage('Publish Artifact') {
            steps {
                nexusArtifactUploader(
                    nexusVersion: 'nexus3',
                    protocol: 'http',
                    nexusUrl: pipelineGlobals.nexusURL(),
                    groupId: 'com.roboshop',
                    version: "${packageVersion}",
                    repository: "${component}",
                    credentialsId: 'nexus-auth',
                    artifacts: [
                        [artifactId:"${component}",
                        classifier: '',
                        file: "${component}.zip",
                        type: 'zip']
                    ]
                )
            }
        }
        stage('Deploy') {
            when {
                expression {
                    params.Deploy == 'true'
                }
            }
            steps {
                script {
                        def params = [
                            string(name: 'version', value: "$packageVersion"),
                            string(name: 'environment', value: "dev")
                        ]
                        build job: "${component}-deploy", wait: true, parameters: params
                }
            }
        }
    }
    // post build
    post { 
        always { 
            echo 'I will always say Hello again!'
            // remove workspace folder in pipleline 
            // deleteDir()
        }
        failure { 
            echo 'this runs when pipeline is failed, used generally to send some alerts'
        }
        success{
            echo 'I will say Hello when pipeline is success'
        }
    }
}