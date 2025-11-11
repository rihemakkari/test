pipeline {
    agent any

    environment {
        SONARQUBE_ENV = 'sq1'   // Your SonarQube configuration name in Jenkins
        PROJECT_NAME = 'test'   // Your project name
    }

    stages {
        stage('Build') {
            steps {
                echo 'Building the project...'
                sh './mvnw clean install'
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv(SONARQUBE_ENV) {
                    echo 'Running SonarQube scan...'
                    sh './mvnw clean org.sonarsource.scanner.maven:sonar-maven-plugin:3.9.0.2155:sonar'
                }
            }
        }

        stage('SAST - Semgrep') {
            steps {
                echo 'Running Semgrep SAST scan...'
                sh 'semgrep --config=p/ci . --json --output semgrep-report.json || true'
            }
        }

        stage('SCA / Trivy') {
            steps {
                echo 'Running Trivy SCA / filesystem scan...'
                sh 'trivy fs --format json --output trivy-fs-report.json . || true'
            }
        }

        stage('Secrets Scan - Gitleaks') {
            steps {
                echo 'Running Gitleaks secrets scan...'
                sh '''
                    docker run --rm -v $PWD:/scan zricethezav/gitleaks:latest detect \
                    --source /scan --report-path /scan/gitleaks-report.json || true
                '''
            }
        }

        stage('Quality Gate') {
            steps {
                echo 'Waiting for SonarQube Quality Gate...'
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }
    }

    post {
        always {
            echo 'Publishing scan reports...'
            archiveArtifacts artifacts: 'semgrep-report.json, trivy-fs-report.json, gitleaks-report.json', allowEmptyArchive: true
        }
        failure {
            echo 'Pipeline failed! Check SonarQube and scan reports for details.'
        }
        success {
            echo 'Pipeline succeeded! All scans completed.'
        }
    }
}
