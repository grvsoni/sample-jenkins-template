@Library(['my-shared-library@main'])_

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
