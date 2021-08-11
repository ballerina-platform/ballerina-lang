// Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

function func1() returns never {
    panic error("error!");
}

function func2() returns record {| never x; |} {
    panic error("error!");
}

function func3() returns int {
    panic error("error!");
}

function testNeverRuntime1() {
    boolean b = func1 is function () returns record {| never x; |};
    assertEquality(true, b);
}

function testNeverRuntime2() {
    boolean b = func1 is function () returns never;
    assertEquality(true, b);
}

function testNeverRuntime3() {
    boolean b = func1 is function () returns int;
    assertEquality(true, b);
}

function testNeverRuntime4() {
    boolean b = func2 is function () returns never;
    assertEquality(true, b);
}

function testNeverRuntime5() {
    boolean b = func2 is function () returns [never];
    assertEquality(true, b);
}

function testNeverRuntime6() {
    boolean b = func2 is function () returns string;
    assertEquality(true, b);
}

function testNeverRuntime7() {
    boolean b = func3 is function () returns never;
    assertEquality(false, b);
}

function testNeverRuntime8() {
    boolean b = func3 is function () returns record {| never x; |};
    assertEquality(false, b);
}

function testNeverRuntime9() {
    map<never> x = {};
    boolean b = x is map<string>;
    assertEquality(true, b);
}

function testNeverRuntime10() {
    int x = 100;
    boolean b = x is never;
    assertEquality(false, b);
}

type Record record {|
    int i;
    never[] j;
|};

type Record2 record {|
    int i;
    never[] j = [];
|};

function testNeverRuntime11() {
    Record x = { i: 1, j: [] };
    boolean b = x is never;
    assertEquality(false, b);
}

function testNeverRuntime12() {
    Record x = {i: 1, j: []};
    boolean b = x is Record2;
    assertEquality(true, b);
}

function testNeverWithAnyAndAnydataRuntime() {
    map<never> a = {};
    never[] b = [];

    anydata m = a;
    assertEquality(true, m is map<any>);
    assertEquality(true, m is any);

    any y1 = a;
    assertEquality(true, y1 is map<anydata>);
    assertEquality(true, y1 is anydata);

    anydata n = b;
    assertEquality(true, n is any[]);
    assertEquality(true, n is any);

    any y2 = b;
    assertEquality(true, y2 is anydata[]);
    assertEquality(true, y2 is anydata);

    boolean c1 = baz1 is function () returns map<anydata>;
    assertEquality(true, c1);

    boolean c2 = baz2 is function () returns anydata[];
    assertEquality(true, c2);

    boolean c3 = baz1 is function () returns map<any>;
    assertEquality(true, c3);

    boolean c4 = baz2 is function () returns any[];
    assertEquality(true, c4);
}

function baz1() returns map<never> {
    return {};
}

function baz2() returns never[] {
    return [];
}

type AssertionError distinct error;

const ASSERTION_ERROR_REASON = "AssertionError";

function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    string expectedValAsString = expected is error ? expected.toString() : expected.toString();
    string actualValAsString = actual is error ? actual.toString() : actual.toString();
    panic error AssertionError(ASSERTION_ERROR_REASON,
            message = "expected '" + expectedValAsString + "', found '" + actualValAsString + "'");
}
