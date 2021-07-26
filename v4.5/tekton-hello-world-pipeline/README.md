# Pipeline Hello World Tekton 

## Steps to create the pipeline
The commands below were used to create a pipeline based on Tekton technology.

First, login into the Openshift Cluster.
```
oc login --server=<openshift_cluster_url_login>
```

Then, create a new namespace to host the pipeline.
```
oc new-project <namespace_name>
```

Finally, create the tasks and the pipeline.
```
oc create -f tasks/greetings-task.yaml
oc create -f tasks/farewell-task.yaml
oc create -f pipeline.yaml
```

## Executing the pipeline
Execute the following comamnd to execute the pipeline:
```
oc create -f pipeline-run.yaml
```

### Observation!
Changed something in the pipeline-run.yaml and want to run the pipeline again ?
```
oc create -f pipeline-run.yaml -n pipelines-tutorial
```
You just have to create the PipelineRun again. You may wonder "Shouldn't this operation fail ?" and the answer is "No !". This happens because the PipelineRun get a new generated name every time is created. See the YAML file for more details.

## Some useful commands

### To see all the tasks
```
tkn task list
```

### To see all the pipelines
```
tkn pipeline list
```

### To see all the pipeline runs
```
tkn pipelinerun list
```

### Need to see some logs ?
```
tkn pipelinerun logs <pipeline-run-name> -f -n <namespace>
```


