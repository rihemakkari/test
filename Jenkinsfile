pipeline {
    agent any
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
                // Use Maven wrapper if present, otherwise fallback to mvn
                sh './mvnw clean install || mvn clean install'
            }
        }

        stage('SonarQube Analysis') {
            steps {
                // Make sure the installation name matches your SonarQube server in Jenkins
                withSonarQubeEnv(installationName: 'sq1') {
                    sh './mvnw clean org.sonarsource.scanner.maven:sonar-maven-plugin:3.9.0.2155:sonar || mvn clean org.sonarsource.scanner.maven:sonar-maven-plugin:3.9.0.2155:sonar'
                }
            }
        }
    }
}
