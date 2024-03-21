// Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
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
import ballerina/lang.runtime;
import ballerina/test;

@test:Config {
    dataProvider: stringDataProvider,
    serialExecution: true
}
function testAddingValues0(string fValue, string sValue, string result) returns error? {
    int value1 = check 'int:fromString(fValue);
    int value2 = check 'int:fromString(sValue);
    int result1 = check 'int:fromString(result);
    runtime:sleep(1);
    test:assertEquals(value1 + value2, result1, msg = "Incorrect Sum");
}

function stringDataProvider() returns (string[][]) {
    return [
        ["1", "2", "3"],
        ["10", "20", "30"],
        ["5", "6", "11"],
        ["1", "2", "3"],
        ["10", "20", "30"],
        ["5", "6", "11"],
        ["1", "2", "3"],
        ["10", "20", "30"],
        ["5", "6", "11"],
        ["1", "2", "3"],
        ["10", "20", "30"],
        ["5", "6", "11"]
    ,
        ["1", "2", "3"],
        ["10", "20", "30"],
        ["5", "6", "11"],
        ["1", "2", "3"],
        ["10", "20", "30"],
        ["5", "6", "11"],
        ["1", "2", "3"],
        ["10", "20", "30"],
        ["5", "6", "11"]
    ,
        ["1", "2", "3"],
        ["10", "20", "30"],
        ["5", "6", "11"],
        ["1", "2", "3"],
        ["10", "20", "30"],
        ["5", "6", "11"],
        ["1", "2", "3"],
        ["10", "20", "30"],
        ["5", "6", "11"]
    ];
}
