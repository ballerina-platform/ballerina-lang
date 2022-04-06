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

import ballerina/lang.'float as floats;
import ballerina/lang.test;

function testIsFinite() returns [boolean, boolean] {
    float f = 12.34;
    float inf = 1.0/0.0;
    return [f.isFinite(), inf.isFinite()];
}

function testIsInfinite() returns [boolean, boolean] {
    float f = 12.34;
    float inf = 1.0/0.0;
    return [f.isInfinite(), inf.isInfinite()];
}

function testSum() returns float {
    return floats:sum(12.34, 23.45, 34.56);
}

function testFloatConsts() returns [float,float] {
    return [floats:NaN, floats:Infinity];
}

type Floats 12f|21.0;

function testLangLibCallOnFiniteType() {
    Floats x = 21;
    float y = x.sum(1, 2.3);
    test:assertValueEqual(24.3, y);
}

function testFloatEquality() {
    test:assertTrue(42.0 == 42.0);
    test:assertFalse(1.0 == 12.0);
    test:assertTrue(float:NaN == float:NaN);
    test:assertTrue(-0.0 == 0.0);
}

function testFloatNotEquality() {
    test:assertFalse(42.0 != 42.0);
    test:assertTrue(1.0 != 12.0);
    test:assertFalse(float:NaN != float:NaN);
    test:assertFalse(-0.0 != 0.0);
}

function testFloatExactEquality() {
    test:assertTrue(42.0 === 42.0);
    test:assertFalse(1.0 === 12.0);
    test:assertTrue(float:NaN === float:NaN);
    test:assertFalse(-0.0 === 0.0);
}

function testFloatNotExactEquality() {
    test:assertFalse(42.0 !== 42.0);
    test:assertTrue(1.0 !== 12.0);
    test:assertFalse(float:NaN !== float:NaN);
    test:assertTrue(-0.0 !== 0.0);
}

function testFromHexString() {
    float|error v1 = float:fromHexString("0xa.bp1");
    test:assertValueEqual(checkpanic v1, 21.375);

    v1 = float:fromHexString("+0xa.bp1");
    test:assertValueEqual(checkpanic v1, 21.375);

    v1 = float:fromHexString("-0xa.bp1");
    test:assertValueEqual(checkpanic v1, -21.375);

    v1 = float:fromHexString("0Xa2c.b32p2");
    test:assertValueEqual(checkpanic v1, 10418.798828125);

    v1 = float:fromHexString("0Xa.b32P-5");
    test:assertValueEqual(checkpanic v1, 0.3343658447265625);

    v1 = float:fromHexString("-0x123.fp-5");
    test:assertValueEqual(checkpanic v1, -9.123046875);

    v1 = float:fromHexString("0x123fp-5");
    test:assertValueEqual(checkpanic v1, 145.96875);

    v1 = float:fromHexString("0x.ab5p2");
    test:assertValueEqual(checkpanic v1, 2.6767578125);

    v1 = float:fromHexString("0x1a");
    error err = <error> v1;
    test:assertValueEqual(err.message(), "{ballerina/lang.float}NumberParsingError");
    test:assertValueEqual(<string> checkpanic err.detail()["message"], "For input string: \"0x1a\"");

    v1 = float:fromHexString("NaN");
    test:assertValueEqual(checkpanic v1, float:NaN);

    v1 = float:fromHexString("+Infinity");
    test:assertValueEqual(checkpanic v1, float:Infinity);

    v1 = float:fromHexString("-Infinity");
    test:assertValueEqual(checkpanic v1, -float:Infinity);

    v1 = float:fromHexString("Infinity");
    test:assertValueEqual(checkpanic v1, float:Infinity);

    v1 = float:fromHexString("AInvalidNum");
    err = <error> v1;
    test:assertValueEqual(err.message(), "{ballerina/lang.float}NumberParsingError");
    test:assertValueEqual(<string> checkpanic err.detail()["message"], "invalid hex string: 'AInvalidNum'");

    v1 = float:fromHexString("12.3");
    err = <error> v1;
    test:assertValueEqual(err.message(), "{ballerina/lang.float}NumberParsingError");
    test:assertValueEqual(<string> checkpanic err.detail()["message"], "invalid hex string: '12.3'");

    v1 = float:fromHexString("1");
    err = <error> v1;
    test:assertValueEqual(err.message(), "{ballerina/lang.float}NumberParsingError");
    test:assertValueEqual(<string> checkpanic err.detail()["message"], "invalid hex string: '1'");

    v1 = float:fromHexString("+1");
    err = <error> v1;
    test:assertValueEqual(err.message(), "{ballerina/lang.float}NumberParsingError");
    test:assertValueEqual(<string> checkpanic err.detail()["message"], "invalid hex string: '+1'");

    v1 = float:fromHexString("-1");
    err = <error> v1;
    test:assertValueEqual(err.message(), "{ballerina/lang.float}NumberParsingError");
    test:assertValueEqual(<string> checkpanic err.detail()["message"], "invalid hex string: '-1'");

    v1 = float:fromHexString("2A");
    err = <error> v1;
    test:assertValueEqual(err.message(), "{ballerina/lang.float}NumberParsingError");
    test:assertValueEqual(<string> checkpanic err.detail()["message"], "invalid hex string: '2A'");

    v1 = float:fromHexString("0x");
    err = <error> v1;
    test:assertValueEqual(err.message(), "{ballerina/lang.float}NumberParsingError");
    test:assertValueEqual(<string> checkpanic err.detail()["message"], "invalid hex string: '0x'");

    v1 = float:fromHexString("0i");
    err = <error> v1;
    test:assertValueEqual(err.message(), "{ballerina/lang.float}NumberParsingError");
    test:assertValueEqual(<string> checkpanic err.detail()["message"], "invalid hex string: '0i'");

    v1 = float:fromHexString("0i123");
    err = <error> v1;
    test:assertValueEqual(err.message(), "{ballerina/lang.float}NumberParsingError");
    test:assertValueEqual(<string> checkpanic err.detail()["message"], "invalid hex string: '0i123'");

    v1 = float:fromHexString("+inf");
    err = <error> v1;
    test:assertValueEqual(err.message(), "{ballerina/lang.float}NumberParsingError");
    test:assertValueEqual(<string> checkpanic err.detail()["message"], "invalid hex string: '+inf'");
}

function testMinAndMaxWithNaN() {
    float a = float:max(1, float:NaN);
    test:assertTrue(a === float:NaN);

    float b = float:min(5, float:NaN);
    test:assertTrue(b === float:NaN);
}

function testFromStringPositive() {
    float|error a1 = float:fromString("123");
    assertEquality(true, a1 is float);
    assertEquality(123.0, a1);

    a1 = float:fromString("-123");
    assertEquality(true, a1 is float);
    assertEquality(-123.0, a1);

    a1 = float:fromString("123.0");
    assertEquality(true, a1 is float);
    assertEquality(123.0, a1);

    a1 = float:fromString("-123.0");
    assertEquality(true, a1 is float);
    assertEquality(-123.0, a1);

    a1 = float:fromString("12E+2");
    assertEquality(true, a1 is float);
    assertEquality(1200.0, a1);

    a1 = float:fromString("-12E+2");
    assertEquality(true, a1 is float);
    assertEquality(-1200.0, a1);

    a1 = float:fromString("12e-2");
    assertEquality(true, a1 is float);
    assertEquality(0.12, a1);

    a1 = float:fromString("-12e-2");
    assertEquality(true, a1 is float);
    assertEquality(-0.12, a1);

    a1 = float:fromString("12.23E+2");
    assertEquality(true, a1 is float);
    assertEquality(1223.0, a1);

    a1 = float:fromString("-12.23E+2");
    assertEquality(true, a1 is float);
    assertEquality(-1223.0, a1);

    a1 = float:fromString("12.23e-2");
    assertEquality(true, a1 is float);
    assertEquality(0.1223, a1);

    a1 = float:fromString("-12.23e-2");
    assertEquality(true, a1 is float);
    assertEquality(-0.1223, a1);

    a1 = float:fromString("+12.23E+2");
    assertEquality(true, a1 is float);
    assertEquality(1223.0, a1);

    a1 = float:fromString("+12.23e-2");
    assertEquality(true, a1 is float);
    assertEquality(0.1223, a1);

    a1 = float:fromString("+123.0");
    assertEquality(true, a1 is float);
    assertEquality(123.0, a1);

    a1 = float:fromString("NaN");
    assertEquality(true, a1 is float);
    assertEquality(float:NaN, a1);

    a1 = float:fromString("+Infinity");
    assertEquality(true, a1 is float);
    assertEquality(float:Infinity, a1);

    a1 = float:fromString("-Infinity");
    assertEquality(true, a1 is float);
    assertEquality(-float:Infinity, a1);

    a1 = float:fromString("Infinity");
    assertEquality(true, a1 is float);
    assertEquality(float:Infinity, a1);
}

