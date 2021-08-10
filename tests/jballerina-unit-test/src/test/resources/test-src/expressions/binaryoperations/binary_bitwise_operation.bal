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
    int:Unsigned16 a11 = e | f;
    assertEqual(a11, 13);

    int a12 = e | panicIndex;
    assertEqual(a12, 13);

    int:Unsigned16|int a13 = c | e;
    assertEqual(a13, 15);
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
    int:Unsigned16 a11 = e ^ f;
    assertEqual(a11, 9);

    int:Unsigned32|int a12 = e ^ panicIndex;
    assertEqual(a12, 9);

    int:Unsigned16|int a13 = c ^ e;
    assertEqual(a13, 15);
}

function assertEqual(anydata actual, anydata expected) {
    if actual == expected {
        return;
    }

    string actualValAsString = actual.toString();
    string expectedValAsString = expected.toString();
    panic error(string `Assertion error: expected ${expectedValAsString} found ${actualValAsString}`);
}
