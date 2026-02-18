#!/bin/bash

set -eu

ip=$(minikube ip)

echo "minikube ip is: " $ip

file="server = \"https://$ip:5000\"
[host.\"http://$ip:5000\"]
  capabilities = [\"pull\",\"resolve\",\"push\"]
  skip_verify = true"

echo "Creating host.toml:"
minikube ssh -- "sudo mkdir -p /etc/containerd/certs.d/$ip:5000 && echo '$file' | sudo tee /etc/containerd/certs.d/$ip:5000/hosts.toml"
echo "Check hosts.toml:"
minikube ssh -- "sudo cat /etc/containerd/certs.d/$ip:5000/hosts.toml"
minikube ssh -- "sudo systemctl stop containerd && sudo systemctl start containerd"
echo "restarted containerd"
echo "done"
