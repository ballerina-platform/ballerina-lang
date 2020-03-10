// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ballerina/lang.'int as ints;

function testValueAssignment() {
    ints:Signed32 a1 = 2147483647;
    ints:Signed32 a2 = -2147483648;
    ints:Signed16 b1 = 32767;
    ints:Signed16 b2 = -32768;
    ints:Signed8 c1 = 127;
    ints:Signed8 c2 = -128;
    ints:Unsigned32 d1 = 4294967295;
    ints:Unsigned32 d2 = 0;
    ints:Unsigned16 e1 = 65535;
    ints:Unsigned16 e2 = 0;
    ints:Unsigned8 f1 = 255;
    ints:Unsigned8 f2 = 0;
    byte g1 = 255;
    byte g2 = 0;

    assertValueEqual(2147483647, a1);
    assertValueEqual(-2147483648, a2);
    assertValueEqual(32767, b1);
    assertValueEqual(-32768, b2);
    assertValueEqual(127, c1);
    assertValueEqual(-128, c2);
    assertValueEqual(4294967295, d1);
    assertValueEqual(0, d2);
    assertValueEqual(65535, e1);
    assertValueEqual(0, e2);
    assertValueEqual(255, f1);
    assertValueEqual(0, f2);
    assertValueEqual(255, g1);
    assertValueEqual(0, g2);
}

function testSigned32Assignment() {
    ints:Signed32 value = 2147483647;
    int a = value;
    assertValueEqual(2147483647, a);
}

function testSigned16Assignment() {
    ints:Signed16 value = 32767;
    int a = value;
    ints:Signed32 b = value;
    assertValueEqual(32767, a);
    assertValueEqual(32767, b);
}

function testSigned8Assignment() {
    ints:Signed8 value = 127;
    int a = value;
    ints:Signed32 b = value;
    ints:Signed16 c = value;
    assertValueEqual(127, a);
    assertValueEqual(127, b);
}

function testUnsigned32Assignment() {
    ints:Unsigned32 value = 4294967295;
    int a = value;
    assertValueEqual(4294967295, a);
}

function testUnsigned16Assignment() {
    ints:Unsigned16 value = 32767;
    int a = value;
    ints:Signed32 b = value;
    ints:Unsigned32 c = value;
    assertValueEqual(32767, a);
    assertValueEqual(32767, b);
    assertValueEqual(32767, c);
}

function testUnsigned8Assignment() {
    ints:Unsigned8 value = 255;
    int a = value;
    ints:Signed32 b = value;
    ints:Signed16 c = value;
    ints:Unsigned32 d = value;
    ints:Unsigned16 e = value;
    byte f = value;
    assertValueEqual(255, a);
    assertValueEqual(255, b);
    assertValueEqual(255, c);
    assertValueEqual(255, d);
    assertValueEqual(255, e);
    assertValueEqual(255, f);
}

type NewInt ints:Signed32;

function testTypeAlias() {
    NewInt value = 2147483647;
    int a = value;
    ints:Signed32 b = value;
    assertValueEqual(2147483647, a);
    assertValueEqual(2147483647, b);
}

function testMathsOperators(){
    ints:Signed32 x = 100;
    ints:Signed16 y = -50;
    int a = x + y;
    int b = x - y;
    int c = x * y;
    int d = x / y;
    int e = x % y;
    assertValueEqual(50, a);
    assertValueEqual(150, b);
    assertValueEqual(-5000, c);
    assertValueEqual(-2, d);
    assertValueEqual(0, e);
}

