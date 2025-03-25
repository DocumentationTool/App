pipeline {
    agent any

    stages {
        stage('Pull Latest Changes') {
            steps {
                git 'https://github.com/DocumentationTool/App'
            }
        }

        stage('Build Backend') {
            steps {
                script {
                    sh './gradlew build'
                }
            }
        }

        stage('Move WAR to Staging Folder') {
            steps {
                script {
                    sh 'mv **/build/libs/*-plain.war /path/to/staging/folder/'
                }
            }
        }

        stage('Shutdown Tomcat') {
            steps {
                script {
                    sh '/path/to/tomcat/bin/shutdown.sh'
                }
            }
        }

        stage('Deploy WAR to Tomcat') {
            steps {
                script {
                    sh 'mv /path/to/staging/folder/your-backend.war /path/to/tomcat/webapps/'
                }
            }
        }

        stage('Start Tomcat') {
            steps {
                script {
                    sh '/path/to/tomcat/bin/startup.sh'
                }
            }
        }
    }

    post {
        always {
            echo 'Pipeline execution finished.'
        }

        success {
            echo 'Deployment was successful.'
        }

        failure {
            echo 'Deployment failed.'
        }
    }
}
