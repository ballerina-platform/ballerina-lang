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

public const SIXTY_TWO = 62;
public const SIXTY_THREE = 63;
public const SIXTY_FOUR = 64;
public const SIXTY_FIVE = 65;

const int CAI = 60 + 2;
const int CAF = 60 + 3;
const int CAD = 60 + 4;

type SixtiesCode SIXTY_TWO|SIXTY_THREE|SIXTY_FOUR|SIXTY_FIVE;

type ThisOrThat 63|65;

type SixtiesConst CAI|CAF|CAD;

public const byte ONE = 1;
public const byte TWO = 2;
public const byte THREE = 3;

type ThreeNumbers ONE|TWO|THREE;

const THIRTY_TWO1 = -32;
const THIRTY_TWO2 = 32;

type ThirtiesCode THIRTY_TWO1|THIRTY_TWO2;

type NumberSet 2|15|30;

function testBitwiseLeftShiftOp() {
    SixtiesCode a1 = 62;
    assertEqual(1 << a1, 0x4000000000000000);

    SixtiesCode a2 = 63;
    assertEqual(1 << a2, -9223372036854775807 - 1);

    SixtiesCode a3 = 64;
    assertEqual(1 << a3, 0x1);

    SixtiesCode a4 = 65;
    assertEqual(1 << a4, 0x2);

    assertEqual(1 << SIXTY_TWO, 0x4000000000000000);

    SIXTY_TWO|SIXTY_FOUR a6 = 64;
    assertEqual(1 << a6, 0x1);

    ThisOrThat a7 = 65;
    assertEqual(1 << a7, 0x2);

    SixtiesConst a8 = 62;
    assertEqual(1 << a8, 0x4000000000000000);

    SixtiesConst a9 = 63;
    assertEqual(1 << a9, -9223372036854775807 - 1);

    CAI|CAD a10 = 64;
    assertEqual(1 << a10, 0x1);

    ThreeNumbers a11 = 3;
    assertEqual(a11 << SIXTY_TWO, -0x4000000000000000);

    ThreeNumbers a12 = 2;
    assertEqual(a12 << a1, -9223372036854775807 - 1);

    assertEqual(1 << 62, 0x4000000000000000);

    assertEqual(1 << 64, 0x1);

    int:Unsigned16 a13 = 1;
    assertEqual(a13 << a3, 0x1);

    int|int:Unsigned32 a14 = 1;
    assertEqual(a14 << a3, 0x1);
}

function testBitwiseRightShiftOp() {
    SixtiesCode a1 = 62;
    assertEqual(1 >> a1, 0x0);

    SixtiesCode a2 = 63;
    assertEqual(1 >> a2, 0x0);

    SixtiesCode a3 = 64;
    assertEqual(1 >> a3, 0x1);

    SixtiesCode a4 = 65;
    assertEqual(1 >> a4, 0x0);

    assertEqual(1 >> SIXTY_TWO, 0x0);

    SIXTY_TWO|SIXTY_FOUR a6 = 64;
    assertEqual(1 >> a6, 0x1);

    ThisOrThat a7 = 65;
    assertEqual(1 >> a7, 0x0);

    SixtiesConst a8 = 62;
    assertEqual(1 >> a8, 0x0);

    SixtiesConst a9 = 63;
    assertEqual(1 >> a9, 0x0);

    CAI|CAD a10 = 64;
    assertEqual(1 >> a10, 0x1);

    ThreeNumbers a11 = 3;
    assertEqual(a11 >> SIXTY_TWO, 0x0);

    ThreeNumbers a12 = 2;
    assertEqual(a12 >> a1, 0x0);

    assertEqual(1 >> 62, 0x0);

    assertEqual(1 >> 64, 0x1);

    int:Unsigned16 a13 = 1;
    assertEqual(a13 >> a3, 0x1);

    int|int:Unsigned32 a14 = 1;
    assertEqual(a14 >> a3, 0x1);
}

