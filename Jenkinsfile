#!groovy
node {
    stage('Load pipeline') {
        dir('jenkins-pipeline-scripts') {
            git url: "https://github.com/test-editor/jenkins-pipeline-scripts.git", branch: "master"
            load "pipelines/gradle-pipeline.groovy"
        }
    }
}