function testTypeCastingWithInt() {

    // Testing int
    int i1 = 2147483648;
    int i2 = -2147483649;
    int i3 = 100;

    assertError(trap <ints:Signed32> i1);
    assertError(trap <ints:Signed32> i2);
    assertNotError(trap <ints:Signed32> i3);

    assertError(trap <ints:Signed16> i1);
    assertError(trap <ints:Signed16> i2);
    assertNotError(trap <ints:Signed16> i3);

    assertError(trap <ints:Signed8> i1);
    assertError(trap <ints:Signed8> i2);
    assertNotError(trap <ints:Signed8> i3);

    int i4 = 4294967500;

    assertNotError(trap <ints:Unsigned32> i1);
    assertError(trap <ints:Unsigned32> i2);
    assertNotError(trap <ints:Unsigned32> i3);
    assertError(trap <ints:Unsigned32> i4);

    assertError(trap <ints:Unsigned16> i1);
    assertError(trap <ints:Unsigned16> i2);
    assertNotError(trap <ints:Signed16> i3);

    assertError(trap <ints:Unsigned8> i1);
    assertError(trap <ints:Unsigned8> i2);
    assertNotError(trap <ints:Signed8> i3);

}

function testTypeCastingWith32() {

    ints:Signed32 a1 = 2147483647;
    ints:Signed32 a2 = -2147483648;
    ints:Unsigned32 a3 = 4294967295;
    ints:Unsigned32 a4 = 0;

    int ia1 = a1;
    json ja1 = a1;
    anydata ada1 = a1;
    any aa1 = a1;
    int ia3 = a3;
    json ja3 = a3;
    anydata ada3 = a3;
    any aa3 = a3;

    assertNotError(trap <ints:Signed32> ia1);
    assertNotError(trap <ints:Signed32> ja1);
    assertNotError(trap <ints:Signed32> ada1);
    assertNotError(trap <ints:Signed32> aa1);
    assertNotError(trap <ints:Unsigned32> ia3);
    assertNotError(trap <ints:Unsigned32> ja3);
    assertNotError(trap <ints:Unsigned32> ada3);
    assertNotError(trap <ints:Unsigned32> aa3);

    assertNotError(trap <ints:Unsigned32> a1);
    assertError(trap <ints:Unsigned32> a2);
    assertError(trap <ints:Signed32> a3);
    assertNotError(trap <ints:Signed32> a4);

    assertError(trap <ints:Signed16> a1);
    assertError(trap <ints:Signed16> a2);
    assertNotError(trap <ints:Signed16> a4);
    assertError(trap <ints:Unsigned16> a1);
    assertError(trap <ints:Unsigned16> a2);
    assertNotError(trap <ints:Signed16> a4);

    assertError(trap <ints:Signed8> a1);
    assertError(trap <ints:Signed8> a2);
    assertNotError(trap <ints:Signed8> a4);
    assertError(trap <ints:Unsigned8> a1);
    assertError(trap <ints:Unsigned8> a2);
    assertNotError(trap <ints:Signed8> a4);


}

function testTypeCastingWith16() {

    ints:Signed16 b1 = 32767;
    ints:Signed16 b2 = -32768;
    ints:Unsigned16 b3 = 65535;
    ints:Unsigned16 b4 = 0;

    int ib1 = b1;
    json jb1 = b1;
    anydata adb1 = b1;
    any ab1 = b1;
    int ib3 = b3;
    json jb3 = b3;
    anydata adb3 = b3;
    any ab3 = b3;

    assertNotError(trap <ints:Signed16> ib1);
    assertNotError(trap <ints:Signed16> jb1);
    assertNotError(trap <ints:Signed16> adb1);
    assertNotError(trap <ints:Signed16> ab1);
    assertNotError(trap <ints:Unsigned16> ib3);
    assertNotError(trap <ints:Unsigned16> jb3);
    assertNotError(trap <ints:Unsigned16> adb3);
    assertNotError(trap <ints:Unsigned16> ab3);

    assertNotError(trap <ints:Unsigned16> b1);
    assertError(trap <ints:Unsigned16> b2);
    assertError(trap <ints:Signed16> b3);
    assertNotError(trap <ints:Signed16> b4);

    assertError(trap <ints:Signed8> b1);
    assertError(trap <ints:Signed8> b2);
    assertNotError(trap <ints:Signed8> b4);
    assertError(trap <ints:Unsigned8> b1);
    assertError(trap <ints:Unsigned8> b2);
    assertNotError(trap <ints:Unsigned8> b4);

}

