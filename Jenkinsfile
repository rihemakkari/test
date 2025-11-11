pipeline {
    agent any
    stages {
        stage('Build') {
            steps {
                sh './mvnw clean install'
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('sq1') {
                    sh './mvnw clean org.sonarsource.scanner.maven:sonar-maven-plugin:3.9.0.2155:sonar'
                }
            }
        }
      stage("Bandit SAST Scan"){
    steps {
        sh 'bandit -r ./backend -o bandit-report.json -f json'
        // Optionally archive reports
        archiveArtifacts artifacts: 'bandit-report.json'
    }
}

    }
}
