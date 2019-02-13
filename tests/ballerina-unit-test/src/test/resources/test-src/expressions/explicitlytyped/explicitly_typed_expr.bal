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

//////////////////////// from string ////////////////////////

function testStringAsString(string s1) returns boolean {
    string s2 = <string> s1;
    anydata s3 = <string> getString(s1);

    return s1 == s3 && s2 == s1 && s3 is string;
}

function testStringInUnionAsString(string s1) returns boolean {
    Employee|string|int s2 = s1;
    json s3 = s1;
    anydata s4 = s1;
    any s5 = s1;

    string s6 = <string> s2;
    string s7 = <string> s3;
    string s8 = <string> s4;
    string s9 = <string> s5;

    return s1 == s6 && s7 == s6 && s7 == s8 && s9 == s8;
}

//////////////////////// from float ////////////////////////

function testFloatAsString(float f1) returns (boolean, string) {
    string s3 = <string> f1;
    anydata s4 = <string> getFloat(f1);

    return (s3 == s4 && s4 is string, s3);
}

function testFloatInUnionAsString(float f1) returns (boolean, string) {
    Employee|string|float f2 = f1;
    json f3 = f1;
    anydata f4 = f1;
    any f5 = f1;

    string s6 = <string> f2;
    string s7 = <string> f3;
    string s8 = <string> f4;
    string s9 = <string> f5;

    return (s7 == s6 && s7 == s8 && s9 == s8, s6);
}

function testFloatAsFloat(float f1) returns (boolean, float) {
    float s3 = <float> f1;
    anydata s4 = <float> getFloat(f1);

    return (s3 == s4 && s4 is float, s3);
}

function testFloatInUnionAsFloat(float f1) returns (boolean, float) {
    Employee|string|float f2 = f1;
    json f3 = f1;
    anydata f4 = f1;
    any f5 = f1;

    float s6 = <float> f2;
    float s7 = <float> f3;
    float s8 = <float> f4;
    float s9 = <float> f5;

    return (s7 == s6 && s7 == s8 && s9 == s8, s6);
}

function testFloatAsDecimal(float f1) returns (boolean, decimal) {
    decimal s3 = <decimal> f1;
    anydata s4 = <decimal> getFloat(f1);

    return (s3 == s4 && s4 is decimal, s3);
}

function testFloatInUnionAsDecimal(float f1) returns (boolean, decimal) {
    Employee|string|float f2 = f1;
    json f3 = f1;
    anydata f4 = f1;
    any f5 = f1;

    decimal s6 = <decimal> f2;
    decimal s7 = <decimal> f3;
    decimal s8 = <decimal> f4;
    decimal s9 = <decimal> f5;

    return (s7 == s6 && s7 == s8 && s9 == s8, s6);
}

function testFloatAsInt(float f1) returns (boolean, int) {
    int s3 = <int> f1;
    anydata s4 = <int> getFloat(f1);

    return (s3 == s4 && s4 is int, s3);
}

function testFloatInUnionAsInt(float f1) returns (boolean, int) {
    Employee|string|float f2 = f1;
    json f3 = f1;
    anydata f4 = f1;
    any f5 = f1;

    int s6 = <int> f2;
    int s7 = <int> f3;
    int s8 = <int> f4;
    int s9 = <int> f5;

    return (s7 == s6 && s7 == s8 && s9 == s8, s6);
}

function testFloatAsBoolean() returns (boolean, boolean) {
    float f1 = 1.0;
    boolean s3 = <boolean> f1;
    any s4 = <boolean> getFloat(f1);
    boolean nonZeroAsBoolean = s3;
    if (s4 is boolean) {
        nonZeroAsBoolean = nonZeroAsBoolean && s4;
    }

    f1 = -134567.20;
    s3 = <boolean> f1;
    s4 = <boolean> getFloat(f1);
    nonZeroAsBoolean = nonZeroAsBoolean && s3;
    if (s4 is boolean) {
        nonZeroAsBoolean = nonZeroAsBoolean && s4;
    }

    f1 = 0.0;
    s3 = <boolean> f1;
    s4 = <boolean> getFloat(f1);
    boolean zeroAsBoolean = s3;
    if (s4 is boolean) {
        zeroAsBoolean = zeroAsBoolean || s4;
    }
    return (nonZeroAsBoolean, zeroAsBoolean);
}

