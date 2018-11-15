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

//////////////////////// from float ////////////////////////

function testFloatAsString(float f1) returns (boolean, string) {
    string s3 = <string> f1;
    anydata s4 = <string> getFloat(f1);

    return (s3 == s4 && s4 is string, s3);
}

function testFloatAsFloat(float f1) returns (boolean, float) {
    float s3 = <float> f1;
    anydata s4 = <float> getFloat(f1);

    return (s3 == s4 && s4 is float, s3);
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

//////////////////////// from decimal ////////////////////////

function testDecimalAsString(decimal f1) returns (boolean, string) {
    string s3 = <string> f1;
    anydata s4 = <string> getDecimal(f1);

    return (s3 == s4 && s4 is string, s3);
}

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

function testDecimalAsInt(decimal f1) returns (boolean, int) {
    int s3 = <int> f1;
    anydata s4 = <int> getDecimal(f1);
    return (s3 == s4 && s4 is int, s3);
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

//////////////////////// from int ////////////////////////

function testIntAsString(int f1) returns (boolean, string) {
    string s3 = <string> f1;
    anydata s4 = <string> getInt(f1);

    return (s3 == s4 && s4 is string, s3);
}

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

function testIntAsInt(int f1) returns (boolean, int) {
    int s3 = <int> f1;
    anydata s4 = <int> getInt(f1);
    return (s3 == s4 && s4 is int, s3);
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

//////////////////////// from boolean ////////////////////////

function testBooleanAsString() returns (string, string, string, string) {
    boolean b1 = true;
    string s1 = <string> b1;
    anydata a = <string> getBoolean(b1);
    string s2;
    if (a is string) {
        s2 = a;
    }

    b1 = false;
    string s3 = <string> b1;
    string s4 = <string> getBoolean(b1);

    return (s1, s2, s3, s4);
}

function testBooleanAsFloat() returns (float, float, float, float) {
    boolean b1 = true;
    float s1 = <float> b1;
    anydata a = <float> getBoolean(b1);
    float s2;
    if (a is float) {
        s2 = a;
    }

    b1 = false;
    float s3 = <float> b1;
    float s4 = <float> getBoolean(b1);

    return (s1, s2, s3, s4);
}

function testBooleanAsDecimal() returns (decimal, decimal, decimal, decimal) {
    boolean b1 = true;
    decimal s1 = <decimal> b1;
    anydata a = <decimal> getBoolean(b1);
    decimal s2;
    if (a is decimal) {
        s2 = a;
    }

    b1 = false;
    decimal s3 = <decimal> b1;
    decimal s4 = <decimal> getBoolean(b1);

    return (s1, s2, s3, s4);
}

function testBooleanAsInt() returns (int, int, int, int) {
    boolean b1 = true;
    int s1 = <int> b1;
    anydata a = <int> getBoolean(b1);
    int s2;
    if (a is int) {
        s2 = a;
    }

    b1 = false;
    int s3 = <int> b1;
    int s4 = <int> getBoolean(b1);

    return (s1, s2, s3, s4);
}

function testBooleanAsBoolean() returns (boolean, boolean, boolean, boolean) {
    boolean b1 = true;
    boolean s1 = <boolean> b1;
    anydata a = <boolean> getBoolean(b1);
    boolean s2;
    if (a is boolean) {
        s2 = a;
    }

    b1 = false;
    boolean s3 = <boolean> b1;
    boolean s4 = <boolean> getBoolean(b1);

    return (s1, s2, s3, s4);
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