function testFromStringNegative() {
    float|error a1 = float:fromString("123f");
    assertEquality(true, a1 is error);
    if (a1 is error) {
        assertEquality("{ballerina/lang.float}NumberParsingError", a1.message());
        assertEquality("'string' value '123f' cannot be converted to 'float'", <string> checkpanic a1.detail()["message"]);
    }

    a1 = float:fromString("123F");
    assertEquality(true, a1 is error);
    if (a1 is error) {
        assertEquality("{ballerina/lang.float}NumberParsingError", a1.message());
        assertEquality("'string' value '123F' cannot be converted to 'float'", <string> checkpanic a1.detail()["message"]);
    }

    a1 = float:fromString("123.67f");
    assertEquality(true, a1 is error);
    if (a1 is error) {
        assertEquality("{ballerina/lang.float}NumberParsingError", a1.message());
        assertEquality("'string' value '123.67f' cannot be converted to 'float'", <string> checkpanic a1.detail()["message"]);
    }

    a1 = float:fromString("123.67F");
    assertEquality(true, a1 is error);
    if (a1 is error) {
        assertEquality("{ballerina/lang.float}NumberParsingError", a1.message());
        assertEquality("'string' value '123.67F' cannot be converted to 'float'", <string> checkpanic a1.detail()["message"]);
    }

    a1 = float:fromString("12E+2f");
    assertEquality(true, a1 is error);
    if (a1 is error) {
        assertEquality("{ballerina/lang.float}NumberParsingError", a1.message());
        assertEquality("'string' value '12E+2f' cannot be converted to 'float'", <string> checkpanic a1.detail()["message"]);
    }

    a1 = float:fromString("-12E+2F");
    assertEquality(true, a1 is error);
    if (a1 is error) {
        assertEquality("{ballerina/lang.float}NumberParsingError", a1.message());
        assertEquality("'string' value '-12E+2F' cannot be converted to 'float'", <string> checkpanic a1.detail()["message"]);
    }

    a1 = float:fromString("12e-2F");
    assertEquality(true, a1 is error);
    if (a1 is error) {
        assertEquality("{ballerina/lang.float}NumberParsingError", a1.message());
        assertEquality("'string' value '12e-2F' cannot be converted to 'float'", <string> checkpanic a1.detail()["message"]);
    }

    a1 = float:fromString("-12e-2f");
    assertEquality(true, a1 is error);
    if (a1 is error) {
        assertEquality("{ballerina/lang.float}NumberParsingError", a1.message());
        assertEquality("'string' value '-12e-2f' cannot be converted to 'float'", <string> checkpanic a1.detail()["message"]);
    }

    a1 = float:fromString("12.23E+2F");
    assertEquality(true, a1 is error);
    if (a1 is error) {
        assertEquality("{ballerina/lang.float}NumberParsingError", a1.message());
        assertEquality("'string' value '12.23E+2F' cannot be converted to 'float'", <string> checkpanic a1.detail()["message"]);
    }

    a1 = float:fromString("-12.23E+2f");
    assertEquality(true, a1 is error);
    if (a1 is error) {
        assertEquality("{ballerina/lang.float}NumberParsingError", a1.message());
        assertEquality("'string' value '-12.23E+2f' cannot be converted to 'float'", <string> checkpanic a1.detail()["message"]);
    }

    a1 = float:fromString("12.23e-2f");
    assertEquality(true, a1 is error);
    if (a1 is error) {
        assertEquality("{ballerina/lang.float}NumberParsingError", a1.message());
        assertEquality("'string' value '12.23e-2f' cannot be converted to 'float'", <string> checkpanic a1.detail()["message"]);
    }

    a1 = float:fromString("-12.23e-2F");
    assertEquality(true, a1 is error);
    if (a1 is error) {
        assertEquality("{ballerina/lang.float}NumberParsingError", a1.message());
        assertEquality("'string' value '-12.23e-2F' cannot be converted to 'float'", <string> checkpanic a1.detail()["message"]);
    }

    a1 = float:fromString("+12.23E+2F");
    assertEquality(true, a1 is error);
    if (a1 is error) {
        assertEquality("{ballerina/lang.float}NumberParsingError", a1.message());
        assertEquality("'string' value '+12.23E+2F' cannot be converted to 'float'", <string> checkpanic a1.detail()["message"]);
    }

    a1 = float:fromString("+12.23e-2f");
    assertEquality(true, a1 is error);
    if (a1 is error) {
        assertEquality("{ballerina/lang.float}NumberParsingError", a1.message());
        assertEquality("'string' value '+12.23e-2f' cannot be converted to 'float'", <string> checkpanic a1.detail()["message"]);
    }

    a1 = float:fromString("+123.0f");
    assertEquality(true, a1 is error);
    if (a1 is error) {
        assertEquality("{ballerina/lang.float}NumberParsingError", a1.message());
        assertEquality("'string' value '+123.0f' cannot be converted to 'float'", <string> checkpanic a1.detail()["message"]);
    }

    a1 = float:fromString("12d");
    assertEquality(true, a1 is error);
    if (a1 is error) {
        assertEquality("{ballerina/lang.float}NumberParsingError", a1.message());
        assertEquality("'string' value '12d' cannot be converted to 'float'", <string> checkpanic a1.detail()["message"]);
    }

     a1 = float:fromString("12D");
     assertEquality(true, a1 is error);
     if (a1 is error) {
         assertEquality("{ballerina/lang.float}NumberParsingError", a1.message());
         assertEquality("'string' value '12D' cannot be converted to 'float'", <string> checkpanic a1.detail()["message"]);
     }

    a1 = float:fromString("12.23E+2D");
    assertEquality(true, a1 is error);
    if (a1 is error) {
        assertEquality("{ballerina/lang.float}NumberParsingError", a1.message());
        assertEquality("'string' value '12.23E+2D' cannot be converted to 'float'", <string> checkpanic a1.detail()["message"]);
    }

    a1 = float:fromString("-12.23e-2d");
    assertEquality(true, a1 is error);
    if (a1 is error) {
        assertEquality("{ballerina/lang.float}NumberParsingError", a1.message());
        assertEquality("'string' value '-12.23e-2d' cannot be converted to 'float'", <string> checkpanic a1.detail()["message"]);
    }

    a1 = float:fromString("0xabcf");
    assertEquality(true, a1 is error);
    if (a1 is error) {
        assertEquality("{ballerina/lang.float}NumberParsingError", a1.message());
        assertEquality("'string' value '0xabcf' cannot be converted to 'float'", <string> checkpanic a1.detail()["message"]);
    }

    a1 = float:fromString("-0xabcf");
    assertEquality(true, a1 is error);
    if (a1 is error) {
        assertEquality("{ballerina/lang.float}NumberParsingError", a1.message());
        assertEquality("'string' value '-0xabcf' cannot be converted to 'float'", <string> checkpanic a1.detail()["message"]);
    }

    a1 = float:fromString("0Xabcf");
    assertEquality(true, a1 is error);
    if (a1 is error) {
        assertEquality("{ballerina/lang.float}NumberParsingError", a1.message());
        assertEquality("'string' value '0Xabcf' cannot be converted to 'float'", <string> checkpanic a1.detail()["message"]);
    }

    a1 = float:fromString("-0Xabcf");
    assertEquality(true, a1 is error);
    if (a1 is error) {
        assertEquality("{ballerina/lang.float}NumberParsingError", a1.message());
        assertEquality("'string' value '-0Xabcf' cannot be converted to 'float'", <string> checkpanic a1.detail()["message"]);
    }
}

