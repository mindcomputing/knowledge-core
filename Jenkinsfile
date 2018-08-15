pipeline {
	agent any
	options {
		buildDiscarder(logRotator(numToKeepStr: '5', artifactNumToKeepStr: '5'))
	}
	tools {
		// Maven installation declared in the Jenkins "Global Tool Configuration"
		maven 'M3' 
	}
	stages {
		stage('Build') {
			steps {
				sh "mvn clean install"
				openTasks high: 'FIXME', normal: 'TODO', pattern: '**/*.java'
			}
		}
	}
	post { 
		always { 
			junit 'target/**/surefire-reports/**/*.xml'
			cleanWs()
		}
	}
}