function testTypeCastingWith8() {

    ints:Signed8 c1 = 127;
    ints:Signed8 c2 = -128;
    ints:Unsigned8 c3 = 255;
    ints:Unsigned8 c4 = 0;

    int ic1 = c1;
    json jc1 = c1;
    anydata adc1 = c1;
    any ac1 = c1;
    int ic3 = c3;
    json jc3 = c3;
    anydata adc3 = c3;
    any ac3 = c3;

    assertNotError(trap <ints:Signed8> ic1);
    assertNotError(trap <ints:Signed8> jc1);
    assertNotError(trap <ints:Signed8> adc1);
    assertNotError(trap <ints:Signed8> ac1);
    assertNotError(trap <ints:Unsigned8> ic3);
    assertNotError(trap <ints:Unsigned8> jc3);
    assertNotError(trap <ints:Unsigned8> adc3);
    assertNotError(trap <ints:Unsigned8> ac3);

    assertNotError(trap <ints:Unsigned8> c1);
    assertError(trap <ints:Unsigned8> c2);
    assertError(trap <ints:Signed8> c3);
    assertNotError(trap <ints:Signed8> c4);

    byte d1 = 255;
    byte d2 = 0;
    assertNotError(trap <ints:Unsigned8> d1);
    assertNotError(trap <ints:Unsigned8> d2);
}

function testTypeCastingWithFloat() {

    float f1 = 2147483648.1234;
    float f2 = -2147483649.1234;
    float f3 = 100.1;

    assertError(trap <ints:Signed32> f1);
    assertError(trap <ints:Signed32> f2);
    assertNotError(trap <ints:Signed32> f3);

    assertError(trap <ints:Signed16> f1);
    assertError(trap <ints:Signed16> f2);
    assertNotError(trap <ints:Signed16> f3);

    assertError(trap <ints:Signed8> f1);
    assertError(trap <ints:Signed8> f2);
    assertNotError(trap <ints:Signed8> f3);

    float f4 = 4294967500.2345;

    assertNotError(trap <ints:Unsigned32> f1);
    assertError(trap <ints:Unsigned32> f2);
    assertNotError(trap <ints:Unsigned32> f3);
    assertError(trap <ints:Unsigned32> f4);

    assertError(trap <ints:Unsigned16> f1);
    assertError(trap <ints:Unsigned16> f2);
    assertNotError(trap <ints:Signed16> f3);

    assertError(trap <ints:Unsigned8> f1);
    assertError(trap <ints:Unsigned8> f2);
    assertNotError(trap <ints:Signed8> f3);
}

function testTypeCastingWithDecimal() {

    decimal d1 = 2147483648.1234;
    decimal d2 = -2147483649.1234;
    decimal d3 = 100.1;

    assertError(trap <ints:Signed32> d1);
    assertError(trap <ints:Signed32> d2);
    assertNotError(trap <ints:Signed32> d3);

    assertError(trap <ints:Signed16> d1);
    assertError(trap <ints:Signed16> d2);
    assertNotError(trap <ints:Signed16> d3);

    assertError(trap <ints:Signed8> d1);
    assertError(trap <ints:Signed8> d2);
    assertNotError(trap <ints:Signed8> d3);

    decimal d4 = 4294967500.2345;

    assertNotError(trap <ints:Unsigned32> d1);
    assertError(trap <ints:Unsigned32> d2);
    assertNotError(trap <ints:Unsigned32> d3);
    assertError(trap <ints:Unsigned32> d4);

    assertError(trap <ints:Unsigned16> d1);
    assertError(trap <ints:Unsigned16> d2);
    assertNotError(trap <ints:Signed16> d3);

    assertError(trap <ints:Unsigned8> d1);
    assertError(trap <ints:Unsigned8> d2);
    assertNotError(trap <ints:Signed8> d3);
}

