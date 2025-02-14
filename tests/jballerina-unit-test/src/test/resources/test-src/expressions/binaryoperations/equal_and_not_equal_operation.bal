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

import ballerina/jballerina.java;
import ballerina/test;

type OpenEmployee record {
    string|json name = "";
    int id = 0;
};

type OpenPerson record {
    string name;
};

type ClosedEmployee record {|
    string name = "";
    int id = 0;
|};

type OpenEmployeeTwo record {|
    string name;
    int...;
|};

type OpenRecordWithOptionalFieldOne record {
    string name;
    int one?;
};

type OpenRecordWithOptionalFieldTwo record {
    string name;
    int two?;
};

type ClosedRecordWithOptionalFieldOne record {|
    string name;
    int one?;
|};

type ClosedRecordWithOptionalFieldTwo record {|
    string name;
    int two?;
|};

function checkBooleanEquality() {
    boolean a = true;
    boolean b = false;
    test:assertTrue((a == a) && !(a != a));
    test:assertTrue((b == b) && !(b != b));
    test:assertFalse((a == b) || !(a != b));
    test:assertFalse((b == a) || !(b != a));
}

function checkIntEquality() {
    int a = 10;
    int b = 20193746;
    test:assertTrue((a == a) && !(a != a));
    test:assertTrue((b == b) && !(b != b));
    test:assertFalse((a == b) || !(a != b));
    test:assertFalse((b == a) || !(b != a));
}

function checkByteEquality() {
    byte a = 0;
    byte b = 10;
    byte c = 255;
    test:assertTrue((a == a) && !(a != a));
    test:assertTrue((b == b) && !(b != b));
    test:assertTrue((c == c) && !(c != c));
    test:assertFalse((a == b) || !(a != b));
    test:assertFalse((b == a) || !(b != a));
    test:assertFalse((a == c) || !(a != c));
    test:assertFalse((c == a) || !(c != a));
    test:assertFalse((b == c) || !(b != c));
    test:assertFalse((c == b) || !(c != b));
}

function checkFloatEquality() {
    float a = 5.3;
    float b = 201937.46;
    test:assertTrue((a == a) && !(a != a));
    test:assertTrue((b == b) && !(b != b));
    test:assertFalse((a == b) || !(a != b));
    test:assertFalse((b == a) || !(b != a));
}

function checkDecimalEquality() {
    decimal d1 = 0d;
    decimal d2 = 0.0d;
    decimal d3 = 1.0001;
    decimal d4 = 1.000100;
    test:assertTrue((d1 == d2) && !(d1 != d2));
    test:assertTrue((d3 == d4) && !(d3 != d4));
    test:assertFalse((d1 == d3) && !(d4 != d2));
}

function checkStringEquality() {
    string a = "a";
    string b = "Hello, from Ballerina!";
    test:assertTrue((a == a) && !(a != a));
    test:assertTrue((b == b) && !(b != b));
    test:assertFalse((a == b) || !(a != b));
    test:assertFalse((b == a) || !(b != a));
}

function checkEqualityToNil() {
    any a = ();
    test:assertTrue((a == ()) && !(a != ()));
    a = true;
    test:assertFalse((a == ()) || !(a != ()));
    a = false;
    test:assertFalse((a == ()) || !(a != ()));
    a = 5;
    test:assertFalse((a == ()) || !(a != ()));
    a = 5.0;
    test:assertFalse((a == ()) || !(a != ()));
    a = "Hi from Ballerina!";
    test:assertFalse((a == ()) || !(a != ()));
    a = {name: "John Doe"};
    test:assertFalse((a == ()) || !(a != ()));
}

function checkAnyDataEquality() {
    anydata a = 10;
    anydata b = 10;
    int c = 10;
    byte d = 10;

    test:assertTrue((a == b) && !(a != b));
    test:assertTrue((a == c) && !(a != c));
    test:assertTrue((a == d) && !(a != d));

    float e = 10;
    decimal f = 10;
    anydata g = 12;
    test:assertFalse((a == e) || !(a != e));
    test:assertFalse((a == f) || !(a != f));
    test:assertFalse((a == g) || !(a != g));
}

type IntOne 1;
type FloatOne 1.0;
type IntTwo 2;
type FloatTwo 2f;

function checkFiniteTypeEquality() {
    IntOne intOne_1 = 1;
    IntOne intOne_2 = 1;
    IntTwo intTwo = 2;
    FloatOne floatOne = 1f;
    FloatTwo floatTwo = 2.0;

    test:assertTrue((intOne_1 == intOne_2) && !(intOne_1 != intOne_2));
    test:assertTrue((floatOne != floatTwo) && !(floatOne == floatTwo));
    test:assertFalse((intOne_1 == intTwo) && !(intOne_1 != intTwo));
}

type ErrorDetail record {
    string message?;
    error cause?;
    int intVal?;
};

function checkOpenRecordEqualityPositive() {
    OpenEmployee e1 = {name: "Em", id: 4000};
    OpenEmployee e2 = e1;

    OpenEmployee e3 = {name: "Em"};
    OpenPerson e4 = {name: "Em", "id": 0};

    OpenEmployee e5 = {name: "Em", id: 4000, "dept": "finance"};
    OpenEmployee e6 = {name: "Em", id: 4000, "dept": "finance"};

    OpenEmployee e7 = {};
    OpenEmployee e8 = {};

    test:assertTrue((e1 == e2) && !(e1 != e2) && isEqual(e3, e4) && (e5 == e6) && !(e5 != e6) && (e7 == e8) &&
    !(e7 != e8));
}

function checkOpenRecordEqualityNegative() {
    OpenEmployee e1 = {name: "Em", id: 4000};
    OpenEmployee e2 = {};

    OpenEmployee e3 = {name: "Em", id: 4000};
    OpenEmployee e4 = {name: "Em", "area": 51};

    OpenEmployee e5 = {name: "Em"};
    OpenPerson e6 = {name: "Em"};

    OpenEmployee e7 = {name: "Em", id: 4000, "dept": "finance"};
    OpenEmployee e8 = {name: "Em", id: 4000, "dept": "hr"};

    test:assertFalse((e1 == e2) || !(e1 != e2) || (e3 == e4) || !(e3 != e4) || isEqual(e5, e6) || (e7 == e8) || !(e7 != e8));
}

function testOpenRecordWithOptionalFieldsEqualityPositive() {
    OpenRecordWithOptionalFieldOne e1 = {name: "Em", one: 4000, "two": 3000};
    OpenRecordWithOptionalFieldOne e2 = e1;

    OpenRecordWithOptionalFieldOne e3 = {name: "Em"};
    OpenRecordWithOptionalFieldTwo e4 = {name: "Em"};

    OpenRecordWithOptionalFieldOne e5 = {name: "Em", one: 4000, "two": 3000};
    OpenRecordWithOptionalFieldTwo e6 = {name: "Em", two: 3000, "one": 4000};

    test:assertTrue((e1 == e2) && !(e1 != e2) && isEqual(e3, e4) && (e5 == e6) && !(e5 != e6));
}

function testOpenRecordWithOptionalFieldsEqualityNegative() {
    OpenRecordWithOptionalFieldOne e1 = {name: "Em"};
    OpenRecordWithOptionalFieldTwo e2 = {name: "Zee"};

    OpenRecordWithOptionalFieldOne e3 = {name: "Em", one: 4000};
    OpenRecordWithOptionalFieldTwo e4 = {name: "Em", two: 4000};

    test:assertFalse((e1 == e2) || !(e1 != e2) || (e3 == e4) || !(e3 != e4));
}

function testClosedRecordWithOptionalFieldsEqualityPositive() {
    ClosedRecordWithOptionalFieldOne e1 = {name: "Em", one: 4000};
    ClosedRecordWithOptionalFieldOne e2 = e1;

    ClosedRecordWithOptionalFieldOne e3 = {name: "Em"};
    ClosedRecordWithOptionalFieldTwo e4 = {name: "Em"};

    test:assertTrue((e1 == e2) && !(e1 != e2) && isEqual(e3, e4));
}

