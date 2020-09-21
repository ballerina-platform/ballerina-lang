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
import ballerina/lang.'int;

const constLength = 2;

// Int Arrays

function createIntSealedArray() {
    int[5] sealedArray = [2, 15, 200, 1500, 5000];
    assertArrayLengthPanic(5, sealedArray);
}

function createIntAutoFilledSealedArray() {
    int[5] sealedArray = [2];
    sealedArray[4] = 2;
    assertArrayValuePanic(2, sealedArray, 4);
    assertArrayValuePanic(2, sealedArray, 0);
    assertArrayValuePanic(0, sealedArray, 1);
    assertArrayValuePanic(0, sealedArray, 2);
    assertArrayValuePanic(0, sealedArray, 2);
    assertArrayLengthPanic(5, sealedArray);
}

function createIntSealedArrayWithLabel() {
    int[*] sealedArray = [2, 15, 200, 1500, 5000];
    assertArrayLengthPanic(5, sealedArray);
}

function createIntDefaultSealedArray() {
    int[5] sealedArray = [0, 0, 0, 0, 0];
    assertArrayLengthPanic(5, sealedArray);
    isEqualPanic("[0,0,0,0,0]", sealedArray);
}

function createSealedArraysOfIntSubtypes() {
    ('int:Unsigned16|'int:Signed32)[2] sealedArray = [];
    assertArrayLengthPanic(2, sealedArray);
}

// Boolean Arrays

function createBoolSealedArray() {
    boolean[5] sealedArray = [true, false, false, true, false];
    assertArrayLengthPanic(5, sealedArray);
}

function createBoolAutoFilledSealedArray() {
    boolean[5] sealedArray = [true, false];
    sealedArray[4] = false;
    assertArrayValuePanic(false, sealedArray, 4);
    assertArrayLengthPanic(5, sealedArray);
}

function createBoolSealedArrayWithLabel() {
    boolean[*] sealedArray = [true, false, false, true, false];
    assertArrayLengthPanic(5, sealedArray);
}

function createBoolDefaultSealedArray() {
    boolean[5] sealedArray = [false, false, false, false, false];
    assertArrayLengthPanic(5, sealedArray);
    isEqualPanic("[false,false,false,false,false]", sealedArray);
}

// Float Arrays

function createFloatSealedArray() {
    float[5] sealedArray = [0.0, 15.2, 1100.0, -25.8, -10.0];
    assertArrayLengthPanic(5, sealedArray);
}

function createFloatAutoFilledSealedArray() {
    float[5] sealedArray = [0.0, 15.2];
    sealedArray[4] = 2.5;
    assertArrayValuePanic(2.5, sealedArray, 4);
    assertArrayLengthPanic(5, sealedArray);
}

function createFloatSealedArrayWithLabel() {
    float[*] sealedArray = [0.0, 15.2, 1100.0, -25.8, -10.0];
    assertArrayLengthPanic(5, sealedArray);
}

function createFloatDefaultSealedArray() {
    float[5] sealedArray = [0.0, 0.0, 0.0, 0.0, 0.0];
    assertArrayLengthPanic(5, sealedArray);
    isEqualPanic("[0.0,0.0,0.0,0.0,0.0]", sealedArray);
}

// String Arrays

function createStringSealedArray() {
    string[5] sealedArray = ["a", "abc", "12", "-12", "."];
    assertArrayLengthPanic(5, sealedArray);
}

function createStringAutoFilledSealedArray() {
    string[5] sealedArray = ["a"];
    assertArrayLengthPanic(5, sealedArray);
}

function createStringSealedArrayWithLabel() {
    string[*] sealedArray = ["a", "abc", "12", "-12", "."];
    assertArrayLengthPanic(5, sealedArray);
}

function createStringDefaultSealedArray() {
    string[5] sealedArray = ["", "", "", "", ""];
    isEqualPanic("[\"\",\"\",\"\",\"\",\"\"]", sealedArray);
    assertArrayLengthPanic(5, sealedArray);
}

// Any Type Arrays

function createAnySealedArray() {
    any[5] sealedArray = ["a", true, 12, -12.5, "."];
    assertArrayLengthPanic(5, sealedArray);
}

function createAnyAutoFilledSealedArray() {
    any[5] sealedArray = ["a"];
    sealedArray[4] = 23;
    sealedArray[3] = -2.5;
    assertArrayLengthPanic(5, sealedArray);
}

function createAnySealedArrayWithLabel() {
    any[*] sealedArray = ["a", true, 12, -12.5, "."];
    assertArrayLengthPanic(5, sealedArray);
}

