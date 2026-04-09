# microk8s Setup

See https://microk8s.io/ for the latest installation instructions.

The following worked on ubuntu LTS 24.

## Install

```shell
sudo snap install microk8s --classic
```

Start microk8s.

```shell
microk8s start
```

### kubectl
Needed to add kubectl snap before it would connect on integration tests.

```shell
sudo snap install kubectl --classic
```

Configure kubectl access:

```shell
microk8s config > ~/.kube/config
microk8s kubectl config view --raw > ~/.kube/config
```

Needed to add namespace to config file.

```shell
kubectl config set-context --current --namespace default
```

## Stop / Start

Stop:
```shell
microk8s stop
```

Start:
```shell
microk8s start
```

## Registry

You must enable the registry plugin and [Enable insecure registry pull](#enable-insecure-registry-pull)

```shell
microk8s enable registry
```

### Enable insecure registry pull.

Replace the registry host and port with your configuration.

```shell
sudo mkdir -p /var/snap/microk8s/current/args/certs.d/192.168.50.178:5000
sudo touch /var/snap/microk8s/current/args/certs.d/192.168.50.178:5000/hosts.toml
```

```toml
# /var/snap/microk8s/current/args/certs.d/192.168.50.178:5000/hosts.toml
server = "http://192.168.50.178:5000"

[host."http://192.168.50.178:5000"]
capabilities = ["pull", "resolve"]
```

Restart microk8s for the change to take effect.

```shell
microk8s stop
microk8s start
```


## Dashboard

If you want to use the dashboard, enable the following plugin:

```shell
microk8s enable dashboard
```