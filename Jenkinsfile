pipeline {
    agent any

    environment {
        APP_NAME = "event-ledger-api"
    }

    stages {
        stage('Checkout') {
            steps {
                echo "Pull code from GitHub ${APP_NAME}..."
            }
        }

        stage('API Diff Check') {
            steps {
                echo "API Diff Check running..."
                sh 'oasdiff diff api-specs/openapi-test1.yaml api-specs/openapi-test2.yaml'
            }
        }
        
        stage('Build') {
            steps {
                echo "Building ${APP_NAME}..."
                // sh './gradlew clean build'
            }
        }

        stage('Test') {
            steps {
                echo "Running tests..."
                // sh './gradlew test'
            }
            post {
                always {
                    echo "Tests completed (results publishing disabled for now)."
                    // junit '**/build/test-results/test/*.xml'
                }
            }
        }

        stage('Deploy') {
            steps {
                echo "Deploying ${APP_NAME}..."
                // sh './scripts/deploy.sh'
            }
        }
    }

    post {
        success {
            echo "Pipeline completed successfully!"
        }
        failure {
            echo "Pipeline failed. Check logs for details."
        }
    }
}
