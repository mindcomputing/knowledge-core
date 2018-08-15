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
				//DEPLOY should be specified in jenkins -> configure system -> env variables - if you don't want it to deploy, leave the value blank.
				//Or, set it to something like 'deploy -DaltDeploymentRepository=snapshotRepo::default::http://52.61.165.55:9092/nexus/content/repositories/snapshots/'
				sh "mvn clean install $DEPLOY"
				openTasks high: 'FIXME', normal: 'TODO', pattern: '**/*.java'
			}
		}
	}
	post { 
		always { 
			junit '**/target/surefire-reports/*.xml'
			cleanWs()
		}
	}
}