function testFloatInUnionAsBoolean() returns (boolean, boolean) {
    float f1 = 1.0;
    float|Employee|int f2 = f1;
    json f3 = f1;
    anydata f4 = f1;
    any f5 = f1;

    boolean b1 = <boolean> f2;
    boolean b2 = <boolean> f3;
    boolean b3 = <boolean> f4;
    boolean b4 = <boolean> f5;
    boolean nonZeroAsBoolean = b1 && b2 && b3 && b4;

    f1 = -134567.20;
    f2 = f1;
    f3 = f1;
    f4 = f1;
    f5 = f1;
    b1 = <boolean> f2;
    b2 = <boolean> f3;
    b3 = <boolean> f4;
    b4 = <boolean> f5;
    nonZeroAsBoolean = nonZeroAsBoolean && b1 && b2 && b3 && b4;

    f1 = 0.0;
    f2 = f1;
    f3 = f1;
    f4 = f1;
    f5 = f1;
    b1 = <boolean> f2;
    b2 = <boolean> f3;
    b3 = <boolean> f4;
    b4 = <boolean> f5;
    return (nonZeroAsBoolean, b1 || b2 || b3 || b4);
}

//////////////////////// from decimal ////////////////////////

function testDecimalAsString(decimal f1) returns (boolean, string) {
    string s3 = <string> f1;
    anydata s4 = <string> getDecimal(f1);

    return (s3 == s4 && s4 is string, s3);
}

function testDecimalInUnionAsString(decimal f1) returns (boolean, string) {
    Employee|string|decimal f2 = f1;
    json f3 = f1;
    anydata f4 = f1;
    any f5 = f1;

    string s6 = <string> f2;
    string s7 = <string> f3;
    string s8 = <string> f4;
    string s9 = <string> f5;

    return (s7 == s6 && s7 == s8 && s9 == s8, s6);
}

function testDecimalAsFloat(decimal f1) returns (boolean, float) {
    float s3 = <float> f1;
    anydata s4 = <float> getDecimal(f1);

    return (s3 == s4 && s4 is float, s3);
}

function testDecimalInUnionAsFloat(decimal f1) returns (boolean, float) {
    Employee|string|decimal f2 = f1;
    json f3 = f1;
    anydata f4 = f1;
    any f5 = f1;

    float s6 = <float> f2;
    float s7 = <float> f3;
    float s8 = <float> f4;
    float s9 = <float> f5;

    return (s7 == s6 && s7 == s8 && s9 == s8, s6);
}

function testDecimalAsDecimal(decimal f1) returns (boolean, decimal) {
    decimal s3 = <decimal> f1;
    anydata s4 = <decimal> getDecimal(f1);

    return (s3 == s4 && s4 is decimal, s3);
}

function testDecimalInUnionAsDecimal(decimal f1) returns (boolean, decimal) {
    Employee|string|decimal f2 = f1;
    json f3 = f1;
    anydata f4 = f1;
    any f5 = f1;

    decimal s6 = <decimal> f2;
    decimal s7 = <decimal> f3;
    decimal s8 = <decimal> f4;
    decimal s9 = <decimal> f5;

    return (s7 == s6 && s7 == s8 && s9 == s8, s6);
}

function testDecimalAsInt(decimal f1) returns (boolean, int) {
    int s3 = <int> f1;
    anydata s4 = <int> getDecimal(f1);
    return (s3 == s4 && s4 is int, s3);
}

function testDecimalInUnionAsInt(decimal f1) returns (boolean, int) {
    Employee|string|decimal f2 = f1;
    json f3 = f1;
    anydata f4 = f1;
    any f5 = f1;

    int s6 = <int> f2;
    int s7 = <int> f3;
    int s8 = <int> f4;
    int s9 = <int> f5;

    return (s7 == s6 && s7 == s8 && s9 == s8, s6);
}

