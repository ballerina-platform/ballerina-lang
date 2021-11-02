// Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

const PANIC_ARITHMETIC_OVERFLOW = 1;
const PANIC_DIVIDE_BY_ZERO = 2;
const PANIC_TYPE_CAST = 3;
const PANIC_STACK_OVERFLOW = 4;
const PANIC_INDEX_OUT_OF_BOUNDS = 5;
const PANIC_LIST_TOO_LONG = 6;

type PanicIndex PANIC_ARITHMETIC_OVERFLOW|PANIC_DIVIDE_BY_ZERO|PANIC_TYPE_CAST|PANIC_STACK_OVERFLOW|PANIC_INDEX_OUT_OF_BOUNDS;

const int A = 10 + 2;
const int B = 25;

type ThisOrThat 12|25;

type AorB A|B;

public const byte ONE = 1;
public const byte TWO = 2;
public const byte THREE = 3;

type ThreeNumbers ONE|TWO|THREE;

const NEG_THIRTY_TWO = -32;
const POS_THIRTY_TWO = 32;

type ThirtiesCode NEG_THIRTY_TWO|POS_THIRTY_TWO;

function testBitwiseANDOperation() {
    PanicIndex panicIndex = 5;
    int a1 = panicIndex & 3;
    assertEqual(a1, 1);

    a1 = panicIndex & A;
    assertEqual(a1, 4);

    byte a2 = panicIndex & 0x5;
    assertEqual(a2, 0x5);

    byte a = 12;
    byte a3 = a & panicIndex;
    assertEqual(a3, 0x4);

    int:Signed32|int:Unsigned8 b = 12;
    int a4 = b & panicIndex;
    assertEqual(a4, 0x4);

    ThreeNumbers c = 3;
    int a5 = c & b;
    assertEqual(a5, 0);

    int a6 = b & NEG_THIRTY_TWO;
    assertEqual(a6, 0);

    byte a7 = 5 & a;
    assertEqual(a7, 4);

    int a8 = 5 & 12;
    assertEqual(a8, 4);

    ThirtiesCode d = 32;
    int a9 = d & NEG_THIRTY_TWO;
    assertEqual(a9, 32);

    int:Unsigned16 e = 12;
    int:Unsigned16 a10 = e & 5;
    assertEqual(a10, 4);

    int:Unsigned32 f = 5;
    int:Unsigned16 a11 = e & f;
    assertEqual(a11, 4);

    int:Unsigned32 a12 = e & panicIndex;
    assertEqual(a12, 4);

    int:Unsigned16 a13 = c & e;
    assertEqual(a13, 0);

    ThisOrThat g = 12;
    AorB h = 25;
    assertEqual(g & h, 8);
}

function testBitwiseOROperation() {
    PanicIndex panicIndex = 5;
    int a1 = panicIndex | 3;
    assertEqual(a1, 7);

    a1 = panicIndex | A;
    assertEqual(a1, 13);

    int a2 = panicIndex | 0x5;
    assertEqual(a2, 0x5);

    byte a = 12;
    int a3 = a | panicIndex;
    assertEqual(a3, 13);

    int:Signed32|int:Unsigned8 b = 12;
    int a4 = b | panicIndex;
    assertEqual(a4, 13);

    ThreeNumbers c = 3;
    int a5 = c | b;
    assertEqual(a5, 15);

    int a6 = b | NEG_THIRTY_TWO;
    assertEqual(a6, -20);

    int a7 = 5 | a;
    assertEqual(a7, 13);

    int a8 = 5 | 12;
    assertEqual(a7, 13);

    ThirtiesCode d = 32;
    int a9 = d | NEG_THIRTY_TWO;
    assertEqual(a9, -32);

    int:Unsigned16 e = 12;
    int a10 = e | 5;
    assertEqual(a10, 13);

    int:Unsigned32 f = 5;
    int:Unsigned32 a11 = e | f;
    assertEqual(a11, 13);

    int a12 = e | panicIndex;
    assertEqual(a12, 13);

    int:Unsigned16|int a13 = c | e;
    assertEqual(a13, 15);

    int:Unsigned16 g = 256;
    int:Unsigned8 h = 0;

    int:Unsigned16 i = g | h;
    assertEqual(i, 256);
    assertEqual((i == (256|0)), true);

    int:Unsigned32 j = 12;
    int:Unsigned8 k = 5;
    int:Unsigned32 l = j | k;
    assertEqual(l, 13);
}

function testBitwiseXOROperation() {
    PanicIndex panicIndex = 5;
    int a1 = panicIndex ^ 3;
    assertEqual(a1, 6);

    a1 = panicIndex ^ A;
    assertEqual(a1, 9);

    int a2 = panicIndex ^ 0x5;
    assertEqual(a2, 0);

    byte a = 12;
    int a3 = a ^ panicIndex;
    assertEqual(a3, 9);

    int:Signed32|int:Unsigned8 b = 12;
    int a4 = b ^ panicIndex;
    assertEqual(a4, 9);

    ThreeNumbers c = 3;
    int a5 = c ^ b;
    assertEqual(a5, 15);

    int a6 = b ^ NEG_THIRTY_TWO;
    assertEqual(a6, -20);

    int a7 = 5 ^ a;
    assertEqual(a7, 9);

    int a8 = 5 ^ 12;
    assertEqual(a7, 9);

    ThirtiesCode d = 32;
    int a9 = d ^ NEG_THIRTY_TWO;
    assertEqual(a9, -64);

    int:Unsigned16 e = 12;
    int a10 = e ^ 5;
    assertEqual(a10, 9);

    int:Unsigned32 f = 5;
    int:Unsigned32 a11 = e ^ f;
    assertEqual(a11, 9);

    int:Unsigned32|int a12 = e ^ panicIndex;
    assertEqual(a12, 9);

    int:Unsigned16|int a13 = c ^ e;
    assertEqual(a13, 15);
}

