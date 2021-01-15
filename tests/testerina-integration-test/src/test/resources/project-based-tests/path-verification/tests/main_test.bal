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

string testString = "";

# Before Suite Function

@test:BeforeSuite
function beforeSuiteFunc() {
    testString += "beforeSuiteFunc";
}

# Before test function

function beforeFunc() {
    testString += "beforeFunc";
}

# Test function

@test:Config {
    before: beforeFunc,
    after: afterFunc
}
function testFunction() {
    testString += "test";
}

# After test function

function afterFunc() {
    testString += "afterFunc";
}

# After Suite Function

@test:AfterSuite {}
function afterSuiteFunc() {
    testString += "afterSuiteFunc";
}

# Second Test function
@test:Config {
    dependsOn : [testFunction]
}
function testFunction2() {
    test:assertEquals(testString, "beforeSuiteFuncbeforeFunctestafterFunc");
}
