#!/usr/bin/env sh

# Install git-clone task.
kubectl apply -f https://raw.githubusercontent.com/tektoncd/catalog/main/task/git-clone/0.9/git-clone.yaml

# Install buildah task.
kubectl apply -f https://api.hub.tekton.dev/v1/resource/tekton/task/buildah/0.8/raw

# Install all custom tasks.
kubectl apply -Rf ./tasks/