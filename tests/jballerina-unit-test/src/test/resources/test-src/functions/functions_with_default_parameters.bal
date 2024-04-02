// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import ballerina/jballerina.java;
import ballerina/test;

int GLB = 0;

function getInt() returns int {
    return 100;
}

function getFloat() returns float {
    return 1.1;
}

function getString() returns string {
    return "default";
}

function getBoolean() returns boolean {
    return true;
}

function getRecord() returns FooRecord {
    return {a: "default2", b: 150, c: true, d: 33.3};
}

type FooRecord record {
    string a;
    int b;
    boolean c;
    float d;
};

class FooObject {
    string a;
    int b;

    function init(string a, int b) {
        self.a = a;
        self.b = b;
    }

    function getValues() returns [string, int] {
        return [self.a, self.b];
    }
}

function getIntOrError(int a) returns int|error {
    if (a == 0) {
        error e = error("Generated Error");
        return e;
    } else {
        return 100;
    }
}

function getStringOrNil() returns string? {
    if (GLB == 0) {
        return "string";
    }
}

function getIntOrPanic() returns int {
    if (GLB == 0) {
        return GLB;
    } else {
        panic error("Panic!");
    }
}

// Test 1
function testFunctionCallAsDefaultExpr() returns [[int, string, float, boolean], [int, string, float, boolean], [int, string, float, boolean]] {
    [int, string, float, boolean] x = foo();
    [int, string, float, boolean] y = foo(a = 200, b = "given", c = 4.4, d = false);
    [int, string, float, boolean] z = foo(a = 500, c = 5.5);
    return [x, y, z];
}

function foo(int a = getInt(), string b = getString(), float c = getFloat(), boolean d = getBoolean()) returns [int, string, float, boolean] {
    return [a, b, c, d];
}

// Test 2
function testRecordAsDefaultExpr() returns [FooRecord, FooRecord, FooRecord, FooRecord] {
    FooRecord fRec1 = {a: "f1", b: 100, c: true, d: 22.2};
    FooRecord fRec2 = {a: "f2", b: 200, c: false, d: 44.4};
    [FooRecord, FooRecord] x = bar(fRec2 = fRec2);
    [FooRecord, FooRecord] y = bar(fRec1 = fRec1);
    return [x[0], x[1], y[0], y[1]];
}

function bar(FooRecord fRec1 = {a: "default", b: 50, c: false, d: 11.1}, FooRecord fRec2 = getRecord()) returns [FooRecord, FooRecord] {
    return [fRec1, fRec2];
}

// Test 3
function testDefaultExprInFunctionPointers() returns [int, string, float, boolean] {
    function (int, string, float, boolean) returns [int, string, float, boolean] fp1 = foo;
    [int, string, float, boolean] y = fp1(200, "given", 4.4, false);
    return y;
}

// Test 4
function testCombinedExprAsDefaultValue() returns [[int, string, float], [int, string, float], [int, string, float]] {
    [int, string, float] a = foo2();
    [int, string, float] b = foo2(a = 50);
    [int, string, float] c = foo2(b = "given" + getString(), c = 9.9);

    return [a, b, c];
}

function foo2(int a = getInt() + 5 + getInt(), string b = "def" + getString(), float c = getFloat() + <float>getInt())
returns [int, string, float] {
    return [a, b, c];
}

// Test 5
function testDefaultValueWithError() {
    GLB = 0;
    error e1 = foo3();
    GLB = 1;
    error e2 = foo3();
    error e3 = foo3(a = 2);
    assertEquality(e1.message(), "Generated Error");
    assertEquality(e2.message(), "Not Error 100");
    assertEquality(e3.message(), "Not Error 2");
}

function foo3(int|error a = getIntOrError(GLB)) returns error {
    if (a is error) {
        return a;
    }
    if (a is int) { // always true
        return error("Not Error " + a.toString());
    }
}

// Test 6
function testDefaultValueWithTernary() returns [[float, string], [float, string], [float, string]] {
    GLB = 0;
    [float, string] a = foo4();
    GLB = 1;
    [float, string] b = foo4();

    [float, string] c = foo4(f = 3.3, s = "given");

    return [a, b, c];
}

function foo4(float f = (GLB == 0 ? 1.1 : 2.2), string s = getStringOrNil() ?: "empty") returns [float, string] {
    return [f, s];
}

// Test 7
function testPanicWithinDefaultExpr() returns int {
    GLB = 0;
    int a = foo5();
    GLB = 1;
    int b = foo5();
    return a;
}

function foo5(int a = getIntOrPanic()) returns int {
    return a;
}

// Test 8
function testDefaultObject() returns [[string, int], [string, int]] {
    [string, float, FooObject] a = foo7("fp");
    [string, float, FooObject] b = foo7("fp", o = new ("given", 200));
    return [a[2].getValues(), b[2].getValues()];
}

function foo7(string s, float b = 1.1, FooObject o = new ("default", 100)) returns [string, float, FooObject] {
    return [s, b, o];
}

