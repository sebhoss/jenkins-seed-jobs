import groovy.json.JsonSlurper

def projectCatalog = new File("/var/git/stable/jenkins-jobs-setup/projects.json")
def slurper = new JsonSlurper()
def jsonText = projectCatalog.getText()
def json = slurper.parseText(jsonText).findAll { !"maven-build-process".equals(it.name) }

json.each {
    def project = it
    job("${project.name}_with_latest_parent") {
        blockOnUpstreamProjects()
        logRotator {
            numToKeep(5)
            daysToKeep(7)
        }
        scm {
            git(project.repository)
        }
        triggers {
            project.upstreams.each {
                upstream("${it}_deploy_to_local-nexus", "SUCCESS")
            }
            cron("@daily")
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
