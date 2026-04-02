# Minikube setup

See https://minikube.sigs.k8s.io/docs/ for the latest installation instructions.

The following worked on ubuntu LTS 24.

## Install

```shell
mkdir minikube
cd minikube
curl -LO https://github.com/kubernetes/minikube/releases/latest/download/minikube-linux-amd64
sudo install minikube-linux-amd64 /usr/local/bin/minikube && rm minikube-linux-amd64
```

Chose driver you wish to run minikube with:

https://minikube.sigs.k8s.io/docs/drivers/


### Install kvm2 driver

The following describes kvm2 installation.

https://minikube.sigs.k8s.io/docs/drivers/kvm2/

https://help.ubuntu.com/community/KVM/Installation


## Registry

The minikube registry addon is transitive - which results in all published images
disappearing between restarts of minikube. To prevent this a PVC will be used to back the
registry deployment allowing for persistence of images.

There is unfortunately an additional issue with insecure registry access.

Depending on the version of minikube it will either run with docker or containerd.

If docker, there is a configuration required to enable insecure registry pulls. This *must* 
be performed on the first start, otherwise the configuration does change without deleting minikube
and creating a new instance.

If container, the configuration also does not work even on the first creation of the minikube. So a post
startup script is required to be run, to configure insecure registry pulls.

If docker, use the following to start for the first time, with the appropriate subnet for your configuration:

```shell
minikube start --vm-driver kvm2 --insecure-registry "192.168.0.0/16"
```

If containerd, use the following to start for the first time.

```shell
minikube start --vm-driver kvm2 --container-runtime=containerd
```

### Registry

```shell
minikube addons enable registry
```

Create pvc.

```shell
cd ./manifests
kubectl apply -f registry-pvc.yaml
kubectl patch deployment registry -n kube-system --patch-file registry.json
```

If using containerd, after every startup the containerd configuration needs to be updated to add the host
to skip verifications allowing insecure regsitry pulls.

This can be done manually, or using the provided script:

```shell
./minikube-containerd-post-startup.sh
```

### kubectl

Starting minikube configures your local kubectl automatically.

Though you may need to set the current context if it is not set:

```shell
kubectl config set-context --current --namespace default
```

## Stop / Start

Stop:
```shell
minikube stop
```

Start:

```shell
minikube start
```

If using containerd you also need to run the following script:
```shell
./minikube-containerd-post-startup.sh
```

## Dashboard

If you want to use the dashboard, enable the following plugin:


```shell
minikube addons enable dashboard
```