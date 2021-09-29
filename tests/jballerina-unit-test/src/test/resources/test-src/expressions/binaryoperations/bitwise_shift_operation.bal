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
    assertEqual(1 << a2, -0x8000000000000000);

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
    assertEqual(1 << a9, -0x8000000000000000);

    CAI|CAD a10 = 64;
    assertEqual(1 << a10, 0x1);

    ThreeNumbers a11 = 3;
    assertEqual(a11 << SIXTY_TWO, -0x4000000000000000);

    ThreeNumbers a12 = 2;
    assertEqual(a12 << a1, -0x8000000000000000);

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

    assertEqual(1 << a1, 0x4000000000000000);
    assertEqual(1 << a2, -0x8000000000000000);
    assertEqual(1 << a3, 0x1);
    assertEqual(1 >> a1, 0x0);
    assertEqual(1 >> a2, 0x0);
    assertEqual(1 >> a3, 0x1);
    assertEqual(a4 >>> 2, 0x3ffffffffffffff8);
    assertEqual(a5 >>> a6, 0x3);
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
