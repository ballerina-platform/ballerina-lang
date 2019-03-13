#!/bin/bash
# Copyright (c) 2019, WSO2 Inc. (http://wso2.org) All Rights Reserved.
#
# WSO2 Inc. licenses this file to you under the Apache License,
# Version 2.0 (the "License"); you may not use this file except
# in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.

function pod_ready() {
  [[ "$(kubectl get pods "$1" -o 'jsonpath={.status.conditions[?(@.type=="Ready")].status}')" == 'True' ]]
}

function pods_ready() {
  local pod

  [[ "$#" == 0 ]] && return 0

  for pod in $pods; do
    #If pod is not ready returns 1
    pod_ready "$pod" || return 1
  done
  # All pods ready
  echo "All pods ready"

  return 0
}

function wait_until_pods_ready() {
  local period interval i pods

  if [[ $# != 2 ]]; then
    echo "Usage: wait-until-pods-ready PERIOD INTERVAL" >&2
    echo "" >&2
    echo "This script waits for all pods to be ready in the current namespace." >&2

    return 1
  fi

  period="$1"
  interval="$2"

  for ((i=0; i<$period; i+=$interval)); do
    pods="$(kubectl get pods -o 'jsonpath={.items[*].metadata.name}')"
    if pods_ready $pods; then
      return 0
    fi

    echo "Waiting for the pods to be ready..."
    sleep "$interval"
  done

  echo "Waited for $period seconds, but all pods are not ready yet."
  return 1
}

wait_until_pods_ready $@