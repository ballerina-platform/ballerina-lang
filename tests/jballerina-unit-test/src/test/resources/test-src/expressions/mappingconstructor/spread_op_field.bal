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
    map<boolean|float> m2 = {a: true, ...m1};

    assertEquality(3, m2.length());
    assertEquality(true, m2["a"]);
    assertEquality(1.0, m2["q"]);
    assertEquality(2.0, m2["w"]);
}

public function testMapValueViaFuncAsSpreadOp() {
    map<int> m1 = {q: 1, w: 2};
    function () returns map<int> func = () => m1;

    map<boolean|int> m2 = {a: true, ...func()};

    assertEquality(3, m2.length());
    assertEquality(true, m2["a"]);
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

public function testRecordValueViaFuncAsSpreadOp() {
    var fn = function () returns Bar => {s: "str", i: 1};
    Foo f = {...fn(), f: 123.4};

    assertEquality(3, f.length());
    assertEquality("str", f["s"]);
    assertEquality(1, f["i"]);
    assertEquality(123.4, f["f"]);
}

const map<float> constFloatMap = {z: 1.0, y: 2.0};

const map<float> constFloatMap2 = {x: 3.0, ...constFloatMap};

public function testSpreadOpInConstMap() {
    assertEquality(3, constFloatMap2.length());
    assertEquality(3.0, constFloatMap2["x"]);
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

map<int|string|float> globalISFMap = {
    f: 4,
    ...globalIntMap,
    ...constFloatMap2,
    g: 12.0,
    ...getStringMap()
};

public function testSpreadOpInGlobalMap() {
    assertEquality(10, globalISFMap.length());
    assertEquality(4, globalISFMap["f"]);
    assertEquality(1, globalISFMap["a"]);
    assertEquality(2, globalISFMap["b"]);
    assertEquality(3.0, globalISFMap["x"]);
    assertEquality(1.0, globalISFMap["z"]);
    assertEquality(2.0, globalISFMap["y"]);
    assertEquality(12.0, globalISFMap["g"]);
    assertEquality("el", globalISFMap["l"]);
    assertEquality("em", globalISFMap["m"]);
    assertEquality("en", globalISFMap["n"]);
}

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
