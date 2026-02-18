# microk8s Setup

https://microk8s.io/

sudo snap install microk8s --classic

microk8s config > ~/.kube/config

Needed to add namespace to config file.

microk8s kubectl config view --raw > ~/.kube/config

Add the namespace into the configuration.

kubectl config set-context --current --namespace default

Needed to add kubectl snap before it would connect on integration tests.

sudo snap install kubectl --classic

microk8s stop

## Dashboard
microk8s enable dashboard

## Registry
microk8s enable registry

### Enable insecure registry pull.

sudo mkdir -p /var/snap/microk8s/current/args/certs.d/192.168.68.106:32000
sudo touch /var/snap/microk8s/current/args/certs.d/192.168.68.106:32000/hosts.toml

```toml
# /var/snap/microk8s/current/args/certs.d/192.168.68.106:32000/hosts.toml
server = "http://192.168.68.106:32000"

[host."http://192.168.68.106:32000"]
capabilities = ["pull", "resolve"]
```

microk8s stop
microk8s start


## Tekton

kubectl apply --filename https://storage.googleapis.com/tekton-releases/pipeline/latest/release.yaml

kubectl get pods --namespace tekton-pipelines --watch



