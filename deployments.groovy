import groovy.json.JsonSlurper

def projectCatalog = new File("/var/git/stable/jenkins-jobs-setup/projects.json")
def slurper = new JsonSlurper()
def jsonText = projectCatalog.getText()
def json = slurper.parseText(jsonText)

json.each {
    def project = it
    folder(project.name)
    job("${project.name}/${project.name}_deploy_to_local-nexus") {
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
                providedGlobalSettings("talk-to-local-nexus")
            }
        }
        steps {
            maven {
                goals("sonar:sonar")
                properties("sonar.host.url": "http://sonar:9000")
                properties("sonar.pitest.mode": "reuseReport")
                mavenInstallation("maven-latest")
                providedGlobalSettings("talk-to-local-nexus")
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
            name("${it.name}/${it.name}_deploy_to_local-nexus")
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
