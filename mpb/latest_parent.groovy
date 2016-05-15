import groovy.json.JsonSlurper

def jobBasePath = "mpb"

def projectCatalog = new File("/var/git/stable/jenkins-jobs-setup/mpb/projects.json")
def slurper = new JsonSlurper()
def jsonText = projectCatalog.getText()
def json = slurper.parseText(jsonText)

json.each {
    def project = it
    if (!"maven-build-process".equals(project.name)) {
        job("$jobBasePath/${project.name}_with_latest_parent") {
            scm {
                git(project.repository)
            }
            triggers {
                project.upstreams.each {
                    upstream("$jobBasePath/${it}_deploy_to_local-nexus", "SUCCESS")
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
            name("${it.name}_with_latest_parent")
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

listView("$jobBasePath/Failure") {
    description('All failing jobs of stable modules')
    jobs {
        json.each {
            name("${it.name}_with_latest_parent")
        }
    }
    jobFilters {
        status {
            status(Status.UNSTABLE, Status.FAILED, Status.ABORTED)
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

listView("$jobBasePath/Success") {
    description('All successful jobs of stable modules')
    jobs {
        json.each {
            name("${it.name}_with_latest_parent")
        }
    }
    jobFilters {
        status {
            status(Status.STABLE)
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
