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

int value = 0;

@test:Config
@test:EvalConfig {
    confidence: 1,
    iterations: 3
}
isolated function testIsolatedEvalFailureWithLowConfidence() returns error? {
    test:assertEquals(1, 2);
}

@test:Config
@test:EvalConfig {
    confidence: 1,
    iterations: 3
}
function testNonIsolatedEvalFailureWithLowConfidence() returns error? {
    value += 1;
    test:assertEquals(1, 2);
}

@test:Config {
    dataProvider: goldenDataSet
}
@test:EvalConfig {
    confidence: 1,
    iterations: 3
}
isolated function testIsolatedEvalFailureWithLowConfidenceWithDataProvider(string query) returns error? {
    test:assertEquals(1, 2);
}

@test:Config {
    dataProvider: goldenDataSet
}
@test:EvalConfig {
    confidence: 1,
    iterations: 3
}
function testNonIsolatedEvalFailureWithLowConfidenceWithDataProvider(string query) returns error? {
    value += 1;
    test:assertEquals(1, 2);
}

@test:Config
@test:EvalConfig {
    confidence: 1,
    iterations: 3
}
isolated function testIsolatedEvalFailureForInvalidInput(int query) returns error? {
}

@test:Config
@test:EvalConfig {
    confidence: 1,
    iterations: 3
}
function testNonIsolatedEvalFailureForInvalidInput(int query) returns error? {
    value += 1;
}

@test:Config {
    dataProvider: goldenDataSet
}
@test:EvalConfig {
    confidence: 1,
    iterations: 3
}
isolated function testIsolatedEvalFailureForInvalidInputWithDataProvider(int query) returns error? {
}

@test:Config {
    dataProvider: goldenDataSet
}
@test:EvalConfig {
    confidence: 1,
    iterations: 3
}
function testNonIsolatedEvalFailureForInvalidInputWithDataProvider(int query) returns error? {
    value += 1;
}

@test:Config {
    dataProvider: emptyDataSet
}
@test:EvalConfig {
    confidence: 1,
    iterations: 3
}
isolated function testIsolatedEvalFailureForEmptyDataProvider(string query) returns error? {
}

@test:Config {
    dataProvider: emptyDataSet
}
@test:EvalConfig {
    confidence: 1,
    iterations: 3
}
function testNonIsolatedEvalFailureForEmptyDataProvider(string query) returns error? {
    value += 1;
}

@test:Config {
    dataProvider: nonReadonlyDataset
}
@test:EvalConfig {
    confidence: 1,
    iterations: 3
}
isolated function testIsolatedEvalFailureForNonReadOnlyDataEntry() returns error? {
}

@test:Config {
    dataProvider: nonReadonlyDataset
}
@test:EvalConfig {
    confidence: 1,
    iterations: 3
}
function testNonIsolatedEvalFailureForNonReadOnlyDataEntry() returns error? {
    value += 1;
}

@test:Config
@test:EvalConfig {
    confidence: 1,
    iterations: 3
}
isolated function testIsolatedEvalReturningError() returns error? {
    return error("invalid response returned from the model");
}

@test:Config
@test:EvalConfig {
    confidence: 1,
    iterations: 3
}
function testNonIsolatedEvalReturningError() returns error? {
    value += 1;
    return error("invalid response returned from the model");
}

@test:Config {
    dataProvider: goldenDataSet
}
@test:EvalConfig {
    confidence: 1,
    iterations: 3
}
isolated function testIsolatedEvalReturningErrorWithDataProvider(string query) returns error? {
    return error("invalid response returned from the model");
}

@test:Config {
    dataProvider: goldenDataSet
}
@test:EvalConfig {
    confidence: 1,
    iterations: 3
}
function testNonIsolatedEvalReturningErrorWithDataProvider(string query) returns error? {
    value += 1;
    return error("invalid response returned from the model");
}

