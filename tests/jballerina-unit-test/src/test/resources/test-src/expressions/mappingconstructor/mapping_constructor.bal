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

const ASSERTION_ERROR_REASON = "AssertionError";

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

function testNonAmbiguousMapUnionTarget() {
    map<int>|map<string> m1 = {a: 1, b: 2};

    map<int|string>|map<string|boolean> m2 = {
        a: true,
        b: false,
        c: true
    };

    if !(m1 is map<int>) {
        panic getFailureError("map<int>", typeof m1);
    }
    assertEquality(1, m1["a"]);
    assertEquality(2, m1["b"]);

    if !(m2 is map<string|boolean>) {
        panic getFailureError("map<(string|boolean)>", typeof m2);
    }
    assertEquality(true, m2["a"]);
    assertEquality(false, m2["b"]);
    assertEquality(true, m2["c"]);
}

function testTypeWithReadOnlyInUnionCET() {
    map<string> m = {x: "x", y: "y"};
    map<map<json>>|readonly mr = {
        a: m,
        b: {}
    };

    assertEquality(true, <any> checkpanic mr is map<map<json>>);
    assertEquality(false, mr is map<map<json>> & readonly);

    // Updates should be allowed.
    map<map<json>> mj = <map<map<json>>> checkpanic mr;
    mj["a"]["z"] = "z";
    assertEquality(true, mj["b"] is map<json>);
    mj["b"]["a"] = 1;
    mj["c"] = m;

    assertEquality("x", mj["a"]["x"]);
    assertEquality("y", mj["a"]["y"]);
    assertEquality("z", mj["a"]["z"]);
    assertEquality("z", mj["a"]["z"]);
    assertEquality(1, mj["b"]["a"]);
    assertEquality((), mj["b"]["b"]);
    assertEquality(m, mj["c"]);
}

function testFieldsWithEscapeSequences() {
    map<int> _ = {a\\: 454, "a\\\\": 543};
    map<int> _ = {a\\: 454, "b\\": 543};
    
    string s = "a\\";
    map<int> m = {a\\: 454, [s]: 543};
    map<int> n = {"a\\": 543};
    assertEquality(n, m);
}

function assertEquality(any|error expected, any|error actual) {
    if isEqual(expected, actual) {
        return;
    }

    panic getFailureError(expected, actual);
}

function getFailureError(any|error expected, any|error actual) returns error {
    string expectedValAsString = expected is error ? expected.toString() : expected.toString();
    string actualValAsString = actual is error ? actual.toString() : actual.toString();
    return  error(ASSERTION_ERROR_REASON,
                    message = "expected '" + expectedValAsString + "', found '" + actualValAsString + "'");
}

isolated function isEqual(any|error val1, any|error val2) returns boolean {
    if (val1 is anydata && val2 is anydata) {
        return (val1 == val2);
    } else {
        return (val1 === val2);
    }
}
