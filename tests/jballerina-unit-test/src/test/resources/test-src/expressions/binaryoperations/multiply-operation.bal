// Copyright (c) 2019, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
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

import ballerina/lang.test as test;

function intMultiply(int a, int b) returns (int) {
    return a * b;
}

function overflowByMultiplication() {
    int num1 = -1;
    int num2 = -9223372036854775807 - 1;
    int ans = num1 * num2;
}

function floatMultiply(float a, float b) returns (float) {
    return a * b;
}

public const A = 10;
public const B = 20;
public const C = 30;
public const D = 40;

type SomeTypes A|B|C|D;

type E 12|13|14;

const float F = 20.5;
const float G = 10.5;

type H F|G;

type I 10.5|30.5;

const decimal J = 4.5;
const decimal K = 10.5;

type L J|K;

type IntType1 -2|-1|0|1|2;
type IntType2 int:Unsigned8|int:Signed32;
type IntType3 IntType1|IntType2;
type IntType4 IntType1|byte;

const float AA = 1.25;
const float BB = 2.5;

type FloatType1 -2.0f|-1.0f|0.0f|1.0f|2.0f;
type FloatType2 FloatType1;
type FloatType3 AA|BB;

const decimal CC = 1.25;
const decimal DD = 3.0;

type DecimalType1 CC|DD;
type DecimalType2 1d|2d|-1d|2d;
type DecimalType3 DecimalType1|DecimalType2;

function testMultiplicationWithTypes() {
    SomeTypes a1 = 10;
    int a2 = 20;
    SomeTypes a3 = 30;
    byte a4 = 25;
    int|int:Signed16 a5 = 15;
    E a6 = 12;
    float a7 = 10.5;
    H a8 = 10.5;
    I a9 = 30.5;
    L a10 = 10.5;
    decimal a11 = 20;

    assertEqual(a1 * a2, 200);
    assertEqual(a2 * a3, 600);
    assertEqual(a3 * a4, 750);
    assertEqual(a1 * a5, 150);
    assertEqual(a1 * a6, 120);
    assertEqual(a4 * a6, 300);
    assertEqual(a5 * a6, 180);
    assertEqual(a7 * a8, 110.25);
    assertEqual(a7 * a9, 320.25);
    assertEqual(a8 * a9, 320.25);
    assertEqual(a10 * a11, 210d);

    IntType3 a21 = 1;
    IntType3|int a22 = 2;
    int|IntType4 a23 = 3;

    assertEqual(a21 * a21, 1);
    assertEqual(a21 * a22, 2);
    assertEqual(a21 * a23, 3);
    assertEqual(a22 * a23, 6);
    assertEqual(a23 * a23, 9);

    FloatType2 a24 = -2;
    FloatType2 a25 = 1;
    FloatType3|float a26 = 1.25;
    float|FloatType3 a27 = 2.5;

    assertEqual(a24 * a24, 4.0);
    assertEqual(a24 * a25, -2.0);
    assertEqual(a24 * a26, -2.5);
    assertEqual(a24 * a27, -5.0);
    assertEqual(a25 * a25, 1.0);
    assertEqual(a25 * a26, 1.25);
    assertEqual(a25 * a27, 2.5);
    assertEqual(a26 * a26, 1.5625);
    assertEqual(a26 * a27, 3.125);
    assertEqual(a27 * a27, 6.25);

    DecimalType1 a28 = 1.25;
    DecimalType3|decimal a29 = 2;
    decimal|DecimalType3 a30 = 3;

    assertEqual(a28 * a28, 1.5625d);
    assertEqual(a28 * a29, 2.5d);
    assertEqual(a28 * a30, 3.75d);
    assertEqual(a29 * a29, 4d);
    assertEqual(a29 * a30, 6d);
    assertEqual(a30 * a30, 9d);
}

function testMultiplySingleton() {
    20 a1 = 20;
    int a2 = 2;
    20.5 a3 = 20.5;
    float a4 = 10;
    SomeTypes a5 = 30;
    int|int:Signed16 a6 = 5;
    E a7 = 12;

    assertEqual(a1 * a2, 40);
    assertEqual(a3 * a4, 205.0);
    assertEqual(a1 * a5, 600);
    assertEqual(a1 * a6, 100);
    assertEqual(a1 * a7, 240);
}

function testContextuallyExpectedTypeOfNumericLiteralInMultiply() {
    float a1 = 10.0 * 2;
    float a2 = 5 * 3 * 2.0;
    decimal a3 = 15.0 * 2;
    decimal a4 = 5.0 * 3.0 * 2;
    float? a5 = 10 * 5;
    decimal? a6 = 2 * 10.0;

    assertEqual(a1, 20.0);
    assertEqual(a2, 30.0);
    assertEqual(a3, 30.0d);
    assertEqual(a4, 30.0d);
    assertEqual(a5, 50.0);
    assertEqual(a6, 20.0d);
}

