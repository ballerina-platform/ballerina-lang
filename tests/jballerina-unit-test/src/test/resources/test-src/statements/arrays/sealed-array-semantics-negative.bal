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

function accessInvalidIndexOfSealedArray() returns int {
    int[5] sealedArray = [1, 2, 3, 4, 5];
    int result = sealedArray[5];
    return result;
}

function accessInvalidIndexOfSealedArrayUsingKeyword() returns string {
    string[*] sealedArray = ["1", "2", "3", "4", "5"];
    string result = sealedArray[5];
    return result;
}

function initializeInvalidSizedSealedArray() {
    boolean[4] sealedArray1 = [true, true, false];
    boolean[4] sealedArray2 = [true, true, false, false, true];
}

function assignValueToInvalidIndex() {
    int[5] sealedArray1;
    int[*] sealedArray2 = [1, 2, 3, 4, 5];
    sealedArray1[5] = 12;
    sealedArray2[5] = 12;
    boolean[*] x;
}

function arrayAssignments() {
    int[3] x1 = [3, 4, 5];
    int[] x2 = [1, 2, 3];
    int[3] x3 = x1;
    int[3] x4 = x2;
}

function functionParametersAndReturns() returns int {
    boolean[3] sealedArray = [true, false, false];

    string[2] returnedStrArray = mockFunction(sealedArray);

    return returnedStrArray.length();
}

function mockFunction(boolean[4] sealedArray) returns (string[]) {
    string[*] sealedStrArray = ["Sam", "Smith"];
    return sealedStrArray;
}

function unionMatchStatments() {
    int | int[] | int[4] x = [1, 2, 3, 4];
    int | int[] | int[4] x1 = [1, 2, 3, 4, 5];
    int | int[] | int[4] | int[5] x2 = [1, 2, 3, 4, 5];
}

function unionTestInvalidOrderedMatch(boolean | int[] | float[4] | float[] x) returns string {
    if (x is boolean) {
        return "matched boolean";
    } else if (x is int[]) {
        return "matched int array";
    } else {
        return "matched float array";
    }
}

function invalidJSONArrays() {
    json[4] x1 = [1, true];
    json[4] x2 = [1, 1.5, true, false, "abc"];
    json[4] x3 = [1, 1.5, true, false];
    x3[3] = 12;
    x3[4] = 12;
    json[3] x4 = [1, true, "3"];
    json[*] x5 = x4;
    json[] x6 = [1, true, "3"];
    json[3] x7 = x6;
}

const SEVEN = 7;

type FiniteOne "S1"|"S2";
type FiniteTwo 3|4|5;
type FiniteThree 0|1|2|"S1";
type FiniteFour FiniteThree|"S3";
type FiniteFive FiniteTwo|SEVEN;

function testInvalidArrayAccessByFiniteType() {
    (string|int)[*] sArray = ["ballerina", 1];
    string[1] sArrayTwo = ["ballerina"];
    FiniteOne f1 = "S1";
    FiniteTwo f2 = 3;
    FiniteThree f3 = 2;
    FiniteFour f4 = 0;
    FiniteFive f5 = 3;
    var a = sArray[f1]; // incompatible types: expected 'int', found 'S1|S2'
    var b = sArray[f2]; // invalid array index expression: value space '3|4|5' out of range
    var c = sArrayTwo[f2]; // invalid array index expression: value space '3|4|5' out of range
    var d = sArray[f3]; // incompatible types: expected 'int', found '0|1|2|S1'
    var e = sArray[f4]; // incompatible types: expected 'int', found '0|1|2|S1|S3'
    var f = sArrayTwo[f5]; // invalid array index expression: value space '3|4|5|7' out of range
}
