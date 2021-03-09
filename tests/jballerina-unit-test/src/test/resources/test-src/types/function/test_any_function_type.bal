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

function foo(function x) returns int {
   int j = 0;
   if x is function (int) returns int {
      j = x(1);
   }
   return j;
}

//------------ Testing a function with 'function' return type ---------

function bar(int b) returns int {
    return b;
}

isolated function bar2(int b) returns int {
    return b;
}

function baz = bar;

function baz2 = bar2;

function functionWithFunctionReturnType1() returns function {
    function func = bar;
    return func;
}

function functionWithFunctionReturnType2(function bar) returns function {
    function func = bar;
    return func;
}

function functionWithFunctionReturnType3(function (int) returns int bar) returns function {
    function func = <function> bar;
    return func;
}

function functionWithFunctionReturnType4(function foo) returns function {
    function func = foo;
    return func;
}

function functionWithFunctionReturnType5(isolated function bar2) returns function {
    function func = bar2;
    return func;
}

function functionWithFunctionReturnType6() returns function {
    isolated function func = bar2;
    return func;
}

function functionWithFunctionReturnType7() {
    function func = <function> bar;
    function () returns int f = <function () returns int> func;
}

function functionWithFunctionReturnType8() {
    function func = <function> bar;
    function (int) returns int f = <function (int) returns int> func;
}

function functionWithFunctionReturnType9() returns function? {
    function? func = bar;
    return func;
}

function testFunctionWithFunctionReturnType() {
    error? e1 = trap functionWithFunctionReturnType7();
    error? e2 = trap functionWithFunctionReturnType8();
    function f = <function> functionWithFunctionReturnType9();
    assertEquality(true, functionWithFunctionReturnType1() is function (int) returns int);
    assertEquality(true, functionWithFunctionReturnType2(bar) is function (int) returns int);
    assertEquality(true, functionWithFunctionReturnType3(bar) is function (int) returns int);
    assertEquality(true, functionWithFunctionReturnType4(baz) is function (int) returns int);
    assertEquality(true, functionWithFunctionReturnType5(bar2) is function (int) returns int);
    assertEquality(true, functionWithFunctionReturnType6() is function (int) returns int);
    assertEquality(true, functionWithFunctionReturnType6() is function (int) returns int|string);
    assertEquality(true, functionWithFunctionReturnType9() is function);
    assertEquality(true, e1 is error);
    assertEquality(false, e2 is error);
}

//------------ Testing record type with 'function' field ---------


type Person record {
    int a;
    function b;
    function c?;
};

function testRecordWithFunctionTypeField() {
    Person p = {
        a: 10,
        b: bar
    };
    assertEquality(true, p.b is function (int) returns int|string);
    assertEquality(false, p["c"] is function (int) returns int|string);
    assertEquality(true, p["c"] is ());
}

// --------------Test 'function' with 'map' type ----------------------

function testFunctionAsMappingTypeParam() {
    map<function> mp = {
        func1 : bar,
        func2 : bar2
    };
    assertEquality(true, mp["func1"] is function (int) returns int|string);
    assertEquality(false, mp["func1"] is isolated function (int) returns int|string);
    assertEquality(true, mp["func2"] is function (int) returns int|string);
    assertEquality(true, mp["func2"] is isolated function (int) returns int|string);
}

//------------ Testing object type with 'function' field ---------

class Student {
    function func;

    function init(function func) {
        self.func = func;
    }

    function getFunction() returns function {
        return self.func;
    }
}

function objectWithFunctionTypeField() returns boolean {
    Student p1 = new(<function> bar);
    if (p1.func is function (int) returns int) {
        return true;
    }
    return false;
}

function objectWithFunctionTypeField2() returns boolean {
    Student p1 = new(<function> bar);
    if (p1.getFunction() is function (int) returns int) {
        return true;
    }
    return false;
}

function testObjectWithFunctionTypeField() {
    assertEquality(true, objectWithFunctionTypeField());
    assertEquality(true, objectWithFunctionTypeField2());
}
//---------------Test 'function' types with 'union-type' descriptors ------------

function testFunctionWithUnionType() {
    string|function j = "sample";
    function|string m = "sample";
    string h = <string> j;
    string n = <string> m;
    assertEquality("sample", h);
    assertEquality("sample", n);

    string|function k = bar;
    function l = <function> k;
    assertEquality(true, l is function (int) returns int|string);
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
