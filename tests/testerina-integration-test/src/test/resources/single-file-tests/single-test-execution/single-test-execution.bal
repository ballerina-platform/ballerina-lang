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

// This tests the single test execution option

string testString = "";

@test:BeforeEach
function beforeEachFunc() {
    testString += "beforeEach";
}

// 2nd function
@test:Config {}
public function testFunc() {
    testString += "test";
}

// After function
@test:AfterEach
public function afterEachFunc() {
    testString += "afterEach";
}

// 2nd function
@test:Config {
    dependsOn:["testFunc"]
}
public function testFunc2() {
    io:println("TestFunc2");
    test:assertEquals(testString, "beforeEachtestafterEachbeforeEach");
}

// Disabled function
@test:Config {
    enable: false
}
public function testDisabledFunc() {
    io:println("testDisabledFunc");
    testString += "disabled";
    test:assertEquals(testString, "beforeEachdisabled");
}

// Test Dependent on disabled function
@test:Config {
    dependsOn: ["testDisabledFunc"]
}
public function testDependentDisabledFunc() {
    io:println("testDependentDisabledFunc");
    test:assertEquals(testString, "beforeEachdisabledafterEachbeforeEach");
}
