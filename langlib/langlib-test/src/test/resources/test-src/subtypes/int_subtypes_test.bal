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
import ballerina/lang.test as test;

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

    test:assertValueEqual(2147483647, a1);
    test:assertValueEqual(-2147483648, a2);
    test:assertValueEqual(32767, b1);
    test:assertValueEqual(-32768, b2);
    test:assertValueEqual(127, c1);
    test:assertValueEqual(-128, c2);
    test:assertValueEqual(4294967295, d1);
    test:assertValueEqual(0, d2);
    test:assertValueEqual(65535, e1);
    test:assertValueEqual(0, e2);
    test:assertValueEqual(255, f1);
    test:assertValueEqual(0, f2);
    test:assertValueEqual(255, g1);
    test:assertValueEqual(0, g2);
}

function testSigned32Assignment() {
    ints:Signed32 value = 2147483647;
    int a = value;
    test:assertValueEqual(2147483647, a);
}

function testSigned16Assignment() {
    ints:Signed16 value = 32767;
    int a = value;
    ints:Signed32 b = value;
    test:assertValueEqual(32767, a);
    test:assertValueEqual(32767, b);
}

function testSigned8Assignment() {
    ints:Signed8 value = 127;
    int a = value;
    ints:Signed32 b = value;
    ints:Signed16 c = value;
    test:assertValueEqual(127, a);
    test:assertValueEqual(127, b);
}

function testUnsigned32Assignment() {
    ints:Unsigned32 value = 4294967295;
    int a = value;
    test:assertValueEqual(4294967295, a);
}

function testUnsigned16Assignment() {
    ints:Unsigned16 value = 32767;
    int a = value;
    ints:Signed32 b = value;
    ints:Unsigned32 c = value;
    test:assertValueEqual(32767, a);
    test:assertValueEqual(32767, b);
    test:assertValueEqual(32767, c);
}

function testUnsigned8Assignment() {
    ints:Unsigned8 value = 255;
    int a = value;
    ints:Signed32 b = value;
    ints:Signed16 c = value;
    ints:Unsigned32 d = value;
    ints:Unsigned16 e = value;
    byte f = value;
    test:assertValueEqual(255, a);
    test:assertValueEqual(255, b);
    test:assertValueEqual(255, c);
    test:assertValueEqual(255, d);
    test:assertValueEqual(255, e);
    test:assertValueEqual(255, f);
}

type NewInt ints:Signed32;

function testTypeAlias() {
    NewInt value = 2147483647;
    int a = value;
    ints:Signed32 b = value;
    test:assertValueEqual(2147483647, a);
    test:assertValueEqual(2147483647, b);
}

function testMathsOperators(){
    ints:Signed32 x = 100;
    ints:Signed16 y = -50;
    int a = x + y;
    int b = x - y;
    int c = x * y;
    int d = x / y;
    int e = x % y;
    test:assertValueEqual(50, a);
    test:assertValueEqual(150, b);
    test:assertValueEqual(-5000, c);
    test:assertValueEqual(-2, d);
    test:assertValueEqual(0, e);

    int val = x.sum(y);
    test:assertValueEqual(50, val);
}