function testDecimalAsBoolean() returns (boolean, boolean) {
    decimal f1 = 1.0;
    boolean s3 = <boolean> f1;
    any s4 = <boolean> getDecimal(f1);
    boolean nonZeroAsBoolean = s3;
    if (s4 is boolean) {
        nonZeroAsBoolean = nonZeroAsBoolean && s4;
    }

    f1 = -134567.20;
    s3 = <boolean> f1;
    s4 = <boolean> getDecimal(f1);
    nonZeroAsBoolean = nonZeroAsBoolean && s3;
    if (s4 is boolean) {
        nonZeroAsBoolean = nonZeroAsBoolean && s4;
    }

    f1 = 0.0;
    s3 = <boolean> f1;
    s4 = <boolean> getDecimal(f1);
    boolean zeroAsBoolean = s3;
    if (s4 is boolean) {
        zeroAsBoolean = zeroAsBoolean || s4;
    }
    return (nonZeroAsBoolean, zeroAsBoolean);
}

function testDecimalInUnionAsBoolean() returns (boolean, boolean) {
    decimal f1 = 1.0;
    int|decimal|Employee f2 = f1;
    json f3 = f1;
    anydata f4 = f1;
    any f5 = f1;

    boolean b1 = <boolean> f2;
    boolean b2 = <boolean> f3;
    boolean b3 = <boolean> f4;
    boolean b4 = <boolean> f5;
    boolean nonZeroAsBoolean = b1 && b2 && b3 && b4;

    f1 = -134567.20;
    f2 = f1;
    f3 = f1;
    f4 = f1;
    f5 = f1;
    b1 = <boolean> f2;
    b2 = <boolean> f3;
    b3 = <boolean> f4;
    b4 = <boolean> f5;
    nonZeroAsBoolean = nonZeroAsBoolean && b1 && b2 && b3 && b4;

    f1 = 0.0;
    f2 = f1;
    f3 = f1;
    f4 = f1;
    f5 = f1;
    b1 = <boolean> f2;
    b2 = <boolean> f3;
    b3 = <boolean> f4;
    b4 = <boolean> f5;
    return (nonZeroAsBoolean, b1 || b2 || b3 || b4);
}

//////////////////////// from int ////////////////////////

function testIntAsString(int f1) returns (boolean, string) {
    string s3 = <string> f1;
    anydata s4 = <string> getInt(f1);

    return (s3 == s4 && s4 is string, s3);
}

function testIntInUnionAsString(int f1) returns (boolean, string) {
    Employee|string|int f2 = f1;
    json f3 = f1;
    anydata f4 = f1;
    any f5 = f1;

    string s6 = <string> f2;
    string s7 = <string> f3;
    string s8 = <string> f4;
    string s9 = <string> f5;

    return (s7 == s6 && s7 == s8 && s9 == s8, s6);
}

function testIntAsFloat(int f1) returns (boolean, float) {
    float s3 = <float> f1;
    anydata s4 = <float> getInt(f1);

    return (s3 == s4 && s4 is float, s3);
}

function testIntInUnionAsFloat(int f1) returns (boolean, float) {
    Employee|string|int f2 = f1;
    json f3 = f1;
    anydata f4 = f1;
    any f5 = f1;

    float s6 = <float> f2;
    float s7 = <float> f3;
    float s8 = <float> f4;
    float s9 = <float> f5;

    return (s7 == s6 && s7 == s8 && s9 == s8, s6);
}

function testIntAsDecimal(int f1) returns (boolean, decimal) {
    decimal s3 = <decimal> f1;
    anydata s4 = <decimal> getInt(f1);

    return (s3 == s4 && s4 is decimal, s3);
}

function testIntInUnionAsDecimal(int f1) returns (boolean, decimal) {
    Employee|string|int f2 = f1;
    json f3 = f1;
    anydata f4 = f1;
    any f5 = f1;

    decimal s6 = <decimal> f2;
    decimal s7 = <decimal> f3;
    decimal s8 = <decimal> f4;
    decimal s9 = <decimal> f5;

    return (s7 == s6 && s7 == s8 && s9 == s8, s6);
}

function testIntAsInt(int f1) returns (boolean, int) {
    int s3 = <int> f1;
    anydata s4 = <int> getInt(f1);
    return (s3 == s4 && s4 is int, s3);
}

function testIntInUnionAsInt(int f1) returns (boolean, int) {
    Employee|string|int f2 = f1;
    json f3 = f1;
    anydata f4 = f1;
    any f5 = f1;

    int s6 = <int> f2;
    int s7 = <int> f3;
    int s8 = <int> f4;
    int s9 = <int> f5;

    return (s7 == s6 && s7 == s8 && s9 == s8, s6);
}

