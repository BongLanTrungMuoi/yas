pipeline {
    agent any
    stages {
        stage('Build Media Service') {
            when {
                changeset "vmedia/**" 
            }
            steps {
                dir('media') {
                    echo "media change"
                }
            }
        }
        
        stage('Build Product Service') {
            when {
                changeset "product/**"
            }
            steps {
                dir('product') {
                    echo "product change"
                }
            }
        }
    }
}