function testTypeCastingWithInt() {

    // Testing int
    int i1 = 2147483648;
    int i2 = -2147483649;
    int i3 = 100;

    test:assertError(trap <ints:Signed32> i1);
    test:assertError(trap <ints:Signed32> i2);
    test:assertNotError(trap <ints:Signed32> i3);

    test:assertError(trap <ints:Signed16> i1);
    test:assertError(trap <ints:Signed16> i2);
    test:assertNotError(trap <ints:Signed16> i3);

    test:assertError(trap <ints:Signed8> i1);
    test:assertError(trap <ints:Signed8> i2);
    test:assertNotError(trap <ints:Signed8> i3);

    int i4 = 4294967500;

    test:assertNotError(trap <ints:Unsigned32> i1);
    test:assertError(trap <ints:Unsigned32> i2);
    test:assertNotError(trap <ints:Unsigned32> i3);
    test:assertError(trap <ints:Unsigned32> i4);

    test:assertError(trap <ints:Unsigned16> i1);
    test:assertError(trap <ints:Unsigned16> i2);
    test:assertNotError(trap <ints:Signed16> i3);

    test:assertError(trap <ints:Unsigned8> i1);
    test:assertError(trap <ints:Unsigned8> i2);
    test:assertNotError(trap <ints:Signed8> i3);

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

    test:assertNotError(trap <ints:Signed32> ia1);
    test:assertNotError(trap <ints:Signed32> ja1);
    test:assertNotError(trap <ints:Signed32> ada1);
    test:assertNotError(trap <ints:Signed32> aa1);
    test:assertNotError(trap <ints:Unsigned32> ia3);
    test:assertNotError(trap <ints:Unsigned32> ja3);
    test:assertNotError(trap <ints:Unsigned32> ada3);
    test:assertNotError(trap <ints:Unsigned32> aa3);

    test:assertNotError(trap <ints:Unsigned32> a1);
    test:assertError(trap <ints:Unsigned32> a2);
    test:assertError(trap <ints:Signed32> a3);
    test:assertNotError(trap <ints:Signed32> a4);

    test:assertError(trap <ints:Signed16> a1);
    test:assertError(trap <ints:Signed16> a2);
    test:assertNotError(trap <ints:Signed16> a4);
    test:assertError(trap <ints:Unsigned16> a1);
    test:assertError(trap <ints:Unsigned16> a2);
    test:assertNotError(trap <ints:Signed16> a4);

    test:assertError(trap <ints:Signed8> a1);
    test:assertError(trap <ints:Signed8> a2);
    test:assertNotError(trap <ints:Signed8> a4);
    test:assertError(trap <ints:Unsigned8> a1);
    test:assertError(trap <ints:Unsigned8> a2);
    test:assertNotError(trap <ints:Signed8> a4);


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

    test:assertNotError(trap <ints:Signed16> ib1);
    test:assertNotError(trap <ints:Signed16> jb1);
    test:assertNotError(trap <ints:Signed16> adb1);
    test:assertNotError(trap <ints:Signed16> ab1);
    test:assertNotError(trap <ints:Unsigned16> ib3);
    test:assertNotError(trap <ints:Unsigned16> jb3);
    test:assertNotError(trap <ints:Unsigned16> adb3);
    test:assertNotError(trap <ints:Unsigned16> ab3);

    test:assertNotError(trap <ints:Unsigned16> b1);
    test:assertError(trap <ints:Unsigned16> b2);
    test:assertError(trap <ints:Signed16> b3);
    test:assertNotError(trap <ints:Signed16> b4);

    test:assertError(trap <ints:Signed8> b1);
    test:assertError(trap <ints:Signed8> b2);
    test:assertNotError(trap <ints:Signed8> b4);
    test:assertError(trap <ints:Unsigned8> b1);
    test:assertError(trap <ints:Unsigned8> b2);
    test:assertNotError(trap <ints:Unsigned8> b4);

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

    test:assertNotError(trap <ints:Signed8> ic1);
    test:assertNotError(trap <ints:Signed8> jc1);
    test:assertNotError(trap <ints:Signed8> adc1);
    test:assertNotError(trap <ints:Signed8> ac1);
    test:assertNotError(trap <ints:Unsigned8> ic3);
    test:assertNotError(trap <ints:Unsigned8> jc3);
    test:assertNotError(trap <ints:Unsigned8> adc3);
    test:assertNotError(trap <ints:Unsigned8> ac3);

    test:assertNotError(trap <ints:Unsigned8> c1);
    test:assertError(trap <ints:Unsigned8> c2);
    test:assertError(trap <ints:Signed8> c3);
    test:assertNotError(trap <ints:Signed8> c4);

    byte d1 = 255;
    byte d2 = 0;
    test:assertNotError(trap <ints:Unsigned8> d1);
    test:assertNotError(trap <ints:Unsigned8> d2);
}

