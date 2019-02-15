#!/bin/bash

# Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

set -o xtrace

WORK_DIR=`pwd`

function usage()
{
    echo "
    Usage bash deployment-data.sh --input-dir /workspace/data-bucket/in --output-dir /workspace/data-bucket/out
    Following are the expected input parameters. all of these are optional
    --input-dir       | -i    : input directory for test.sh
    --output-dir      | -o    : output directory for test.sh
    "
}

# Process inputs
# ex. bash test.sh --input-dir <path-to-input-dir> --output-dir <path-to-output-dir>
optspec=":hio-:"
while getopts "$optspec" optchar; do
    case "${optchar}" in
        -)
            case "${OPTARG}" in
                input-dir)
                    val="${!OPTIND}"; OPTIND=$(( $OPTIND + 1 ))
                    INPUT_DIR=$val
                    ;;
                output-dir)
                    val="${!OPTIND}"; OPTIND=$(( $OPTIND + 1 ))
                    OUTPUT_DIR=$val
                    ;;
            esac;;
        h)
            usage
            exit 2
            ;;
        o)
            OUTPUT_DIR=$val
            ;;
        i)
            INPUT_DIR=$val
            ;;
        *)
            usage
            if [ "$OPTERR" != 1 ] || [ "${optspec:0:1}" = ":" ]; then
                echo "Non-option argument: '-${OPTARG}'" >&2
            fi
            ;;
    esac
done

echo "working Directory : ${WORK_DIR}"
echo "input directory : ${INPUT_DIR}"
echo "output directory : ${OUTPUT_DIR}"

export DATA_BUCKET_LOCATION=${INPUT_DIR}

#=============== Run deployment configuration ===============================================
# YOUR TEST EXECUTION LOGIC GOES HERE
# A sample execution for maven-based testng/junit tests is shown below.
# For maven, we add -fae (fail-at-end), and a system property to reduce jar download log verbosity.
#IFS='=' read -r -a array <<< "$(head -n 1 infrastructure.properties)"

cat $INPUT_DIR/infrastructure.properties

pwd
ls

# Read configuration into an associative array
declare -A CONFIG
# IFS is the 'internal field separator'. In this case, your file uses '='
IFS="="
while read -r key value
do
     CONFIG[$key]=$value
done < $INPUT_DIR/infrastructure.properties
unset IFS

export DATABASE_HOST=${CONFIG[DatabaseHost]}
export DATABASE_PORT=${CONFIG[DatabasePort]}
export DATABASE_NAME=test
export DATABASE_USERNAME=${CONFIG[DBUsername]}
export DATABASE_PASSWORD=${CONFIG[DBPassword]}

ClusterName=${CONFIG[ClusterName]};
ClusterRegion=${CONFIG[ClusterRegion]};
ConfigFileName=${CONFIG[ConfigFileName]};

bash product-scenarios/mysql_init.sh ${DATABASE_HOST} ${DATABASE_PORT} ${DATABASE_USERNAME} ${DATABASE_PASSWORD}

wget https://product-dist.ballerina.io/nightly/0.990.3-SNAPSHOT/ballerina-linux-installer-x64-0.990.3-SNAPSHOT.deb
sudo dpkg -i ballerina-linux-installer-x64-0.990.3-SNAPSHOT.deb --quiet

ballerina version

wget https://dev.mysql.com/get/Downloads/Connector-J/mysql-connector-java-5.1.47.tar.gz --quiet

tar -xzf mysql-connector-java-5.1.47.tar.gz --directory ./

ballerina build product-scenarios/scenarios/1/data-service.bal

export KUBECONFIG=${INPUT_DIR}/${ConfigFileName}

kubectl config view

kubectl config current-context

kubectl apply -f kubernetes/

TIMEOUT=300
INTERVAL=20
bash 'product-scenarios/wait_for_pod_ready.sh' ${TIMEOUT} ${INTERVAL}

READY_STATUS=$?

echo "Ready Staus: ${READY_STATUS}"
if [ ${READY_STATUS} -ne 0 ]; then
    exit 1;
fi
echo "All pods ready!"

# Temporary sleep to check whether app eventually becomes ready..
# Ideally there should have been a kubernetes readiness probe
# which would make sure the "Ready" status would actually mean
# the pod is ready to accept requests (app is ready) so the above
# readiness script would suffice
sleep 120s

kubectl get svc

kubectl get pods

kubectl get svc ballerina-data-service -o=json

EXTERNAL_IP=$(kubectl get svc ballerina-data-service -o jsonpath='{.status.loadBalancer.ingress[0].hostname}')

echo "ExternalIP=$EXTERNAL_IP" >> ${OUTPUT_DIR}/deployment.properties
echo "ServicesToBeDeleted=ballerina-data-service" >> ${OUTPUT_DIR}/infrastructure-cleanup.properties