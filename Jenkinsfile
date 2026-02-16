pipeline {
    agent any

    stages {
        stage('Pre-Scan: Compile Java Microservices') {
            steps {
                echo '--- Compiling Java modules for accurate Sonar analysis ---'
                sh """
                    docker run --rm \
                        -v "${WORKSPACE}":/usr/src/app \
                        -v "${HOME}/.m2":/root/.m2 \
                        -w /usr/src/app \
                        maven:3.9-eclipse-temurin-21 \
                        mvn clean test-compile -DskipTests
                """
            }
        }

        stage("Security & Quality Scans") {
            environment {
                SNYK_TOKEN = credentials('snyk-token-id')
                SONAR_TOKEN = credentials('sonarqube-token-id')
                SONAR_HOST_URL = credentials('sonarqube-host-url')
            }
            
            parallel {
                stage('1. Gitleaks: Secrets') {
                    steps {
                        echo '--- Scanning Latest Commit across the entire Monorepo ---'
                        sh """
                            docker run --rm \
                                -v "${WORKSPACE}":/code \
                                ghcr.io/gitleaks/gitleaks:latest \
                                detect --source /code --verbose --redact --no-color || true
                        """
                    }
                }

                stage('2. Snyk: Dependencies') {
                    steps {
                        echo '--- Scanning Maven & NPM Dependencies ---'
                        sh """
                            docker run --rm \
                                -e SNYK_TOKEN=${SNYK_TOKEN} \
                                -v "${WORKSPACE}":/project \
                                -w /project \
                                snyk/snyk:maven \
                                snyk test --severity-threshold=high --all-projects || true
                        """
                    }
                }
                
                stage('3. SonarQube: Code Quality (Polyglot)') {
                    steps {
                        echo '--- Scanning Java and Next.js code in one pass ---'
                        sh """
                            docker run --rm \
                                -e SONAR_HOST_URL=${SONAR_HOST_URL} \
                                -e SONAR_TOKEN=${SONAR_TOKEN} \
                                -v "${WORKSPACE}":/usr/src \
                                sonarsource/sonar-scanner-cli \
                                -Dsonar.projectKey=yas-monorepo \
                                -Dsonar.sources=. \
                                -Dsonar.java.binaries=**/target/classes \
                                -Dsonar.exclusions=**/node_modules/**,**/.next/**,**/dist/**,**/postgres/**,**/.turbo/** || true
                        """
                    }
                }
            }
        }
    }
}