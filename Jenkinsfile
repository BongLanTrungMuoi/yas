// pipeline {
//     agent any
    
//     tools {
//         maven 'Maven 3.8'
//         // jdk 'Java 21'
//     }

//     stages {
//         stage('Test Media') {
//             when {
//                 changeset "media/**"
//             }
//             steps {
//                 dir('media') {
//                     sh 'mvn test'
//                 }
//             }
//             post {
//                 always {
//                     junit 'media/target/surefire-reports/*.xml'
//                     jacoco(execPattern: 'media/target/jacoco.exec')
//                 }
//             }
//         }

//         stage('Build Media') {
//             when {
//                 changeset "media/**"
//             }
//             steps {
//                 dir('media') {
//                     sh 'mvn package -DskipTests'
//                 }
//             }
//         }

//         stage('Test Product') {
//             when {
//                 changeset "product/**"
//             }
//             steps {
//                 dir('product') {
//                     sh 'mvn test'
//                 }
//             }
//             post {
//                 always {
//                     junit 'product/target/surefire-reports/*.xml'
//                     jacoco(execPattern: 'product/target/jacoco.exec')
//                 }
//             }
//         }

//         stage('Build Product') {
//             when {
//                 changeset "product/**"
//             }
//             steps {
//                 dir('product') {
//                     sh 'mvn package -DskipTests'
//                 }
//             }
//         }

//         stage('Test Cart') {
//             when {
//                 changeset "cart/**"
//             }
//             steps {
//                 dir('cart') {
//                     sh 'mvn test'
//                 }
//             }
//             post {
//                 always {
//                     junit 'cart/target/surefire-reports/*.xml'
//                     jacoco(execPattern: 'cart/target/jacoco.exec')
//                 }
//             }
//         }

//         stage('Build Cart') {
//             when {
//                 changeset "cart/**"
//             }
//             steps {
//                 dir('cart') {
//                     sh 'mvn package -DskipTests'
//                 }
//             }
//         }
//     }

//     post {
//         success {
//             echo 'Pipeline SUCCESS'
//         }
//         failure {
//             echo 'Pipeline FAILED'
//         }
//     }
// }

pipeline {

    agent {
        docker {
            image 'maven:3.9.6-eclipse-temurin-21'
            args '-v $HOME/.m2:/root/.m2'
        }
    }
    stages {
        stage('Check Environment') {
            steps {
                echo 'Checking Java and Maven versions...'
                sh 'java -version'
                sh 'mvn -version'
            }
        }
        
        stage('Test Media') {
            steps {
                sh 'mvn test'
            }
        }
    }
}