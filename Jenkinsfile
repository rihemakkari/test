pipeline {
    agent any

    stages {
        stage('Build') {
            steps {
                dir('myapp') {
                    sh '../mvnw clean install'
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                dir('myapp') {
                    withSonarQubeEnv('sq1') {
                        sh '../mvnw clean org.sonarsource.scanner.maven:sonar-maven-plugin:3.9.0.2155:sonar'
                    }
                }
            }
        }
    }
}
