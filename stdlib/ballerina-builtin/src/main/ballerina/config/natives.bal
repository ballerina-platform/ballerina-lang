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

type ValueType "STRING"|"INT"|"FLOAT"|"BOOLEAN"|"MAP"|"ARRAY";

@final ValueType STRING = "STRING";
@final ValueType INT = "INT";
@final ValueType FLOAT = "FLOAT";
@final ValueType BOOLEAN = "BOOLEAN";
@final ValueType MAP = "MAP";
@final ValueType ARRAY = "ARRAY";

documentation {
    Retrieves the specified configuration value as a string.

    P{{key}} The configuration to be retrieved
    P{{default}} The default value to be use in case there is no mapping for the provided key
    R{{}} Configuration value mapped by the key
}
public function getAsString(@sensitive string key, string default = "") returns string {
    if (contains(key)) {
        var value = get(key, STRING);

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

documentation {
    Retrieves the specified configuration value as an int.

    P{{key}} The configuration to be retrieved
    P{{default}} The default value to be use in case there is no mapping for the provided key
    R{{}} Configuration value mapped by the key
}
public function getAsInt(@sensitive string key, int default = 0) returns int {
    if (contains(key)) {
        var value = get(key, INT);

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

documentation {
    Retrieves the specified configuration value as a float.

    P{{key}} The configuration to be retrieved
    P{{default}} The default value to be use in case there is no mapping for the provided key
    R{{}} Configuration value mapped by the key
}
public function getAsFloat(@sensitive string key, float default = 0.0) returns float {
    if (contains(key)) {
        var value = get(key, FLOAT);

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

documentation {
    Retrieves the specified configuration value as a boolean.

    P{{key}} The configuration to be retrieved
    P{{default}} The default value to be use in case there is no mapping for the provided key
    R{{}} Configuration value mapped by the key
}
public function getAsBoolean(@sensitive string key, boolean default = false) returns boolean {
    if (contains(key)) {
        var value = get(key, BOOLEAN);

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

documentation {
    Retrieves the specified configuration value as a map. If there is no mapping, an empty map will be returned.

    P{{key}} The configuration to be retrieved
    R{{}} Configuration value mapped by the key
}
public function getAsMap(@sensitive string key) returns map {
    var value = get(key, MAP);

    match value {
        map section => return section;
        int|float|boolean|string|any[]|()|error=> {
            error err = {message:"invalid value type"};
            throw err;
        }
    }
}

documentation {
    Checks whether the given key is in the configuration registry.

    P{{key}} The configuration key to be looked-up
    R{{}} Returns true if the key is present; if not returs false
}
public native function contains(@sensitive string key) returns boolean;

documentation {
    Sets the specified key/value pair as a configuration.

    P{{key}} The key of the configuration value to be set
    P{{value}} The configuration value to be set
}
public native function setConfig(string key, string|int|float|boolean value);

native function get(@sensitive string key, ValueType vType) returns string|int|float|boolean|map|any[]|()|error;