function testIntAsBoolean() returns (boolean, boolean) {
    int f1 = 1;
    boolean s3 = <boolean> f1;
    any s4 = <boolean> getInt(f1);
    boolean nonZeroAsBoolean = s3;
    if (s4 is boolean) {
        nonZeroAsBoolean = nonZeroAsBoolean && s4;
    }

    f1 = -134567;
    s3 = <boolean> f1;
    s4 = <boolean> getInt(f1);
    nonZeroAsBoolean = nonZeroAsBoolean && s3;
    if (s4 is boolean) {
        nonZeroAsBoolean = nonZeroAsBoolean && s4;
    }

    f1 = 0;
    s3 = <boolean> f1;
    s4 = <boolean> getInt(f1);
    boolean zeroAsBoolean = s3;
    if (s4 is boolean) {
        zeroAsBoolean = zeroAsBoolean || s4;
    }
    return (nonZeroAsBoolean, zeroAsBoolean);
}

function testIntInUnionAsBoolean() returns (boolean, boolean) {
    int f1 = 1;
    int|decimal|Employee f2 = f1;
    json f3 = f1;
    anydata f4 = f1;
    any f5 = f1;

    boolean b1 = <boolean> f2;
    boolean b2 = <boolean> f3;
    boolean b3 = <boolean> f4;
    boolean b4 = <boolean> f5;
    boolean nonZeroAsBoolean = b1 && b2 && b3 && b4;

    f1 = -134567;
    f2 = f1;
    f3 = f1;
    f4 = f1;
    f5 = f1;
    b1 = <boolean> f2;
    b2 = <boolean> f3;
    b3 = <boolean> f4;
    b4 = <boolean> f5;
    nonZeroAsBoolean = nonZeroAsBoolean && b1 && b2 && b3 && b4;

    f1 = 0;
    f2 = f1;
    f3 = f1;
    f4 = f1;
    f5 = f1;
    b1 = <boolean> f2;
    b2 = <boolean> f3;
    b3 = <boolean> f4;
    b4 = <boolean> f5;
    return (nonZeroAsBoolean, b1 || b2 || b3 || b4);
}

//////////////////////// from boolean ////////////////////////

function testBooleanAsString() returns (string, string, string, string) {
    boolean b1 = true;
    string s1 = <string> b1;
    anydata a = <string> getBoolean(b1);
    string s2 = "";
    if (a is string) {
        s2 = a;
    }

    b1 = false;
    string s3 = <string> b1;
    string s4 = <string> getBoolean(b1);

    return (s1, s2, s3, s4);
}

function testBooleanInUnionAsString() returns (string, string) {
    boolean f1 = true;
    Employee|string|int|boolean f2 = f1;
    json f3 = true;
    anydata f4 = f1;
    any f5 = f1;

    string s6 = <string> f2;
    string s7 = <string> f3;
    string s8 = <string> f4;
    string s9 = <string > f5;

    string st1 = (s7 == s6 && s7 == s8 && s9 == s8) ? s6 : "";

    f1 = false;
    f2 = f1;
    f3 = false;
    f4 = f1;
    f5 = f1;

    s6 = <string> f2;
    s7 = <string> f3;
    s8 = <string> f4;
    s9 = <string> f5;

    string st2 = (s7 == s6 && s7 == s8 && s9 == s8) ? s6 : "";

    return(st1, st2);
}

function testBooleanAsFloat() returns (float, float, float, float) {
    boolean b1 = true;
    float s1 = <float> b1;
    anydata a = <float> getBoolean(b1);
    float s2 = 0.0;
    if (a is float) {
        s2 = a;
    }

    b1 = false;
    float s3 = <float> b1;
    float s4 = <float> getBoolean(b1);

    return (s1, s2, s3, s4);
}

function testBooleanInUnionAsFloat() returns (float, float) {
    boolean f1 = true;
    Employee|string|int|boolean f2 = f1;
    json f3 = true;
    anydata f4 = f1;
    any f5 = f1;

    float s6 = <float> f2;
    float s7 = <float> f3;
    float s8 = <float> f4;
    float s9 = <float > f5;

    float ft1 = (s7 == s6 && s7 == s8 && s9 == s8) ? s6 : 1.23456;

    f1 = false;
    f2 = f1;
    f3 = false;
    f4 = f1;
    f5 = f1;

    s6 = <float> f2;
    s7 = <float> f3;
    s8 = <float> f4;
    s9 = <float> f5;

    float ft2 = (s7 == s6 && s7 == s8 && s9 == s8) ? s6 : 1.23456;

    return(ft1, ft2);
}

