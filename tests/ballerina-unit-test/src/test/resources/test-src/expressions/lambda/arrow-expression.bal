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

function testArrowExprWithOneParam() returns int {
    function (int) returns int lambda = param1 => param1*2;
    return lambda.call(12);
}

function testArrowExprWithTwoParams() returns string {
    function (int, string) returns string lambda = (x, y) => x + y;
    return lambda.call(12, "John");
}

function testReturnArrowExpr() returns string {
    var lambda = returnsArrowExpr();
    int intVar = 10;
    string stringVar = "Adam";
    return lambda.call(intVar, stringVar);
}

function returnsArrowExpr() returns function (int x, string y) returns string {
    return (x, y) => x + y;
}

function testArrowExprReturnTuple() returns (string, int) {
    function (int, string) returns (string, int) lambda = (x, y) => (x + y, x);
    return lambda.call(12, "John");
}

function testArrowExprReturnUnion() returns (string|int) {
    function (int, string) returns (string|int) lambda = (x, y) => x + y;
    return lambda.call(12, "John");
}

function testBooleanParamType() returns boolean {
    function (boolean) returns boolean invertBoolean = param1 => !param1;
    return invertBoolean.call(false);
}

function testClosure() returns int {
    int closureVar = 10;
    function (int, string) returns int lambda = (param1, param2) => closureVar + param1;
    return lambda.call(25, "ignore");
}

function testClosureWithCasting() returns float {
    int closureVar = 25;
    function (int, string) returns float lambda = (param1, param2) => <float>closureVar + <float>param1;
    return lambda.call(20, "ignore");
}

type Person record {
   string name;
    int age;
    !...
};

function testRecordTypeWithArrowExpr() returns Person {
    function (Person) returns Person lambda = (param1) => param1;
    return lambda.call({name:"John", age:12});
}

function testNillableParameter() returns string {
    function (string?) returns string lambda = (x) => x but {() => "John"};
    return lambda.call(());
}

function testTupleInput() returns (string, string) {
    function ((string, boolean, Person), string) returns (string, string) lambda = (tupleEntry, str) => (tupleEntry[2].name, str);
    (string, boolean, Person) tupleEntry = ("John", true, {name: "Doe", age: 12});
    return lambda.call(tupleEntry, "Peter");
}

function twoLevelTestWithEndingArrowExpr() returns (function (int) returns (int)) {
    int methodInt1 = 2;
    var addFunc1 = function (int funcInt1) returns (int) {
        int methodInt2 = 23;
        function (int) returns (int) addFunc2 = funcInt2 => methodInt1 + funcInt2 + methodInt2;
        return addFunc2.call(5) + funcInt1;
    };
    return addFunc1;
}

function twoLevelTest() returns int {
    var foo = twoLevelTestWithEndingArrowExpr();
    return foo.call(6);
}

function threeLevelTestWithEndingArrowExpr() returns (function (int) returns (int)) {
    int methodInt1 = 2;
    var addFunc1 = function (int funcInt1) returns (int) {
        int methodInt2 = 23;
        var addFunc2 = function (int funcInt2) returns (int) {
            int methodInt3 = 7;
            function (int) returns (int) addFunc3 = funcInt3 => funcInt3 + methodInt1 + methodInt2 + methodInt3;
            return addFunc3.call(8) + funcInt2;
        };
        return addFunc2.call(4) + funcInt1;
    };
    return addFunc1;
}

function threeLevelTest() returns int {
    var foo = threeLevelTestWithEndingArrowExpr();
    return foo.call(6);
}

function testNestedArrowExpression() returns string {
    function (int, string) returns function (int, string) returns string lambda =
                        (integerVar, stringVar) => (integerVar2, stringVar2) => stringVar + integerVar;
    var lambda2 = lambda.call(18, "John");
    return lambda2.call(20, "Doe");
}

function testNestedArrowExpression2() returns string {
    function (int, string) returns function (int, string) returns function (int, string) returns string lambda =
                        (integerVar, stringVar) => (integerVar2, stringVar2) =>
                            (integerVar3, stringVar3) => stringVar + stringVar2 + stringVar3;
    var lambda2 = lambda.call(18, "Do");
    var lambda3 = lambda2.call(20, "Re");
    return lambda3.call(22, "Me");
}

function testNestedArrowExpression3() returns string {
    function (int, string) returns function (int, string) returns function (int, string) returns function (int, string) returns string lambda =
                        (integerVar, stringVar) => (integerVar2, stringVar2) =>
                            (integerVar3, stringVar3) => (integerVar4, stringVar4) => stringVar + stringVar2 + stringVar3 + stringVar4;
    var lambda2 = lambda.call(18, "Do");
    var lambda3 = lambda2.call(20, "Re");
    var lambda4 = lambda3.call(22, "Me");
    return lambda4.call(24, "Fa");
}

function testNestedArrowExpression4() returns string {
    function (int, string) returns function (int, string) returns function (int, string) returns function (int, string) returns string lambda =
                        (integerVar, stringVar) => (integerVar2, stringVar2) =>
                            function (int integerVar3, string stringVar3) returns function (int, string) returns string {
                                return (integerVar4, stringVar4) => stringVar + stringVar2 + stringVar3 + stringVar4;
                        };
    var lambda2 = lambda.call(18, "Do");
    var lambda3 = lambda2.call(20, "Re");
    var lambda4 = lambda3.call(22, "Me");
    return lambda4.call(24, "Fa");
}
