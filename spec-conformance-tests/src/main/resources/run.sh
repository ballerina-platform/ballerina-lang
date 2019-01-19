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
#
# ----------------------------------------------------------------------------
# Run Ballerina Spec Conformance Tests.
# ----------------------------------------------------------------------------

# Change directory to provided path
cd ${1}

echo `date "+%Y-%m-%d-%H:%M:%S"`" : Start Running Ballerina Spec Conformance Tests"
echo $PWD
sh target/ballerina-tools-${2}/bin/ballerina init

sh target/ballerina-tools-${2}/bin/ballerina test --experimental || exit 1
echo `date "+%Y-%m-%d-%H:%M:%S"`" : Ballerina Spec Conformance Tests Run Successfully!"
