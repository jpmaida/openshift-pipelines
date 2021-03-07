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
tkn pipelinerun list
```

Need to see some logs ?
```
tkn pipelinerun logs <pipeline-run-name> -n pipelines-tutorial
```

Changed something in the pipeline-run.yaml and want to run the pipeline again ?
```
oc create -f pipeline-run.yaml -n pipelines-tutorial
```
You just have to create the PipelineRun again. You may wonder "Shouldn't this operation fail ?" and the answer is "No !". This happens because the PipelineRun get a new generated name every time is created. See the YAML file for more details.