const float f1 = 5.7;

type FloatType 5.7;

type FloatType2 5.7|2.8;

type FloatType3 float;

function testToFixedStringWithPositiveFloat() {
    float a = 5.7;
    string b = float:toFixedString(a, 16);
    assertEquality("5.7000000000000002", b);
    assertEquality("5.70", float:toFixedString(a, 2));
    assertEquality("5.700000000000000", float:toFixedString(a, 15));
    assertEquality("5.70000000", float:toFixedString(a, 8));
    assertEquality("5.7000", float:toFixedString(a, 4));

    string c = a.toFixedString(16);
    assertEquality("5.7000000000000002", c);
    assertEquality("5.70", a.toFixedString(2));
    assertEquality("5.700000000000000", a.toFixedString(15));
    assertEquality("5.70000000", a.toFixedString(8));
    assertEquality("5.7000", a.toFixedString(4));

    5.7 d = 5.7;
    assertEquality("5.7000000000000002", d.toFixedString(16));
    assertEquality("5.70", d.toFixedString(2));
    assertEquality("5.700000000000000", d.toFixedString(15));
    assertEquality("5.70000000", d.toFixedString(8));
    assertEquality("5.7000", d.toFixedString(4));

    assertEquality("5.7000000000000002", f1.toFixedString(16));
    assertEquality("5.70", f1.toFixedString(2));
    assertEquality("5.700000000000000", f1.toFixedString(15));
    assertEquality("5.70000000", f1.toFixedString(8));
    assertEquality("5.7000", f1.toFixedString(4));

    FloatType e = 5.7;
    assertEquality("5.7000000000000002", e.toFixedString(16));
    assertEquality("5.70", e.toFixedString(2));
    assertEquality("5.700000000000000", e.toFixedString(15));
    assertEquality("5.70000000", e.toFixedString(8));
    assertEquality("5.7000", e.toFixedString(4));

    FloatType2 f = 5.7;
    assertEquality("5.7000000000000002", f.toFixedString(16));
    assertEquality("5.70", f.toFixedString(2));
    assertEquality("5.700000000000000", f.toFixedString(15));
    assertEquality("5.70000000", f.toFixedString(8));
    assertEquality("5.7000", f.toFixedString(4));

    FloatType3 g = 5.7;
    assertEquality("5.7000000000000002", float:toFixedString(g, 16));
    assertEquality("5.70", float:toFixedString(g, 2));
    assertEquality("5.700000000000000", float:toFixedString(g, 15));
    assertEquality("5.70000000", float:toFixedString(g, 8));
    assertEquality("5.7000", float:toFixedString(g, 4));
}

const float f2 = -5.7;

type FloatType4 -5.7;

type FloatType5 -5.7|2.8;

function testToFixedStringWithNegativeFloat() {
    float a = -5.7;
    string b = float:toFixedString(a, 16);
    assertEquality("-5.7000000000000002", b);
    assertEquality("-5.70", float:toFixedString(a, 2));
    assertEquality("-5.700000000000000", float:toFixedString(a, 15));
    assertEquality("-5.70000000", float:toFixedString(a, 8));
    assertEquality("-5.7000", float:toFixedString(a, 4));

    string c = a.toFixedString(16);
    assertEquality("-5.7000000000000002", c);
    assertEquality("-5.70", a.toFixedString(2));
    assertEquality("-5.700000000000000", a.toFixedString(15));
    assertEquality("-5.70000000", a.toFixedString(8));
    assertEquality("-5.7000", a.toFixedString(4));

    -5.7 d = -5.7;
    assertEquality("-5.7000000000000002", d.toFixedString(16));
    assertEquality("-5.70", d.toFixedString(2));
    assertEquality("-5.700000000000000", d.toFixedString(15));
    assertEquality("-5.70000000", d.toFixedString(8));
    assertEquality("-5.7000", d.toFixedString(4));

    assertEquality("-5.7000000000000002", f2.toFixedString(16));
    assertEquality("-5.70", f2.toFixedString(2));
    assertEquality("-5.700000000000000", f2.toFixedString(15));
    assertEquality("-5.70000000", f2.toFixedString(8));
    assertEquality("-5.7000", f2.toFixedString(4));

    FloatType4 e = -5.7;
    assertEquality("-5.7000000000000002", e.toFixedString(16));
    assertEquality("-5.70", e.toFixedString(2));
    assertEquality("-5.700000000000000", e.toFixedString(15));
    assertEquality("-5.70000000", e.toFixedString(8));
    assertEquality("-5.7000", e.toFixedString(4));

    FloatType5 f = -5.7;
    assertEquality("-5.7000000000000002", f.toFixedString(16));
    assertEquality("-5.70", f.toFixedString(2));
    assertEquality("-5.700000000000000", f.toFixedString(15));
    assertEquality("-5.70000000", f.toFixedString(8));
    assertEquality("-5.7000", f.toFixedString(4));

    FloatType3 g = -5.7;
    assertEquality("-5.7000000000000002", float:toFixedString(g, 16));
    assertEquality("-5.70", float:toFixedString(g, 2));
    assertEquality("-5.700000000000000", float:toFixedString(g, 15));
    assertEquality("-5.70000000", float:toFixedString(g, 8));
    assertEquality("-5.7000", float:toFixedString(g, 4));
}

function testToFixedStringWithInfinity() {
    assertEquality("Infinity", float:toFixedString(float:Infinity, 16));
    assertEquality("Infinity", float:toFixedString(float:Infinity, 8));
    assertEquality("Infinity", float:toFixedString(float:Infinity, 4));
    assertEquality("Infinity", float:toFixedString(float:Infinity, 2));

    assertEquality("Infinity", float:Infinity.toFixedString(16));
    assertEquality("Infinity", float:Infinity.toFixedString(8));
    assertEquality("Infinity", float:Infinity.toFixedString(4));
    assertEquality("Infinity", float:Infinity.toFixedString(2));
}

function testToFixedStringWithNaN() {
    assertEquality("NaN", float:toFixedString(float:NaN, 16));
    assertEquality("NaN", float:toFixedString(float:NaN, 8));
    assertEquality("NaN", float:toFixedString(float:NaN, 4));
    assertEquality("NaN", float:toFixedString(float:NaN, 2));

    assertEquality("NaN", float:NaN.toFixedString(16));
    assertEquality("NaN", float:NaN.toFixedString(8));
    assertEquality("NaN", float:NaN.toFixedString(4));
    assertEquality("NaN", float:NaN.toFixedString(2));
}

