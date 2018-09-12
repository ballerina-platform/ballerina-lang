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
    return lambda(12);
}

function testArrowExprWithTwoParams() returns string {

    function (int, string) returns string lambda = (x, y) => x + y;

    return lambda(12, "John");
}

function testReturnArrowExpr() returns string {
    var lambda = returnsArrowExpr();
    int intVar = 10;
    string stringVar = "Adam";

    return lambda(intVar, stringVar);
}

function returnsArrowExpr() returns function (int x, string y) returns string {

    return (x, y) => x + y;
}

function testArrowExprReturnTuple() returns (string, int) {

    function (int, string) returns (string, int) lambda = (x, y) => (x + y, x);

    return lambda(12, "John");
}

function testArrowExprReturnUnion() returns (string|int) {

    function (int, string) returns (string|int) lambda = (x, y) => x + y;

    return lambda(12, "John");
}

function testBooleanParamType() returns boolean {

    function (boolean) returns boolean invertBoolean = param1 => !param1;
    return invertBoolean(false);
}

function testClosureAccess() returns float {

    int closureVar = 25;

    function (int, string) returns float lambda = (param1, param2) => <float>closureVar + <float>param1;
    return lambda(25, "ignore");
}

type Person record {
   string name,
    int age,
    !...
};

function testRecordTypeWithArrowExpr() returns Person {

    function (Person) returns Person lambda = (param1) => param1;
    return lambda({name:"John", age:12});
}

function testNillableParameter() returns string {
    function (string?) returns string lambda = (x) => x but {() => "John"};
    return lambda(());
}

function testTupleInput() returns (string, string) {
    function ((string, boolean, Person), string) returns (string, string) lambda = (tupleEntry, str) => (tupleEntry[2].name, str);
    (string, boolean, Person) tupleEntry = ("John", true, {name: "Doe", age: 12});
    return lambda(tupleEntry, "Peter");
}

function testClosure() returns int {

    int closureVar = 10;
    function (int, string) returns int lambda = (param1, param2) => closureVar + param1;
    return lambda(25, "ignore");
}
