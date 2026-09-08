library identifier: 'sample-jenkins-template@main',
    retriever: modernSCM([$class: 'GitSCMSource',
                          remote: 'https://github.com/grvsoni/sample-jenkins-template.git'])

def appName = 'sample-service'

runSampleTemplate teamName: 'service-excellence',
    appName: "${appName}",
    environment: params.ENVIRONMENT,
    jiraProject: params.JIRA_PROJECT,
    sevSeoLevel: (params.ENVIRONMENT == 'PROD' || params.RTV) ? 'high' : 'normal',
    jiraComponent: 'sample-component',
    deployEnabled: params.DEPLOY_ENABLED
