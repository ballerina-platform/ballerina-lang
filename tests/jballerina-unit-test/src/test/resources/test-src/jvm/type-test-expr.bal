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

function testValueTypeInUnion() returns string {
    int|string x = "hello";
    int y = 10;
    if (x is int) {
        return "int";
    } else {
        return "string";
    }
}

function testUnionTypeInUnion() returns string {
    int|string|float x = 5;
    if (x is int|float) {
        return "numeric";
    } else {
        return "string";
    }
}

function testNestedTypeCheck() returns [any, any, any] {
    return [bar(true), bar(1234), bar("hello")];
}

function bar (string | int | boolean i)  returns string {
    if (i is int){
        return "int";
    } else {
        if (i is string) {
            return "string";
        } else {
            return "boolean";
        }
    }
}

function testTypeInAny() returns (string) {
    any a = "This is working";
    if (a is string) {
        return "string value: " + <string> a;
    } else if(a is int) {
        return "int";
    } else {
        return "any";
    }
}

function testNilType() returns (string) {
    any a = ();
    if (a is string) {
        return "string";
    } else if(a is int) {
        return "int";
    } else if(a is ()) {
        return "nil";
    }else {
        return "any";
    }
}

type A1 record {|
    int x;
    anydata...;
|};

type B1 record {|
    int x;
    string y;
    anydata...;
|};

function testSimpleRecordTypes_1() returns string {
    A1 a1 = {x:0};
    any a = a1;
     if (a is A1) {
        return "a is A1";
    } else if (a is B1) {
        return "a is B1";
    }

    return "n/a";
}

function testSimpleRecordTypes_2() returns [boolean, boolean] {
    B1 b = {x:0, y:""};
    any a = b;
    return [a is A1, a is B1];
}

type A2 record {|
    int x;
    anydata...;
|};

type B2 record {|
    int x;
    anydata...;
|};

function testSimpleRecordTypes_3() returns [boolean, boolean] {
    B2 b = {x:0};
    any a = b;
    return [a is A2, a is B2];
}

function testAnyJsonTypes() returns [boolean, boolean, boolean, boolean] {
    int a = 1;
    byte b = 1;
    decimal d = 1;
    float f = 1.0;

    any aa = a;
    any bb = b;
    any dd = d;
    any ff = f;

    return [aa is json, bb is json, dd is json, ff is json];
}

function testIsLikeForTupleWithRestDescriptor() {
    anydata k1 = [1, 2, 3, 4];
    anydata k2 = k1.cloneReadOnly();
    assertEquals(k2 is [int, int, anydata...], true);

    anydata k3 = [1, 2, true, "str"];
    anydata k4 = k3.cloneReadOnly();
    assertEquals(k4 is [int, int, anydata...], true);

    anydata k5 = [1, "str", true, "str"];
    anydata k6 = k5.cloneReadOnly();
    assertEquals(k6 is [int, string, anydata...], true);

    anydata k7 = [1, "str", true, "str"];
    anydata k8 = k7.cloneReadOnly();
    assertEquals(k8 is [int, int, anydata...], false);

    [anydata, anydata] k9 = [1, 2];
    anydata k10 = k9.cloneReadOnly();
    assertEquals(k10 is [int, int, int...], true);

    [int, int, int] k11 = [1, 2, 3];
    anydata k12 = k11.cloneReadOnly();
    assertEquals(k12 is [int, int, int, int, int...], false);
}

function testIsLikeForTupleWithOutRestDescriptor() {
    [anydata, anydata] k1 = [1, 2];
    anydata k2 = k1.cloneReadOnly();
    assertEquals(k2 is [int, int], true);
    assertEquals(k2 is [int, byte], true);
    assertEquals(k2 is [int, string], false);

    [anydata, anydata] k3 = [1, "str"];
    anydata k4 = k3.cloneReadOnly();
    assertEquals(k4 is [int, string], true);
}

function assertEquals(anydata expected, anydata actual) {
    if expected == actual {
        return;
    }

    panic error("expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}
