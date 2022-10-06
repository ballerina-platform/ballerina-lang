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

import ballerina/lang.'decimal as decimals;
import ballerina/lang.test;

function testSum(decimal p1, decimal p2) returns decimal {
    return decimals:sum(p1, p2);
}

function testOneArgMax(decimal arg) returns decimal {
    return decimals:max(arg);
}

function testMultiArgMax(decimal arg, decimal[] otherArgs) returns decimal {
    return decimals:max(arg, ...otherArgs);
}

function testOneArgMin(decimal arg) returns decimal {
    return decimals:min(arg);
}

function testMultiArgMin(decimal arg, decimal[] otherArgs) returns decimal {
    return decimals:min(arg, ...otherArgs);
}

function testAbs(decimal arg) returns decimal {
    return decimals:abs(arg);
}

function testRound(decimal arg) returns decimal {
    return decimals:round(arg);
}

type RoundFullDataPoint [decimal, int, decimal];

function testRunnerTestRoundToFractionDigits() {

    RoundFullDataPoint[] data = [
        [-504023030303030303030.303567, 3, -504023030303030303030.304],
        [-50402.303567, 8, -50402.303567],
        [3.303567, 0, 3],
        [3234.5, 0, 3234],
        [3234.303567, 0, 3234],

        [3234.503567, 0, 3235],
        [3233.503567, 0, 3234],
        [-3234.303567, 0, -3234],
        [-3234.5, 0, -3234],
        [3.303567, -1, 0],

        [3234.303567, -2, 3.2E+3],
        [3234.303567, -10, 0],
        [3234.303567, 7, 3234.303567],
        [3234.303567, 10, 3234.303567],
        [5.5555, 4, 5.5555],

        [5.5555, 3, 5.556],
        [5.5555, 2, 5.56],
        [5.56, 1, 5.6],
        [5.55, -1, 1E+1],
        [5.55, -200, 0],

        [5.55, 2147483647, 5.55],
        [5.55, -2147483648, 0],
        [5.5E-2, 3, 0.055],
        [5.52156E2, 3, 552.156],
        [5.521561234452654235E2, 15, 552.156123445265424]
    ];

    foreach RoundFullDataPoint dataPoint in data {
        assertEquality(decimals:round(dataPoint[0], dataPoint[1]), dataPoint[2]);
    }
}

function testRoundWithNamedArguments() {
    decimal x = decimals:round(5.55, fractionDigits = 1);
    decimal y = decimals:round(x = 5.55);
    decimal z = decimals:round(x = 5.55, fractionDigits = 1);
    assertEquality(x.toBalString(), "5.6d");
    assertEquality(y.toBalString(), "6d");
    assertEquality(z.toBalString(), "5.6d");
}

function testRunnerTestRoundToZeroWithCast() {
    decimal[] data = [
        -50402.303567,
        3.303567,
        3234.5,
        3234.303567,
        -3234.303567,
        -3234.5,
        3.303567,
        3234.303567,
        5.5555,
        5.56,
        5.55,
        504023030303031.5035
    ];

    foreach decimal decimalNumber in data {
        testRoundToZeroWithCast(decimalNumber);
    }
}

function testRoundToZeroWithCast(decimal arg) {
    int casted = <int>arg;
    decimal rounded = arg.round(0);
    string castedStr = casted.toBalString() + "d"; // hack to make a decimal string
    string roundedStr = rounded.toBalString();
    assertEquality(castedStr, roundedStr);
}

function testFloor(decimal arg) returns decimal {
    return decimals:floor(arg);
}

function testCeiling(decimal arg) returns decimal {
    return decimals:ceiling(arg);
}

function testFromString(string arg) returns decimal|error {
    return decimals:fromString(arg);
}

function testMaxAsMethodInvok(decimal x, decimal...xs) returns decimal {
    return x.max(...xs);
}

function testMinAsMethodInvok(decimal x, decimal...xs) returns decimal {
    return x.min(...xs);
}

