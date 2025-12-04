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

## Fabric8 Test Service Account

We need to have a service account which has permissions to query the kubernetes api which runs the tekton pipelines.

In openshift this is provided by the `pipeline` service account.

```shell
kubectl create serviceaccount tekton-pipeline-test
kubectl create role test-reader --verb=get --verb=list --verb=watch --resource=pods
kubectl create rolebinding test-binding --role=test-reader --serviceaccount=default:tekton-pipeline-test
```
Confirm permissions
```shell
kubectl auth can-i --list --as=system:serviceaccount:default:tekton-pipeline-test
```


## Services

kubectl expose pod casual-jca-test --port=9990
curl -iv http://10.43.92.138:9990
kubectl expose pod casual-jca-test --name=casual-jca-test-lb --type=LoadBalancer --port=9990
kubectl get pods -A
kubectl logs svclb-casual-jca-test-lb-4eb028a5-qdmb6 -n kube-system

The following exposes externally the pod on port 9990:
```shell
kubectl expose pod casual-java-test --name=casual-java-test-lb2 --type=LoadBalancer --port=9990
```
Which can be accessed through the external ip:
```shell
ck@node02:~$ curl -iv 192.168.68.130:9990
*   Trying 192.168.68.130:9990...
* Connected to 192.168.68.130 (192.168.68.130) port 9990 (#0)
> GET / HTTP/1.1
> Host: 192.168.68.130:9990
> User-Agent: curl/7.81.0
> Accept: */*
>
* Mark bundle as not supporting multiuse
  < HTTP/1.1 302 Found
  HTTP/1.1 302 Found
  < Connection: keep-alive
  Connection: keep-alive
  < Location: /console/index.html
  Location: /console/index.html
  < Content-Length: 0
  Content-Length: 0
  < Date: Mon, 31 Mar 2025 07:45:04 GMT
  Date: Mon, 31 Mar 2025 07:45:04 GMT

<
* Connection #0 to host 192.168.68.130 left intact
```

Though if you try and create two with the same port, it will be stuck pending waiting for an external ip, as 9990 is already bound i assume.
Yes, confirmed this by deleting the original, as soon as that happens the service is no longer pending and receives the external ip.

Is is also possible to portforward to a service or a deployment.

So if we can check if we are running inside a container, then we can determine if we want to run a port forward or not.

The question remains though, how we want to access the resources.

Should you be able to get anything inside the namespace or should you be only able to get things you have created?

I don't see the need for the restriction, though equally, why are you going to use the tooling if you are not doing some for of setup.

I am not sure i want to have a failover which check services first and then checks pods blah.

but if you are wanting a connection and there is not service it would fail anyway - so i guess it will be fine.

if you are wanting a connection and it is a service - you use that. 

if you are wanting a connection and there is no service - you get a port forward to the pod.

if you are wanting a connection and it is a service, but you are not inside the container runtime you need a portforward to the service.

Can add an override saying - allow port-foward false if we really wanted?