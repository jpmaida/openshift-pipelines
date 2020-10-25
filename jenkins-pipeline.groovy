pipeline {
	agent {
		label 'maven'
	}
	options {
		timeout(time: 30, unit: 'MINUTES') 
		buildDiscarder(logRotator(numToKeepStr: '10'))
	}
	stages {
	  	stage('Printing env vars'){
	  		steps{
	  			script{
	  				echo "Environment variables:\n" +
	  						"FOO_NAME: ${env.FOO_NAME}"
	  			}
	  		}
	  	}
    }
}
