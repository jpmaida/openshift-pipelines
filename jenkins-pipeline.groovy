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
	  	stage('Build') {
			steps {
				script {
					echo "Clone GIT URL: ${env.APP_GIT_URL} da branch: ${env.APP_GIT_BRANCH}"
					git branch: env.APP_GIT_BRANCH, credentialsId: '', url: env.APP_GIT_URL
					echo "Clone realizado com sucesso"

					def pom = readMavenPom file: "${env.POM_FILE_PATH}"
					def version = pom.version
					echo "Definido versão para uma aplicação maven. Versão: ${version}"

					sh "mvn clean install -DskipTests=true -f ${env.POM_FILE_PATH}"
				}
			}
		}
		stage('Test') {			
			steps {
				script{
					sh "mvn test -f ${env.POM_FILE_PATH}"
				}
			}
		}
		stage('Code Analysis') {
			steps {
				script {
					try{
						def sonarqubeUrl = "https://sonarqube-cicd.apps.na311.openshift.opentlc.com "
						sh "mvn sonar:sonar -Dsonar.host.url=${sonarqubeUrl} -DskipTests=true -Dsonar.scm.provider=git -f ${env.POM_FILE_PATH}"
					} catch(Exception e) {
						echo "Erro ao chamar o Sonar. - " + e.getMessage()
					}
				}
			}
		}
		stage('Deploy') {
			steps {
				script {
					openshift.withCluster(){
						def openshiftNamespace = openshift.selector("namespace", env.PROJECT_NAME)
						if(openshiftNamespace.exists()){
							echo "Existe projeto"
						} else {
							echo "N Existe projeto"
						}
					}
				}
			}
		}
    }
}
