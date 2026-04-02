# Kubernetes Setup

The following provides detailed instructions for how to run the integration tests on different
flavours of kubernetes.

* [microk8s](microk8s/README.md)
* [minikube](minikube/README.md)
* [k3s](k3s/README.md)
* KiND ?
* GitHub Actions ?
* AWS ?

## Image Registry

Once the `k8s` image registry is setup you need to update a few files in this repository with the
correct location of the registry.

If you want you can use the following script to perform this automatically:

```shell
./updateImageRegsitry.sh 192.168.68.130:5000
```

or for minikube:

```shell
./updateImageRegistry.sh $(minikube ip)
```