type Ints 1|2;
type T1 1|2|()|3;
type T2 1|2|3?;
type Decimals 1d|2d;

function testMultiplyNullable() {
    int? a1 = 10;
    int? a2 = 2;
    int? a3 = 1;
    int? a4 = ();
    int a5 = 5;
    float? a6 = 30.0;
    float? a7 = 10.0;
    float? a8 = ();
    float a9 = 5.0;

    int? a10 = (a1 * a2) * a5;
    int? a11 = a5 * a3;
    int? a12 = a4 * a1;
    float? a13 = a6 * a7;
    float? a14 = a6 * a9;
    float? a15 = a6 * a8;

    Ints a16 = 2;
    int? a17 = 1;
    int? a18 = a16 * a17;

    int a19 = 25;
    Ints? a20 = 2;

    T1 a21 = 2;
    T2? a22 = 1;
    ()|int a23 = ();
    T2? a24 = 1;

    Decimals? a25 = 1;
    Decimals? a26 = 2;

    int:Unsigned8 a = 1;
    int:Unsigned16 b = 2;
    int:Unsigned32 c = 5;
    int:Signed8 d = 20;
    int:Signed16 e = 10;
    int:Signed32 f = 10;
    byte g = 30;

    assertEqual(a10, 100);
    assertEqual(a11, 5);
    assertEqual(a12, ());
    assertEqual(a13, 300.0);
    assertEqual(a14, 150.0);
    assertEqual(a15, ());
    assertEqual(a18, 2);
    assertEqual(a19 * a20, 50);

    assertEqual(a21 * a21, 4);
    assertEqual(a21 * a22, 2);
    assertEqual(a21 * a23, ());
    assertEqual(a22 * a22, 1);
    assertEqual(a22 * a23, ());
    assertEqual(a23 * a23, ());
    assertEqual(a24 * a21, 2);
    assertEqual(a25 * a26, 2d);

    assertEqual(a * a, 1);
    assertEqual(a * b, 2);
    assertEqual(a * c, 5);
    assertEqual(a * d, 20);
    assertEqual(a * e, 10);
    assertEqual(a * f, 10);
    assertEqual(a * g, 30);

    assertEqual(b * c, 10);
    assertEqual(b * d, 40);
    assertEqual(b * e, 20);
    assertEqual(b * f, 20);
    assertEqual(b * g, 60);
    assertEqual(b * b, 4);

    assertEqual(c * c, 25);
    assertEqual(c * d, 100);
    assertEqual(c * e, 50);
    assertEqual(c * f, 50);
    assertEqual(c * g, 150);

    assertEqual(d * d, 400);
    assertEqual(d * e, 200);
    assertEqual(d * f, 200);
    assertEqual(d * g, 600);

    assertEqual(e * e, 100);
    assertEqual(e * f, 100);
    assertEqual(e * g, 300);

    assertEqual(f * f, 100);
    assertEqual(f * g, 300);

    assertEqual(g * g, 900);

    IntType3? a27 = 1;
    IntType3? a28 = 2;
    IntType4? a29 = 3;

    assertEqual(a27 * a27, 1);
    assertEqual(a27 * a28, 2);
    assertEqual(a27 * a29, 3);
    assertEqual(a28 * a29, 6);
    assertEqual(a29 * a29, 9);

    FloatType2? a30 = -2;
    FloatType2? a31 = 1;
    FloatType3? a32 = 1.25;
    FloatType3? a33 = 2.5;

    assertEqual(a30 * a30, 4.0);
    assertEqual(a30 * a31, -2.0);
    assertEqual(a30 * a32, -2.5);
    assertEqual(a30 * a33, -5.0);
    assertEqual(a31 * a31, 1.0);
    assertEqual(a31 * a32, 1.25);
    assertEqual(a31 * a33, 2.5);
    assertEqual(a32 * a32, 1.5625);
    assertEqual(a32 * a33, 3.125);
    assertEqual(a33 * a33, 6.25);

    DecimalType1? a34 = 1.25;
    DecimalType3? a35 = 2;
    DecimalType3? a36 = 3;

    assertEqual(a34 * a34, 1.5625d);
    assertEqual(a34 * a35, 2.5d);
    assertEqual(a34 * a36, 3.75d);
    assertEqual(a35 * a35, 4d);
    assertEqual(a35 * a36, 6d);
    assertEqual(a36 * a36, 9d);
}

const int constInt = 5;

const float constFloat = 20.5;

type MyInt int;

type MyFloat float;

type TWO 2;

type FOUR_POINT_FIVE 4.5;