function testAbsAsMethodInvok(decimal x) returns decimal {
    return x.abs();
}

function testRoundAsMethodInvok(decimal x) returns decimal {
    return x.round();
}

function testFloorAsMethodInvok(decimal x) returns decimal {
    return x.floor();
}

function testCeilingAsMethodInvok(decimal x) returns decimal {
    return x.ceiling();
}

function value() returns decimal|error {
    return 'decimal:fromString("x");
}

function testFromStringWithStringArg() {
    decimal|error res = value();
    assertEquality(true, res is error);

    error resError = <error> res;
    assertEquality("'string' value 'x' cannot be converted to 'decimal'", resError.detail().get("message"));
}

type Decimals 12d|21d;

function testLangLibCallOnFiniteType() {
    Decimals x = 12;
    decimal y = x.sum(1, 2, 3);
    assertEquality(18d, y);
}

decimal d1 = -0.0;
decimal d2 = 0.0;
decimal d3 = 1.0;
decimal d4 = 2.0;
decimal d5 = 2.00;

function testDecimalEquality() {
    test:assertTrue(d3 == d3);
    test:assertFalse(d3 == d4);
    test:assertTrue(d4 == d5);
    test:assertTrue(d1 == d2);
}

function testDecimalNotEquality() {
    test:assertFalse(d3 != d3);
    test:assertTrue(d3 != d4);
    test:assertFalse(d4 != d5);
    test:assertFalse(d1 != d2);
}

function testDecimalExactEquality() {
    test:assertTrue(d5 === d5);
    test:assertFalse(d3 === d4);
    test:assertFalse(d4 === d5);
    test:assertTrue(d1 === d2);
}

function testDecimalNotExactEquality() {
    test:assertFalse(d5 !== d5);
    test:assertTrue(d3 !== d4);
    test:assertTrue(d4 !== d5);
    test:assertFalse(d1 !== d2);
}

