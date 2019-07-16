// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

// Int Arrays

function createIntSealedArray() returns int {
    int[5] sealedArray = [2, 15, 200, 1500, 5000];
    return sealedArray.length();
}

function createIntSealedArrayWithLabel() returns int {
    int[*] sealedArray = [2, 15, 200, 1500, 5000];
    return sealedArray.length();
}

function createIntDefaultSealedArray() returns [int[], int] {
    int[5] sealedArray = [0, 0, 0, 0, 0];
    return [sealedArray, sealedArray.length()];
}

// Boolean Arrays

function createBoolSealedArray() returns int {
    boolean[5] sealedArray = [true, false, false, true, false];
    return sealedArray.length();
}

function createBoolSealedArrayWithLabel() returns int {
    boolean[*] sealedArray = [true, false, false, true, false];
    return sealedArray.length();
}

function createBoolDefaultSealedArray() returns [boolean[], int] {
    boolean[5] sealedArray = [false, false, false, false, false];
    return [sealedArray, sealedArray.length()];
}

// Float Arrays

function createFloatSealedArray() returns int {
    float[5] sealedArray = [0.0, 15.2, 1100.0, -25.8, -10.0];
    return sealedArray.length();
}

function createFloatSealedArrayWithLabel() returns int {
    float[*] sealedArray = [0.0, 15.2, 1100.0, -25.8, -10.0];
    return sealedArray.length();
}

function createFloatDefaultSealedArray() returns [float[], int] {
    float[5] sealedArray = [0.0, 0.0, 0.0, 0.0, 0.0];
    return [sealedArray, sealedArray.length()];
}

// String Arrays

function createStringSealedArray() returns int {
    string[5] sealedArray = ["a", "abc", "12", "-12", "."];
    return sealedArray.length();
}

function createStringSealedArrayWithLabel() returns int {
    string[*] sealedArray = ["a", "abc", "12", "-12", "."];
    return sealedArray.length();
}

function createStringDefaultSealedArray() returns [string[], int] {
    string[5] sealedArray = ["", "", "", "", ""];
    return [sealedArray, sealedArray.length()];
}

// Any Type Arrays

function createAnySealedArray() returns int {
    any[5] sealedArray = ["a", true, 12, -12.5, "."];
    return sealedArray.length();
}

function createAnySealedArrayWithLabel() returns int {
    any[*] sealedArray = ["a", true, 12, -12.5, "."];
    return sealedArray.length();
}

// Record Type Arrays

type Person record {
    string name = "";
    int age = 0;
};

function createRecordSealedArray() returns int {
    Person[5] sealedArray = [{}, {}, {}, {}, {}];
    return sealedArray.length();
}

function createRecordSealedArrayWithLabel() returns int {
    Person[*] sealedArray = [{}, {}, {}, {}, {}];
    return sealedArray.length();
}

// Byte Arrays

function createByteSealedArray() returns int {
    byte a = 5;
    byte[5] sealedArray = [a, a, a, a, a];
    return sealedArray.length();
}

function createByteSealedArrayWithLabel() returns int {
    byte a = 7;
    byte[*] sealedArray = [a, a, a, a, a];
    return sealedArray.length();
}

function createByteDefaultSealedArray() returns [byte[], int] {
    byte[5] sealedArray = [0, 0, 0, 0, 0];
    return [sealedArray, sealedArray.length()];
}

// Tuple Arrays

function createTupleSealedArray() returns int {
    [int, boolean][3] sealedArray = [[2, true], [3, false], [6, true]];
    sealedArray[2] = [5, false];
    return sealedArray.length();
}

function createTupleSealedArrayWithLabel() returns int {
    [int, boolean][*] sealedArray = [[2, true], [3, false], [6, true]];
    return sealedArray.length();
}

// Function Params and Returns

function functionParametersAndReturns() returns [int, int] {
    boolean[3] sealedArray = [true, false, false];
    boolean[3] returnedBoolArray = [false, false, false];
    string[2] returnedStrArray = ["", ""];
    [returnedBoolArray, returnedStrArray] = mockFunction(sealedArray);

    return [returnedBoolArray.length(), returnedStrArray.length()];
}

function mockFunction(boolean[3] sealedArray) returns [boolean[3], string[2]] {
    string[*] sealedStrArray = ["Sam", "Smith"];
    return [sealedArray, sealedStrArray];
}

// Runtime Exceptions

function invalidIndexAccess(int index) {
    boolean[3] x1 = [true, false, true];
    boolean x2 = x1[index];
}

function assignedArrayInvalidIndexAccess() {
    int[3] x1 = [1, 2, 3];
    int[] x2 = x1;
    x2[4] = 10;
}

// Match Statments

function unionAndMatchStatementSealedArray(float[] x) returns string {
    return unionTestSealedArray(x);
}

function unionAndMatchStatementUnsealedArray(float[] x) returns string {
    return unionTestNoSealedArray(x);
}

function unionTestSealedArray(boolean | int[] | float[4] | float[] | float[6] x) returns string {
    if (x is boolean) {
        return "matched boolean";
    } else if (x is int[]) {
        return "matched int array";
    } else if (x is float[6]) {
        return "matched sealed float array size 6";
    } else if (x is float[4]) {
        return "matched sealed float array size 4";
    } else {
        return "matched float array";
    }
}

function unionTestNoSealedArray(boolean | int[] | float[4] | float[] x) returns string {
    if (x is boolean) {
        return "matched boolean";
    } else if (x is int[]) {
        return "matched int array";
    } else {
        return "matched float array";
    }
}

function accessIndexOfMatchedSealedArray(int[] | int[3] x, int index) returns int {
    if (x is int[3]) {
        x[index] = 10;
        return x[index];
    } else {
        x[index] = 10;
        return x[index];
    }
}

// JSON Arrays

function createJSONSealedArray() returns int {
    json[5] sealedArray = [false, "abc", "12", -12, "."];
    return sealedArray.length();
}

function createJSONSealedArrayWithLabel() returns int {
    json[*] sealedArray = [false, "abc", "12", -12, "."];
    return sealedArray.length();
}

function invalidIndexJSONArray(int index) {
    json[3] x1 = [true, 12, false];
    x1[index] = 100.1;
}

function invalidIndexReferenceJSONArray() {
    json[3] x1 = [1, true, "3"];
    json[] x2 = x1;
    x2[3] = 1.0;
}

function createJSONDefaultSealedArray() returns [json[], int] {
    json[5] sealedArray = [(), (), (), (), ()];
    return [sealedArray, sealedArray.length()];
}

function testSealedArrayConstrainedMap (int[3] x1, int[] x2) returns int {
    map<int[]> x = {};
    x["v1"] = x1;
    x["v2"] = x2;
    int[] arr = <int[]>x["v1"];
    return arr[2];
}

function testSealedArrayConstrainedMapInvalidIndex (int[3] x1, int index) {
    map<int[]> x = {};
    x["v1"] = x1;
    x["v1"][index] = 4;
}
