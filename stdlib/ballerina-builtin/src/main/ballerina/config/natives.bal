// Copyright (c) 2017 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package ballerina.config;

@Description {value:"Retrieves the specified configuration value as a string"}
@Param {value:"configKey: The configuration to be retrieved" }
@Return {value:"Configuration value mapped by the configKey" }
public native function getAsString(string configKey) returns string?;

@Description {value:"Retrieves the specified table of configurations as a map"}
@Param {value:"tableHeader: The table to be retrieved"}
@Return {value:"The specified table"}
public native function getTable (string tableHeader) returns map;

@Description {value:"Checks whether the given key is in the configuration registry"}
@Param {value:"configKey: The configuration key to be looked-up"}
@Return {value:"Returns true if the key is present; if not returs false"}
public native function contains (string configKey) returns boolean;

@Description { value:"Sets the specified key/value pair as a configuration" }
@Param { value:"configKey: The key of the configuration value to be set" }
@Param { value:"configValue: The configuration value to be set" }
public native function setConfig(string configKey, string configValue);
