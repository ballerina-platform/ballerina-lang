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

    utils:FooRecord a6 = { fooFieldOne: "test string 1" };
    utils:FooRecord a7 = { fooFieldOne: "test string 2" };
    utils:FooRecord a8 = { fooFieldOne: "test string 3" };
    utils:FooRecord a9 = { fooFieldOne: "test string 4" };
    utils:FooRecord a10 = { fooFieldOne: "test string 5" };
    utils:FooRecord a11 = { fooFieldOne: "test string 6" };
    utils:FooRecord[] fooRecordArray = [a6, a7, a8, a9];

    test:assertEquals(fooRecordArray[0], a6, msg = "expected value not found at index 0");
    test:assertEquals(fooRecordArray[1], a7, msg = "expected value not found at index 1");
    test:assertEquals(fooRecordArray[2], a8, msg = "expected value not found at index 2");
    test:assertEquals(fooRecordArray[3], a9, msg = "expected value not found at index 3");

    fooRecordArray[4] = a10;
    test:assertEquals(fooRecordArray[0], a6, msg = "expected value not found at index 0");
    test:assertEquals(fooRecordArray[1], a7, msg = "expected value not found at index 1");
    test:assertEquals(fooRecordArray[2], a8, msg = "expected value not found at index 2");
    test:assertEquals(fooRecordArray[3], a9, msg = "expected value not found at index 3");
    test:assertEquals(fooRecordArray[4], a10, msg = "expected value not found at index 4");

    fooRecordArray[1] = a11;
    test:assertEquals(fooRecordArray[0], a6, msg = "expected value not found at index 0");
    test:assertEquals(fooRecordArray[1], a11, msg = "expected value not found at index 1");
    test:assertEquals(fooRecordArray[2], a8, msg = "expected value not found at index 2");
    test:assertEquals(fooRecordArray[3], a9, msg = "expected value not found at index 3");
    test:assertEquals(fooRecordArray[4], a10, msg = "expected value not found at index 4");

    utils:FooObject a12 = new("test string 1");
    utils:FooObject a13 = new("test string 2");
    utils:FooObject a14 = new("test string 3");
    utils:FooObject a15 = new("test string 4");
    utils:FooObject[] fooObjectArray = [a12, a13];

    test:assertEquals(fooObjectArray[0], a12, msg = "expected value not found at index 0");
    test:assertEquals(fooObjectArray[1], a13, msg = "expected value not found at index 1");

    fooObjectArray[2] = a14;
    test:assertEquals(fooObjectArray[0], a12, msg = "expected value not found at index 0");
    test:assertEquals(fooObjectArray[1], a13, msg = "expected value not found at index 1");
    test:assertEquals(fooObjectArray[2], a14, msg = "expected value not found at index 1");

    fooObjectArray[0] = a15;
    test:assertEquals(fooObjectArray[0], a15, msg = "expected value not found at index 0");
    test:assertEquals(fooObjectArray[1], a13, msg = "expected value not found at index 1");
    test:assertEquals(fooObjectArray[2], a14, msg = "expected value not found at index 1");
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

    utils:FooRecord a7 = { fooFieldOne: "test string 0" };
    utils:FooRecord a8 = { fooFieldOne: "test string 1" };
    utils:FooRecord a9 = { fooFieldOne: "test string 2" };
    utils:FooRecord a10 = { fooFieldOne: "test string 3" };
    utils:FooRecord[] fooRecordArray = [a7, a8, a9];

    utils:FooRecord b7 = fooRecordArray[0];
    utils:FooRecord b8 = fooRecordArray[1];
    utils:FooRecord b9 = fooRecordArray[2];

    test:assertEquals(b7, a7, msg = "expected value not found at index 0");
    test:assertEquals(b8, a8, msg = "expected value not found at index 1");
    test:assertEquals(b9, a9, msg = "expected value not found at index 2");

    fooRecordArray[0] = a10;
    utils:FooRecord b10 = fooRecordArray[0];
    test:assertEquals(b10, a10, msg = "expected value not found at index 0");

    utils:FooObject a11 = new("test string 1");
    utils:FooObject a12 = new("test string 2");
    utils:FooObject a13 = new("test string 3");
    utils:FooObject[] fooObjectArray = [a11, a12];

    utils:FooObject b11 = fooObjectArray[0];
    utils:FooObject b12 = fooObjectArray[1];
    test:assertEquals(b11, a11, msg = "expected value not found at index 0");
    test:assertEquals(b12, a12, msg = "expected value not found at index 1");

    fooObjectArray[1] = a13;
    utils:FooObject b13 = fooObjectArray[1];
    test:assertEquals(b13, a13, msg = "expected value not found at index 1");
}

