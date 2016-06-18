import groovy.json.JsonSlurper

def projectCatalog = new File("/var/git/stable/jenkins-jobs-setup/projects.json")
def slurper = new JsonSlurper()
def jsonText = projectCatalog.getText()
def json = slurper.parseText(jsonText)

json.each {
    def project = it
    job("${project.name}_deploy_to_local-nexus") {
        logRotator {
            numToKeep(5)
            daysToKeep(7)
        }
        scm {
            git(project.repository)
        }
        triggers {
            scm("H/15 * * * *")
        }
        steps {
            maven {
                goals("clean")
                goals("deploy")
                mavenInstallation("maven-latest")
                providedGlobalSettings("talk-to-docker-nexus")
            }
        }
        steps {
            maven {
                goals("sonar:sonar")
                properties("sonar.host.url": "http://sonar:9000")
                mavenInstallation("maven-latest")
                providedGlobalSettings("talk-to-docker-nexus")
            }
        }
        publishers {
            irc {
                strategy("ALL")
                notificationMessage("SummaryOnly")
            }
        }
    }
}

listView("Deployments") {
    description("All jobs that deploy artifacts")
    jobs {
        json.each {
            name("${it.name}_deploy_to_local-nexus")
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