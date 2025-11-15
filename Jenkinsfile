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
                git branch: 'main', url: 'https://github.com/rihemakkari/test.git'
            }
        }

        stage('Build') {
               sh 'bash ./mvnw clean install -DskipTests'
            }
        

        stage('Test') {
            steps {
                sh './mvnw test'
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