function testFromStringFunctionWithInvalidValues() {
    decimal|error a1 = decimal:fromString("123f");
    assertEquality(true, a1 is error);
    if (a1 is error) {
        assertEquality("{ballerina/lang.decimal}NumberParsingError", a1.message());
        assertEquality("'string' value '123f' cannot be converted to 'decimal'", <string> checkpanic a1.detail()["message"]);
    }

    a1 = decimal:fromString("123F");
    assertEquality(true, a1 is error);
    if (a1 is error) {
        assertEquality("{ballerina/lang.decimal}NumberParsingError", a1.message());
        assertEquality("'string' value '123F' cannot be converted to 'decimal'", <string> checkpanic a1.detail()["message"]);
    }

    a1 = decimal:fromString("123.67f");
    assertEquality(true, a1 is error);
    if (a1 is error) {
        assertEquality("{ballerina/lang.decimal}NumberParsingError", a1.message());
        assertEquality("'string' value '123.67f' cannot be converted to 'decimal'", <string> checkpanic a1.detail()["message"]);
    }

    a1 = decimal:fromString("123.67F");
    assertEquality(true, a1 is error);
    if (a1 is error) {
        assertEquality("{ballerina/lang.decimal}NumberParsingError", a1.message());
        assertEquality("'string' value '123.67F' cannot be converted to 'decimal'", <string> checkpanic a1.detail()["message"]);
    }

    a1 = decimal:fromString("12E+2f");
    assertEquality(true, a1 is error);
    if (a1 is error) {
        assertEquality("{ballerina/lang.decimal}NumberParsingError", a1.message());
        assertEquality("'string' value '12E+2f' cannot be converted to 'decimal'", <string> checkpanic a1.detail()["message"]);
    }

    a1 = decimal:fromString("-12E+2F");
    assertEquality(true, a1 is error);
    if (a1 is error) {
        assertEquality("{ballerina/lang.decimal}NumberParsingError", a1.message());
        assertEquality("'string' value '-12E+2F' cannot be converted to 'decimal'", <string> checkpanic a1.detail()["message"]);
    }

    a1 = decimal:fromString("12e-2F");
    assertEquality(true, a1 is error);
    if (a1 is error) {
        assertEquality("{ballerina/lang.decimal}NumberParsingError", a1.message());
        assertEquality("'string' value '12e-2F' cannot be converted to 'decimal'", <string> checkpanic a1.detail()["message"]);
    }

    a1 = decimal:fromString("-12e-2f");
    assertEquality(true, a1 is error);
    if (a1 is error) {
        assertEquality("{ballerina/lang.decimal}NumberParsingError", a1.message());
        assertEquality("'string' value '-12e-2f' cannot be converted to 'decimal'", <string> checkpanic a1.detail()["message"]);
    }

    a1 = decimal:fromString("12.23E+2F");
    assertEquality(true, a1 is error);
    if (a1 is error) {
        assertEquality("{ballerina/lang.decimal}NumberParsingError", a1.message());
        assertEquality("'string' value '12.23E+2F' cannot be converted to 'decimal'", <string> checkpanic a1.detail()["message"]);
    }

    a1 = decimal:fromString("-12.23E+2f");
    assertEquality(true, a1 is error);
    if (a1 is error) {
        assertEquality("{ballerina/lang.decimal}NumberParsingError", a1.message());
        assertEquality("'string' value '-12.23E+2f' cannot be converted to 'decimal'", <string> checkpanic a1.detail()["message"]);
    }

    a1 = decimal:fromString("12.23e-2f");
    assertEquality(true, a1 is error);
    if (a1 is error) {
        assertEquality("{ballerina/lang.decimal}NumberParsingError", a1.message());
        assertEquality("'string' value '12.23e-2f' cannot be converted to 'decimal'", <string> checkpanic a1.detail()["message"]);
    }

    a1 = decimal:fromString("-12.23e-2F");
    assertEquality(true, a1 is error);
    if (a1 is error) {
        assertEquality("{ballerina/lang.decimal}NumberParsingError", a1.message());
        assertEquality("'string' value '-12.23e-2F' cannot be converted to 'decimal'", <string> checkpanic a1.detail()["message"]);
    }

    a1 = decimal:fromString("+12.23E+2F");
    assertEquality(true, a1 is error);
    if (a1 is error) {
        assertEquality("{ballerina/lang.decimal}NumberParsingError", a1.message());
        assertEquality("'string' value '+12.23E+2F' cannot be converted to 'decimal'", <string> checkpanic a1.detail()["message"]);
    }

    a1 = decimal:fromString("+12.23e-2f");
    assertEquality(true, a1 is error);
    if (a1 is error) {
        assertEquality("{ballerina/lang.decimal}NumberParsingError", a1.message());
        assertEquality("'string' value '+12.23e-2f' cannot be converted to 'decimal'", <string> checkpanic a1.detail()["message"]);
    }

    a1 = decimal:fromString("+123.0f");
    assertEquality(true, a1 is error);
    if (a1 is error) {
        assertEquality("{ballerina/lang.decimal}NumberParsingError", a1.message());
        assertEquality("'string' value '+123.0f' cannot be converted to 'decimal'", <string> checkpanic a1.detail()["message"]);
    }

    a1 = decimal:fromString("12d");
    assertEquality(true, a1 is error);
    if (a1 is error) {
        assertEquality("{ballerina/lang.decimal}NumberParsingError", a1.message());
        assertEquality("'string' value '12d' cannot be converted to 'decimal'", <string> checkpanic a1.detail()["message"]);
    }

     a1 = decimal:fromString("12D");
     assertEquality(true, a1 is error);
     if (a1 is error) {
         assertEquality("{ballerina/lang.decimal}NumberParsingError", a1.message());
         assertEquality("'string' value '12D' cannot be converted to 'decimal'", <string> checkpanic a1.detail()["message"]);
     }

    a1 = decimal:fromString("12.23E+2D");
    assertEquality(true, a1 is error);
    if (a1 is error) {
        assertEquality("{ballerina/lang.decimal}NumberParsingError", a1.message());
        assertEquality("'string' value '12.23E+2D' cannot be converted to 'decimal'", <string> checkpanic a1.detail()["message"]);
    }

    a1 = decimal:fromString("-12.23e-2d");
    assertEquality(true, a1 is error);
    if (a1 is error) {
        assertEquality("{ballerina/lang.decimal}NumberParsingError", a1.message());
        assertEquality("'string' value '-12.23e-2d' cannot be converted to 'decimal'", <string> checkpanic a1.detail()["message"]);
    }

    a1 = decimal:fromString("0xabcf");
    assertEquality(true, a1 is error);
    if (a1 is error) {
        assertEquality("{ballerina/lang.decimal}NumberParsingError", a1.message());
        assertEquality("'string' value '0xabcf' cannot be converted to 'decimal'", <string> checkpanic a1.detail()["message"]);
    }

    a1 = decimal:fromString("-0xabcf");
    assertEquality(true, a1 is error);
    if (a1 is error) {
        assertEquality("{ballerina/lang.decimal}NumberParsingError", a1.message());
        assertEquality("'string' value '-0xabcf' cannot be converted to 'decimal'", <string> checkpanic a1.detail()["message"]);
    }

    a1 = decimal:fromString("0Xabcf");
    assertEquality(true, a1 is error);
    if (a1 is error) {
        assertEquality("{ballerina/lang.decimal}NumberParsingError", a1.message());
        assertEquality("'string' value '0Xabcf' cannot be converted to 'decimal'", <string> checkpanic a1.detail()["message"]);
    }

    a1 = decimal:fromString("-0Xabcf");
    assertEquality(true, a1 is error);
    if (a1 is error) {
        assertEquality("{ballerina/lang.decimal}NumberParsingError", a1.message());
        assertEquality("'string' value '-0Xabcf' cannot be converted to 'decimal'", <string> checkpanic a1.detail()["message"]);
    }

    a1 = decimal:fromString("0x12a.12fa");
    assertEquality(true, a1 is error);
    if (a1 is error) {
        assertEquality("{ballerina/lang.decimal}NumberParsingError", a1.message());
        assertEquality("'string' value '0x12a.12fa' cannot be converted to 'decimal'", <string> checkpanic a1.detail()["message"]);
    }

    a1 = decimal:fromString("+0x12a");
    assertEquality(true, a1 is error);
    if (a1 is error) {
        assertEquality("{ballerina/lang.decimal}NumberParsingError", a1.message());
        assertEquality("'string' value '+0x12a' cannot be converted to 'decimal'", <string> checkpanic a1.detail()["message"]);
    }

    a1 = decimal:fromString("");
    assertEquality(true, a1 is error);
    if (a1 is error) {
        assertEquality("{ballerina/lang.decimal}NumberParsingError", a1.message());
        assertEquality("'string' value '' cannot be converted to 'decimal'", <string> checkpanic a1.detail()["message"]);
    }

    a1 = decimal:fromString("1e-6143");
    assertEquality(false, a1 is error);
    assertEquality(1e-6143d, checkpanic a1);

    a1 = trap decimal:fromString("9.999999999999999999999999999999999E6145");
    assertEquality(true, a1 is error);
    if (a1 is error) {
        assertEquality("{ballerina}NumberOverflow", a1.message());
        assertEquality("decimal range overflow", <string> checkpanic a1.detail()["message"]);
    }

    a1 = trap decimal:fromString("10E6145");
    assertEquality(true, a1 is error);
    if (a1 is error) {
        assertEquality("{ballerina}NumberOverflow", a1.message());
        assertEquality("decimal range overflow", <string> checkpanic a1.detail()["message"]);
    }
}