function testTypeCastingWithFloat() {

    float f1 = 2147483648.1234;
    float f2 = -2147483649.1234;
    float f3 = 100.1;

    test:assertError(trap <ints:Signed32> f1);
    test:assertError(trap <ints:Signed32> f2);
    test:assertNotError(trap <ints:Signed32> f3);

    test:assertError(trap <ints:Signed16> f1);
    test:assertError(trap <ints:Signed16> f2);
    test:assertNotError(trap <ints:Signed16> f3);

    test:assertError(trap <ints:Signed8> f1);
    test:assertError(trap <ints:Signed8> f2);
    test:assertNotError(trap <ints:Signed8> f3);

    float f4 = 4294967500.2345;

    test:assertNotError(trap <ints:Unsigned32> f1);
    test:assertError(trap <ints:Unsigned32> f2);
    test:assertNotError(trap <ints:Unsigned32> f3);
    test:assertError(trap <ints:Unsigned32> f4);

    test:assertError(trap <ints:Unsigned16> f1);
    test:assertError(trap <ints:Unsigned16> f2);
    test:assertNotError(trap <ints:Signed16> f3);

    test:assertError(trap <ints:Unsigned8> f1);
    test:assertError(trap <ints:Unsigned8> f2);
    test:assertNotError(trap <ints:Signed8> f3);
}

function testTypeCastingWithDecimal() {

    decimal d1 = 2147483648.1234;
    decimal d2 = -2147483649.1234;
    decimal d3 = 100.1;

    test:assertError(trap <ints:Signed32> d1);
    test:assertError(trap <ints:Signed32> d2);
    test:assertNotError(trap <ints:Signed32> d3);

    test:assertError(trap <ints:Signed16> d1);
    test:assertError(trap <ints:Signed16> d2);
    test:assertNotError(trap <ints:Signed16> d3);

    test:assertError(trap <ints:Signed8> d1);
    test:assertError(trap <ints:Signed8> d2);
    test:assertNotError(trap <ints:Signed8> d3);

    decimal d4 = 4294967500.2345;

    test:assertNotError(trap <ints:Unsigned32> d1);
    test:assertError(trap <ints:Unsigned32> d2);
    test:assertNotError(trap <ints:Unsigned32> d3);
    test:assertError(trap <ints:Unsigned32> d4);

    test:assertError(trap <ints:Unsigned16> d1);
    test:assertError(trap <ints:Unsigned16> d2);
    test:assertNotError(trap <ints:Signed16> d3);

    test:assertError(trap <ints:Unsigned8> d1);
    test:assertError(trap <ints:Unsigned8> d2);
    test:assertNotError(trap <ints:Signed8> d3);
}

function testTypeTest() {

    // Testing int
    int i1 = 2147483648;
    int i2 = -2147483649;
    int i3 = 100;

    test:assertFalse(i1 is ints:Signed32);
    test:assertFalse(i2 is ints:Signed32);
    test:assertTrue(i3 is ints:Signed32);

    test:assertFalse(i1 is ints:Signed16);
    test:assertFalse(i2 is ints:Signed16);
    test:assertTrue(i3 is ints:Signed16);

    test:assertFalse(i1 is ints:Signed8);
    test:assertFalse(i2 is ints:Signed8);
    test:assertTrue(i3 is ints:Signed8);

    int i4 = 4294967500;

    test:assertTrue(i1 is ints:Unsigned32);
    test:assertFalse(i2 is ints:Unsigned32);
    test:assertTrue(i3 is ints:Unsigned32);
    test:assertFalse(i4 is ints:Unsigned32);

    test:assertFalse(i1 is ints:Unsigned16);
    test:assertFalse(i2 is ints:Unsigned16);
    test:assertTrue(i3 is ints:Signed16);

    test:assertFalse(i1 is ints:Unsigned8);
    test:assertFalse(i2 is ints:Unsigned8);
    test:assertTrue(i3 is ints:Signed8);

}

