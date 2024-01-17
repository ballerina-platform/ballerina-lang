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
    int c = 10;
    int d?;
|};

type Bar record {|
    int a;
    int b;
    int c = 10;
    int d;
|};

type Baz record {|
    int c;
|};

type Val record{|
|};

function getSum(int a, int b, int c, int d = 1) returns int {
    return a + b + c + d;
}

function getAvg(int a, int b, int c, int d) returns int {
    return (a + b + c + d)/4;
}

function getTotal(int a, int b, int c, int... m) returns int {
    return a + b + c;
}

function getRestParam(int a, int b, int c, int... m) returns int[] {
    int[] l = <int[]> m;
    return l;
}

function testFunctionWithMappingTypeRestArg() {
    Foo f = {};
    Foo f1 = {c:20, d:15};
    Foo f2 = {c:20};
    Foo f3 = {d:20};
    assertEquality(35, getSum(10, 15, ...f));
    assertEquality(60, getSum(10, 15, ...f1));
    assertEquality(45, getSum(10, 15, ...f2));
    assertEquality(55, getSum(10, 15, ...f3));

    Bar b = {a: 10, b: 10, d: 10};
    Bar b1 = {a:10, b: 10, c:50, d: 10};
    assertEquality(10, getAvg(...b));
    assertEquality(20, getAvg(...b1));

    Baz z = {c:10};
    assertEquality(30, getTotal(10, 10, ...z));

    record {| int a; int b; int c; |} x = {a: 3, b: 5, c: 10};
    assertEquality(0, getRestParam(...x).length());
}

const ASSERTION_ERROR_REASON = "AssertionError";


function assertEquality(any expected, any actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    panic error(ASSERTION_ERROR_REASON,
                message = "expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}

