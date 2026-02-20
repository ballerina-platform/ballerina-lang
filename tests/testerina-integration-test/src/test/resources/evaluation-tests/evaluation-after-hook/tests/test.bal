// Copyright (c) 2026 WSO2 LLC. (http://www.wso2.com)
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

isolated function afterHook() => println("after function executed");

isolated function afterHookFail() returns error => error("after hook method failed");

int value = 0;

@test:Config {
    after: afterHook
}
@test:EvalConfig {
    confidence: 1,
    iterations: 3
}
isolated function testIsolatedEval() returns error? {
    println("run");
}

@test:Config {
    after: afterHook
}
@test:EvalConfig {
    confidence: 1,
    iterations: 3
}
function testNonIsolatedEval() returns error? {
    value += 1;
    println("run");
}

@test:Config {
    dataProvider: goldenDataSet,
    after: afterHook
}
@test:EvalConfig {
    confidence: 1,
    iterations: 3
}
isolated function testIsolatedEvalWithDataProvider(string query) returns error? {
    println("run");
}

@test:Config {
    dataProvider: goldenDataSet,
    after: afterHook
}
@test:EvalConfig {
    confidence: 1,
    iterations: 3
}
function testNonIsolatedEvalWithDataProvider(string query) returns error? {
    value += 1;
    println("run");
}

@test:Config {
    after: afterHookFail
}
@test:EvalConfig {
    confidence: 1,
    iterations: 3
}
isolated function testIsolatedEvalAfterFunctionFailure() returns error? {

}

@test:Config {
    after: afterHookFail
}
@test:EvalConfig {
    confidence: 1,
    iterations: 3
}
function testNonIsolatedEvalAfterFunctionFailure() returns error? {
    value += 1;
}

@test:Config {
    after: afterHookFail,
    dataProvider: goldenDataSet
}
@test:EvalConfig {
    confidence: 1,
    iterations: 3
}
isolated function testIsolatedEvalAfterFunctionFailureWithDataProvider(string query) returns error? {

}

@test:Config {
    after: afterHookFail,
    dataProvider: goldenDataSet
}
@test:EvalConfig {
    confidence: 1,
    iterations: 3
}
function testNonIsolatedEvalAfterFunctionFailureWithDataProvider(string query) returns error? {
    value += 1;
}
