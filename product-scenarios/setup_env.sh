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

parent_path=$( cd "$(dirname "${BASH_SOURCE[0]}")" ; pwd -P )
. ${parent_path}/utils.sh

input_dir=$1
output_dir=$2

# Read infrastructure.properties content into an associative array
declare -A infra_config
read_property_file "${input_dir}/infrastructure.properties" infra_config

# Update kube config to point to the existing cluster
aws eks update-kubeconfig --name ${cluster_name}
# Create a custom random namespace
custom_namespace=$(generate_random_namespace)
kubectl create namespace ${custom_namespace}
# Enforce the created namespace for future kubectl usages
kubectl config set-context $(kubectl config current-context) --namespace=${custom_namespace}

# Install ballerina
install_ballerina "0.990.3"

# Store namespace to be cleaned up at the end
echo "NamespacesToCleanup=${custom_namespace}" >> ${output_dir}/infrastructure-cleanup.properties

