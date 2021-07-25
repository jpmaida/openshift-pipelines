# Pipeline Tekton + GitlabCI de deploy em ambiente JBoss EAP 7

## Arquitetura
![Visão arquitetural e processual](imgs/arquitetura.jpg)

### Observação
Todos os dados relacionados ao payload usado no desenvolvimento desta esteira estão dentro do diretório `gitlab-ci`.

## Etapas de criação do pipeline

### Criação de namespace
```
oc login --server=<openshift_cluster_url_login>
oc new-project <namespace_name_here>
```

### Criação do pipeline
A criação do pipeline consiste na criação do arquivo `pipeline.yaml` e consequentemente seu provisionamento no Openshift. Para provisionar o pipeline a partir do arquivo `pipeline.yaml`, execute:
```
oc create -f pipeline.yaml
```
Os tópicos a seguir descreverão aspectos deste arquivo.

#### Parâmetros
Abaixo segue relação de parâmetros usados no pipeline e suas tasks:
* PAYLOAD: Representa o payload como um todo em sua forma JSON, enviado pela Trigger Template.
* GIT_CLONE_URL: URL do repositório git que deverá ser clonado pela cluster-task git-clone. Este dado está contido no payload.
* GIT_BRANCH: Branch do repositório git que deverá ser clonado pela cluster-task git-clone. Este dado está contido no payload.
* JBOSS_EAP7_SERVER_GROUP: Server group do servidor JBoss EAP 7 onde a aplicação será implantada. Este dado está contido no payload.
* DEPLOYED_ARTIFACT_PATH: Caminho do artefato (ear, war ou jar) compilado que será implantado no ambiente JBoss EAP 7. Este dado está contido no payload.

#### Workspaces
Workspaces representam áreas comuns onde informações são trocadas entre tasks e cluster-tasks ou arquivos que podem ser montados dentro dos containers das tasks. Abaixo, segue relação de workspaces usados:
* shared-workspace
  * Tipo: Persistent Volume Claim (PVC)
  * Claim Name: shared-workspace
* maven-settings
  * Tipo: Config Map
  * Nome do Config Map: maven-settings

### Criação das tasks

#### Task echo-payload
* Definição: Imprime no terminal o payload recebido pelo Trigger Template criado.
* Tipo: Task
  * Arquivo: tasks/echo-payload-task.yaml
* Parâmetros:
  * PAYLOAD: Payload enviado pelo Trigger Template. O payload contém dados necessários para que a esteira consiga realizar suas atividades.

#### Task git-clone
* Definição: Realiza o git clone de um repositório passado por parâmetro.
* Tipo: Cluster Task
* Parâmetros:
  * url: URL do repositório a ser clonado.
  * sslVerify: Flag que habilita ou desabilita a verificação por SSL.
  * deleteExisting: Flag para determinar se qualquer dado temporário deve ser apagado ou não antes do 'git clone'.
  * verbose: Flag que determina se a operação será verbosa ou não.
  * revision: Branch a ser clonada.
  * gitInitImage: Imagem base para o container que representa a task. Este campo já vem preenchido com uma imagem padrão.
* Workspaces:
  * shared-workspace
* Procedimentos adicionais:
  * É necessária a criação de uma secret para o git clone via SSH do código da aplicação.
    ```
    oc create secret generic gitlab-ssh-key --from-file=ssh-privatekey=<path_to_your_private_key> --type="kubernetes.io/ssh-auth"
    ```
  * É necessário anotar a secret.
    ``` 
    oc annotate secret gitlab-ssh-key tekton.dev/git-0='<url_to_gitlab_without_https>:<custom_port_if_exists>'
    ```
  * Para que o código possa ser baixado é necesário associar a secret criado com a services account usadas pelo pipeline.
    ```
    oc secrets link pipeline gitlab-ssh-key
    ```

#### Task verify-cloned-repo
* Definição: Lista arquivos do workspace relacionado à task.
* Tipo: Task
  * Arquivo: tasks/ls-task.yaml
* Workspaces:
  * shared-workspace

#### Task maven-build
* Definição: Realiza um maven build (install, package, etc.) na forma mais geral possível.
* Tipo: Cluster Task
* Parâmetros:
  * MAVEN_IMAGE: Imagem base para o container que representa a task. Este campo já vem preenchido com uma imagem padrão.
  * GOALS: Parâmetro do tipo array que contém os goals (clean, package, install, etc.) a serem usados.
  * CONTEXT_DIR: Diretório base para busca de arquivo pom.xml e submódulos maven.
* Workspaces:
  * shared-workspace
  * maven-settings

#### Task deploy
* Definição: Realize o deploy de um artefato ear, war ou jar no ambiente JBoss EAP 7.
* Tipo: Task
  * Arquivo: tasks/jboss-eap7-deploy.yaml
* Parâmetros:
  * SERVER_GROUP: Representa o server group no qual o artefato será deployado.
  * DEPLOYED_ARTIFACT_PATH: Caminho do artefato que será deployado. Este caminho leva em conta o diretório base de build como início.
  * CONNECTION_DATA_SECRET_NAME: Nome da secret que contém os dados de conexão com o ambiente EAP 7.
* Workspaces:
  * shared-workspace

#### Secret com dados para conexão no EAP 7
```
oc create secret generic jboss-eap7-connection-data --from-literal=CONTROLLER_IP="<controller_ip>" --from-literal=MGNT_USER=<management_user> --from-literal=MGNT_PASSWORD=<management_password>
```