function testClosedRecordWithOptionalFieldsEqualityNegative() {
    ClosedRecordWithOptionalFieldOne e1 = {name: "Em"};
    ClosedRecordWithOptionalFieldTwo e2 = {name: "Zee"};

    ClosedRecordWithOptionalFieldOne e3 = {name: "Em", one: 4000};
    ClosedRecordWithOptionalFieldTwo e4 = {name: "Em", two: 4000};

    test:assertFalse((e1 == e2) || !(e1 != e2) || (e3 == e4) || !(e3 != e4));
}

function checkClosedRecordEqualityPositive() {
    ClosedEmployee e1 = {name: "Em", id: 4000};
    ClosedEmployee e2 = {name: "Em", id: 4000};

    ClosedEmployee e3 = {name: "Em"};
    ClosedEmployee e4 = {name: "Em"};

    ClosedEmployee e5 = {};
    ClosedEmployee e6 = {};

    test:assertTrue(isEqual(e1, e2) && (e3 == e4) && !(e3 != e4) && (e5 == e6) && !(e5 != e6));
}

function checkClosedRecordEqualityNegative() {
    ClosedEmployee e1 = {name: "Em", id: 4000};
    ClosedEmployee e2 = {};

    ClosedEmployee e3 = {name: "Em", id: 4000};
    ClosedEmployee e4 = {name: "Em"};

    ClosedEmployee e5 = {name: "Em"};
    ClosedEmployee e6 = {name: "Em", id: 4100};

    test:assertFalse((e1 == e2) || !(e1 != e2) || isEqual(e3, e4) || (e5 == e6) || !(e5 != e6));
}

function check1DArrayEqualityPositive(boolean[]|int[]|float[]|string[] a, boolean[]|int[]|float[]|string[] b) {
    test:assertTrue((a == b) && !(a != b));
}

function check1DArrayEqualityNegative(boolean[]|int[]|float[]|string[] a, boolean[]|int[]|float[]|string[] b) {
    test:assertFalse((a == b) || !(a != b));
}

function check1DClosedArrayEqualityPositive() {
    boolean[4] b1 = [false, false, false, false];
    boolean[4] b2 = [false, false, false, false];

    boolean[3] b3 = [true, false, false];
    boolean[*] b4 = [true, false, false];

    int[5] i1 = [0, 0, 0, 0, 0];
    int[5] i2 = [0, 0, 0, 0, 0];

    int[2] i3 = [123, 45678];
    int[2] i4 = [123, 45678];

    float[8] f1 = [0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0];
    float[8] f2 = [0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0];

    float[1] f3 = [12.3];
    float[*] f4 = [12.3];

    byte[3] by1 = [0, 0, 0];
    byte[3] by2 = [0, 0, 0];

    byte[4] by3 = [0, 10, 100, 255];
    byte[*] by4 = [0, 10, 100, 255];

    string[3] s1 = ["", "", ""];
    string[3] s2 = ["", "", ""];

    string[3] s3 = ["hello", "from", "ballerina"];
    string[3] s4 = ["hello", "from", "ballerina"];

    anydata[7] a1 = [(), (), (), (), (), (), ()];
    anydata[7] a2 = [(), (), (), (), (), (), ()];

    json j1 = {hello: "world", lang: "ballerina"};
    json j2 = {hello: "world", lang: "ballerina"};

    map<anydata> m1 = {key1: "val1", key2: 2, key3: 3.1};
    map<anydata> m2 = {key1: "val1", key2: 2, key3: 3.1};

    anydata[6] a3 = ["hi", 1, true, 54.3, j1, m1];
    anydata[6] a4 = ["hi", 1, true, 54.3, j2, m2];

    test:assertTrue((b1 == b2) && !(b1 != b2) && (b3 == b4) && !(b3 != b4) &&
        (i1 == i2) && !(i1 != i2) && (i3 == i4) && !(i3 != i4) &&
        (by1 == by2) && !(by1 != by2) && (by3 == by4) && !(by3 != by4) &&
        isEqual(f1, f2) && (f3 == f4) && !(f3 != f4) &&
        (s1 == s2) && !(s1 != s2) && isEqual(s3, s4) &&
        (a1 == a2) && !(a1 != a2) && (a3 == a4) && !(a3 != a4));
}

function check1DClosedArrayEqualityNegative() {
    boolean[3] b1 = [true, false, false];
    boolean[*] b2 = [true, false, true];

    int[2] i1 = [123, 45678];
    int[2] i2 = [123, 45674];

    byte[4] by1 = [123, 145, 255, 0];
    byte[4] by2 = [123, 45, 255, 0];

    float[1] f1 = [12.3];
    float[*] f2 = [12.2];

    string[3] s1 = ["hello", "from", "ballerina"];
    string[3] s2 = ["hello", "from", "ball"];

    json j1 = {hello: "world", lang: "ball"};
    json j2 = {hello: "world", lang: "ballerina"};

    map<anydata> m1 = {key1: "val1", key2: 2, key3: 3.1};
    map<anydata> m2 = {key1: "val1", key2: 2, key3: 3.1};

    anydata[6] a1 = ["hi", 1, true, 54.3, j1, m1];
    anydata[6] a2 = ["hi", 1, true, 54.3, j2, m2];

    test:assertFalse((b1 == b2) || !(b1 != b2) || (i1 == i2) || !(i1 != i2) || (by1 == by2) || !(by1 != by2) ||
        (f1 == f2) || !(f1 != f2) || isEqual(s1, s2) || (a1 == a2) || !(a1 != a2));
}

function check1DAnyArrayEqualityPositive() {
    json j1 = {hello: "world", lang: "ballerina"};
    json j2 = {hello: "world", lang: "ballerina"};

    map<anydata> m1 = {key1: "val1", key2: 2, key3: 3.1};
    map<anydata> m2 = {key1: "val1", key2: 2, key3: 3.1};

    anydata[] a = ["hi", 1, j1, 2.3, false, m1];
    anydata[] b = ["hi", 1, j2, 2.3, false, m2];

    test:assertTrue((a == b) && !(a != b));
}

function check1DAnyArrayEqualityNegative() {
    json j1 = {hello: "world", lang: "ballerina"};
    json j2 = {hello: "world", lang: "ballerina"};

    map<anydata> m1 = {key1: "val1", key2: 2, key3: 3.1};
    map<anydata> m2 = {key1: "val1", key2: 2, key3: 3.1};

    anydata[] a = ["hi", 1, j1, 2.1, false, m1];
    anydata[] b = ["hi", 1, j2, 2.3, false, m2];

    test:assertFalse((a == b) || !(a != b));
}

function checkOpenClosedArrayEqualityPositive() {
    string[*] a = ["a", "bcd", "ef"];
    string[] b = ["a", "bcd", "ef"];

    (int|float)?[] c = [5, 4.12, 54, 23.1];
    (float|int)?[4] d = [5, 4.12, 54, 23.1];

    test:assertTrue((a == b) && !(a != b) && (c == d) && !(c != d));
}

function checkOpenClosedArrayEqualityNegative() {
    boolean[] a = [true, false];
    boolean[3] b = [true, false, false];

    string[] c = ["true", "false", "false", "true"];
    string[4] d = ["true", "false", "false", "false"];

    test:assertFalse((a == b) || !(a != b) || (c == d) || !(c != d));
}

function check2DBooleanArrayEqualityPositive() {
    boolean[][] b1 = [[], [true, false, false], [false]];
    boolean[][] b2 = [[], [true, false, false], [false]];

    test:assertTrue(b1 == b2 && !(b1 != b2));
}

function check2DBooleanArrayEqualityNegative() {
    boolean[][] b1 = [[], [true, false, false], [false]];
    boolean[][] b2 = [[], [false, false, false], [false]];

    boolean[][] b3 = [[], [true, false, false], [false]];
    boolean[][] b4 = [[], [true, false, false]];

    test:assertFalse(b1 == b2 || !(b1 != b2) || b3 == b4 || !(b3 != b4));
}

