pipeline {
    agent any
    tools {
        maven 'M2_HOME'  // Must match the Maven name in Jenkins global tool config
    }
    stages {
        stage('Checkout') {
            steps {
                git branch: 'sonar', 
                    url: 'https://github.com/rihemakkari/test.git', 
                    credentialsId: 'jenkins-example-github-pat'
            }
        }
        stage('Build') {
            steps {
                sh 'mvn clean install'
            }
        }
        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQube') { // Must match your Jenkins SonarQube server name
                    sh "mvn sonar:sonar -Dsonar.projectKey=myproject -Dsonar.host.url=http://192.168.56.10:9000 -Dsonar.login=squ_01c9a1a1438da5901b8452fb3b4a6b3da0c20c5c"
                }
            }
        }
    }
}
