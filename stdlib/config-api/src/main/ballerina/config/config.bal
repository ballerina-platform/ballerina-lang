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
# + default - The default value to be use in case there is no mapping for the provided key
# + return - Configuration value mapped by the key
public function getAsString(@sensitive string key, string default = "") returns string {
    if (contains(key)) {
        var value = get(key, STRING);

        match value {
            string strValue => return strValue;
            int|float|boolean|map|any[]|() => {
                error err = error("Invalid value. Expected a 'string'.");
                panic err;
            }
            error err => {
                // TODO : Fix me. Do we nee cause here ?
                //map data = { cause: err };
                error e = error("Invalid value. Expected a 'string'.");
                panic e;
            }
        }
    }

    string envVar = lookupEnvVar(key);
    return envVar == "" ? default : envVar;
}

# Retrieves the specified configuration value as an int.
#
# + key - The configuration to be retrieved
# + default - The default value to be use in case there is no mapping for the provided key
# + return - Configuration value mapped by the key
public function getAsInt(@sensitive string key, int default = 0) returns int {
    if (contains(key)) {
        var value = get(key, INT);

        match value {
            int intVal => return intVal;
            string|float|boolean|map|any[]|() x => {
                error err = error("Invalid value. Expected an 'int'.");
                panic err;
            }
            error err => {
                //map data = { cause: err };
                error e = error("Invalid value. Expected an 'int'.");
                panic e;
            }
        }
    }

    string strVal = lookupEnvVar(key);
    if (strVal == "") {
        return default;
    }

    var envVar = <int> strVal;
    match envVar {
        int intVal => return intVal;
        error err => panic err;
    }
}

# Retrieves the specified configuration value as a float.
#
# + key - The configuration to be retrieved
# + default - The default value to be use in case there is no mapping for the provided key
# + return - Configuration value mapped by the key
public function getAsFloat(@sensitive string key, float default = 0.0) returns float {
    if (contains(key)) {
        var value = get(key, FLOAT);

        match value {
            float floatVal => return floatVal;
            int|string|boolean|map|any[]|() => {
                error err = error("Invalid value. Expected a 'float'.");
                panic err;
            }
            error err => {
                //map data = { cause: err };
                error e = error("Invalid value. Expected a 'float'.");
                panic e;
            }
        }
    }

    string strVal = lookupEnvVar(key);
    if (strVal == "") {
        return default;
    }

    var envVar = <float> strVal;
    match envVar {
        float floatVal => return floatVal;
        error err => panic err;
    }
}

# Retrieves the specified configuration value as a boolean.
#
# + key - The configuration to be retrieved
# + default - The default value to be use in case there is no mapping for the provided key
# + return - Configuration value mapped by the key
public function getAsBoolean(@sensitive string key, boolean default = false) returns boolean {
    if (contains(key)) {
        var value = get(key, BOOLEAN);

        match value {
            boolean booleanVal => return booleanVal;
            int|float|string|map|any[]|() => {
                error err = error("Invalid value. Expected a 'boolean'.");
                panic err;
            }
            error err => {
                //map data = { cause  : err };
                error e = error("Invalid value. Expected a 'boolean'.");
                panic e;
            }
        }
    }

    string strVal = lookupEnvVar(key);
    if (strVal == "") {
        return default;
    }

    return <boolean> strVal;
}

# Retrieves the specified configuration value as a map. If there is no mapping, an empty map will be returned.
#
# + key - The configuration to be retrieved
# + return - Configuration value mapped by the key
public function getAsMap(@sensitive string key) returns map {
    var value = get(key, MAP);

    match value {
        map section => return section;
        int|float|boolean|string|any[]|() => {
            error err = error("Invalid value. Expected a 'map'.");
            panic err;
        }
        error err => {
            //map data = { cause: err };
            error e = error("Invalid value. Expected a 'map'.");
            panic e;
        }
    }
}

function lookupEnvVar(string key) returns string {
    string convertedKey = key.replace(".", "_");
    return system:getEnv(convertedKey);
}
