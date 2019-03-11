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
grand_parent_path=$(dirname ${parent_path})
great_grand_parent_path=$(dirname ${grand_parent_path})
great_great_grant_parent_path=$(dirname ${great_grand_parent_path})

. ${great_grand_parent_path}/usage.sh
. ${great_grand_parent_path}/setup_test_env.sh ${INPUT_DIR} ${OUTPUT_DIR}

external_ip=${deployment_config["ExternalIP"]}
node_port=${deployment_config["NodePort"]}

host_port=${external_ip}:${node_port}

echo "Host Port: ${host_port}"

curl -v -X POST -d '{"name":"Alice", "age":20,"ssn":123456789,"employeeId":1}' \
"http://${host_port}/records/employee" -H "Content-Type:application/json"

curl -v  "http://${host_port}/records/employee/1"

curl -v -X PUT -d '{"name":"Alice Updated", "age":30,"ssn":123456789,"employeeId":1}' \
"http://${host_port}/records/employee" -H "Content-Type:application/json"

curl -v -X DELETE "http://${host_port}/records/employee/1"

mvn -version

mvn clean install -f ${great_great_grant_parent_path}/pom.xml -Dmaven.repo.local=./tempm2 -fae -Ddata.bucket.location=${INPUT_DIR} -Ddata.backed.service.host=${external_ip} -Ddata.backed.service.port=${node_port}

#mvn clean install -f ${great_great_grant_parent_path}/bbg/pom.xml -Ddata.bucket.location=${INPUT_DIR} -Ddata.backed.service.host=${external_ip} -Ddata.backed.service.port=${node_port} -Dmaven.repo.local=./tempm2

mkdir -p ${OUTPUT_DIR}/scenarios/data-backed-service

cp -r ${great_great_grant_parent_path}/bbg/target ${OUTPUT_DIR}/scenarios/data-backed-service/

#find ${great_great_grant_parent_path}/* -name "surefire-reports" -exec cp --parents -r {} ${OUTPUT_DIR}/scenarios \;
