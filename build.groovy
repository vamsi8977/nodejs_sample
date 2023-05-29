pipeline {
  agent any
  options {
    buildDiscarder(logRotator(numToKeepStr:'2' , artifactNumToKeepStr: '2'))
    timestamps()
    }
  stages {
    stage('SCM') {
      steps {
        echo 'Checking out project from Bitbucket....'
        git branch: 'main', url: 'git@github.com:vamsi8977/nodejs_sample.git'
      }
    }
    stage('Build') {
      steps {
        ansiColor('xterm') {
          echo 'NPM Build....'
          sh '''
            npm install
            npm audit fix --force
            npm test
          '''
        }
      }
    }
    stage('SonarQube') {
      steps {
        withSonarQubeEnv('SonarQube') {
          sh "sonar-scanner"
        }
      }
    }
    stage('JFrog') {
      steps {
        ansiColor('xterm') {
          sh '''
            jf rt u test/config.json nodejs/
            jf scan test/config.json --fail-no-op --build-name=nodejs --build-number=$BUILD_NUMBER
          '''
        }
      }
    }
  }
  post {
    success {
      archiveArtifacts artifacts: "out/test-results.xml"
      echo "The build passed."
    }
    failure {
      echo "The build failed."
    }
    cleanup {
      deleteDir()
    }
  }
}
