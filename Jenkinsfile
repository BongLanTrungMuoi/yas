pipeline {
    agent any
    
    environment {
        REVISION = "1.0-SNAPSHOT"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                sh 'ls -F'
            }
        }

        stage('Build & Install') {
            steps {
                echo "Installing dependencies with revision ${REVISION}..."
                sh "mvn clean install -DskipTests -Drevision=${REVISION} -U"
            }
        }

        // ==================== TAX SERVICE ====================
        stage('Test Tax Service') {
            steps {
                echo 'Testing tax service...'
                sh "mvn test -pl tax -am -Drevision=${REVISION}"
            }
        }

        stage('Coverage Check Tax') {
            steps {
                script {
                    echo 'Generating Jacoco Report for tax...'
                    sh "mvn jacoco:report -pl tax -Drevision=${REVISION}"
                    
                    def coverageStr = sh(
                        script: "cat tax/target/site/jacoco/index.html | grep -o 'Total[^%]*%' | grep -oE '[0-9]+%' | tr -d '%' | head -n 1",
                        returnStdout: true
                    ).trim()

                    if (coverageStr == null || coverageStr == "") {
                        error "Không thể trích xuất chỉ số Coverage của tax."
                    }

                    int coverage = coverageStr.toInteger()
                    echo "Độ phủ code hiện tại của tax là: ${coverage}%"

                    if (coverage < 70) {
                        error "FAILED: Độ phủ code của tax (${coverage}%) dưới mức yêu cầu 70%!"
                    }
                }
            }
        }

        // ==================== SEARCH SERVICE ====================
        stage('Test Search Service') {
            steps {
                echo 'Testing search service...'
                // Chạy test riêng cho module search
                sh "mvn test -pl search -am -Drevision=${REVISION}"
            }
        }

        stage('Coverage Check Search') {
            steps {
                script {
                    echo 'Generating Jacoco Report for search...'
                    sh "mvn jacoco:report -pl search -Drevision=${REVISION}"
                    
                    // Lọc % từ file của search
                    def coverageStr = sh(
                        script: "cat search/target/site/jacoco/index.html | grep -o 'Total[^%]*%' | grep -oE '[0-9]+%' | tr -d '%' | head -n 1",
                        returnStdout: true
                    ).trim()

                    if (coverageStr == null || coverageStr == "") {
                        error "Không thể trích xuất chỉ số Coverage của search."
                    }

                    int coverage = coverageStr.toInteger()
                    echo "Độ phủ code hiện tại của search là: ${coverage}%"

                    if (coverage < 70) {
                        error "FAILED: Độ phủ code của search (${coverage}%) dưới mức yêu cầu 70%!"
                    }
                }
            }
        }
    }

    post {
        always {
            // Gom chung kết quả JUnit của cả 2 service bằng dấu phẩy
            junit testResults: 'tax/target/surefire-reports/*.xml, search/target/surefire-reports/*.xml', allowEmptyResults: true
        }
        success {
            echo "Pipeline hoàn thành xuất sắc! Cả Tax và Search đều pass Coverage >= 70%."
        }
    }
}