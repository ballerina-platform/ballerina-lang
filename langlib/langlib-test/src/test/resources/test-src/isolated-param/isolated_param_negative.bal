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

isolated function testInvalidArgForIsolatedParam() {
    int[] x = [1, 2, 3];

    'array:forEach(x, arrForEachFunc);
    'array:forEach(func = arrForEachFunc, arr = x);
    x.forEach(func = arrForEachFunc);
    x.forEach(arrForEachFunc);
    x.forEach(val => arrForEachFunc(val));

    map<boolean> y = {a: true, b: true, c: false};
    y = y.filter(mapFilterFunc);
    y = y.filter(func = mapFilterFunc);
    y = 'map:filter(y, mapFilterFunc);
    y = 'map:filter(y, val => !val && glob);

    map<int> a = {a: 100, b: 200};
    int _ = 'map:reduce(a, function (int i, int j) returns int {return i + j + globInt;}, ...[1]);
}

boolean glob = true;
int globInt = 10;

function arrForEachFunc(int i) {

}

function mapFilterFunc(boolean val) returns boolean => !val;

type OneTwoThree 1|2|3;

type Foo record {
    int[] x = array:filter(<int[]> [1, 2, 3, 4, 5, 1, 2], val => val is OneTwoThree && glob);
};

type Greetings "hello"|"hi";

class Bar {
    map<string> m = 'map:filter(<map<string>> {a: "hello", b: "world", c: "hi"}, strMapFilterFunc);
}

function strMapFilterFunc(string val) returns boolean => val is Greetings;

isolated function testInvalidNonIsolatedFuncArgInFixedLengthArrayRestArg() {
    int[] marks = [75, 80, 45, 90];
    (function (int x) returns boolean)[1] fns = [x => x > 79];
    _ = marks.filter(...fns);
}
