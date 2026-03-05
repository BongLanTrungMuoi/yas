pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Test Media Service') {
            steps {
                sh 'chmod +x mvnw'
                
                echo 'Running tests specifically for Media Service...'
                // Chạy test giới hạn trong module media
                sh './mvnw clean test -pl media -am' 
            }
        }

        stage('Coverage Check') {
            steps {
                // Điều chỉnh đường dẫn (Path) để JaCoCo chỉ nhìn vào module media
                jacoco(
                    // Chỉ lấy file kết quả .exec bên trong folder media
                    execPattern: 'media/target/*.exec',
                    
                    // Chỉ quét các file class đã biên dịch của service media
                    classPattern: 'media/target/classes',
                    
                    // Thư mục chứa code thuần của media
                    sourcePattern: 'media/src/main/java',
                    
                    // Ngưỡng 70%
                    instructionCoverage: '70', 
                    branchCoverage: '70',
                    lineCoverage: '70',
                    
                    // Quan trọng: Build sẽ FAIL nếu không đạt 70%
                    changeBuildStatus: true
                )
            }
        }
    }

    post {
        always {
            // Lưu lại kết quả test để xem trên giao diện Jenkins
            junit 'media/target/surefire-reports/*.xml'
        }
    }
}