// Copyright (c) 2026 WSO2 LLC. (http://www.wso2.com).
//
// WSO2 LLC. licenses this file to you under the Apache License,
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

@test:AfterEach
isolated function afterEach() returns error? {
    println("after each");
}

int value = 0;

@test:Config {
    minPassRate: 1,
    runs: 3
}
function testNonIsolatedEval() returns error? {
    value += 1;
    println("testNonIsolatedEval");
}

@test:Config {
    dependsOn: [testNonIsolatedEval],
    minPassRate: 1,
    runs: 3
}
isolated function testIsolatedEval() returns error? {
    println("testIsolatedEval");
}

@test:Config {
    dataProvider: goldenDataSet,
    dependsOn: [testIsolatedEval],
    minPassRate: 1,
    runs: 3
}
function testNonIsolatedEvalWithDataProvider(string query) returns error? {
    value += 1;
    println("testNonIsolatedEvalWithDataProvider");
}

@test:Config {
    dataProvider: goldenDataSet,
    dependsOn: [testNonIsolatedEvalWithDataProvider],
    minPassRate: 1,
    runs: 3
}
isolated function testIsolatedEvalWithDataProvider(string query) returns error? {
    println("testIsolatedEvalWithDataProvider");
}