function testToFixedStringWhenFractionDigitsIsLessThanZero() {
    float a = 5.7;

    string|error b = trap a.toFixedString(-2);
    assertEquality(true, b is error);
    if (b is error) {
        assertEquality("{ballerina/lang.float}InvalidFractionDigits", b.message());
        assertEquality("fraction digits cannot be less than 0", <string> checkpanic b.detail()["message"]);
    }

    b = trap float:toFixedString(a, -2);
    assertEquality(true, b is error);
    if (b is error) {
        assertEquality("{ballerina/lang.float}InvalidFractionDigits", b.message());
        assertEquality("fraction digits cannot be less than 0", <string> checkpanic b.detail()["message"]);
    }

    b = trap float:toFixedString(a, -(+2));
    assertEquality(true, b is error);
    if (b is error) {
        assertEquality("{ballerina/lang.float}InvalidFractionDigits", b.message());
        assertEquality("fraction digits cannot be less than 0", <string> checkpanic b.detail()["message"]);
    }

    b = trap float:toFixedString(a, ~2);
    assertEquality(true, b is error);
    if (b is error) {
        assertEquality("{ballerina/lang.float}InvalidFractionDigits", b.message());
        assertEquality("fraction digits cannot be less than 0", <string> checkpanic b.detail()["message"]);
    }
}

function testToFixedStringWhenFractionDigitsIsZero() {
    float a = 5.7;
    string b = float:toFixedString(a, 0);
    assertEquality("6", b);
    assertEquality("6", a.toFixedString(0));

    float c = -5.7;
    string d = float:toFixedString(c, 0);
    assertEquality("-6", d);
    assertEquality("-6", c.toFixedString(0));
}

function testToFixedStringWhenFractionDigitsIsNil() {
    float a = 5.7;
    string b = float:toFixedString(a, ());
    assertEquality("5.7", b);
    assertEquality("5.7", a.toFixedString(()));

    float c = -5.7;
    string d = float:toFixedString(c, ());
    assertEquality("-5.7", d);
    assertEquality("-5.7", c.toFixedString(()));

    assertEquality("12321321312321", float:toFixedString(12321321312321, ()));
    assertEquality("12321321312321.123", float:toFixedString(12321321312321.122432123123, ()));
}

function testToFixedStringWhenFractionDigitsIsVeryLargeInt() {
    float a = 5.7;
    string b = float:toFixedString(a, 2147483648);
    assertEquality("5.7", b);
    assertEquality("5.7", a.toFixedString(2147483647 + 1));

    float c = -5.7;
    string d = float:toFixedString(c, 2147483648 + 1);
    assertEquality("-5.7", d);
    assertEquality("-5.7", c.toFixedString(2147483647 + 1));
}

function testToFixedStringWhenFractionDigitsIsIntMax() {
    float a = 5.7;
    string b = float:toFixedString(a, 9223372036854775807);
    assertEquality("5.7", b);
    assertEquality("5.7", a.toFixedString(int:MAX_VALUE));

    float c = -5.7;
    string d = float:toFixedString(c, 9223372036854775807);
    assertEquality("-5.7", d);
    assertEquality("-5.7", c.toFixedString(int:MAX_VALUE));
}

function testToFixedStringWithMorePositiveFloats() {
    assertEquality("45362.12", float:toFixedString(45362.12364, 2));
    assertEquality("45362.124", float:toFixedString(45362.12364, 3));
    assertEquality("45362.1233399999982794", float:toFixedString(45362.12334, 16));
    assertEquality("0.12", float:toFixedString(0.1237469219, 2));
    assertEquality("0.124", float:toFixedString(0.1237469219, 3));
    assertEquality("0.1237469219000000", float:toFixedString(0.1237469219, 16));
    assertEquality("0.12374692", float:toFixedString(0.1237469219, 8));
    assertEquality("0.012", float:toFixedString(0.0123654, 3));
    assertEquality("0.0124", float:toFixedString(0.0123654, 4));
    assertEquality("0.01236540", float:toFixedString(0.0123654, 8));
    assertEquality("0.01237", float:toFixedString(0.0123654, 5));
    assertEquality("0.000000056", float:toFixedString(0.0000000564, 9));
    assertEquality("0.00000006", float:toFixedString(0.0000000564, 8));
    assertEquality("0.0000001", float:toFixedString(0.0000000564, 7));
    assertEquality("0.00000", float:toFixedString(0, 5));
    assertEquality("0.000000000000000", float:toFixedString(0, 15));

    assertEquality("45362.1233399999982794", float:toFixedString(45362.12334f, 16));
    assertEquality("0.1237469219000000", float:toFixedString(0.1237469219f, 16));
    assertEquality("0.12374692", float:toFixedString(0.1237469219F, 8));
    assertEquality("0.0124", float:toFixedString(0.0123654f, 4));
    assertEquality("0.01236540", float:toFixedString(0.0123654F, 8));
    assertEquality("0.01237", float:toFixedString(0.0123654f, 5));
    assertEquality("0.00000006", float:toFixedString(0.0000000564f, 8));
    assertEquality("0.0000001", float:toFixedString(0.0000000564F, 7));
    assertEquality("0.00000", float:toFixedString(0f, 5));
    assertEquality("0.000000000000000", float:toFixedString(0F, 15));

    assertEquality("12321321312321", float:toFixedString(12321321312321, 11431241242));
    assertEquality("12321321312321.123", float:toFixedString(12321321312321.122432123123, 11431241242));
}

function testToFixedStringWithVerySmallAndLargePositiveFloats() {
    float a = 2.5E-17;
    assertEquality("0.00000000000000002", float:toFixedString(a, 17));
    assertEquality("0.0000000000", float:toFixedString(a, 10));
    assertEquality("0.0000", float:toFixedString(a, 4));

    float b = 2.5E+17;
    assertEquality("250000000000000000.00000000000000000", float:toFixedString(b, 17));
    assertEquality("250000000000000000.0000000000", float:toFixedString(b, 10));
    assertEquality("250000000000000000.0000", float:toFixedString(b, 4));

    float c = 1.423223E6;
    assertEquality("1423223.00000000000000000", c.toFixedString(17));
    assertEquality("1423223.0000000000", c.toFixedString(10));
    assertEquality("1423223.0000", c.toFixedString(4));

    float d = 0.9E-10f;
    assertEquality("0.00000000009000000", d.toFixedString(17));
    assertEquality("0.000000000090", d.toFixedString(12));
    assertEquality("0.0000", d.toFixedString(4));
}

function testToFixedStringWithMoreNegativeFloats() {
    assertEquality("-45362.1233399999982794", float:toFixedString(-45362.12334, 16));
    assertEquality("-0.1237469219000000", float:toFixedString(-0.1237469219, 16));
    assertEquality("-0.12374692", float:toFixedString(-0.1237469219, 8));
    assertEquality("-0.0124", float:toFixedString(-0.0123654, 4));
    assertEquality("-0.01236540", float:toFixedString(-0.0123654, 8));
    assertEquality("-0.01237", float:toFixedString(-0.0123654, 5));
    assertEquality("-0.00000006", float:toFixedString(-0.0000000564, 8));
    assertEquality("-0.0000001", float:toFixedString(-0.0000000564, 7));
    assertEquality("0.00000", float:toFixedString(-0, 5));
    assertEquality("0.000000000000000", float:toFixedString(-0, 15));

    assertEquality("-45362.1233399999982794", float:toFixedString(-45362.12334f, 16));
    assertEquality("-0.1237469219000000", float:toFixedString(-0.1237469219f, 16));
    assertEquality("-0.12374692", float:toFixedString(-0.1237469219F, 8));
    assertEquality("-0.0124", float:toFixedString(-0.0123654F, 4));
    assertEquality("-0.01236540", float:toFixedString(-0.0123654f, 8));
    assertEquality("-0.01237", float:toFixedString(-0.0123654f, 5));
    assertEquality("-0.00000006", float:toFixedString(-0.0000000564F, 8));
    assertEquality("-0.0000001", float:toFixedString(-0.0000000564F, 7));
    assertEquality("0.00000", float:toFixedString(-0f, 5));
    assertEquality("0.000000000000000", float:toFixedString(-0F, 15));
}

