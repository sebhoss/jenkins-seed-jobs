import groovy.json.JsonSlurper

def jobBasePath = "mpb"

folder(jobBasePath) {
    description 'Contains modules build w/ the maven-build-process'
}

def projectCatalog = new File("/var/git/stable/jenkins-jobs-setup/mpb/projects.json")
def slurper = new JsonSlurper()
def jsonText = projectCatalog.getText()
def json = slurper.parseText(jsonText)

json.each {
    def project = it
    job("$jobBasePath/$project.name (deploy to local-nexus)") {
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
                mavenInstallation("maven-3.3.9")
                providedGlobalSettings("talk-to-docker-nexus")
            }
        }
        steps {
            maven {
                goals("sonar:sonar")
                properties("sonar.host.url": "http://sonar:9000")
                mavenInstallation("maven-3.3.9")
                providedGlobalSettings("talk-to-docker-nexus")
            }
        }
    }
}

listView("$jobBasePath/Deployments") {
    description("All deploying jobs of modules that use the maven-build-process")
    jobs {
        json.each {
            name("$it.name (deploy to local-nexus)")
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