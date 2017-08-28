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
    # Running Ballerina bal file or compiled balx, balx has priority
    arg_str=$(find /ballerina/files -type f -name "*.balx" -print | head -n 1)
    if [ -z "$arg_str" ]; then
        arg_str=$(find /ballerina/files -type f -name "*.bal" -print | head -n 1)
    fi
}

collectArgs

cmd="ballerina run"

exec bin/$cmd $arg_str
