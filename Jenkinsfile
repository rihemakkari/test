pipeline {
    agent any

    tools {
        maven 'M2_HOME'  // Must match the Maven installation name in Jenkins
    }

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
                // Run Maven clean install
                sh 'mvn clean install'
            }
        }

        stage('SonarQube Analysis') {
            steps {
                // Use the installed Maven to run Sonar analysis
                withSonarQubeEnv('sq1') { 
                    sh """
                        mvn sonar:sonar \
                        -Dsonar.projectKey=myproject \
                        -Dsonar.host.url=http://192.168.56.10:9000 \
                        -Dsonar.login=<your_sonar_token>
                    """
                }
            }
        }
    }
}