function testBooleanAsDecimal() returns (decimal, decimal, decimal, decimal) {
    boolean b1 = true;
    decimal s1 = <decimal> b1;
    anydata a = <decimal> getBoolean(b1);
    decimal s2 = 0.0;
    if (a is decimal) {
        s2 = a;
    }

    b1 = false;
    decimal s3 = <decimal> b1;
    decimal s4 = <decimal> getBoolean(b1);

    return (s1, s2, s3, s4);
}

function testBooleanInUnionAsDecimal() returns (decimal, decimal) {
    boolean f1 = true;
    Employee|string|int|boolean f2 = f1;
    json f3 = true;
    anydata f4 = f1;
    any f5 = f1;

    decimal s6 = <decimal> f2;
    decimal s7 = <decimal> f3;
    decimal s8 = <decimal> f4;
    decimal s9 = <decimal > f5;

    decimal ft1 = (s7 == s6 && s7 == s8 && s9 == s8) ? s6 : 1.23456;

    f1 = false;
    f2 = f1;
    f3 = false;
    f4 = f1;
    f5 = f1;

    s6 = <decimal> f2;
    s7 = <decimal> f3;
    s8 = <decimal> f4;
    s9 = <decimal> f5;

    decimal ft2 = (s7 == s6 && s7 == s8 && s9 == s8) ? s6 : 1.23456;

    return(ft1, ft2);
}

function testBooleanAsInt() returns (int, int, int, int) {
    boolean b1 = true;
    int s1 = <int> b1;
    anydata a = <int> getBoolean(b1);
    int s2 = 0;
    if (a is int) {
        s2 = a;
    }

    b1 = false;
    int s3 = <int> b1;
    int s4 = <int> getBoolean(b1);

    return (s1, s2, s3, s4);
}

function testBooleanInUnionAsInt() returns (int, int) {
    boolean f1 = true;
    Employee|string|int|boolean f2 = f1;
    json f3 = true;
    anydata f4 = f1;
    any f5 = f1;

    int s6 = <int> f2;
    int s7 = <int> f3;
    int s8 = <int> f4;
    int s9 = <int > f5;

    int ft1 = (s7 == s6 && s7 == s8 && s9 == s8) ? s6 : 123456;

    f1 = false;
    f2 = f1;
    f3 = false;
    f4 = f1;
    f5 = f1;

    s6 = <int> f2;
    s7 = <int> f3;
    s8 = <int> f4;
    s9 = <int> f5;

    int ft2 = (s7 == s6 && s7 == s8 && s9 == s8) ? s6 : 123456;

    return(ft1, ft2);
}

function testBooleanAsBoolean() returns (boolean, boolean, boolean, boolean) {
    boolean b1 = true;
    boolean s1 = <boolean> b1;
    anydata a = <boolean> getBoolean(b1);
    boolean s2 = false;
    if (a is boolean) {
        s2 = a;
    }

    b1 = false;
    boolean s3 = <boolean> b1;
    boolean s4 = <boolean> getBoolean(b1);

    return (s1, s2, s3, s4);
}

