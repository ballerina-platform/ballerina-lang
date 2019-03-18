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

readonly deployment_http_parent_path=$( cd "$(dirname "${BASH_SOURCE[0]}")" ; pwd -P )
readonly deployment_http_grand_parent_path=$(dirname ${deployment_http_parent_path})
readonly deployment_http_great_grand_parent_path=$(dirname ${deployment_http_grand_parent_path})

. ${deployment_http_grand_parent_path}/util/usage.sh
. ${deployment_http_grand_parent_path}/util/setup-deployment-env.sh ${INPUT_DIR} ${OUTPUT_DIR}

function setup_deployment() {
    bal_path=${deployment_http_great_grand_parent_path}/http/src/test/resources/sources/circuit-breaker/http_circuit_breaker_frontend.bal
    replace_variables_in_bal_file
    build_and_deploy_guide
    wait_for_pod_readiness
    retrieve_and_write_properties_to_data_bucket
    local is_debug_enabled=${infra_config["isDebugEnabled"]}
    if [ "${is_debug_enabled}" = "true" ]; then
        print_kubernetes_debug_info
    fi
}

## Functions
function print_kubernetes_debug_info() {
    cat ${bal_path}
    kubectl get pods
    kubectl get svc circuit_breaker_service -o=json
    kubectl get nodes --output wide
}

function replace_variables_in_bal_file() {
    sed -i "s:<USERNAME>:${docker_user}:g" ${bal_path}
    sed -i "s:<PASSWORD>:${docker_password}:g" ${bal_path}
    sed -i "s:scenarios.ballerina.io:${docker_user}:g" ${bal_path}
}

function build_and_deploy_guide() {
    cd ${deployment_http_great_grand_parent_path}/http/src/test/resources/sources
    echo "Circuit Breaker http_circuit_breaker_frontend bal file"
    cat circuit-breaker/http_circuit_breaker_frontend.bal
    ${ballerina_home}/bin/ballerina init
    ${ballerina_home}/bin/ballerina build circuit-breaker --skiptests
    kubectl apply -f target/kubernetes/circuit-breaker
    cd
}

function retrieve_and_write_properties_to_data_bucket() {
    kubectl get svc
    local lb_ingress_host=$(kubectl get svc circuit_breaker_service -o jsonpath='{.status.loadBalancer.ingress[0].hostname}')
    declare -A deployment_props
    deployment_props["LB_INGRESS_HOST"]=${lb_ingress_host}
    write_to_properties_file ${OUTPUT_DIR}/deployment.properties deployment_props
    local is_debug_enabled=${infra_config["isDebugEnabled"]}
    if [ "${is_debug_enabled}" = "true" ]; then
        echo "LB_INGRESS_HOST: ${lb_ingress_host}"
    fi
}

setup_deployment