function testMultiplyFloatInt() {
    int a = 2;
    float b = 4.5;
    float c = 4.5e-1;
    float d = -10.5;
    int e = int:MAX_VALUE;
    int f = int:MIN_VALUE;
    MyInt g = 2;
    MyFloat h = 4.5;
    2 i = 2;
    4.5 j = 4.5;
    constInt k = 5;
    constFloat m = 20.5;
    TWO n = 2;
    FOUR_POINT_FIVE p = 4.5;

    float var1 = a * b;
    assertEqual(var1, 9.0);
    float var2 = a * c;
    assertEqual(var2, 0.9);
    float var3 = a * d;
    assertEqual(var3, -21.0);
    float var31 = a * h;
    assertEqual(var31, 9.0);
    float var32 = a * j;
    assertEqual(var32, 9.0);
    float var33 = a * m;
    assertEqual(var33, 41.0);
    float var34 = a * p;
    assertEqual(var34, 9.0);

    float var4 = b * a;
    assertEqual(var4, 9.0);
    float var5 = c * a;
    assertEqual(var5, 0.9);
    float var6 = d * a;
    assertEqual(var6, -21.0);
    float var61 = h * a;
    assertEqual(var61, 9.0);
    float var62 = j * a;
    assertEqual(var62, 9.0);
    float var63 = m * a;
    assertEqual(var63, 41.0);
    float var64 = p * a;
    assertEqual(var64, 9.0);

    float var7 = constInt * b;
    assertEqual(var7, 22.5);
    float var8 = constInt * c;
    assertEqual(var8, 2.25);
    float var9 = constInt * d;
    assertEqual(var9, -52.5);
    float var91 = constInt * h;
    assertEqual(var91, 22.5);
    float var92 = constInt * j;
    assertEqual(var92, 22.5);
    float var93 = constInt * m;
    assertEqual(var93, 102.5);
    float var94 = constInt * p;
    assertEqual(var94, 22.5);

    float var10 = b * constInt;
    assertEqual(var10, 22.5);
    float var11 = c * constInt;
    assertEqual(var11, 2.25);
    float var12 = d * constInt;
    assertEqual(var12, -52.5);
    float var121 = h * constInt;
    assertEqual(var121, 22.5);
    float var122 = j * constInt;
    assertEqual(var122, 22.5);
    float var123 = m * constInt;
    assertEqual(var123, 102.5);
    float var124 = p * constInt;
    assertEqual(var124, 22.5);

    float var13 = constFloat * constInt;
    assertEqual(var13, 102.5);
    float var14 = constInt * constFloat;
    assertEqual(var14, 102.5);

    float var15 = constFloat * a;
    assertEqual(var15, 41.0);
    float var16 = a * constFloat;
    assertEqual(var16, 41.0);

    float var17 = b * e;
    assertEqual(var17, 4.150517416584649E19);
    float var18 = b * f;
    assertEqual(var18, -4.150517416584649E19);
    float var19 = b * g;
    assertEqual(var19, 9.0);

    float var20 = g * h;
    assertEqual(var20, 9.0);

    float var21 = i * b;
    assertEqual(var21, 9.0);
    float var22 = b * i;
    assertEqual(var22, 9.0);
    float var23 = h * i;
    assertEqual(var23, 9.0);
    float var24 = i * h;
    assertEqual(var24, 9.0);
    float var25 = i * constFloat;
    assertEqual(var25, 41.0);
    float var26 = constFloat * i;
    assertEqual(var26, 41.0);

    float var27 = k * b;
    assertEqual(var27, 22.5);
    float var28 = b * k;
    assertEqual(var28, 22.5);
    float var29 = h * k;
    assertEqual(var29, 22.5);
    float var30 = k * h;
    assertEqual(var30, 22.5);
    float var311 = k * constFloat;
    assertEqual(var311, 102.5);
    float var312 = constFloat * k;
    assertEqual(var312, 102.5);

    float var313 = n * b;
    assertEqual(var313, 9.0);
    float var314 = b * n;
    assertEqual(var314, 9.0);
    float var315 = h * n;
    assertEqual(var315, 9.0);
    float var316 = n * h;
    assertEqual(var316, 9.0);
    float var317 = n * constFloat;
    assertEqual(var317, 41.0);
    float var318 = constFloat * n;
    assertEqual(var318, 41.0);
}

function testMultiplyFloatIntSubTypes() {
    int:Signed8 a = -2;
    int:Signed16 b = 2;
    int:Signed32 c = -654;
    int:Unsigned8 d = 3;
    int:Unsigned16 e = 5;
    int:Unsigned32 f = 10;
    byte g = 43;

    float h = 2.5;

    float var1 = a * h;
    assertEqual(var1, -5.0);
    float var2 = b * h;
    assertEqual(var2, 5.0);
    float var3 = c * h;
    assertEqual(var3, -1635.0);
    float var4 = d * h;
    assertEqual(var4, 7.5);
    float var5 = e * h;
    assertEqual(var5, 12.5);
    float var6 = f * h;
    assertEqual(var6, 25.0);
    float var7 = g * h;
    assertEqual(var7, 107.5);

    float var8 = h * a;
    assertEqual(var8, -5.0);
    float var9 = h * b;
    assertEqual(var9, 5.0);
    float var10 = h * c;
    assertEqual(var10, -1635.0);
    float var11 = h * d;
    assertEqual(var11, 7.5);
    float var12 = h * e;
    assertEqual(var12, 12.5);
    float var13 = h * f;
    assertEqual(var13, 25.0);
    float var14 = h * g;
    assertEqual(var14, 107.5);
}

