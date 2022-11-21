// Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

type CodeFragment [string, string];

@test:Config {
    dataProvider: dataGen
}
function intDataProviderTest(int value1, int value2, int result1) returns error? {
    test:assertEquals(value1 + value2, result1, msg = "The sum is not correct");
}

@test:Config {
    dataProvider: dataGen2
}
function fruitsDataProviderTest(int value1, int value2, string fruit) returns error? {
    test:assertEquals(value1, value2, msg = "The sum is not correct");
    test:assertEquals(fruit.length(), 6);
}

@test:Config{
    dataProvider: dataGen3
}
function jsonDataProviderTest (json json1, json json2, json json3) {
    json a = {"a": "a"};
    json b = {"b": "b"};
    json c = {"c": "c"};
    test:assertEquals(json1, a, msg = "json data provider failed");
    test:assertEquals(json2, b, msg = "json data provider failed");
    test:assertEquals(json3, c, msg = "json data provider failed");
}

@test:Config {
    dataProvider: dataGen4
}
function errorDataProviderTest(int value1, int value2, int result1) returns error? {
    test:assertEquals(value1 + value2, result1, msg = "The sum is not correct");
}

@test:Config {
    dataProvider: dataGen6
}
function testFunction1(int value1, int value2, int result1) returns error? {
    test:assertEquals(value1 + value2, result1, msg = "The sum is not correct");
}

@test:Config {
    dataProvider: dataGen7
}
function intArrayDataProviderTest(int value1, int value2, int result1) returns error? {
    test:assertEquals(value1 + value2, result1, msg = "The sum is not correct");
}

@test:Config {
    dataProvider: dataGen8
}
function testFunction2(int value1, int value2, int result1) returns error? {
    test:assertEquals(value1 + value2, result1, msg = "The sum is not correct");
}

@test:Config {
    dataProvider: dataGen9
}
function testFunction3(string value1, string value2) returns error? {
   test:assertEquals("E", value1, msg = "The code fragment is not correct.");
}

string tally = "";  // used to track which function has been executed

function beforeFunction() {
    tally += "b";
}

function afterFunction() {
    tally += "a";
}

@test:Config {
    dataProvider:  dataGen10,
     before:  beforeFunction,
    after:  afterFunction
}
function testDividingValues(string fValue, string sValue, string result) returns error? {
    tally += "f";
    
    int value1 = check 'int:fromString(fValue);
    int value2 = check 'int:fromString(sValue);
    int result1 = check 'int:fromString(result);

    test:assertEquals(value1/value2, result1, msg = "Incorrect Division");
}

@test:Config {
    dependsOn: [testDividingValues]
}
function testExecutionOfBeforeAfter() {
    test:assertEquals(tally, "bfabfabfabfabfa");
}

function beforeFailsFunction() {
    tally += "b";

    // Condition for a failure
    if (tally == "bfabfab") {
        // Something happens during the 3rd iteration
        int a = 9/0;
    }
}

@test:Config {
    dataProvider:  dataGen10,
    before:  beforeFailsFunction,
    after:  afterFunction
}
function testDividingValuesWithBeforeFailing(string fValue, string sValue, string result) returns error? {
    tally += "f";
    
    int value1 = check 'int:fromString(fValue);
    int value2 = check 'int:fromString(sValue);
    int result1 = check 'int:fromString(result);

    test:assertEquals(value1/value2, result1, msg = "Incorrect Division");
}

// Depends on testDividingValuesWithBeforeFailing
// However since it fails in one instance, dependsOn will not work
@test:Config {}
function testExecutionOfBeforeFailing() {
    test:assertEquals(tally, "bfabfabbfabfa");
}

function afterFailsFunction() {
    tally += "a";

    // Condition for a failure
    if (tally == "bfabfabfa") {
        // Something happens during the 4th iteration
        int a = 9/0;
    }
}

@test:Config {
    dataProvider:  dataGen10,
    before:  beforeFunction,
    after:  afterFailsFunction
}
function testDividingValuesWithAfterFailing(string fValue, string sValue, string result) returns error? {
    tally += "f";
    
    int value1 = check 'int:fromString(fValue);
    int value2 = check 'int:fromString(sValue);
    int result1 = check 'int:fromString(result);

    test:assertEquals(value1/value2, result1, msg = "Incorrect Division");
}

// Depends on testDividingValuesWithAfterFailing
// However since it fails in one instance, dependsOn will not work
@test:Config {}
function testExecutionOfAfterFailing() {
    test:assertEquals(tally, "bfabfabfabfabfa");
}

@test:Config {
    dataProvider:  dataGen11,
    before:  beforeFunction,
    after:  afterFunction
}
function testDividingValuesNegative(string fValue, string sValue, string result) returns error? {
    tally += "f";
    
    int value1 = check 'int:fromString(fValue);
    int value2 = check 'int:fromString(sValue);
    int result1 = check 'int:fromString(result);

    test:assertEquals(value1/value2, result1, msg = "Incorrect Division");
}

