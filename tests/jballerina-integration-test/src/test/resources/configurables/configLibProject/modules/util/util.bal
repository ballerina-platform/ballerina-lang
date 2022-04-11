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
import ballerina/test;

public function print(string value) {
    handle strValue = java:fromString(value);
    handle stdout1 = stdout();
    printInternal(stdout1, strValue);
}

function stdout() returns handle = @java:FieldGet {
    name: "out",
    'class: "java/lang/System"
} external;

function printInternal(handle receiver, handle strValue) = @java:Method {
    name: "println",
    'class: "java/io/PrintStream",
    paramTypes: ["java.lang.String"]
} external;

public function testTableIterator(table<map<anydata>> tab) {
    int count = 0;
    foreach var entry in tab {
        count += 1;
    }
    test:assertEquals(count, 2);
}

public function testMapIterator(map<anydata> testMap, int length) {
    int count = 0;
    foreach var entry in testMap {
        count += 1;
    }
    test:assertEquals(count, length);
}

public function testArrayIterator(anydata[] testArray, int length) {
    int count = 0;
    foreach var entry in testArray {
        count += 1;
    }
    test:assertEquals(count, length);
}

public function testRecordIterator(record {} testRecord, int length) {
    int count = 0;
    foreach var entry in testRecord {
        count += 1;
    }
    test:assertEquals(count, length);
}