// Record Type Arrays

type Person record {
    string name = "";
    int age = 0;
};

type PersonFillable record {
    string name = "doe";
    int age?;
    boolean employee?;
};

function createRecordSealedArrayAutoFill() {
    PersonFillable p = {name:"John", age:25, employee:false};
    PersonFillable[5] sealedArray = [p];
    sealedArray[3] = p;
    assertEqualPanic("doe", sealedArray[2].name);
    assertEqualPanic("John", sealedArray[3].name);
    assertArrayLengthPanic(5, sealedArray);
}

function createRecordSealedArray() {
    Person[5] sealedArray = [{}, {}, {}, {}, {}];
    assertArrayLengthPanic(5, sealedArray);
}

function createRecordAutoFilledSealedArray() {
    Person[5] sealedArray = [{}];
    assertArrayLengthPanic(5, sealedArray);
}

function createRecordSealedArrayWithLabel() {
    Person[*] sealedArray = [{}, {}, {}, {}, {}];
    assertArrayLengthPanic(5, sealedArray);
}

// Byte Arrays

function createByteSealedArray() {
    byte a = 5;
    byte[5] sealedArray = [a, a, a, a, a];
    assertArrayLengthPanic(5, sealedArray);
}

function createByteAutoFilledSealedArray() {
    byte a = 5;
    byte[5] sealedArray = [a];
    sealedArray[4] = a;
    assertArrayLengthPanic(5, sealedArray);
}

function createByteSealedArrayWithLabel() {
    byte a = 7;
    byte[*] sealedArray = [a, a, a, a, a];
    assertArrayLengthPanic(5, sealedArray);
}

function createByteDefaultSealedArray() {
    byte[5] sealedArray = [0, 0, 0, 0, 0];
    isEqualPanic("[0,0,0,0,0]", sealedArray);
    assertArrayLengthPanic(5, sealedArray);
}

// Tuple Arrays

function createTupleSealedArray() {
    [int, boolean][3] sealedArray = [[2, true], [3, false], [6, true]];
    sealedArray[2] = [5, false];
    assertArrayLengthPanic(3, sealedArray);
}

function createTupleAutoFilledSealedArray() {
    [int, boolean][3] sealedArray = [[2, true]];
    sealedArray[2] = [5, false];
    assertArrayLengthPanic(3, sealedArray);
}

function createTupleSealedArrayWithLabel() {
    [int, boolean][*] sealedArray = [[2, true], [3, false], [6, true]];
    assertArrayLengthPanic(3, sealedArray);
}

// Function Params and Returns

function functionParametersAndReturns() {
    boolean[3] sealedArray = [true, false, false];
    boolean[3] returnedBoolArray = [false, false, false];
    string[2] returnedStrArray = ["", ""];
    [returnedBoolArray, returnedStrArray] = mockFunction(sealedArray);

    assertArrayLengthPanic(3, returnedBoolArray);
    assertArrayLengthPanic(2, returnedStrArray);
}

