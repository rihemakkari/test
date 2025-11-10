pipeline {
    agent any 

    tools {
        maven 'M2_HOME'
    }

    stages {

        stage('GIT') {
            steps {
                git branch: 'main',
                    url: 'https://github.com/rihemakkari/test',
                    credentialsId: 'jenkins-example-github-pat'
            }
        }

        stage('Maven') {
            steps {
                sh 'mvn -version'
            }
        }

    }
}
