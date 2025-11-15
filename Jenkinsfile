pipeline {
    agent any

    tools {
        maven 'M2_HOME'
    }

    options {
        timestamps()
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'sonar', url: 'https://github.com/rihemakkari/test.git'
            }
        }

        stage('Build') {
            steps {
                sh './mvnw clean install -DskipTests'
            }
        }

        stage('Test') {
            steps {
                sh './mvnw test'
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv(installationName: 'sq1') {
                sh './mvnw sonar:sonar -Dsonar.login=$SONAR_TOKEN -Dsonar.host.url=https://your-sonarqube-url'
            }
        } 
        }

        stage('Package') {
            steps {
                sh './mvnw package'
            }
        }

        stage('Docker Build') {
            when {
                expression { fileExists('Dockerfile') }
            }
            steps {
                sh 'docker build -t myapp:latest .'
            }
        }
    }

    post {
        always {
            archiveArtifacts artifacts: '**/target/*.jar', allowEmptyArchive: true
        }
    }
}
