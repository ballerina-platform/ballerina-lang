// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

type F1 function (int) returns function (int) returns int;
F1 f1 = a => b => a + b;

type F2 function (int) returns function (int) returns function (int) returns int;
F2 f2 = a => b => c => a + b + c;

function testArrowExprWithOneParam() returns int {
    function (int) returns int lambda = param1 => param1*2;
    return lambda(12);
}

function testArrowExprWithTwoParams() returns string {
    function (int, string) returns string lambda = (x, y) => x.toString() + y;
    return lambda(12, "John");
}

function testReturnArrowExpr() returns string {
    var lambda = returnsArrowExpr();
    int intVar = 10;
    string stringVar = "Adam";
    return lambda(intVar, stringVar);
}

function returnsArrowExpr() returns function (int x, string y) returns string {
    return (x, y) => x.toString() + y;
}

function testArrowExprReturnTuple() returns [string, int] {
    function (int, string) returns [string, int] lambda = (x, y) => [x.toString() + y, x];
    return lambda(12, "John");
}

function testArrowExprReturnUnion() returns (string|int) {
    function (int, string) returns (string|int) lambda = (x, y) => x.toString() + y;
    return lambda(12, "John");
}

function testBooleanParamType() returns boolean {
    function (boolean) returns boolean invertBoolean = param1 => !param1;
    return invertBoolean(false);
}

function testClosure() returns int {
    int closureVar = 10;
    function (int, string) returns int lambda = (param1, param2) => closureVar + param1;
    return lambda(25, "ignore");
}

function testClosureWithCasting() returns float {
    int closureVar = 25;
    function (int, string) returns float lambda = (param1, param2) => <float>closureVar + <float>param1;
    return lambda(20, "ignore");
}

type Person record {|
   string name;
    int age;
|};

function testRecordTypeWithArrowExpr() returns Person {
    function (Person) returns Person lambda = (param1) => param1;
    return lambda({name:"John", age:12});
}

function testNillableParameter() returns string {
    function (string?) returns string lambda = (x) => x ?: "John";
    return lambda(());
}

function testTupleInput() returns [string, string] {
    function ([string, boolean, Person], string) returns [string, string] lambda = (tupleEntry, str) => [tupleEntry[2].name, str];
    [string, boolean, Person] tupleEntry = ["John", true, {name: "Doe", age: 12}];
    return lambda(tupleEntry, "Peter");
}

function twoLevelTestWithEndingArrowExpr() returns (function (int) returns (int)) {
    int methodInt1 = 2;
    var addFunc1 = function (int funcInt1) returns (int) {
        int methodInt2 = 23;
        function (int) returns (int) addFunc2 = funcInt2 => methodInt1 + funcInt2 + methodInt2;
        return addFunc2(5) + funcInt1;
    };
    return addFunc1;
}

function twoLevelTest() returns int {
    var foo = twoLevelTestWithEndingArrowExpr();
    return foo(6);
}

function threeLevelTestWithEndingArrowExpr() returns (function (int) returns (int)) {
    int methodInt1 = 2;
    var addFunc1 = function (int funcInt1) returns (int) {
        int methodInt2 = 23;
        var addFunc2 = function (int funcInt2) returns (int) {
            int methodInt3 = 7;
            function (int) returns (int) addFunc3 = funcInt3 => funcInt3 + methodInt1 + methodInt2 + methodInt3;
            return addFunc3(8) + funcInt2;
        };
        return addFunc2(4) + funcInt1;
    };
    return addFunc1;
}

function threeLevelTest() returns int {
    var foo = threeLevelTestWithEndingArrowExpr();
    return foo(6);
}

function testNestedArrowExpression() returns string {
    function (int, string) returns function (int, string) returns string lambda =
                        (integerVar, stringVar) => (integerVar2, stringVar2) => stringVar + integerVar.toString();
    var lambda2 = lambda(18, "John");
    return lambda2(20, "Doe");
}

