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

function testCastOfReadonlyIntArrayToByteArray() {
    readonly & int[] a = [1, 255];
    any b = a;

    test:assertTrue(b is (float|byte)[]);
    test:assertFalse(b is (boolean|float)[]);

    byte[] c = <byte[]> b;

    test:assertTrue(c === b);
    test:assertEquals("[1,255]", c.toString());
    test:assertEquals(1, c[0]);
    test:assertEquals(255, c[1]);
}

function testCastOfReadonlyIntArrayToByteArrayNegative() {
    readonly & int[] e = [1, 100, 1000];
    any f = e;
    byte[]|error g = trap <byte[]> f;
    test:assertTrue(g is error);
    error err = <error> g;
    test:assertEquals("{ballerina}TypeCastError", err.message());
    test:assertEquals("incompatible types: 'int[] & readonly' cannot be cast to 'byte[]'", <string> checkpanic err.detail()["message"]);
}

function testCastOfReadonlyAnyToByteArray() {
    readonly & any x = [1, 2, 3];
    any xx = x;
    byte[] y = <byte[]> xx;
    test:assertTrue(xx === y);
    test:assertEquals(1, y[0]);
}

function testCastOfReadonlyArrayToUnion() {
    readonly & int[] a = [1, 255];
    any b = a;
    (float|byte)[] i = <(float|byte)[]> b;
    test:assertTrue(i === b);
    test:assertEquals("[1,255]", i.toString());

    readonly & float[] d = [1, 2.5, 27.5f];
    any e = d;
    (int|float|byte)[] f = <(int|float|byte)[]> e;
    test:assertEquals("[1.0,2.5,27.5]", f.toString());
}

function testCastOfReadonlyUnionArrayToByteArray() {
    readonly & (int|byte)[] a = [1,2,3];
    any b = a;
    byte[] c = <byte[]> b;
    test:assertEquals("[1,2,3]", c.toString());

    readonly & (any|byte)[] d = [1,2,3];
    any e = d;
    byte[] f = <byte[]> e;
    test:assertEquals("[1,2,3]", f.toString());
}

type Foo record {|
    string s;
    int[] arr;
|};

type Bar record {|
    string s;
    byte[] arr;
|};

function testCastOfReadonlyRecord() {
    (Foo & readonly) f = {s: "a", arr: [1,2,3]};
    any a = f;
    Bar b = <Bar> a;
    test:assertTrue(b === a);
    test:assertEquals("{\"s\":\"a\",\"arr\":[1,2,3]}", b.toString());
}

function testCastOfReadonlyRecordNegative() {
    (Foo & readonly) f = {s: "a", arr: [1,2,300]};
    any a = f;
    Bar b = <Bar> a;
}

const FOO = "foo";

function testCastOfReadonlyStringArrayToStringConstantArray() {

    readonly & string[] d = [FOO, FOO];
    any e =  d;
    FOO[] f = <FOO[]> e;

    test:assertTrue(f === e);
    test:assertEquals("[\"foo\",\"foo\"]", f.toString());
}

function testCastOfTwoDimensionalIntArrayToByteArray() {
    (readonly & int[][]) a = [[1,2,3], [4,5,6]];
    any b = a;
    byte[][] c = <byte[][]> b;

    test:assertTrue(c === b);
    test:assertEquals("[[1,2,3],[4,5,6]]", c.toString());
}
