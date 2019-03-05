#!/bin/bash

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