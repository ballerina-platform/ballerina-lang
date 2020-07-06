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

import ballerina/lang.'int as ints;

function testArrayForeachAndTrap() returns int {
    string[] invalidArray = ["2", "waruna", "7"];
    int|error result = trap convertAndGetSumFromArray(invalidArray);
    assertTrue(result is error);
    // Program should continue without panic.
    string[] validArray = ["2", "5", "7"];
    result = trap convertAndGetSumFromArray(validArray);
    assertTrue(result is int);
    return <int>result;
}

function testArrayForeachAndPanic() {
    string[] invalidArray = ["2", "waruna", "7"];
    int result = convertAndGetSumFromArray(invalidArray);
    // This line should not be executed.
    panic error(ASSERTION_ERROR_REASON,
                message = "Program should be panic before this line");
}

function convertAndGetSumFromArray(string[] stringNumbers) returns int {
    int sum = 0;
    stringNumbers.forEach(function (string s) {
	    int val = <int>ints:fromString(s);
        sum = sum + val;
    });
    return sum;
}
const ASSERTION_ERROR_REASON = "AssertionError";

function assertTrue(any|error actual) {
    if actual is boolean && actual {
        return;
    }
    panic error(ASSERTION_ERROR_REASON,
                message = "expected 'true', found '" + actual.toString () + "'");
}