function testList() {

    ints:Signed32[] a1 = [1, 2, 3, 4, 5];
    ints:Signed32 t1 = a1[0];
    test:assertValueEqual(1, t1);
    a1[6] = 7;
    test:assertValueEqual([1,2,3,4,5,0,7], a1);

    any t2 = a1;
    test:assertTrue(t2 is ints:Signed32[]);

    int[] t3 = a1;
    test:assertError(trap insertListValue(t3, 0, 4294967295));

    int[] a2 = [1, 2, 0, -1, -2];
    ints:Signed32[] b2 = <ints:Signed32[]> a1;
    test:assertError(trap <ints:Unsigned32[]> a2);

    [ints:Signed32, ints:Unsigned8, int] a3 = [-100, 10,  4294967295];
    int[] t4 = a3;
    test:assertError(trap insertListValue(t4, 1, -1));
}

function insertListValue(int[] list, int pos, int value) {
    list[pos] = value;
}

function testMapping() {

    map<ints:Signed8> m1 = {};
    m1["k1"] = 10;
    int? t0 = m1["k1"];
    test:assertValueEqual(10, t0);
    map<ints:Signed16> t1 = m1;
    map<int> t2 = m1;

    test:assertError(trap <map<ints:Unsigned32>> t2);
    test:assertError(trap insertMapValue(t1, "k2", 200));
    test:assertError(trap insertMapValue(t2, "k2", 200));

    record {
        ints:Signed8 i;
        ints:Unsigned8 k;
    } rec = { i : -10, k : 10};
    rec.i = 11;
    test:assertValueEqual(11, rec.i);

    record { int i; int k;} t3 = rec;
    test:assertError(trap updateRecord(t3, 200));
}
// TODO : Fix this, Issue : #21542
//const ints:Signed32 ca1 = 10;
//const ints:Signed16 cb1 = 10;
//const ints:Signed8 cc1 = 10;
//const ints:Unsigned32 cd1 = 10;
//const ints:Unsigned16 ce1 = 10;
//const ints:Unsigned8 cf1 = 10;
const byte cg1 = 10;

// TODO : Fix this, Issue : #21542
//function testConstReference() {
//    ints:Unsigned8 a = ca1;
//    ints:Unsigned8 b = cb1;
//    ints:Unsigned8 c = cc1;
//    ints:Unsigned8 d = cd1;
//    ints:Unsigned8 e = ce1;
//    ints:Unsigned8 f = cf1;
//    ints:Unsigned8 g = cg1;
//}

function insertMapValue(map<int> m, string key, int value) {
    m[key] = value;
}

function updateRecord(record { int i; int k;} rec, int value) {
    rec.i = value;
}

function testLeftShift() {
    'testSigned8LeftShift();
    'testSigned16LeftShift();
    'testSigned32LeftShift();
    testIntLeftShift();
    testByteLeftShift();
    'testUnsigned8LeftShift();
    'testUnsigned16LeftShift();
    'testUnsigned32LeftShift();
}

function 'testSigned8LeftShift() {
    ints:Signed8 i1 = 127;
    int j = 2;
    int k = i1 << j;
    test:assertValueEqual(508, k);

    ints:Signed8 i2 = -128;
    k = i2 << j;
    test:assertValueEqual(-512, k);

    ints:Signed8 i3 = 0;
    k = i3 << j;
    test:assertValueEqual(0, k);

    ints:Signed8 i4 = 1;
    k = i4 << j;
    test:assertValueEqual(4, k);
}

function 'testSigned16LeftShift() {
    ints:Signed16 i1 = 32767;
    int j = 2;
    int k = i1 << j;
    test:assertValueEqual(131068, k);

    ints:Signed16 i2 = -32768;
    k = i2 << j;
    test:assertValueEqual(-131072, k);

    ints:Signed16 i3 = 0;
    k = i3 << j;
    test:assertValueEqual(0, k);

    ints:Signed16 i4 = 1;
    k = i4 << j;
    test:assertValueEqual(4, k);
}

