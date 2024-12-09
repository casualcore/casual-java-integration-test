# k3s setup

https://docs.k3s.io

## WIP: Instructions

Installed k3s.
https://docs.k3s.io/quick-start

`systemctl status k3s`
`systemctl stop k3s`
`systemctl start k3s`

`k3s-killall.sh`

Uninstall
`/usr/local/bin/k3s-uninstall.sh`

Check images that are available.

`sudo k3s ctr images ls`

Client config:

On server:
`cat /etc/rancher/k3s/k3s.yaml`
Copy contents into `~/.kube/config`

replace hostname with ip (issue with my local dns)

`kubectl get all`

Setup tekton.
https://tekton.dev/docs/getting-started/tasks/

Install:
kubectl apply --filename https://storage.googleapis.com/tekton-releases/pipeline/latest/release.yaml
Check install status:
kubectl get pods --namespace tekton-pipelines --watch


## Install docker-registry
https://rpi4cluster.com/k3s-docker-registry/

`kubectl create namespace docker-registry`

Modify the pvc.yaml and apply. Removed storage class.

Modify the deployment.yaml and apply. Removed node-selector.

Create a svc for the regsitry:

`kubectl expose deployment registry -n docker-registry`

### TLS Registry

`mkdir docker-registry`
`cd docker-registry`
`openssl req -x509 --newkey rsa:4096 -sha256 -days 3650 -nodes -keyout registry.key -out registry.crt -subj "/CN=registry.cube.local" -addext "subjectAltName=DNS:registry.cube.local:*.cube.local,IP:192.168.68.130"`
`kubectl create secret tls docker-registry-tls-cert -n docker-registry --cert=registry.crt --key=registry.key`

Reapply the updated deployment with the envs for TLS and mount.

Add changes for the node TLS trust.
```
ck@node02:~/docker-registry$ sudo cp registry.* /usr/local/share/ca-certificates/
ck@node02:~/docker-registry$ sudo update-ca-certificates
Updating certificates in /etc/ssl/certs...
rehash: warning: skipping ca-certificates.crt,it does not contain exactly one certificate or CRL
1 added, 0 removed; done.
Running hooks in /etc/ca-certificates/update.d...

Adding debian:registry.pem
done.
done.
```

Add dns entry in /etc/hosts
```
192.168.0.202 registry registry.cube.local
```
Create a private registry configuration for k3s:

```yaml
mirrors:
  docker-registry:
    endpoint:
      - "https://192.168.68.130:5000"
configs:
  192.168.68.130:
    tls:
      ca_file: "/usr/local/share/ca-certificates/registry.crt"
      key_file: "/usr/local/share/ca-certificates/registry.key"
```

There is an issue with resolving the registry name inside the containers, i think it might be related to the namespace in which the service is created.

We also still need to use the tls skip verify in the build process as the tls fails to validate.
Not sure why the builder pod doesn't trust it.
Could try and make a cert which comes from letencrypt or similar which will be a trusted issuer, but not sure how to do that compared to the openssl command.
