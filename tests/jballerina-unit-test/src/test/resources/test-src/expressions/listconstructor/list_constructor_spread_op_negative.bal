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

    [(int|string), int, any...] a2 = [1, 2];
    [(int|string), string...] _ = [...a2]; // error
    [(int|string), int, int...] _ = [...a2]; // error

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
    [int...] _ = [...a1]; // error

    (int|string)[2] a2 = [1, "s"];
    [int, string, int] _ = [1, ...a2]; // error

    string[3] a3 = [];
    [string, string, int...] _ = [...a3]; // error
    [string, string] _ = [...a3]; // error

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
    (int|string)[] _ = [1, ...a5]; // error
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

type Qux int;

function testSpreadOpWithListTypeBeingTypeRef() {
    IntArr _ = [1, ...[2, "s"]]; // error

    IntArr a = [1, ...[2, 3]]; // OK
    IntArr3 _ = [1, ...a]; // error

    Foo _ = [3, ...[4]]; // error

    Foo t = [3, ...["s"]]; // OK
    Bar _ = [...t]; // error

    Qux x = 3;
    IntArr _ = [1, 2, ...x]; // error
}