function 'testSigned32LeftShift() {
    ints:Signed32 i1 = 2147483647;
    int j = 2;
    int k = i1 << j;
    test:assertValueEqual(8589934588, k);

    ints:Signed32 i2 = -2147483648;
    k = i2 << j;
    test:assertValueEqual(-8589934592, k);

    ints:Signed32 i3 = 0;
    k = i3 << j;
    test:assertValueEqual(0, k);

    ints:Signed32 i4 = 1;
    k = i4 << j;
    test:assertValueEqual(4, k);
}

function testIntLeftShift() {
    int i1 = 9223372036854775807;
    int j = 2;
    int k = i1 << j;
    test:assertValueEqual(-4, k);

    int i2 = -9223372036854775808;
    k = i2 << j;
    test:assertValueEqual(0, k);

    int i3 = 0;
    k = i3 << j;
    test:assertValueEqual(0, k);

    int i4 = 1;
    k = i4 << j;
    test:assertValueEqual(4, k);
}

function testByteLeftShift() {
    byte i1 = 255;
    int j = 2;
    int k = i1 << j;
    test:assertValueEqual(1020, k);

    byte i2 = 0;
    k = i2 << j;
    test:assertValueEqual(0, k);

    byte i3 = 1;
    k = i3 << j;
    test:assertValueEqual(4, k);
}

function 'testUnsigned8LeftShift() {
    ints:Unsigned8 i1 = 255;
    int j = 2;
    int k = i1 << j;
    test:assertValueEqual(1020, k);

    ints:Unsigned8 i2 = 0;
    k = i2 << j;
    test:assertValueEqual(0, k);

    ints:Unsigned8 i3 = 1;
    k = i3 << j;
    test:assertValueEqual(4, k);
}

function 'testUnsigned16LeftShift() {
    ints:Unsigned16 i1 = 65535;
    int j = 2;
    int k = i1 << j;
    test:assertValueEqual(262140, k);

    ints:Unsigned16 i2 = 0;
    k = i2 << j;
    test:assertValueEqual(0, k);

    ints:Unsigned16 i3 = 1;
    k = i3 << j;
    test:assertValueEqual(4, k);
}

function 'testUnsigned32LeftShift() {
    ints:Unsigned32 i1 = 4294967295;
    int j = 2;
    int k = i1 << j;
    test:assertValueEqual(17179869180, k);

    ints:Unsigned32 i2 = 0;
    k = i2 << j;
    test:assertValueEqual(0, k);

    ints:Unsigned32 i3 = 1;
    k = i3 << j;
    test:assertValueEqual(4, k);
}

function testRightShift() {
    'testSigned8RightShift();
    'testSigned16RightShift();
    'testSigned32RightShift();
    testIntRightShift();
    testByteRightShift();
    'testUnsigned8RightShift();
    'testUnsigned16RightShift();
    'testUnsigned32RightShift();
}

function 'testSigned8RightShift() {
    ints:Signed8 i1 = 127;
    int j = 3;
    int k = i1 >> j;
    test:assertValueEqual(15, k);

    ints:Signed8 i2 = -128;
    k = i2 >> j;
    test:assertValueEqual(-16, k);

    ints:Signed8 i3 = 0;
    k = i3 >> j;
    test:assertValueEqual(0, k);

    ints:Signed8 i4 = 8;
    k = i4 >> j;
    test:assertValueEqual(1, k);
}

function 'testSigned16RightShift() {
    ints:Signed16 i1 = 32767;
    int j = 3;
    int k = i1 >> j;
    test:assertValueEqual(4095, k);

    ints:Signed16 i2 = -32768;
    k = i2 >> j;
    test:assertValueEqual(-4096, k);

    ints:Signed16 i3 = 0;
    k = i3 >> j;
    test:assertValueEqual(0, k);

    ints:Signed16 i4 = 8;
    k = i4 >> j;
    test:assertValueEqual(1, k);
}

function 'testSigned32RightShift() {
    ints:Signed32 i1 = 2147483647;
    int j = 3;
    int k = i1 >> j;
    test:assertValueEqual(268435455, k);

    ints:Signed32 i2 = -2147483648;
    k = i2 >> j;
    test:assertValueEqual(-268435456, k);

    ints:Signed32 i3 = 0;
    k = i3 >> j;
    test:assertValueEqual(0, k);

    ints:Signed32 i4 = 8;
    k = i4 >> j;
    test:assertValueEqual(1, k);
}

