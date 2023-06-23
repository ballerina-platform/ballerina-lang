// Copyright (c) 2023 WSO2 LLC. (http://www.wso2.com).
//
// WSO2 LLC. licenses this file to you under the Apache License,
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

const int[2] ARR_11 = [3, 1];
const int[] ARR_12 = [3, 2];
const int[*] ARR_13 = [3, 2, 1];

function testIntArrayAsExpectedType() {
    assertTrue(ARR_11.length() == 2);
    assertEquals(ARR_11.toString(), "[3,1]");

    assertTrue(ARR_12.length() == 2);
    assertEquals(ARR_12.toString(), "[3,2]");

    assertTrue(ARR_13.length() == 3);
    assertEquals(ARR_13.toString(), "[3,2,1]");
}

const int:Signed16[] ARR_21 = [3, 1];
const int:Signed16[3] ARR_22 = [3, -2, 1];
const int:Signed16[*] ARR_23 = [3, -2, 1];
const int:Signed16[2][2] ARR_24 = [[3, -2], [1, 0]];
const int:Signed16[][2] ARR_25 = [[3, -2], [1, 0]];
const int:Signed16[*][2] ARR_26 = [[3, -2], [1, 0]];
const int:Signed32[3] ARR_27 = [3, -2, 1];
const int:Signed8[*] ARR_28 = [3, -2, 1];
const int:Unsigned8[] ARR_29 = [3, 2, 1];
const int:Unsigned16[3] ARR_30 = [3, 2, 1];
const int:Unsigned32[*] ARR_31 = [3, 2, 1];

function testSubTypeOfIntArrayAsExpectedType() {
    assertTrue(ARR_21.length() == 2);
    assertEquals(ARR_21.toString(), "[3,1]");

    assertTrue(ARR_22.length() == 3);
    assertEquals(ARR_22.toString(), "[3,-2,1]");

    assertTrue(ARR_23.length() == 3);
    assertEquals(ARR_23.toString(), "[3,-2,1]");

    assertTrue(ARR_24.length() == 2);
    assertEquals(ARR_24.toString(), "[[3,-2],[1,0]]");

    assertTrue(ARR_25.length() == 2);
    assertEquals(ARR_25.toString(), "[[3,-2],[1,0]]");

    assertTrue(ARR_26.length() == 2);
    assertEquals(ARR_26.toString(), "[[3,-2],[1,0]]");

    assertTrue(ARR_27.length() == 3);
    assertEquals(ARR_27.toString(), "[3,-2,1]");

    assertTrue(ARR_28.length() == 3);
    assertEquals(ARR_28.toString(), "[3,-2,1]");

    assertTrue(ARR_29.length() == 3);
    assertEquals(ARR_29.toString(), "[3,2,1]");

    assertTrue(ARR_30.length() == 3);
    assertEquals(ARR_30.toString(), "[3,2,1]");

    assertTrue(ARR_31.length() == 3);
    assertEquals(ARR_31.toString(), "[3,2,1]");
}

const byte[] ARR_32 = [3, 1];
const byte[3] ARR_33 = [3, 2, 1];
const byte[*] ARR_34 = [3, 2, 1];

function testByteArrayAsExpectedType() {
    assertTrue(ARR_32.length() == 2);
    assertEquals(ARR_32.toString(), "[3,1]");

    assertTrue(ARR_33.length() == 3);
    assertEquals(ARR_33.toString(), "[3,2,1]");

    assertTrue(ARR_34.length() == 3);
    assertEquals(ARR_34.toString(), "[3,2,1]");
}

const float[] ARR_35 = [3, 2.0];
const float[3] ARR_36 = [3, 2.0, 1];
const float[*] ARR_37 = [3, 2, 1f];

function testFloatArrayAsExpectedType() {
    assertTrue(ARR_35.length() == 2);
    assertEquals(ARR_35.toString(), "[3.0,2.0]");

    assertTrue(ARR_36.length() == 3);
    assertEquals(ARR_36.toString(), "[3.0,2.0,1.0]");

    assertTrue(ARR_37.length() == 3);
    assertEquals(ARR_37.toString(), "[3.0,2.0,1.0]");
}

const decimal[] ARR_38 = [3, 2.0];
const decimal[3] ARR_39 = [3, 2.0, 1d];
const decimal[*] ARR_40 = [3, 2, 1];

function testDecimalAsExpectedType() {
    assertTrue(ARR_38.length() == 2);
    assertEquals(ARR_38.toString(), "[3,2.0]");

    assertTrue(ARR_39.length() == 3);
    assertEquals(ARR_39.toString(), "[3,2.0,1]");

    assertTrue(ARR_40.length() == 3);
    assertEquals(ARR_40.toString(), "[3,2,1]");
}

