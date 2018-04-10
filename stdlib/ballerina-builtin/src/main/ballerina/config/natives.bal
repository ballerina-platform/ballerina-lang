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

type ValueType "STRING"|"INT"|"FLOAT"|"BOOLEAN"|"MAP"|"ARRAY";

@final ValueType STRING = "STRING";
@final ValueType INT = "INT";
@final ValueType FLOAT = "FLOAT";
@final ValueType BOOLEAN = "BOOLEAN";
@final ValueType MAP = "MAP";
@final ValueType ARRAY = "ARRAY";

@Description {value:"Retrieves the specified configuration value as a string"}
@Param {value:"configKey: The configuration to be retrieved" }
@Return {value:"Configuration value mapped by the configKey" }
public function getAsString(string configKey, string default = "") returns string {
    if (contains(configKey)) {
        var value = get(configKey, STRING);

        match value {
            string strValue => return strValue;
            int|float|boolean|map|any[]|()|error=> {
                error err = {message:"invalid value type"};
                throw err;
            }
        }
    }

    return default;
}

public function getAsInt(string configKey, int default = 0) returns int {
    if (contains(configKey)) {
        var value = get(configKey, INT);

        match value {
            int intVal => return intVal;
            string|float|boolean|map|any[]|()|error=> {
                error err = {message:"invalid value type"};
                throw err;
            }
        }
    }

    return default;
}

public function getAsFloat(string configKey, float default = 0.0) returns float {
    if (contains(configKey)) {
        var value = get(configKey, FLOAT);

        match value {
            float floatVal => return floatVal;
            int|string|boolean|map|any[]|()|error=> {
                error err = {message:"invalid value type"};
                throw err;
            }
        }
    }

    return default;
}

public function getAsBoolean(string configKey, boolean default = false) returns boolean {
    if (contains(configKey)) {
        var value = get(configKey, BOOLEAN);

        match value {
            boolean booleanVal => return booleanVal;
            int|float|string|map|any[]|()|error=> {
                error err = {message:"invalid value type"};
                throw err;
            }
        }
    }

    return default;
}

public function getAsMap(string configKey, map default) returns map {
    if (contains(configKey)) {
        var value = get(configKey, MAP);

        match value {
            map section => return section;
            int|float|boolean|string|any[]|()|error=> {
                error err = {message:"invalid value type"};
                throw err;
            }
        }
    }

    return default;
}

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

native function get(string key, ValueType vType) returns string|int|float|boolean|map|any[]|()|error;
