#!groovy
nodeWithProperWorkspace {
    stage 'checkout'
    checkout scm

    if (isMaster()) {
        if (lastCommitFromJenkins()) {
            // Workaround: we don't want infinite releases.
            echo "Aborting build as the current commit on master is already released."
            return
        }
        // git by default checks out detached, we need a local branch
        sh "git checkout $env.BRANCH_NAME" // workaround for https://issues.jenkins-ci.org/browse/JENKINS-31924
        sh 'git fetch --prune origin +refs/tags/*:refs/tags/*' // delete all local tags
        sh "git reset --hard origin/master"
    } 
    sh "git clean -ffdx"

    stage 'Build'
    withGradleEnv {
        gradle 'clean build install'
    }

    if (isMaster()) {
        stage 'Release'
        currentBuild.displayName = getVersion().replaceAll('-SNAPSHOT', '')
        withGradleEnv {
            sh 'git config user.email "jenkins@ci.testeditor.org"'
            sh 'git config user.name "jenkins"' // used for recursion detection (see 'lastCommitFromJenkins()') 
            // workaround: cannot push without credentials using HTTPS => push using SSH
            sh "git remote set-url origin ${getGithubUrlAsSsh()}"
            withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: '1e68e4c1-48a6-428c-8896-42511359493e', passwordVariable: 'BINTRAY_KEY', usernameVariable: 'BINTRAY_USER']]) {
                gradle 'release -Prelease.useAutomaticVersion=true'
            }
        }
        
        // TODO merge back to develop
    }

}

String getVersion() {
    def properties = readProperties file: 'gradle.properties'
    return properties.version
}