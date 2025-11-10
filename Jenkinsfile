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
                    sh './mvnw clean org.sonarsource.scanner.maven:sonar-maven-plugin:3.9.0.2155:sonar || mvn clean org.sonarsource.scanner.maven:sonar-maven-plugin:3.9.0.2155:sonar'
            }
        }
    }
}
