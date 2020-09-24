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
    'array:forEach(x, arrForEachFunc);
    'array:forEach(func = arrForEachFunc, arr = x);
    x.forEach(func = arrForEachFunc);
    x.forEach(isolated function (int i) {
        assertEquality(true, i is OneTwoThree);
    });

    map<boolean> y = {a: true, b: true, c: false};
    y = y.filter(mapFilterFunc);
    assertEquality(<map<boolean>> {c: false}, y);

    y = {a: false, b: false, c: false, d: true};
    y = y.filter(func = isolated function (boolean val) returns boolean => !val);
    assertEquality(<map<boolean>> {a: false, b: false, c: false}, y);

    y = {a: true, b: true, d: true};
    y = 'map:filter(y, mapFilterFunc);
    assertEquality(<map<boolean>> {}, y);
}

type OneTwoThree 1|2|3;

isolated function arrForEachFunc(int i) {
    assertEquality(true, i is OneTwoThree);
}

isolated function mapFilterFunc(boolean val) returns boolean => !val;

isolated function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    panic error(string `expected '${expected.toString()}', found '${actual.toString()}'`);
}
