pipeline {
    agent any

    stages {
        stage('Pull Latest Changes') {
            steps {
                git 'https://github.com/DocumentationTool/Backend'
            }
        }

		stage('Build Backend') {
		    steps {
		        script {
		            sh 'chmod +x ./gradlew'
		            sh './gradlew build'
		        }
		    }
		}


        stage('Move WAR to Staging Folder') {
            steps {
                script {
                    sh 'mv **/build/libs/*-plain.war /opt/staging'
                }
            }
        }

        stage('Shutdown Tomcat') {
            steps {
                script {
                    sh '/opt/tomcat/bin/shutdown.sh'
                }
            }
        }

        stage('Deploy WAR to Tomcat') {
            steps {
                script {
                    sh 'mv /opt/staging/*.war /opt/tomcat/webapps'
                }
            }
        }

        stage('Start Tomcat') {
            steps {
                script {
                    sh '/opt/tomcat/bin/startup.sh'
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
