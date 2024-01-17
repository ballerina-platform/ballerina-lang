// Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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


int gVar1 = 10;

type F function(int a, int b, int c) returns int;

type D function(int a, int b = 12, int c = 13) returns int;

type U int|function(int a = 11, int b = 12, int c = 13) returns int;

type I readonly & function(int a = 11, int b = 12, int c = 13) returns int;

type P function(function(int, int b = 10, int c = 15) returns int i, int d = 12, int e = 13) returns int;

type L function(int k, int d = 12, int e = 13) returns function(int a, int b = 10, int c = 15) returns int;

type K record {|
    function (int a, int b = 10, int c = 14) returns int d;
|};

int gVar2 = 20;

type M record {|
    function (int a, int b = 10, int c = 14) returns int d;
    function (string, string lName) returns string ...;

|};

type N record {|
    *M;
|};

type O map<function(int a, int b, int c) returns int>;

type Q function(int a = gVar1, int b = gVar2, int c = gVar3) returns int;

type R function(int a = gVar1, int b = gVar2 + gVar1, int c = gVar3) returns int;

type S function(int a = gVar1, int b = gVar2 + gVar1 + a, int c = gVar3 + a + b) returns int;

function testOnlyWithRequiredParams() returns F {
    F func = bar;
    return func;
}

function testWithDefaultParams() returns int {
    D func = bar;
    return func(10);
}

function testWithDefaultParams2() returns int {
    D func = bar;
    return func(10, 20);
}

function testWithDefaultParams3() {
    Q func = bar;
    R func2 = bar;
    S func3 = bar;

    assertEquality(60, func());
    assertEquality(70, func2());
    assertEquality(130, func3());

    assertEquality(70, func(20));
    assertEquality(70, func2(20,20));
    assertEquality(110, func3(20, 20));
    assertEquality(60, func3(20, 20, 20));
}

function testUnionTypeWithFunctionType() returns int {
    U func = bar;
    if !(func is int) {
        return func();
    } 
    return 0;
}

function testIntersectionTypeWithFunction() returns int {
    I func = bar;
    return func();
}

function testReturnWithFunctionType() returns int {
    P func = foo;
    return func(bar);
}

function testRecordTypeWithFuncTypeRestField() returns string {
    M func = {d:bar, "name": mat};
    var val = func["name"];
    if !(val is ()) {
        return val("Chiran", "Sachintha");
    }
    return " ";
}

function testTypeDefWithMap() returns int {
    K func = {d:bar};
    O mapValue = func;
    ()|function (int,int b = 23,int c = 7) returns (int) val = mapValue["d"];
    if !(val is ()) {
        return val(10);
    }
    return 0; 
}

function bar(int a, int b, int c) returns int {
    return a + b + c;
}

function mat(string a, string b) returns string {
    return a + " " + b;
}

function foo(function(int a, int b = 10, int c = 15) returns int i, int d = 100, int e = 200) returns int {
    return i(5) + d + 13;
}

function baz(int i, int d = 100, int e = 200) returns function(int x, int y = 10, int z = 15) returns int {
    return bar;
}

function testFunctionType() {
    var func = testOnlyWithRequiredParams();
    assertEquality(30, func(5, 10, 15));

    int res = testWithDefaultParams();
    int res2 = testWithDefaultParams2();
    assertEquality(35, res);
    assertEquality(43, res2);

    int res3 = testUnionTypeWithFunctionType();
    assertEquality(36, res3);

    int res4 = testIntersectionTypeWithFunction();
    assertEquality(36, res4);

    int res5 = testReturnWithFunctionType();
    assertEquality(55, res5);

    string res8 = testRecordTypeWithFuncTypeRestField();
    assertEquality("Chiran Sachintha", res8);

    int res11 = testTypeDefWithMap();
    assertEquality(40, res11);

    testWithDefaultParams3();
}

int gVar3 = 30;

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
