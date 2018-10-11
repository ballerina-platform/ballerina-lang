// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

# Checks whether the given key is in the configuration registry.
#
# + key - The configuration key to be looked-up
# + return - Returns true if the key is present; if not returs false
public extern function contains(@sensitive string key) returns boolean;

# Sets the specified key/value pair as a configuration.
#
# + key - The key of the configuration value to be set
# + value - The configuration value to be set
public extern function setConfig(string key, string|int|float|boolean value);

extern function get(@sensitive string key, ValueType vType) returns string|int|float|boolean|map|any[]|()|error;