function testIntRightShift() {
    int i1 = 9223372036854775807;
    int j = 3;
    int k = i1 >> j;
    test:assertValueEqual(1152921504606846975, k);

    int i2 = -9223372036854775808;
    k = i2 >> j;
    test:assertValueEqual(-1152921504606846976, k);

    int i3 = 0;
    k = i3 >> j;
    test:assertValueEqual(0, k);

    int i4 = 8;
    k = i4 >> j;
    test:assertValueEqual(1, k);
}

function testByteRightShift() {
    byte i1 = 255;
    int j = 3;
    byte k = i1 >> j;
    test:assertValueEqual(31, k);

    byte i2 = 0;
    k = i2 >> j;
    test:assertValueEqual(0, k);

    byte i3 = 8;
    k = i3 >> j;
    test:assertValueEqual(1, k);
}

function 'testUnsigned8RightShift() {
    ints:Unsigned8 i1 = 255;
    int j = 3;
    ints:Unsigned8 k = i1 >> j;
    test:assertValueEqual(31, k);

    ints:Unsigned8 i2 = 0;
    k = i2 >> j;
    test:assertValueEqual(0, k);

    ints:Unsigned8 i3 = 8;
    k = i3 >> j;
    test:assertValueEqual(1, k);
}

function 'testUnsigned16RightShift() {
    ints:Unsigned16 i1 = 65535;
    int j = 3;
    ints:Unsigned16 k = i1 >> j;
    test:assertValueEqual(8191, k);

    ints:Unsigned16 i2 = 0;
    k = i2 >> j;
    test:assertValueEqual(0, k);

    ints:Unsigned16 i3 = 8;
    k = i3 >> j;
    test:assertValueEqual(1, k);
}

function 'testUnsigned32RightShift() {
    ints:Unsigned32 i1 = 4294967295;
    int j = 3;
    ints:Unsigned32 k = i1 >> j;
    test:assertValueEqual(536870911, k);

    ints:Unsigned32 i2 = 0;
    k = i2 >> j;
    test:assertValueEqual(0, k);

    ints:Unsigned32 i3 = 8;
    k = i3 >> j;
    test:assertValueEqual(1, k);
}

function testUnsignedRightShift() {
    'testSigned8UnsignedRightShift();
    'testSigned16UnsignedRightShift();
    'testSigned32UnsignedRightShift();
    testIntUnsignedRightShift();
    testByteUnsignedRightShift();
    'testUnsigned8UnsignedRightShift();
    'testUnsigned16UnsignedRightShift();
    'testUnsigned32UnsignedRightShift();
}

function 'testSigned8UnsignedRightShift() {
    ints:Signed8 i1 = 127;
    int j = 3;
    int k = i1 >>> j;
    test:assertValueEqual(15, k);

    ints:Signed8 i2 = -128;
    k = i2 >>> j;
    test:assertValueEqual(2305843009213693936, k);

    ints:Signed8 i3 = 0;
    k = i3 >>> j;
    test:assertValueEqual(0, k);

    ints:Signed8 i4 = 8;
    k = i4 >>> j;
    test:assertValueEqual(1, k);
}

function 'testSigned16UnsignedRightShift() {
    ints:Signed16 i1 = 32767;
    int j = 3;
    int k = i1 >>> j;
    test:assertValueEqual(4095, k);

    ints:Signed16 i2 = -32768;
    k = i2 >>> j;
    test:assertValueEqual(2305843009213689856, k);

    ints:Signed16 i3 = 0;
    k = i3 >>> j;
    test:assertValueEqual(0, k);

    ints:Signed16 i4 = 8;
    k = i4 >>> j;
    test:assertValueEqual(1, k);
}

