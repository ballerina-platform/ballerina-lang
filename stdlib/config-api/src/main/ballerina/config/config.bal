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

import ballerina/system;

type ValueType STRING|INT|FLOAT|BOOLEAN|MAP|ARRAY;

const STRING = "STRING";
const INT = "INT";
const FLOAT = "FLOAT";
const BOOLEAN = "BOOLEAN";
const MAP = "MAP";
const ARRAY = "ARRAY";

# Retrieves the specified configuration value as a string.
#
# + key - The configuration to be retrieved
# + defaultValue - The default value to be use in case there is no mapping for the provided key
# + return - Configuration value mapped by the key
public function getAsString(@sensitive string key, string defaultValue = "") returns string {
    if (contains(key)) {
        var value = get(key, STRING);

        if (value is string) {
            return value;
        } else {
            error err = error("Invalid value. Expected a 'string'.");
            panic err;
        }
    }

    string envVar = lookupEnvVar(key);
    return envVar == "" ? defaultValue : envVar;
}

# Retrieves the specified configuration value as an int.
#
# + key - The configuration to be retrieved
# + defaultValue - The default value to be use in case there is no mapping for the provided key
# + return - Configuration value mapped by the key
public function getAsInt(@sensitive string key, int defaultValue = 0) returns int {
    if (contains(key)) {
        var value = get(key, INT);

        if (value is int) {
            return value;
        } else {
            error err = error("Invalid value. Expected an 'int'.");
            panic err;
        }
    }

    string strVal = lookupEnvVar(key);
    if (strVal == "") {
        return defaultValue;
    }

    var envVar = int.convert(strVal);
    if (envVar is int) {
        return envVar;
    } else {
        panic envVar;
    }
}

# Retrieves the specified configuration value as a float.
#
# + key - The configuration to be retrieved
# + defaultVal - The default value to be use in case there is no mapping for the provided key
# + return - Configuration value mapped by the key
public function getAsFloat(@sensitive string key, float defaultVal = 0.0) returns float {
    if (contains(key)) {
        var value = get(key, FLOAT);

        if (value is float) {
            return value;
        } else {
            error err = error("Invalid value. Expected a 'float'.");
            panic err;
        }
    }

    string strVal = lookupEnvVar(key);
    if (strVal == "") {
        return defaultVal;
    }

    var envVar = float.convert(strVal);
    if (envVar is float) {
        return envVar;
    } else {
        panic envVar;
    }
}

# Retrieves the specified configuration value as a boolean.
#
# + key - The configuration to be retrieved
# + defaultValue - The default value to be use in case there is no mapping for the provided key
# + return - Configuration value mapped by the key
public function getAsBoolean(@sensitive string key, boolean defaultValue = false) returns boolean {
    if (contains(key)) {
        var value = get(key, BOOLEAN);

        if (value is boolean) {
            return value;
        } else {
            error err = error("Invalid value. Expected a 'boolean'.");
            panic err;
        }
    }

    string strVal = lookupEnvVar(key);
    if (strVal == "") {
        return defaultValue;
    }

    return boolean.convert(strVal);
}

# Retrieves the specified configuration value as a map. If there is no mapping, an empty map will be returned.
#
# + key - The configuration to be retrieved
# + return - Configuration value mapped by the key
public function getAsMap(@sensitive string key) returns map<any> {
    var value = get(key, MAP);

    if (value is map<any>) {
        return value;
    } else {
        error err = error("Invalid value. Expected a 'map'.");
        panic err;
    }
}

function lookupEnvVar(string key) returns string {
    string convertedKey = key.replace(".", "_");
    return system:getEnv(convertedKey);
}
