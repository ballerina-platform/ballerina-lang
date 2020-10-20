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

type Foo record {|
    string s;
    int i;
    float f;
    boolean|float...;
|};

type Bar record {|
    string s;
    int i;
|};

public function testMapRefAsSpreadOp() {
    map<float> m1 = {q: 1.0, w: 2.0};
    map<boolean|float> m2 = {...m1};

    assertEquality(2, m2.length());
    assertEquality(1.0, m2["q"]);
    assertEquality(2.0, m2["w"]);
}

public function testMapValueViaFuncAsSpreadOp() {
    map<int> m1 = {q: 1, w: 2};
    function () returns map<int> func = () => m1;

    map<boolean|int> m2 = {...func()};

    assertEquality(2, m2.length());
    assertEquality(1, m2["q"]);
    assertEquality(2, m2["w"]);
}

public function testRecordRefAsSpreadOp() {
    Bar b = {s: "str", i: 1};
    Foo f = {...b, f: 123.4};

    assertEquality(3, f.length());
    assertEquality("str", f["s"]);
    assertEquality(1, f["i"]);
    assertEquality(123.4, f["f"]);
}

type Quux record {
    int j;
    never i?;
};

public function testRecordRefWithNeverType() {
    Quux b = {j: 2, "k": 3};
    map<anydata> m = {i: 0, ...b};

    assertEquality(3, m.length());
    assertEquality(0, m["i"]);
}

public function testRecordValueViaFuncAsSpreadOp() {
    var fn = function () returns Bar => {s: "str", i: 1};
    Foo f = {...fn(), f: 123.4};

    assertEquality(3, f.length());
    assertEquality("str", f["s"]);
    assertEquality(1, f["i"]);
    assertEquality(123.4, f["f"]);
}

const map<float> constFloatMap = {z: 1.0, y: 2.0};

const map<float> constFloatMap2 = {...constFloatMap};

public function testSpreadOpInConstMap() {
    assertEquality(2, constFloatMap2.length());
    assertEquality(2.0, constFloatMap2["y"]);
    assertEquality(1.0, constFloatMap2["z"]);
}

map<int> globalIntMap = {a: 1, b: 2};

function getStringMap() returns map<string> {
    return {
        l: "el",
        m: "em",
        n: "en"
    };
}

map<int> globalMapInt = {...globalIntMap};
map<float> globalMapFloat = {...constFloatMap2};
map<string> globalMapString = {...getStringMap()};

function testSpreadOpInGlobalMap() {
    assertEquality(2, globalMapInt.length());
    assertEquality(1, globalMapInt["a"]);
    assertEquality(2, globalMapInt["b"]);

    assertEquality(2, globalMapFloat.length());
    assertEquality(1.0, globalMapFloat["z"]);
    assertEquality(2.0, globalMapFloat["y"]);

    assertEquality(3, globalMapString.length());
    assertEquality("el", globalMapString["l"]);
    assertEquality("em", globalMapString["m"]);
    assertEquality("en", globalMapString["n"]);
}

function testMappingConstrExprAsSpreadExpr() {
    Foo f = {s: "hello", ...{i: 1, f: 2.0}, "oth": true};

    assertEquality(4, f.length());
    assertEquality("hello", f.s);
    assertEquality(1, f.i);
    assertEquality(2.0, f.f);
    assertEquality(true, f["oth"]);
}

type Baz record {
    int i;
    string s;
};

type Qux record {|
    int i;
    boolean...;
|};

function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    panic error(ASSERTION_ERROR_REASON,
                message = "expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}
