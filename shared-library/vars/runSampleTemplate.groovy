#!/usr/bin/env groovy
//
// Shared library global function: runSampleTemplate
//
// Invoke from a Jenkinsfile like:
//
//   @Library(['my-shared-library'])_
//   runSampleTemplate teamName: 'my-team',
//       appName: 'my-app',
//       environment: 'DEV',
//       jiraProject: 'SEV',
//       sevSeoLevel: 'normal',
//       jiraComponent: 'my-component',
//       deployEnabled: true
//
// Supported keys (all optional except teamName/appName):
//   teamName      - owning team, used for credential/resource naming
//   appName       - application name
//   environment   - target environment (DEV/QA/PROD)
//   jiraProject   - Jira project key for deployment tracking subtasks
//   sevSeoLevel   - 'normal' or 'high'; 'high' runs extra SEO/governance checks
//   jiraComponent - Jira component to tag on created subtasks
//   deployEnabled - whether the deploy stage runs
//   aiEnabled     - whether to enable AI-assisted deployment decisions

def call(Map config = [:]) {
    config = config ?: [:]

    config.teamName      = config.teamName      ?: error("runSampleTemplate: 'teamName' is required")
    config.appName       = config.appName       ?: error("runSampleTemplate: 'appName' is required")
    config.environment   = config.environment   ?: 'DEV'
    config.jiraProject   = config.jiraProject   ?: 'SEV'
    config.sevSeoLevel   = config.sevSeoLevel   ?: 'normal'
    config.jiraComponent = config.jiraComponent ?: config.teamName
    config.deployEnabled = (config.deployEnabled == null) ? true : config.deployEnabled.toBoolean()
    config.aiEnabled     = true

    pipeline {
        agent any

        options {
            timestamps()
            disableConcurrentBuilds()
            buildDiscarder(logRotator(numToKeepStr: '10'))
        }

        stages {
            stage('Initialize') {
                steps {
                    script {
                        echo "Team        : ${config.teamName}"
                        echo "App         : ${config.appName}"
                        echo "Environment : ${config.environment}"
                        echo "Jira project: ${config.jiraProject} / component: ${config.jiraComponent}"
                        echo "SEO level   : ${config.sevSeoLevel}"
                        echo "Deploy      : ${config.deployEnabled}"
                    }
                }
            }

            stage('Build') {
                steps {
                    script {
                        echo "Building ${config.appName} for team ${config.teamName}..."
                        // e.g. sh 'mvn -B clean package'
                    }
                }
            }

            stage('Test') {
                steps {
                    script {
                        echo "Running unit tests for ${config.appName}..."
                        // e.g. sh 'mvn -B test'
                    }
                }
            }

            stage('SEO/Governance Check') {
                when {
                    expression { config.sevSeoLevel == 'high' }
                }
                steps {
                    script {
                        echo "HIGH severity run (${config.environment}): running extended SEO checks before deploy."
                        openJiraSubtask(config, "SEO validation for ${config.environment} deployment of ${config.appName}")
                    }
                }
            }

            stage('Deploy') {
                when {
                    expression { config.deployEnabled }
                }
                steps {
                    script {
                        echo "Deploying ${config.appName} to ${config.environment}..."
                        // e.g. sh "kubectl -n ${config.teamName} set image deployment/${config.appName} ..."
                        openJiraSubtask(config, "Deployment of ${config.appName} to ${config.environment}")
                    }
                }
            }
        }

        post {
            success {
                echo "${config.appName} pipeline completed successfully for ${config.environment}."
            }
            failure {
                echo "${config.appName} pipeline FAILED for ${config.environment}."
            }
        }
    }
}

// Simulates creating a Jira subtask for deployment tracking.
// Replace the echo with a real Jira REST call or the JIRA pipeline steps plugin.
def openJiraSubtask(Map config, String summary) {
    echo "[JIRA] (${config.jiraProject} / ${config.jiraComponent}) ${summary}"
}
