# sample-jenkins-template

A sample **template-style scripted Jenkins pipeline**: a thin `Jenkinsfile` that reads job parameters from `config.xml` and delegates everything to a shared library function (`runSampleTemplate`) with multiple named parameters.

For simplicity, this single repo doubles as both the pipeline repo and the shared
library repo: `vars/` sits at the repo root (required by Jenkins shared
libraries) while `Jenkinsfile` lives alongside it. In production you'd normally
split `vars/` into a dedicated `my-shared-library` repository.

## Repository layout

```
.
├── Jenkinsfile                      # Template-style Jenkinsfile
├── config.xml                       # Jenkins job definition with parameters
└── vars/
    └── runSampleTemplate.groovy     # Shared library global function
```

## How it works

1. **`config.xml`** defines a parameterized Pipeline job ("Pipeline script from SCM"
   pointing at this repo's `Jenkinsfile`). Parameters defined there:
   - `ENVIRONMENT` (choice: DEV / QA / PROD)
   - `JIRA_PROJECT` (string, default `SEV`)
   - `RTV` (boolean)
   - `DEPLOY_ENABLED` (boolean, default `true`)

2. **`Jenkinsfile`** loads `@Library(['my-shared-library@main'])_` and calls the template
   function with 7 named parameters, mixing hardcoded values with job parameters:

   | Template parameter | Source |
   |---|---|
   | `teamName` | hardcoded |
   | `appName` | local variable |
   | `environment` | `params.ENVIRONMENT` (from config.xml) |
   | `jiraProject` | `params.JIRA_PROJECT` (from config.xml) |
   | `sevSeoLevel` | computed from `params.ENVIRONMENT` + `params.RTV` (from config.xml) |
   | `jiraComponent` | hardcoded |
   | `deployEnabled` | `params.DEPLOY_ENABLED` (from config.xml) |

   `sevSeoLevel` becomes `high` when deploying to PROD or when RTV is checked —
   high-severity runs inject an extra SEO/governance check stage.

3. **`runSampleTemplate`** (`vars/runSampleTemplate.groovy`) renders the actual
   Declarative pipeline: Initialize → Build → Test → (conditional) SEO/Governance
   Check → (conditional) Deploy, plus Jira subtask placeholders for deployment
   tracking.

## Setting it up on a Jenkins server

1. **No shared library registration needed** — the Jenkinsfile loads the library
   with an inline retriever:

   ```groovy
   library identifier: 'my-shared-library@main',
       retriever: modernSCM([$class: 'GitSCMSource',
                             remote: 'https://github.com/grvsoni/sample-jenkins-template.git'])
   ```

   because `vars/` sits at the root of this same repo. (If you prefer the classic
   setup: Manage Jenkins → System → *Global Pipeline Libraries*, Name:
   `my-shared-library`, Default version: `main`, Git URL of this repo — and then
   you can use `@Library(['my-shared-library@main'])_` in the Jenkinsfile instead.)

2. **Create the job from `config.xml`**
   ```bash
   curl -X POST "https://<jenkins>/createItem?name=sample-jenkins-template" \
     --user <user>:<api-token> \
     -H "Content-Type: application/xml" \
     --data-binary @config.xml
   ```
   (Or: New Item → Pipeline, add the parameters, and set "Pipeline script from SCM".)

3. **Run** — use "Build with Parameters": pick `ENVIRONMENT`, set `JIRA_PROJECT`,
   toggle `RTV` / `DEPLOY_ENABLED`. The Jenkinsfile passes them to the shared
   library template, which executes the pipeline.

## Troubleshooting

- **`Could not find any definition of libraries [my-shared-library]`** — that
  error comes from the `@Library` annotation, which requires a library to be
  preregistered by name. The current Jenkinsfile uses an inline
  `library identifier: ... retriever: modernSCM(...)` instead, so you should
  no longer see it. (It would only return if you switched back to `@Library`
  without registering the library in Manage Jenkins.)

- **`vars/... could not be found`** — the library repo must have `vars/` at its
  root, not in a subfolder.