function testToFixedStringWithVerySmallAndLargeNegativeFloats() {
    float a = -2.5E-17;
    assertEquality("-0.00000000000000002", float:toFixedString(a, 17));
    assertEquality("0.0000000000", float:toFixedString(a, 10));
    assertEquality("0.0000", float:toFixedString(a, 4));

    float b = -2.5E+17;
    assertEquality("-250000000000000000.00000000000000000", float:toFixedString(b, 17));
    assertEquality("-250000000000000000.0000000000", float:toFixedString(b, 10));
    assertEquality("-250000000000000000.0000", float:toFixedString(b, 4));

    float c = -1.423223E6;
    assertEquality("-1423223.00000000000000000", c.toFixedString(17));
    assertEquality("-1423223.0000000000", c.toFixedString(10));
    assertEquality("-1423223.0000", c.toFixedString(4));

    float d = -0.9E-10f;
    assertEquality("-0.00000000009000000", d.toFixedString(17));
    assertEquality("-0.000000000090", d.toFixedString(12));
    assertEquality("0.0000", d.toFixedString(4));
}

function testToFixedStringWithHexaDecimalFloatingPoints() {
    float a = 0xabcff;
    assertEquality("703743.00000000000000000", a.toFixedString(17));
    assertEquality("703743.0000000000", a.toFixedString(10));
    assertEquality("703743.0000", a.toFixedString(4));

    float b = 0xAB126fa;
    assertEquality("179382010.00000000000000000", b.toFixedString(17));
    assertEquality("179382010.0000000000", b.toFixedString(10));
    assertEquality("179382010.0000", b.toFixedString(4));

    a = -0xabcff;
    assertEquality("-703743.00000000000000000", a.toFixedString(17));
    assertEquality("-703743.0000000000", a.toFixedString(10));
    assertEquality("-703743.0000", a.toFixedString(4));

    b = -0xAB126fa;
    assertEquality("-179382010.00000000000000000", b.toFixedString(17));
    assertEquality("-179382010.0000000000", b.toFixedString(10));
    assertEquality("-179382010.0000", b.toFixedString(4));

    a = 0x1.e412904862198p4;
    assertEquality("30.25453213000000119", a.toFixedString(17));
    assertEquality("30.2545321300", a.toFixedString(10));
    assertEquality("30.2545", a.toFixedString(4));

    b = 0x1.cbe3p17;
    assertEquality("235462.00000000000000000", b.toFixedString(17));
    assertEquality("235462.0000000000", b.toFixedString(10));
    assertEquality("235462.0000", b.toFixedString(4));

    a = -0x1.e412904862198p4;
    assertEquality("-30.25453213000000119", a.toFixedString(17));
    assertEquality("-30.2545321300", a.toFixedString(10));
    assertEquality("-30.2545", a.toFixedString(4));

    b = -0x1.cbe3p17;
    assertEquality("-235462.00000000000000000", b.toFixedString(17));
    assertEquality("-235462.0000000000", b.toFixedString(10));
    assertEquality("-235462.0000", b.toFixedString(4));
}

const float f3 = 45362.12334;

type FloatType6 45362.12334;

type FloatType7 45362.12334|2.85743;

function testToExpStringWithPositiveFloat() {
    float a = 45362.12334;
    string b = float:toExpString(a, 16);
    assertEquality("4.5362123339999998e+4", b);
    assertEquality("4.54e+4", float:toExpString(a, 2));
    assertEquality("4.536212334000000e+4", float:toExpString(a, 15));
    assertEquality("4.53621233e+4", float:toExpString(a, 8));
    assertEquality("4.5362e+4", float:toExpString(a, 4));

    string c = a.toExpString(16);
    assertEquality("4.5362123339999998e+4", c);
    assertEquality("4.54e+4", a.toExpString(2));
    assertEquality("4.536212334000000e+4", a.toExpString(15));
    assertEquality("4.53621233e+4", a.toExpString(8));
    assertEquality("4.5362e+4", a.toExpString(4));

    45362.12334 d = 45362.12334;
    assertEquality("4.5362123339999998e+4", d.toExpString(16));
    assertEquality("4.54e+4", d.toExpString(2));
    assertEquality("4.536212334000000e+4", d.toExpString(15));
    assertEquality("4.53621233e+4", d.toExpString(8));
    assertEquality("4.5362e+4", d.toExpString(4));

    assertEquality("4.5362123339999998e+4", f3.toExpString(16));
    assertEquality("4.54e+4", f3.toExpString(2));
    assertEquality("4.536212334000000e+4", f3.toExpString(15));
    assertEquality("4.53621233e+4", f3.toExpString(8));
    assertEquality("4.5362e+4", f3.toExpString(4));

    FloatType6 e = 45362.12334;
    assertEquality("4.5362123339999998e+4", f3.toExpString(16));
    assertEquality("4.54e+4", f3.toExpString(2));
    assertEquality("4.536212334000000e+4", f3.toExpString(15));
    assertEquality("4.53621233e+4", f3.toExpString(8));
    assertEquality("4.5362e+4", f3.toExpString(4));

    FloatType7 f = 45362.12334;
    assertEquality("4.5362123339999998e+4", f.toExpString(16));
    assertEquality("4.54e+4", f.toExpString(2));
    assertEquality("4.536212334000000e+4", f.toExpString(15));
    assertEquality("4.53621233e+4", f.toExpString(8));
    assertEquality("4.5362e+4", f.toExpString(4));

    FloatType3 g = 45362.12334;
    assertEquality("4.5362123339999998e+4", float:toExpString(g, 16));
    assertEquality("4.54e+4", float:toExpString(g, 2));
    assertEquality("4.536212334000000e+4", float:toExpString(g, 15));
    assertEquality("4.53621233e+4", float:toExpString(g, 8));
    assertEquality("4.5362e+4", float:toExpString(g, 4));
}

const float f4 = -45362.12334;

type FloatType8 -45362.12334;

type FloatType9 -45362.12334|-2.85743;

function testToExpStringWithNegativeFloat() {
    float a = -45362.12334;
    string b = float:toExpString(a, 16);
    assertEquality("-4.5362123339999998e+4", b);
    assertEquality("-4.54e+4", float:toExpString(a, 2));
    assertEquality("-4.536212334000000e+4", float:toExpString(a, 15));
    assertEquality("-4.53621233e+4", float:toExpString(a, 8));
    assertEquality("-4.5362e+4", float:toExpString(a, 4));

    string c = a.toExpString(16);
    assertEquality("-4.5362123339999998e+4", c);
    assertEquality("-4.54e+4", a.toExpString(2));
    assertEquality("-4.536212334000000e+4", a.toExpString(15));
    assertEquality("-4.53621233e+4", a.toExpString(8));
    assertEquality("-4.5362e+4", a.toExpString(4));

    -45362.12334 d = -45362.12334;
    assertEquality("-4.5362123339999998e+4", d.toExpString(16));
    assertEquality("-4.54e+4", d.toExpString(2));
    assertEquality("-4.536212334000000e+4", d.toExpString(15));
    assertEquality("-4.53621233e+4", d.toExpString(8));
    assertEquality("-4.5362e+4", d.toExpString(4));

    assertEquality("-4.5362123339999998e+4", f4.toExpString(16));
    assertEquality("-4.54e+4", f4.toExpString(2));
    assertEquality("-4.536212334000000e+4", f4.toExpString(15));
    assertEquality("-4.53621233e+4", f4.toExpString(8));
    assertEquality("-4.5362e+4", f4.toExpString(4));

    FloatType8 e = -45362.12334;
    assertEquality("-4.5362123339999998e+4", f4.toExpString(16));
    assertEquality("-4.54e+4", f4.toExpString(2));
    assertEquality("-4.536212334000000e+4", f4.toExpString(15));
    assertEquality("-4.53621233e+4", f4.toExpString(8));
    assertEquality("-4.5362e+4", f4.toExpString(4));

    FloatType9 f = -45362.12334;
    assertEquality("-4.5362123339999998e+4", f.toExpString(16));
    assertEquality("-4.54e+4", f.toExpString(2));
    assertEquality("-4.536212334000000e+4", f.toExpString(15));
    assertEquality("-4.53621233e+4", f.toExpString(8));
    assertEquality("-4.5362e+4", f.toExpString(4));

    FloatType3 g = -45362.12334;
    assertEquality("-4.5362123339999998e+4", float:toExpString(g, 16));
    assertEquality("-4.54e+4", float:toExpString(g, 2));
    assertEquality("-4.536212334000000e+4", float:toExpString(g, 15));
    assertEquality("-4.53621233e+4", float:toExpString(g, 8));
    assertEquality("-4.5362e+4", float:toExpString(g, 4));
}

