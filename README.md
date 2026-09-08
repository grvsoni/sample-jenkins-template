# sample-jenkins-template

A sample **template-style scripted Jenkins pipeline**: a thin `Jenkinsfile` that reads job parameters from `config.xml` and delegates everything to a shared library function (`runSampleTemplate`) with multiple named parameters.

## Repository layout

```
.
├── Jenkinsfile                                  # Template-style Jenkinsfile (this repo)
├── config.xml                                   # Jenkins job definition with parameters
└── shared-library/
    └── vars/
        └── runSampleTemplate.groovy             # Shared library global function
```

> In production, the shared library normally lives in its own repository (named
> `my-shared-library`) with the standard `vars/`, `src/`, `resources/` layout.
> A copy of `runSampleTemplate.groovy` is included here under `shared-library/`
> for reference.

## How it works

1. **`config.xml`** defines a parameterized Pipeline job ("Pipeline script from SCM"
   pointing at this repo's `Jenkinsfile`). Parameters defined there:
   - `ENVIRONMENT` (choice: DEV / QA / PROD)
   - `JIRA_PROJECT` (string, default `SEV`)
   - `RTV` (boolean)
   - `DEPLOY_ENABLED` (boolean, default `true`)

2. **`Jenkinsfile`** loads `@Library(['my-shared-library'])_` and calls the template
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

3. **`runSampleTemplate`** (shared library) renders the actual Declarative pipeline:
   Initialize → Build → Test → (conditional) SEO/Governance Check → (conditional) Deploy,
   plus Jira subtask placeholders for deployment tracking.

## Setting it up on a Jenkins server

1. **Register the shared library**
   Manage Jenkins → System → *Global Pipeline Libraries* → add a library:
   - Name: `my-shared-library`
   - Default version: `main`
   - Retrieval: Modern SCM → Git → URL of the shared library repo
   - Check "Load implicitly" (or load explicitly via `@Library` as done in the Jenkinsfile)

2. **Create the job from `config.xml`**
   ```bash
   curl -X POST "https://<jenkins>/createItem?name=sample-jenkins-template" \
     --user <user>:<api-token> \
     -H "Content-Type: application/xml" \
     --data-binary @config.xml
   ```
   (Or: New Item → Pipeline, then paste the parameters and set "Pipeline script from SCM".)
   Don't forget to update the Git URL inside `config.xml` first.

3. **Run** — use "Build with Parameters": pick `ENVIRONMENT`, set `JIRA_PROJECT`,
   toggle `RTV` / `DEPLOY_ENABLED`. The Jenkinsfile passes them to the shared
   library template, which executes the pipeline.
