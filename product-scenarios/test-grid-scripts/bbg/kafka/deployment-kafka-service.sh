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

readonly deployment_kafka_parent_path=$( cd "$(dirname "${BASH_SOURCE[0]}")" ; pwd -P )
readonly deployment_kafka_grand_parent_path=$(dirname ${deployment_kafka_parent_path})
readonly deployment_kafka_great_grand_parent_path=$(dirname ${deployment_kafka_grand_parent_path})

. ${deployment_kafka_great_grand_parent_path}/util/usage.sh
. ${deployment_kafka_great_grand_parent_path}/util/setup-deployment-env.sh ${INPUT_DIR} ${OUTPUT_DIR}


function clone_bbg_and_set_bal_path() {
    local bbg_repo_name="messaging-with-kafka"
    git clone https://github.com/ballerina-guides/${bbg_repo_name} --branch kafka-testgrid
    bal_path_admin=${bbg_repo_name}/guide/product_admin_portal/product_admin_portal.bal
    bal_path_inventory=${bbg_repo_name}/guide/inventory_control_system/inventory_control_system.bal
}

function setup_deployment() {
    cat ${INPUT_DIR}/infrastructure.properties
    clone_bbg_and_set_bal_path
    download_and_install_kafka_connector
    deploy_kafka_resources
    replace_variables_in_bal_file $bal_path_admin
    replace_variables_in_bal_file $bal_path_inventory
    build_and_deploy_guide
    wait_for_pod_readiness
    retrieve_and_write_properties_to_data_bucket
    local is_debug_enabled=${infra_config["isDebugEnabled"]}
    if [ "${is_debug_enabled}" = "true" ]; then
        print_kubernetes_debug_info
    fi
}

## Functions
function download_and_install_kafka_connector() {
    KAFKA_CONNECTOR_VERSION=0.980.2
    KAFKA_CONNECTOR_DISTRIBUTION=wso2-kafka-${KAFKA_CONNECTOR_VERSION}
    wget https://github.com/wso2-ballerina/module-kafka/releases/download/v${KAFKA_CONNECTOR_VERSION}/${KAFKA_CONNECTOR_DISTRIBUTION}.zip --quiet
    unzip ${KAFKA_CONNECTOR_DISTRIBUTION}.zip -d ${KAFKA_CONNECTOR_DISTRIBUTION}

    cp ${KAFKA_CONNECTOR_DISTRIBUTION}/dependencies/wso2-kafka-module-${KAFKA_CONNECTOR_VERSION}.jar ${ballerina_home}/bre/lib
    cp -r ${KAFKA_CONNECTOR_DISTRIBUTION}/balo/wso2 ${ballerina_home}/lib/repo
}

function deploy_kafka_resources() {
    # Create Kafka Zookeeper and Kafka Server in the same docker image from Spotify/Kafka
    docker login --username=${docker_user} --password=${docker_password}
    docker run -p 2181:2181 -p 9092:9092 --env ADVERTISED_HOST=`docker-machine ip \`docker-machine active\`` --env ADVERTISED_PORT=9092 spotify/kafka

    sed -i "s/spotify\/kafka/${docker_user}\/spotify\/kafka/" messaging-with-kafka/resources/kubernetes/kafka-deployment.yaml
    kubectl create -f messaging-with-kafka/resources/kubernetes/
}

function replace_variables_in_bal_file() {
    local bal_path=$1
    sed -i "s/\"localhost/\"kafka-service/" ${bal_path}
    sed -i "s:<path_to_kafka_connector_jars>:${work_dir}/{KAFKA_CONNECTOR_DISTRIBUTION}/dependencies/:g" ${bal_path}
    sed -i "s:<USERNAME>:${docker_user}:g" ${bal_path}
    sed -i "s:<PASSWORD>:${docker_password}:g" ${bal_path}
    sed -i "s:ballerina.guides.io:${docker_user}:g" ${bal_path}
}

function build_and_deploy_guide() {
    cd messaging-with-kafka/guide
    ${ballerina_home}/bin/ballerina build product_admin_portal --skiptests
    cd ../..
    kubectl apply -f ${work_dir}/messaging-with-kafka/guide/target/kubernetes/product_admin_portal
}

function retrieve_and_write_properties_to_data_bucket() {
    local external_ip=$(kubectl get nodes -o=jsonpath='{.items[0].status.addresses[?(@.type=="ExternalIP")].address}')
    local node_port=$(kubectl get svc ballerina-guides-product-admin-portal -o=jsonpath='{.spec.ports[0].nodePort}')
    declare -A deployment_props
    deployment_props["ExternalIP"]=${external_ip}
    deployment_props["NodePort"]=${node_port}
    write_to_properties_file ${OUTPUT_DIR}/deployment.properties deployment_props
    local is_debug_enabled=${infra_config["isDebugEnabled"]}
    if [ "${is_debug_enabled}" = "true" ]; then
        echo "ExternalIP: ${external_ip}"
        echo "NodePort: ${node_port}"
    fi
}

function print_kubernetes_debug_info() {
    cat ${bal_path_admin}
    cat ${bal_path_inventory}
    kubectl get pods
    kubectl get svc ballerina-guides-product-admin-portal -o=json
    kubectl get nodes --output wide
}

setup_deployment
