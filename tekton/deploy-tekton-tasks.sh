#!/usr/bin/env sh

# Install git-clone task.
kubectl apply -f https://github.com/tektoncd/catalog/raw/main/task/git-clone/0.10/git-clone.yaml

# Install buildah task.
kubectl apply -f https://github.com/tektoncd/catalog/raw/main/task/buildah/0.9/buildah.yaml

# Install all custom tasks.
kubectl apply -Rf ./tasks/