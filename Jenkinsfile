stages {

    stage('Checkout') {
        steps {
            git branch: 'sonar', url: 'https://github.com/rihemakkari/test.git'
        }
    }

    stage('Build') {
        steps {
            sh 'bash ./mvnw clean install -DskipTests'
        }
    }

    stage('Test') {
        steps {
            sh './mvnw test'
        }
    }

    stage('SonarQube Analysis') {
        environment {
            SONAR_TOKEN = credentials('sq1')
        }
        steps {
            sh './mvnw sonar:sonar -Dsonar.login=$SONAR_TOKEN'
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
