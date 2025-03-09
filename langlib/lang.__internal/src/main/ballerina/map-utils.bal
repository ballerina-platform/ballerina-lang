// Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ballerina/jballerina.java;

# Constructs a record value of type `t` from the provided `v` value
#
# + v - the value map to be cloned
# + t - the record type for the cloned to be constructed
# + return - a new record value that belongs to type `t`, or an error if this cannot be done
public function createRecordFromMap(map<any> v, typedesc<any> t = <>) returns t|error = @java:Method {
 'class: "org.ballerinalang.langlib.internal.CreateRecordValue",
 name: "createRecordFromMap"
} external;

# Update a map with the keys and values from another map
#
# + target - the value map to be updated
# + source - the value map of which the keys and values are inserted into map v
public function putAll(map<any> target, map<any> 'source) = @java:Method {
 'class: "org.ballerinalang.langlib.internal.UpdateMapValue",
 name: "putAll"
} external;