function testMultiplyFloatIntWithNullableOperands() {
    int a = 2;
    int? b = 4;
    5? c = 5;
    float d = 4.5e-1;
    float? e = -10.5;
    2.5? f = 2.5;

    float? var1 = a * e;
    assertEqual(var1, -21.0);
    float? var2 = e * a;
    assertEqual(var2, -21.0);

    float? var3 = b * d;
    assertEqual(var3, 1.8);
    float? var4 = d * b;
    assertEqual(var4, 1.8);

    float? var5 = b * e;
    assertEqual(var5, -42.0);
    float? var6 = e * b;
    assertEqual(var6, -42.0);

    float? var7 = constInt * e;
    assertEqual(var7, -52.5);
    float? var8 = e * constInt;
    assertEqual(var8, -52.5);

    float? var9 = constFloat * b;
    assertEqual(var9, 82.0);
    float? var10 = b * constFloat;
    assertEqual(var10, 82.0);

    float? var11 = a * f;
    assertEqual(var11, 5.0);
    float? var12 = f * a;
    assertEqual(var12, 5.0);

    float? var13 = c * d;
    assertEqual(var13, 2.25);
    float? var14 = d * c;
    assertEqual(var14, 2.25);

    float? var15 = c * f;
    assertEqual(var15, 12.5);
    float? var16 = f * c;
    assertEqual(var16, 12.5);
}

function testMultiplyFloatIntSubTypeWithNullableOperands() {
    int:Signed8 a = -2;
    int:Signed16 b = 2;
    int:Signed32 c = -654;
    int:Unsigned8 d = 3;
    int:Unsigned16 e = 5;
    int:Unsigned32 f = 10;
    byte g = 43;

    int:Signed8? h = -2;
    int:Signed16? i = 2;
    int:Signed32? j = -654;
    int:Unsigned8? k = 3;
    int:Unsigned16? m = 5;
    int:Unsigned32? n = 10;
    byte? p = 43;

    float q = 2.5;
    float? r = 2.5;

    float? var1 = a * r;
    assertEqual(var1, -5.0);
    float? var2 = b * r;
    assertEqual(var2, 5.0);
    float? var3 = c * r;
    assertEqual(var3, -1635.0);
    float? var4 = d * r;
    assertEqual(var4, 7.5);
    float? var5 = e * r;
    assertEqual(var5, 12.5);
    float? var6 = f * r;
    assertEqual(var6, 25.0);
    float? var7 = g * r;
    assertEqual(var7, 107.5);

    float? var8 = r * a;
    assertEqual(var8, -5.0);
    float? var9 = r * b;
    assertEqual(var9, 5.0);
    float? var10 = r * c;
    assertEqual(var10, -1635.0);
    float? var11 = r * d;
    assertEqual(var11, 7.5);
    float? var12 = r * e;
    assertEqual(var12, 12.5);
    float? var13 = r * f;
    assertEqual(var13, 25.0);
    float? var14 = r * g;
    assertEqual(var14, 107.5);

    float? var15 = h * q;
    assertEqual(var15, -5.0);
    float? var16 = i * q;
    assertEqual(var16, 5.0);
    float? var17 = j * q;
    assertEqual(var17, -1635.0);
    float? var18 = k * q;
    assertEqual(var18, 7.5);
    float? var19 = m * q;
    assertEqual(var19, 12.5);
    float? var20 = n * q;
    assertEqual(var20, 25.0);
    float? var21 = p * q;
    assertEqual(var21, 107.5);

    float? var22 = q * h;
    assertEqual(var22, -5.0);
    float? var23 = q * i;
    assertEqual(var23, 5.0);
    float? var24 = q * j;
    assertEqual(var24, -1635.0);
    float? var25 = q * k;
    assertEqual(var25, 7.5);
    float? var26 = q * m;
    assertEqual(var26, 12.5);
    float? var27 = q * n;
    assertEqual(var27, 25.0);
    float? var28 = q * p;
    assertEqual(var28, 107.5);

    float? var29 = h * r;
    assertEqual(var29, -5.0);
    float? var30 = i * r;
    assertEqual(var30, 5.0);
    float? var31 = j * r;
    assertEqual(var31, -1635.0);
    float? var32 = k * r;
    assertEqual(var32, 7.5);
    float? var33 = m * r;
    assertEqual(var33, 12.5);
    float? var34 = n * r;
    assertEqual(var34, 25.0);
    float? var35 = p * r;
    assertEqual(var35, 107.5);

    float? var36 = r * h;
    assertEqual(var36, -5.0);
    float? var37 = r * i;
    assertEqual(var37, 5.0);
    float? var38 = r * j;
    assertEqual(var38, -1635.0);
    float? var39 = r * k;
    assertEqual(var39, 7.5);
    float? var40 = r * m;
    assertEqual(var40, 12.5);
    float? var41 = r * n;
    assertEqual(var41, 25.0);
    float? var42 = r * p;
    assertEqual(var42, 107.5);
}

