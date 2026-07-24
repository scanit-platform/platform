pipeline {
    agent any
    
    tools {
        maven 'mvn'
        nodejs 'node'
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'develop', url: 'https://github.com/scanit-platform/platform.git'
            }
        }
        stage('Build') {
            parallel {
                stage('Backend') {
                    steps {
                        dir('app/api') {
                            sh 'mvn clean install'
                        }
                    }
                }
                stage('Frontend') {
                    steps {
                        dir('app/web') {
                            sh 'npm install'
                            sh 'npm run build'
                        }
                    }
                }
            }
        }
        stage('Test') {
            parallel {
                stage('Backend') {
                    steps {
                        dir('app/api') {
                            sh 'mvn test'
                        }
                    }
                }
                stage('Frontend') {
                    steps {
                        dir('app/web') {
                            sh 'npm run test'
                        }
                    }
                }
            }
        }
        stage('Deploy to Staging') {
            when {
                branch 'develop'
            }
            steps {
                withEnv(["PATH+DOCKER=/usr/local/bin"]) {
                    dir('app/api') {
                        withCredentials([file(credentialsId: 'scanit-staging-env', variable: 'ENV_FILE')]) {
                            sh 'cp "$ENV_FILE" .env'
                        }
                        sh 'docker compose build'
                        sh 'docker compose up -d'
                    }
                }
            }
        }
        stage('Smoke Test') {
            when {
                branch 'develop'
            }
            steps {
                sh '''
                    for i in $(seq 1 10); do
                        curl -sf http://localhost:8081/actuator/health && break
                        echo "API not ready yet, retrying..."
                        sleep 3
                    done
                    curl -sf http://localhost:8081/actuator/health
                    curl -sf http://localhost:3000/ -o /dev/null
                '''
            }
        }
    }
}