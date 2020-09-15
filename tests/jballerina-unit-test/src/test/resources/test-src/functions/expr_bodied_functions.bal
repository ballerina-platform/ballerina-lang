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

import ballerina/lang.'int;

// Test literals
function testReturningLiterals() {
    var intRet = function () returns int => 100;
    var floatRet = function () returns float => 12.34;
    var strRet = function () returns string => "foo";
    var boolRet = function () returns boolean => true;
    var nilRet = function () returns () => ();

    assert(100, intRet());
    assert(12.34, floatRet());
    assert("foo", strRet());
    assert(true, boolRet());
    assert((), nilRet());
}

// Test returning lists
function returnsAnArray() returns string[] => ["Hello", "World"];

function returnsATuple() returns [int, float, string] => [10, 12.34, "foo"];

function testReturningLists() {
    string[] strArr = returnsAnArray();
    assert(["Hello", "World"], strArr);

    [int, float, string] tup = returnsATuple();
    assert([10, 12.34, "foo"], tup);
}

// Test returning XML
function testReturningXML() {
    var fn = function () returns xml => xml `<fname>John</fname>`;
    assert(xml `<fname>John</fname>`, fn());
}

// Test returning string templates
function testReturningStringTemplate() {
    var fn = function (string name) returns string => string `Hello ${name}`;
    assert("Hello Ballerina", fn("Ballerina"));
}

// Test returning service constructor exprs.
function testReturningServiceConstructors() {
    var fn = function () returns service => service {
        resource function greet() {
        }
    };
    var s = fn();
}

// Test checkpanic expr as expr body
function panicsACheck() returns int => checkpanic 'int:fromString("invalid");

function testCheckPanic() {
    int res = panicsACheck();
}

// Test simple binary expr
function sum(int a, int b) returns int => a + b;

function testBinaryExprs() {
    int s = sum(10, 20);
    assert(30, s);
}

// Test nil returning functions
function returnsNil1() => ();

function returnsNil2() returns () => ();

function testNilReturningFunctions() {
    () v1 = returnsNil1();
    () v2 = returnsNil2();

    assert((), v1);
    assert((), v2);
}

// Test closures
function testClosures(int a) {
    int b = 20;
    var sum = function (int i, int j) returns int => i + j + a + b;
    int result = sum(50, 100);
    assert(a + b + 150, result);
}

// Test records as an expr
type Person record {
    string fname;
    string lname;
    int age;
};

type Employee record {
    string name;
};

function toEmployee(Person p) returns Employee => {
    name: p.fname + " " + p.lname
};

function testRecordAsAnExpr() {
    Employee e = toEmployee({fname: "John", lname: "Doe", age: 25});
    assert("{\"name\":\"John Doe\"}", e.toString());
}

// Test returning param ref
function getSameRef(Person p) returns Person => p;

function testSameVarRefAsExpr() {
    Person p1 = {fname: "John", lname: "Doe", age: 25};
    Person p2 = getSameRef(p1);
}

// Test function invocation
function foo() returns string {
    return "foo";
}

var fnBar = function () returns string {
    return "bar";
};

function invokeFn1() returns string => foo();

function invokeFn2() returns string => fnBar();

function testFunctionInvocation() {
    string s1 = invokeFn1();
    string s2 = invokeFn2();

    assert("foo", s1);
    assert("bar", s2);
}

function testFunctionInvocationAsLambdas() {
    var fn1 = function () returns string => foo();
    var fn2 = function () returns string => fnBar();

    string s1 = fn1();
    string s2 = fn2();

    assert("foo", s1);
    assert("bar", s2);
}

// Test expr bodies in object methods
class PersonObj {
    string name;
    int age;

    function init(string name, int age) {
        self.name = name;
        self.age = age;
    }

    function getName() returns string => self.name;

    function getAge() returns int => self.age;

    function toString() returns string => "[name: " + self.name + ", age: " + self.age.toString() + "]";
}

function testExprsBodiesInMethods() {
    PersonObj p = new("John Doe", 25);
    assert("John Doe", p.getName());
    assert(25, p.getAge());
}

// Test object init method as an expr bodied function
function getError(boolean returnErr) returns error? {
    if (returnErr) {
        error e = error("Dummy Error");
        return e;
    } else {
        return ();
    }
}

class PersonObj2 {
    string name = "Anonymous";

    function init(boolean retErr) returns error? =>  check getError(retErr);
}

function testObjectInitBodyAsAnExpr() {
    PersonObj2|error p1 = new(true);
    PersonObj2|error p2 = new(false);

    assertTrue(p1 is error, "p1 is error");
    assertTrue(p2 is PersonObj2, "p2 is PersonObj2");
}

// Test objects expr body
function returnObj() returns PersonObj => new("John Doe", 25);

function testObjectsAsExprBody() {
    PersonObj p = returnObj();
    assert("[name: John Doe, age: 25]", p.toString());
}

// Test anonymous function exprs as bodies
function returnLambda() returns function (string) returns string => function (string s) returns string { return s + " from Lambda"; };

function returnArrowFunc() returns function (string) returns string => (s) => s + " from Arrow";

function testAnonFuncsAsExprBody() {
    var lambda = returnLambda();
    string v = lambda("Echo");
    assert("Echo from Lambda", v);

    var arrow = returnArrowFunc();
    v = arrow("Echo");
    assert("Echo from Arrow", v);
}

// Taint checking test
function sensitiveFunc(@untainted string param) returns string {
    return "untainted value: " + param;
}

function returnsTainted() returns @tainted string => "Hello World!";

// Test directly passing a tainted value
function testTaintCheckingInExprBodies() {
    string val = sensitiveFunc(<@untainted>returnsTainted());
    assert("untainted value: Hello World!", val);
}

function letAsBody() returns float => let float a = 12.34 in let int b = 100 in a * b;

function testLetExprAsExprBody() {
    float f = letAsBody();
    assert(1234.0, f);
}


// Util functions

function assert(anydata expected, anydata actual) {
    if (expected != actual) {
        typedesc<anydata> expT = typeof expected;
        typedesc<anydata> actT = typeof actual;
        string reason = "expected [" + expected.toString() + "] of type [" + expT.toString()
                            + "], but found [" + actual.toString() + "] of type [" + actT.toString() + "]";
        error e = error(reason);
        panic e;
    }
}

function assertSameRef(anydata expected, anydata actual) {
    if (expected !== actual) {
        typedesc<anydata> expT = typeof expected;
        typedesc<anydata> actT = typeof actual;
        string reason = "expected [" + expected.toString() + "] of type [" + expT.toString()
                            + "], but found [" + actual.toString() + "] of type [" + actT.toString() + "]";
        error e = error(reason);
        panic e;
    }
}

function assertTrue(boolean result, string condition) {
    if (!result) {
        string reason = "condition [" + condition + "] evaluated to 'false'";
        error e = error(reason);
        panic e;
    }
}