function testToExpStringWithInfinity() {
    assertEquality("Infinity", float:toExpString(float:Infinity, 16));
    assertEquality("Infinity", float:toExpString(float:Infinity, 8));
    assertEquality("Infinity", float:toExpString(float:Infinity, 4));
    assertEquality("Infinity", float:toExpString(float:Infinity, 2));

    assertEquality("Infinity", float:Infinity.toExpString(16));
    assertEquality("Infinity", float:Infinity.toExpString(8));
    assertEquality("Infinity", float:Infinity.toExpString(4));
    assertEquality("Infinity", float:Infinity.toExpString(2));
}

function testToExpStringWithNaN() {
    assertEquality("NaN", float:toExpString(float:NaN, 16));
    assertEquality("NaN", float:toExpString(float:NaN, 8));
    assertEquality("NaN", float:toExpString(float:NaN, 4));
    assertEquality("NaN", float:toExpString(float:NaN, 2));

    assertEquality("NaN", float:NaN.toExpString(16));
    assertEquality("NaN", float:NaN.toExpString(8));
    assertEquality("NaN", float:NaN.toExpString(4));
    assertEquality("NaN", float:NaN.toExpString(2));
}

function testToExpStringWhenFractionDigitsIsLessThanZero() {
    float a = 5.7;

    string|error b = trap a.toExpString(-2);
    assertEquality(true, b is error);
    if (b is error) {
        assertEquality("{ballerina/lang.float}InvalidFractionDigits", b.message());
        assertEquality("fraction digits cannot be less than 0", <string> checkpanic b.detail()["message"]);
    }

    b = trap float:toExpString(a, -2);
    assertEquality(true, b is error);
    if (b is error) {
        assertEquality("{ballerina/lang.float}InvalidFractionDigits", b.message());
        assertEquality("fraction digits cannot be less than 0", <string> checkpanic b.detail()["message"]);
    }

    b = trap float:toExpString(a, -(+2));
    assertEquality(true, b is error);
    if (b is error) {
        assertEquality("{ballerina/lang.float}InvalidFractionDigits", b.message());
        assertEquality("fraction digits cannot be less than 0", <string> checkpanic b.detail()["message"]);
    }

    b = trap float:toExpString(a, ~2);
    assertEquality(true, b is error);
    if (b is error) {
        assertEquality("{ballerina/lang.float}InvalidFractionDigits", b.message());
        assertEquality("fraction digits cannot be less than 0", <string> checkpanic b.detail()["message"]);
    }
}

function testToExpStringWhenFractionDigitsIsZero() {
    float a = 45362.12334;
    string b = float:toExpString(a, 0);
    assertEquality("5e+4", b);
    assertEquality("5e+4", a.toExpString(0));

    float c = -45362.12334;
    string d = float:toExpString(c, 0);
    assertEquality("-5e+4", d);
    assertEquality("-5e+4", c.toExpString(0));
}

function testToExpStringWhenFractionDigitsIsNil() {
    float a = 45362.12334;
    string b = float:toExpString(a, ());
    assertEquality("4.536212334e+4", b);
    assertEquality("4.536212334e+4", a.toExpString(()));

    float c = -45362.12334;
    string d = float:toExpString(c, ());
    assertEquality("-4.536212334e+4", d);
    assertEquality("-4.536212334e+4", c.toExpString(()));

    float e = 0;
    assertEquality("0.0e+0", e.toExpString(()));
    assertEquality("0.0e+0", 0.0.toExpString(()));

    float f = 0.000;
    assertEquality("0.0e+0", f.toExpString(()));
    assertEquality("0.0e+0", 0.00000.toExpString(()));
}

function testToExpStringWhenFractionDigitsIsVeryLargeInt() {
    float a = 45362.12334;
    string b = float:toExpString(a, 2147483648);
    string v = "4.536212333999999827938154339790344238281250000000000000000000" +
                   "00000000000000000000000000000000000000000000000000000000000000000000000000000000000" +
                   "0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000" +
                   "00000000000000000000000000000000000000000000000000000000000000000000000000000e+4";
    assertEquality(v, b);
    assertEquality(v, a.toExpString(2147483647 + 1));

    float c = -45362.12334;
    string d = float:toExpString(c, 2147483647 + 1);
    assertEquality("-" + v, d);
    assertEquality("-" + v, c.toExpString(2147483647 + 1));
}

function testToExpStringWhenFractionDigitsIsIntMax() {
    float a = 45362.12334;
    string b = float:toExpString(a, 9223372036854775807);
    string v = "4.536212333999999827938154339790344238281250000000000000000000" +
                   "00000000000000000000000000000000000000000000000000000000000000000000000000000000000" +
                   "0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000" +
                   "00000000000000000000000000000000000000000000000000000000000000000000000000000e+4";
    assertEquality(v, b);
    assertEquality(v, a.toExpString(int:MAX_VALUE));

    float c = -45362.12334;
    string d = float:toExpString(c, 9223372036854775807);
    assertEquality("-" + v, d);
    assertEquality("-" + v, c.toExpString(int:MAX_VALUE));
}

function testToExpStringWithMorePositiveFloats() {
    assertEquality("4.54e+4", float:toExpString(45362.12334, 2));
    assertEquality("4.536e+4", float:toExpString(45362.12334, 3));
    assertEquality("4.5362123339999998e+4", float:toExpString(45362.12334, 16));
    assertEquality("1.2375e-1", float:toExpString(0.1237469219, 4));
    assertEquality("1.237e-1", float:toExpString(0.1237469219, 3));
    assertEquality("1.2374692190000000e-1", float:toExpString(0.1237469219, 16));
    assertEquality("1.23746922e-1", float:toExpString(0.1237469219, 8));
    assertEquality("1.237e-2", float:toExpString(0.0123654, 3));
    assertEquality("1.2365e-2", float:toExpString(0.0123654, 4));
    assertEquality("1.23654000e-2", float:toExpString(0.0123654, 8));
    assertEquality("1.23654e-2", float:toExpString(0.0123654, 5));
    assertEquality("5.6e-8", float:toExpString(0.0000000564, 1));
    assertEquality("6e-8", float:toExpString(0.0000000564, 0));
    assertEquality("5.64000000e-8", float:toExpString(0.0000000564, 8));
    assertEquality("5.6400000e-8", float:toExpString(0.0000000564, 7));
    assertEquality("0.00000e+0", float:toExpString(0, 5));
    assertEquality("0.000000000000000e+0", float:toExpString(0, 15));

    assertEquality("4.5362123339999998e+4", float:toExpString(45362.12334f, 16));
    assertEquality("1.2374692190000000e-1", float:toExpString(0.1237469219F, 16));
    assertEquality("1.23746922e-1", float:toExpString(0.1237469219f, 8));
    assertEquality("1.2365e-2", float:toExpString(0.0123654F, 4));
    assertEquality("1.23654000e-2", float:toExpString(0.0123654f, 8));
    assertEquality("1.23654e-2", float:toExpString(0.0123654F, 5));
    assertEquality("5.64000000e-8", float:toExpString(0.0000000564f, 8));
    assertEquality("5.6400000e-8", float:toExpString(0.0000000564F, 7));
    assertEquality("0.00000e+0", float:toExpString(0f, 5));
    assertEquality("0.000000000000000e+0", float:toExpString(0F, 15));
}

