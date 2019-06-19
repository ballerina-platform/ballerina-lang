// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import utils;

const EXPECTED_THE_ORIGINAL_VALUE_FAILURE_MESSAGE = "expected the original value";
const EXPECTED_FIXED_AND_IMPLIED_LENGTH_TO_BE_EQUAL_FAILURE_MESSAGE =
                                            "expected fixed length array and implied length array to be equal";

// Both kinds of type descriptor are covariant in the types of their members.
@test:Config {}
function testArrayCovariance() {
    string[] stringArray = ["string one", "string two"];
    (string|int)?[] stringOrIntArray = stringArray;
    stringOrIntArray[2] = "string three";

    test:assertEquals(stringOrIntArray[0], "string one", msg = EXPECTED_THE_ORIGINAL_VALUE_FAILURE_MESSAGE);
    test:assertEquals(stringOrIntArray[1], "string two", msg = EXPECTED_THE_ORIGINAL_VALUE_FAILURE_MESSAGE);
    test:assertEquals(stringOrIntArray[2], "string three", msg = EXPECTED_THE_ORIGINAL_VALUE_FAILURE_MESSAGE);
    utils:assertPanic(function () { stringOrIntArray[0] = 1; },
                      INHERENT_TYPE_VIOLATION_REASON,
                      INVALID_REASON_ON_INHERENT_TYPE_VIOLATING_ARRAY_INSERTION_FAILURE_MESSAGE);
}

// array-type-descriptor := member-type-descriptor [ [ array-length ] ]
// member-type-descriptor := type-descriptor
// array-length := int-literal | constant-reference-expr | implied-array-length
// implied-array-length := ! ...
@test:Config {}
function testArrayTypeDescriptor() {
    int[] array1 = [];
    int[] expectedArray = [];
    test:assertEquals(array1, expectedArray, msg = EXPECTED_FIXED_AND_IMPLIED_LENGTH_TO_BE_EQUAL_FAILURE_MESSAGE);

    // An array length of !... means that the length of the array is to be implied from the
    // context; this is allowed in the same contexts where var would be allowed in place of the
    // type descriptor in which array-length occurs (see “Typed binding patterns”); its meaning
    // is the same as if the length was specified explicitly.
    string[3] array2 = ["a", "b", "c"];
    string[*] array3 = ["a", "b", "c"];
    any tempVar = array3;
    test:assertTrue(tempVar is string[3], msg = EXPECTED_FIXED_AND_IMPLIED_LENGTH_TO_BE_EQUAL_FAILURE_MESSAGE);
    test:assertEquals(array2, array3, msg = EXPECTED_FIXED_AND_IMPLIED_LENGTH_TO_BE_EQUAL_FAILURE_MESSAGE);
}
