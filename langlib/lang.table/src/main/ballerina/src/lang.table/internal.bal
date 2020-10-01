// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ballerina/java;

# Represent the iterator type returned when `iterator` method is invoked.
class TableIterator {

    private table<Type> t;
    private KeyType[] keys;
    private int size;

    public function init(table<Type> t) {
       self.t = t;
        self.size = length(t);
       if (t is table<Type> key<KeyType>) {
           self.keys = keys(t);
       } else {
           self.keys = [];
       }
    }

    # Return the next member in table iterator, nil if end of iterator is reached.
    # + return - iterator result
    public isolated function next() returns record {| Type value; |}? {
       return externNext(self);
    }
}
function externNext(TableIterator iterator) returns record {| Type value; |}? = @java:Method {
    'class: "org.ballerinalang.langlib.table.Next",
    name: "next"
} external;
