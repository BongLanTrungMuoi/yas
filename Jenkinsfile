pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                // In ra danh sách file để bạn kiểm tra nếu vẫn lỗi
                sh 'ls -F'
            }
        }

        stage('Test Media Service') {
            steps {
                script {
                    // // TRƯỜNG HỢP 1: mvnw nằm ở thư mục gốc
                    // if (fileExists('mvnw')) {
                    //     sh 'chmod +x mvnw'
                    //     sh './mvnw clean test -pl media -am'
                    // } 
                    // // TRƯỜNG HỢP 2: mvnw nằm trong thư mục media
                    // else if (fileExists('media/mvnw')) {
                    //     sh 'chmod +x media/mvnw'
                    //     dir('media') {
                    //         sh './mvnw clean install'
                    //     }
                    // } 
                    // // TRƯỜNG HỢP 3: Không có mvnw, dùng mvn hệ thống
                    // else {
                    //     echo 'Không tìm thấy mvnw, sử dụng lệnh mvn hệ thống...'
                    //     sh 'mvn clean install -pl media -am'
                    // }
                    echo 'Install dependencies...'
                    sh 'mvn clean install -DskipTests -Drevision=1.0-SNAPSHOT -U'
                    echo 'Test service media...'
                    sh 'mvn clean test -pl media -am'
                }
            }
        }

        stage('Coverage Check') {
            steps {
                // Sử dụng dấu ** để tìm file báo cáo dù cấu trúc thư mục thế nào
                jacoco(
                    // execPattern: '**/target/*.exec',
                    // classPattern: '**/media/target/classes',
                    // sourcePattern: '**/media/src/main/java',
                    execPattern: 'media/target/jacoco.exec',
                    classPattern: 'media/target/classes',
                    sourcePattern: 'media/src/main/java',
                    
                    // Ngưỡng 70%
                    instructionCoverage: '70', 
                    branchCoverage: '70',
                    lineCoverage: '70',
                    
                    // Đánh dấu Build FAILED nếu không đạt ngưỡng
                    changeBuildStatus: true,
                    runEveryShortTests: true
                )
            }
        }
    }

    post {
        always {
            // allowEmptyResults: true giúp tránh lỗi pipeline dừng khi chưa có file report
            junit testResults: '**/target/surefire-reports/*.xml', allowEmptyResults: true
        }
    }
}