// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

//////////////////////// from float ////////////////////////
function testFloatAsFloat(float f1) returns [boolean, float] {
    float s3 = <float> f1;
    anydata s4 = <float> getFloat(f1);

    return [s3 == s4 && s4 is float, s3];
}

function testFloatAsFloatInUnions(float f1) returns [boolean, float] {
    Employee|string|float f2 = f1;
    json f3 = f1;
    anydata f4 = f1;
    any f5 = f1;

    float s6 = <float> f2;
    float|boolean s7 = <float|boolean> f3;
    float s8 = <float> f4;
    float s9 = <float> f5;

    return [s7 == s6 && s7 == s8 && s9 == s8, s6];
}

function testFloatAsDecimal(float f1) returns [boolean, decimal] {
    decimal s3 = <decimal> f1;
    anydata s4 = <decimal> getFloat(f1);

    return [s3 == s4 && s4 is decimal, s3];
}

function testFloatAsDecimalInUnions(float f1) returns [boolean, decimal] {
    Employee|string|float f2 = f1;
    json f3 = f1;
    anydata f4 = getFloat(f1);
    any f5 = getFloat(f1);

    decimal s6 = <decimal> f2;
    decimal|Employee s7 = <decimal|Employee> f3;
    decimal s8 = <decimal> f4;
    decimal s9 = <decimal> f5;

    return [s7 == s6 && s7 == s8 && s9 == s8, s6];
}

function testFloatAsInt(float f1) returns [boolean, int] {
    int s3 = <int> f1;
    anydata s4 = <int> getFloat(f1);

    return [s3 == s4 && s4 is int, s3];
}

function testFloatAsIntInUnions(float f1) returns [boolean, int] {
    Employee|string|float f2 = f1;
    json f3 = f1;
    anydata f4 = getFloat(f1);
    any f5 = getFloat(f1);

    int s6 = <int> f2;
    int|Employee s7 = <int|Employee> f3;
    int s8 = <int> f4;
    int|string s9 = <int|string> f5;

    return [s7 == s6 && s7 == s8 && s9 == s8, s6];
}

function testFloatAsByte(float f1) returns [boolean, byte] {
    byte s3 = <byte> f1;
    anydata s4 = <byte> getFloat(f1);

    return [s3 == s4 && s4 is byte, s3];
}

function testFloatAsByteInUnions(float f1) returns [boolean, byte] {
    Employee|string|float f2 = f1;
    json f3 = f1;
    anydata f4 = getFloat(f1);
    any f5 = getFloat(f1);

    byte s6 = <byte> f2;
    byte|Employee s7 = <byte|Employee> f3;
    byte s8 = <byte> f4;
    byte|string s9 = <byte|string> f5;

    return [s7 == s6 && s7 == s8 && s9 == s8, s6];
}

//////////////////////// from decimal ////////////////////////
function testDecimalAsFloat(decimal f1) returns [boolean, float] {
    float s3 = <float> f1;
    anydata s4 = <float> getDecimal(f1);

    return [s3 == s4 && s4 is float, s3];
}

function testDecimalAsFloatInUnions(decimal f1) returns [boolean, float|Employee|boolean|string] {
    Employee|string|decimal f2 = f1;
    json f3 = f1;
    anydata f4 = f1;
    any f5 = f1;

    float|string s6 = <float|string> f2;
    float|Employee|boolean s7 = <float|Employee|boolean> f3;
    float s8 = <float> f4;
    float s9 = <float> f5;

    return [s7 == s6 && s7 == s8 && s9 == s8, s6];
}

function testDecimalAsDecimal(decimal f1) returns [boolean, decimal] {
    decimal s3 = <decimal> f1;
    anydata s4 = <decimal> getDecimal(f1);

    return [s3 == s4 && s4 is decimal, s3];
}

function testDecimalAsDecimalInUnions(decimal f1) returns [boolean, decimal|string] {
    Employee|string|decimal f2 = f1;
    json f3 = f1;
    anydata f4 = f1;
    any f5 = f1;

    decimal|string s6 = <decimal|string> f2;
    decimal s7 = <decimal> f3;
    decimal s8 = <decimal> f4;
    decimal s9 = <decimal> f5;

    return [s7 == s6 && s7 == s8 && s9 == s8, s6];
}

