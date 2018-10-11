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

# Removes the specified element from the map.
#
# + key - The key to be removed
# + return - A boolean to indicate whether the key is removed or not from map
public extern function map::remove(string key) returns (boolean);

# Returns an array of keys contained in the specified map.
#
# + return - A string array of keys contained in the specified map
public extern function map::keys() returns (string[]);

# Check whether specific key exists from the given map.
#
# + key - The key to be find existence
public extern function map::hasKey(string key) returns (boolean);

# Clear the items from given map.
public extern function map::clear();

# Returns an array of values contained in the specified map.
#
# + return - An any array of values contained in the specified map
public extern function map::values() returns (any[]);
