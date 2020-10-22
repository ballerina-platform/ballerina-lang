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

# Test file to test the test execution behavior when @BeforeGroups and @AfterGroups functions
# are defined. The expected behavior is that, each @BeforeGroups function will execute before the first
# test of that group is executed and each @AfterGroups function will execute after the
# last test of that group is executed.

import ballerina/test;

string a = "";

@test:BeforeSuite
function beforeSuiteFunc() {
    a += "1";
}

@test:BeforeGroups { value : ["g1"] }
function beforeGroupsFunc1() {
    a += "7";
}

@test:AfterGroups { value : ["g1"] }
function afterGroupsFunc1() {
    a += "9";
}

@test:BeforeEach
function beforeEachFunc() {
    a += "2";
}

@test:AfterEach
function afterEachFunc() {
    a += "6";
}

# Before test function
function beforeFunc() {
    a += "3";
}

# Test function
@test:Config {
    before: "beforeFunc",
    after: "afterFunc"
}
function testFunction() {
    a += "4";
}

@test:Config {groups: ["g1"]}
function testFunction2() {
    a += "8";
}

# After test function

function afterFunc() {
    a += "5";
}

# After Suite Function
@test:AfterSuite {}
function afterSuiteFunc() {
    test:assertEquals(a, "12345672869");
}
