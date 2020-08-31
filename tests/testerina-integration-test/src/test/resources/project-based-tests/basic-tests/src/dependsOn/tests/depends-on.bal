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

import ballerina/test;
import ballerina/io;

// This tests the functionality of dependsOn attribute in a test
// using string concatenation

string testString = "";

// 2nd function
@test:Config {
    dependsOn:["test1"]
}
public function test2() {
    testString = testString + "test2";
    io:println("test2");
}

// 1st function
@test:Config {}
public function test1() {
    testString = testString + "test1";
    io:println("test1");
}

// 3rd function
@test:Config {
    dependsOn:["test2"]
}
public function test3() {
    testString = testString + "test3";
    io:println("test3");
}

// Last function
@test:Config {
    dependsOn:["test3"]
}
public function test4() {
    test:assertEquals(testString, "test1test2test3", msg = "Order is not correct");
    io:println("test4");
}
