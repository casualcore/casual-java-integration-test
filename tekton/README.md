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
Where possible existing tekton tasks from artifact hub are used.
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

## Multiple binding pvc

It is not possible by default to share multiple pvcs with pipelineruns and taskruns.
This is due to issues with availability zones in cloud providers where pods might not be able to access the same pvc if they are
scheduled in different availability zones. Therefore tekton tries to keep taskruns in the same node if they have a pvc. This is not possible
if there are multiple pvcs though.

https://tekton.dev/docs/pipelines/workspaces/#availability-zones

There is however a tekton feature flag called coscehdule which has a default value of `workspaces`, which can be changes to `pipelineruns`, which will
allow mounting of multiple pvcs.
Typical use case is a single node cluster with minikube and microk8s - so changing this should have no impact.

https://tekton.dev/docs/pipelines/additional-configs/#customizing-the-pipelines-controller-behavior

kubectl edit configmap feature-flags -n tekton-pipelines

update and save change:

`coscheulde: pipelineruns`

Changes will take effect with next run.
