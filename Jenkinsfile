// Load the shared library directly from this repo. Using an explicit retriever
// like this does NOT require registering a Global Pipeline Library in
// Manage Jenkins, which makes this sample runnable out of the box.
// (In production you'd usually register it globally and use
//  @Library(['my-shared-library@main'])_ instead.)
library identifier: 'sample-jenkins-template@main',
    retriever: modernSCM([$class: 'GitSCMSource',
                          remote: 'https://github.com/grvsoni/sample-jenkins-template.git'])

// Application name for this service
def appName = 'sample-service'

// Run sample multi branch job template.
// ENVIRONMENT, JIRA_PROJECT, RTV and DEPLOY_ENABLED below come from the job
// parameters defined in config.xml and are exposed as `params.*`.
runSampleTemplate teamName: 'service-excellence',
    appName: "${appName}",
    environment: params.ENVIRONMENT,
    jiraProject: params.JIRA_PROJECT,
    sevSeoLevel: (params.ENVIRONMENT == 'PROD' || params.RTV) ? 'high' : 'normal',
    jiraComponent: 'sample-component',
    deployEnabled: params.DEPLOY_ENABLED
