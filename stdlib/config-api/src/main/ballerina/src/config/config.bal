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

import ballerina/lang.'float as langfloat;
import ballerina/lang.'int as langint;
import ballerina/stringutils;
import ballerina/system;

type ValueType STRING|INT|FLOAT|BOOLEAN|MAP|ARRAY;

const STRING = "STRING";
const INT = "INT";
const FLOAT = "FLOAT";
const BOOLEAN = "BOOLEAN";
const MAP = "MAP";
const ARRAY = "ARRAY";

# Retrieves the specified configuration value as a string.
# ```ballerina
# string host = config:getAsString("http.host");
# ```
#
# + key - The key of the configuration to be retrieved
# + defaultValue - The default value to be used in case there is no mapping for the provided key
# + return - Configuration value mapped with the given key
public function getAsString(@untainted string key, string defaultValue = "") returns string {
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
# ```ballerina
# int port = config:getAsInt("http.port");
# ```
#
# + key - The key of the configuration to be retrieved
# + defaultValue - The default value to be used in case there is no mapping for the provided key
# + return - Configuration value mapped with the given key
public function getAsInt(@untainted string key, int defaultValue = 0) returns int {
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

    var envVar = langint:fromString(strVal);
    if (envVar is int) {
        return envVar;
    } else {
        panic envVar;
    }
}

# Retrieves the specified configuration value as a float.
# ```ballerina
# float evictionFactor = config:getAsFloat("http.eviction_factor");
# ```
#
# + key - The key of the configuration to be retrieved
# + defaultVal - The default value to be used in case there is no mapping for the provided key
# + return - Configuration value mapped with the given key
public function getAsFloat(@untainted string key, float defaultVal = 0.0) returns float {
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

    var envVar = langfloat:fromString(strVal);
    if (envVar is float) {
        return envVar;
    } else {
        panic envVar;
    }
}

# Retrieves the specified configuration value as a boolean.
# ```ballerina
# boolean cachingEnabled = config:getAsBoolean("http.caching_enabled");
# ```
#
# + key - The key of the configuration to be retrieved
# + defaultValue - The default value to be used in case there is no mapping for the provided key
# + return - Configuration value mapped with the given key
public function getAsBoolean(@untainted string key, boolean defaultValue = false) returns boolean {
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

    return (strVal.toLowerAscii() == "true");
}

# Retrieves the specified configuration value as a map.
# ```ballerina
# map<anydata> configValue = config:getAsMap("http.listenerConfig");
# ```
#
# + key - The key of the configuration to be retrieved
# + return - Configuration value mapped with the given key. If there is no mapping, an empty map will be returned
public function getAsMap(@untainted string key) returns map<anydata> {
    var value = get(key, MAP);

    if (value is map<anydata>) {
        return value;
    } else {
        error err = error("Invalid value. Expected a 'map'.");
        panic err;
    }
}

# Retrieves the specified configuration value as an array.
# ```ballerina
# int[]|error ports = config:getAsArray("ports").cloneWithType(int[]);
# ```
#
# + key - The key of the configuration to be retrieved
# + return - Configuration value mapped with the given key. If there is no mapping, an empty array will be returned
public function getAsArray(@untainted string key) returns anydata[] {
    return <anydata[]>get(key, ARRAY);
}

function lookupEnvVar(string key) returns string {
    string convertedKey = stringutils:replace(key, ".", "_");
    return system:getEnv(convertedKey);
}
