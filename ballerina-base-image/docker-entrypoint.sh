#!/usr/bin/env bash
# Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

function collectArgs() {
    if [ ! -z "$FILE_MODE" ] && [ "$FILE_MODE" = "true" ]; then
        # Running single Ballerina files
        arg_str=$(find /ballerina/files -type f -name "*.bal")
    elif [ ! -z "$SVC_MODE" ] && [ "$SVC_MODE" = "true" ]; then
        # Running Ballerina service packages
        arg_str=$(find /ballerina/files -type f -name "*.bsz")
    else
        # Running Ballerina main packages
        arg_str=$(find /ballerina/files -type f -name "*.bmz")
    fi
}

collectArgs

if [ ! -z "$SVC_MODE" ] && [ "$SVC_MODE" = "true" ]; then
  cmd="ballerina run service"
else
  cmd="ballerina run main"
fi

exec bin/$cmd $arg_str
