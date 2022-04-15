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

function testArrayArrayCompatibility() {
    int[] a1 = [3, 4];
    int[] a2 = [6, 7];

    int[] v1 = [...a1];
    assertArrayEquality(v1, [3, 4]);

    int[] v2 = [...a1, ...a2];
    assertArrayEquality(v2, [3, 4, 6, 7]);

    int[] v3 = [1, 2, ...a1];
    assertArrayEquality(v3, [1, 2, 3, 4]);

    int[] v4 = [1, 2, ...a1, ...a2];
    assertArrayEquality(v4, [1, 2, 3, 4, 6, 7]);

    int[] v5 = [1, 2, ...a1, 5, ...a2];
    assertArrayEquality(v5, [1, 2, 3, 4, 5, 6, 7]);

    int[2] a3 = [1, 2];
    int[4] a4 = [3];

    int[] v6 = [...a3];
    assertArrayEquality(v6, [1, 2]);

    int[] v7 = [...a3, ...a4];
    assertArrayEquality(v7, [1, 2, 3, 0, 0, 0]);

    int[] v8 = [1, 2, ...a3];
    assertArrayEquality(v8, [1, 2, 1, 2]);

    int[2] v9 = [...a3];
    assertArrayEquality(v9, [1, 2]);

    int[6] v10 = [1, ...a4];
    assertArrayEquality(v10, [1, 3, 0, 0, 0, 0]);

    int[2] a5 = [];
    string[1] a6 = [];

    (string|int)[] v11 = [...a5, ...a6];
    assertArrayEquality(v11, [0, 0, ""]);

    (string|int)[4] v12 = [...a5, ...a6, 7];
    assertArrayEquality(v12, [0, 0, "", 7]);

    (int|string)[] a7 = [1, 2];
    any[] v13 = [...a7];
    assertArrayEquality(v13, [1, 2]);

    int[*] a8 = [3, 4];
    int[*] a9 = [6, 7];

    int[] v14 = [2, ...a8, 5, ...a9];
    assertArrayEquality(v14, [2, 3, 4, 5, 6, 7]);

    int[*] v15 = [2, ...a8, 5, ...a9];
    assertArrayEquality(v15, [2, 3, 4, 5, 6, 7]);

    int[] x = [21, 5];
    (int|string)[] y = x;
    (int|string)[] v16 = [...y];
    assertArrayEquality(v16, [21, 5]);

    int[3] p = [41, 21];
    (int|string)[] q = p;
    (int|string)[] v17 = [...q];
    assertArrayEquality(v17, [41, 21, 0]);
}

function testTupleTupleCompatibility() {
    [int, string] a1 = [1, "s"];
    [int, string, int] v1 = [...a1, 4];
    assertArrayEquality(v1, [1, "s", 4]);

    [(int|string), int, string...] a2 = [1, 2];
    [(int|string), any...] v2 = [...a2];
    assertArrayEquality(v2, [1, 2]);

    [int, "s"] a3 = [1, "s"];
    [int, string, (int|string)...] a4 = [1, "s"];

    [int, int, "s"] v3 = [1, ...a3];
    assertArrayEquality(v3, [1, 1, "s"]);

    [int, int, (int|string)...] v4 = [1, ...a3, "x"];
    assertArrayEquality(v4, [1, 1, "s", "x"]);

    [int, int, (int|string)...] v5 = [1, ...a4];
    assertArrayEquality(v5, [1, 1, "s"]);

    [int, string] x = [1, "s"];
    [int, string|int] y = x;
    [int, string|int] v6 = [...y];
    assertArrayEquality(v6, [1, "s"]);

    [(int|string), any...] p = [1];
    [any, any...] q = p;
    [any, any...] v7 = [...q];
    assertArrayEquality(v7, [1]);
}

function testTupleArrayCompatibility() {
    int[2] a1 = [1];
    [(int|string)...] v1 = [...a1];
    assertArrayEquality(v1, [1, 0]);

    int[1] a2 = [1];
    [(int|string), int...] v2 = [...a2];
    assertArrayEquality(v2, [1]);

    (int|string)[2] a3 = [1, "s"];
    [int, any, (int|string|boolean)] v3 = [1, ...a3];
    assertArrayEquality(v3, [1, 1, "s"]);

    [int|string, any...] v4 = [...a3];
    assertArrayEquality(v4, [1, "s"]);

    (int|string)[*] a4 = [1, "s"];
    [int, any, (int|string)...] v5 = [1, ...a4];
    assertArrayEquality(v5, [1, 1, "s"]);

    anydata[2] a5 = [1, "s"];
    [int, any, anydata] v6 = [1, ...a5];
    assertArrayEquality(v6, [1, 1, "s"]);

    (int|string)[] a6 = [1, "s"] ;
    [int, (int|string)...] v7 = [1, ...a6, "x", ...a6, 4];
    assertArrayEquality(v7, [1, 1, "s", "x", 1, "s", 4]);

    [int|string] x = ["s"];
    (int|string)[1] y = x;
    [int|string] v8 = [...y];
    assertArrayEquality(v8, ["s"]);

    [(int|string)...] p = [56, "zoo", "run"];
    (int|string)[] q = p;
    [(int|string)...] v9 = [...q];
    assertArrayEquality(v9, [56, "zoo", "run"]);
}