function testQuantize() {
    assertEquality(decimal:quantize(123.123, 2), 123d);
    assertEquality(123.123d.quantize(2), 123d);
    assertEquality(decimal:quantize(123.123, 2.0), 123.1d);
    assertEquality(123.123d.quantize(2.0), 123.1d);
    assertEquality(decimal:quantize(123.123, 2.00), 123.12d);
    assertEquality(123.123d.quantize(2.00), 123.12d);
    assertEquality(decimal:quantize(123.123, 2.000), 123.123d);
    assertEquality(123.123d.quantize(2.000), 123.123d);
    assertEquality(decimal:quantize(123.123, 2.0000), 123.1230d);
    assertEquality(123.123d.quantize(2.0000), 123.1230d);
    assertEquality(decimal:quantize(123.123, 2.00000), 123.12300d);
    assertEquality(123.123d.quantize(2.00000), 123.12300d);
    assertEquality(decimal:quantize(1.123, 2.00000000000000000000000000000000000000000000000000000), 1.123000000000000000000000000000000d);
    assertEquality(decimal:quantize(x = 123.123, y = 0.2), 123.1d);
    assertEquality(decimal:quantize(x = 123.123, y = 0.02), 123.12d);
    assertEquality(decimal:quantize(x = 123.123, y = 0.002), 123.123d);
    assertEquality(decimal:quantize(x = 123.123, y = 0.0002), 123.1230d);
    assertEquality(decimal:quantize(y = 0.1E1, x = 123.123), 123d);
    assertEquality(decimal:quantize(123.123, 0.01E2), 123d);
    assertEquality(decimal:quantize(123.123, 0.001E3), 123d);
    assertEquality(decimal:quantize(123.123, 0.0001E4), 123d);
    assertEquality(decimal:quantize(123.123, 0.00001E5), 123d);
    assertEquality(decimal:quantize(123.123, 0.01E1), 123.1d);
    assertEquality(decimal:quantize(123.123, 0.001E1), 123.12d);
    assertEquality(decimal:quantize(123.123, 0.0001E1), 123.123d);
    assertEquality(decimal:quantize(123.123, 0.00001E1), 123.1230d);
    assertEquality(decimal:quantize(123.123, 0.000001E1), 123.12300d);
    assertEquality(decimal:quantize(123, 10E0), 123d);
    assertEquality(123d.quantize(10E0), 123d);
    assertEquality(decimal:quantize(123, 10E1), 1.2E2d);
    assertEquality(123d.quantize(10E1), 1.2E2d);
    assertEquality(decimal:quantize(123, 10E2), 1E2d);
    assertEquality(123d.quantize(10E2), 1E2d);
    assertEquality(decimal:quantize(123, 10E3), 0d);
    assertEquality(123d.quantize(10E3), 0d);
    assertEquality(decimal:quantize(123, 10E4), 0d);
    assertEquality(123d.quantize(10E4), 0d);
    assertEquality(decimal:quantize(263E2, 10E2), 263E2d);
    assertEquality(263E2d.quantize(10E2), 263E2d);
    assertEquality(decimal:quantize(263E2, 10E3), 2.6E4d);
    assertEquality(263E2d.quantize(10E3),  2.6E4d);
    assertEquality(decimal:quantize(255E2, 10E3), 2.6E4d);
    assertEquality(decimal:quantize(263E2, 10E4), 3E4d);
    assertEquality(decimal:quantize(263E2, 10E5), 0d);
    assertEquality(decimal:quantize(263E2, 10E6), 0d);
    assertEquality(decimal:quantize(-263E2, 10E2), -263E2d);
    assertEquality(decimal:quantize(-263E2, 10E3), -2.6E4d);
    assertEquality(decimal:quantize(-255E2, 10E3), -2.6E4d);
    assertEquality(decimal:quantize(-263E2, 10E4), -3E4d);
    assertEquality(decimal:quantize(-263E2, 10E5), 0d);
    assertEquality(decimal:quantize(263E2, 10E6), 0d);
    assertEquality(decimal:quantize(0, 10E5), 0d);
    assertEquality(decimal:quantize(-0, 10E5), 0d);
    assertEquality(decimal:quantize(2.17, 10E0), 2d);
    assertEquality(decimal:quantize(2.17, 10E1), 0d);
    assertEquality(decimal:quantize(2.17, 10E2), 0d);
    assertEquality(decimal:quantize(123.123, 10E0), 123d);
    assertEquality(decimal:quantize(123.123, 10E1), 1.2E+2d);
    assertEquality(decimal:quantize(123.123, 10E2), 1E+2d);
    assertEquality(decimal:quantize(123.123, 10E3), 0d);
    assertEquality(decimal:quantize(123.123, 10E4), 0d);
    assertEquality(decimal:quantize(123.123, 10E5), 0d);
    assertEquality(decimal:quantize(123.123, 10E-1), 123.1d);
    assertEquality(decimal:quantize(123.123, 10E-2), 123.12d);
    assertEquality(decimal:quantize(123.123, 10E-3), 123.123d);
    assertEquality(decimal:quantize(123.123, 10E-4), 123.1230d);
    assertEquality(decimal:quantize(123.123, 10E-5), 123.12300d);
    assertEquality(decimal:quantize(123.123, 1E-1), 123.1d);
    assertEquality(decimal:quantize(123.123, 0.1E-1), 123.12d);
    assertEquality(decimal:quantize(123.123, 0.01E-1), 123.123d);
    assertEquality(decimal:quantize(123.123, 0.001E-1), 123.1230d);
    assertEquality(decimal:quantize(123.123, 0.0001E-1), 123.12300d);
    assertEquality(decimal:quantize(263E5, 10E2), 26300000.00d);
    assertEquality(decimal:quantize(263E5, 10E3), 26300000.000d);
    assertEquality(decimal:quantize(263E5, 10E4), 26300000.0000d);
    assertEquality(decimal:quantize(263, 10E-1), 263.0d);
    assertEquality(decimal:quantize(263, 10E-2), 263.00d);
    assertEquality(decimal:quantize(263, 10E-3), 263.000d);
    assertEquality(decimal:quantize(263.2E2, 10E-1), 26320.0d);
    assertEquality(decimal:quantize(263.2E2, 10E-2), 26320.00d);
    assertEquality(decimal:quantize(2.632E2, 10E-1), 263.2d);
    assertEquality(decimal:quantize(2.632E2, 10E-2), 263.20d);
    assertEquality(decimal:quantize(2.632E2, 10E-3), 263.200d);
    assertEquality(decimal:quantize(2.632E2, 10E-31), 263.2000000000000000000000000000000d);
    assertEquality(decimal:quantize(-0.1, 1), 0d);
    assertEquality(decimal:quantize(2632E-2, 10E-4), 26.3200d);
    assertEquality(decimal:quantize(2632E-2, 10E-3), 26.320d);
    assertEquality(decimal:quantize(2632E-2, 10E-2), 26.32d);
    assertEquality(decimal:quantize(2632E-2, 10E-1), 26.3d);
    assertEquality(decimal:quantize(2632E-2, 10E0), 26d);
    assertEquality(decimal:quantize(2632E-2, 10E1), 3E1d);
    assertEquality(decimal:quantize(2632E-2, 10E2), 0d);
    assertEquality(decimal:quantize(2632E-2, 10E3), 0d);
    assertEquality(decimal:quantize(0.26E-2, 10E-6), 0.002600d);
    assertEquality(decimal:quantize(0.26E-2, 10E-5), 0.00260d);
    assertEquality(decimal:quantize(0.26E-2, 10E-4), 0.0026d);
    assertEquality(decimal:quantize(0.26E-2, 10E-3), 0.003d);
    assertEquality(decimal:quantize(0.26E-2, 10E-2), 0.00d);
    assertEquality(decimal:quantize(0.26E-2, 10E-1), 0d);
    assertEquality(decimal:quantize(0.26E-2, 10E0), 0d);
    assertEquality(decimal:quantize(-0.26E-2, 10E-6), -0.002600d);
    assertEquality(decimal:quantize(-0.26E-2, 10E-5), -0.00260d);
    assertEquality(decimal:quantize(-0.26E-2, 10E-4), -0.0026d);
    assertEquality(decimal:quantize(-0.26E-2, 10E-3), -0.003d);
    assertEquality(decimal:quantize(-0.26E-2, 10E-2), -0.00d);
    assertEquality(decimal:quantize(-0.26E-2, 10E-1), 0d);
    assertEquality(decimal:quantize(-0.26E-2, 10E0), 0d);
    assertEquality(decimal:quantize(9.999999999999999999999999999999999E6143, 1E6143), 1.0E+6144d);
    assertEquality(decimal:quantize(9.999999999999999999999999999999999E6143, 1E6144), 1.0E+6144d);
    assertEquality(decimal:quantize(9.999999999999999999999999999999999E6143, 1E6144), 1.0E+6144d);
    assertEquality(decimal:quantize(1E-6142, 1E0), 0d);
    assertEquality(decimal:quantize(1E-6142, 1E10), 0d);
    assertEquality(decimal:quantize(1E-6142, 1E-6142), 1E-6142d);
    assertEquality(decimal:quantize(1E-6142, 1E-6141), 0d);
    assertEquality(decimal:quantize(1E-6142, 1E-6140), 0d);
    assertEquality(decimal:quantize(0.000000000000000000000000000000000E6-6176, 1E0), -6176d);
    assertEquality(decimal:quantize(0.000000000000000000000000000000000E6-6176, 1E1), -6.18E+3d);
    assertEquality(decimal:quantize(0.000000000000000000000000000000000E6-6176, 1E2), -6.2E+3d);
    assertEquality(decimal:quantize(0.000000000000000000000000000000000E6-6176, 1E3), -6E+3d);
}

