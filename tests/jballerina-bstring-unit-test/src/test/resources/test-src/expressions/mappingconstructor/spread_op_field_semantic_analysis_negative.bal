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

///////////////////////// Record Tests /////////////////////////

type Foo record {|
    string s;
    int i;
|};

type Bar record {|
    int s;
    float i;
|};

string s = "hello";

function testSpreadOpFieldOfIncorrectType() {
    boolean b = true;
    Foo f = {...s, s, i: 1, ...b};
}

function testFieldsOfIncorrectTypeViaSpreadOp() {
    Bar b = {s: 1, i: 1.0};
    record {
        boolean i = true;
    } r = {};
    Foo f = {...b, i: 1, ...r};
}

function testUnspecifiedClosedRecordFieldViaSpreadOp() {
    record {
        int i;
        boolean x;
    } r = {i: 1, x: true};
    Foo f = {s: "hello", ...r};
}

function testUndeclaredSpreadOpField() {
    Foo f = {s, i: 2, ...b};
}

///////////////////////// Map Tests /////////////////////////

function testMapSpreadOpFieldOfIncorrectType() {
    int iv = 1;
    map<int> f = {i: 1, ...iv};
}

function testMapFieldsOfIncorrectTypeViaSpreadOp() {
    Bar b = {s: 1, i: 1.0};
    record {
        boolean i = true;
    } r = {};
    map<string> f = {...b, i: "j", ...r};
}

function testUndeclaredMapSpreadOpField() {
    map<string|int> m = {s, i: 2, ...b, ...getFoo()};
}

function testJsonSpreadOpFieldOfIncorrectType() {
    map<anydata> m = {a: 1, b: "hi"};
    record {|
        string s;
        any...;
    |} r = {s: "str"};

    json j = {...r, c: 2, ...m};
    map<json> mj = {a: 1, ...r, ...m};
}
