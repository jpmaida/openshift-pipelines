# Openshift Pipelines

Repository dedicated to the creation of pipelines hosted on Openshift. Each directory represents an Openshift version and inside each one there is a different implementation based on the Openshift technology using several techniques. Below, you can see a detailed explanation about this structure.

```
.
├── README.md
├── v3.11
│   └── jenkins-deploy-springboot-container             ~> Pipeline CI/CD using Jenkins to deploy SpringBoot application in Openshift
├── v4.5
│   └── tekton-hello-world-pipeline                     ~> Pipeline CI/CD using Tekton to do a simple hello world
└── v4.7
    └── tekton-deploy-jbosseap7-triggeredby-gitlabci    ~> Pipeline CI/CD using Tekton to deploy a JEE application in JBoss EAP 7.x environment.
```