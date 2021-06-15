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

public type Label int; // If the TypeHashVisitor takes in type.flags of `Label` type, there will be runtime faliures.

function testClosedIntRange() {
    int startValue = 1;
    int endValue = 5;
    int[] returnArray = [];
    int returnArrayIndex = 0;
    foreach var val in startValue ... endValue {
        returnArray[returnArrayIndex] = val;
        returnArrayIndex += 1;
    }

    assertEquality(returnArray.length(), 5);
    assertEquality(returnArray[0], 1);
    assertEquality(returnArray[1], 2);
    assertEquality(returnArray[2], 3);
    assertEquality(returnArray[3], 4);
    assertEquality(returnArray[4], 5);
}

type AssertionError distinct error;

const ASSERTION_ERROR_REASON = "AssertionError";

function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    string expectedValAsString = expected is error ? expected.toString() : expected.toString();
    string actualValAsString = actual is error ? actual.toString() : actual.toString();
    panic error AssertionError(ASSERTION_ERROR_REASON,
            message = "expected '" + expectedValAsString + "', found '" + actualValAsString + "'");
}