function testDecimalAsInt(decimal f1) returns [boolean, int] {
    int s3 = <int> f1;
    anydata s4 = <int> getDecimal(f1);
    return [s3 == s4 && s4 is int, s3];
}

function testDecimalAsIntInUnions(decimal f1) returns [boolean, int] {
    Employee|string|decimal f2 = f1;
    json f3 = f1;
    anydata f4 = f1;
    any f5 = f1;

    int s6 = <int> f2;
    int|string s7 = <int|string> f3;
    int s8 = <int> f4;
    int s9 = <int> f5;

    return [s7 == s6 && s7 == s8 && s9 == s8, s6];
}

function testDecimalAsByte(decimal f1) returns [boolean, byte] {
    byte s3 = <byte> f1;
    anydata s4 = <byte> getDecimal(f1);
    return [s3 == s4 && s4 is byte, s3];
}

function testDecimalAsByteInUnions(decimal f1) returns [boolean, byte] {
    Employee|string|decimal f2 = f1;
    json f3 = f1;
    anydata f4 = f1;
    any f5 = f1;

    byte|Employee s6 = <byte|Employee> f2;
    byte|string s7 = <byte|string> f3;
    byte s8 = <byte> f4;
    byte s9 = <byte> f5;

    return [s7 == s6 && s7 == s8 && s9 == s8, s9];
}

//////////////////////// from int ////////////////////////
function testIntAsFloat(int f1) returns [boolean, float] {
    float s3 = <float> f1;
    anydata s4 = <float> getInt(f1);

    return [s3 == s4 && s4 is float, s3];
}

function testIntAsFloatInUnions(int f1) returns [boolean, float] {
    string|int|boolean f2 = f1;
    json f3 = f1;
    anydata f4 = f1;
    any f5 = f1;

    float s6 = <float> f2;
    float|string s7 = <float|string> f3;
    float|Employee|map<int> s8 = <float|Employee|map<int>> f4;
    float s9 = <float> f5;

    return [s7 == s6 && s7 == s8 && s9 == s8, s6];
}

function testIntAsDecimal(int f1) returns [boolean, decimal] {
    decimal s3 = <decimal> f1;
    anydata s4 = <decimal> getInt(f1);

    return [s3 == s4 && s4 is decimal, s3];
}

function testIntAsDecimalInUnions(int f1) returns [boolean, decimal] {
    string|int|boolean f2 = f1;
    json f3 = f1;
    anydata f4 = f1;
    any f5 = f1;

    decimal s6 = <decimal> f2;
    decimal|string s7 = <decimal|string> f3;
    decimal|Employee|map<float> s8 = <decimal|Employee|map<float>> f4;
    decimal s9 = <decimal> f5;

    return [s7 == s6 && s7 == s8 && s9 == s8, s6];
}

function testIntAsInt(int f1) returns [boolean, int] {
    int s3 = <int> f1;
    anydata s4 = <int> getInt(f1);
    return [s3 == s4 && s4 is int, s3];
}

function testIntAsIntInUnions(int f1) returns [boolean, int|boolean] {
    Employee|string|int f2 = f1;
    json f3 = f1;
    anydata f4 = f1;
    any f5 = f1;

    int|boolean s6 = <int|boolean> f2;
    int s7 = <int> f3;
    int s8 = <int> f4;
    int s9 = <int> f5;

    return [s7 == s6 && s7 == s8 && s9 == s8, s6];
}

function testIntAsByte(int f1) returns [boolean, byte] {
    byte s3 = <byte> f1;
    anydata s4 = <byte> getInt(f1);
    return [s3 == s4 && s4 is byte, s3];
}

