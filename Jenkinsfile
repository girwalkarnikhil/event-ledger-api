pipeline {
    agent any

    environment {
        // Define environment variables if needed
        APP_NAME = "event-ledger-api"
    }

    stages {
        stage('Checkout') {
            steps {
                // Pull code from GitHub
                echo "Pull code from GitHub ${APP_NAME}..."
            }
        }

        stage('Build') {
            steps {
                echo "Building ${APP_NAME}..."
                //sh './gradlew clean build'   // Example for Gradle build
            }
        }

        stage('Test') {
            steps {
                echo "Running tests..."
                //sh './gradlew test'
            }
            post {
                //always {
                  //  junit '**/build/test-results/test/*.xml'
                }
            }
        }

        stage('Deploy') {
            steps {
                echo "Deploying ${APP_NAME}..."
                // Replace with your deployment script or commands
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