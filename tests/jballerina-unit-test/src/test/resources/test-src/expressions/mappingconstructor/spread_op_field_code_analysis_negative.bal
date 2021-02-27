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

type Foo record {|
    string s;
    int i;
|};

type Bar record {|
    string s;
    int i;
    float f;
|};

function testDulpicateKeyWithSpreadOpField() {
    Foo f = {s: "str", i: 2};
    Bar b = {i: 1, f: 1.0, ...f, s: "hi"};
    map<string|int|float> m = {s: "hello", ...b, f: 1.0, i: "bye"};
    map<anydata> n = {s: "hello", ...{s: "hi", i: 1}, f: 1.0, i: "bye"};
}

type Alpha record {|
    int i?;
|};

function testDuplicateKeyWithOptionalField() {
    Alpha alpha = {i:2};
    map<int> m = {i:1, ...alpha};
}

type AlphaWithRest record {|
    int...;
|};

function testClosedRecordWithRestField() {
    AlphaWithRest a = {"i": 1, "k": 2};
    map<int> m = {i: 1, ...a};
}

type Beta record {
    int j;
};

function testInclusiveRecordWithSpreadOp1() {
    Beta b = {j: 1, "i": 2};
    map<anydata> m = {i: 0, ...b};
}

function testInclusiveRecordWithSpreadOp2() {
    Beta b = {j: 1, "i": 2};
    map<anydata> m = {...b, i:3};
}

function testMappWithSpreadOp1() {
    map<string> m1 = {x: "aa", y: "bb"};
    map<string> m2 = {...m1, y: "cc"};
}

function testMappWithSpreadOp2() {
    map<string> m1 = {x: "aa", y: "bb"};
    map<string> m2 = {y: "cc", ...m1};
}

type Beta2 record {
    int i;
};

function testMultipleInclRecordsWithSpreadOp1() {
    Beta b1 = {j: 1, "k": 4};
    Beta2 b2 = {i: 2, "k": 4};
    map<any|error> m = {...b1, ...b2};
}

type Beta3 record {
    int i;
};

function testMultipleInclRecordsWithSpreadOp2() {
    Beta2 b2 = {i: 2};
    Beta3 b3 = {i: 3};
    map<any|error> m = {...b2, ...b3};
}

function testMultipleMapSpreadField() {
    map<int> m1 = {i: 1};
    map<int> m2 = {i: 2};
    map<int> mm = {...m1, ...m2};
}

function testMultipleMapIncRecordSpreadField() {
    Beta2 beta = {i: 2};
    map<int> m1 = {i: 2};
    map<anydata> mm = {...m1, ...beta};
}
