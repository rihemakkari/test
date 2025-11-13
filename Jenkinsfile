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
                echo "Building the Maven project..."
                sh './mvnw clean install || true'
            }
        }

        stage('SAST - SonarQube Analysis') {
            steps {
                echo "Running SonarQube static code analysis..."
                withSonarQubeEnv("${SONARQUBE}") {
                    sh './mvnw clean org.sonarsource.scanner.maven:sonar-maven-plugin:3.9.0.2155:sonar || true'
                }
            }
        }

        stage('Dependency Scan - SCA (Trivy)') {
            steps {
                echo "Running Trivy dependency scan..."
                sh """
                    mkdir -p ${REPORTS_DIR}
                    trivy fs --format json --output ${REPORTS_DIR}/trivy-fs-report.json . || true
                """
            }
        }

        stage('Secrets Scan - Gitleaks') {
            steps {
                echo "Running Gitleaks secrets scan (bypassed errors)..."
                sh """
                    mkdir -p ${REPORTS_DIR}
                    docker run --rm -v ${WORKSPACE}:/scan zricethezav/gitleaks:latest detect \
                        --source /scan --report-path /scan/scan-reports/gitleaks-report.json || true
                """
            }
        }

        stage('Code Security - Bandit & Semgrep') {
            steps {
                echo "Running Bandit and Semgrep scans..."
                sh """
                    mkdir -p ${REPORTS_DIR}
                    bandit -r . -f json -o ${REPORTS_DIR}/bandit-report.json || true
                    semgrep --config auto --json --output ${REPORTS_DIR}/semgrep-report.json || true
                """
            }
        }

        stage('Docker Build & Image Scan') {
            steps {
                echo "Building Docker image and scanning it..."
                sh """
                    docker build -t ${IMAGE_NAME} ./docker || true
                    trivy image --format json --output ${REPORTS_DIR}/trivy-image-report.json ${IMAGE_NAME} || true
                """
            }
        }

        stage('Quality Gate') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: false
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
            echo "❌ Pipeline failed! Check reports for details."
        }

        success {
            echo "✅ Pipeline succeeded! All scans completed."
        }
    }
}
