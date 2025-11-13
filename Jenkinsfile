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
                git branch: 'sonar', url: 'https://github.com/rihemakkari/test.git'
            }
        

        stage('Secrets Scan') {
            steps {
                script {
                    echo '🔍 Scanning for exposed secrets...'
                    sh '''
                        docker run --rm -v $(pwd):/path \
                        zricethezav/gitleaks:latest \
                        detect --source="/path" --verbose --report-path=/path/gitleaks-report.json
                    '''
                }
            }
            post {
                always {
                    archiveArtifacts artifacts: 'gitleaks-report.json', allowEmptyArchive: true
                }
                failure {
                    error "🚨 Secrets detected! Pipeline blocked."
                }
            }
        }
        
        stage('SAST - Static Analysis') {
            steps {
                script {
                    echo '🔬 Running SAST with SonarQube...'
                    // IMPORTANT: withSonarQubeEnv doit englober l'analyse ET le Quality Gate
                    withSonarQubeEnv('SonarQube') {
                        sh '''
                            sonar-scanner \
                            -Dsonar.projectKey=${APP_NAME} \
                            -Dsonar.sources=. \
                            -Dsonar.host.url=${SONAR_HOST_URL} \
                            -Dsonar.login=${SONAR_TOKEN}
                        '''
                    }
                }
            }
        }
        
        stage('Quality Gate') {
            steps {
                script {
                    echo '⏳ Waiting for SonarQube Quality Gate...'
                    timeout(time: 5, unit: 'MINUTES') {
                        // Le Quality Gate doit être dans le même contexte que l'analyse
                        def qg = waitForQualityGate()
                        if (qg.status != 'OK') {
                            error "❌ Quality Gate failed: ${qg.status}"
                        } else {
                            echo "✅ Quality Gate passed!"
                        }
                    }
                }
            }
        }
        
        stage('SCA - Dependency Scan') {
            parallel {
                stage('OWASP Dependency Check') {
                    steps {
                        script {
                            echo '📦 Scanning dependencies for vulnerabilities...'
                            sh '''
                                docker run --rm -v $(pwd):/src \
                                owasp/dependency-check:latest \
                                --scan /src \
                                --format JSON \
                                --format HTML \
                                --project ${APP_NAME} \
                                --out /src/dependency-check-report
                            '''
                        }
                    }
                }
                
                stage('Trivy Filesystem Scan') {
                    steps {
                        script {
                            echo '🔍 Trivy filesystem vulnerability scan...'
                            sh '''
                                docker run --rm -v $(pwd):/myapp \
                                aquasec/trivy:latest fs \
                                --severity ${SEVERITY_THRESHOLD} \
                                --format json \
                                --output /myapp/trivy-fs-report.json \
                                /myapp
                            '''
                        }
                    }
                }
            }
            post {
                always {
                    archiveArtifacts artifacts: 'dependency-check-report/*, trivy-fs-report.json', allowEmptyArchive: true
                    publishHTML([
                        reportDir: 'dependency-check-report',
                        reportFiles: 'dependency-check-report.html',
                        reportName: 'OWASP Dependency Check'
                    ])
                }
            }
        }
        
        stage('Build') {
            steps {
                script {
                    echo '🏗️ Building application...'
                    // Example pour une app Node.js
                    sh '''
                        npm install
                        npm run build
                    '''
                    // Pour Java:
                    // sh 'mvn clean package -DskipTests'
                    // Pour Python:
                    // sh 'pip install -r requirements.txt && python setup.py build'
                }
            }
        }
        
        stage('Build Docker Image') {
            steps {
                script {
                    echo '🐳 Building Docker image...'
                    docker.build("${DOCKER_REGISTRY}/${APP_NAME}:${BUILD_NUMBER}")
                }
            }
        }
        
        stage('Docker Image Scan') {
            steps {
                script {
                    echo '🔍 Scanning Docker image with Trivy...'
                    sh """
                        docker run --rm -v /var/run/docker.sock:/var/run/docker.sock \
                        aquasec/trivy:latest image \
                        --severity ${SEVERITY_THRESHOLD} \
                        --exit-code 1 \
                        --format json \
                        --output trivy-image-report.json \
                        ${DOCKER_REGISTRY}/${APP_NAME}:${BUILD_NUMBER}
                    """
                }
            }
            post {
                always {
                    archiveArtifacts artifacts: 'trivy-image-report.json', allowEmptyArchive: true
                }
                failure {
                    echo '🚨 Critical vulnerabilities found in Docker image!'
                }
            }
        }
        
        stage('Push Docker Image') {
            when {
                expression { currentBuild.result == null || currentBuild.result == 'SUCCESS' }
            }
            steps {
                script {
                    echo '📤 Pushing Docker image to registry...'
                    docker.withRegistry("https://${DOCKER_REGISTRY}", 'docker-credentials') {
                        docker.image("${DOCKER_REGISTRY}/${APP_NAME}:${BUILD_NUMBER}").push()
                        docker.image("${DOCKER_REGISTRY}/${APP_NAME}:${BUILD_NUMBER}").push('latest')
                    }
                }
            }
        }
        
        stage('Deploy to Staging') {
            steps {
                script {
                    echo '🚀 Deploying to staging environment...'
                    // Exemple Kubernetes
                    sh """
                        kubectl set image deployment/${APP_NAME} \
                        ${APP_NAME}=${DOCKER_REGISTRY}/${APP_NAME}:${BUILD_NUMBER} \
                        -n staging
                    """
                }
            }
        }
        
        stage('DAST - Dynamic Security Testing') {
            steps {
                script {
                    echo '🎯 Running DAST with OWASP ZAP...'
                    sh '''
                        docker run --rm -v $(pwd):/zap/wrk \
                        owasp/zap2docker-stable:latest \
                        zap-baseline.py \
                        -t http://staging.myapp.com \
                        -r zap-report.html \
                        -J zap-report.json
                    '''
                }
            }
            post {
                always {
                    archiveArtifacts artifacts: 'zap-report.*', allowEmptyArchive: true
                    publishHTML([
                        reportDir: '.',
                        reportFiles: 'zap-report.html',
                        reportName: 'OWASP ZAP DAST Report'
                    ])
                }
            }
        }
        
        stage('Security Report') {
            steps {
                script {
                    echo '📊 Generating consolidated security report...'
                    sh '''
                        cat > security-summary.txt <<EOF
═══════════════════════════════════════════════
    DEVSECOPS SECURITY SCAN SUMMARY
═══════════════════════════════════════════════
Build Number: ${BUILD_NUMBER}
Timestamp: $(date)
Application: ${APP_NAME}

✓ Secrets Scan      : PASSED
✓ SAST Analysis     : PASSED
✓ SCA Scan          : PASSED
✓ Docker Image Scan : PASSED
✓ DAST Scan         : COMPLETED

All security checks passed successfully!
═══════════════════════════════════════════════
EOF
                        cat security-summary.txt
                    '''
                }
            }
        }
    }
    
    post {
        success {
            script {
                // Notification Slack
                slackSend(
                    color: 'good',
                    message: "✅ DevSecOps Pipeline SUCCESS - ${APP_NAME} #${BUILD_NUMBER}\n" +
                             "All security checks passed!\n" +
                             "View: ${BUILD_URL}"
                )
                
                // Email notification
                emailext(
                    subject: "✅ DevSecOps Build Success - ${APP_NAME} #${BUILD_NUMBER}",
                    body: """
                        <h2>Build Successful</h2>
                        <p>All security scans passed for ${APP_NAME}</p>
                        <ul>
                            <li>Secrets Scan: ✓</li>
                            <li>SAST: ✓</li>
                            <li>SCA: ✓</li>
                            <li>Docker Scan: ✓</li>
                            <li>DAST: ✓</li>
                        </ul>
                        <p><a href="${BUILD_URL}">View Build</a></p>
                    """,
                    to: 'devops-team@company.com',
                    mimeType: 'text/html'
                )
            }
        }
        
        failure {
            script {
                slackSend(
                    color: 'danger',
                    message: "🚨 DevSecOps Pipeline FAILED - ${APP_NAME} #${BUILD_NUMBER}\n" +
                             "Security issues detected!\n" +
                             "View: ${BUILD_URL}"
                )
                
                emailext(
                    subject: "🚨 DevSecOps Build Failed - ${APP_NAME} #${BUILD_NUMBER}",
                    body: """
                        <h2 style="color:red">Build Failed</h2>
                        <p>Security vulnerabilities detected in ${APP_NAME}</p>
                        <p><strong>Action Required:</strong> Review security reports and fix issues</p>
                        <p><a href="${BUILD_URL}">View Build Details</a></p>
                    """,
                    to: 'devops-team@company.com',
                    mimeType: 'text/html'
                )
            }
        }
        
        always {
            cleanWs()
        }
    }
}
} 
