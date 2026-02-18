# Minikube setup

https://minikube.sigs.k8s.io/docs/

https://minikube.sigs.k8s.io/docs/start/?arch=%2Flinux%2Fx86-64%2Fstable%2Fbinary+download

```shell
mkdir minikube
cd minikube
curl -LO https://github.com/kubernetes/minikube/releases/latest/download/minikube-linux-amd64
sudo install minikube-linux-amd64 /usr/local/bin/minikube && rm minikube-linux-amd64
```

```shell
minikube start

ck@node01:~/minikube$ minikube start
* minikube v1.35.0 on Ubuntu 24.04
* Unable to pick a default driver. Here is what was considered, in preference order:
* Alternatively you could install one of these drivers:
  - docker: Not installed: exec: "docker": executable file not found in $PATH
  - kvm2: Not installed: exec: "virsh": executable file not found in $PATH
  - podman: Not installed: exec: "podman": executable file not found in $PATH
  - qemu2: Not installed: exec: "qemu-system-x86_64": executable file not found in $PATH
  - virtualbox: Not installed: unable to find VBoxManage in $PATH

X Exiting due to DRV_NOT_DETECTED: No possible driver was detected. Try specifying --driver, or see https://minikube.sigs.k8s.io/docs/start/
```

https://minikube.sigs.k8s.io/docs/drivers/kvm2/

https://help.ubuntu.com/community/KVM/Installation
sudo apt install cpu-checker

sudo apt-get install qemu-kvm libvirt-daemon-system libvirt-clients bridge-utils

ck@node01:~/minikube$ sudo adduser `id -un` libvirt
info: The user `ck' is already a member of `libvirt'.
ck@node01:~/minikube$ sudo adduser `id -un` kvm
info: Adding user `ck' to group `kvm' ...
ck@node01:~/minikube$

ck@node01:~$ groups
ck adm cdrom sudo dip plugdev users lpadmin libvirt kvm
ck@node01:~$ virsh list --all
Id   Name   State
--------------------

ck@node01:~$ virt-host-validate
QEMU: Checking for hardware virtualization                                 : PASS
QEMU: Checking if device /dev/kvm exists                                   : PASS
QEMU: Checking if device /dev/kvm is accessible                            : PASS
QEMU: Checking if device /dev/vhost-net exists                             : PASS
QEMU: Checking if device /dev/net/tun exists                               : PASS
QEMU: Checking for cgroup 'cpu' controller support                         : PASS
QEMU: Checking for cgroup 'cpuacct' controller support                     : PASS
QEMU: Checking for cgroup 'cpuset' controller support                      : PASS
QEMU: Checking for cgroup 'memory' controller support                      : PASS
QEMU: Checking for cgroup 'devices' controller support                     : WARN (Enable 'devices' in kernel Kconfig file or mount/enable cgroup controller in your system)
QEMU: Checking for cgroup 'blkio' controller support                       : PASS
QEMU: Checking for device assignment IOMMU support                         : WARN (No ACPI DMAR table found, IOMMU either disabled in BIOS or not supported by this hardware platform)
QEMU: Checking for secure guest support                                    : WARN (Unknown if this platform has Secure Guest support)
LXC: Checking for Linux >= 2.6.26                                         : PASS
LXC: Checking for namespace ipc                                           : PASS
LXC: Checking for namespace mnt                                           : PASS
LXC: Checking for namespace pid                                           : PASS
LXC: Checking for namespace uts                                           : PASS
LXC: Checking for namespace net                                           : PASS
LXC: Checking for namespace user                                          : PASS
LXC: Checking for cgroup 'cpu' controller support                         : PASS
LXC: Checking for cgroup 'cpuacct' controller support                     : PASS
LXC: Checking for cgroup 'cpuset' controller support                      : PASS
LXC: Checking for cgroup 'memory' controller support                      : PASS
LXC: Checking for cgroup 'devices' controller support                     : FAIL (Enable 'devices' in kernel Kconfig file or mount/enable cgroup controller in your system)
LXC: Checking for cgroup 'freezer' controller support                     : FAIL (Enable 'freezer' in kernel Kconfig file or mount/enable cgroup controller in your system)
LXC: Checking for cgroup 'blkio' controller support                       : PASS
LXC: Checking if device /sys/fs/fuse/connections exists                   : PASS

Ignored this and just ran it:

minikube config set driver kvm2

minikube start

ck@node01:~/repos/tdk8s$ minikube kubectl -- get pods -A
NAMESPACE     NAME                               READY   STATUS    RESTARTS        AGE
kube-system   coredns-668d6bf9bc-xtdqt           1/1     Running   1 (2m9s ago)    8h
kube-system   etcd-minikube                      1/1     Running   1 (2m14s ago)   8h
kube-system   kube-apiserver-minikube            1/1     Running   1 (2m13s ago)   8h
kube-system   kube-controller-manager-minikube   1/1     Running   1 (2m14s ago)   8h
kube-system   kube-proxy-4vvbh                   1/1     Running   1 (2m14s ago)   8h
kube-system   kube-scheduler-minikube            1/1     Running   1 (2m14s ago)   8h
kube-system   storage-provisioner                1/1     Running   1 (2m14s ago)   8h

minikube status

minikube stop

## Containerd

minikube start --container-runtime=containerd

## Dashboard

minikube addons enable dashboard

## Registry

minikube addons enable registry

Create pvc.
cd ./manifests
kubectl apply -f registry-pvc.yaml
kubectl patch deployment registry -n kube-system --patch-file registry.json

minikube ssh
sudo su
$ cd /etc/containerd/
$ ls -l
total 4
drwxr-xr-x 4 root root   80 Feb 17 09:18 certs.d
-rw-r--r-- 1 root root 1919 Feb 17 09:01 config.toml
$ cd certs.d/
$ ls -l
total 0
drwxr-xr-x 2 root root 60 Feb 17 09:19 192.168.39.55:5000
drwxr-xr-x 2 root root 60 Jan 27 23:01 docker.io
$ cd 192.168.39.55\:5000/
$ more hosts.toml
server = "https://192.168.39.55:5000"
[host."http://192.168.39.55:5000"]
capabilities = ["pull","resolve","push"]
skip_verify = true

systemctl stop containerd
systemctl start containerd

## Tekton

kubectl apply --filename https://storage.googleapis.com/tekton-releases/pipeline/latest/release.yaml

kubectl get pods --namespace tekton-pipelines --watch


### minikube default build 1.38

minikube start --vm-driver kvm2 --insecure-registry "192.168.0.0/16"

minikube addons enable registry

Create pvc.
cd ./manifests
kubectl apply -f registry-pvc.yaml
kubectl patch deployment registry -n kube-system --patch-file registry.json

kubectl apply --filename https://storage.googleapis.com/tekton-releases/pipeline/latest/release.yaml

kubectl get pods --namespace tekton-pipelines --watch



