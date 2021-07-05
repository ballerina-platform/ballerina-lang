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

@test:Config{
    dataProvider: dataGenMod1
}
function stringDataProviderMod1Test (string fValue, string sValue, string result) returns error? {

    int|error val1 = fValue.cloneWithType(int);
    int value1 = val1 is int ? val1 : 0;
    int|error val2 = sValue.cloneWithType(int);
    var value2 = val2 is int ? val2 : 0;
    int|error res1 = result.cloneWithType(int);
    var result1 = res1 is int ? res1 : 0;
    test:assertEquals(value1 + value2, result1, msg = "The sum is not correct");
    return;
}

function dataGenMod1() returns string[][] | error{
    return [["1", "2", "3"], ["10", "20", "30"], ["5", "6", "11"]];
}
