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

# Test file to test the test execution behavior when a @AfterGroups function
# fails. The expected behavior is that none other function should be exectuted
# subsequently other than the @AfterSuite function.

string a = "";

@test:BeforeGroups { value : ["g1"] }
function beforeGroupsFunc1() {
    a += "2";
}

@test:BeforeGroups { value : ["g2"] }
function beforeGroupsFunc2() {
    a += "4";
}

@test:AfterGroups { value : ["g1"] }
function afterGroupsFunc1() {
    int b = 2/0;
}

@test:AfterGroups { value : ["g2"] }
function afterGroupsFunc2() {
    a += "9";
}

# Test function
@test:Config {}
function testFunction() {
    a += "1";
}

@test:Config {groups: ["g1"]}
function testFunction2() {
    a += "3";
}

@test:Config {groups : ["g2"]}
function testFunction3() {
    a += "5";
}

@test:Config {groups : ["g1", "g2"]}
function testFunction4() {
    a += "6";
}

@test:Config {groups : ["g2"]}
function testFunction5() {
    a += "8";
}

# After Suite Function
@test:AfterSuite {}
function afterSuiteFunc() {
    test:assertEquals(a, "123456");
}
