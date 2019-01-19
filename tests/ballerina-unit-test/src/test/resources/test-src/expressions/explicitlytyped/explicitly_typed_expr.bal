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

function testFloatAsInt(float f1) returns (boolean, int) {
    int s3 = <int> f1;
    anydata s4 = <int> getFloat(f1);

    return (s3 == s4 && s4 is int, s3);
}

//////////////////////// from decimal ////////////////////////
function testDecimalAsFloat(decimal f1) returns (boolean, float) {
    float s3 = <float> f1;
    anydata s4 = <float> getDecimal(f1);

    return (s3 == s4 && s4 is float, s3);
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

//////////////////////// from int ////////////////////////
function testIntAsFloat(int f1) returns (boolean, float) {
    float s3 = <float> f1;
    anydata s4 = <float> getInt(f1);

    return (s3 == s4 && s4 is float, s3);
}

function testIntAsDecimal(int f1) returns (boolean, decimal) {
    decimal s3 = <decimal> f1;
    anydata s4 = <decimal> getInt(f1);

    return (s3 == s4 && s4 is decimal, s3);
}

function testIntAsDecimalInUnion(int f1) returns (boolean, string|decimal) {
    string|decimal d1 = <string|decimal> f1;
    boolean|decimal d2 = <boolean|decimal> getInt(f1);
    return (d1 == d2 && d2 is decimal, d1);
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

//////////////////////// from boolean ////////////////////////

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
    boolean s9 = <boolean> f5;

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

    // test from int
    int i = 12345;
    float intf = 12345.0;
    decimal intd = 12345;

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

    // test from boolean
    boolean b = false;
    float boolf = 0.0;
    decimal boold = 0.0;

    boolean b4 = <boolean> b;
    if (b4 != b) {
        error e = error("invalid resultant value, from boolean to boolean");
        return e;
    }

    b = true;
    boolf = 1.0;
    boold = 1.0;

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

function testConversionFromUnionWithNumericBasicTypes() returns boolean {
    int|boolean u1 = 12;
    float f1 = <float> u1;
    boolean conversionSuccessful = f1 == <float> 12.0;

    float|int|string u2 = <float> 110.5;
    decimal|boolean d1 = <decimal|boolean> u2;
    return conversionSuccessful && d1 == <decimal> 110.5;
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
