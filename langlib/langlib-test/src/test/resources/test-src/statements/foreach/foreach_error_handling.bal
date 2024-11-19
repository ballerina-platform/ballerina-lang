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

function testArrayForeachAndTrap() {
    string[] invalidArray = ["2", "waruna", "7"];
    int|error result = trap convertAndGetSumFromArray(invalidArray);
    assertTrue(result is error);
    // Program should continue without panic.
    string[] validArray = ["2", "5", "7"];
    result = trap convertAndGetSumFromArray(validArray);
    assertTrue(result is int);
    assertTrue(checkpanic result == 14);
}

function testArrayForeachAndPanic() {
    string[] invalidArray = ["2", "waruna", "7"];
    int|error result = trap convertAndGetSumFromArray(invalidArray);
    assertTrue(result is error);
    if (result is error) {
        error e = result;
        assertEquality("error(\"{ballerina/lang.int}NumberParsingError\",message=\"'string' value 'waruna' cannot be " +
                "converted to 'int'\")", e.toString());
        string expected = "[callableName: fromString moduleName: ballerina.lang.int.0 fileName: int.bal lineNumber: 175," +
        "callableName: $lambda$_0  fileName: foreach_error_handling.bal lineNumber: 54,callableName: forEach moduleName: " +
        "ballerina.lang.array.0 fileName: array.bal lineNumber: 115,callableName: convertAndGetSumFromArray  fileName: " +
        "foreach_error_handling.bal lineNumber: 53,callableName: testArrayForeachAndPanic  fileName: " +
        "foreach_error_handling.bal lineNumber: 32]";
        assertEquality(expected, e.stackTrace().toString());
    } else {
        // This line should not be executed.
        panic error(ASSERTION_ERROR_REASON,
                message = "Program should be panic before this line");
    }
}

function convertAndGetSumFromArray(string[] stringNumbers) returns int {
    int sum = 0;
    stringNumbers.forEach(function(string s) {
        int val = checkpanic ints:fromString(s);
        sum = sum + val;
    });
    return sum;
}

const ASSERTION_ERROR_REASON = "AssertionError";

function assertTrue(any|error actual) {
    if actual is boolean && actual {
        return;
    }

    string actualValAsString = actual is error ? actual.toString() : actual.toString();
    panic error(ASSERTION_ERROR_REASON, message = "expected 'true', found '" + actualValAsString + "'");
}

function assertEquality(anydata expected, anydata actual) {
    if expected == actual {
        return;
    }

    panic error(ASSERTION_ERROR_REASON, message = "expected '" + expected.toString() + "', found '" + actual.toString() + "'");
}
