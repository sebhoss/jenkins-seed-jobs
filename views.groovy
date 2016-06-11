import groovy.json.JsonSlurper

def projectCatalog = new File("/var/git/stable/jenkins-jobs-setup/projects.json")
def slurper = new JsonSlurper()
def jsonText = projectCatalog.getText()
def json = slurper.parseText(jsonText)

listView("Deployments") {
    description("All deploying jobs of modules that use the maven-build-process")
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

listView("Latest Parent") {
    description("All deploying jobs of modules that use the maven-build-process")
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

listView("Failure") {
    description('All failing jobs of deploying jobs')
    jobs {
        json.each {
            name("${it.name}_deploy_to_local-nexus")
            if (!"maven-build-process".equals(it.name)) {
                name("${it.name}_with_latest_parent")
            }
        }
    }
    jobFilters {
        status {
            status(Status.UNSTABLE, Status.FAILED, Status.ABORTED)
            matchType(MatchType.EXCLUDE_UNMATCHED)
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

listView("Success") {
    description('All successful jobs of deploying jobs')
    jobs {
        json.each {
            name("${it.name}_deploy_to_local-nexus")
            if (!"maven-build-process".equals(it.name)) {
                name("${it.name}_with_latest_parent")
            }
        }
    }
    jobFilters {
        status {
            status(Status.STABLE)
            matchType(MatchType.EXCLUDE_UNMATCHED)
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
