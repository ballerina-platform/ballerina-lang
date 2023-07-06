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

// Verifies the test execution behavior when a @BeforeGroups function fails.
// The expected behavior is that the tests belonging to that group will be skipped
// including the @before, @after functions and @AfterGroups functions for that group.
// Tests belonging to other groups will not be affected.

import ballerina/test;

string a = "";

@test:BeforeGroups { value : ["g1"] }
function beforeGroupsFunc1() {
    panic error("Failed before group for g1");
}

@test:BeforeGroups { value : ["g2"] }
function beforeGroupsFunc2() {
    a += "3";
}

@test:AfterGroups { value : ["g1"], alwaysRun: true }
function afterGroupsFunc1() {
    a += "6";
}

@test:AfterGroups { value : ["g1"] }
function afterGroupsFunc2() {
    a += "7";
}

@test:AfterGroups { value : ["g1", "g2"] }
function afterGroupsFunc3() {
    a += "9";
}

# Test function
@test:Config {}
function testFunction() {
    a += "1";
}

@test:Config {
    groups: ["g1"],
    dependsOn: [testFunction]
}
function testFunction2() {
    a += "2";
}

@test:Config {
    groups : ["g2"],
    dependsOn: [testFunction2]
}
function testFunction3() {
    a += "4";
}

@test:Config {
    groups : ["g1", "g2"],
    dependsOn: [testFunction]
}
function testFunction4() {
    a += "5";
}

@test:Config {
    groups : ["g2"],
    dependsOn: [testFunction4]
}
function testFunction5() {
    a += "8";
}

# After Suite Function
@test:AfterSuite {}
function afterSuiteFunc() {
    test:assertEquals(a, "135689");
}
