pipeline {
    agent any

    environment {
        SONARQUBE = 'sq1' // SonarQube server configured in Jenkins
        IMAGE_NAME = 'myapp:latest'
        REPORTS_DIR = "${WORKSPACE}/scan-reports"
    }

    options {
        skipDefaultCheckout(true)
        timestamps()
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                sh './mvnw clean install'
            }
        }

        stage('SAST - SonarQube Analysis') {
            steps {
                withSonarQubeEnv("${SONARQUBE}") {
                    sh './mvnw clean org.sonarsource.scanner.maven:sonar-maven-plugin:3.9.0.2155:sonar'
                }
            }
        }

        stage('Dependency Scan - SCA (Trivy)') {
            steps {
                echo "Running Trivy dependency scan..."
                sh """
                    mkdir -p ${REPORTS_DIR}
                    trivy fs --format json --output ${REPORTS_DIR}/trivy-fs-report.json .
                """
            }
        }

        stage('Secrets Scan - Gitleaks') {
            steps {
                echo "Running Gitleaks secrets scan..."
                sh """
                    mkdir -p ${REPORTS_DIR}
                    docker run --rm -v ${WORKSPACE}:/scan zricethezav/gitleaks:latest detect \
                        --source /scan --report-path /scan/scan-reports/gitleaks-report.json || true
                """
            }
        }

        stage('Docker Scan') {
            steps {
                echo "Building Docker image..."
                sh "docker build -t ${IMAGE_NAME} ."
                
                echo "Scanning Docker image with Trivy..."
                sh """
                    mkdir -p ${REPORTS_DIR}
                    trivy image --format json --output ${REPORTS_DIR}/trivy-image-report.json ${IMAGE_NAME}
                """
            }
        }

        stage('Quality Gate') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }
    }

    post {
        always {
            echo "Archiving scan reports..."
            archiveArtifacts artifacts: 'scan-reports/*.json', allowEmptyArchive: true
        }

        failure {
            echo "Pipeline failed! Check reports for details."
            // Optionally add email or Slack notifications here
        }

        success {
            echo "Pipeline succeeded! All scans completed."
        }
    }
}
