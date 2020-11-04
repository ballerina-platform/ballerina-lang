// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

import ballerina/java;

# Check whether a given key has a configuard value
#
# + key - The key value
# + return - Boolean value
public function hasConfigurableValue(string key) returns boolean = @java:Method {
    'class: "org.ballerinalang.langlib.internal.Configurable",
    name: "hasConfigurableValue"
} external;

# Get the value of the configurable variable
#
# + key - The key value
# + return - Configured value of the key
public function getConfigurableValue(string key) returns anydata = @java:Method {
    'class: "org.ballerinalang.langlib.internal.Configurable",
    name: "getConfigurableValue"
} external;
