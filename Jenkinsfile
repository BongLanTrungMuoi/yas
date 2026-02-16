pipeline {
    agent any

    stages {
        stage("Security & Quality Scan") {
            stages {
                stage ("Gitleaks Scan") {
                    steps {
                        echo "Gitleaks scanning ..."
                    }
                }

                stage ("Sonarqube Scan") {
                    steps {
                        echo "Sonarqube scanning ..."
                    }
                }

                stage ("Snyk Scan") {
                    steps {
                        echo "Snyk scanning ..."
                    }
                }                
            }
        }
    }
}