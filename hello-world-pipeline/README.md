# Used commands
The commands below were used to perform a pipeline based on Tekton technology.
```
oc new-project pipelines-tutorial
oc create -f build-task.yaml
oc create -f pipeline.yaml
tkn task list
tkn pipeline list
```