@test:Config {
    dependsOn: [testDividingValuesNegative]
}
function testExecutionOfDataValueFailing() {
    test:assertEquals(tally, "bfabfabfabfabfa");
}

@test:Config {
    dataProvider:  dataGen12
}
function errorData(error input, string expected) {
    final string actual = getFunction(input);
    test:assertEquals(actual, expected);
}

@test:Config {
    dataProvider: dataGen13
}
function mapOfTupleOfFunctionTest(function (int x) returns boolean func, int value) {
    test:assertTrue(func(value));
}

@test:Config {
    dataProvider: dataGen14
}
function arrayOfArrayOfFunctionTest(function (int x) returns boolean func1, function (int x) returns boolean func2) {
    int value = 2;
    test:assertTrue(func1(value) || func2(value));
}

// Data Providers

function dataGen() returns map<[int, int, int]>|error {
    map<[int, int, int]> dataSet = {
        "Case1": [1, 2, 4],
        "Case2": [10, 20, 31],
        "Case3": [5, 6, 11]
    };
    return dataSet;
}

function dataGen2() returns map<[int, int, string]>|error {
    map<[int, int, string]> dataSet = {
        "banana": [10, 10, "banana"],
        "cherry": [5, 5, "cherry"]
    };
    return dataSet;
}

function dataGen3() returns map<[json,json, json]> {
    map<[json, json, json]> dataSet = {
        "json1": [{"a": "a"}, {"b": "b"}, {"c": "c"}],
        "json2": [{"a": "a"}, {"b": "b"}, {"c": "c"}]
    };
    return dataSet;
}

function dataGen4() returns map<[string]>|error{
    return error("Error occurred while generating data set.");
}

function dataGen6() returns map<[int, int, int]>|error {
    map<[int, int, int]> dataSet = {
        "CaseNew1": [1, 2, 3],
        "CaseNew2": [10, 20, 30],
        "Case3": [5, 6, 11]
    };
    return dataSet;
}

function dataGen7() returns int[][] {
    return [[1, 2, 4], [10, 20, 31], [5, 6, 11]];
}

function dataGen8() returns map<[int, int, int]>|error {
    map<[int, int, int]> dataSet = {
        "Case#1": [1, 2, 3],
        "Case#2": [10, 20, 30],
        "Case#3": [5, 6, 11]
    };
    return dataSet;
}

function dataGen9() returns map<CodeFragment>|error {
    CodeFragment[] sources = [
        ["E", "`'" +  string`"\a"` + string`"`],
        ["E", "\"\\" + "u{D7FF}\"" + "\"\t\""],
        ["E",  "a +\n\r b"],
        ["E", "(x * 1) != (y / 3) || (a ^ b) == (b & c) >> (1 % 2)"],
        ["E",  "(1"],
        ["E", "a:x(c,d)[]; ^(x|y).ok();"],
        ["E",  string`map<any> v = { "x": 1 };`]];

    map<CodeFragment> tests = {};
    foreach var s in sources {
        tests[s[1]] = s;
    }
    return tests;
}

function dataGen10() returns (string[][]) {
    return [["10", "2", "5"], ["10", "1", "10"], ["10", "2", "5"], ["10", "1", "10"], ["10", "2", "5"]];
}

function dataGen11() returns (string[][]) {
    return [["10", "2", "5"], ["10", "1", "10"], ["10", "0", "5"], ["10", "1", "10"], ["10", "2", "5"]];
}

function dataGen12() returns map<[error, string]> {
    error e = error("foo");

    return {
        "foo": [e, "foo"]
    };
}

function dataGen13() returns map<[function, int]> {
    map<[function, int]> dataSet = {
        "1": [isOdd, 1],
        "2": [isEven, 2],
        "3": [isOdd, 3],
        "4": [isEven, 4]
    };
    return dataSet;
}

function dataGen14() returns function[][] {
    function[][] dataSet = [[isOdd, isEven], [isEven, isOdd], [isEven, isEven]];
    return dataSet;
}

function isEven(int x) returns boolean {
    return x % 2 == 0;
}

function isOdd(int x) returns boolean {
    return x % 2 != 0;
}

public function getFunction(error e) returns (string) {
    return e.message();
}

type Feed record {
    int responseCode;
    string message;
};

@test:Config{ dataProvider:getStateResponseDataProvider }
function testGetState(Feed dataFeed) {
    test:assertEquals(200, dataFeed.responseCode);
    test:assertEquals("Hello World!!!", dataFeed.message);
}

function getStateResponseDataProvider() returns Feed[][] {
     return [
            [{responseCode:200, message:"Hello World!!!"}],
            [{responseCode:20, message:"Hello World!!!"}]
     ];
}
