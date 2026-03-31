# k3s setup

See https://docs.k3s.io for the latest installation instructions.

The following worked on ubuntu LTS 24.

## Install

https://docs.k3s.io/quick-start

```shell
curl -sfL https://get.k3s.io | sh -
```

Start k3s.

```shell
systemctl start k3s
```

### kubectl

```shell
cp /etc/rancher/k3s/k3s.yaml > ~/.kube/config`
```

Add the namespace into the configuration.

```shell
kubectl config set-context --current --namespace default
```

## Stop / Start

Stop:
```shell
systemctl stop k3s
```

Start:
```shell
systemctl start k3s
```

## Registry

Following instructions here:

https://rpi4cluster.com/k3s-docker-registry/

```shell
kubectl create namespace docker-registry
```

Create a pvc and deployment for the registry.

```shell
cd ./manifests
kubectl apply -f registry-pvc.yaml
kubectl apply -f docker-registry.yaml
````

Create a svc for the regsitry:

```shell
kubectl expose deployment registry -n docker-registry
```

Edit as root `/etc/rancher/k3s/registries.yaml`, set host ip according to your configuration.

```yaml
mirrors:
  docker-registry:
    endpoint:
      - "http://192.168.68.130:5000"
```