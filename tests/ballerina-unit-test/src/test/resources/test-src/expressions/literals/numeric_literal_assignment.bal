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

int i = 12;
byte b = 12;
float f = 12.0;
decimal d = 12.0;

function testIntLiteralAsInt() returns boolean {
    int x = 12;
    anydata y = x;
    return y is int && i == y;
}

function testIntLiteralAsByte() returns boolean {
    byte x = 12;
    anydata y = x;
    return y is byte && b == y;
}

function testIntLiteralAsFloat() returns boolean {
    float x = 12;
    anydata y = x;
    return y is float && f == y;
}

function testIntLiteralAsDecimal() returns boolean {
    decimal x = 12;
    anydata y = x;
    return y is decimal && d == y;
}

float f2 = 123.04;
decimal d2 = 123.04;

function testFloatLiteralAsFloat() returns boolean {
    float x = 123.04;
    anydata y = x;
    return y is float && f2 == y;
}

function testFloatLiteralAsDecimal() returns boolean {
    decimal x = 123.04;
    anydata y = x;
    return y is decimal && d2 == y;
}

const byte B1 = 12;
const byte B2 = 13;
const decimal D1 = 12.0;
const decimal D2 = 13.0;

type Foo 12|13;
type Bar 12.0|13.0;
type Baz B1|B2;
type Qux D1|D2;

function testIntLiteralAsIntInUnion() returns boolean {
    byte|float|int|decimal x = 12;
    boolean result = x is int && i == x;

    int|Bar|string y = 12;
    return result && y is int && i == y;
}

function testIntLiteralAsByteInUnion() returns boolean {
    float|byte|decimal x = 12;
    boolean result = x is byte && b == x;

    string|byte|Bar y = 12;
    return result && y is byte && b == y;
}

type TempOne 120|130;

function testIntLiteralAsByteInUnion_2() returns boolean {
    TempOne|byte|Bar y = 12;
    return y is byte && b == y;
}

function testIntLiteralAsFloatInUnion() returns boolean {
    float|decimal x = 12;
    boolean result = x is float && f == x;

    Qux|float y = 12;
    return result && y is float && f == y;
}

function testIntLiteralAsFloatInUnion_2() returns boolean {
    Baz|float y = 120;
    return y is float && 120.0 == y;
}

function testIntLiteralAsDecimalInUnion() returns boolean {
    xml|decimal x = 12;
    return x is decimal && d == x;
}

function testIntLiteralAsDecimalInUnion_2() returns boolean {
    Foo|decimal x = 123;
    decimal dec = 123.0;
    return x is decimal && dec == x;
}

function testFloatLiteralAsFloatInUnion() returns boolean {
    float|decimal x = 12.0;
    boolean result = x is float && f == x;

    boolean|float|Qux y = 12.0;
    return result && y is float && f == y;
}

function testFloatLiteralAsDecimalInUnion() returns boolean {
    xml|decimal x = 12.0;
    return x is decimal && d == x;
}

function testFloatLiteralAsDecimalInUnion_2() returns boolean {
    Bar|decimal y = 123.0;
    decimal dec = 123.0;
    return y is decimal && dec == y;
}

function testIntLiteralAsIntViaFiniteType() returns boolean {
    Foo x = 12;
    anydata y = x;
    boolean result = y is int && i == y;

    float|Foo|Bar z = 12;
    return result && z is int && i == z;
}

function testIntLiteralAsByteViaFiniteType() returns boolean {
    Baz x = 12;
    anydata y = x;
    boolean result = y is byte && b == y;

    decimal|Baz|Bar z = 12;
    return result && z is byte && b == z;
}

function testIntLiteralAsFloatViaFiniteType() returns boolean {
    Bar x = 12;
    anydata y = x;
    boolean result = y is float && f == y;

    decimal|Bar|Qux z = 12;
    return result && z is float && f == z;
}

function testIntLiteralAsDecimalViaFiniteType() returns boolean {
    Qux x = 12;
    anydata y = x;
    boolean result = y is decimal && d == y;

    string|Qux z = 12;
    return result && z is decimal && d == z;
}

function testFloatLiteralAsFloatViaFiniteType() returns boolean {
    Bar x = 12.0;
    anydata y = x;
    boolean result = y is float && f == y;

    decimal|Bar|Qux z = 12.0;
    return result && z is float && f == z;
}

function testFloatLiteralAsDecimalViaFiniteType() returns boolean {
    Qux x = 12.0;
    anydata y = x;
    boolean result = y is decimal && d == y;

    Qux|xml z = 12.0;
    return result && z is decimal && d == z;
}

function testIntLiteralAsIntWithBuiltinUnion() returns boolean {
    anydata|float x = 12;
    boolean result = x is int && x == i;

    decimal|json y = 12;
    result = result && y is int && y == i;

    byte|any|float z = 12;
    return result && z is int && z == i;
}

type TempTwo 123.0|124.0;

function testFloatLiteralAsFloatWithBuiltinUnion() returns boolean {
    anydata|decimal x = 12.0;
    boolean result = x is float && x == f;

    decimal|json y = 12.0;
    result = result && y is float && y == f;

    TempTwo|any|decimal z = 12.0;
    return result && z is float && z == f;
}