function testResultTypeOfMultiplyFloatIntByInfering() {
    float a = 2.5;
    int b = 3;

    var c = a * b;
    float var1 = c;
    assertEqual(var1, 7.5);

    var d = b * a;
    float var2 = d;
    assertEqual(var2, 7.5);

    var e = a * constInt;
    float var3 = e;
    assertEqual(var3, 12.5);

    var f = constInt * a;
    float var4 = f;
    assertEqual(var4, 12.5);

    var g = constFloat * b;
    float var5 = g;
    assertEqual(var5, 61.5);

    var h = b * constFloat;
    float var6 = h;
    assertEqual(var6, 61.5);

    var i = constFloat * constInt;
    float var7 = i;
    assertEqual(var7, 102.5);

    var j = constInt * constFloat;
    float var8 = j;
    assertEqual(var8, 102.5);
}

function testResultTypeOfMultiplyFloatIntForNilableOperandsByInfering() {
    float? a = 2.5;
    int? b = 3;

    var c = a * b;
    float? var1 = c;
    assertEqual(var1, 7.5);

    var d = b * a;
    float? var2 = d;
    assertEqual(var2, 7.5);

    var e = a * constInt;
    float? var3 = e;
    assertEqual(var3, 12.5);

    var f = constInt * a;
    float? var4 = f;
    assertEqual(var4, 12.5);

    var g = constFloat * b;
    float? var5 = g;
    assertEqual(var5, 61.5);

    var h = b * constFloat;
    float? var6 = h;
    assertEqual(var6, 61.5);
}

function testMultiplyFloatIntToInfinityAndNaN() {
    float a = 8388608333e+298;
    int b = 20;
    float c = float:Infinity;
    float d = float:NaN;

    float var1 = a * b;
    assertEqual(var1, float:Infinity);

    float var2 = b * a;
    assertEqual(var2, float:Infinity);

    float var3 = c * b;
    assertEqual(var3, float:Infinity);

    float var4 = b * c;
    assertEqual(var4, float:Infinity);

    float var5 = d * b;
    assertEqual(var5, float:NaN);

    float var6 = b * d;
    assertEqual(var6, float:NaN);
}

const decimal constDecimal = 20.5;

type MyDecimal decimal;

type FOUR_POINT_FIVE_DECIMAL 4.5d;

