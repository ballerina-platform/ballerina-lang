#!/bin/bash
# Copyright (c) 2018, WSO2 Inc. (http://wso2.org) All Rights Reserved.
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
#
# ----------------------------------------------------------------------------
# Run Ballerina examples.
# ----------------------------------------------------------------------------

skipTest=""

# Change directory to provided path
cd ${1}

# Change skip tests based on argument
if [ ${2} ]
then
    # skipTest="--skiptests"
    exit 0
fi

# List of BBEs excluded from building
exclude=("proto-to-ballerina"
        "swagger-to-ballerina"
        "taint-checking"
        "websub-hub-client-sample"
        "websub-remote-hub-sample"
        "counter-metrics"
        "config-api"
        "testerina-function-mocks"
        "jms-queue-message-receiver-with-client-acknowledgment"
        "gauge-metrics"
        "jdbc-client-crud-operations"
        "jdbc-client-batch-update"
        "jdbc-client-call-procedures"
        "streaming-big-dataset"
        "docker-deployment"
        "kubernetes-deployment"
        "awslambda-deployment"
        )

packages=($( sed -n 's/.*"url": "\([^"]*\)"/\1/p' index.json ))
echo `date "+%Y-%m-%d-%H:%M:%S"`" : Start building ${#packages[@]} Ballerina By Examples and ${#exclude[@]} will be skipped"

# Remove excludes
for i in "${exclude[@]}"
do
         packages=(${packages[@]//*$i*})
done

sh ../bin/ballerina init

for package in "${packages[@]}"
do
  sh ../bin/ballerina build ${package} ${skipTest} --experimental || exit 1
done
echo `date "+%Y-%m-%d-%H:%M:%S"`" : Ballerina By Examples built successfully!"