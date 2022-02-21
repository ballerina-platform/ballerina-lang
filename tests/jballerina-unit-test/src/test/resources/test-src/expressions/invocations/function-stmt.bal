// Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

import ballerina/lang.runtime;

function emptyFunction () {
}

function funcEmptyDefaultWorker () {
    worker newWorker1 {
        int a = 5;
    }

    worker newWorker2 {
        int a = 5;
    }
}

function test1 () {
    string a = "hello";
    if (a == "a") {
        a = "b";
    }
}

function addFloat1 (float x, float y) returns (float) {
    float z = x + y;
    return z;
}

function test2 () {
    string a = "hello";
    if (a == "a") {
        a = "b";
    } else {
        a = "c";
    }
}

function addFloat2 (float x, float y) returns (float) {
    float z = x + y;
    return z;
}

function test3 () {
    string a = "hello";
    if (a == "a") {
        a = "b";
    } else if (a == "b") {
        a = "c";
    }
}

function addFloat3 (float x, float y) returns (float) {
    float z = x + y;
    return z;
}

function addFloat4 (float x, float y) returns (float) {
    float z = x + y;
    return z;
}

function addFloat5 (float x, float y) returns (float) {
    float z = x + y;
    return z;
}

function testRecursiveFunctionWhichYields() {
   bar(0, 1000);
}

function testRecursiveFunctionWhereStackOverflows() {
   bar(0, 10000);
}

function bar(int x, int lim) {
    if (x == lim) {
        runtime:sleep(0.01);
        return;
    } else {
        bar(x + 1, lim);
    }
}
