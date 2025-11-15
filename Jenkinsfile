pipeline {
    agent any
    tools { maven 'M2_HOME' }

    options { timestamps() }

    stages {
        stage('Checkout') { steps { git branch: 'sonar', url: 'https://github.com/rihemakkari/test.git' } }

        stage('Build') { steps { sh './mvnw clean install -DskipTests' } }

        stage('Test') { steps { sh './mvnw test' } }

        stage('ESLint') {
            steps {
                script {
                    sh 'npm install'
                    sh 'npx eslint src/**/*.js || true'
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv(installationName: 'sq1') {
                    sh './mvnw sonar:sonar -Dsonar.login=$SONAR_TOKEN -Dsonar.java.binaries=target/classes'
                }
            }
        }

        stage('Dependency-Check') {
            steps {
                script {
                    sh './tools/dependency-check/bin/dependency-check.sh --project myapp --scan ./pom.xml --format HTML --out target/dependency-check-report.html'
                }
                archiveArtifacts artifacts: 'target/dependency-check-report.html', allowEmptyArchive: true
            }
        }

        stage('Package') { steps { sh './mvnw package' } }

        stage('Docker Scan') {
            when { expression { fileExists('Dockerfile') } }
            steps {
                sh 'docker build -t myapp:latest .'
                sh 'trivy image --severity HIGH,CRITICAL myapp:latest || true'
            }
        }
    }

    post {
        always { archiveArtifacts artifacts: '**/target/*.jar', allowEmptyArchive: true }
    }
}
