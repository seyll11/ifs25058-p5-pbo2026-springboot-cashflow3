pipeline {
    agent any

    environment {
        APP_NAME = 'ifs25058-p5-pbo2026-springboot-cashflow'
        IMAGE    = "registry.example.com/pbo2026/${APP_NAME}"
        TAG      = "${env.BUILD_NUMBER}"
    }

    options {
        timestamps()
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }

    stages {
        stage('Build & Test') {
            steps {
                sh './mvnw -B clean test'
            }
            post {
                always { junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml' }
            }
        }

        stage('Jacoco 100%') {
            steps {
                // gagal bila coverage counter terukur < 100% (view & auth dikecualikan di pom.xml)
                sh './mvnw -B verify'
            }
            post {
                always { archiveArtifacts artifacts: 'target/site/jacoco/**', allowEmptyArchive: true }
            }
        }

        stage('Trivy Scan') {
            steps {
                sh 'trivy fs --exit-code 1 --severity HIGH,CRITICAL --skip-dirs target,node_modules .'
            }
        }

        stage('Package') {
            steps {
                sh './mvnw -B clean package -Pproduction -DskipTests'
            }
        }

        stage('Publish') {
            steps {
                sh '''
                    docker build -t ${IMAGE}:${TAG} -t ${IMAGE}:latest .
                    docker push ${IMAGE}:${TAG}
                    docker push ${IMAGE}:latest
                '''
            }
        }

        stage('Deploy') {
            when { branch 'main' }
            steps {
                // DB_*, ROOT_*, UPLOAD_DIR, SESSION_SECRET disuplai dari credential/env server, bukan dari repo
                sh '''
                    docker rm -f ${APP_NAME} || true
                    docker run -d --name ${APP_NAME} --restart unless-stopped \
                        -p 8080:8080 --env-file /opt/${APP_NAME}/.env \
                        -v /opt/${APP_NAME}/uploads:/app/uploads \
                        ${IMAGE}:${TAG}
                '''
            }
        }
    }

    post {
        failure { echo 'Pipeline gagal - periksa stage yang merah.' }
    }
}
