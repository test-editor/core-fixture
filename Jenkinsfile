#!groovy

/**

    Requirements for this Jenkinsfile:
    - JDK8 with the id "jdk8"
    - credentials for jcenter api (testeditor.org) under id '1e68e4c1-48a6-428c-8896-42511359493e'

*/

String getGitUrl() {
    return scm.userRemoteConfigs.first().url
}

String getGitUrlAsSsh() {
    return getGitUrl().replace("https://github.com/", "git@github.com:")
}

nodeWithProperWorkspace {
    stage 'checkout'
    checkout scm

    if (isMaster()) {
        echo "is master"
        if (headIsJenkinsCommitted()) {
            // Workaround: we don't want infinite releases.
            echo "Aborting build as the current commit on master is already released."
            return
        }
        echo "recursion check ok"
        // git by default checks out detached, we need a local branch
        sh "git checkout $env.BRANCH_NAME" // workaround for https://issues.jenkins-ci.org/browse/JENKINS-31924
        sh 'git fetch --prune origin +refs/tags/*:refs/tags/*' // delete all local tags
        sh "git reset --hard origin/master"
    } 
    sh "git clean -ffdx"

    def buildTargets = ['clean', 'build']
    def publishTargets = ['release']
    def stageString = 'build'
    if (isMaster()) {
        stageString = 'build and publish'
    }
    
    stage stageString
    withGradleEnv {
        if (isMaster()) {
            echo "master build and publish"
            currentBuild.displayName = getVersion().replaceAll('-SNAPSHOT','')
            withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: '1e68e4c1-48a6-428c-8896-42511359493e', passwordVariable: 'JCENTER_PASSWD', usernameVariable: 'JCENTER_USER']]) {
                sh 'git config user.email "jenkins@ci.testeditor.org"' // used for recursion detection (see 'headIsJenkinsCommitted') 
                sh 'git config user.name "Jenkins"'
                // workaround: cannot push without credentials using HTTPS => push using SSH
                sh "git remote set-url origin ${getGitUrlAsSsh()}"
                sh './gradlew '+publishTargets.join(' ')
                sh 'git checkout develop' 
                sh 'git rebase origin/master'
                sh 'git push'
            }
        } else {
            echo "regular build"
            sh './gradlew '+buildTargets.join(' ')
        }
    }
}

String getVersion() {
    Properties properties = new Properties()
    File propertiesFile = new File('gradle.properties')
    propertiesFile.withInputStream {
        properties.load(it)
    }
    return properties."version"
}

boolean headIsJenkinsCommitted() {
    echo "check last committer"
    sh '''#!/bin/bash
          git show --oneline --format="%ae" HEAD > last.commit.txt
       '''
    def lastCommit = readFile('last.commit.txt').trim()
    return  lastCommit.startsWith('jenkins@ci.testeditor.org')
}

boolean isMaster() {
    return env.BRANCH_NAME == 'master'
}

void withGradleEnv(List envVars = [], def body) {
    String jdkTool = tool name: 'jdk8', type: 'hudson.model.JDK'
    List gradleEnv = [
        "PATH+JDK=${jdkTool}/bin",
        "JAVA_HOME=${jdkTool}"
    ]
    gradleEnv.addAll(envVars)
    withEnv(gradleEnv) {
        body.call()
    }
}
/**
 * Workaround for Jenkins bug with feature branches (workspace has feature%2Fmy_feature in it).
 * See https://issues.jenkins-ci.org/browse/JENKINS-30744 (marked as resolved but still occurs).
 */
void nodeWithProperWorkspace(def body) {
    node {
        ws(getWorkspace()) {
            body.call()
        }
    }
}

def getWorkspace() {
    pwd().replace("%2F", "_")
}
