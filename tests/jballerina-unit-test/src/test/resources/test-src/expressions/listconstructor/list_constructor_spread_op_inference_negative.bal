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

// TODO: add readonly tests
