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

// Verifies behaviour of the @BeforeGroups and @AfterGroups functions with other test
// configurations when there are multiple test groups.

import ballerina/test;

string a = "";

@test:BeforeGroups { value : ["g1"] }
function beforeGroupsFunc1() {
    a += "2";
}

@test:BeforeGroups { value : ["g2"] }
function beforeGroupsFunc2() {
    a += "4";
}

@test:AfterGroups { value : ["g1", "g2"] }
function afterGroupsFunc1() {
    a += "7";
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
    a += "3";
}

@test:Config {
    groups : ["g2"],
    dependsOn: [testFunction2]
}
function testFunction3() {
    a += "5";
}

@test:Config {
    groups : ["g1", "g2"],
    dependsOn: [testFunction]
}
function testFunction4() {
    a += "6";
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
    test:assertEquals(a, "123456787");
}
