# Casual Java Integration Test

The purpose of this repository is to perform integration testing on casual java.

It provides two main functions:
* build the images for usage during integration testing.
* run integration tests.

## Build Images

The build stage ensures all images required during integration testing are available
within the target `k8s` compliant cluster / image registry.

Image builds are run within the cluster using the provided `tekton` pipelines.

As casual java integration tests need casual-java (jca) and casual (native) images there are pipelines
to create both these types of images.

Casual-java (jca) images are built using the desired versions of the different casual java 
repositories e.g. `casual-java`, `casual-caller` etc are used to build an image to test.

Casual (native) images are built by extending the dockerhub published images to configure the domain.

## Run Integration Tests

Once the images are built, they can then be tests within the `k8s` compliant cluster using `tdk8s`.

The concept for integration tests is that they should perform tests that are not possible
from within unit tests, though should still not take a "long time" to run, ideally less
than an hour for all integration tests.

Initial integration tests provided include:
* Test of casual-java (jca) connected to casual (native) version 1.6, 1.7 and 1.8.
* Test of casual-java (jca) fielded.
* Test of casual (native) version 1.8 against casual (native) version 1.6, 1.7 and 1.8. 

All tests that are run here are expected to be run prior to/ in conjunction with a PR 
and a release.

The answer to the question of which tests have been run for a given release must always
be "All the tests.". Any other answer becomes too quickly very complicated.

These integration tests are limited to functional tests and do not currently include non functional tests.

Readiness and Liveness probes must be configured carefully to ensure low latency startups of the environments
and reducing the delay prior to testing being "fired".

## Getting Started

The integration test images and tests shall run on a simple `k8s` cluster which has the following:
* Image Registry - for storing the build docker images.
* Tekton - installed and configured.

The [k8s-setup](k8s-setup/README.md) folder provides some examples for how this can be setup.
The [tekton](tekton/README.md) folder provides details of how to setup tekton on a k8s cluster.

## Build Images

The current integration tests required 4 images:

* 3 casual images - version `1.6`, `1.7`, `1.8` each with a simple `domain.yaml`.
* 1 casual-java image.

### Casual Images

_NB - If you have not already done so, modify the `image-registry` parameter to reflect your `k8s` configuration 
using the [Image Registry Update Script](./k8s-setup/README.md#image-registry)._

To build the casual images - use the 3 `pipelinerun` files.
* `pipelinerun/build-casual-repo-pipeline-run-1.6.yaml`
* `pipelinerun/build-casual-repo-pipeline-run-1.7.yaml`
* `pipelinerun/build-casual-repo-pipeline-run-1.8.yaml`

```shell
kubectl create -f pipelinerun/build-casual-repo-pipeline-run-1.6.yaml
kubectl create -f pipelinerun/build-casual-repo-pipeline-run-1.7.yaml
kubectl create -f pipelinerun/build-casual-repo-pipeline-run-1.8.yaml 
```

To monitor the progress of the pipelines you can use:

```shell
kubectl get pr -w
```
Or monitor each taskrun with:
```shell
kubectl get tr -w
```

Once complete delete the pr objects to remove the resulting PVC workspaces.

### Casual Java Image

_NB - If you have not already done so, modify the `image-registry` parameter to reflect your `k8s` configuration
using the [Image Registry Update Script](./k8s-setup/README.md#image-registry)._

To build the casual java image 4 repositories are required:
* `casual-java-integration-test` - this repository - contains docker files.
* `casual-java` - main casual jca repository.
* `casual-caller` - casual caller optional addon.
* `casual-test-apps` - test application to be used within integration testing.

For each of these repositories you can specify which git branch or tag you wish to build from using
the `revision` parameter for the respective repositories:

There is also an optional `build` parameter for 3 of the repositories, which determines if the artifacts
used in the image should be built or not. If they are not built - the version of the repository revision
is determined from the `versions.gradle` and used to download the artifacts from maven central.

The `image-registry` parameter should be set according to your k8s configuration (see note above).

The `casual-java-image` parameter is used to push the resulting image to the registry and should be updated
in the integration test  [CasualJavaResources](casual/casual-java-integration-test/src/integration/java/se/laz/casual/test/CasualJavaResources.java).

The example below shows how to build an image with:
* a new `casual-java` built from source from the feature branch `feature/my-feature`
* the latest version of `casual-caller` downloaded.

```yaml
  params:
    - name: image-registry
      value: 192.168.68.130:5000
    - name: repository-casual-java-integration-test
      value:
        revision: "feature/tekton-build"
    - name: repository-casual-java
      value:
        revision: "feature/my-feature"
        build: "true"
    - name: repository-casual-caller
      value:
        revision: "dev"
        build: "false"
    - name: repository-casual-java-test-apps
      value:
        revision: "dev"
        build: "true"
    - name: casual-java-image
      value:
        name: "casual-java"
        version: "3.3.10-SNAPSHOT"
```

Run the build pipeline using the following:

```shell
kubectl create -f pipelinerun/build-casual-java-repo-pipeline-run.yaml
```

To monitor the progress of the pipelines you can use:

```shell
kubectl get pr -w
```
Or monitor each taskrun with:
```shell
kubectl get tr -w
```

Once complete delete the pr object to remove the resulting PVC workspaces.

## Run Integration Tests

The integration tests are written using the `tdk8s` framework to managed the provisioning of `k8s` resources
as needed during the integration tests.

To run the integration tests, ensure that the image registry is configured correctly inside:
* [CasualJavaResources](casual/casual-java-integration-test/src/integration/java/se/laz/casual/test/CasualJavaResources.java)
* [CasualResources](casual/casual-java-integration-test/src/integration/java/se/laz/casual/test/CasualResources.java)

To run the integration tests - either run the test from the integration folder.
Or run them using `./gradlew intTest`

### Run Integration Tests in Tekton

TODO: run integration tests within a tekton pipeline.

We need to have a service account which has permissions to query the kubernetes api which runs the tekton pipelines.

In openshift this is provided by the `pipeline` service account.

In other `k8s` clusters you need to create a service account with the necessary permissions.

```shell
kubectl create serviceaccount tekton-pipeline-test
kubectl create role test-reader --verb=get --verb=list --verb=watch --resource=pods
kubectl create rolebinding test-binding --role=test-reader --serviceaccount=default:tekton-pipeline-test
```
Confirm permissions
```shell
kubectl auth can-i --list --as=system:serviceaccount:default:tekton-pipeline-test
```

### Troubleshooting

A common issue occurs when the default namespace in the client `~/.kube/config` is not specified.
Use the following command to ensure this is set:
```shell
kubectl config set-context --current --namespace default
```

If you have an issue where you stop / kill the JVM running integration tests, resulting in cleanup not running.
You can delete the `k8s` resources that remain using the following commands:

```shell
kubectl delete all -l tdk8s
kubectl delete cm -l tdk8s
```