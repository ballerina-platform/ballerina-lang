#!/bin/bash

# Copyright (c) 2019, WSO2 Inc. (http://wso2.com) All Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

readonly test_http_parent_path=$( cd "$(dirname "${BASH_SOURCE[0]}")" ; pwd -P )
readonly test_http_grand_parent_path=$(dirname ${test_http_parent_path})
readonly test_http_great_grand_parent_path=$(dirname ${test_http_grand_parent_path})

. ${test_http_great_grand_parent_path}/util/usage.sh
. ${test_http_great_grand_parent_path}/util/setup-test-env.sh ${INPUT_DIR} ${OUTPUT_DIR}

function print_debug_info() {
    echo "Host And Port: ${external_ip}:${node_port}"
}

function run_tests() {
    local external_ip=${deployment_config["ExternalIP"]}
    local node_port=${deployment_config["NodePort"]}

    local is_debug_enabled=${deployment_config["isDebugEnabled"]}
    if [ "${is_debug_enabled}" = "true" ]; then
        print_debug_info
    fi

    declare -A sys_prop_array
    sys_prop_array["http.service.host"]=${external_ip}
    sys_prop_array["http.service.port"]=${node_port}

    # Builds and run tests of the given BBG section and copies resulting surefire reports to output directory
    local maven_profile=bbg-http
    local test_section=http
    local -n properties_array=sys_prop_array
    local sys_prop_str=""
    bash --version
    for x in "${!properties_array[@]}"; do sys_prop_str+="-D$x=${properties_array[$x]} " ; done

    mvn clean install -f ${utils_great_grand_parent_path}/pom.xml -fae -Ddata.bucket.location=${INPUT_DIR} ${sys_prop_str} -P ${maven_profile}

    mkdir -p ${OUTPUT_DIR}/scenarios

    cp -r ${utils_great_grand_parent_path}/${test_section}/target ${OUTPUT_DIR}/scenarios/${test_section}/
}

run_tests
