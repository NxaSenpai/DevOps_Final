// ═══════════════════════════════════════════════════════════════
// Jenkinsfile — NxaSenpai CI/CD Pipeline
// Group: B  |  Name: TANG NAKRY
//
// Flow:
//   1. Poll SCM every 5 minutes
//   2. Checkout → Build → Test
//   3. On failure: email srengty@gmail.com + the developer who
//      committed the breaking change
//   4. On success: run Ansible playbook to deploy to Web Server
//   5. Archive built JAR artifact
// ═══════════════════════════════════════════════════════════════

pipeline {

    agent any

    // ── Global environment variables ──
    environment {
        JAVA_HOME = '/opt/java/openjdk'
        GRADLE_OPTS = '-Dorg.gradle.daemon=false'
        ANSIBLE_HOST_KEY_CHECKING = 'False'
    }

    // ── Poll Git every 5 minutes ──
    triggers {
        pollSCM('H/5 * * * *')
    }

    // ── Pipeline stages ──
    stages {

        // ═══════════════════════════════════════════════
        // Stage 1 — Checkout
        // ═══════════════════════════════════════════════
        stage('Checkout') {
            steps {
                checkout scm
                script {
                    env.GIT_COMMITTER = sh(
                        script: 'git log -1 --pretty=format:"%ae"',
                        returnStdout: true
                    ).trim()
                    env.GIT_COMMIT_MSG = sh(
                        script: 'git log -1 --pretty=format:"%s"',
                        returnStdout: true
                    ).trim()
                    env.GIT_BRANCH = env.GIT_BRANCH ?: 'master'
                    echo "Committer: ${env.GIT_COMMITTER}"
                    echo "Commit message: ${env.GIT_COMMIT_MSG}"
                }
            }
        }

        // ═══════════════════════════════════════════════
        // Stage 2 — Build (compile Java)
        // ═══════════════════════════════════════════════
        stage('Build') {
            steps {
                sh '''
                    echo "=== Gradle Compile ==="
                    chmod +x ./gradlew
                    ./gradlew compileJava --no-daemon --console=plain
                '''
            }
        }

        // ═══════════════════════════════════════════════
        // Stage 3 — Test
        // ═══════════════════════════════════════════════
        stage('Test') {
            steps {
                sh '''
                    echo "=== Gradle Test ==="
                    ./gradlew test --no-daemon --console=plain
                '''
            }
            post {
                always {
                    // Publish JUnit test results for trend graphs
                    junit allowEmptyResults: true,
                          testResults: 'build/test-results/test/TEST-*.xml'
                }
            }
        }

        // ═══════════════════════════════════════════════
        // Stage 4 — Package JAR
        // ═══════════════════════════════════════════════
        stage('Package') {
            steps {
                sh '''
                    echo "=== Gradle BootJar ==="
                    ./gradlew bootJar --no-daemon --console=plain
                    echo "=== Listing built artifacts ==="
                    ls -lh build/libs/
                '''
            }
        }

        // ═══════════════════════════════════════════════
        // Stage 5 — Deploy via Ansible
        // ═══════════════════════════════════════════════
        stage('Deploy') {
            steps {
                sh '''
                    echo "=== Running Ansible Playbook ==="
                    cd ansible
                    ansible-playbook -i inventory.ini playbook.yml
                '''
            }
        }
    }

    // ── Post-build actions ──
    post {
        // ── Always archive the built JAR ──
        always {
            archiveArtifacts artifacts: 'build/libs/*.jar', fingerprint: true
        }

        // ── On SUCCESS — success email (optional) ──
        success {
            echo "✅ Pipeline SUCCESS — build & deploy complete"
        }

        // ── On FAILURE — notify committer + admin ──
        failure {
            script {
                def committerEmail = env.GIT_COMMITTER ?: 'unknown'
                def subject = "❌ BUILD FAILED: ${env.JOB_NAME} [#${env.BUILD_NUMBER}]"
                def body = """\
                    |The build has FAILED.
                    |
                    |━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                    |  Project:   ${env.JOB_NAME}
                    |  Build:     #${env.BUILD_NUMBER}
                    |  Branch:    ${env.GIT_BRANCH}
                    |  Committer: ${committerEmail}
                    |  Commit:    ${env.GIT_COMMIT_MSG}
                    |  Duration:  ${currentBuild.durationString.replace(' and counting', '')}
                    |━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                    |
                    |  Console Output:
                    |  ${env.BUILD_URL}console
                    |
                    |Please review the build log and fix the issue.
                    |""".stripMargin()

                // Send email to admin + the developer who committed
                emailext (
                    to: 'srengty@gmail.com',
                    subject: subject,
                    body: body,
                    recipientProviders: [
                        [$class: 'CulpritsRecipientProvider'],
                        [$class: 'DevelopersRecipientProvider']
                    ]
                )
            }
        }

        // ── On UNSTABLE — notify ──
        unstable {
            echo "⚠️  Pipeline UNSTABLE — check test results"
        }
    }
}
