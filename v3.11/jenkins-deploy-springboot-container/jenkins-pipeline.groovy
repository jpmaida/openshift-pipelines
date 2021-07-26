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
						def sonarqubeUrl = "<route_to_sonarqube>"
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
						openshift.withProject(env.PROJECT_NAME){
							def pingPongDC = openshift.selector("dc/${env.APP_NAME}")
							if(pingPongDC.exists()){
								echo "Aplicação já existe."
								openshift.apply('-f ./ocp/configmap.yaml')
								def bc = openshift.selector("bc/${env.APP_NAME}")
								bc.startBuild("--from-file=example/target/${env.APP_NAME}.jar", "--wait=true").logs('-f')
								openshift.selector("dc/${env.APP_NAME}").rollout().status()
							} else {
								echo "Aplicação ainda não existe."
								openshift.create('-f ./ocp/configmap.yaml')
								openshift.newBuild("--name=${env.APP_NAME} --image-stream=openshift/java:8 -l app=${env.APP_NAME}", "--binary=true")
								openshift.selector("bc/${env.APP_NAME}").startBuild("--from-file=example/target/${env.APP_NAME}.jar", "--wait=true").logs('-f')
								openshift.newApp("--name=${env.APP_NAME} --image-stream=${env.APP_NAME}:latest")
								openshift.raw("expose svc/${env.APP_NAME}")
								openshift.set("env dc/${env.APP_NAME} MATCH_TIME_IN_MINUTES=10")
								openshift.set("env dc/${env.APP_NAME} --from=configmap/ping-pong-config --overwrite")
							}
						}
					}
				}
			}
		}
    }
}
