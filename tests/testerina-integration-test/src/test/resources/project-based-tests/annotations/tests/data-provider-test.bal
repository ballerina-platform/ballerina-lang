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

@test:Config{
    dataProvider: dataGen
}
function stringDataProviderTest (string fValue, string sValue, string result) returns error? {
    int value1 = check 'int:fromString(fValue);
    int value2 = check 'int:fromString(sValue);
    int result1 = check 'int:fromString(result);
    test:assertEquals(value1 + value2, result1, msg = "The sum is not correct");
    return;
}

function dataGen() returns (string[][]) {
    return [["1", "2", "3"], ["10", "20", "30"], ["5", "6", "11"]];
}

@test:Config{
    dataProvider: dataGen2
}
function stringDataProviderTest2 (string fValue, string sValue, string result) returns error? {
    int value1 = check 'int:fromString(fValue);
    int value2 = check 'int:fromString(sValue);
    int result1 = check 'int:fromString(result);
    test:assertEquals(value1 + value2, result1, msg = "The sum is not correct");
    return;
}

function dataGen2() returns (string[][]) {
    return [["1", "2", "3"]];
}

@test:Config{
    dataProvider: dataGen3
}
function jsonDataProviderTest (json fValue, json sValue, json result) {
    json a = {"a": "a"};
    json b = {"b": "b"};
    json c = {"c": "c"};
    test:assertEquals(fValue, a, msg = "json data provider failed");
    test:assertEquals(sValue, b, msg = "json data provider failed");
    test:assertEquals(result, c, msg = "json data provider failed");
}

function dataGen3() returns (json[][]) {
    return [[{"a": "a"}, {"b": "b"}, {"c": "c"}]];
}

// @test:Config{
//     dataProvider: dataGen4
// }
// function tupleDataProviderTest ([int, int, [int, int]] result) {
//     int a = 10;
//     int b = 20;
//     [int, int] c = [30, 30];
//     test:assertEquals(result[0], a, msg = "tuple data provider failed");
//     test:assertEquals(result[1], b, msg = "tuple data provider failed");
//     test:assertEquals(result[2], c, msg = "tuple data provider failed");
// }

// function dataGen4() returns ([int, int, [int, int]][]) {
//     return [[10, 20, [30, 30]]];
// }

