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
    var _ = [...a]; // error

    int n = 3;
    var _ = [...n]; // error
    var _ = [1, 2, ...n, 3]; // error

    int[] a1 = [];
    var _ = [...a1]; // error
    var _ = [1, 2, ...a1, 3]; // error

    [int, int...] a2 = [1];
    var _ = [...a2]; // error
    var _ = [1, 2, ...a2, 3]; // error

    int[2] a3 = [];
    var v1 = [...a3]; // OK
    var v2 = [1, "y", ...a3, true]; // OK

    int _ = v1; // error: expected 'int', found '[int,int]'
    int _ = v2; // error: expected 'int', found '[int,int,int,int,int]'

    [string, (int|boolean)] a4 = ["s", true];
    var v3 = [...a4]; // OK
    var v4 = [1, "y", ...a4, true]; // OK

    int _ = v3; // error: expected 'int', found '[int,int]'
    int _ = v4; // error: expected 'int', found '[int,int,int,int,int]'
}

function testSpreadOpInferenceWithReadonly1() {
    readonly v1 = [...a]; // error

    int n = 3;
    readonly v2 = [...n]; // error
    readonly v3 = [1, 2, ...n, 3]; // error

    int[] a1 = [];
    readonly v4 = [...a1]; // error
    readonly v5 = [1, 2, ...a1, 3]; // error

    [int, int...] a2 = [1];
    readonly v6 = [...a2]; // error
    readonly v7 = [1, 2, ...a2, 3]; // error

    int[2] a3 = [];
    readonly v8 = [...a3]; // error
    readonly v9 = [1, "y", ...a3, true]; // error

    [int, string] a4 = [];
    readonly v10 = [...a4]; // error
    readonly v11 = [1, "y", ...a4, true]; // error

    [string, (int|boolean)] a5 = ["s", true];
    readonly v12 = [...a5]; // error
    readonly v13 = [1, "y", ...a5, true]; // error
}

function testSpreadOpInferenceWithReadonly2() {
    int[] & readonly a1 = [];
    readonly v1 = [...a1]; // error
    readonly v2 = [1, 2, ...a1, 3]; // error

    [int, int...] & readonly a2 = [1];
    readonly v3 = [...a2]; // error
    readonly v4 = [1, 2, ...a2, 3]; // error
}

type ReadonlyIntArr readonly & int[3];

function testSpreadOpInferenceWithReadonly3() {
    int[2] & readonly a1 = [];
    readonly v1 = [...a1]; // error
    readonly v2 = [1, "y", ...a1, true]; // OK

    int _ = v1; // error: expected 'int', found 'readonly'
    int _ = v2; // error: expected 'int', found 'readonly'

    [int, string] & readonly a2 = [];
    readonly v3 = [...a2]; // error
    readonly v4 = [1, "y", ...a2, true]; // OK

    int _ = v3; // error: expected 'int', found 'readonly'
    int _ = v4; // error: expected 'int', found 'readonly'

    [string, (int|boolean)] & readonly a3 = ["s", true];
    readonly v5 = [...a3]; // OK
    readonly v6 = [1, "y", ...a3, true]; // OK

    int _ = v5; // error: expected 'int', found 'readonly'
    int _ = v6; // error: expected 'int', found 'readonly'

    ReadonlyIntArr a4 = [2];
    readonly v7 = [...a4]; // OK
    readonly v8 = [33, ...a4, 5]; // OK

    int _ = v7; // error: expected 'int', found 'readonly'
    int _ = v8; // error: expected 'int', found 'readonly'

    string|int s = "s";
    readonly v9 = [33, ...a4, s, 5]; // OK

    int _ = v9; // error: expected 'int', found 'readonly'

    readonly|int v10 = [33, ...a4, s, 5]; // OK
    readonly & (int|string)[] v11 = [33, ...a4, s, 5]; // OK

    boolean _ = v10; // error: expected 'int', found '(readonly|int)'
    boolean _ = v11; // error: expected 'boolean', found '((int|string)[] & readonly)'
}

function testSpreadOpInferenceWithNeverRestDesc() {
    [int, never...] a1 = [1];
    [(int|string), string, never...] a2 = [1];

    var v1 = [...a1]; // OK
    var v2 = [1, ...a1, "s", ...a2, true]; // OK

    boolean _ = v1; // error: expected 'boolean', found '[int]'
    boolean _ = v2; // error: expected 'boolean', found '[int,int,string,(int|string),string,boolean]'
}

function testSpreadOpInferenceWithVarPositive() {
    int[2] a1 = [];
    var v1 = [...a1]; // OK
    var v2 = [1, "y", ...a1, true]; // OK

    [string, (int|boolean)] a2 = ["s", true];
    var v3 = [...a2]; // OK
    var v4 = [1, "y", ...a2, true]; // OK

    var v5 = [ true, ...a1, 56, ...a2, "s"]; // OK
    var v6 = [...v5, false]; // OK
}

function testSpreadOpInferenceWithReadonlyPositive() {
    int[2] & readonly a1 = [];
    readonly v1 = [...a1]; // error
    readonly v2 = [1, "y", ...a1, true]; // OK

    [int, string] & readonly a2 = [];
    readonly v3 = [...a2]; // error
    readonly v4 = [1, "y", ...a2, true]; // OK

    [string, (int|boolean)] & readonly a3 = ["s", true];
    readonly v5 = [...a3]; // OK
    readonly v6 = [1, "y", ...a3, true]; // OK

    ReadonlyIntArr a4 = [2];
    readonly v7 = [...a4]; // OK
    readonly v8 = [33, ...a4, 5]; // OK

    string|int s = "s";
    readonly v9 = [33, ...a4, s, 5]; // OK

    readonly|int v10 = [33, ...a4, s, 5]; // OK
    readonly & (int|string)[] v11 = [33, ...a4, s, 5]; // OK
}

function testSpreadOpInferenceWithNeverRestDescPositive() {
    [int, never...] a1 = [1];
    [(int|string), string, never...] a2 = [1];

    var v1 = [...a1]; // OK
    var v2 = [1, ...a1, "s", ...a2, true]; // OK
}