function testNestedArrowExpression2() returns string {
    function (int, string) returns function (int, string) returns function (int, string) returns string lambda =
                        (integerVar, stringVar) => (integerVar2, stringVar2) =>
                            (integerVar3, stringVar3) => stringVar + stringVar2 + stringVar3;
    var lambda2 = lambda(18, "Do");
    var lambda3 = lambda2(20, "Re");
    return lambda3(22, "Me");
}

function testNestedArrowExpression3() returns string {
    function (int, string) returns function (int, string) returns function (int, string) returns function (int, string) returns string lambda =
                        (integerVar, stringVar) => (integerVar2, stringVar2) =>
                            (integerVar3, stringVar3) => (integerVar4, stringVar4) => stringVar + stringVar2 + stringVar3 + stringVar4;
    var lambda2 = lambda(18, "Do");
    var lambda3 = lambda2(20, "Re");
    var lambda4 = lambda3(22, "Me");
    return lambda4(24, "Fa");
}

function testNestedArrowExpression4() returns string {
    function (int, string) returns function (int, string) returns function (int, string) returns function (int, string) returns string lambda =
                        (integerVar, stringVar) => (integerVar2, stringVar2) =>
                            function (int integerVar3, string stringVar3) returns function (int, string) returns string {
                                return (integerVar4, stringVar4) => stringVar + stringVar2 + stringVar3 + stringVar4;
                        };
    var lambda2 = lambda(18, "Do");
    var lambda3 = lambda2(20, "Re");
    var lambda4 = lambda3(22, "Me");
    return lambda4(24, "Fa");
}

int k = 10;

type Foo record {
    function (int) returns int lambda = (i) => i * k;
};

class Bar {
    function (int) returns int lambda = (i) => i * k;
}

function testArrowExprInRecord() returns int {
    Foo f = {};
    return f.lambda(5);
}

function testArrowExprInObject() returns int {
    Bar f = new;
    var fp = f.lambda;
    return fp(6);
}

string packageVar = "Global Text";

function testArrowExprWithNoArguments() returns string {
    function () returns string lambda = () => "Some Text " + packageVar;
    return lambda();
}

function testArrowExprWithNoArgumentsAndStrTemplate() returns string {
    function () returns string lambda = () => string`Some Text ${packageVar}`;
    return lambda();
}

function testArrowExprWithNoArgumentsAndClosure() returns string {
    string closureVar = "Closure Text";
    function () returns string lambda = () => "Some Text " + packageVar + " " + closureVar;
    return lambda();
}

function testArrowExprInBracedExpr() returns string {
    function () returns string lambda = (() => "Some Text");
    return lambda();
}

int gVar = 100;

function testArrowExprWithNoReturn() returns int {
    function (int) lambda = integerVar => incrementInt(integerVar);
    lambda(20);
    return gVar;
}

function incrementInt(int x) {
    gVar += x;
}

type Sum function (int) returns function (int) returns int;

function testNestedArrowExpressionWithOneParameter() {
    Sum sum = intVar1 => intVar2 => intVar1 + intVar2;
    var newFunction = sum(7);
    var result = newFunction(5);
    assertEquality(12, result);
}

function testTypeNarrowingInArrowExpression() {
    var expected = "Hello World";
    var result = "Wrong Text Here";

    string|int s1 = "World";
    if (s1 is string) {
        string s2 = s1;
        function() returns string arrowFun = () => "Hello " + s2;
        result = arrowFun();
    }

    assertEquality(expected, result);
}

function testGlobalArrowExpressionsWithClosure() {
    var expected = 5;

    var a = f1(2);
    var result = a(3);
    assertEquality(expected, result);

    expected = 18;

    var b = f2(5);
    var c = b(6);
    result = c(7);
    assertEquality(expected, result);
}

const ASSERTION_ERR_REASON = "AssertionError";

function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }
    if expected === actual {
        return;
    }
    panic error(ASSERTION_ERR_REASON,
                message = "expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}
