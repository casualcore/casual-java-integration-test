#!/usr/bin/env sh

# Install custom pipelines.
kubectl apply -Rf ./pipelines
kubectl apply -Rf ./pvc
