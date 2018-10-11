// Copyright (c) 2017 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

function checkBooleanEqualityPositive(boolean a, boolean b) returns boolean {
    return (a == b) && !(a != b);
}

function checkBooleanEqualityNegative(boolean a, boolean b) returns boolean {
    return (a == b) || !(a != b);
}

function checkIntEqualityPositive(int a, int b) returns boolean {
    return (a == b) && !(a != b);
}

function checkIntEqualityNegative(int a, int b) returns boolean {
    return (a == b) || !(a != b);
}

function checkFloatEqualityPositive(float a, float b) returns boolean {
    return (a == b) && !(a != b);
}

function checkFloatEqualityNegative(float a, float b) returns boolean {
    return (a == b) || !(a != b);
}

function checkStringEqualityPositive(string a, string b) returns boolean {
    return (a == b) && !(a != b);
}

function checkStringEqualityNegative(string a, string b) returns boolean {
    return (a == b) || !(a != b);
}

function checkEqualityToNilPositive(any a) returns boolean {
    return (a == ()) && !(a != ());
}

function checkEqualityToNilNegative(any a) returns boolean {
    return (a == ()) || !(a != ());
}

function check1DArrayEqualityPositive(boolean[]|int[]|float[]|string[] a, boolean[]|int[]|float[]|string[] b)
             returns boolean {
    return (a == b) && !(a != b);
}

function check1DArrayEqualityNegative(boolean[]|int[]|float[]|string[] a, boolean[]|int[]|float[]|string[] b)
             returns boolean {
    return (a == b) || !(a != b);
}

function check1DClosedArrayEqualityPositive() returns boolean {
    boolean[4] b1;
    boolean[4] b2;

    boolean[3] b3 = [true, false, false];
    boolean[!...] b4 = [true, false, false];

    int[5] i1;
    int[5] i2;

    int[2] i3 = [123, 45678];
    int[2] i4 = [123, 45678];

    float[8] f1;
    float[8] f2;

    float[1] f3 = [12.3];
    float[!...] f4 = [12.3];

    string[15] s1;
    string[15] s2;

    string[3] s3 = ["hello", "from", "ballerina"];
    string[3] s4 = ["hello", "from", "ballerina"];

    any[7] a1;
    any[7] a2;

    json j1 = { hello: "world", lang: "ballerina" };
    json j2 = { hello: "world",lang: "ballerina" };

    map m1 = { key1: "val1", key2: 2, key3: 3.1 };
    map m2 = { key1: "val1", key2: 2, key3: 3.1 };

    any[6] a3 = ["hi", 1, true, 54.3, j1, m1];
    any[6] a4 = ["hi", 1, true, 54.3, j2, m2];

    return (b1 == b2) && !(b1 != b2) && (b3 == b4) && !(b3 != b4) &&
        (i1 == i2) && !(i1 != i2) && (i3 == i4) && !(i3 != i4) &&
        (f1 == f2) && !(f1 != f2) && (f3 == f4) && !(f3 != f4) &&
        (s1 == s2) && !(s1 != s2) && (s3 == s4) && !(s3 != s4) &&
        (a1 == a2) && !(a1 != a2) && (a3 == a4) && !(a3 != a4);
}

function check1DClosedArrayEqualityNegative() returns boolean {
    boolean[3] b1 = [true, false, false];
    boolean[!...] b2 = [true, false, true];

    int[2] i1 = [123, 45678];
    int[2] i2 = [123, 45674];

    float[1] f1 = [12.3];
    float[!...] f2 = [12.2];

    string[3] s1 = ["hello", "from", "ballerina"];
    string[3] s2 = ["hello", "from", "ball"];

    json j1 = { hello: "world", lang: "ball" };
    json j2 = { hello: "world",lang: "ballerina" };

    map m1 = { key1: "val1", key2: 2, key3: 3.1 };
    map m2 = { key1: "val1", key2: 2, key3: 3.1 };

    any[6] a1 = ["hi", 1, true, 54.3, j1, m1];
    any[6] a2 = ["hi", 1, true, 54.3, j2, m2];

    return (b1 == b2) || !(b1 != b2) || (i1 == i2) || !(i1 != i2) || (f1 == f2) || !(f1 != f2) ||
        (s1 == s2) || !(s1 != s2) || (a1 == a2) || !(a1 != a2);
}

function check1DAnyArrayEqualityPositive() returns boolean {
    json j1 = { hello: "world", lang: "ballerina" };
    json j2 = { hello: "world",lang: "ballerina" };

    map m1 = { key1: "val1", key2: 2, key3: 3.1 };
    map m2 = { key1: "val1", key2: 2, key3: 3.1 };

    any[] a = ["hi", 1, j1, 2.3, false, m1];
    any[] b = ["hi", 1, j2, 2.3, false, m2];

    return (a == b) && !(a != b);
}

