pipeline {
    agent any
    environment {
        SONAR_HOME = tool 'Sonar'   // Make sure 'Sonar' is configured in Jenkins global tools
    }
    stages {
        stage("Checkout Code") {
            steps {
                git url: "https://github.com/rihemakkari/test.git", branch: "sonar"
            }
        }

        stage("Build") {
            steps {
                sh './mvnw clean install'
            }
        }

        stage("SonarQube Analysis") {
            steps {
                withSonarQubeEnv("Sonar") {
                    sh "$SONAR_HOME/bin/sonar-scanner -Dsonar.projectName=test -Dsonar.projectKey=test"
                }
            }
        }

        stage("Install Node Dependencies") {
            steps {
                sh """
                    echo "Installing Node dependencies..."
                    if [ -d frontend ]; then cd frontend && npm install && cd ..
                    fi
                    if [ -d backend ]; then cd backend && npm install && cd ..
                    fi
                """
            }
        }

        stage("OWASP Dependency Check") {
            steps {
                dependencyCheck additionalArguments: '--scan ./', odcInstallation: 'dc'
                dependencyCheckPublisher pattern: '**/dependency-check-report.xml'
            }
        }

        stage("Trivy Filesystem Scan") {
            steps {
                sh "trivy fs --format json --output trivy-fs-report.json ."
            }
        }

        stage("Gitleaks Secret Scan") {
            steps {
                sh "docker run --rm -v ${WORKSPACE}:/scan zricethezav/gitleaks:latest detect --source /scan --report-path /scan/gitleaks-report.json"
            }
        }

        stage("Quality Gate") {
            steps {
                timeout(time: 2, unit: "MINUTES") {
                    waitForQualityGate abortPipeline: false
                }
            }
        }
    }

    post {
        always {
            archiveArtifacts artifacts: '**/dependency-check-report.xml, trivy-fs-report.json, gitleaks-report.json', allowEmptyArchive: true
        }
    }
}
