// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
class StringIterator {

    private string m;

    public function init(string m) {
        self.m = m;
    }

    # Return the next member in string iterator, nil if end of iterator is reached.
    # + return - iterator result
    public isolated function next() returns record {| string value; |}? {
        return externNext(self);
    }
}

function externNext(StringIterator iterator) returns record {| string value; |}? = @java:Method {
    'class: "org.ballerinalang.langlib.string.Next",
    name: "next"
} external;
