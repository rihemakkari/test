pipeline {
    agent any

    environment {
        SONARQUBE = 'sq1'
        IMAGE_NAME = 'myapp:latest'
        REPORTS_DIR = "${WORKSPACE}/scan-reports"
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
            steps {
                echo "Building project (Maven wrapper may fail if missing)"
                sh './mvnw clean install || true'
            }
        }

        stage('SAST - SonarQube') {
            steps {
                echo "SonarQube scan (won't run if build failed)"
                withSonarQubeEnv("${SONARQUBE}") {
                    sh './mvnw sonar:sonar || true'
                }
            }
        }

        stage('Dependency Scan - Trivy') {
            steps {
                echo "Trivy dependency scan"
                sh """
                    mkdir -p ${REPORTS_DIR}
                    trivy fs --format json --output ${REPORTS_DIR}/trivy-fs-report.json . || true
                """
            }
        }

        stage('Secrets Scan - Gitleaks') {
            steps {
                echo "Gitleaks secrets scan (Docker permissions may block this)"
                sh """
                    mkdir -p ${REPORTS_DIR}
                    docker run --rm -v ${WORKSPACE}:/scan zricethezav/gitleaks:latest detect \
                    --source /scan --report-path /scan/scan-reports/gitleaks-report.json || true
                """
            }
        }

        stage('Docker Scan - Trivy') {
            steps {
                echo "Docker image scan (requires Docker access)"
                sh 'docker build -t myapp:latest ./docker || true'
                sh 'trivy image --format json --output scan-reports/trivy-image-report.json myapp:latest || true'
            }
        }
    }

    post {
        always {
            echo "Archiving scan reports..."
            archiveArtifacts artifacts: 'scan-reports/*.json', allowEmptyArchive: true
        }
    }
}
