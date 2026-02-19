# Tekton

This folder contains the required tekton `pipelines` and `tasks`.

It also includes the `pipelinerun` definition which is used to run
the integration test `pipeline`.

## Install

Initial installation of tekton can be performed using the following command:

```shell
kubectl apply --filename https://storage.googleapis.com/tekton-releases/pipeline/latest/release.yaml
```
Monitor and wait for all resources to become ready:
```shell
kubectl get pods --namespace tekton-pipelines --watch
```

## Tasks
Where possible existing tekton tasks from tekton hub are used.
When a custom task is required it is provided within the `tasks` folder.

The required tasks, both from the hub and tasks folder are installed into 
the cluster using the provided script:

```shell
./deploy-tekton-tasks.sh
```

## Pipelines
The pipeline for running integration tests is parameterised to determine primarily
where the different artifacts should be built or downloaded from.

To install the pipeline into the cluster run the following:

```shell
./deploy-tekton-pipeline.sh
```