const string[] ARR_41 = ["3", "2.0"];

function testStringArrayAsExpectedType() {
    assertTrue(ARR_41.length() == 2);
    assertEquals(ARR_41.toString(), "[\"3\",\"2.0\"]");
}

const int[2] ARR_42 = [3, 1];
const int[6] ARR_43 = [0x2, 1, ...ARR_42, 1, 1];

function testListConstructorExprWithSpreadOpExpr() {
    assertTrue(ARR_42.length() == 2);
    assertEquals(ARR_42.toString(), "[3,1]");

    assertTrue(ARR_43.length() == 6);
    assertEquals(ARR_43.toString(), "[2,1,3,1,1,1]");
}

const int[10] ARR_44 = [0x2, 1, ...ARR_42, 1, 1];
const int[10][2] ARR_45 = [[]];

function testListConstructorExprWithIntFillMembers() {
    assertTrue(ARR_44.length() == 10);
    assertEquals(ARR_44.toString(), "[2,1,3,1,1,1,0,0,0,0]");

    assertTrue(ARR_45.length() == 10);
    assertEquals(ARR_45.toString(), "[[0,0],[0,0],[0,0],[0,0],[0,0],[0,0],[0,0],[0,0],[0,0],[0,0]]");
}

const string[3][2] ARR_46 = [[]];
const string[3] ARR_47 = ["a"];

function testListConstructorExprWithStringFillMembers() {
    assertTrue(ARR_46.length() == 3);
    assertEquals(ARR_46.toString(), "[[\"\",\"\"],[\"\",\"\"],[\"\",\"\"]]");

    assertTrue(ARR_47.length() == 3);
    assertEquals(ARR_47.toString(), "[\"a\",\"\",\"\"]");
}

const [string, int] TUPLE_11 = ["ABC", 1];
const [string, int, string, int, int, float] TUPLE_12 = ["ABC", 1, ...TUPLE_11, 1, 1];
const [string, int, string, int, string, int, int, float, int, int] TUPLE_13 = ["ABC", 1, ...TUPLE_12, 1, 1];
const [record {| int a; 2 b;|}, string, int] TUPLE_14 = [{a: 1, b: 2}, ...TUPLE_11];
const [[record {| int a; 2 b;|}], string, int] TUPLE_15 = [[{a: 1, b: 2}], ...TUPLE_11];
const [int...] TUPLE_151 = [1, 2, 4];

function testTupleAsExpectedType() {
    assertTrue(TUPLE_11.length() == 2);
    assertEquals(TUPLE_11.toString(), "[\"ABC\",1]");

    assertTrue(TUPLE_12.length() == 6);
    assertEquals(TUPLE_12.toString(), "[\"ABC\",1,\"ABC\",1,1,1.0]");

    assertTrue(TUPLE_13.length() == 10);
    assertEquals(TUPLE_13.toString(), "[\"ABC\",1,\"ABC\",1,\"ABC\",1,1,1.0,1,1]");

    assertTrue(TUPLE_14.length() == 3);
    assertEquals(TUPLE_14.toString(), "[{\"a\":1,\"b\":2},\"ABC\",1]");

    assertTrue(TUPLE_15.length() == 3);
    assertEquals(TUPLE_15.toString(), "[[{\"a\":1,\"b\":2}],\"ABC\",1]");

    assertTrue(TUPLE_151.length() == 3);
    assertEquals(TUPLE_151.toString(), "[1,2,4]");
}

const [()] TUPLE_16 = [];
const [boolean] TUPLE_17 = [];
const [int] TUPLE_18 = [];
const [float] TUPLE_19 = [];
const [decimal] TUPLE_20 = [];
const [string] TUPLE_21 = [];
const [int[]] TUPLE_22 = [];
const [[int]] TUPLE_23 = [];
const [record {}] TUPLE_24 = [];
const [map<int>] TUPLE_25 = [];
const [3] TUPLE_26 = [];
const [3, 2] TUPLE_27 = [];
const [byte] TUPLE_28 = [];
const [any] TUPLE_29 = [];
const [anydata] TUPLE_30 = [];
const [int?] TUPLE_31 = [];
const [(), boolean, int, float, decimal, string, int[], [int], record {}, map<int>, any, anydata] TUPLE_32 = [];

