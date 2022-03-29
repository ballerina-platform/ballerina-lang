// Copyright (c) 2022 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

function testUndefinedSpreadMemberExpr() {
    int[] _ = [...a1];
    int[] _ = [1, 2, ...a2];
    int[] _ = [1, 2, ...a3, 4];
    int[3] _ = [1, 2, ...a4];
    int[*] _ = [1, 2, ...a5];
}

function testTypeCheckingForArrayArrayCompatibility() {
    int n = 10;
    int[] _ = [1, 2, ...n]; // error

    string[] a1 = ["s1", "s2"];
    int[] _ = [1, 2, ...a1]; // error

    (int|string)[] a2 = [1, 2];
    int[] _ = [...a2]; // error

    any[] a3 = [1, 2];
    any[2] a4 = [1, 2];

    int[] _ = [1, 2, ...a3]; // error
    int[] _ = [1, 2, ...a4, 4]; // error
    int[5] _ = [1, 2, ...a3, 4]; // error
    int[5] _ = [1, 2, ...a4]; // error

    int[] x = [];
    (int|string)[] y = x;
    int[] _ = [...y]; // error

    int[3] p = [];
    (int|string)[] q = p;
    (int|string)[3] _ = [...q]; // error
}

function testTypeForTupleTupleCompatibility() {
    int n = 10;
    [int, string, int] _ = [1, "s", ...n]; // error
    [int, string, int...] _ = [1, "s", ...n]; // error

    [int, string] a1 = [1, "s"];
    [int, string, int] _ = [1, ...a1]; // error

    [(int|string), any...] a2 = [1, 2];
    [(int|string), int...] _ = [...a2]; // error

    [int, string] a3 = [1, "s"];
    [int, string, string...] a4 = [1, "s"];
    [int, string, (int|boolean)...] a5 = [1, "s"];

    [int, int, "s"] _ = [1, ...a3]; // error
    [int, int, string, int] _ = [1, ...a4, 4]; // error
    [int, int, (int|boolean)...] _ = [1, ...a3, true]; // error
    [int, int, (int|string)...] _ = [1, ...a5]; // error

    [int, string] x = [1, "s"];
    [int, string|int] y = x;
    [int, string, int] _ = [...y]; // error

    [(int|string), any...] p = [1];
    [any, any...] q = p;
    [(int|string), int...] _ = [...q]; // error
}

function testTypeForTupleArrayCompatibility() {
    (int|string)[] a1 = [1, "s"];
    [int, string, int] _ = [1, ...a1]; // error

    (int|string)[2] a2 = [1, "s"];
    [int, string, int] _ = [1, ...a2]; // error

    int[2] a3 = [1];
    [(int|string)] _ = [...a3]; // error

    any[2] a4 = [1, "s"];
    (int|string)[] a5 = [1, "s"];

    [int, int, "s"] _ = [1, ...a4]; // error
    [int, int, string, int] _ = [1, ...a5, 4]; // error
    [int, int, (int|boolean)...] _ = [1, ...a4, true]; // error
    [int, int, (int|string)...] _ = [1, ...a5]; // error

    [string, int] x = ["s", 1];
    (int|string)[2] y = x;
    [int, string, int] _ = [1, ...y]; // error

    [string, int, string...] p = ["s", 1];
    (int|string)[] q = p;
    [int, string, int, string...] _ = [1, ...q]; // error
}

function testTypeForArrayTupleCompatibility() {
    [int, string] a1 = [1, "s"];
    int[] _ = [1, ...a1]; // error
    int[3] _ = [1, ...a1]; // error

    [(int|string), any...] a2 = [1];
    (int|string)[] _ = [...a2]; // error

    [(int|string), string] a3 = ["x", "y"];
    ("x"|"y")[] _ = ["x", ...a3]; // error
    ("x"|"y")[] _ = [...a3, "y"]; // error
    ("x"|"y")[] _ = [...a3]; // error
    ("x"|"y")[2] _ = [...a3]; // error
    ("x"|"y")[*] _ = [...a3]; // error
    (string|boolean)[] _ = ["s", ...a3, true]; // error

    [int, string, string...] a4 = [1, "s"];
    (int|string)[4] _ = [1, ...a4, 4]; // error

    [int, string, (int|boolean)...] a5 = [1, "s"];
    [int, int, (int|string)...] _ = [1, ...a5]; // error
}

function testFixedLengthListError() {
    int[] a1 = [];
    int[] a2 = [3, 4, 5];

    int[0] _ = [ ... a1]; // error
    int[6] _ = [1, 2, ... a2, 6]; // error
    int[*] _ = [1, 2, ... a2, 6]; // error
    int[6] _ = [1, ...a1, 2, ... a2, 6]; // error
    int[*] _ = [...a1, ... a2]; // error
}

function testFixedMemberExpectedError() {
    string[] a1 = [];
    [string, string, string...] _ = [...a1]; // @error

    (int|string)[] a2 = [1, "s"];
    [int, any, (int|string)...] _ = [1, ...a2]; // error

    int[] a3 = [1, 2, 3, 4];
    [(int|string), int...] _ = [...a3]; // error

    [int, string, boolean...] a4 = [];
    [int, string, boolean, string...] _ = [...a4]; // error
}

function testNoFillerValueError() {
    string[1] a1 = [];
    [string, (string|int), string...] _ = [...a1]; // error
}

