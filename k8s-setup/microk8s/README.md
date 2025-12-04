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