function testBinaryBitwiseOperationsForNullable() {
    int? a = 43;

    byte? b = 5;

    int:Signed32? c = -21474836;
    int:Signed16? d = -454;
    int:Signed8? e = 100;

    int:Unsigned32? f = 21474836;
    int:Unsigned16? g = 54522;
    int:Unsigned8? h = 255;

    int i = 43;
    byte j = 5;
    int:Signed32 k = -21474836;

    assertEqual(a & b, 1);
    assertEqual(a & j, 1);
    assertEqual(j & a, 1);
    assertEqual(a & c, 40);
    assertEqual(a & d, 42);
    assertEqual(a & e, 32);
    assertEqual(a & f, 0);
    assertEqual(a & g, 42);
    assertEqual(a & h, 43);
    assertEqual(b & b, 5);
    assertEqual(b & c, 4);
    assertEqual(b & k, 4);
    assertEqual(k & b, 4);
    assertEqual(b & d, 0);
    assertEqual(b & e, 4);
    assertEqual(b & f, 4);
    assertEqual(b & g, 0);
    assertEqual(b & h, 5);
    assertEqual(c & c, -21474836);
    assertEqual(c & d, -21475288);
    assertEqual(c & e, 100);
    assertEqual(c & f, 4);
    assertEqual(c & g, 20712);
    assertEqual(c & h, 236);
    assertEqual(d & d, -454);
    assertEqual(d & e, 32);
    assertEqual(d & f, 21474832);
    assertEqual(d & g, 54330);
    assertEqual(d & h, 58);
    assertEqual(e & e, 100);
    assertEqual(e & f, 4);
    assertEqual(e & g, 96);
    assertEqual(e & h, 100);
    assertEqual(f & f, 21474836);
    assertEqual(f & g, 33808);
    assertEqual(f & h, 20);
    assertEqual(g & g, 54522);
    assertEqual(g & h, 250);
    assertEqual(h & h, 255);
    assertEqual(a & d, 42);
    assertEqual(a & e, 32);
    assertEqual(a & f, 0);
    assertEqual(a & g, 42);
    assertEqual(a & h, 43);

    assertEqual(a | b, 47);
    assertEqual(a | c, -21474833);
    assertEqual(a | d, -453);
    assertEqual(a | e, 111);
    assertEqual(a | f, 21474879);
    assertEqual(a | g, 54523);
    assertEqual(a | h, 255);
    assertEqual(b | b, 5);
    assertEqual(b | c, -21474835);
    assertEqual(b | d, -449);
    assertEqual(b | e, 101);
    assertEqual(c | c, -21474836);
    assertEqual(c | d, -2);
    assertEqual(c | e, -21474836);
    assertEqual(c | f, -4);
    assertEqual(c | g, -21441026);
    assertEqual(c | h, -21474817);
    assertEqual(d | d, -454);
    assertEqual(d | e, -386);
    assertEqual(d | f, -450);
    assertEqual(d | g, -262);
    assertEqual(d | h, -257);
    assertEqual(e | e, 100);
    assertEqual(e | f, 21474932);
    assertEqual(e | g, 54526);
    assertEqual(e | h, 255);
    assertEqual(f | f, 21474836);
    assertEqual(g | g, 54522);
    assertEqual(h | h, 255);

    assertEqual(a ^ b, 46);
    assertEqual(a ^ c, -21474873);
    assertEqual(a ^ d, -495);
    assertEqual(a ^ e, 79);
    assertEqual(a ^ f, 21474879);
    assertEqual(a ^ g, 54481);
    assertEqual(a ^ h, 212);
    assertEqual(b ^ b, 0);
    assertEqual(b ^ c, -21474839);
    assertEqual(b ^ d, -449);
    assertEqual(b ^ e, 97);
    assertEqual(c ^ c, 0);
    assertEqual(c ^ d, 21475286);
    assertEqual(c ^ e, -21474936);
    assertEqual(c ^ f, -8);
    assertEqual(c ^ g, -21461738);
    assertEqual(c ^ h, -21475053);
    assertEqual(d ^ d, 0);
    assertEqual(d ^ e, -418);
    assertEqual(d ^ f, -21475282);
    assertEqual(d ^ g, -54592);
    assertEqual(d ^ h, -315);
    assertEqual(e ^ e, 0);
    assertEqual(e ^ f, 21474928);
    assertEqual(e ^ g, 54430);
    assertEqual(e ^ h, 155);
    assertEqual(f ^ f, 0);
    assertEqual(g ^ g, 0);
    assertEqual(h ^ h, 0);
}

function assertEqual(anydata actual, anydata expected) {
    if actual == expected {
        return;
    }

    string actualValAsString = actual.toString();
    string expectedValAsString = expected.toString();
    panic error(string `Assertion error: expected ${expectedValAsString} found ${actualValAsString}`);
}
