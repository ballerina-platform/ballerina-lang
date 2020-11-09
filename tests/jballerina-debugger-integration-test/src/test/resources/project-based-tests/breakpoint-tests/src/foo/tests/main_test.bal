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

# Before Suite Function
@test:BeforeSuite
function beforeSuiteFunc() {
    int x = 0;
}

# Before test function
function beforeFunc() {
    int y = 0;
}

# Test function
@test:Config {
    before: "beforeFunc",
    after: "afterFunc"
}
function testMain() {
    main();
    test:assertTrue(true, msg = "Failed!");
}

@test:Config {
    dependsOn: ["testMain"]
}
function testFunction() {
    test:when(intAddMockFn).call("mockAdd");
    test:assertEquals(intAdd(10, 6), 125, msg = "function mocking failed");

}

# After test function
function afterFunc() {
    int z = 0;
}

# After Suite Function
@test:AfterSuite {}
function afterSuiteFunc() {
    int a = 0;
}

@test:Mock { functionName: "intAdd" }
test:MockFunction intAddMockFn = new();

function mockAdd(int a, int b) returns int {
    return 125;
}
