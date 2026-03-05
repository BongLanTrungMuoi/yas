pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Test media') {
            steps {
                echo 'Running tests for Media Service...'
                // Giả sử dùng Maven, lệnh này sẽ chạy test và tạo báo cáo coverage
                sh './mvnw clean test' 
            }
        }

        stage('Coverage Check') {
            steps {
                // Sử dụng Plugin JaCoCo để phân tích kết quả
                // Các con số tương ứng với: (Min, Max)
                jacoco(
                    execPattern: '**/target/*.exec',
                    classPattern: '**/target/classes',
                    sourcePattern: '**/src/main/java',
                    exclusionPattern: '**/src/test/**',
                    
                    // Thiết lập ngưỡng (Thresholds)
                    // Nếu instructionCoverage < 70, build sẽ chuyển sang trạng thái UNSTABLE hoặc FAILURE
                    instructionCoverage: '70', 
                    branchCoverage: '70',
                    lineCoverage: '70'
                )
            }
        }
    }

    post {
        success {
            echo 'Chúc mừng! Media Service pass test với độ phủ trên 70%.'
        }
        unsuccessful {
            echo 'Build failed hoặc Code Coverage không đạt ngưỡng 70%.'
        }
    }
}