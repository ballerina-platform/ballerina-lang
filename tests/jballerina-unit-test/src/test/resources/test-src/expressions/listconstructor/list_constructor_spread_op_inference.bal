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

function testSpreadOpInferenceWithVar() {
    int[2] a1 = [];

    var v1 = [...a1];
    assertArrayEquality(v1, [0, 0]);

    var v2 = [1, "y", ...a1, true];
    assertArrayEquality(v2, [1, "y", 0, 0, true]);

    [string, (int|boolean)] a2 = ["s", true];

    var v3 = [...a2];
    assertArrayEquality(v3, ["s", true]);

    var v4 = [1, "y", ...a2, true];
    assertArrayEquality(v4, [1, "y", "s", true, true]);

    var v5 = [true, ...a1, 56, ...a2, "s"];
    assertArrayEquality(v5, [true, 0, 0, 56, "s", true, "s"]);

    var v6 = [...v5, false];
    assertArrayEquality(v6, [true, 0, 0, 56, "s", true, "s", false]);
}

type ReadonlyIntArr readonly & int[3];

function testSpreadOpInferenceWithReadonly() {
    int[2] & readonly a1 = [];
    readonly v1 = [...a1]; // OK
    readonly v2 = [1, "y", ...a1, true]; // OK

    [int, string] & readonly a2 = [];
    readonly v3 = [...a2]; // OK
    readonly v4 = [1, "y", ...a2, true]; // OK

    [string, (int|boolean)] & readonly a3 = ["s", true];
    readonly v5 = [...a3]; // OK
    readonly v6 = [1, "y", ...a3, true]; // OK

    ReadonlyIntArr a4 = [2];
    readonly v7 = [...a4]; // OK
    readonly v8 = [33, ...a4, 5]; // OK

    string|int s = "SHH";

    readonly v9 = [33, ...a4, s, 5]; // OK
    readonly|int v10 = [33, ...a4, s, 5]; // OK
    readonly & (int|string)[] v11 = [33, ...a4, s, 5]; // OK
}

function testSpreadOpWithTypedesc() {
    int[2] a1 = [];
    typedesc v1 = [...a1]; // OK
    typedesc v2 = [string, ...a1, boolean]; // OK

    [string, (int|boolean)] a2 = ["s", true];
    typedesc v3 = [...a2]; // OK
    typedesc v4 = [string , ...a2, any]; // OK

    typedesc v5 = [string , ...[int, string], [3]]; // OK
}

type IntArr int[];

type IntArr3 int[3];

type Foo [anydata, string];

function testInferenceViaSpreadOpWithTypeRef() {
    IntArr3 v1 = [1, ...[2, 3]];
    assertArrayEquality(v1, [1, 2, 3]);

    var v2 = ["s", ...v1];
    assertArrayEquality(v2, ["s", 1, 2, 3]);

    var v3 = [...v2, false];
    assertArrayEquality(v3, ["s", 1, 2, 3, false]);

    Foo v4 = [...[true], "s"];
    assertArrayEquality(v4, [true, "s"]);

    var v5 = [...v4, 4];
    assertArrayEquality(v5, [true, "s", 4]);

    var v6 = [...v5];
    assertArrayEquality(v6, [true, "s", 4]);
}

function testSpreadOpInferenceWithNeverRestDescriptor() {
    [int, never...] a1 = [1];
    [(int|string), string, never...] a2 = [1];

    var v1 = [...a1];
    assertArrayEquality(v1, [1]);

    var v2 = [1, ...a1, "s", ...a2, true];
    assertArrayEquality(v2, [1, 1, "s", 1, "", true]);
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
