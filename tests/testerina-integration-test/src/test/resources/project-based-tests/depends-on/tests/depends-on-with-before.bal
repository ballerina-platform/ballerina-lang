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

// This tests the behavior when there is a test with before and dependsOn
// using string concatenation

string testStr = "";

function before() {
    testStr = testStr + "before";
}

// 2nd function
@test:Config {
    dependsOn:["testWithBefore1"]
}
public function testWithBefore2() {
    testStr = testStr + "test2";
}

// 1st function
@test:Config {
    before: "before",
    dependsOn: ["test4"] // added to preserve order of test execution 
}
public function testWithBefore1() {
    testStr = testStr + "test1";
}

// 3rd function
@test:Config {
    dependsOn:["testWithBefore2"]
}
public function testWithBefore3() {
    testStr = testStr + "test3";
}

// Last function
@test:Config {
    dependsOn:["testWithBefore3"]
}
public function testWithBefore4() {
    test:assertEquals(testStr, "beforetest1test2test3", msg = "Order is not correct");
}