function testIntAsByteInUnions(int f1) returns [boolean, byte|boolean] {
    Employee|string|int f2 = f1;
    json f3 = f1;
    anydata f4 = f1;
    any f5 = f1;

    byte|boolean s6 = <byte|boolean> f2;
    byte s7 = <byte> f3;
    byte s8 = <byte> f4;
    byte s9 = <byte> f5;

    return [s7 == s6 && s7 == s8 && s9 == s8, s6];
}

//////////////////////// from byte ////////////////////////
function testByteAsFloat(byte f1) returns [boolean, float] {
    float s3 = <float> f1;
    anydata s4 = <float> getByte(f1);

    return [s3 == s4 && s4 is float, s3];
}

function testByteAsFloatInUnions(byte f1) returns [boolean, float] {
    string|byte|boolean f2 = f1;
    anydata f3 = f1;
    any f4 = f1;

    float s6 = <float> f2;
    float|string s7 = <float|string> f3;
    float|Employee|map<int> s8 = <float|Employee|map<int>> f4;

    return [s7 == s6 && s7 == s8, s6];
}

function testByteAsDecimal(byte f1) returns [boolean, decimal] {
    decimal s3 = <decimal> f1;
    anydata s4 = <decimal> getByte(f1);

    return [s3 == s4 && s4 is decimal, s3];
}

function testByteAsDecimalInUnions(byte f1) returns [boolean, decimal] {
    string|byte f2 = f1;
    anydata f3 = f1;
    any f4 = f1;

    decimal s6 = <decimal> f2;
    decimal|string s7 = <decimal|string> f3;
    decimal|Employee|map<float> s8 = <decimal|Employee|map<float>> f4;

    return [s7 == s6 && s7 == s8, s6];
}

function testByteAsInt(byte f1) returns [boolean, int] {
    int s3 = <int> f1;
    anydata s4 = <int> getByte(f1);
    return [s3 == s4 && s4 is int, s3];
}

function testByteAsIntInUnions(byte f1) returns [boolean, int|boolean] {
    Employee|byte f2 = f1;
    anydata f3 = f1;
    any f4 = f1;

    int|boolean s6 = <int|boolean> f2;
    int s7 = <int> f3;
    int s8 = <int> f4;

    return [s7 == s6 && s7 == s8, s6];
}

function testByteAsByte(byte f1) returns [boolean, byte] {
    byte s3 = <byte> f1;
    anydata s4 = <byte> getByte(f1);
    return [s3 == s4 && s4 is byte, s3];
}

function testByteAsByteInUnions(byte f1) returns [boolean, byte|boolean] {
    Employee|string|byte f2 = f1;
    anydata f3 = f1;
    any f4 = f1;

    byte|boolean s6 = <byte|boolean> f2;
    byte s7 = <byte> f3;
    byte s8 = <byte> f4;

    return [s7 == s6 && s7 == s8, s6];
}

function testNaNFloatAsByte() {
    float f = 0.0/0;
    byte i = <byte> f;
}

function testInfiniteFloatAsByte() {
    float f = 1.0/0;
    byte i = <byte> f;
}

function testNaNFloatInUnionAsByte() {
    float|boolean f = 0.0/0;
    byte i = <byte> f;
}

function testInfiniteFloatInUnionAsByte() {
    float|boolean f = 2.0/0;
    byte i = <byte> f;
}

function testNaNFloatAsInt() {
    float f = 0.0/0;
    int i = <int> f;
}

function testInfiniteFloatAsInt() {
    float f = 1.0/0;
    int i = <int> f;
}

function testOutOfIntRangePositiveFloatAsInt() {
    float f = 9223372036854775807.5;
    int i = <int> f;
}

function testOutOfIntRangeNegativeFloatAsInt() {
    float f = -9223372036854775807.6;
    int i = <int> f;
}

function testNaNFloatInUnionAsInt() {
    float|int f = 0.0/0;
    int i = <int> f;
}

function testInfiniteFloatInUnionAsInt() {
    float|boolean f = 2.0/0;
    int i = <int> f;
}

function testOutOfIntRangePositiveFloatInUnionAsInt() {
    any f = 99223372036854775807.0;
    int i = <int> f;
}

