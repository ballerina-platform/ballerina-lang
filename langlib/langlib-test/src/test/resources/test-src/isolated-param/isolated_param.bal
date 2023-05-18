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

import ballerina/lang.array;
import ballerina/lang.'map;

isolated function testIsolatedFunctionArgForIsolatedParam() {
    int[] x = [1, 2, 3];
    array:forEach(x, arrForEachFunc);
    array:forEach(func = arrForEachFunc, arr = x);
    array:forEach(func = val => assertEquality(true, val is OneTwoThree), arr = x);
    x.forEach(func = arrForEachFunc);
    x.forEach(isolated function (int i) {
        assertEquality(true, i is OneTwoThree);
    });
    x.forEach(val => assertEquality(true, val is OneTwoThree));

    map<boolean> y = {a: true, b: true, c: false};
    map<boolean> v1 = y.filter(mapFilterFunc);
    assertEquality(<map<boolean>> {c: false}, v1);
    map<boolean> v2 = y.filter(val => !val);
    assertEquality(<map<boolean>> {c: false}, v2);

    y = {a: false, b: false, c: false, d: true};
    map<boolean> v3 = y.filter(func = isolated function (boolean val) returns boolean => !val);
    assertEquality(<map<boolean>> {a: false, b: false, c: false}, v3);
    //map<boolean> v4 = y.filter(func = val => !val); // https://github.com/ballerina-platform/ballerina-lang/issues/26111
    //assertEquality(<map<boolean>> {a: false, b: false, c: false}, v3);

    y = {a: true, b: true, d: true};
    map<boolean> v5 = 'map:filter(y, mapFilterFunc);
    assertEquality(<map<boolean>> {}, v5);
    map<boolean> v6 = 'map:filter(y, val => !val);
    assertEquality(<map<boolean>> {}, v6);

    map<int> a = {a: 100, b: 200};
    int v7 = 'map:reduce(a, isolated function (int i, int j) returns int {return i + j;}, ...[1]);
    assertEquality(301, v7);
}

type OneTwoThree 1|2|3;

isolated function arrForEachFunc(int i) {
    assertEquality(true, i is OneTwoThree);
}

isolated function mapFilterFunc(boolean val) returns boolean => !val;

type FuncTuple [isolated function (string)];

isolated int i = 0;

function testIsolatedParamWithTypeRefTypedRestArg() {
    FuncTuple f = [isolated function (string s) { lock { i += s.length(); } }];

    string[] arr = ["Hello", "Ballerina"];
    arr.forEach(...f);
    lock {
        assertEquality(14, i);
    }
}

isolated function testArgAnalysisWithFixedLengthArrayRestArg() {
    int[3] x = [21, 2, 3];
    int i = int:min(...x);
    assertEquality(2, i);
}

isolated function testIsolatedFuncArgInFixedLengthArrayRestArg() {
    int[] marks = [75, 80, 45, 90];
    (isolated function (int x) returns boolean)[1] fns = [x => x > 79];
    int[] filtered = marks.filter(...fns);
    assertEquality(<int[]> [80, 90], filtered);
}

isolated function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    string expectedValAsString = expected is error ? expected.toString() : expected.toString();
    string actualValAsString = actual is error ? actual.toString() : actual.toString();
    panic error(string `expected '${expectedValAsString}', found '${actualValAsString}'`);
}
