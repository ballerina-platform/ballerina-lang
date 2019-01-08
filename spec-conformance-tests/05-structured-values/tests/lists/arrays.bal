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

// A list value is a container that keeps its members in an ordered list.
@test:Config {}
function testArrayMemberOrder() {
    int a1 = 30;
    int a2 = 10;
    int a3 = 20;
    int a4 = 5;
    int a5 = 11;
    int[] intArray = [a1, a2, a3];

    test:assertEquals(intArray[0], a1, msg = "expected value not found at index 0");
    test:assertEquals(intArray[1], a2, msg = "expected value not found at index 1");
    test:assertEquals(intArray[2], a3, msg = "expected value not found at index 2");

    intArray[3] = a4;
    test:assertEquals(intArray[0], a1, msg = "expected value not found at index 0");
    test:assertEquals(intArray[1], a2, msg = "expected value not found at index 1");
    test:assertEquals(intArray[2], a3, msg = "expected value not found at index 2");
    test:assertEquals(intArray[3], a4, msg = "expected value not found at index 3");

    intArray[0] = a5;
    test:assertEquals(intArray[0], a5, msg = "expected value not found at index 0");
    test:assertEquals(intArray[1], a2, msg = "expected value not found at index 1");
    test:assertEquals(intArray[2], a3, msg = "expected value not found at index 2");
    test:assertEquals(intArray[3], a4, msg = "expected value not found at index 3");
}

// A member of a list can be referenced by an integer index representing its position in the list.
// For a list of length n, the indices of the members of the list, from first to last, are 0,1,...,n - 1.
@test:Config {}
function testArrayMemberReferenceByValidIntegerIndex() {
    string a1 = "test string 1";
    string a2 = "test string 2";
    string a3 = "test string 3";
    string a4 = "test string 4";
    string a5 = "test string 5";
    string a6 = "test string 6";
    string[] stringArray = [a1, a2, a3, a4, a5];

    string b1 = stringArray[0];
    string b2 = stringArray[1];
    string b3 = stringArray[2];
    string b4 = stringArray[3];
    string b5 = stringArray[4];

    test:assertEquals(b1, a1, msg = "expected value not found at index 0");
    test:assertEquals(b2, a2, msg = "expected value not found at index 1");
    test:assertEquals(b3, a3, msg = "expected value not found at index 2");
    test:assertEquals(b4, a4, msg = "expected value not found at index 3");
    test:assertEquals(b5, a5, msg = "expected value not found at index 4");

    stringArray[2] = a6;
    string b6 = stringArray[2];
    test:assertEquals(b6, a6, msg = "expected value not found at index 2");
}

@test:Config {}
function testArrayMemberReferenceByInvalidIntegerIndex() {
    float[] floatArray = [1.1, 0.0, 2.20]; 

    int index = -1;
    assertErrorReason(trap floatArray[index], "{ballerina}IndexOutOfRange", 
                      "invalid reason on access by negative index");

    index = floatArray.length();
    assertErrorReason(trap floatArray[index], "{ballerina}IndexOutOfRange", 
                      "invalid reason on access by index == array length");

    index = floatArray.length() + 3;
    assertErrorReason(trap floatArray[index], "{ballerina}IndexOutOfRange", 
                      "invalid reason on access by index > array length");
}

// The shape of a list value is an ordered list of the shapes of its members.
@test:Config {}
function testArrayShape() {
    // TODO: 
}

// A list is iterable as a sequence of its members.
@test:Config {}
function testArrayMemberIteration() {
    int a = 4;
    string b = "string 1";
    string c = "string 2";
    int d = 1;

    (int|string)[] array = [a, b, c, d];
    (int|string)[] arrayTwo = [a, b, c, d];
    int currentIndex = 0;

    foreach string|int intValue in array {
        test:assertEquals(intValue, arrayTwo[currentIndex], 
                          msg = "incorrect member value found on iteration");
        currentIndex = currentIndex + 1;
    }
}

// The inherent type of a list value determines a type Ti for a member with index i.
// The runtime system will enforce a constraint that a value written to index i will
// belong to type Ti. Note that the constraint is not merely that the value looks
// like Ti.
@test:Config {}
function testArrayInherentTypeViolation() {
    int[] intArray = [1, 2];
    any[] anyArray = intArray;
    assertErrorReason(trap insertElementToArray(anyArray, intArray.length() - 1, "not an int"), 
                      "{ballerina}InherentTypeViolation", 
                      "invalid reason on inherent type violating array insertion");

    map<string>[] stringMapArray = [ 
        { 
            one: "test string 1",
            two: "test string 2" 
        },
        { 
            three: "test string 3"
        }
    ];
    anyArray = stringMapArray;

    // `m` looks like `map<string>`
    map<string|int> stringOrIntMap = {
        one: "test string 1",
        two: "test string 2"
    };
    assertErrorReason(trap insertElementToArray(anyArray, 0, stringOrIntMap), 
                      "{ballerina}InherentTypeViolation", 
                      "invalid reason on inherent type violating array insertion");
}

function insertElementToArray(any[] array, int index, any element) {
    array[index] = element;
}

# Util method expected to be used with the result of a trapped expression. 
# Validates that `result` is of type `error` and that the error has the reason specified as `expectedReason`,
# and fails with the `invalidReasonFailureMessage` string if the reasons mismatch.
# 
# + result - the result of the trapped expression
# + expectedReason - the reason the error is expected to have
# + invalidReasonFailureMessage - the failure message on reason mismatch
function assertErrorReason(any|error result, string expectedReason, string invalidReasonFailureMessage) {
    if (result is error) {
        test:assertEquals(result.reason(), expectedReason, msg = invalidReasonFailureMessage);
    } else {
        test:assertFail(msg = "expected expression to panic");
    }
}
