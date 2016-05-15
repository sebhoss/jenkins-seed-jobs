import groovy.io.FileType

String jobBasePath = 'stable'

folder(jobBasePath) {
    description 'Contains stable modules'
}

def jobNames = []
def gitBasePath = "/var/git/stable"
def base = new File(gitBasePath)
base.eachDir() { directory ->
    def path = directory.path
    def pom = new File(path + "/pom.xml")
    if (pom.exists()) {
        def project = path.substring(gitBasePath.length() + 1).replaceAll('/','-')
        def jobName = "$jobBasePath/$project"
        jobNames << jobName
        job(jobName) {
            scm {
                git(path)
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
}

listView("$jobBasePath/Failure") {
    description('All failing jobs of stable modules')
    jobs {
        names(jobNames as String[])
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
        names(jobNames as String[])
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
