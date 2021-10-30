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

    float|error v2 = float:fromHexString("+0xa.bp1");
    test:assertValueEqual(checkpanic v2, 21.375);

    float|error v3 = float:fromHexString("-0xa.bp1");
    test:assertValueEqual(checkpanic v3, -21.375);

    float|error v4 = float:fromHexString("0Xa2c.b32p2");
    test:assertValueEqual(checkpanic v4, 10418.798828125);

    float|error v5 = float:fromHexString("0Xa.b32P-5");
    test:assertValueEqual(checkpanic v5, 0.3343658447265625);

    float|error v6 = float:fromHexString("-0x123.fp-5");
    test:assertValueEqual(checkpanic v6, -9.123046875);

    float|error v7 = float:fromHexString("0x123fp-5");
    test:assertValueEqual(checkpanic v7, 145.96875);

    float|error v8 = float:fromHexString("0x.ab5p2");
    test:assertValueEqual(checkpanic v8, 2.6767578125);

    float|error v9 = float:fromHexString("0x1a");
    error err = <error> v9;
    test:assertValueEqual(err.message(), "{ballerina/lang.float}NumberParsingError");
    test:assertValueEqual(<string> checkpanic err.detail()["message"], "For input string: \"0x1a\"");

    float|error v10 = float:fromHexString("NaN");
    test:assertValueEqual(checkpanic v10, float:NaN);

    float|error v11 = float:fromHexString("+Infinity");
    test:assertValueEqual(checkpanic v11, float:Infinity);

    float|error v12 = float:fromHexString("-Infinity");
    test:assertValueEqual(checkpanic v12, -float:Infinity);

    float|error v13 = float:fromHexString("AInvalidNum");
    err = <error> v13;
    test:assertValueEqual(err.message(), "{ballerina/lang.float}NumberParsingError");
    test:assertValueEqual(<string> checkpanic err.detail()["message"], "For input string: \"AInvalidNum\"");
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
