stage('Build') {
    steps {
        dir('myapp') {
            sh 'mvn clean install'
        }
    }
}

stage('SonarQube Analysis') {
    steps {
        dir('myapp') {
            withSonarQubeEnv(installationName: 'sq1') {
                sh 'mvn sonar:sonar -Dsonar.projectKey=myproject -Dsonar.host.url=http://192.168.56.10:9000 -Dsonar.login=squ_01c9a1a1438da5901b8452fb3b4a6b3da0c20c5c'
            }
        }
    }
}