function testTypeTest() {

    // Testing int
    int i1 = 2147483648;
    int i2 = -2147483649;
    int i3 = 100;

    assertFalse(i1 is ints:Signed32);
    assertFalse(i2 is ints:Signed32);
    assertTrue(i3 is ints:Signed32);

    assertFalse(i1 is ints:Signed16);
    assertFalse(i2 is ints:Signed16);
    assertTrue(i3 is ints:Signed16);

    assertFalse(i1 is ints:Signed8);
    assertFalse(i2 is ints:Signed8);
    assertTrue(i3 is ints:Signed8);

    int i4 = 4294967500;

    assertTrue(i1 is ints:Unsigned32);
    assertFalse(i2 is ints:Unsigned32);
    assertTrue(i3 is ints:Unsigned32);
    assertFalse(i4 is ints:Unsigned32);

    assertFalse(i1 is ints:Unsigned16);
    assertFalse(i2 is ints:Unsigned16);
    assertTrue(i3 is ints:Signed16);

    assertFalse(i1 is ints:Unsigned8);
    assertFalse(i2 is ints:Unsigned8);
    assertTrue(i3 is ints:Signed8);

}

function testList() {

    ints:Signed32[] a1 = [1, 2, 3, 4, 5];
    ints:Signed32 t1 = a1[0];
    assertValueEqual(1, t1);
    a1[6] = 7;
    assertValueEqual([1,2,3,4,5,0,7], a1);

    any t2 = a1;
    assertTrue(t2 is ints:Signed32[]);

    int[] t3 = a1;
    assertError(trap insertListValue(t3, 0, 4294967295));

    int[] a2 = [1, 2, 0, -1, -2];
    ints:Signed32[] b2 = <ints:Signed32[]> a1;
    assertError(trap <ints:Unsigned32[]> a2);

    [ints:Signed32, ints:Unsigned8, int] a3 = [-100, 10,  4294967295];
    int[] t4 = a3;
    assertError(trap insertListValue(t4, 1, -1));
}

function insertListValue(int[] list, int pos, int value) {
    list[pos] = value;
}

function testMapping() {

    map<ints:Signed8> m1 = {};
    m1["k1"] = 10;
    int? t0 = m1["k1"];
    assertValueEqual(10, t0);
    map<ints:Signed16> t1 = m1;
    map<int> t2 = m1;

    assertError(trap <map<ints:Unsigned32>> t2);
    assertError(trap insertMapValue(t1, "k2", 200));
    assertError(trap insertMapValue(t2, "k2", 200));

    record {
        ints:Signed8 i;
        ints:Unsigned8 k;
    } rec = { i : -10, k : 10};
    rec.i = 11;
    assertValueEqual(11, rec.i);

    record { int i; int k;} t3 = rec;
    assertError(trap updateRecord(t3, 200));
}

const ints:Signed32 ca1 = 10;
const ints:Signed16 cb1 = 10;
const ints:Signed8 cc1 = 10;
const ints:Unsigned32 cd1 = 10;
const ints:Unsigned16 ce1 = 10;
const ints:Unsigned8 cf1 = 10;
const byte cg1 = 10;

function testConstReference() {
    ints:Unsigned8 a = ca1;
    ints:Unsigned8 b = cb1;
    ints:Unsigned8 c = cc1;
    ints:Unsigned8 d = cd1;
    ints:Unsigned8 e = ce1;
    ints:Unsigned8 f = cf1;
    ints:Unsigned8 g = cg1;
}

function insertMapValue(map<int> m, string key, int value) {
    m[key] = value;
}

function updateRecord(record { int i; int k;} rec, int value) {
    rec.i = value;
}

// Test Functions

function assertValueEqual(anydata expected, anydata actual) {
    if(expected != actual) {
        error e = error("Not Equal", message = "expected: " + expected.toString() + ", found: " + actual.toString());
        panic e;
    }
}

function assertError(anydata|error value) {
    if !(value is error) {
        error e = error("Not Error", message = "expected: Error, found: " + value.toString());
        panic e;
    }
}

function assertNotError(anydata|error value) {
    if (value is error) {
        error e = error("Not Error", message = "expected: Error, found: " + value.toString());
        panic e;
    }
}

function assertTrue(boolean value) {
    if !(value) {
        error e = error("Not True", message = "expected: true, found: " + value.toString());
        panic e;
    }
}

function assertFalse(boolean value) {
    if (value) {
        error e = error("Not False", message = "expected: false, found: " + value.toString());
        panic e;
    }
}
