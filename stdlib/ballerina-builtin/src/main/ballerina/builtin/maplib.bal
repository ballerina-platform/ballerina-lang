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

documentation {
    Removes the specified element from the map.

    T{{m}} The map object
    P{{key}} The key to be removed
    R{{}} A boolean to indicate whether the key is removed or not from map
}
public native function<map m> remove(string key) returns (boolean);

documentation {
    Returns an array of keys contained in the specified map.

    T{{m}} The map object
    R{{}} A string array of keys contained in the specified map
}
public native function<map m> keys() returns (string[]);

documentation {
    Check whether specific key exists from the given map.

    T{{m}} The map object
    P{{key}} The key to be find existence
}
public native function<map m> hasKey(string key) returns (boolean);

documentation {
    Clear the items from given map.

    T{{m}} The map object
}
public native function<map m> clear();

documentation {
    Returns an array of values contained in the specified map.

    T{{m}} The map object
    R{{}} An any array of values contained in the specified map
}
public native function<map m> values() returns (any[]);
