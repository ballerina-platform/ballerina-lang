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

readonly utils_parent_path=$( cd "$(dirname "${BASH_SOURCE[0]}")" ; pwd -P )
readonly utils_grand_parent_path=$(dirname ${utils_parent_path})
readonly utils_great_grand_parent_path=$(dirname ${utils_grand_parent_path})

readonly cluster_name="ballerina-testgrid-cluster-v2"

# Read a property file to a given associative array
#
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

# Write key value pairs in a given associative array to a given property file
#
# $1 - file path
# $2 - associative array of key value pairs
function write_to_properties_file() {
    local properties_file_path=$1
    local -n properties_array=$2

    # Keys are accessed through exclamation point
    for i in ${!properties_array[@]}
    do
      echo ${i}=${properties_array[$i]} >> ${properties_file_path}
    done
}

# Install the provided Ballerina version
#
# $1 - Ballerina version
function install_ballerina() {
    local ballerina_version=$1
    wget https://product-dist.ballerina.io/downloads/${ballerina_version}/ballerina-${ballerina_version}.zip --quiet
    unzip ballerina-${ballerina_version}.zip -d ${utils_parent_path}
    ${utils_parent_path}/ballerina-${ballerina_version}/bin/ballerina version
    readonly ballerina_home=${utils_parent_path}/ballerina-${ballerina_version}
}

# Downloads and extracts the MySQL connector
#
# $1 - Download location
function download_and_extract_mysql_connector() {
    local download_location=$1
    wget https://dev.mysql.com/get/Downloads/Connector-J/mysql-connector-java-5.1.47.tar.gz --quiet

    tar -xzf mysql-connector-java-5.1.47.tar.gz --directory ${download_location}

    ls ${download_location}/mysql-connector-java-5.1.47
}

# Generates a random namespace name
function generate_random_namespace() {
    echo "kubernetes-namespace"-$(generate_random_name)
}

# Generates a random name
function generate_random_database_name() {
    echo "test-database"-$(generate_random_name)
}

# Generates a random database name
function generate_random_name() {
    local new_uuid=$(cat /dev/urandom | tr -dc 'a-z0-9' | fold -w 8 | head -n 1)
    echo ${new_uuid}
}

# Wait for pod readiness
function wait_for_pod_readiness() {
    local timeout=300
    local interval=20
    bash ${utils_parent_path}/wait-for-pod-ready.sh ${timeout} ${interval}

    # Temporary sleep to check whether app eventually becomes ready..
    # Ideally there should have been a kubernetes readiness probe
    # which would make sure the "Ready" status would actually mean
    # the pod is ready to accept requests (app is ready) so the above
    # readiness script would suffice
    sleep 120s
}

# Builds run tests of the provided BBG section profile and copies the surefire reports to teh output directory
#
# $1 - BBG section directory name
# $2 - Associative array of system property-value pairs
# $3 - System properties associative array
# $4 - Input directory
# $5 - Output directory
function run_bbg_section_tests() {
    local maven_profile=$1
    local bbg_section=$2
    local -n properties_array=$3
    local __input_dir=$4
    local __output_dir=$5
    local sys_prop_str=""
    bash --version
    for x in "${!properties_array[@]}"; do sys_prop_str+="-D$x=${properties_array[$x]} " ; done

    mvn clean install -f ${utils_great_grand_parent_path}/pom.xml -fae -Ddata.bucket.location=${__input_dir} ${sys_prop_str} -P ${maven_profile}

    mkdir -p ${__output_dir}/scenarios

    cp -r ${utils_great_grand_parent_path}/bbg/${bbg_section}/target ${__output_dir}/scenarios/${bbg_section}/
}

# Clones the given BBG.
#
# $1 - BBG repository name
function clone_bbg() {
    local bbg_repo=$1
    git clone https://github.com/ballerina-guides/${bbg_repo} --branch testgrid-onboarding
}

function push_image_to_docker_registry() {
    local image=$1
    local tag=$2
    docker login --username=${docker_user} --password=${docker_password}
    docker push ${docker_user}/${image}:${tag}
}

function build_docker_image() {
    local image=$1
    local tag=$2
    local image_location=$3
    docker build -t ${docker_user}/${image}:${tag} ${image_location}
}