function 'testSigned32UnsignedRightShift() {
    ints:Signed32 i1 = 2147483647;
    int j = 3;
    int k = i1 >>> j;
    test:assertValueEqual(268435455, k);

    ints:Signed32 i2 = -2147483648;
    k = i2 >>> j;
    test:assertValueEqual(2305843008945258496, k);

    ints:Signed32 i3 = 0;
    k = i3 >>> j;
    test:assertValueEqual(0, k);

    ints:Signed32 i4 = 8;
    k = i4 >>> j;
    test:assertValueEqual(1, k);
}

function testIntUnsignedRightShift() {
    int i1 = 9223372036854775807;
    int j = 3;
    int k = i1 >>> j;
    test:assertValueEqual(1152921504606846975, k);

    int i2 = -9223372036854775808;
    k = i2 >>> j;
    test:assertValueEqual(1152921504606846976, k);

    int i3 = 0;
    k = i3 >>> j;
    test:assertValueEqual(0, k);

    int i4 = 8;
    k = i4 >>> j;
    test:assertValueEqual(1, k);
}

function testByteUnsignedRightShift() {
    byte i1 = 255;
    int j = 3;
    byte k = i1 >>> j;
    test:assertValueEqual(31, k);

    byte i2 = 0;
    k = i2 >>> j;
    test:assertValueEqual(0, k);

    byte i3 = 8;
    k = i3 >>> j;
    test:assertValueEqual(1, k);
}

function 'testUnsigned8UnsignedRightShift() {
    ints:Unsigned8 i1 = 255;
    int j = 3;
    ints:Unsigned8 k = i1 >>> j;
    test:assertValueEqual(31, k);

    ints:Unsigned8 i2 = 0;
    k = i2 >>> j;
    test:assertValueEqual(0, k);

    ints:Unsigned8 i3 = 8;
    k = i3 >>> j;
    test:assertValueEqual(1, k);
}

function 'testUnsigned16UnsignedRightShift() {
    ints:Unsigned16 i1 = 65535;
    int j = 3;
    ints:Unsigned16 k = i1 >>> j;
    test:assertValueEqual(8191, k);

    ints:Unsigned16 i2 = 0;
    k = i2 >>> j;
    test:assertValueEqual(0, k);

    ints:Unsigned16 i3 = 8;
    k = i3 >>> j;
    test:assertValueEqual(1, k);
}

function 'testUnsigned32UnsignedRightShift() {
    ints:Unsigned32 i1 = 4294967295;
    int j = 3;
    ints:Unsigned32 k = i1 >>> j;
    test:assertValueEqual(536870911, k);

    ints:Unsigned32 i2 = 0;
    k = i2 >>> j;
    test:assertValueEqual(0, k);

    ints:Unsigned32 i3 = 8;
    k = i3 >>> j;
    test:assertValueEqual(1, k);
}

function testBitwiseAnd() {
    ints:Signed8 a = 1;
    ints:Signed16 b = -1567;
    ints:Signed32 c = 139058;
    int d = 6429485;

    byte e = 23;
    ints:Unsigned8 f = 12;
    ints:Unsigned16 g = 2345;
    ints:Unsigned32 h = 5739412;

    ints:Signed8 i = -100;
    ints:Signed16 j = 31267;
    ints:Signed32 k = -2137483647;
    int l = -9223372036854775808;

    ints:Signed8 s81 = -1;
    int res = s81 & s81;
    test:assertValueEqual(-1, res);

    byte v1 = 128;
    ints:Signed16 v2 = 384;
    byte res2 = v1 & v2;
    test:assertValueEqual(128, res2);

    int n = c & i;
    test:assertValueEqual(139024, n);

    int o = j & b;
    test:assertValueEqual(30753, o);

    int p = k & a;
    test:assertValueEqual(1, p);

    int q = b & l;
    test:assertValueEqual(-9223372036854775808, q);

    int r = a & 1;
    test:assertValueEqual(1, r);

    int s = b & c;
    test:assertValueEqual(137504, s);

    int t = d & 2;
    test:assertValueEqual(0, t);

    byte u = e & e;
    test:assertValueEqual(23, u);

    ints:Unsigned8 v = f & d;
    test:assertValueEqual(12, v);

    ints:Unsigned16 w = g & h;
    test:assertValueEqual(256, w);

    ints:Unsigned8 x = h & f;
    test:assertValueEqual(4, x);

    ints:Unsigned32 y = a & h;
    test:assertValueEqual(0, y);

    ints:Unsigned8 z = f & b;
    test:assertValueEqual(0, z);
}

