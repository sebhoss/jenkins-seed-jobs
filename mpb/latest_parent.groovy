import groovy.json.JsonSlurper

def jobBasePath = "mpb"

def projectCatalog = new File("/var/git/stable/jenkins-jobs-setup/mpb/projects.json")
def slurper = new JsonSlurper()
def jsonText = projectCatalog.getText()
def json = slurper.parseText(jsonText)

json.each {
    def project = it
    if (!"maven-build-process".equals(project.name)) {
        job("$jobBasePath/$project.name (with latest parent)") {
            scm {
                git(project.repository)
            }
            triggers {
                project.upstreams.each {
                    upstream("$jobBasePath/$it (deploy to local-nexus)", "SUCCESS")
                }
            }
            steps {
                maven {
                    goals("versions:update-parent")
                    properties("generateBackupPoms": false)
                    mavenInstallation("maven-3.3.9")
                    providedGlobalSettings("talk-to-docker-nexus")
                }
            }
            steps {
                maven {
                    goals("clean")
                    goals("verify")
                    mavenInstallation("maven-3.3.9")
                    providedGlobalSettings("talk-to-docker-nexus")
                }
            }
        }
    }
}

listView("$jobBasePath/Latest Parent") {
    description("Jobs running with the latest version of the maven-build-process")
    jobs {
        json.each {
            name("$it.name (with latest parent)")
        }
    }
    recurse(true)
    columns {
        status()
        weather()
        name()
        lastSuccess()
        lastFailure()
        lastDuration()
        buildButton()
    }
}