function testOutOfIntRangeNegativeFloatInUnionAsInt() {
    anydata f = -81223372036854775807.0;
    int i = <int> f;
}

function testOutOfIntRangePositiveDecimalAsInt() {
    decimal f = 9223372036854775807.5;
    int i = <int> f;
}

function testOutOfIntRangeNegativeDecimalAsInt() {
    decimal f = -9323372036854775807.6;
    int i = <int> f;
}

function testOutOfIntRangePositiveDecimalInUnionAsInt() {
    decimal d = 99223372036854775807.0;
    decimal|int f = d;
    int i = <int> f;
}

function testOutOfIntRangeNegativeDecimalInUnionAsInt() {
    decimal d = -81223372036854775807.0;
    anydata f = d;
    int i = <int> f;
}

function testNaNDecimalAsByte() {
    decimal d1 = 0.0;
    decimal d2 = d1/0;
    byte i = <byte> d2;
}

function testPositiveInfiniteDecimalAsByte() {
    decimal d1 = 0.0;
    decimal d2 = 1.0/d1;
    byte i = <byte> d2;
}

function testNegativeInfiniteDecimalAsByte() {
    decimal d1 = 0.0;
    decimal d2 = -1.0/d1;
    byte i = <byte> d2;
}

function testNaNDecimalInUnionAsByte() {
    decimal d1 = 0.0;
    decimal|boolean d2 = d1/0;
    byte i = <byte> d2;
}

function testPositiveInfiniteDecimalInUnionAsByte() {
    decimal d1 = 0.0;
    decimal|boolean d2 = 1.0/d1;
    byte i = <byte> d2;
}

function testNegativeInfiniteDecimalInUnionAsByte() {
    decimal d1 = 0.0;
    decimal|boolean d2 = -1.0/d1;
    byte i = <byte> d2;
}

function testNaNDecimalAsInt() {
    decimal d1 = 0.0;
    decimal d2 = d1/0;
    int i = <int> d2;
}

function testPositiveInfiniteDecimalAsInt() {
    decimal d1 = 0.0;
    decimal d2 = 1.0/d1;
    int i = <int> d2;
}

function testNegativeInfiniteDecimalAsInt() {
    decimal d1 = 0.0;
    decimal d2 = -1.0/d1;
    int i = <int> d2;
}

function testNaNDecimalInUnionAsInt() {
    decimal d1 = 0.0;
    decimal|boolean d2 = d1/0;
    int i = <int> d2;
}

function testPositiveInfiniteDecimalInUnionAsInt() {
    decimal d1 = 0.0;
    decimal|boolean d2 = 1.0/d1;
    int i = <int> d2;
}

function testNegativeInfiniteDecimalInUnionAsInt() {
    decimal d1 = 0.0;
    decimal|boolean d2 = -1.0/d1;
    int i = <int> d2;
}

