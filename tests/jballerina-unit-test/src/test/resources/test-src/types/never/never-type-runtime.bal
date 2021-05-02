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

function testNeverRuntime() {
    error? e1 = trap test1();
    assertEquality(true, e1 is error);
    assertEquality("Error value is:true", (<error>e1).message());

    error? e2 = trap test2();
    assertEquality(true, e2 is error);
    assertEquality("Error value is:true", (<error>e2).message());

    error? e3 = trap test3();
    assertEquality(true, e3 is error);
    assertEquality("Error value is:true", (<error>e3).message());

    error? e4 = trap test4();
    assertEquality(true, e4 is error);
    assertEquality("Error value is:true", (<error>e4).message());

    error? e5 = trap test5();
    assertEquality(true, e5 is error);
    assertEquality("Error value is:true", (<error>e5).message());

    error? e6 = trap test6();
    assertEquality(true, e6 is error);
    assertEquality("Error value is:true", (<error>e6).message());
}

function func1() returns never {
    panic error("error!");
}

function func2() returns record {| never x; |} {
    panic error("error!");
}

function test1() {
    boolean b = <any> func1 is function () returns record {| never x; |};
    panic error("Error value is:" + string `${b}`);
}

function test2() {
    boolean b = <any> func1 is function () returns never;
    panic error("Error value is:" + string `${b}`);
}

function test3() {
    boolean b = <any> func1 is function () returns int;
    panic error("Error value is:" + string `${b}`);
}

function test4() {
    boolean b = <any> func2 is function () returns never;
    panic error("Error value is:" + string `${b}`);
}

function test5() {
    boolean b = <any> func2 is function () returns [never];
    panic error("Error value is:" + string `${b}`);
}

function test6() {
    boolean b = <any> func2 is function () returns string;
    panic error("Error value is:" + string `${b}`);
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