function testBitwiseUnsignedRightShiftOp() {
    ThirtiesCode a1 = -32;
    assertEqual(a1 >>> 2, 0x3ffffffffffffff8);

    byte a2 = 2;
    assertEqual(a1 >>> a2, 0x3ffffffffffffff8);

    NumberSet a3 = 15;
    assertEqual(a3 >>> 2, 0x3);

    NumberSet a4 = 2;
    assertEqual(a1 >>> a4, 0x3ffffffffffffff8);

    assertEqual(-32 >>> 2, 0x3ffffffffffffff8);

    assertEqual(15 >>> 2, 0x3);

    int:Unsigned16 a5 = 15;

    assertEqual(a5 >>> 2, 0x3);

    int|int:Unsigned32 a6 = 15;
    assertEqual(a6 >>> a4, 0x3);
}

function testBitWiseOperationsForNullable() {
    int? a1 = 62;
    int? a2 = 63;
    int? a3 = 64;
    int? a4 = -32;
    int:Unsigned32? a5 = 15;
    int? a6 = 2;

    int? a = 128;
    int:Signed8? b = 7;
    int:Signed8? c = -120;
    int:Unsigned8? d = 5;
    int:Signed16? e = 8;
    int:Signed16? f = -32750;
    int:Unsigned16? g = 3;
    int:Signed32? h = 9;
    int:Signed32? i = -2147483641;
    int:Unsigned32? j = 17;

    assertEqual(1 << a1, 0x4000000000000000);
    assertEqual(1 << a2, -0x8000000000000000);
    assertEqual(1 << a3, 0x1);
    assertEqual(1 >> a1, 0x0);
    assertEqual(1 >> a2, 0x0);
    assertEqual(1 >> a3, 0x1);
    assertEqual(a4 >>> 2, 0x3ffffffffffffff8);
    assertEqual(a5 >>> a6, 0x3);

    assertEqual(a << b, 16384);
    assertEqual(a << c, 32768);

    assertEqual(a << d, 4096);

    assertEqual(a << e, 32768);
    assertEqual(a << f, 33554432);

    assertEqual(a << g, 1024);

    assertEqual(a << h, 65536);
    assertEqual(a << i, 16384);

    assertEqual(a << j, 16777216);

    assertEqual(11 << b, 1408);

    byte lhsval1 = 251;
    int:Unsigned8 lhsval2 = 251;
    byte? lhsval3 = 251;
    int:Unsigned8? lhsval4 = 251;
    int:Signed8? rhsval1 = -123;
    int? rhsval2 = -123;
    int:Signed8 rhsval3 = -123;
    int rhsval4 = -123;

    byte? ans1 = lhsval1 >>> rhsval1;
    int:Unsigned8?|float ans2 = lhsval2 >> rhsval1;
    ()|int ans3 = lhsval1 >>> rhsval2;
    int? ans4 = lhsval2 >> rhsval2;
    byte? ans5 = lhsval1 >> rhsval1;
    int:Unsigned8?|float ans6 = lhsval2 >>> rhsval1;
    var ans7 = lhsval3 >> rhsval1;
    var ans8 = lhsval4 >>> rhsval2;

    assertEqual([ans1, ans2, ans3, ans4], [7, 7, 7, 7]);
    assertEqual([ans5, ans6, ans7, ans8], [7, 7, 7, 7]);
    assertEqual([lhsval3 >>> rhsval3, lhsval4 >> rhsval3, lhsval3 >>> rhsval4, lhsval4 >> rhsval4],[7, 7, 7, 7]);
}

int intVal = 10;

function testNoShortCircuitingInBitwiseLeftShiftWithNullable() {
    int? result = foo() << bar();
    assertEqual(result, ());
    assertEqual(intVal, 18);

    result = foo() << 12;
    assertEqual(result, ());
    assertEqual(intVal, 20);

    result = 12 << bar();
    assertEqual(result, ());
    assertEqual(intVal, 26);

    int? x = 12;
    result = foo() << x;
    assertEqual(result, ());
    assertEqual(intVal, 28);

    result = x << bar();
    assertEqual(result, ());
    assertEqual(intVal, 34);

    result = x << bam();
    assertEqual(result, 384);
    assertEqual(intVal, 44);

    result = bam() << x;
    assertEqual(result, 20480);
    assertEqual(intVal, 54);

    result = foo() << bam();
    assertEqual(result, ());
    assertEqual(intVal, 66);

    result = bam() << bar();
    assertEqual(result, ());
    assertEqual(intVal, 82);
}