function testSpreadOpWithVaryingLength() {
    int[] a1 = [1];
    [int, int...] _ = [...a1]; // error

    string[] a2 = [];
    string[] a3 = ["s"];
    [string, string, string...] _ = [...a2,...a3]; // error

    [int, string...] a4 = [1, "s"];
    [int, string, string...] _ = [...a4]; // error
}

type IntArr int[];

type IntArr3 int[3];

type Foo [int, string, anydata...];

type Bar [int, string];

type Baz int[]|string|boolean;

function testSpreadOpWithListTypeBeingTypeRef() {
    IntArr _ = [1, ...[2, "s"]]; // error

    IntArr a = [1, ...[2, 3]]; // OK
    IntArr3 _ = [1, ...a]; // error

    Foo _ = [3, ...[4]]; // error

    Foo t = [3, ...["s"]]; // OK
    Bar _ = [...t]; // error
}

function testFixedMemberExpectedErrorPositive() {
    string[] a1 = [];
    string[1] a2 = [];

    [string, string, string...] _ = ["s1", "s2", ...a1]; // OK
    [string, string, string...] _ = [...a2, ...a2, ...a1]; // OK
    [string, string, string...] _ = [...a2, "s", ...a1]; // OK

    string[2] a3 = [];
    ["s", "S"] a4 = [];
    ["s", "S", "x", string] a5 = [];

    [string, string, string...] _ = [...a3]; // OK
    [string, string, string...] _ = [...a4]; // OK
    [string, string, string...] _ = [...a5]; // OK
    [int, boolean, "s", string...] _ = [ 2, true, ...a4]; // OK
    [int, boolean, "s", string...] _ = [ 2, true, ...a4, "y"]; // OK

    string[] a6 = [];
    [string...] a7 = [];

    string[] _ = [...a6]; // OK
    string[] _ = [...a7]; // OK
    [string...] _ = [...a6]; // OK
    [string...] _ = [...a7]; // OK
}

function testTypeCheckingPositive1() {
    int[] a1 = [3, 4];
    int[] a2 = [6, 7];

    int[] _ = [...a1]; // OK
    int[] _ = [...a1, ...a2]; // OK
    int[] _ = [1, 2, ...a1]; // OK
    int[] _ = [1, 2, ...a1, ...a2]; // OK
    int[] _ = [1, 2, ...a1, 5, ...a2]; // OK

    int[2] a3 = [1, 2];
    int[4] a4 = [3];

    int[] _ = [...a3]; // OK
    int[] _ = [...a3, ...a4]; // OK
    int[] _ = [1, 2, ...a3]; // OK
    int[2] _ = [...a3]; // OK
    int[6] _ = [1, ...a4, 2]; // OK

    int[2] a5 = [];
    string[1] a6 = [];

    (string|int)[] _ = [...a5, ...a6]; // OK
    (string|int)[4] _ = [...a5, ...a6, 7]; // OK

    (int|string)[] a7 = [1, 2];
    any[] _ = [...a7]; // OK

    int[] x = [];
    (int|string)[] y = x;
    (int|string)[] _ = [...y]; // OK

    int[3] p = [];
    (int|string)[] q = p;
    (int|string)[] _ = [...q]; // OK
}

function testTypeCheckingPositive2() {
    (int|string)[2] a1 = [1, "s"];
    [int, any, (int|string|boolean)] _ = [1, ...a1]; // OK

    (int|string)[*] a2 = [1, "s"];
    [int, any, (int|string)...] _ = [1, ...a2]; // OK

    int[1] a5 = [1];
    [(int|string), int...] _ = [...a5]; // OK
}

function testFillerValuePositive() {
    string[1] a1 = [];
    [string, string, string...] _ = [...a1]; // OK
    [string, string, (int|string)...] _ = [...a1]; // OK
}

function testSpreadOpWithVaryingLengthPositive() {
    int[] a1 = [];
    [int, string, boolean...] a2 = [];
    [int, string, any...] _ = [1, "s", ...a1, ...a1, 23, "x", ...a2]; // OK

    [int, string] a3 = [1, "s"];
    [int, any, anydata...] _ = [...a3, ...a1, true, ...a2, "x"]; // OK

    [int, string, string] a4 = [1, "s"];
    [int, any, anydata...] _ = [...a4, ...a2, true, ...a1, "x"]; // OK
}

function testSpreadOpWithNoRefPositive() {
    int[] _ = [1, ...[2, 3]]; // OK
    (int|string)[] _ = [1, "x", ...["s", 4]]; // OK
    int[] _ = [1, 2, ...[3, ...[...[4, 5], 6], 7], ...[8]]; // OK

    int[3] _ = [1, ...[2, 3]]; // OK
    (int|string)[4] _ = [1, "x", ...["s", 4]]; // OK
    int[8] _ = [1, 2, ...[3, ...[...[4, 5], 6], 7], ...[8]]; // OK

    [int, string...] _ = [3, ...["x", "y"]]; //OK
    [string, string, string...] _ = [...["x", "y"]]; // OK

    [int, string] _ = [3, ...["x"]]; // OK
    [string, boolean|string, int] _ = [...["x", "y"], 2]; // OK
}

function testSpreadOpWithListTypeBeingTypeRefPositive() {
    IntArr a = [1, ...[2, 3]]; // OK
    IntArr _ = [1, ...a]; // OK

    [int, string, boolean]  t = [];
    Foo _ = [...t, true, false]; // OK

    Baz _ = [...a, 4]; // OK

    Foo b = [4, "x"];
    Foo _ = [4, "x", ...b]; // OK
}

// TODO: add test with `never`