function testBitwiseOr() {
    ints:Signed8 a = 1;
    ints:Signed16 b = -1567;
    ints:Signed32 c = 139058;
    int d = 6429485;

    byte e = 23;
    ints:Unsigned8 f = 12;
    ints:Unsigned16 g = 2345;
    ints:Unsigned32 h = 5739412;

    ints:Signed8 i = -100;
    ints:Signed16 j = 31267;
    ints:Signed32 k = -2137483647;
    int l = -9223372036854775808;

    ints:Signed8 s81 = -1;
    int res = s81 | s81;
    test:assertValueEqual(-1, res);

    byte v1 = 128;
    ints:Signed16 v2 = 384;
    int res2 = v1 | v2;
    test:assertValueEqual(384, res2);

    res2 = v2 | v1;
    test:assertValueEqual(384, res2);

    int n = c | i;
    test:assertValueEqual(-66, n);

    int o = j | b;
    test:assertValueEqual(-1053, o);

    int p = k | a;
    test:assertValueEqual(-2137483647, p);

    int q = b | l;
    test:assertValueEqual(-1567, q);

    int r = a | 1;
    test:assertValueEqual(1, r);

    int s = b | c;
    test:assertValueEqual(-13, s);

    int t = d | 2;
    test:assertValueEqual(6429487, t);

    byte u = e | e;
    test:assertValueEqual(23, u);

    int v = f | d;
    test:assertValueEqual(6429485, v);

    ints:Unsigned16 w = g | h;
    test:assertValueEqual(39869, w);

    ints:Unsigned8 x = h | f;
    test:assertValueEqual(156, x);

    int y = a | h;
    test:assertValueEqual(5739413, y);

    int z = f | b;
    test:assertValueEqual(-1555, z);
}

function testBitwiseXor() {
    ints:Signed8 a = 1;
    ints:Signed16 b = -1567;
    ints:Signed32 c = 139058;
    int d = 6429485;

    byte e = 23;
    ints:Unsigned8 f = 12;
    ints:Unsigned16 g = 2345;
    ints:Unsigned32 h = 5739412;

    ints:Signed8 i = -100;
    ints:Signed16 j = 31267;
    ints:Signed32 k = -2137483647;
    int l = -9223372036854775808;

    ints:Signed8 s81 = -1;
    int res = s81 ^ s81;
    test:assertValueEqual(0, res);

    byte v1 = 128;
    ints:Signed16 v2 = 384;
    int res2 = v1 ^ v2;
    test:assertValueEqual(256, res2);

    res2 = v2 ^ v1;
    test:assertValueEqual(256, res2);

    int n = c ^ i;
    test:assertValueEqual(-139090, n);

    int o = j ^ b;
    test:assertValueEqual(-31806, o);

    int p = k ^ a;
    test:assertValueEqual(-2137483648, p);

    int q = b ^ l;
    test:assertValueEqual(9223372036854774241, q);

    int r = a ^ 1;
    test:assertValueEqual(0, r);

    int s = b ^ c;
    test:assertValueEqual(-137517, s);

    int t = d ^ 2;
    test:assertValueEqual(6429487, t);

    byte u = e ^ e;
    test:assertValueEqual(0, u);

    int v = f ^ d;
    test:assertValueEqual(6429473, v);

    ints:Unsigned16 w = g ^ h;
    test:assertValueEqual(39613, w);

    ints:Unsigned8 x = h ^ f;
    test:assertValueEqual(152, x);

    int y = a ^ h;
    test:assertValueEqual(5739413, y);

    int z = f ^ b;
    test:assertValueEqual(-1555, z);
}