function check2DIntArrayEqualityPositive() {
    int[][] i1 = [[1], [], [100, 1200000, 9475883]];
    int[][] i2 = [[1], [], [100, 1200000, 9475883]];

    test:assertTrue(i1 == i2 && !(i1 != i2));
}

function check2DIntArrayEqualityNegative() {
    int[][] i1 = [[], [100, 2222, 111102], [294750]];
    int[][] i2 = [[], [100, 2222, 2349586], [294750]];

    int[][] i3 = [[1], [], [100, 1200000, 9475883]];
    int[][] i4 = [[1], []];

    test:assertFalse(i1 == i2 || !(i1 != i2) || i3 == i4 || !(i3 != i4));
}

function check2DByteArrayEqualityPositive() {
    byte[][] b1 = [[1, 100], [0, 255, 45], []];
    byte[][] b2 = [[1, 100], [0, 255, 45], []];

    test:assertTrue(b1 == b2 && !(b1 != b2));
}

function check2DByteArrayEqualityNegative() {
    byte[][] b1 = [[1, 100], [0, 255, 145], []];
    byte[][] b2 = [[1, 100], [0, 255, 45], []];

    byte[][] b3 = [[1, 100], [0, 255, 45], [23, 234]];
    byte[][] b4 = [[1, 100], [0, 255, 45]];

    test:assertFalse(b1 == b2 || !(b1 != b2) || b3 == b4 || !(b3 != b4));
}

function check2DFloatArrayEqualityPositive() {
    float[][] f1 = [[1.221], [], [10.0, 123.45, 9.475883]];
    float[][] f2 = [[1.221], [], [10.0, 123.45, 9.475883]];

    test:assertTrue(f1 == f2 && !(f1 != f2));
}

function check2DFloatArrayEqualityNegative() {
    float[][] f1 = [[1.221], [], [10.0, 123.45, 9.475883]];
    float[][] f2 = [[1.221], [], [10.0, 1.2, 9.475883]];

    float[][] f3 = [[1.221], [], [10.0, 123.45, 9.475883]];
    float[][] f4 = [[1.221], []];

    test:assertFalse(f1 == f2 || !(f1 != f2) || f3 == f4 || !(f3 != f4));
}

function check2DStringArrayEqualityPositive() {
    string[][] s1 = [[], ["hello world", "from", "ballerina"], ["ballet shoes"]];
    string[][] s2 = [[], ["hello world", "from", "ballerina"], ["ballet shoes"]];

    test:assertTrue(s1 == s2 && !(s1 != s2));
}

function check2DStringArrayEqualityNegative() {
    string[][] s1 = [[], ["hello", "from", "ballerina"], ["ballet shoes"]];
    string[][] s2 = [[], ["hi", "from", "ballerina"], ["ballet shoes"]];

    string[][] s3 = [[], ["hello", "from", "ballerina"], ["ballet shoes"]];
    string[][] s4 = [[], ["hello", "from", "ballerina"]];

    test:assertFalse(s1 == s2 || !(s1 != s2) || s3 == s4 || !(s3 != s4));
}

