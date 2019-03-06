#!/bin/bash

parent_path=$( cd "$(dirname "${BASH_SOURCE[0]}")" ; pwd -P )
. ${parent_path}/utils.sh

input_dir=$1
output_dir=$2

declare -A infra_config

read_property_file "${input_dir}/infrastructure.properties" infra_config

aws eks update-kubeconfig --name ${cluster_name}

custom_namespace=$(generate_random_namespace)

kubectl create namespace ${custom_namespace}

kubectl config set-context $(kubectl config current-context) --namespace=${custom_namespace}

install_ballerina "0.990.3"

kubectl config view

kubectl config current-context

echo "NamespacesToCleanup=${custom_namespace}" >> ${output_dir}/infrastructure-cleanup.properties