function testFuncWithDefaultByteValue() {
    test:assertEquals(funcWithDefaultByteValue(), 10);
    test:assertEquals(funcWithDefaultByteValue(15), 15);
}

function testDefaultByteValueInFunctionPointers() {
    function (byte b = 5) returns byte fp = funcWithDefaultByteValue;
    test:assertEquals(fp(15), 15);
    test:assertEquals(fp(funcWithDefaultByteValue()), 10);

    //TODO : Enable this after merge #31589
    //test:assertEquals(fp(), 5);
}

function funcWithDefaultByteValue(byte b = 10) returns byte {
    return b;
}

function testFuncWithAsyncDefaultParamExpression() returns string {
    return funcWithAsyncDefaultParamExpression() + funcWithAsyncDefaultParamExpression("world") + funcWithAsyncDefaultParamExpression("sample", "value");
}

function funcWithAsyncDefaultParamExpression(string a1 = asyncRet(), string a2 = asyncRet()) returns string {
    return a1 + a2;
}

function asyncRet() returns string {
    sleep(50);
    return "hello";
}

function asyncRetWithVal(string a = "sample") returns string {
    sleep(50);
    return a + "async";
}

function testUsingParamValues() returns string {
    return usingParamValues() + usingParamValues("world") + usingParamValues("sample", "value");
}

function usingParamValues(string a1 = asyncRet(), string a2 = asyncRetWithVal(a1)) returns string {
    return a1 + a2;
}

public class Person {

    function funcWithAsyncDefaultParamExpression(string a1 = asyncRet(), string a2 = asyncRet()) returns string {
        return a1 + a2;
    }

    function usingParamValues(string a1 = asyncRet(), string a2 = asyncRetWithVal(a1)) returns string {
        return a1 + a2;
    }
}

client class MyClient {
    resource function get [string... path](map<string|string[]>? headers = ()) returns string {
        return path[0];
    }
}

final MyClient clientEP = new ();

function testResourceFunctionDefaultParam() returns string|error {
    return check clientEP->/foo/bar();
}

function testAttachedAsyncDefaultParam() returns string {
    Person p = new;
    return p.funcWithAsyncDefaultParamExpression() + p.funcWithAsyncDefaultParamExpression("world") + p.funcWithAsyncDefaultParamExpression("sample", "value");
}

function testUsingParamValuesInAttachedFunc() returns string {
    Person p = new;
    return p.usingParamValues() + p.usingParamValues("world") + p.usingParamValues("sample", "value");
}

public function sleep(int millis) = @java:Method {
    'class: "org.ballerinalang.test.utils.interop.Utils"
} external;

function testFuncWithVariableNameFieldInMappingConstructorForDefaultValue() {
    int a = 10;
    function (record {|int a;|} x = {a}) returns int y = value1;
    assertEquality(y(), 10);
}

function testFuncWithComputedNameFieldInMappingConstructorForDefaultValue() {
    int a = 100;
    string b = "a";
    function (record {|int a;|} x = {a: 10, [b] : a}) returns int y = value1;
    assertEquality(y(), 100);
}

function value1(record {|int a;|} x) returns int {
    return x.a;
}

function testFuncWithSpreadFieldInMappingConstructorForDefaultValue() {
    int a = 10;
    int b = 20;

    record {|int a; int b;|} r = {a: a, b: b};
    function (record {|int a; int b;|} x = {...r}) returns int y = value2;
    assertEquality(y(), 30);
}

function value2(record {|int a; int b;|} x) returns int {
    return x.a + x.b;
}

type F1 function() returns int;

type F2 function() returns function() returns int;

function baz(int a, F1 b = function() returns int { return a + 1; }) returns int {
    return a + b();
}

function baz1(int a, F2 b = function() returns function() returns int {
            return function() returns int {
                return a;
            };
        }) returns int {
    F1 f = b();
    return a + f();
}

function baz2(int a, function() returns int b = function() returns int {
            function (int x = a) returns int f = function(int p) returns int {
                return p + 1;
            };
            return f();
        }) returns int {
    return a + b();
}

function baz3(int a, int c, function() returns int b = function() returns int {
            function (int x = a + c) returns int f = function(int p) returns int {
                return p + 1;
            };
            return f();
        }) returns int {
    return a + b() + c;
}

function baz4(int a, function() returns int b = function() returns int {
            function(int x = a) returns int f = function(int p) returns int => p + 1 + a;
            return f();
        }) returns int {
    return a + b();
}

function testUsingParamInAnonFuncDefaultValueOfSubsequentParam() {
    assertEquality(baz(10), 21);
    assertEquality(baz1(100), 200);
    assertEquality(baz2(10), 21);
    assertEquality(baz3(10, 20), 61);
    assertEquality(baz4(10), 31);
}

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
    panic error(ASSERTION_ERROR_REASON, message =
    "expected '" + expectedValAsString + "', found '" + actualValAsString + "'");
}