function testExplicitlyTypedExprForExactValues() returns error? {
    // test from float
    float f = 12.345;
    decimal fd = 12.34500000000000063948846218409017;
    byte fb = 12;

    float f1 = <float> f;
    if (f1 != f) {
        error e = error("invalid resultant value, from float to float");
        return e;
    }

    decimal d1 = <decimal> f;
    if (d1 != fd)  {
        error e = error("invalid resultant value, from float to decimal");
        return e;
    }

    int i1 = <int> f;
    if (i1 != 12) {
        error e = error("invalid resultant value, from float to int");
        return e;
    }

    byte b1 = <byte> f;
    if (b1 != fb) {
        error e = error("invalid resultant value, from float to byte");
        return e;
    }

    // test from decimal
    decimal d = 12.345;

    float f2 = <float> d;
    if (f2 != f) {
        error e = error("invalid resultant value, from decimal to float");
        return e;
    }

    decimal d2 = <decimal> d;
    if (d2 != d) {
        error e = error("invalid resultant value, from decimal to decimal");
        return e;
    }

    int i2 = <int> d;
    if (i2 != 12) {
        error e = error("invalid resultant value, from decimal to int");
        return e;
    }

    byte b2 = <byte> d;
    if (b2 != fb) {
        error e = error("invalid resultant value, from decimal to byte");
        return e;
    }

    // test from int
    int i = 123;
    float intf = 123.0;
    decimal intd = 123.0;
    byte intb = 123;

    float f3 = <float> i;
    if (f3 != intf) {
        error e = error("invalid resultant value, from int to float");
        return e;
    }

    decimal d3 = <decimal> i;
    if (d3 != intd) {
        error e = error("invalid resultant value, from int to decimal");
        return e;
    }

    int i3 = <int> i;
    if (i3 != i) {
        error e = error("invalid resultant value, from int to int");
        return e;
    }

    byte b3 = <byte> i;
    if (b3 != intb) {
        error e = error("invalid resultant value, from int to byte");
        return e;
    }

    // test from byte
    byte b = 255;
    float bytef = 255.0;
    decimal byted = 255.0;
    int bytei = 255;

    float f4 = <float> b;
    if (f4 != bytef) {
        error e = error("invalid resultant value, from byte to float");
        return e;
    }

    decimal d4 = <decimal> b;
    if (d4 != byted) {
        error e = error("invalid resultant value, from byte to decimal");
        return e;
    }

    int i4 = <int> b;
    if (i4 != bytei) {
        error e = error("invalid resultant value, from byte to int");
        return e;
    }

    byte b4 = <byte> b;
    if (b4 != b) {
        error e = error("invalid resultant value, from byte to byte");
        return e;
    }
    return;
}

function testConversionFromUnionWithNumericBasicTypes() returns boolean {
    float f = 12.0;
    decimal d = 110.5;
    int|boolean u1 = 12;
    float f1 = <float> u1;
    boolean conversionSuccessful = f1 == f;

    float|int|string u2 = 110.5f;
    decimal|boolean d1 = <decimal|boolean> u2;
    return conversionSuccessful && d1 == d;
}

function testNumericConversionFromBasicTypeToUnionType() returns boolean {
    float f1 = 12.0;
    int i1 = 12;
    decimal d1 = 12.0;
    byte b1 = 12;

    int|boolean u1 = <int|boolean> f1;
    decimal|string u2 = <decimal|string> f1;
    byte|string|Employee u3 = <byte|string|Employee> f1;
    boolean conversionSuccessful = u1 == i1 && u2 == d1 && u3 == b1;

    float|boolean u4 = <float|boolean> i1;
    decimal|string u5 = <decimal|string> i1;
    byte|string|Employee u6 = <byte|string|Employee> i1;
    conversionSuccessful = conversionSuccessful && u4 == f1 && u5 == d1 && u6 == b1;

    float|boolean u7 = <float|boolean> d1;
    int|string u8 = <int|string> d1;
    byte|string|Employee u9 = <byte|string|Employee> d1;
    conversionSuccessful = conversionSuccessful && u7 == f1 && u8 == i1 && u9 == b1;

    float|boolean u10 = <float|boolean> b1;
    int|string u11 = <int|string> b1;
    decimal|string|Employee u12 = <decimal|string|Employee> b1;
    conversionSuccessful = conversionSuccessful && u10 == f1 && u11 == i1 && u12 == d1;
    return conversionSuccessful;
}

type IntOrFoo int|"foo";
type OneOrString 1|string;
type OneOrTwo 1.0|2.0;

function testNumericConversionFromFiniteType() returns boolean {
    IntOrFoo a = 10;
    float b = <float> a;
    boolean conversionSuccessful = b == 10.0;

    OneOrString c = 1;
    decimal d = <decimal> c;
    decimal e = 1.0;
    conversionSuccessful = conversionSuccessful && d == e;

    OneOrTwo f = 2.0;
    int g = <int> f;
    return conversionSuccessful && g === 2;
}

function getFloat(float f) returns float {
    return f;
}

function getDecimal(decimal d) returns decimal {
    return d;
}

function getInt(int i) returns int {
    return i;
}

function getByte(byte b) returns byte {
    return b;
}

type Employee record {
    string name;
};
