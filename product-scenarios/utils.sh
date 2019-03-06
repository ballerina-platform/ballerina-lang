#!/bin/bash

parent_path=$( cd "$(dirname "${BASH_SOURCE[0]}")" ; pwd -P )

cluster_name="ballerina-testgrid-cluster-v2"

# $1 - Property file
# $2 - associative array
# How to call
# declare -A somearray
# read_property_file testplan-props.properties somearray
function read_property_file() {
    local testplan_properties=$1
    # Read configuration into an associative array
    # IFS is the 'internal field separator'. In this case, your file uses '='
    local -n configArray=$2
    IFS="="
    while read -r key value
    do
         configArray[$key]=$value
    done < ${testplan_properties}
    unset IFS
}

# $1 file path
function write_to_properties_file() {
    local properties_file_path=$1
    local -n properties_array=$2

    # Keys are accessed through exclamation point
    for i in ${!properties_array[@]}
    do
      echo ${i}=${properties_array[$i]} >> ${properties_file_path}
    done
}

function install_ballerina() {
    local ballerina_version=$1
    wget https://product-dist.ballerina.io/downloads/${ballerina_version}/ballerina-linux-installer-x64-${ballerina_version}.deb --quiet
    sudo dpkg -i ballerina-linux-installer-x64-${ballerina_version}.deb

    ballerina version
}

function download_and_extract_mysql_connector() {
    wget https://dev.mysql.com/get/Downloads/Connector-J/mysql-connector-java-5.1.47.tar.gz --quiet

    tar -xzf mysql-connector-java-5.1.47.tar.gz --directory ./
}

function generate_random_namespace() {
    local prefix=$1
    local new_uuid=$(cat /dev/urandom | tr -dc 'a-z0-9' | fold -w 8 | head -n 1)
    echo "kubernetes-namespace"-${new_uuid}
}

function wait_for_pod_readiness() {
    TIMEOUT=300
    INTERVAL=20
    bash 'product-scenarios/wait_for_pod_ready.sh' ${TIMEOUT} ${INTERVAL}

    # Temporary sleep to check whether app eventually becomes ready..
    # Ideally there should have been a kubernetes readiness probe
    # which would make sure the "Ready" status would actually mean
    # the pod is ready to accept requests (app is ready) so the above
    # readiness script would suffice
    sleep 120s
}

function setup_env() {
    input_dir=$1
    output_dir=$2
    bash ${parent_path}/setup_env.sh ${input_dir} ${output_dir}
}