function testToExpStringWithVerySmallAndLargePositiveFloats() {
    float a = 2.5E-17;
    assertEquality("2.49999999999999995e-17", float:toExpString(a, 17));
    assertEquality("2.5000000000e-17", float:toExpString(a, 10));
    assertEquality("2.5000e-17", float:toExpString(a, 4));

    float b = 2.5E+17;
    assertEquality("2.50000000000000000e+17", float:toExpString(b, 17));
    assertEquality("2.5000000000e+17", float:toExpString(b, 10));
    assertEquality("2.5000e+17", float:toExpString(b, 4));

    float c = 1.423223E6;
    assertEquality("1.42322300000000000e+6", c.toExpString(17));
    assertEquality("1.4232230000e+6", c.toExpString(10));
    assertEquality("1.4232e+6", c.toExpString(4));

    float d = 0.9E-10f;
    assertEquality("8.99999999999999994e-11", d.toExpString(17));
    assertEquality("9.000000000000e-11", d.toExpString(12));
    assertEquality("9.0000e-11", d.toExpString(4));
}

function testToExpStringWithMoreNegativeFloats() {
    assertEquality("-4.5362123339999998e+4", float:toExpString(-45362.12334, 16));
    assertEquality("-1.2374692190000000e-1", float:toExpString(-0.1237469219, 16));
    assertEquality("-1.23746922e-1", float:toExpString(-0.1237469219, 8));
    assertEquality("-1.2365e-2", float:toExpString(-0.0123654, 4));
    assertEquality("-1.23654000e-2", float:toExpString(-0.0123654, 8));
    assertEquality("-1.23654e-2", float:toExpString(-0.0123654, 5));
    assertEquality("-5.64000000e-8", float:toExpString(-0.0000000564, 8));
    assertEquality("-5.6400000e-8", float:toExpString(-0.0000000564, 7));
    assertEquality("0.00000e+0", float:toExpString(-0, 5));
    assertEquality("0.000000000000000e+0", float:toExpString(-0, 15));

    assertEquality("-4.5362123339999998e+4", float:toExpString(-45362.12334f, 16));
    assertEquality("-1.2374692190000000e-1", float:toExpString(-0.1237469219F, 16));
    assertEquality("-1.23746922e-1", float:toExpString(-0.1237469219f, 8));
    assertEquality("-1.2365e-2", float:toExpString(-0.0123654F, 4));
    assertEquality("-1.23654000e-2", float:toExpString(-0.0123654f, 8));
    assertEquality("-1.23654e-2", float:toExpString(-0.0123654F, 5));
    assertEquality("-5.64000000e-8", float:toExpString(-0.0000000564f, 8));
    assertEquality("-5.6400000e-8", float:toExpString(-0.0000000564F, 7));
    assertEquality("0.00000e+0", float:toExpString(-0f, 5));
    assertEquality("0.000000000000000e+0", float:toExpString(-0F, 15));
}

function testToExpStringWithVerySmallAndLargeNegativeFloats() {
    float a = -2.5E-17;
    assertEquality("-2.49999999999999995e-17", float:toExpString(a, 17));
    assertEquality("-2.5000000000e-17", float:toExpString(a, 10));
    assertEquality("-2.5000e-17", float:toExpString(a, 4));

    float b = -2.5E+17;
    assertEquality("-2.50000000000000000e+17", float:toExpString(b, 17));
    assertEquality("-2.5000000000e+17", float:toExpString(b, 10));
    assertEquality("-2.5000e+17", float:toExpString(b, 4));

    float c = -1.423223E6;
    assertEquality("-1.42322300000000000e+6", c.toExpString(17));
    assertEquality("-1.4232230000e+6", c.toExpString(10));
    assertEquality("-1.4232e+6", c.toExpString(4));

    float d = -0.9E-10f;
    assertEquality("-8.99999999999999994e-11", d.toExpString(17));
    assertEquality("-9.000000000000e-11", d.toExpString(12));
    assertEquality("-9.0000e-11", d.toExpString(4));
}

function testToExpStringWithHexaDecimalFloatingPoints() {
    float a = 0xabcff;
    assertEquality("7.03743000000000000e+5", a.toExpString(17));
    assertEquality("7.0374300000e+5", a.toExpString(10));
    assertEquality("7.0374e+5", a.toExpString(4));

    float b = 0xAB126fa;
    assertEquality("1.79382010000000000e+8", b.toExpString(17));
    assertEquality("1.7938201000e+8", b.toExpString(10));
    assertEquality("1.7938e+8", b.toExpString(4));

    a = -0xabcff;
    assertEquality("-7.03743000000000000e+5", a.toExpString(17));
    assertEquality("-7.0374300000e+5", a.toExpString(10));
    assertEquality("-7.0374e+5", a.toExpString(4));

    b = -0xAB126fa;
    assertEquality("-1.79382010000000000e+8", b.toExpString(17));
    assertEquality("-1.7938201000e+8", b.toExpString(10));
    assertEquality("-1.7938e+8", b.toExpString(4));

    a = 0x1.e412904862198p4;
    assertEquality("3.02545321300000012e+1", a.toExpString(17));
    assertEquality("3.0254532130e+1", a.toExpString(10));
    assertEquality("3.0254e+1", a.toExpString(4));

    b = 0x1.cbe3p17;
    assertEquality("2.35462000000000000e+5", b.toExpString(17));
    assertEquality("2.3546200000e+5", b.toExpString(10));
    assertEquality("2.3546e+5", b.toExpString(4));

    a = -0x1.e412904862198p4;
    assertEquality("-3.02545321300000012e+1", a.toExpString(17));
    assertEquality("-3.0254532130e+1", a.toExpString(10));
    assertEquality("-3.0254e+1", a.toExpString(4));

    b = -0x1.cbe3p17;
    assertEquality("-2.35462000000000000e+5", b.toExpString(17));
    assertEquality("-2.3546200000e+5", b.toExpString(10));
    assertEquality("-2.3546e+5", b.toExpString(4));
}

