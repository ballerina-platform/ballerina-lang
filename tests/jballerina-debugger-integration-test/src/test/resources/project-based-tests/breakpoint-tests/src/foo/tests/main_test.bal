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

import ballerina/io;
import ballerina/test;
import ballerina/time;

# Before Suite Function
@test:BeforeSuite
function beforeSuiteFunc() {
    io:println("I'm the before suite function!");
}

# Before test function
function beforeFunc() {
    io:println("I'm the before function!");
}

# Test function
@test:Config {
    before: "beforeFunc",
    after: "afterFunc"
}
function testMain() {
    io:println("I'm calling the main function in source file!");
    main();
    test:assertTrue(true, msg = "Failed!");
}

@test:Config {
    dependsOn: ["testMain"]
}
function testFunction() {
    time:Time time = time:currentTime();
    test:when(sqrtMockFn).call("mockSqrt");
    test:assertEquals(time:getYear(time), 125);
}

# After test function
function afterFunc() {
    io:println("I'm the after function!");
}

# After Suite Function
@test:AfterSuite {}
function afterSuiteFunc() {
    io:println("I'm the after suite function!");
}

@test:Mock {
    moduleName: "ballerina/time",
    functionName: "getYear"
}
test:MockFunction sqrtMockFn = new();

function mockSqrt(time:Time val) returns int {
    return 125;
}