function testQuantizeFunctionWithInvalidOutput() {
    decimal|error a1 = trap decimal:quantize(12.3, 2E-35);
    assertEquality(true, a1 is error);
    assertEquality("{ballerina/lang.decimal}QuantizeError", (<error> a1).message());

    a1 = trap decimal:quantize(1, 2E-35);
    assertEquality(true, a1 is error);
    assertEquality("{ballerina/lang.decimal}QuantizeError", (<error> a1).message());

    a1 = trap decimal:quantize(1E-2, 2E-36);
    assertEquality(true, a1 is error);
    assertEquality("{ballerina/lang.decimal}QuantizeError", (<error> a1).message());

    a1 = trap decimal:quantize(9.999999999999999999999999999999999E6144, 1E0);
    assertEquality(true, a1 is error);
    assertEquality("{ballerina/lang.decimal}QuantizeError", (<error> a1).message());

    a1 = trap decimal:quantize(9.999999999999999999999999999999999E6144, 1E-10);
    assertEquality(true, a1 is error);
    assertEquality("{ballerina/lang.decimal}QuantizeError", (<error> a1).message());

    a1 = trap decimal:quantize(9.999999999999999999999999999999999E6144, 1E143);
    assertEquality(true, a1 is error);
    assertEquality("{ballerina/lang.decimal}QuantizeError", (<error> a1).message());
    
    a1 = trap decimal:quantize(0.000000000000000000000000000000000E6-6176, 1E-6176);
    assertEquality(true, a1 is error);
    assertEquality("{ballerina/lang.decimal}QuantizeError", (<error> a1).message());

    a1 = trap decimal:quantize(0.000000000000000000000000000000000E6-6176, 1E-6175);
    assertEquality(true, a1 is error);
    assertEquality("{ballerina/lang.decimal}QuantizeError", (<error> a1).message());
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