function testRound() {
    assertEquality(float:round(123.123f, 2), 123.12f);
    assertEquality(float:round(123.123f, 3), 123.123f);
    assertEquality(float:round(123.123f, 4), 123.123f);

    assertEquality(float:round(555.555f, 2), 555.56f);
    assertEquality(float:round(555.555f, 3), 555.555f);
    assertEquality(float:round(555.555f, 4), 555.555f);

    assertEquality(float:round(555.545f, 2), 555.54f);
    assertEquality(float:round(555.545f, 3), 555.545f);
    assertEquality(float:round(555.545f, 4), 555.545f);

    assertEquality(float:round(555.55551f, 3), 555.556f);
    assertEquality(float:round(555.55451f, 3), 555.555f);
    assertEquality(float:round(555.55449f, 3), 555.554f);

    assertEquality(float:round(765f, 1), 765.0f);
    assertEquality(float:round(765f, 2), 765.0f);

    assertEquality(float:round(0.1234f, 2), 0.12f);
    assertEquality(float:round(0.1234f, 4), 0.1234f);
    assertEquality(float:round(0.1234f, 5), 0.1234f);

    assertEquality(float:round(0.1555f, 1), 0.2f);
    assertEquality(float:round(0.1555f, 2), 0.16f);
    assertEquality(float:round(0.1555f, 3), 0.156f);
    assertEquality(float:round(0.1555f, 4), 0.1555f);
    assertEquality(float:round(0.1555f, 5), 0.1555f);

    assertEquality(float:round(0.001234f, 2), 0.0f);
    assertEquality(float:round(0.001234f, 4), 0.0012f);
    assertEquality(float:round(0.001234f, 6), 0.001234f);
    assertEquality(float:round(0.001234f, 7), 0.001234f);

    assertEquality(float:round(0.0050f, 2), 0.0f);
    assertEquality(float:round(0.0051f, 2), 0.01f);

    assertEquality(float:round(0.0055f, 3), 0.006f);
    assertEquality(float:round(0.0045f, 3), 0.004f);

    assertEquality(float:round(1.123456e12f, 1), 1.123456e12f);
    assertEquality(float:round(1.123456e12f, 7), 1.123456e12f);

    assertEquality(float:round(1.123456e2f, 1), 112.3f);
    assertEquality(float:round(1.123456e2f, 2), 112.35f);
    assertEquality(float:round(1.123456e2f, 3), 112.346f);
    assertEquality(float:round(1.12345e2f, 3), 112.345f);
    assertEquality(float:round(1.12345e2f, 20), 112.345f);
    
    assertEquality(float:round(5.0e2f, 2), 5.0e2f);

    assertEquality(float:round(0.000055e2f, 2), 0.01f);
    assertEquality(float:round(0.00005e2f, 2), 0.0f);

    assertEquality(float:round(0.000055e20f, 2), 0.000055e20f);
    assertEquality(float:round(12345.67e-2f, 2), 123.46f);
    assertEquality(float:round(12345.0e-2f, 1), 123.4f);
    assertEquality(float:round(12345.0e-1f, 1), 1234.5f);
    assertEquality(float:round(12355.0e-1f, 1), 1235.5f);

    assertEquality(float:round(12345.67e-20f, 2), 0.0f);

    assertEquality(float:round(0.05e-2f, 3), 0.0f);
    assertEquality(float:round(0.051e-2f, 3), 0.001f);

    assertEquality(float:round(1.1234567891234561f, 15), 1.123456789123456f);
    assertEquality(float:round(1.123456789123456999999f, 15), 1.123456789123457f);
    assertEquality(float:round(2.3e307f, 307), 2.3e307f);
    assertEquality(float:round(2.335e307f, 2), 2.335e307f);

    assertEquality(float:round(0.1f, int:MAX_VALUE), 0.1f);
    assertEquality(float:round(-0.1f, int:MAX_VALUE), -0.1f);
    assertEquality(float:round(2.335e307f, int:MAX_VALUE), 2.335e307f);
    assertEquality(float:round(2.335e-307f, int:MAX_VALUE), 2.335e-307f);

    assertEquality(float:round(123.123f, 0), 123.0f);
    assertEquality(float:round(123.123f), 123.0f);
    assertEquality(float:round(555.555f), 556.0f);
    assertEquality(float:round(554.5f), 554.0f);
    assertEquality(float:round(765f), 765.0f);

    assertEquality(float:round(0.1234f), 0.0f);
    assertEquality(float:round(0.5f), 0.0f);
    assertEquality(float:round(0.9f), 1.0f);
    assertEquality(float:round(0.001234f), 0.0f);

    assertEquality(float:round(1.123456e12f), 1.123456e12f);
    assertEquality(float:round(5.0e2f), 5.0e2f);
    assertEquality(float:round(0.000055e2f), 0.0f);
    assertEquality(float:round(0.000055e20f), 0.000055e20f);
    assertEquality(float:round(12345.67e-2f), 123.0f);
    assertEquality(float:round(12345.0e-2f), 123.0f);
    assertEquality(float:round(12345.0e-1f), 1234f);
    assertEquality(float:round(12345.67e-20f), 0.0f);
    assertEquality(float:round(2.3e307f), 2.3e307f);
    assertEquality(float:round(2.335e-307f), 0.0f);

    assertEquality(float:round(123.123f, -2), 100.0);
    assertEquality(float:round(123.123f, -3), 0.0);
    assertEquality(float:round(123.123f, -4), 0.0);

    assertEquality(float:round(12.5f, -1), 10.0);
    assertEquality(float:round(235.5f, -1), 240.0);
    assertEquality(float:round(250.0f, -1), 250.0);
    assertEquality(float:round(251.0f, -1), 250.0);
    assertEquality(float:round(251.0f, -2), 300.0);
    assertEquality(float:round(251.0f, -3), 0.0);
    assertEquality(float:round(251.0f, -4), 0.0);

    assertEquality(float:round(999.9f, -3), 1000.0);
    assertEquality(float:round(999.9f, -4), 0.0);

    assertEquality(float:round(555.555f, -2), 600.0);
    assertEquality(float:round(555.555f, -3), 1000.0);
    assertEquality(float:round(555.555f, -4), 0.0);

    assertEquality(float:round(765f, -2), 800.0);

    assertEquality(float:round(0.1234f, -1), 0.0);
    assertEquality(float:round(0.1234f, -5), 0.0);
    assertEquality(float:round(0.1555f, -1), 0.0);
    assertEquality(float:round(0.1555f, -4), 0.0);
    assertEquality(float:round(0.001234f, -2), 0.0);
    assertEquality(float:round(0.001234f, -7), 0.0);
    assertEquality(float:round(1.123456e2f, -1), 110.0);
    assertEquality(float:round(1.123456e2f, -2), 100.0);
    assertEquality(float:round(1.123456e2f, -3), 0.0);
    assertEquality(float:round(1.12345e2f, -3), 0.0);
    assertEquality(float:round(1.12345e2f, -20), 0.0);

    assertEquality(float:round(5.0e2f, 2), 500.0);

    assertEquality(float:round(0.000055e2f, -2), 0.0);
    assertEquality(float:round(0.00005e2f, -2), 0.0);
    assertEquality(float:round(0.000055e20f, -2), 5.5e15f);

    assertEquality(float:round(12345.67e-2f, -2), 100.0);
    assertEquality(float:round(12345.0e-2f, -1), 120.0);
    assertEquality(float:round(12345.0e-1f, -1), 1230.0);
    assertEquality(float:round(12355.0e-1f, -1), 1240.0);
    assertEquality(float:round(12345.67e-20f, -2), 0.0);

    assertEquality(float:round(1.123456789123456999999f, -15), 0.0);

    assertEquality(float:round(2.3e307f, -307), 2.0e307f);
    assertEquality(float:round(2.335e307f, -2), 2.335e307);

    assertEquality(float:round(0.1f, int:MIN_VALUE), 0.0);
    assertEquality(float:round(-0.1f, int:MIN_VALUE), 0.0);
    assertEquality(float:round(2.335e307f, int:MIN_VALUE), 0.0);
    assertEquality(float:round(2.335e-307f, int:MIN_VALUE), 0.0);

    float f = float:NaN;
    assertEquality(float:round(f, 3), f);
    assertEquality(float:round(f, -3), f);
    f = -0.0f;
    assertEquality(float:round(f, 3), f);
    assertEquality(float:round(f, -3), f);
    f = 0.0f;
    assertEquality(float:round(f, 3), f);
    assertEquality(float:round(f, -3), f);
    f = float:Infinity;
    assertEquality(float:round(f), f);
    assertEquality(float:round(f, -2), f);
    f = -float:Infinity;
    assertEquality(float:round(f), f);
    assertEquality(float:round(f, -2), f);
}

function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    string expectedValAsString = expected is error ? expected.toString() : expected.toString();
    string actualValAsString = actual is error ? actual.toString() : actual.toString();
    panic error("expected '" + expectedValAsString + "', found '" + actualValAsString + "'");
}