function testArrayTupleCompatibility() {
    [int, string] a1 = [1, "s"];
    (int|string)[] v1 = [1, ...a1];
    assertArrayEquality(v1, [1, 1, "s"]);

    (int|string)[3] v2 = [1, ...a1];
    assertArrayEquality(v2, [1, 1, "s"]);

    [(int|string), boolean...] a2 = [1];
    (int|string|boolean)[] v3 = [...a2];
    assertArrayEquality(v3, [1]);

    [(int|string), string] a3 = ["x", "y"];
    (string|int)[*] v4 = ["s", ...a3, 4, ...a3];
    assertArrayEquality(v4, ["s", "x", "y", 4, "x", "y"]);

    [int] a4 = [1];
    int[4] v5 = [1, ...a4];
    assertArrayEquality(v5, [1, 1, 0, 0]);

    (int|string)[1] x = ["s"];
    [int|string] y = x;
    (int|string)[1] v6 = [...y];
    assertArrayEquality(v6, ["s"]);

    (int|string)[]  p = ["tree", 33, 5];
    [(int|string)...]  q = p;
    (int|string)[] v7 = [...q];
    assertArrayEquality(v7, ["tree", 33, 5]);
}

function testFillerValue1() {
    string[] a1 = [];
    string[1] a2 = [];

    [string, string, string...] v1 = ["s1", "s2", ...a1];
    assertArrayEquality(v1, ["s1", "s2"]);

    [string, string, string...] v2 = [...a2, ...a2, ...a1];
    assertArrayEquality(v2, ["", ""]);

    [string, string, string...] v3 = [...a2, "s", ...a1];
    assertArrayEquality(v3, ["", "s"]);

    string[2] a3 = [];
    ["s", "S"] a4 = [];
    ["s", "S", "x", string] a5 = [];

    [string, string, string...] v4 = [...a3];
    assertArrayEquality(v4, ["", ""]);

    [string, string, string...] v5 = [...a4];
    assertArrayEquality(v5, ["s", "S"]);

    [string, string, string...] v6 = [...a5];
    assertArrayEquality(v6, ["s", "S", "x", ""]);

    [int, boolean, "s", string...] v7 = [2, true, ...a4];
    assertArrayEquality(v7, [2, true, "s", "S"]);

    [int, boolean, "s", string...] v8 = [2, true, ...a4, "y"];
    assertArrayEquality(v8, [2, true, "s", "S", "y"]);

    string[] a6 = [];
    [string...] a7 = [];

    string[] v9 = [...a6];
    string[] v10 = [...a7];
    [string...] v11 = [...a6];
    [string...] v12 = [...a7];

    assertArrayEquality(v9, []);
    assertArrayEquality(v10, []);
    assertArrayEquality(v11, []);
    assertArrayEquality(v12, []);
}

function testFillerValue2() {
    string[1] a1 = [];

    [string, string, string...] v1 = [...a1];
    assertArrayEquality(v1, ["", ""]);

    [string, string, (int|string)...] v2 = [...a1];
    assertArrayEquality(v2, ["", ""]);
}

function testSpreadOpWithVaryingLengthRef() {
    int[] a1 = [];
    [int, string, boolean...] a2 = [];
    [int, string, any...] v1 = [1, "s", ...a1, ...a1, 23, "x", ...a2];
    assertArrayEquality(v1, [1, "s", 23, "x", 0, ""]);

    a1[0] = 33;
    a1[1] = 89;
    assertArrayEquality(v1, [1, "s", 23, "x", 0, ""]);

    [int, string] a3 = [1, "s"];
    [int, any, anydata...] v2 = [...a3, ...a1, true, ...a2, "x"];
    assertArrayEquality(v2, [1, "s", 33, 89, true, 0, "", "x"]);

    [int, string, string] a4 = [1, "s"];
    [int, any, anydata...] v3 = [...a4, ...a2, true, ...a1, "x"];
    assertArrayEquality(v3, [1, "s", "", 0, "", true, 33, 89, "x"]);
}

type IntArr int[];

type IntArr3 int[3];

type Foo [int, string, anydata...];

type Bar [int, string];

type Baz int[]|string|boolean;

function testSpreadOpWithListConstructorTypeBeingTypeRef() {
    IntArr v1 = [1, ...[2, 3]];
    assertArrayEquality(v1, [1, 2, 3]);

    IntArr v2 = [1, ...v1];
    assertArrayEquality(v2, [1, 1, 2, 3]);

    [int, string, boolean]  t = [];
    Foo v3 = [...t, true, false];
    assertArrayEquality(v3, [0, "", false, true, false]);

    Baz v4 = [...v1, 4];
    assertArrayEquality(<any[]>v4, [1, 2, 3, 4]);

    Foo b = [4, "x"];
    Foo v5 = [4, "x", ...b];
    assertArrayEquality(v5, [4, "x", 4, "x"]);
}

