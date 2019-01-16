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

set -o xtrace

WORK_DIR=`pwd`
TEST_SCRIPT=test.sh

function usage()
{
    echo "
    Usage bash test.sh --input-dir /workspace/data-bucket/in --output-dir /workspace/data-bucket/out
    Following are the expected input parameters. all of these are optional
    --input-dir       | -i    : input directory for test.sh
    --output-dir      | -o    : output directory for test.sh
    "
}

# Process inputs
# ex. bash test.sh --input-dir <path-to-input-dir> --output-dir <path-to-output-dir>
optspec=":hiom-:"
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
                mvn-opts)
                    val="${!OPTIND}"; OPTIND=$(( $OPTIND + 1 ))
                    MAVEN_OPTS=$val
                    ;;
                *)
                    usage
                    if [ "$OPTERR" = 1 ] && [ "${optspec:0:1}" != ":" ]; then
                        echo "Unknown option --${OPTARG}" >&2
                    fi
                    ;;
            esac;;
        h)
            usage
            exit 2
            ;;
        o)
            OUTPUT_DIR=$val
            ;;
        m)
            MVN_OPTS=$val
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

#=============== Execute Scenarios ===============================================
# YOUR TEST EXECUTION LOGIC GOES HERE
# A sample execution for maven-based testng/junit tests is shown below.
# For maven, we add -fae (fail-at-end), and a system property to reduce jar download log verbosity.

IFS='=' read -r -a array <<< "$(head -n 1 $INPUT_DIR/deployment.properties)"
LB_INGRESS_HOST=${array[1]};
unset IFS

cat $INPUT_DIR/deployment.properties

curl http://$LB_INGRESS_HOST/cb -v


if [ -z ${JMETER_HOME} ]
then
  echo 'JMETER_HOME env variable not found. Setting up jmeter manually.'
  wget https://archive.apache.org/dist/jmeter/binaries/apache-jmeter-4.0.tgz
  tar -xzf apache-jmeter-4.0.tgz
  export JMETER_HOME=$(pwd)/apache-jmeter-4.0
  JMETER_HOME=$(pwd)/apache-jmeter-4.0
else
  echo JMETER_HOME env variable found at: ${JMETER_HOME}
fi

echo Final Jmeter home: $JMETER_HOME

mkdir scenario2

bash ${JMETER_HOME}/bin/jmeter -t scenarios/2/ballerina-CB.jmx -n  -l ./scenario2/ballerina-CB.jtl -Jhost=${LB_INGRESS_HOST}

cat ./scenario1/ballerina-SELECT.jtl

cp -r scenario1 ${OUTPUT_DIR}

ls ${OUTPUT_DIR}/*

#=============== Copy Surefire Reports ===========================================
# SUREFIRE REPORTS MUST NEED TO BE COPIED TO OUTPUT_DIR.
# You need to preserve the folder structure in order to identify executed scenarios.
echo "Copying surefire-reports to ${OUTPUT_DIR}"

mkdir -p ${OUTPUT_DIR}
find ./* -name "surefire-reports" -exec cp --parents -r {} ${OUTPUT_DIR} \;