function testNoShortCircuitingInBitwiseLeftShiftWithNonNullable() {
    intVal = 10;
    int x = 10;

    int result = x << bam();
    assertEqual(result, 320);
    assertEqual(intVal, 20);

    result = bam() << 12;
    assertEqual(result, 20480);
    assertEqual(intVal, 30);
}

function testNoShortCircuitingInBitwiseSignedRightShiftWithNullable() {
    intVal = 10;

    int? result = foo() >> bar();
    assertEqual(result, ());
    assertEqual(intVal, 18);

    result = foo() >> 12;
    assertEqual(result, ());
    assertEqual(intVal, 20);

    result = 12 >> bar();
    assertEqual(result, ());
    assertEqual(intVal, 26);

    int? x = 12;
    result = foo() >> x;
    assertEqual(result, ());
    assertEqual(intVal, 28);

    result = x >> bar();
    assertEqual(result, ());
    assertEqual(intVal, 34);

    result = x >> bam();
    assertEqual(result, 0);
    assertEqual(intVal, 44);

    result = bam() >> x;
    assertEqual(result, 0);
    assertEqual(intVal, 54);

    result = foo() >> bam();
    assertEqual(result, ());
    assertEqual(intVal, 66);

    result = bam() >> bar();
    assertEqual(result, ());
    assertEqual(intVal, 82);
}

function testNoShortCircuitingInBitwiseSignedRightShiftWithNonNullable() {
    intVal = 10;
    int x = 10;

    int result = x >> bam();
    assertEqual(result, 0);
    assertEqual(intVal, 20);

    result = bam() >> 12;
    assertEqual(result, 0);
    assertEqual(intVal, 30);
}

function testNoShortCircuitingInBitwiseUnsignedRightShiftWithNullable() {
    intVal = 10;

    int? result = foo() >>> bar();
    assertEqual(result, ());
    assertEqual(intVal, 18);

    result = foo() >>> 12;
    assertEqual(result, ());
    assertEqual(intVal, 20);

    result = 12 >>> bar();
    assertEqual(result, ());
    assertEqual(intVal, 26);

    int? x = 12;
    result = foo() >>> x;
    assertEqual(result, ());
    assertEqual(intVal, 28);

    result = x >>> bar();
    assertEqual(result, ());
    assertEqual(intVal, 34);

    result = x >>> bam();
    assertEqual(result, 0);
    assertEqual(intVal, 44);

    result = bam() >>> x;
    assertEqual(result, 0);
    assertEqual(intVal, 54);

    result = foo() >>> bam();
    assertEqual(result, ());
    assertEqual(intVal, 66);

    result = bam() >>> bar();
    assertEqual(result, ());
    assertEqual(intVal, 82);
}

function testNoShortCircuitingInBitwiseUnsignedRightShiftWithNonNullable() {
    intVal = 10;
    int x = 10;

    int result = x >>> bam();
    assertEqual(result, 0);
    assertEqual(intVal, 20);

    result = bam() >>> 12;
    assertEqual(result, 0);
    assertEqual(intVal, 30);
}

function foo() returns int? {
    intVal += 2;
    return ();
}

function bar() returns int? {
    intVal += 6;
    return ();
}

function bam() returns int {
    intVal += 10;
    return 5;
}

function assertEqual(any actual, any expected) {
    if actual is anydata && expected is anydata && actual == expected {
        return;
    }

    if actual === expected {
        return;
    }

    string actualValAsString = actual.toString();
    string expectedValAsString = expected.toString();
    panic error(string `Assertion error: expected ${expectedValAsString} found ${actualValAsString}`);
}