function checkComplex2DArrayEqualityPositive() {
    [int, float][][] a = [];
    [int|string, float]?[][] b = [];

    boolean 'equals = a == b && !(a != b);

    a = [[[1, 3.0]], [[123, 65.4], [234, 23.22]]];
    b[0] = [[1, 3.0]];
    b[1] = [[123, 65.4], [234, 23.22]];

    test:assertTrue('equals && a == b && !(a != b));
}

function checkComplex2DArrayEqualityNegative() {
    [int, float][][] a = [[[123, 65.4], [234, 23.22]]];
    [int|string, float]?[][] b = [[[124, 65.4], [234, 23.22]]];

    boolean 'equals = a == b || !(a != b);

    b = [[[123, 65.4], [234, 23.22]]];
    b[2] = [[123, 65.4], [234, 23.22]];

    test:assertFalse('equals || a == b || !(a != b));
}

function checkMapEqualityPositive() {
    map<anydata> m1 = {};
    map<anydata> m2 = {};

    map<string> m3 = {};
    map<string> m4 = {};

    map<float> m5 = {};
    map<float> m6 = {};

    boolean 'equals = m1 == m2 && !(m1 != m2) && m3 == m4 && !(m3 != m4);

    m1["one"] = 1;
    m2["one"] = 1;
    m2["two"] = "two";
    m1["two"] = "two";
    m1["three"] = 3.0;
    m2["three"] = 3.0;

    m3["last"] = "last";
    m3["a"] = "a";
    m4["a"] = "a";
    m4["last"] = "last";

    m5["one"] = 1.0;
    m6["one"] = 1.0;

    'equals = 'equals && m1 == m2 && !(m1 != m2) && m3 == m4 && !(m3 != m4);
    test:assertTrue('equals);
}

function checkMapEqualityNegative() {
    map<anydata> m1 = {};
    map<anydata> m2 = {};

    m1["one"] = "hi";
    m2["one"] = "hello";

    map<int> m3 = {};
    map<int> m4 = {};

    m3["one"] = 1;
    m4["two"] = 2;

    test:assertFalse(m1 == m2 || !(m1 != m2) || m3 == m4 || !(m3 != m4));
}

function checkComplexMapEqualityPositive() {
    map<map<[boolean, string|float]>> m1 = {};
    map<map<[boolean, float]|int>> m2 = {};

    boolean 'equals = m1 == m2 && !(m1 != m2);

    map<[boolean, string|float]> m3 = {one: [true, 3.8], two: [false, 13.8]};
    m1["one"] = m3;

    map<[boolean, float]|int> m4 = {};
    m4["one"] = [true, 3.8];
    m4["two"] = [false, 13.8];
    m2["one"] = m4;

    test:assertTrue('equals && m1 == m2 && !(m1 != m2));
}

function checkComplexMapEqualityNegative() {
    map<map<[boolean, string|float]>> m1 = {};
    map<map<[boolean, float]|int>> m2 = {};

    map<[boolean, string|float]> m3 = {one: [true, "3.8"], two: [false, 13.8]};
    m1["x"] = m3;

    map<[boolean, float]|int> m4 = {one: [true, 3.8], two: [false, 13.8]};
    m2["x"] = m4;

    test:assertFalse(m1 == m2 || !(m1 != m2));
}

type Array ["array", 1];
type Mapping ["mapping", 2];

function checkTupleEqualityPositive() {
    [string, int] t1 = ["", 0];
    [string, int] t2 = ["", 0];

    [string, int, OpenEmployee] t3 = ["hi", 0, {name: "Em"}];
    [string, int, OpenEmployee] t4 = ["hi", 0, {name: "Em"}];

    test:assertTrue(t1 == t2 && !(t1 != t2) && t3 == t4 && !(t3 != t4));

    Array a = ["array", 1];
    Array b = ["array", 1];
    test:assertTrue(a == b);
}

function checkTupleEqualityNegative() {
    [boolean, int] t1 = [false, 0];
    [boolean, int] t2 = [true, 0];

    [float, int, float] t3 = [1.0, 0, 1.1];
    [float, int, float] t4 = [1.1, 0, 1.0];

    [string, ClosedEmployee] t5 = ["hi", {name: "EmZee"}];
    [string, ClosedEmployee] t6 = ["hi", {name: "Em"}];

    test:assertFalse(t1 == t2 || !(t1 != t2) || t3 == t4 || !(t3 != t4) || t5 == t6 || !(t5 != t6));

    Array a = ["array", 1];
    Mapping b = ["mapping", 2];
    test:assertFalse(a == b);
}

function checkUnionConstrainedMapsPositive() {
    map<string|int> m1 = {};
    map<int> m2 = {};

    boolean 'equals = m1 == m2 && !(m1 != m2);

    m1["one"] = 1;
    m1["two"] = 2;

    m2["one"] = 1;
    m2["two"] = 2;

    'equals = 'equals && m1 == m2 && !(m1 != m2);

    map<string|int> m3 = {};
    map<int|float|string> m4 = {};

    'equals = 'equals && m3 == m4 && !(m3 != m4);

    m3["one"] = "one";
    m3["two"] = 2;

    m4["two"] = 2;
    m4["one"] = "one";

    'equals = 'equals && m3 == m4 && !(m3 != m4);

    map<[string|int, float]>|[string, int] m5;
    map<[string, float]> m6 = {};

    map<[string|int, float]> m7 = {
        one: ["hi", 100.0],
        two: ["hello", 21.1]
    };
    m5 = m7;
    m6["two"] = ["hello", 21.1];
    m6["one"] = ["hi", 100.0];

    test:assertTrue('equals && m5 == m6 && !(m5 != m6));
}

function checkUnionConstrainedMapsNegative() {
    map<int|boolean> m1 = {};
    map<int> m2 = {};

    m1["one"] = true;
    m1["two"] = 2;

    boolean 'equals = m1 == m2 || !(m1 != m2);

    m2["two"] = 2;

    'equals = 'equals || m1 == m2 || !(m1 != m2);

    map<[string|int, float]>|[string, int] m3;
    map<[string, float]> m4 = {};

    map<[string|int, float]> m5 = {
        one: ["hi", 100],
        two: ["hello", 21]
    };
    m3 = m5;
    m4["two"] = ["hello", 21.1];
    m4["one"] = ["hi", 100.0];

    test:assertFalse('equals || m3 == m4 || !(m3 != m4));
}

function checkUnionArrayPositive() {
    (string|int)?[] a1 = [];
    int[] a2 = [];

    boolean 'equals = a1 == a2 && !(a1 != a2);

    a1 = [1, 2000, 937];
    a2 = [1, 2000, 937];

    'equals = 'equals && a1 == a2 && !(a1 != a2);

    (string|int)?[] a3 = [];
    (int|float|string)?[] a4 = [];

    'equals = 'equals && a3 == a4 && !(a3 != a4);

    a3[0] = "one";
    a3[3] = 3;

    a4[3] = 3;
    a4[0] = "one";

    'equals = 'equals && a3 == a4 && !(a3 != a4);

    ([string, int]|float)?[] a5 = [];
    ([string, int|boolean]|float)?[] a6 = [];

    'equals = 'equals && a5 == a6 && !(a5 != a6);

    a5 = [["hi", 1], 3.0];
    a6[0] = ["hi", 1];
    a6[1] = 3.0;

    test:assertTrue('equals && a5 == a6 && !(a5 != a6));
}

function checkUnionArrayNegative() {
    (string|int)?[] a1 = [];
    int[] a2 = [];

    a1[0] = "true";
    a1[2] = 2;

    boolean 'equals = a1 == a2 || !(a1 != a2);

    a2[2] = 2;

    'equals = 'equals || a1 == a2 || !(a1 != a2);

    ([string, int]|float)?[] a5 = [["hi", 1], 3.0];
    ([string, int|boolean]|float)?[] a6 = [["hi", true], 3.0];

    return test:assertFalse('equals || a5 == a6 || !(a5 != a6));
}

function checkTupleWithUnionPositive() {
    [int, string|int, boolean] t1 = [1, "ballerina", false];
    [int, string|int, boolean] t2 = [1, "ballerina", false];

    boolean 'equals = t1 == t2 && !(t1 != t2);

    [int, string|int, boolean] t3 = [1000, "ballerina", true];
    [int, int|string, OpenEmployee|boolean|int] t4 = [1000, "ballerina", true];

    test:assertTrue('equals && t3 == t4 && !(t3 != t4));
}

function checkTupleWithUnionNegative() {
    [int, string|int, boolean] t1 = [1, "hi", false];
    [int, string|int, boolean] t2 = [1, "ballerina", false];

    boolean 'equals = t1 == t2 || !(t1 != t2);

    [int, string|int, boolean] t3 = [1000, "ballerina", true];
    [int, int|string, OpenEmployee|boolean|string] t4 = [1000, "ballerina", "true"];

    test:assertFalse('equals || t3 == t4 || !(t3 != t4));
}

function checkJsonEquality() {
    int a = 1000;
    float b = 12.34;
    string c = "Hello Ballerina";
    boolean d = true;
    boolean e = false;
    anydata f = ();

    test:assertTrue((a == a) && !(a != a));
    test:assertTrue((b == b) && !(b != b));
    test:assertTrue((c == c) && !(c != c));
    test:assertTrue((d == d) && !(d != d));
    test:assertTrue((e == e) && !(e != e));
    test:assertTrue((f == f) && !(f != f));

    int a1 = 50;
    float b1 = 12224.1;
    string c1 = "Hi Ballerina";

    test:assertFalse((a == a1) || !(a != a1));
    test:assertFalse((b == b1) || !(b != b1));
    test:assertFalse((c == c1) || !(c != c1));
    test:assertFalse((d == e) || !(d != e));
}

function checkJsonEqualityPositive(json a, json b) {
    test:assertTrue((a == b) && !(a != b));
}

function checkJsonEqualityNegative(json a, json b) {
    test:assertFalse((a == b) || !(a != b));
}

function testIntByteEqualityPositive() {
    int a = 0;
    byte b = 0;

    boolean 'equals = a == b && !(a != b);

    a = 5;
    b = 5;

    'equals = 'equals && (a == b) && !(a != b);

    int[] c = [];
    byte[] d = [];

    'equals = 'equals && (c == d) && !(c != d);

    c = [1, 2];
    d = [1, 2];

    'equals = 'equals && (c == d) && !(c != d);

    byte[][] e = [[23, 45], [123, 43, 68]];
    (int|float)?[][] f = [[23, 45], [123, 43, 68]];

    'equals = 'equals && (e == f) && !(e != f);

    map<[byte, boolean]> g = {};
    map<[int, boolean]> h = {};

    'equals = 'equals && (g == h) && !(g != h);

    g["one"] = [100, true];
    h["two"] = [1, false];
    h["one"] = [100, true];
    g["two"] = [1, false];

    test:assertTrue('equals && (g == h) && !(g != h));
}

function testIntByteEqualityNegative() {
    int a = 15;
    byte b = 5;

    boolean 'equals = a == b || !(a != b);

    a = 256;
    b = 0;

    'equals = 'equals || a == b || !(a != b);

    int[] c = [2, 1];
    byte[] d = [1, 2];

    'equals = 'equals || (c == d) || !(c != d);

    (int|float)?[][] e = [[2.3, 45], [124, 43, 68]];
    byte[][] f = [[23, 45], [123, 43, 68]];

    'equals = 'equals || (e == f) || !(e != f);

    map<[int, boolean]> g = {};
    map<[byte, boolean]> h = {};

    g["one"] = [10, true];
    h["two"] = [1, false];
    h["one"] = [100, true];
    g["two"] = [1, false];

    test:assertFalse('equals && (g == h) && !(g != h));
}

function testPrimitiveAndJsonEqualityPositive() {
    json a = ();
    () b = ();

    boolean 'equals = a == b && !(a != b);

    a = 1;
    int c = 1;

    'equals = 'equals && (a == c) && !(a != c);

    a = "Hello World, from Ballerina";
    string d = "Hello World, from Ballerina";

    'equals = 'equals && (a == d) && !(a != d);

    a = false;
    boolean|int e = false;

    'equals = 'equals && (a == e) && !(a != e);

    json[] f = [1.5, 4.23, 2.1];
    (map<anydata>|float)?[] g = [1.5, 4.23, 2.1];

    'equals = 'equals && (f == g) && !(f != g);

    OpenEmployee? h = ();
    a = ();

    test:assertTrue('equals && (a == h) && !(a != h));
}

function testPrimitiveAndJsonEqualityNegative() {
    json a = ();
    int? b = 5;

    boolean 'equals = a == b || !(a != b);

    a = 10;
    int c = 1;

    'equals = 'equals || (a == c) || !(a != c);

    a = "Hello from Ballerina";
    string d = "Hello World, from Ballerina";

    'equals = 'equals || (a == d) || !(a != d);

    a = false;
    boolean|int e = true;

    'equals = 'equals || (a == e) || !(a != e);

    json[] f = [1.5, 4.23, 2.1];
    (map<anydata>|int|float)?[] g = [1, 4];

    'equals = 'equals || (f == g) || !(f != g);

    OpenEmployee? h = ();
    a = 10;

    test:assertFalse('equals || (a == h) || !(a != h));
}

function testSimpleXmlPositive() {
    xml x1 = xml `<book>The Lost World</book>`;
    xml x2 = xml `<book>The Lost World</book>`;
    xml x3 = xml `Hello, world!`;
    xml x4 = xml `Hello, world!`;
    xml x5 = xml `<?target data?>`;
    xml x6 = xml `<?target data?>`;
    xml x7 = xml `<!-- I'm a comment -->`;
    xml x8 = xml `<!-- I'm a comment -->`;
    xml x11 = xml `<book>The Lost World</book><!-- I'm a comment -->`;
    boolean x9 = x11 == xml `<book>The Lost World</book><!-- I'm a comment -->`;
    boolean x10 = x1 == xml `<book>The Lost World</book>`;
    boolean x12 = x3 == xml `Hello, world!`;
    boolean x13 = x5 == xml `<?target data?>`;
    boolean x14 = x7 == xml `<!-- I'm a comment -->`;
    test:assertTrue(x1 == x2 && !(x1 != x2) && x3 == x4 && !(x3 != x4) && x5 == x6 && !(x5 != x6) && x7 == x8 &&
    !(x7 != x8) && x9 && x10 && x12 && x13 && x14 && x11 != x8 && x3 != x5 && x7 != x1 && x6 != x7);
}

function testReferenceEqualityXml() {
    test:assertTrue(xml `abc` === xml `abc`);
    test:assertFalse(xml `<book>The Lost World</book>` === xml `<book>The Lost World</book>`);
    test:assertFalse(xml `<!--comment-->` === xml `<!--comment-->`);
    test:assertFalse(xml `<?target data?>` === xml `<?target data?>`);
    //test:assertTrue(xml`<?target data?><!--comment--><root>test</root>` === xml`<?target data?><!--comment-->
    //<root>test</root>`);
    test:assertFalse(xml `<?target data?><!--comment--><root>test</root>` === xml `<?target data?><!--comment-->test`);

    test:assertFalse(xml `abc` !== xml `abc`);
    test:assertTrue(xml `<book>The Lost World</book>` !== xml `<book>The Lost World</book>`);
    test:assertTrue(xml `<!--comment-->` !== xml `<!--comment-->`);
    test:assertTrue(xml `<?target data?>` !== xml `<?target data?>`);
    //test:assertFalse(xml`<?target data?><!--comment--><root>test</root>` !== xml`<?target data?><!--comment-->
    //<root>test</root>`);
    test:assertTrue(xml `<?target data?><!--comment--><root>test</root>` !== xml `<?target data?><!--comment-->test`);
}

function testXmlNeverAndXmlSequenceEquality() {
    xml a = xml ``;
    xml<xml:Element> h = xml ``;
    xml<xml:Comment> i = xml ``;
    xml<xml:ProcessingInstruction> j = xml ``;
    xml<xml:Text> k = xml ``;
    xml:Text l = xml ``;
    xml<never> b = xml ``;
    xml c = 'xml:concat();
    xml<never> d = <xml<never>>c;
    xml<xml:Element> f = xml `<ele></ele>`;
    xml e = f/*;

    xml m = xml `<authors><name>Enid Blyton</name></authors>`;
    xml n = m.filter(function(xml x2) returns boolean {
        xml elements = (x2/*).elements();
        return (elements/*).toString() == "Anne North";
    });

    test:assertTrue(a == b);
    test:assertFalse(a != c);
    test:assertTrue(b == c);
    test:assertTrue(c == d);
    test:assertTrue(b == d);
    test:assertFalse(d != b);
    test:assertTrue(a == h);
    test:assertFalse(h != a);
    test:assertTrue(a == i);
    test:assertTrue(a == j);
    test:assertTrue(a == k);
    test:assertTrue(a == l);
    test:assertTrue(b == h);
    test:assertFalse(h != b);
    test:assertTrue(b == i);
    test:assertTrue(b == j);
    test:assertTrue(b == k);
    test:assertTrue(b == l);
    test:assertFalse(e != b);
    test:assertTrue(n == b);
}

function testSimpleXmlNegative() {
    xml x1 = xml `<book>The Lot World</book>`;
    xml x2 = xml `<book>The Lost World</book>`;
    xml x3 = xml `Hello, world!`;
    xml x4 = xml `Hello world!`;
    xml x5 = xml `<?targt data?>`;
    xml x6 = xml `<?target data?>`;
    xml x7 = xml `<!-- I'm comment one -->`;
    xml x8 = xml `<!-- I'm comment two -->`;
    test:assertFalse(x1 == x2 || !(x1 != x2) || x3 == x4 || !(x3 != x4) || x5 == x6 || !(x5 != x6) || x7 == x8
    || !(x7 != x8));
}

public function testEqualNestedXml() {
    xml x1 = xml `<book><name>The Lost World<year>1912</year></name></book>`;
    xml x2 = xml `<book><name>The Lost World<year>1912</year></name></book>`;
    test:assertTrue(x1 == x2 && !(x1 != x2));
}

public function testUnequalNestedXml() {
    xml x1 = xml `<book><name>The Lost World<year>1920</year></name></book>`;
    xml x2 = xml `<book><name>The Lost World<year>1912</year></name></book>`;
    test:assertFalse(x1 == x2 || !(x1 != x2));
}

public function testEqualXmlWithComments() {
    xml x1 = xml `<book><name>The Lost World<!-- I'm a comment --></name></book>`;
    xml x2 = xml `<book><name>The Lost World<!-- I'm a comment --></name></book>`;
    test:assertTrue(x1 == x2 && !(x1 != x2));
}

public function testUnequalXmlWithUnequalComment() {
    xml x1 = xml `<book><name>The Lost World<!-- Don't Ignore me --></name></book>`;
    xml x2 = xml `<book><name>The Lost World<!-- Ignore me --></name></book>`;
    test:assertFalse(x1 == x2 || !(x1 != x2));
}

public function testEqualXmlIgnoringAttributeOrder() {
    xml x1 = xml `<book><name category="fiction" year="1912">The Lost World<author>Doyle</author></name></book>`;
    xml x2 = xml `<book><name year="1912" category="fiction">The Lost World<author>Doyle</author></name></book>`;
    test:assertTrue(x1 == x2 && !(x1 != x2));
}

public function testUnequalXmlIgnoringAttributeOrder() {
    xml x1 = xml `<book><name category="fantasy" year="1912">The Lost World<author>Doyle</author></name></book>`;
    xml x2 = xml `<book><name year="1912" category="fiction">The Lost World<author>Doyle</author></name></book>`;
    test:assertFalse(x1 == x2 || !(x1 != x2));
}

public function testEqualXmlWithPI() {
    xml x1 = xml `<book><?target data?><name>The Lost World</name><?target_two data_two?></book>`;
    xml x2 = xml `<book><?target data?><name>The Lost World</name><?target_two data_two?></book>`;
    test:assertTrue(x1 == x2 && !(x1 != x2));
}

public function testUnequalXmlWithUnequalPI() {
    xml x1 = xml `<book><?target data?><name>The Lost World</name></book>`;
    xml x2 = xml `<book><?target dat?><name>The Lost World</name></book>`;
    test:assertFalse(x1 == x2 || !(x1 != x2));
}

public function testUnequalXmlWithPIInWrongOrder() {
    xml x1 = xml `<book><?target data?><name>The Lost World</name></book>`;
    xml x2 = xml `<book><name>The Lost World</name><?target data?></book>`;
    test:assertFalse(x1 == x2 || !(x1 != x2));
}

public function testUnequalXmlWithMultiplePIInWrongOrder() {
    xml x1 = xml `<book><?target data?><?target_two data_two?><name>The Lost World</name></book>`;
    xml x2 = xml `<book><?target_two data_two?><?target data?><name>The Lost World</name></book>`;
    test:assertFalse(x1 == x2 || !(x1 != x2));
}

public function testUnequalXmlWithMissingPI() {
    xml x1 = xml `<book><?target data?><name>The Lost World</name></book>`;
    xml x2 = xml `<book><name>The Lost World</name></book>`;
    test:assertFalse(x1 == x2 || !(x1 != x2));
}

public function testXmlWithNamespacesPositive() {
    xml x1 = xml `<book xmlns="http://wso2.com"><name>The Lost World<year>1912</year></name></book>`;
    xml x2 = xml `<book xmlns="http://wso2.com"><name>The Lost World<year>1912</year></name></book>`;

    xmlns "http://wso2.com";
    xml x3 = xml `<book><name>The Lost World<year>1912</year></name></book>`;

    test:assertTrue(x1 == x2 && !(x1 != x2) && x2 == x3 && !(x2 != x3));
}

public function testXmlWithNamespacesNegative() {
    xml x1 = xml `<book xmlns="http://wso2.com"><name>The Lost World<year>1912</year></name></book>`;
    xml x2 = xml `<book xmlns="http://wsotwo.com"><name>The Lost World<year>1912</year></name></book>`;
    xml x3 = xml `<book><name>The Lost World<year>1912</year></name></book>`;

    test:assertFalse(x1 == x2 || !(x1 != x2) || x2 == x3 && !(x2 != x3));
}

public function testXmlSequenceAndXmlItemEqualityPositive() {
    xml x1 = xml `<name>Book One</name>`;
    xml x2 = x1.get(0);
    test:assertTrue(x1 == x2 && !(x1 != x2) && x2 == x1 && !(x2 != x1));
}

public function testXmlSequenceAndXmlItemEqualityNegative() {
    xml x1 = xml `<name>Book One</name>`;
    xml x2 = xml `<name>Book Two</name>`;
    xml x3 = x2.get(0);
    test:assertFalse(x1 == x3 || !(x1 != x3) || x3 == x1 || !(x3 != x1));
}

public function testXmlSequenceLHSEquals() {
    string a = "hello";
    xml x1 = xml `AS-${a}`;
    xml x2 = xml `AS-${a}`;
    test:assertTrue(x1 == x2 && !(x1 != x2));

    x1 = xml `<?target data?><?target_two data_two?>`;
    x2 = xml `<?target data?><?target_two data_two?>`;
    test:assertTrue(x1 == x2 && !(x1 != x2));
}

function testXmlStringNegative() {
    anydata x1 = xml `<book>The Lost World</book>`;
    anydata x2 = "<book>The Lost World</book>";
    anydata x3 = xml `Hello, world!`;
    anydata x4 = "Hello, world!";
    anydata x5 = xml `<?target data?>`;
    anydata x6 = "<?target data?>";
    anydata x7 = xml `<!-- I'm comment -->`;
    anydata x8 = "<!-- I'm comment -->";
    boolean result = x1 == x2 || !(x1 != x2) || x3 == x4 || !(x3 != x4) || x5 == x6 || !(x5 != x6) || x7 == x8 || !(x7
    != x8) || x2 == x1 || !(x2 != x1) || x4 == x3 || !(x4 != x3) || x6 == x5 || !(x6 != x5) || x8 == x7 || !(x8 != x7);
    test:assertFalse(result);
}

public function testJsonRecordMapEqualityPositive() {
    OpenEmployeeTwo e = {name: "Maryam", "id": 1000};

    json j = {name: "Maryam", id: 1000};
    json j2 = j;

    map<string|int> m = {name: "Maryam", id: 1000};
    map<anydata> m2 = m;

    test:assertTrue(e == m && !(m != e) && e == m2 && !(m2 != e) && e == j && !(j != e) && e == j2 && !(j2 != e));
}

public function testJsonRecordMapEqualityNegative() {
    OpenEmployeeTwo e = {name: "Zee", "id": 1000};

    json j = {name: "Maryam", id: 122};
    json j2 = j;

    map<string|int> m = {name: "Maryam"};
    map<anydata> m2 = m;

    test:assertFalse(e == m || !(m != e) || e == m2 || !(m2 != e) || e == j || !(j != e) || e == j2 || !(j2 != e));
}

public function testArrayTupleEqualityPositive() {
    int[] a = [1, 2, 3];
    [int, json, int] b = [1, 2, 3];

    boolean 'equals = a == b && !(b != a);

    int[3] c = [1, 2, 3];
    'equals = 'equals && isEqual(b, c);

    OpenEmployeeTwo e = {name: "Maryam", "id": 1000};
    (OpenEmployeeTwo|json)?[] f = [1, e, true, 3.2];
    [int, OpenEmployeeTwo, boolean, float] g = [1, e, true, 3.2];

    test:assertTrue('equals && g == f && !(f != g));
}

public function testArrayTupleEqualityNegative() {
    (boolean|float)?[] a = [true, 2.0, 1.23];
    [json, json, float] b = [false, 2.0, 1.23];

    boolean 'equals = a == b && !(b != a);

    (string|boolean|float|int)?[3] c = [false, 2, 1.23];
    'equals = 'equals && c == b && !(b != c);

    OpenEmployeeTwo e = {name: "Maryam", "id": 1000};
    OpenEmployeeTwo e2 = {name: "Ziyad", "id": 1000};
    (OpenEmployeeTwo|json)?[] f = [1, e, true, 3.2];
    [int|OpenEmployee, OpenEmployeeTwo, boolean|string, float] g = [1, e2, true, 3.2];

    test:assertFalse('equals || isEqual(g, f));
}

public function testSelfAndCyclicReferencingMapEqualityPositive() {
    map<anydata> m = {"3": "three", "0": 0};
    m["1"] = m;

    map<anydata> n = {"0": 0, "3": "three"};
    n["1"] = n;

    boolean 'equals = isEqual(m, n);

    map<anydata> o = {"0": 0};
    o["1"] = m;

    map<anydata> p = {"0": 0};
    p["1"] = n;

    'equals = 'equals && o == p && !(o != p);

    map<anydata> q = {one: 1};
    map<anydata> r = {one: 1};
    q["r"] = r;
    r["q"] = q;
    r["r"] = r;
    q["q"] = q;

    test:assertTrue('equals && isEqual(q, r));
}

public function testSelfAndCyclicReferencingMapEqualityNegative() {
    map<anydata> m = {"3": "three", "0": 0};
    m["1"] = m;

    map<anydata> n = {"0": 0, "3": "three"};
    n["2"] = n;

    boolean 'equals = m == n || !(n != m);

    n["1"] = m;
    map<anydata> o = {"0": 0};
    o["1"] = m;

    map<anydata> p = {"0": "zero"};
    p["1"] = n;

    'equals = 'equals || o == p || !(o != p);

    map<anydata> q = {one: 1};
    map<anydata> r = {one: 1};
    map<anydata> s = {one: 2};
    q["r"] = r;
    r["q"] = q;
    r["r"] = r;
    q["q"] = s;

    test:assertFalse('equals || isEqual(q, r));
}

public function testSelfAndCyclicReferencingJsonEqualityPositive() {
    map<json> m = {"3": "three", "0": 0};
    m["1"] = m;

    map<json> n = {"0": 0, "3": "three"};
    n["1"] = n;

    boolean 'equals = isEqual(m, n);

    map<json> o = {"0": 0};
    o["1"] = m;

    map<json> p = {"0": 0};
    p["1"] = n;

    'equals = 'equals && o == p && !(o != p);

    map<json> q = {one: 1};
    map<json> r = {one: 1};
    q["r"] = r;
    r["q"] = q;
    r["r"] = r;
    q["q"] = q;

    test:assertTrue('equals && isEqual(q, r));
}

public function testSelfAndCyclicReferencingJsonEqualityNegative() {
    map<json> m = {"3": "three", "0": 0};
    m["1"] = m;

    map<json> n = {"0": 0, "3": "three"};
    n["2"] = n;

    boolean 'equals = m == n || !(n != m);

    n["1"] = m;
    map<json> o = {"0": 0};
    o["1"] = m;

    map<json> p = {"0": "zero"};
    p["1"] = n;

    'equals = 'equals || o == p || !(o != p);

    map<json> q = {one: 1, t: 2};
    map<json> r = {one: 1, t: 23};
    q["r"] = r;
    r["q"] = q;
    r["r"] = r;
    q["q"] = q;

    test:assertFalse('equals || isEqual(q, r));
}

public function testSelfAndCyclicReferencingArrayEqualityPositive() {
    anydata[] m = [1, "hi"];
    m[2] = m;

    anydata[] n = [1, "hi"];
    n[2] = n;

    boolean 'equals = isEqual(m, n);

    anydata[] o = [1, 3, 4.5, m, "hello"];
    anydata[] p = [1, 3, 4.5, n, "hello"];

    'equals = 'equals && o == p && !(o != p);

    o[5] = m;
    p[5] = m;

    test:assertTrue('equals && o == p && !(o != p));
}

public function testSelfAndCyclicReferencingArrayEqualityNegative() {
    anydata[] m = [1, "hi", 3, "ballerina"];
    m[2] = m;

    anydata[] n = [1, "hi", 3, "ball"];
    n[2] = n;

    boolean 'equals = m == n || !(n != m);

    anydata[] o = [1, 3, 4.5, m];
    anydata[] p = [1, 3, 4.5, n];

    'equals = 'equals || o == p || !(p != o);

    o[4] = m;
    p[4] = n;

    test:assertFalse('equals || o == p || !(o != p));
}

public function testSelfAndCyclicReferencingTupleEqualityPositive() {
    [int, string, anydata] m = [1, "hi", false];
    m[2] = m;

    [int, string, anydata] n = [1, "hi", true];
    n[2] = n;

    boolean 'equals = isEqual(m, n);

    [int, int, boolean, [int, string, anydata], anydata] o = [1, 3, true, m, "hello"];
    [int, int, boolean, [int, string, anydata], anydata] p = [1, 3, true, n, "hello"];

    'equals = 'equals && o == p && !(o != p);

    o[4] = m;
    p[4] = m;

    test:assertTrue('equals && o == p && !(o != p));
}

public function testSelfAndCyclicReferencingTupleEqualityNegative() {
    [int, string, anydata, string] m = [1, "hi", 3, "ballerina"];
    m[2] = m;

    [int, string, anydata, string] n = [1, "hi", 3, "ball"];
    n[2] = n;

    boolean 'equals = m == n || !(n != m);

    [int, int, boolean, anydata] o = [1, 3, true, m];
    [int, int, boolean, anydata] p = [1, 3, true, n];

    'equals = 'equals || o == p || !(p != o);

    o[3] = m;
    p[3] = n;

    test:assertFalse('equals || o == p || !(o != p));
}

function testEmptyMapAndRecordEquality() {
    map<string> m = {};
    record {|string s?;int...; |} r = {};
    test:assertTrue(m == r);
}

type Student record {
    readonly string name;
    int id;
};

type Teacher record {
    string name;
    int id;
};

type StudentTable table<Student> key(name);

type TeacherTable table<Teacher>;

function testTableEquality() {
    table<Student> key(name) tbl1 = table [
            {name: "Amy", id: 1234},
            {name: "John", id: 4567}
        ];
    table<Student> key(name) tbl2 = table [
            {name: "Amy", id: 1234},
            {name: "John", id: 4567}
        ];
    var tbl3 = table key(name) [
            {name: "Amy", id: 1234},
            {name: "John", id: 4567}
        ];
    var tbl4 = tbl2;
    anydata tbl5 = table key(name) [
        {name: "Amy", id: 1234},
        {name: "John", id: 4567}
    ];
    var ids = checkpanic table key(id) from var {id} in tbl1 select {id};
    table<record {| readonly int id; |}> key(id) tbl6 = ids;
    StudentTable tbl7 = table key(name) [
            {name: "Amy", id: 1234},
            {name: "John", id: 4567}
        ];
    StudentTable|int tbl8 = tbl7;
    map<anydata> a1 = {
        "a": table key(id) [
                {"id": 1234},
                {"id": 4567}
            ],
        "b": 12
    };
    record {|int id; anydata tbl9;|} a2 = {
        id: 1,
        tbl9: table key(id) [
                {"id": 1234},
                {"id": 4567}
            ]
    };
    anydata[] a3 = [
        12,
        "A",
        table key(id) [
                {"id": 1234},
                {"id": 4567}
            ]
    ];
    [StudentTable, int, string] a4 = [
        table key(name) [
                {name: "Amy", id: 1234},
                {name: "John", id: 4567}
            ],
        12,
        "A"
    ];
    TeacherTable tbl10 = table [
            {name: "Amy", id: 1234},
            {name: "John", id: 4567}
        ];
    var tbl11 = table [
            {name: "Amy", id: 1234},
            {name: "John", id: 4567}
        ];
    table<record {|string name; int id;|}> tbl12 = table [
            {name: "Amy", id: 1234},
            {name: "John", id: 4567}
        ];
    var tbl13 = table [
            {name: "Amber", id: 1234},
            {name: "John", id: 4567}
        ];
    TeacherTable tbl14 = table [
            {name: "John", id: 4567},
            {name: "Amber", id: 1234}
        ];

    test:assertTrue(tbl1 == tbl2);
    test:assertTrue(tbl1 == tbl3);
    test:assertTrue(tbl3 == tbl4);
    test:assertTrue(tbl2 == tbl5);
    test:assertTrue(tbl3 == tbl5);
    test:assertTrue(table key(id) [
            {"id": 1234},
            {"id": 4567}
        ] == tbl6);
    test:assertTrue(tbl1 == tbl7);
    test:assertTrue(tbl3 == tbl7);
    test:assertTrue(tbl5 == tbl7);
    test:assertTrue(tbl1 == tbl8);
    test:assertTrue(a1["a"] == tbl6);
    test:assertTrue(a1["a"] == a2.tbl9);
    test:assertTrue(a2.tbl9 == a3[2]);
    test:assertTrue(tbl1 == a4[0]);
    test:assertTrue(tbl10 == tbl11);
    test:assertTrue(tbl10 == tbl12);
    test:assertTrue(tbl3 != tbl11);
    test:assertTrue(tbl5 != tbl6);
    test:assertTrue(tbl2 != tbl10);
    test:assertTrue(a1["a"] != tbl12);
    test:assertTrue(tbl11 != tbl13);
    test:assertTrue(tbl10 != tbl14);
    test:assertTrue(table key(id) [
            {"id": 4567},
            {"id": 1234}
        ] != tbl6);
}

function isEqual(anydata a, anydata b) returns boolean {
    return a == b && !(b != a);
}

function testTupleJSONEquality() {
    [string, int] t = ["Hi", 1];
    json j = "Hi 1";
    boolean bool1 = t == j && t != j;
    test:assertFalse(bool1);

    [string, int][] e = [["Hi", 1]];
    bool1 = e == j && e != j;
    test:assertFalse(bool1);

    [string, int...] k = ["Hi", 1];
    bool1 = k == j && k != j;
    test:assertFalse(bool1);

    j = ["Hi", 1];
    test:assertTrue(j == t);
    test:assertTrue(j == k);
    j = [["Hi", 1]];
    test:assertTrue(j == e);
    j = true;
    [boolean, string|()] l = [true];
    test:assertTrue(j != l);
}

function testIntersectingUnionEquality() {
    string:Char? a = "a";
    string b = "a";
    test:assertTrue(a == b);
    test:assertFalse(a != b);
    test:assertTrue(b == a);
    test:assertFalse(b != a);
    test:assertTrue(a == "a");
    test:assertFalse(a != "a");
    test:assertTrue("a" == a);
    test:assertFalse("a" != a);

    string c = "x";
    test:assertFalse(a == c);
    test:assertTrue(a != c);
    test:assertFalse(c == a);
    test:assertTrue(c != a);
    test:assertFalse(a == "x");
    test:assertTrue(a != "x");
    test:assertFalse("abc" == a);
    test:assertTrue("abc" != a);
}

class MyObj2 {
}

type MyObject1 object {
};

function testEqualityWithNonAnydataType() {
    map<int|string> a = {one: 1, two: "two"};
    map<any> b = {one: 1, two: "two"};
    test:assertTrue(a == b);
    test:assertFalse(a != b);

    any c = 5;
    int d = 5;
    test:assertTrue(c == d);
    test:assertFalse(c != d);

    MyObject1? obj1 = ();
    test:assertTrue(obj1 == ());
    test:assertFalse(obj1 != ());

    MyObj2? obj2 = new;
    test:assertFalse(obj2 == ());
    test:assertTrue(obj2 != ());
}

type FloatOrInt float|int;

FloatOrInt n1 = 0.0;
FloatOrInt n2 = -0.0;
FloatOrInt n3 = 0.0 / 0.0;
FloatOrInt n4 = -0.0 / 0.0;
FloatOrInt n5 = 2.0;
FloatOrInt n6 = 2.00;
FloatOrInt n7 = 2;

function testEqualityWithFloatUnion() {
    test:assertTrue(n1 == n2);
    test:assertTrue(n3 == n4);
    test:assertTrue(n5 == n6);
    test:assertFalse(n5 == n7);
}

function testNotEqualityWithFloatUnion() {
    test:assertFalse(n1 != n2);
    test:assertFalse(n3 != n4);
    test:assertFalse(n5 != n6);
    test:assertTrue(n5 != n7);
}

function testExactEqualityWithFloatUnion() {
    test:assertFalse(n1 === n2);
    test:assertTrue(n3 === n4);
    test:assertTrue(n5 === n6);
    test:assertFalse(n5 === n7);
}

function testNotExactEqualityWithFloatUnion() {
    test:assertTrue(n1 !== n2);
    test:assertFalse(n3 !== n4);
    test:assertFalse(n5 !== n6);
    test:assertTrue(n5 !== n7);
}

type DecimalOrInt decimal|int;

DecimalOrInt m1 = 0.0;
DecimalOrInt m2 = -0.0;
DecimalOrInt m3 = 2.0;
DecimalOrInt m4 = 2.00;
DecimalOrInt m5 = 2;

function testEqualityWithDecimalUnion() {
    test:assertTrue(m1 == m2);
    test:assertTrue(m3 == m4);
    test:assertFalse(m3 == m5);
}

function testNotEqualityWithDecimalUnion() {
    test:assertFalse(m1 != m2);
    test:assertFalse(m3 != m4);
    test:assertTrue(m3 != m5);
}

function testExactEqualityWithDecimalUnion() {
    test:assertTrue(m1 === m2);
    test:assertFalse(m3 === m4);
    test:assertFalse(m3 === m5);
}

function testNotExactEqualityWithDecimalUnion() {
    test:assertFalse(m1 !== m2);
    test:assertTrue(m3 !== m4);
    test:assertTrue(m3 !== m5);
}

type VALUE_TYPE int|byte|float|boolean|string;

function testEqualityWithUnionOfSimpleTypes() {
    VALUE_TYPE a = "abc";
    VALUE_TYPE b = "abc";
    VALUE_TYPE c = "bcd";
    VALUE_TYPE d = true;
    VALUE_TYPE e = true;
    VALUE_TYPE f = false;

    VALUE_TYPE g = <byte>1;
    VALUE_TYPE h = <byte>1;
    VALUE_TYPE i = <byte>2;

    VALUE_TYPE j = 2;
    VALUE_TYPE k = 2;
    VALUE_TYPE l = 1;
    VALUE_TYPE m = 2.0;

    test:assertTrue(a == b);
    test:assertFalse(b == c);
    test:assertTrue(a === b);
    test:assertFalse(b === c);

    test:assertTrue(d == e);
    test:assertFalse(e == f);
    test:assertTrue(d === e);
    test:assertFalse(e === f);

    test:assertTrue(g == h);
    test:assertFalse(h == i);
    test:assertTrue(g === h);
    test:assertFalse(h === i);

    test:assertTrue(j == k);
    test:assertFalse(j == l);
    test:assertTrue(j === k);
    test:assertFalse(j === l);

    test:assertTrue(i == j);
    test:assertFalse(i == m);
    test:assertTrue(j === i);
    test:assertFalse(i === m);
}

type T xml|handle|string;

function testExactEqualityWithUnionOfNonSimpleTypes() {
    T h1 = java:fromString("abc");
    T h2 = java:fromString("abc");
    T h3 = java:fromString("bcd");
    T x1 = xml `<book>Book One</book>`;
    T x2 = xml `abc`;

    test:assertTrue(h1 === h2);
    test:assertFalse(h1 === h3);
    test:assertFalse(h1 === x1);
    test:assertFalse(x1 === x2);
    test:assertFalse(x2 === h1);
}

function testEqualityByteWithIntSubTypes() {
    byte a = 120;
    int b = 120;
    int:Unsigned8 c = 120;
    int:Unsigned16 d = 120;
    int:Unsigned32 e = 120;
    int:Signed8 f = 120;
    int:Signed16 g = 120;
    int:Signed32 h = 120;

    // == equals check
    test:assertTrue((a == b) && (a == c) && (a == d) && (a == e) &&
    (a == f) && (a == g) && (a == h));
    test:assertTrue((b == a) && (c == a) && (d == a) && (e == a) &&
    (f == a) && (g == a) && (h == a));

    // != equals check
    test:assertFalse((a != b) || (a != c) || (a != d) || (a != e) ||
    (a != f) || (a != g) || (a != h));
    test:assertFalse((b != a) || (c != a) || (d != a) || (e != a) ||
    (f != a) || (g != a) || (h != a));

    // === equals check
    test:assertTrue((a === b) && (a === c) && (a === d) && (a === e) &&
    (a === g) && (a === h));
    test:assertTrue((b === a) && (c === a) && (d === a) && (e === a) &&
    (g === a) && (h === a));
    // Need to add (a === f) , (f === a) after fixing #32924

    // !== equals check
    test:assertFalse((a !== b) || (a !== c) || (a !== d) || (a !== e) ||
    (a !== g) || (a !== h));
    test:assertFalse((b !== a) || (c !== a) || (d !== a) || (e !== a) ||
    (g !== a) || (h !== a));
    // Need to add (a !== f) , (f !== a) after fixing #32924
}

type Part anydata[];
type J anydata;

function testEqualityWithCyclicReferences() {
    map<anydata> m1 = {one: 1, two: 2};
    map<anydata> m2 = {one: 1, two: 2};
    m1["three"] = m2;
    m2["three"] = m1;
    test:assertTrue(m1 == m2);
    test:assertFalse(m1 != m2);

    map<J> j1 = { loop: () };
    map<J> j2 = { loop: () };
    j1["loop"] = j1;
    j2["loop"] = j1;
    map<J> j3 = { loop: () };
    j3["loop"] = { loop: { loop: { loop: j3 }}};
    test:assertTrue(j1 == j3);

    Part yin = [];
    Part yang = [];
    yin[0] = yang;
    yang[0] = yin;
    test:assertTrue(yin == yang);

    table<map<anydata>> t1 = table [];
    table<map<anydata>> t2 = table [];
    t1.add({loop: t2});
    t2.add({loop: t1});
    test:assertTrue(t1 == t2);

    table<map<anydata>> t3 = table [];
    table<map<anydata>> t4 = table [];
    table<map<anydata>> t5 = table [];
    t3.add({loop: t4});
    t4.add({loop: t5});
    t5.add({loop: t3});
    test:assertTrue(t3 == t4);
    test:assertTrue(t3 == t5);
}
