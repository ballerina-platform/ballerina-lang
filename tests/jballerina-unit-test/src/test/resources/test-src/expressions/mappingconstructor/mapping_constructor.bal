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

annotation Foo foo on service;

service serv1 =
@foo {
    s,
    i
}
service {

};

function testVarNameFieldInAnnotation() {
    s = "new value";

    service serv2 =
    @foo {
        s,
        i: 100
    }
    service {

    };

    typedesc<any> t1 = typeof serv1;
    Foo? fn1 = t1.@foo;
    Foo expectedFn1 = {s: "global s", i: 1};

    if fn1 != expectedFn1 {
        panic getFailureError(expectedFn1, fn1);
    }

    typedesc<any> t2 = typeof serv2;
    Foo? fn2 = t2.@foo;
    Foo expectedFn2 = {s: "new value", i: 100};

    if fn2 == expectedFn2 {
        return;
    }

    panic getFailureError(expectedFn2, fn2);
}

const TYPEDESC_MAP_ANY = "typedesc map";

function testMappingConstuctorWithAnyACET() {
    any a = {a: 1, b: 2};
    typedesc<any> ta = typeof a;
    string typedescString = ta.toString();

    if typedescString != TYPEDESC_MAP_ANY {
        panic getFailureError(TYPEDESC_MAP_ANY, typedescString);
    }

    any|map<any> b = {a: "hello", b: 1};
    ta = typeof b;
    typedescString = ta.toString();

    if typedescString == TYPEDESC_MAP_ANY {
        return;
    }

    panic getFailureError(TYPEDESC_MAP_ANY, typedescString);
}

const TYPEDESC_MAP_ANYDATA = "typedesc map<anydata>";

function testMappingConstuctorWithAnydataACET() {
    anydata a = {a: "a", b: "b"};
    typedesc<any> ta = typeof a;
    string typedescString = ta.toString();

    if typedescString != TYPEDESC_MAP_ANYDATA {
        panic getFailureError(TYPEDESC_MAP_ANYDATA, typedescString);
    }

    anydata|string|map<anydata> b = {a: "hello", b: 1};
    ta = typeof b;
    typedescString = ta.toString();

    if typedescString == TYPEDESC_MAP_ANYDATA {
        return;
    }

    panic getFailureError(TYPEDESC_MAP_ANYDATA, typedescString);
}

const TYPEDESC_MAP_JSON = "typedesc map<json>";

function testMappingConstuctorWithJsonACET() {
    json a = {a: "a", b: true};
    typedesc<any> ta = typeof a;
    string typedescString = ta.toString();

    if typedescString != TYPEDESC_MAP_JSON {
        panic getFailureError(TYPEDESC_MAP_JSON, typedescString);
    }

    json|string b = {a: "hello", b: 1};
    ta = typeof b;
    typedescString = ta.toString();

    if typedescString == TYPEDESC_MAP_JSON {
        return;
    }

    panic getFailureError(TYPEDESC_MAP_JSON, typedescString);
}

var v1 = {
    a: 1,
    b: "hello world",
    c: new Bar("bar")
};

function testMappingConstrExprWithNoACET() {
    string expectedTypedescString = "typedesc map<int|string|Bar>";
    typedesc<any> ta = typeof v1;
    string typedescString = ta.toString();

    if typedescString != expectedTypedescString {
        panic getFailureError(expectedTypedescString, typedescString);
    }

    var v2 = {
        s,
        i,
        s2: s,
        t: typeof s
    };

    expectedTypedescString = "typedesc map<string|int|typedesc>";
    ta = typeof v2;
    typedescString = ta.toString();

    if typedescString == expectedTypedescString {
        return;
    }
    panic getFailureError(expectedTypedescString, typedescString);
}

function testMappingConstrExprWithNoACET2() {
    string expectedTypedescString = "typedesc map<json|xml>";

    json j = 2;

    var v2 = {
        a: 1,
        b: j,
        c: 23.10,
        d: xml `foo`,
        e: "str"
    };

    typedesc<any> ta = typeof v2;
    string typedescString = ta.toString();

    if typedescString == expectedTypedescString {
        return;
    }
    panic getFailureError(expectedTypedescString, typedescString);
}

type Bar object {
    public function __init(any arg) {

    }
};

function getFailureError(any|error expected, any|error actual) returns error {
    return  error(ASSERTION_ERROR_REASON,
                    message = "expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}