function testMultiplyDecimalInt() {
    int a = 2;
    decimal b = 4.5;
    decimal c = 4.5e-1;
    decimal d = -10.5;
    int e = int:MAX_VALUE;
    int f = int:MIN_VALUE;
    MyInt g = 2;
    MyDecimal h = 4.5;
    2 i = 2;
    4.5d j = 4.5d;
    constInt k = 5;
    constDecimal m = 20.5;
    TWO n = 2;
    FOUR_POINT_FIVE_DECIMAL p = 4.5d;

    decimal var1 = a * b;
    assertEqual(var1, 9.00d);
    decimal var2 = a * c;
    assertEqual(var2, 0.900d);
    decimal var3 = a * d;
    assertEqual(var3, -21.00d);
    decimal var31 = a * h;
    assertEqual(var31, 9.00d);
    decimal var32 = a * j;
    assertEqual(var32, 9.00d);
    decimal var33 = a * m;
    assertEqual(var33, 41.00d);
    decimal var34 = a * p;
    assertEqual(var34, 9.00d);

    decimal var4 = b * a;
    assertEqual(var4, 9.00d);
    decimal var5 = c * a;
    assertEqual(var5, 0.900d);
    decimal var6 = d * a;
    assertEqual(var6, -21.00d);
    decimal var61 = h * a;
    assertEqual(var61, 9.00d);
    decimal var62 = j * a;
    assertEqual(var62, 9.00d);
    decimal var63 = m * a;
    assertEqual(var63, 41.00d);
    decimal var64 = p * a;
    assertEqual(var64, 9.00d);

    decimal var7 = constInt * b;
    assertEqual(var7, 22.50d);
    decimal var8 = constInt * c;
    assertEqual(var8, 2.250d);
    decimal var9 = constInt * d;
    assertEqual(var9, -52.50d);
    decimal var91 = constInt * h;
    assertEqual(var91, 22.50d);
    decimal var92 = constInt * j;
    assertEqual(var92, 22.50d);
    decimal var93 = constInt * m;
    assertEqual(var93, 102.50d);
    decimal var94 = constInt * p;
    assertEqual(var94, 22.50d);

    decimal var10 = b * constInt;
    assertEqual(var10, 22.50d);
    decimal var11 = c * constInt;
    assertEqual(var11, 2.250d);
    decimal var12 = d * constInt;
    assertEqual(var12, -52.50d);
    decimal var121 = h * constInt;
    assertEqual(var121, 22.50d);
    decimal var122 = j * constInt;
    assertEqual(var122, 22.50d);
    decimal var123 = m * constInt;
    assertEqual(var123, 102.50d);
    decimal var124 = p * constInt;
    assertEqual(var124, 22.50d);

    decimal var13 = constDecimal * constInt;
    assertEqual(var13, 102.50d);
    decimal var14 = constInt * constDecimal;
    assertEqual(var14, 102.50d);

    decimal var15 = constDecimal * a;
    assertEqual(var15, 41.00d);
    decimal var16 = a * constDecimal;
    assertEqual(var16, 41.00d);

    decimal var17 = b * e;
    assertEqual(var17, 41505174165846491131.50d);
    decimal var18 = b * f;
    assertEqual(var18, -41505174165846491136.00d);
    decimal var19 = b * g;
    assertEqual(var19, 9.00d);

    decimal var20 = g * h;
    assertEqual(var20, 9.00d);

    decimal var21 = i * b;
    assertEqual(var21, 9.00d);
    decimal var22 = b * i;
    assertEqual(var22, 9.00d);
    decimal var23 = h * i;
    assertEqual(var23, 9.00d);
    decimal var24 = i * h;
    assertEqual(var24, 9.00d);
    decimal var25 = i * constDecimal;
    assertEqual(var25, 41.00d);
    decimal var26 = constDecimal * i;
    assertEqual(var26, 41.00d);

    decimal var27 = k * b;
    assertEqual(var27, 22.50d);
    decimal var28 = b * k;
    assertEqual(var28, 22.50d);
    decimal var29 = h * k;
    assertEqual(var29, 22.50d);
    decimal var30 = k * h;
    assertEqual(var30, 22.50d);
    decimal var311 = k * constDecimal;
    assertEqual(var311, 102.50d);
    decimal var312 = constDecimal * k;
    assertEqual(var312, 102.50d);

    decimal var313 = n * b;
    assertEqual(var313, 9.00d);
    decimal var314 = b * n;
    assertEqual(var314, 9.00d);
    decimal var315 = h * n;
    assertEqual(var315, 9.00d);
    decimal var316 = n * h;
    assertEqual(var316, 9.00d);
    decimal var317 = n * constDecimal;
    assertEqual(var317, 41.00d);
    decimal var318 = constDecimal * n;
    assertEqual(var318, 41.00d);
}

function testMultiplyDecimalIntSubTypes() {
    int:Signed8 a = -2;
    int:Signed16 b = 2;
    int:Signed32 c = -654;
    int:Unsigned8 d = 3;
    int:Unsigned16 e = 5;
    int:Unsigned32 f = 10;
    byte g = 43;

    decimal h = 2.5;

    decimal var1 = a * h;
    assertEqual(var1, -5.00d);
    decimal var2 = b * h;
    assertEqual(var2, 5.00d);
    decimal var3 = c * h;
    assertEqual(var3, -1635.00d);
    decimal var4 = d * h;
    assertEqual(var4, 7.50d);
    decimal var5 = e * h;
    assertEqual(var5, 12.50d);
    decimal var6 = f * h;
    assertEqual(var6, 25.00d);
    decimal var7 = g * h;
    assertEqual(var7, 107.50d);

    decimal var8 = h * a;
    assertEqual(var8, -5.00d);
    decimal var9 = h * b;
    assertEqual(var9, 5.00d);
    decimal var10 = h * c;
    assertEqual(var10, -1635.00d);
    decimal var11 = h * d;
    assertEqual(var11, 7.50d);
    decimal var12 = h * e;
    assertEqual(var12, 12.50d);
    decimal var13 = h * f;
    assertEqual(var13, 25.00d);
    decimal var14 = h * g;
    assertEqual(var14, 107.50d);
}

