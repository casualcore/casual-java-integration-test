#!/usr/bin/env bash

set -e

if [ $# -eq 0 ] || [ -z "$1" ]; then
    echo "Usage: $0 <new-registry>"
    exit 1
fi

reg=$1

echo "Replacing image registry with: $reg"

find ../casual/casual-java-integration-test/src/ -type f -exec sed -i "s/192.168.68.106:32000/$reg/g" {} \;
find ../tekton/pipelinerun/ -type f -exec sed -i "s/192.168.68.106:32000/$reg/g" {} \;

echo "Done."