// Copyright (c) 2022 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import ballerina/lang.runtime;
import ballerina/test;

int initCount = 0;
int count = 0;

function init() {
    initCount += 1;
    incrementAndAssertInt(1);
    runtime:onGracefulStop(stopHandlerFunc);
}

public function stopHandlerFunc() returns error? {
    incrementAndAssertInt(9);
    panic error("Stopped moduleB");
}

public function getInitCount() returns int {
    return initCount;
}

public function incrementAndAssertInt(int val) {
    count += 1;
    test:assertEquals(count, val);
}

function incrementAndAssert(string name, string expectedName, int val) {
    if (expectedName == name) {
        incrementAndAssertInt(val);
    }
}

public function println(string value) {
    handle strValue = java:fromString(value);
    handle stdout1 = stdout();
    printlnInternal(stdout1, strValue);
}
public function stdout() returns handle = @java:FieldGet {
    name: "out",
    'class: "java/lang/System"
} external;

public function printlnInternal(handle receiver, handle strValue)  = @java:Method {
    name: "println",
    'class: "java/io/PrintStream",
    paramTypes: ["java.lang.String"]
} external;