function testMultiplyDecimalIntWithNullableOperands() {
    int a = 2;
    int? b = 4;
    5? c = 5;
    decimal d = 4.5e-1;
    decimal? e = -10.5;
    2.5d? f = 2.5d;

    decimal? var1 = a * e;
    assertEqual(var1, -21.00d);
    decimal? var2 = e * a;
    assertEqual(var2, -21.00d);

    decimal? var3 = b * d;
    assertEqual(var3, 1.80d);
    decimal? var4 = d * b;
    assertEqual(var4, 1.80d);

    decimal? var5 = b * e;
    assertEqual(var5, -42.00d);
    decimal? var6 = e * b;
    assertEqual(var6, -42.00d);

    decimal? var7 = constInt * e;
    assertEqual(var7, -52.50d);
    decimal? var8 = e * constInt;
    assertEqual(var8, -52.50d);

    decimal? var9 = constDecimal * b;
    assertEqual(var9, 82.00d);
    decimal? var10 = b * constDecimal;
    assertEqual(var10, 82.00d);

    decimal? var11 = a * f;
    assertEqual(var11, 5.00d);
    decimal? var12 = f * a;
    assertEqual(var12, 5.00d);

    decimal? var13 = c * d;
    assertEqual(var13, 2.250d);
    decimal? var14 = d * c;
    assertEqual(var14, 2.250d);

    decimal? var15 = c * f;
    assertEqual(var15, 12.50d);
    decimal? var16 = f * c;
    assertEqual(var16, 12.50d);
}

function testMultiplyDecimalIntSubTypeWithNullableOperands() {
    int:Signed8 a = -2;
    int:Signed16 b = 2;
    int:Signed32 c = -654;
    int:Unsigned8 d = 3;
    int:Unsigned16 e = 5;
    int:Unsigned32 f = 10;
    byte g = 43;

    int:Signed8? h = -2;
    int:Signed16? i = 2;
    int:Signed32? j = -654;
    int:Unsigned8? k = 3;
    int:Unsigned16? m = 5;
    int:Unsigned32? n = 10;
    byte? p = 43;

    decimal q = 2.5;
    decimal? r = 2.5;

    decimal? var1 = a * r;
    assertEqual(var1, -5.00d);
    decimal? var2 = b * r;
    assertEqual(var2, 5.00d);
    decimal? var3 = c * r;
    assertEqual(var3, -1635.00d);
    decimal? var4 = d * r;
    assertEqual(var4, 7.50d);
    decimal? var5 = e * r;
    assertEqual(var5, 12.50d);
    decimal? var6 = f * r;
    assertEqual(var6, 25.00d);
    decimal? var7 = g * r;
    assertEqual(var7, 107.50d);

    decimal? var8 = r * a;
    assertEqual(var8, -5.00d);
    decimal? var9 = r * b;
    assertEqual(var9, 5.00d);
    decimal? var10 = r * c;
    assertEqual(var10, -1635.00d);
    decimal? var11 = r * d;
    assertEqual(var11, 7.50d);
    decimal? var12 = r * e;
    assertEqual(var12, 12.50d);
    decimal? var13 = r * f;
    assertEqual(var13, 25.00d);
    decimal? var14 = r * g;
    assertEqual(var14, 107.50d);

    decimal? var15 = h * q;
    assertEqual(var15, -5.00d);
    decimal? var16 = i * q;
    assertEqual(var16, 5.00d);
    decimal? var17 = j * q;
    assertEqual(var17, -1635.00d);
    decimal? var18 = k * q;
    assertEqual(var18, 7.50d);
    decimal? var19 = m * q;
    assertEqual(var19, 12.50d);
    decimal? var20 = n * q;
    assertEqual(var20, 25.00d);
    decimal? var21 = p * q;
    assertEqual(var21, 107.50d);

    decimal? var22 = q * h;
    assertEqual(var22, -5.00d);
    decimal? var23 = q * i;
    assertEqual(var23, 5.00d);
    decimal? var24 = q * j;
    assertEqual(var24, -1635.00d);
    decimal? var25 = q * k;
    assertEqual(var25, 7.50d);
    decimal? var26 = q * m;
    assertEqual(var26, 12.50d);
    decimal? var27 = q * n;
    assertEqual(var27, 25.00d);
    decimal? var28 = q * p;
    assertEqual(var28, 107.50d);

    decimal? var29 = h * r;
    assertEqual(var29, -5.00d);
    decimal? var30 = i * r;
    assertEqual(var30, 5.00d);
    decimal? var31 = j * r;
    assertEqual(var31, -1635.00d);
    decimal? var32 = k * r;
    assertEqual(var32, 7.50d);
    decimal? var33 = m * r;
    assertEqual(var33, 12.50d);
    decimal? var34 = n * r;
    assertEqual(var34, 25.00d);
    decimal? var35 = p * r;
    assertEqual(var35, 107.50d);

    decimal? var36 = r * h;
    assertEqual(var36, -5.00d);
    decimal? var37 = r * i;
    assertEqual(var37, 5.00d);
    decimal? var38 = r * j;
    assertEqual(var38, -1635.00d);
    decimal? var39 = r * k;
    assertEqual(var39, 7.50d);
    decimal? var40 = r * m;
    assertEqual(var40, 12.50d);
    decimal? var41 = r * n;
    assertEqual(var41, 25.00d);
    decimal? var42 = r * p;
    assertEqual(var42, 107.50d);
}