function testSpreadOpWithExprNotBeingAReference() {
    int[] v1 = [1, ...[2, 3]];
    assertArrayEquality(v1, [1, 2, 3]);

    (int|string)[] v2 = [1, "x", ...["s", 4]];
    assertArrayEquality(v2, [1, "x", "s", 4]);

    int[] v3 = [1, 2, ...[3, ...[...[4, 5], 6], 7], ...[8]];
    assertArrayEquality(v3, [1, 2, 3, 4, 5, 6, 7, 8]);

    int[3] v4 = [1, ...[2, 3]];
    assertArrayEquality(v4, [1, 2, 3]);

    (int|string)[4] v5 = [1, "x", ...["s", 4]];
    assertArrayEquality(v5, [1, "x", "s", 4]);

    int[8] v6 = [1, 2, ...[3, ...[...[4, 5], 6], 7], ...[8]];
    assertArrayEquality(v6, [1, 2, 3, 4, 5, 6, 7, 8]);

    [int, string...] v7 = [3, ...["x", "y"]];
    assertArrayEquality(v7, [3, "x", "y"]);

    [string, string, string...] v8 = [...["x", "y"]];
    assertArrayEquality(v8, ["x", "y"]);

    [int, string] v9 = [3, ...["x"]];
    assertArrayEquality(v9, [3, "x"]);

    [string, boolean|string, int] v10 = [...["x", "y"], 2];
    assertArrayEquality(v10, ["x", "y", 2]);
}

type NeverRecord record {|
    never a;
|};

function testSpreadOpTupleWithNeverRestDescriptor() {
    [int, never...] t1 = [3];
    int[1] v1 = [...t1];
    assertArrayEquality(v1, [3]);

    int[3] v2 = [...t1, 7, ...t1];
    assertArrayEquality(v2, [3, 7, 3]);

    [int, string, int] v3 = [...t1, "x", ...t1];
    assertArrayEquality(v3, [3, "x", 3]);

    [string, boolean, never...] t2 = [];

    [string, boolean, int, any...] v4 = [...t2, 5];
    assertArrayEquality(v4, ["", false, 5]);

    [string, boolean, int] v5 = [...t2];
    assertArrayEquality(v5, ["", false, 0]);

    [string, boolean, NeverRecord...] t3 = ["s"];

    [string, boolean, int, any...] v6 = [...t3, 5];
    assertArrayEquality(v6, ["s", false, 5]);

    [string, boolean, int] v7 = [...t3];
    assertArrayEquality(v7, ["s", false, 0]);
}

int[] a1 = [3, 4];
int[] a2 = [6, 7];
int[] v1 = [1, 2, ...a1, 5, ...a2];

[int, "s"] t1 = [1, "s"];
[int, int, (int|string)...] v2 = [1, ...t1, "x"];

(int|string)[*] a3 = [1, "s"];
[int, any, (int|string)...] v3 = [1, ...a3];

[(int|string), string] t2 = ["x", "y"];
(string|int)[*] v4 = ["s", ...t2, 4, ...t2];

function testSpreadOpAtModuleLevel() {
    assertArrayEquality(v1, [1, 2, 3, 4, 5, 6, 7]);
    assertArrayEquality(v2, [1, 1, "s", "x"]);
    assertArrayEquality(v3, [1, 1, "s"]);
    assertArrayEquality(v4, ["s", "x", "y", 4, "x", "y"]);

    any[] v5 = ["SHH", ...v3, false];
    assertArrayEquality(v5, ["SHH", 1, 1, "s", false]);
}

const ASSERTION_ERROR_REASON_1 = "AssertionError";

function assertEquality(any|error actual, any|error expected) {
    assertEqualityWithErrorReason(actual, expected, ASSERTION_ERROR_REASON_1);
}

function assertEqualityWithErrorReason(any|error actual, any|error expected, string errorReason) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    string expectedValAsString = expected is error ? expected.toString() : expected.toString();
    string actualValAsString = actual is error ? actual.toString() : actual.toString();
    panic error(errorReason, message = "expected '" + expectedValAsString + "', found '" + actualValAsString + "'");
}

const ASSERTION_ERROR_REASON_2 = "ArrayLengthMismatchError";

function assertArrayEquality(any[] actualArr, any[] expectedArr) {
    assertEqualityWithErrorReason(actualArr.length(), expectedArr.length(), ASSERTION_ERROR_REASON_2);

    foreach int i in 0 ..< actualArr.length() {
        var actualElement = actualArr[i];
        var expectedElement = expectedArr[i];

        if actualElement is any[] && expectedElement is any[] {
            assertArrayEquality(actualElement, expectedElement);
            return;
        }

        assertEquality(actualElement, expectedElement);
    }
}
