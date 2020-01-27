// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ballerina/lang.'float;

const ASSERTION_ERROR_REASON = "AssertionError";

type Foo record {|
    string s;
    int i;
|};

string s = "global s";
int i = 1;

function testVarNameAsRecordField() {
    string s = "local s";

    Foo f = {s, i};
    Foo expectedF = {s: "local s", i: 1};

    if f == expectedF {
        return;
    }

    panic getFailureError(expectedF, f);
}

const T = "T value";

function testVarNameAsMapField() {
    string s = "local s";

    map<string|int> m = {s, i, T};
    map<string|int> expectedM = {s: "local s", i: 1, "T": "T value"};

    if m == expectedM {
        return;
    }

    panic getFailureError(expectedM, m);
}

function testVarNameAsJsonField() {
    string s = "local s";

    json j = {s, i};
    json expectedJ = {s: "local s", i: 1};

    if j == expectedJ {
        return;
    }

    panic getFailureError(expectedJ, j);
}

function testLikeModuleQualifiedVarNameAsJsonField() {
    float PI = 1.2;

    _ = 'float:NaN; // to avoid unused import error

    map<float> m = {a: 1.0, 'float:PI};
    map<float> expectedM = {a: 1.0, 'float: 1.2};

    if m == expectedM {
        return;
    }

    panic getFailureError(expectedM, m);
}

function getFailureError(any|error expected, any|error actual) returns error {
    return  error(ASSERTION_ERROR_REASON,
                    message = "expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}