@test:Config {}
function testArrayMemberReferenceByInvalidIntegerIndex() {
    float[] floatArray = [1.1, 0.0, 2.20];

    int index = -1;
    utils:assertErrorReason(trap floatArray[index], "{ballerina}IndexOutOfRange",
        "invalid reason on access by negative index");

    index = floatArray.length();
    utils:assertErrorReason(trap floatArray[index], "{ballerina}IndexOutOfRange",
        "invalid reason on access by index == array length");

    index = floatArray.length() + 3;
    utils:assertErrorReason(trap floatArray[index], "{ballerina}IndexOutOfRange",
        "invalid reason on access by index > array length");

    utils:BarRecord[] barRecordArray = [<utils:BarRecord>{ barFieldOne: 1 }, <utils:BarRecord>{ barFieldOne: 2 }];
    index = -1;
    utils:assertErrorReason(trap barRecordArray[index], "{ballerina}IndexOutOfRange",
        "invalid reason on access by negative index");

    index = barRecordArray.length();
    utils:assertErrorReason(trap barRecordArray[index], "{ballerina}IndexOutOfRange",
        "invalid reason on access by index == array length");

    index = barRecordArray.length();
    utils:assertErrorReason(trap barRecordArray[index], "{ballerina}IndexOutOfRange",
        "invalid reason on access by index > array length");

    utils:BarObject b1 = new(1);
    utils:BarObject b2 = new(2);
    utils:BarObject b3 = new(3);
    utils:BarObject[] barObjectArray = [b1, b2, b3];
    index = -1;
    utils:assertErrorReason(trap barObjectArray[index], "{ballerina}IndexOutOfRange",
        "invalid reason on access by negative index");

    index = barObjectArray.length();
    utils:assertErrorReason(trap barObjectArray[index], "{ballerina}IndexOutOfRange",
        "invalid reason on access by index == array length");

    index = barObjectArray.length();
    utils:assertErrorReason(trap barObjectArray[index], "{ballerina}IndexOutOfRange",
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

    foreach string|int value in array {
        test:assertEquals(value, arrayTwo[currentIndex],
            msg = "incorrect member value found on iteration");
        currentIndex = currentIndex + 1;
    }

    currentIndex = 0;
    utils:FooRecord e = { fooFieldOne: "test string 1" };
    utils:BarRecord f = { barFieldOne: 1 };
    (utils:FooRecord|utils:BarRecord)[] arrayThree = [e, f];
    (utils:FooRecord|utils:BarRecord)[] arrayFour = [e, f];

    foreach utils:FooRecord|utils:BarRecord value in arrayThree {
        test:assertEquals(value, arrayFour[currentIndex],
            msg = "incorrect member value found on iteration");
        currentIndex = currentIndex + 1;
    }

    currentIndex = 0;
    utils:FooObject g = new("test string 1");
    utils:BarObject h = new(1);
    utils:BarObject i = new(1);
    (utils:FooObject|utils:BarObject)[] arrayFive = [g, h, i];
    (utils:FooObject|utils:BarObject)[] arraySix = [g, h, i];

    foreach utils:FooObject|utils:BarObject value in arrayFive {
        test:assertEquals(value, arraySix[currentIndex],
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
    utils:assertErrorReason(trap utils:insertMemberToArray(anyArray, intArray.length() - 1, "not an int"),
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

    // `stringOrIntMap` looks like `map<string>`
    map<string|int> stringOrIntMap = {
        one: "test string 1",
        two: "test string 2"
    };
    utils:assertErrorReason(trap utils:insertMemberToArray(anyArray, 0, stringOrIntMap),
                      "{ballerina}InherentTypeViolation", 
                      "invalid reason on inherent type violating array insertion");

    utils:FooRecord[] fooRecordArray = [<utils:FooRecord>{ fooFieldOne: "test string 1" }];
    anyArray = fooRecordArray;
    utils:assertErrorReason(trap utils:insertMemberToArray(anyArray, intArray.length() - 1, <utils:BarRecord> { barFieldOne: 1 }),
                      "{ballerina}InherentTypeViolation", 
                      "invalid reason on inherent type violating array insertion");

    map<utils:FooRecord>[] fooRecordMapArray = [
        {
            one: <utils:FooRecord>{ fooFieldOne: "test string 1" }
        },
        {
            two: <utils:FooRecord>{ fooFieldOne: "test string 2" },
            three: <utils:FooRecord>{ fooFieldOne: "test string 3" }
        }
    ];
    anyArray = fooRecordMapArray;

    // `fooRecordOrBarRecordMap` looks like `map<utils:FooRecord>`
    map<utils:FooRecord|utils:BarRecord> fooRecordOrBarRecordMap = {
        one: <utils:FooRecord>{ fooFieldOne: "test string 1" }
    };
    utils:assertErrorReason(trap utils:insertMemberToArray(anyArray, 0, fooRecordOrBarRecordMap),
                      "{ballerina}InherentTypeViolation", 
                      "invalid reason on inherent type violating array insertion");

    utils:FooObject f1 = new("test string 1");
    utils:FooObject f2 = new("test string 2");
    utils:FooObject f3 = new("test string 3");
    utils:FooObject[] fooObjectArray = [f1, f2, f3];
    anyArray = fooObjectArray;

    utils:BarObject b1 = new(1);
    utils:assertErrorReason(trap utils:insertMemberToArray(anyArray, 0, b1),
                      "{ballerina}InherentTypeViolation", 
                      "invalid reason on inherent type violating array insertion");

    map<utils:FooObject>[] fooObjectMapArray = [
        {
            one:f1,
            two: f2
        },
        {
            three: f3
        }
    ];
    anyArray = fooObjectMapArray;

    // `fooRecordOrBarObjectMap` looks like `map<utils:FooObject>`
    map<utils:FooObject|utils:BarObject> fooRecordOrBarObjectMap = {
        one: f1
    };
    utils:assertErrorReason(trap utils:insertMemberToArray(anyArray, 0, fooRecordOrBarObjectMap),
                      "{ballerina}InherentTypeViolation", 
                      "invalid reason on inherent type violating array insertion");
}

// array-type-descriptor := member-type-descriptor [ [ array-length ] ]
// member-type-descriptor := type-descriptor
// array-length := int-literal | constant-reference-expr | implied-array-length
// implied-array-length := ! ...
@test:Config {}
function testArrayTypeDescriptor() {
    int[] array1 = [];
    int[] expectedArray = [];
    test:assertEquals(array1, expectedArray, msg = "expected fixed length array and implied length array to be equal");

    // An array length of !... means that the length of the array is to be implied from the
    // context; this is allowed in the same contexts where var would be allowed in place of the
    // type descriptor in which array-length occurs (see “Typed binding patterns”); its meaning
    // is the same as if the length was specified explicitly.
    string[3] array2 = ["a", "b", "c"];
    string[!...] array3 = ["a", "b", "c"];
    any tempVar = array3;
    test:assertTrue(tempVar is string[3], msg = "expected fixed length array and implied length array to be equal");
    test:assertEquals(array2, array3, msg = "expected fixed length array and implied length array to be equal");
}

// When array-length is present, the implicit initial value of the array type is a list of the
// specified length, with each member of the array having its implicit initial value; otherwise, the
// implicit initial value of an array type is an array of length 0.
// When a list value has an inherent type that is an array without an array-length, then
// attempting to write a member of a list at an index i that is greater than or equal to the current
// length of the list will first increase the length of the list to i + 1, with the newly added
// members of the array having the implicit initial value of T
@test:Config {}
function testInitialImplicitValueOfArrays() {
    // tested in 05-values-types-variables#testImplicitInitialValues
}

// An array T[] is iterable as a sequence of values of type T.
@test:Config {}
function testArrayIterable() {
    // tested in 05-values-types-variables#testIterableTypes
}

// Note also that T[n] is a subtype of T[], and that if S is a subtype of T, then S[] is a
// subtype of T[]; this is a consequence of the definition of subtyping in terms of subset
// inclusion of the corresponding sets of shapes.
@test:Config {}
function testArraySubType() {
    int[5] fixedArray = [1, 2, 3, 4, 5];
    any tempArray = fixedArray;
    test:assertTrue(tempArray is int[], msg = "expected fixed length array to be subtype of growing array");

    utils:BazRecordTwo bRecordTwo = { bazFieldOne: 12.0, bazFieldTwo: "fieldTwo" };
    any tempRecord = bRecordTwo;
    test:assertTrue(tempRecord is utils:BazRecord, msg = "expected BazRecordTwo to be subtype of BazRecord");

    utils:BazRecordTwo[] bRecordTwoArray = [bRecordTwo];
    any tempRecordArray = bRecordTwoArray;
    test:assertTrue(tempRecordArray is utils:BazRecord[],
        msg = "expected BazRecordTwo[] to be subtype of BazRecord[]");
}