function testTupleAsExpectedTypeWithFillMembers() {
    assertTrue(TUPLE_16.length() == 1);
    assertEquals(TUPLE_16.toString(), "[null]");

    assertTrue(TUPLE_17.length() == 1);
    assertEquals(TUPLE_17.toString(), "[false]");

    assertTrue(TUPLE_18.length() == 1);
    assertEquals(TUPLE_18.toString(), "[0]");

    assertTrue(TUPLE_19.length() == 1);
    assertEquals(TUPLE_19.toString(), "[0.0]");

    assertTrue(TUPLE_20.length() == 1);
    assertEquals(TUPLE_20.toString(), "[0]");

    assertTrue(TUPLE_21.length() == 1);
    assertEquals(TUPLE_21.toString(), "[\"\"]");

    assertTrue(TUPLE_22.length() == 1);
    assertEquals(TUPLE_22.toString(), "[[]]");

    assertTrue(TUPLE_23.length() == 1);
    assertEquals(TUPLE_23.toString(), "[[0]]");

    assertTrue(TUPLE_24.length() == 1);
    assertEquals(TUPLE_24.toString(), "[{}]");

    assertTrue(TUPLE_25.length() == 1);
    assertEquals(TUPLE_25.toString(), "[{}]");

    assertTrue(TUPLE_26.length() == 1);
    assertEquals(TUPLE_26.toString(), "[3]");

    assertTrue(TUPLE_27.length() == 2);
    assertEquals(TUPLE_27.toString(), "[3,2]");

    assertTrue(TUPLE_28.length() == 1);
    assertEquals(TUPLE_28.toString(), "[0]");

    assertTrue(TUPLE_29.length() == 1);
    assertEquals(TUPLE_29.toString(), "[null]");

    assertTrue(TUPLE_30.length() == 1);
    assertEquals(TUPLE_30.toString(), "[null]");

    assertTrue(TUPLE_31.length() == 1);
    assertEquals(TUPLE_31.toString(), "[null]");

    assertTrue(TUPLE_32.length() == 12);
    assertEquals(TUPLE_32.toString(), "[null,false,0,0.0,0,\"\",[],[0],{},{},null,null]");
}

const TUPLE_33 = ["ABC", 1];
const TUPLE_34 = ["ABC", 1, ...TUPLE_33, 1, 1];
const TUPLE_35 = ["ABC", 1, ...TUPLE_34, 1, 1];
const TUPLE_36 = [{a: 1, b: 2}, ...TUPLE_33];
const TUPLE_37 = [[{a: 1, b: 2}], ...TUPLE_33];

function testListConstExprWithoutExpectedType() {
    assertTrue(TUPLE_33.length() == 2);
    assertEquals(TUPLE_33.toString(), "[\"ABC\",1]");

    assertTrue(TUPLE_34.length() == 6);
    assertEquals(TUPLE_34.toString(), "[\"ABC\",1,\"ABC\",1,1,1]");

    assertTrue(TUPLE_35.length() == 10);
    assertEquals(TUPLE_35.toString(), "[\"ABC\",1,\"ABC\",1,\"ABC\",1,1,1,1,1]");

    assertTrue(TUPLE_36.length() == 3);
    assertEquals(TUPLE_36.toString(), "[{\"a\":1,\"b\":2},\"ABC\",1]");

    assertTrue(TUPLE_37.length() == 3);
    assertEquals(TUPLE_37.toString(), "[[{\"a\":1,\"b\":2}],\"ABC\",1]");
}

const TUPLE_40 = [3, 1];
const TUPLE_40 TUPLE_41 = [3, 1];

function testTupleWithConstAsExpectedType() {
    assertTrue(TUPLE_40.length() == 2);
    assertEquals(TUPLE_40.toString(), "[3,1]");

    assertTrue(TUPLE_41.length() == 2);
    assertEquals(TUPLE_41.toString(), "[3,1]");
}

const float[]|decimal[] ARR_50 = [1, 2f];
const int[]|decimal[] ARR_51 = [1, 2d];

function testListConstantWithUnionAsExpectedType() {
    assertTrue(ARR_50.length() == 2);
    assertTrue(ARR_50[0] is float);
    assertTrue(ARR_50[1] is float);
    assertEquals(ARR_50.toString(), "[1.0,2.0]");

    assertTrue(ARR_51.length() == 2);
    assertTrue(ARR_51[0] is decimal);
    assertTrue(ARR_51[1] is decimal);
    assertEquals(ARR_51.toString(), "[1,2]");
}

type A int;
type B A;
const [A, B] TUPLE_50 = [];

function testListConstExprWithTypeRefFillMember() {
    assertTrue(TUPLE_50.length() == 2);
    assertEquals(TUPLE_50.toString(), "[0,0]");
}

function assertTrue(anydata actual) {
    assertEquals(true, actual);
}

function assertEquals(anydata actual, anydata expected) {
    if expected == actual {
        return;
    }

    panic error(string `expected ${expected.toBalString()}, found ${actual.toBalString()}`);
}