function testBooleanInUnionAsBoolean() returns (boolean, boolean) {
    boolean f1 = true;
    Employee|string|int|boolean f2 = f1;
    json f3 = true;
    anydata f4 = f1;
    any f5 = f1;

    boolean s6 = <boolean> f2;
    boolean s7 = <boolean> f3;
    boolean s8 = <boolean> f4;
    boolean s9 = <boolean > f5;

    boolean ft1 = (s7 == s6 && s7 == s8 && s9 == s8) ? s6 : false;

    f1 = false;
    f2 = f1;
    f3 = false;
    f4 = f1;
    f5 = f1;

    s6 = <boolean> f2;
    s7 = <boolean> f3;
    s8 = <boolean> f4;
    s9 = <boolean> f5;

    boolean ft2 = (s7 == s6 && s7 == s8 && s9 == s8) ? s6 : true;

    return(ft1, ft2);
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

function testInfiniteInUnionFloatAsInt() {
    boolean|float f = 1.0/0;
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

function testExplicitlyTypedExprForExactValues() returns error? {
    // test from string to string
    string s = "hello";
    string s0 = <string> s;
    if (s != s0) {
        error e = error("invalid resultant value, from string to string");
        return e;
    }

    // test from float
    float f = 12.345;
    decimal fd = 12.34500000000000063948846218409017;

    string s1 = <string> f;
    if (s1 != "12.345") {
        error e = error("invalid resultant value, from float to string");
        return e;
    }

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

    boolean b1 = <boolean> f;
    if (b1 != true) {
        error e = error("invalid resultant value, from float to boolean");
        return e;
    }

    // test from decimal
    decimal d = 12.345;

    string s2 = <string> d;
    if (s2 != "12.345") {
        error e = error("invalid resultant value, from decimal to string");
        return e;
    }

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

    boolean b2 = <boolean> d;
    if (b2 != true) {
        error e = error("invalid resultant value, from decimal to boolean");
        return e;
    }

    // test from int
    int i = 12345;
    float intf = 12345.0;
    decimal intd = 12345;

    string s3 = <string> i;
    if (s3 != "12345") {
        error e = error("invalid resultant value, from int to string");
        return e;
    }

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

    boolean b3 = <boolean> i;
    if (b3 != true) {
        error e = error("invalid resultant value, from int to boolean");
        return e;
    }

    // test from boolean
    boolean b = false;
    float boolf = 0.0;
    decimal boold = 0.0;

    string s4 = <string> b;
    if (s4 != "false") {
        error e = error("invalid resultant value, from boolean to string");
        return e;
    }

    float f4 = <float> b;
    if (f4 != boolf) {
        error e = error("invalid resultant value, from boolean to float");
        return e;
    }

    decimal d4 = <decimal> b;
    if (d4 != boold) {
        error e = error("invalid resultant value, from boolean to decimal");
        return e;
    }

    int i4 = <int> b;
    if (i4 != 0) {
        error e = error("invalid resultant value, from boolean to decimal");
        return e;
    }

    boolean b4 = <boolean> b;
    if (b4 != b) {
        error e = error("invalid resultant value, from boolean to boolean");
        return e;
    }

    b = true;
    boolf = 1.0;
    boold = 1.0;

    s4 = <string> b;
    if (s4 != "true") {
        error e = error("invalid resultant value, from boolean to string");
        return e;
    }

    f4 = <float> b;
    if (f4 != boolf) {
        error e = error("invalid resultant value, from boolean to float");
        return e;
    }

    d4 = <decimal> b;
    if (d4 != boold) {
        error e = error("invalid resultant value, from boolean to decimal");
        return e;
    }

    i4 = <int> b;
    if (i4 != 1) {
        error e = error("invalid resultant value, from boolean to decimal");
        return e;
    }

    b4 = <boolean> b;
    if (b4 != b) {
        error e = error("invalid resultant value, from boolean to boolean");
        return e;
    }
    return;
}

function init(InMemoryModeConfig|ServerModeConfig|EmbeddedModeConfig rec) returns string {
    if (rec is ServerModeConfig) {
        return "Server mode configuration";
    } else if (rec is EmbeddedModeConfig) {
        return "Embedded mode configuration";
    } else {
        return "In-memory mode configuration";
    }
}

public type InMemoryModeConfig record {
    string name = "";
    string username = "";
    string password = "";
    map<any> dbOptions = {name:"asdf"};
    !...;
};

public type ServerModeConfig record {
    string host = "";
    int port = 9090;
    *InMemoryModeConfig;
    !...;
};

public type EmbeddedModeConfig record {
    string path = "";
    *InMemoryModeConfig;
    !...;
};

function testTypeAssertionOnRecordLiterals() returns (string, string, string) {
    string s1 = init(<ServerModeConfig>{});
    string s2 = init(<EmbeddedModeConfig>{});
    string s3 = init(<InMemoryModeConfig>{});
    return (s1, s2, s3);
}

function getString(string s) returns string {
    return s;
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

function getBoolean(boolean b) returns boolean {
    return b;
}

type Employee record {
    string name;
};