function testResultTypeOfMultiplyDecimalIntByInfering() {
    decimal a = 2.5;
    int b = 3;

    var c = a * b;
    decimal var1 = c;
    assertEqual(var1, 7.50d);

    var d = b * a;
    decimal var2 = d;
    assertEqual(var2, 7.50d);

    var e = a * constInt;
    decimal var3 = e;
    assertEqual(var3, 12.50d);

    var f = constInt * a;
    decimal var4 = f;
    assertEqual(var4, 12.50d);

    var g = constDecimal * b;
    decimal var5 = g;
    assertEqual(var5, 61.50d);

    var h = b * constDecimal;
    decimal var6 = h;
    assertEqual(var6, 61.50d);

    var i = constDecimal * constInt;
    decimal var7 = i;
    assertEqual(var7, 102.50d);

    var j = constInt * constDecimal;
    decimal var8 = j;
    assertEqual(var8, 102.50d);
}

function testResultTypeOfMultiplyDecimalIntForNilableOperandsByInfering() {
    decimal? a = 2.5;
    int? b = 3;

    var c = a * b;
    decimal? var1 = c;
    assertEqual(var1, 7.50d);

    var d = b * a;
    decimal? var2 = d;
    assertEqual(var2, 7.50d);

    var e = a * constInt;
    decimal? var3 = e;
    assertEqual(var3, 12.50d);

    var f = constInt * a;
    decimal? var4 = f;
    assertEqual(var4, 12.50d);

    var g = constDecimal * b;
    decimal? var5 = g;
    assertEqual(var5, 61.50d);

    var h = b * constDecimal;
    decimal? var6 = h;
    assertEqual(var6, 61.50d);
}

int intVal = 10;

function testNoShortCircuitingInMultiplicationWithNullable() {
    int? result = foo() * bar();
    assertEqual(result, ());
    assertEqual(intVal, 18);

    result = foo() * 12;
    assertEqual(result, ());
    assertEqual(intVal, 20);

    result = 12 * bar();
    assertEqual(result, ());
    assertEqual(intVal, 26);

    int? x = 12;
    result = foo() * x;
    assertEqual(result, ());
    assertEqual(intVal, 28);

    result = x * bar();
    assertEqual(result, ());
    assertEqual(intVal, 34);

    result = x * bam();
    assertEqual(result, 60);
    assertEqual(intVal, 44);

    result = bam() * x;
    assertEqual(result, 60);
    assertEqual(intVal, 54);

    result = foo() * bam();
    assertEqual(result, ());
    assertEqual(intVal, 66);

    result = bam() * bar();
    assertEqual(result, ());
    assertEqual(intVal, 82);
}

function testNoShortCircuitingInMultiplicationWithNonNullable() {
    intVal = 10;
    int x = 10;

    int result = x * bam();
    assertEqual(result, 50);
    assertEqual(intVal, 20);

    result = bam() * 12;
    assertEqual(result, 60);
    assertEqual(intVal, 30);
}

function testDecimalMultiplicationUnderflow() {
    decimal d1 = 9.999999999999999999999999999999999E-6001;
    decimal d2 = 1E-143;
    test:assertTrue(d1 * d2 == 0.0d);
    test:assertTrue(d1 * d2 === 0d);
    test:assertFalse(d1 * d2 === 0.0d);

    d1 = -9.999999999999999999999999999999999E-6001;
    d2 = 1E-143;
    test:assertTrue(d1 * d2 == 0.0d);
    test:assertTrue(d1 * d2 === 0d);
    test:assertFalse(d1 * d2 === 0.00d);

    decimal d3 = -9.999999999999999999999999999999999E-6101;
    decimal d4 = 1E-76;
    test:assertTrue(d3 * d4 == 0.0d);
    test:assertTrue(d3 * d4 === 0d);
    test:assertFalse(d3 * d4 === 0.000d);

    test:assertTrue(d3 * d4 == d1 * d2);
    test:assertTrue(d3 * d4 === d1 * d2);

    d1 = 1.00100E-6120;
    d2 = 1.0E-24;
    test:assertTrue(d1 * d2 == 0.0d);
    test:assertTrue(d1 * d2 === 0d);
    test:assertFalse(d1 * d2 === 0.0d);

    d1 = 1.00100E-6120;
    d2 = 1.0E-23;
    test:assertTrue(d1 * d2 == 1.001E-6143d);
    test:assertTrue(d1 * d2 === 1.001000E-6143d);
    test:assertFalse(d1 * d2 === 1.001E-6143d);
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