function check1DAnyArrayEqualityNegative() returns boolean {
    json j1 = { hello: "world", lang: "ballerina" };
    json j2 = { hello: "world",lang: "ballerina" };

    map m1 = { key1: "val1", key2: 2, key3: 3.1 };
    map m2 = { key1: "val1", key2: 2, key3: 3.1 };

    any[] a = ["hi", 1, j1, 2.1, false, m1];
    any[] b = ["hi", 1, j2, 2.3, false, m2];

    return (a == b) || !(a != b);
}

function check2DBooleanArrayEqualityPositive() returns boolean {
    boolean[][] b1 = [[], [true, false, false], [false]];
    boolean[][] b2 = [[], [true, false, false], [false]];

    return b1 == b2 && !(b1 != b2);
}

function check2DBooleanArrayEqualityNegative() returns boolean {
    boolean[][] b1 = [[], [true, false, false], [false]];
    boolean[][] b2 = [[], [false, false, false], [false]];

    boolean[][] b3 = [[], [true, false, false], [false]];
    boolean[][] b4 = [[], [true, false, false]];

    boolean[][] b5 = [[], [true, false, false], [false]];
    boolean[][] b6 = [[], [true, false, false], [false], [true, true]];

    return b1 == b2 || !(b1 != b2) || b3 == b4 || !(b3 != b4) || (b5 == b6) || !(b5 != b6);
}

function check2DIntArrayEqualityPositive() returns boolean {
    int[][] i1 = [[1], [], [100, 1200000, 9475883]];
    int[][] i2 = [[1], [], [100, 1200000, 9475883]];

    return i1 == i2 && !(i1 != i2);
}

function check2DIntArrayEqualityNegative() returns boolean {
    int[][] i1 = [[], [100, 2222, 111102], [294750]];
    int[][] i2 = [[], [100, 2222, 2349586], [294750]];

    int[][] i3 = [[1], [], [100, 1200000, 9475883]];
    int[][] i4 = [[1], []];

    int[][] i5 = [[1], [], [100, 1200000, 9475883]];
    int[][] i6 = [[1], [], [100, 1200000, 9475883], [1111, 4085759, 2305684]];

    return i1 == i2 || !(i1 != i2) || i3 == i4 || !(i3 != i4) || (i5 == i6) || !(i5 != i6);
}

function check2DFloatArrayEqualityPositive() returns boolean {
    float[][] f1 = [[1.221], [], [10.0, 123.45, 9.475883]];
    float[][] f2 = [[1.221], [], [10.0, 123.45, 9.475883]];

    return f1 == f2 && !(f1 != f2);
}

function check2DFloatArrayEqualityNegative() returns boolean {
    float[][] f1 = [[1.221], [], [10.0, 123.45, 9.475883]];
    float[][] f2 = [[1.221], [], [10.0, 1.2, 9.475883]];

    float[][] f3 = [[1.221], [], [10.0, 123.45, 9.475883]];
    float[][] f4 = [[1.221], []];

    float[][] f5 = [[1.221], [], [10.0, 123.45, 9.475883]];
    float[][] f6 = [[1.221], [], [10.0, 123.45, 9.475883], [11.11, 408575.9, 23.05684]];

    return f1 == f2 || !(f1 != f2) || f3 == f4 || !(f3 != f4) || (f5 == f6) || !(f5 != f6);
}

function check2DStringArrayEqualityPositive() returns boolean {
    string[][] s1 = [[], ["hello world", "from", "ballerina"], ["ballet shoes"]];
    string[][] s2 = [[], ["hello world", "from", "ballerina"], ["ballet shoes"]];

    return s1 == s2 && !(s1 != s2);
}

function check2DStringArrayEqualityNegative() returns boolean {
    string[][] s1 = [[], ["hello", "from", "ballerina"], ["ballet shoes"]];
    string[][] s2 = [[], ["hi", "from", "ballerina"], ["ballet shoes"]];

    string[][] s3 = [[], ["hello", "from", "ballerina"], ["ballet shoes"]];
    string[][] s4 = [[], ["hello", "from", "ballerina"]];

    string[][] s5 = [[], ["hello", "from", "ballerina"], ["ballet shoes"]];
    string[][] s6 = [[], ["hello", "from", "ballerina"], ["ballet shoes"], ["recital", "it is time"]];

    return s1 == s2 || !(s1 != s2) || s3 == s4 || !(s3 != s4) || (s5 == s6) || !(s5 != s6);
}

function checkJsonEqualityPositive(json a, json b) returns boolean {
    return (a == b) && !(a != b);
}

function checkJsonEqualityNegative(json a, json b) returns boolean {
    return (a == b) || !(a != b);
}