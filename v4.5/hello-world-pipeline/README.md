# Used commands
The commands below were used to perform a pipeline based on Tekton technology.
```
oc new-project pipelines-tutorial
oc create -f greetings-task.yaml -n pipelines-tutorial
oc create -f farewell-task.yaml -n pipelines-tutorial
oc create -f pipeline.yaml -n pipelines-tutorial
oc create -f pipeline-run.yaml -n pipelines-tutorial
tkn task list
tkn pipeline list
```
