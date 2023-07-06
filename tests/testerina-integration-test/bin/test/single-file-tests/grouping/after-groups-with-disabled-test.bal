
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

// Verifies the behavior of @AfterGroups function when the specific group contains a disabled test.

import ballerina/test;

string a = "";

@test:Config{
    groups: ["g1","g2"]
}
function testFunc1 () {
    a += "1";
    test:assertFalse(false, msg = "errorMessage");
}

@test:Config{
    groups: ["g1","g2"],
    dependsOn: [testFunc1]
}
function testFunc2 () {
    a += "2";
    test:assertFalse(false, msg = "errorMessage");
}

@test:Config{
    groups: ["g1"],
    dependsOn: [testFunc2]
}
function testFunc3 () {
    a += "3";
    test:assertFalse(false, msg = "errorMessage");
}

@test:Config{
    enable : false,
    groups: ["g1"],
    dependsOn: [testFunc3]
}
function testFunc5 () {
    a += "4";
    test:assertFalse(false, msg = "errorMessage");
}

@test:AfterGroups { value : ["g1"] }
function afterGroupsFunc1() {
    a += "5";
}
@test:AfterSuite {}
function afterSuiteFunc() {
    test:assertEquals(a, "1235");
}