function functionParametersAndReturnsAutoFilling() {
    boolean[3] sealedArray = [];
    boolean[3] returnedBoolArray = [];
    string[2] returnedStrArray = [""];
    [returnedBoolArray, returnedStrArray] = mockFunction(sealedArray);

    assertArrayLengthPanic(3, returnedBoolArray);
    assertArrayLengthPanic(2, returnedStrArray);
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

function assignedAutoFilledArrayInvalidIndexAccess() {
    int[3] x1 = [1, 2];
    int[] x2 = x1;
    x2[4] = 10;
}

// Union arrays

type myUnion 1 | 2 | 3 | 4 | 0;
function createUnionAutoFillArray() {
    myUnion[3] unionArray = [];
    unionArray[2] = 4;
    assertArrayValuePanic(0, unionArray, 0);
    assertArrayValuePanic(0, unionArray, 1);
    assertArrayValuePanic(4, unionArray, 2);
    assertArrayLengthPanic(3, unionArray);
}

type myFloatUnion 1.2 | 3.5 | 0.0 | 3.4;
function createUnionFloatAutoFillArray() {
    myFloatUnion[2] unionArray = [];
    unionArray[1] = 3.5;
    assertArrayValuePanic(0.0, unionArray, 0);
    assertArrayValuePanic(3.5, unionArray, 1);
    assertArrayLengthPanic(2, unionArray);
}

type myBooleanUnion true | false;
function createUnionBooleanAutoFillArray() {
    myBooleanUnion[2] unionArray = [];
    unionArray[1] = true;
    assertArrayValuePanic(false, unionArray, 0);
    assertArrayValuePanic(true, unionArray, 1);
    assertArrayLengthPanic(2, unionArray);
}

type myNullableUnion 1 | 2 | 3 | 4 | ();
function createNullableUnionAutoFillArray() {
    myNullableUnion[3] unionArray = [];
    unionArray[2] = 4;
    assertArrayValuePanic((), unionArray, 0);
    assertArrayValuePanic((), unionArray, 1);
    assertArrayValuePanic(4, unionArray, 2);
    assertArrayLengthPanic(3, unionArray);
}

// TODO: test code hit points
type multiTypeUnion 1 | "hi" | 4.0 | ();
function createNullableNonHomogeneousUnionAutoFillArray() {
    multiTypeUnion[3] unionArray = [];
    unionArray[2] = 4.0;
    assertArrayValuePanic((), unionArray, 0);
    assertArrayValuePanic((), unionArray, 1);
    assertArrayValuePanic(4, unionArray, 2);
    assertArrayLengthPanic(3, unionArray);
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

function createJSONSealedArray() {
    json[5] sealedArray = [false, "abc", "12", -12, "."];
    assertArrayLengthPanic(5, sealedArray);
}

function createJSONSealedArrayWithLabel() {
    json[*] sealedArray = [false, "abc", "12", -12, "."];
    assertArrayLengthPanic(5, sealedArray);
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

function createJSONDefaultSealedArray() {
    json[5] sealedArray = [(), (), (), (), ()];
    assertArrayLengthPanic(5, sealedArray);
}

function createJSONAutoFilledSealedArray() {
    json[5] sealedArray = [(), ()];
    assertEqualPanic("", sealedArray[4].toString());
    assertArrayLengthPanic(5, sealedArray);
}

function testSealedArrayConstrainedMap(int[3] x1, int[] x2) returns int {
    map<int[]> x = {};
    x["v1"] = x1;
    x["v2"] = x2;
    int[] arr = <int[]>x["v1"];
    return arr[2];
}

function testSealedArrayConstrainedMapInvalidIndex(int[3] x1, int index) {
    map<int[]> x = {};
    x["v1"] = x1;
    x["v1"][index] = 4;
}

// xml arrays

function createXMLAutoFilledSealedArray() {
    xml a = xml `<name>Ballerina</name>`;
    xml[5] sealedArray = [a, a];
    sealedArray[3] = a;
    assertEqualPanic("", sealedArray[4].toString());
    assertArrayLengthPanic(5, sealedArray);
}

// const literal broad type filler value

const FOO = 0;
type Bar FOO | 1;
function createConstLiteralAutoFilledSealedArray() {
    Bar a = 1;
    Bar[5] sealedArray = [a, a];
    sealedArray[3] = a;
    assertArrayValuePanic(1, sealedArray, 0);
    assertArrayValuePanic(1, sealedArray, 1);
    assertArrayValuePanic(0, sealedArray, 2);
    assertArrayValuePanic(1, sealedArray, 3);
    assertArrayLengthPanic(5, sealedArray);
}

function testArrayWithConstantSizeReferenceFill() {
    int[constLength] sealedArray = [1];
    sealedArray[1] = 2;
    assertArrayLengthPanic(2, sealedArray);
    assertArrayValuePanic(2, sealedArray, 1);
}

// helper methods

function assertArrayLengthPanic(int expected, any[] arr, string message = "Array length did not match") {
    int actual = arr.length();
    if (expected != actual) {
        panic error(message + " Expected : " + expected.toString() + " Actual : " + actual.toString());
    }
}

function assertArrayValuePanic(anydata expected, anydata[] arr, int index, string message = "Array value mismatch") {
    anydata actual = arr[index];
    if (expected != actual) {
        panic error(message + " Expected : " + expected.toString() + " Actual : " + actual.toString());
    }
}

function assertEqualPanic(anydata expected, anydata actual, string message = "Value mismatch") {
    if (expected != actual) {
        panic error(message + " Expected : " + expected.toString() + " Actual : " + actual.toString());
    }
}

function isEqualPanic(string expected, any[] arr, string message = "Not equal") {
    string actual = arr.toString().trim();
    if (expected != actual) {
        panic error(message + " Expected : " + expected + " Actual : " + actual);
    }
}

function assertTruePanic(boolean actual, string message = "Not equal") {
    if (true != actual) {
        panic error(message + " Expected : true" + " Actual : " + actual.toString());
    }
}
