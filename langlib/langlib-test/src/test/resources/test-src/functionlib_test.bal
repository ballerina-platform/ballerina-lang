// Copyright (c) 2022 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

public type NewPerson record {
    string firstName;
    string secondName;
};

type NewStudent record {
    string firstName = "chiran";
    string secondName = "sachintha";
};

type Foo record {
    never a?;
    never b?;
};

type Bar record {|
    int a;
    int b;
|};

function value1(int a, int b, int c = 15) returns int {
    return a + b + c;
}

function value2(int a, int b, int c) returns int {
    return a + b + c;
}

function testCallFunctionWithRequiredParameters() {
    assertEquality(function:call(value1, 10, 10, 10), 30);
    assertEquality(function:call(value1, 20, 20, 10), 50);
    assertEquality(function:call(value1, 10, 10), 35);
    assertEquality(function:call(value2, 10, 20, 30), 60);
}

function value3(int a = 10, int b = 15, int c = 20) returns int {
    return a + b + c;
}

function testCallFunctionWithDefaultParameters() {
    int a = 5;
    int b = 20; 
    int c = 30;
    assertEquality(function:call(value3), 45);
    assertEquality(function:call(value3, 15), 50);
    assertEquality(function:call(value3, a), 40);
    assertEquality(function:call(value3, 15, 20), 55);
    assertEquality(function:call(value3, a, b), 45);
    assertEquality(function:call(value3, 10, 10, 10), 30);
    assertEquality(function:call(value3, a, b, c), 55);
}

function value4(int a, int b, int... c) returns int {
    return a + b + c[0] + c[1];
}

function value5(int a, int b = 10, int... c) returns int {
    return a + b + c[0] + c[1];
}

function value6(int a = 10, int b = 10, int... c) returns int {
    return a + b;
}

function value7(int a, int b = 10, int... c) returns int {
    return a + b;
}

function value8(int... a) returns int {
    return a[0];
}

function testCallFunctionWithRestParameters() {
    assertEquality(function:call(value4, 10, 10, 10, 10), 40);
    assertEquality(function:call(value5, 10, 10, 10, 10), 40);
    assertEquality(function:call(value6, 10, 10, 10, 10), 20);
    assertEquality(function:call(value6, 10, 10), 20);
    assertEquality(function:call(value6, 20), 30);
    assertEquality(function:call(value7, 10), 20);
    assertEquality(function:call(value8, 10), 10);
    assertEquality(function:call(value8, 10, 20, 30, 40), 10);
}

function test9(*NewPerson person) returns string {
    return person.firstName + " " + person.secondName;
}

function test10(string middleName = " ", *NewPerson person) returns string {
    return person.firstName + middleName + person.secondName;
}

function test11(*NewStudent person) returns string {
    return person.firstName + " " + person.secondName;
}

function test12(*Foo foo, *Bar bar) returns anydata {
    return foo["value"];
}

function test13(int a, int b = 5, *Foo foo, int... c) returns int {
    return a + b;
}

function test14(int a, int b, *Foo foo) returns int {
    return a + b + <int> foo["value"];
}

function test15(int a = 10, int b = 15, *Foo foo) returns int {
    return a + b + <int> foo["value"];
}

function testCallFunctionWithIncludedRecordParameters() {
    NewPerson person = {firstName: "chiran", secondName: "sachintha"};
    NewStudent student = {};
    Foo f = {"value": 10};
    Bar b = {a:10, b: 20};

    assertEquality(function:call(test9, person), "chiran sachintha");
    assertEquality(function:call(test10, " ", person), "chiran sachintha");
    assertEquality(function:call(test11, student), "chiran sachintha");
    assertEquality(function:call(test12, f, b), 10);
    assertEquality(function:call(test13, 10, 10, f), 20);
    assertEquality(function:call(test13, 10, 10, f, 10, 10), 20);
    assertEquality(function:call(test14, 10, 10, f), 30);
    assertEquality(function:call(test15, 15, 15, f), 40);
}

function test16() returns any|error {
    function(int, int) returns int addFunction = func1;
    return function:call(addFunction, 1, 2);
}

function func1(int a, int b) returns int {
    return a + b;
}

function test17() returns any|error {
    function(int, int) returns string sumFunction = function(int a, int b) returns string {
                                                        int value =  a + b;
                                                        return "sum is " + value.toString();
                                                    };
    return function:call(sumFunction, 1, 2);
}

function test18() returns any|error {
    return function:call(func2, 1, func1);
}

function func2(int a, function(int x, int y) returns int func) returns int {
    return  a + func(1, 2);
}

function test19() returns any|error {
    function(string a, string b) returns string foo = func3();
    return function:call(foo, "hello ", "world.");
}

function func3() returns function(string a, string b) returns string {
                            return function(string x, string y) returns string {
                                        return x + y;
                                    };
}

function test20(string... details) returns string {
    return details[0];
}

function testCallFunctionWithFunctionPointers() {
    assertEquality(test16(), 3);
    assertEquality(test17(), "sum is 3");
    assertEquality(test18(), 4);
    assertEquality(test19(), "hello world.");
}

function testCallFunctionWithInvalidArguments() {
    any|error a1 = trap function:call(value1, 10);
    assertEquality(true, a1 is error);
    assertEquality("{ballerina/lang.function}IncompatibleArguments", (<error> a1).message());
    assertEquality("arguments of incompatible types: argument(s) of type(s) 'int,int,int', cannot be passed to function expecting parameter(s) of type(s) 'int'", (<error> a1).detail()["message"]);

    a1 = trap function:call(value1, 10, "10");
    assertEquality(true, a1 is error);
    assertEquality("{ballerina/lang.function}IncompatibleArguments", (<error> a1).message());
    assertEquality("arguments of incompatible types: argument(s) of type(s) 'int,int,int', cannot be passed to function expecting parameter(s) of type(s) 'int,string'", (<error> a1).detail()["message"]);

    a1 = trap function:call(value2, 10, 10, 10, 10);
    assertEquality(true, a1 is error);
    assertEquality("{ballerina/lang.function}IncompatibleArguments", (<error> a1).message());
    assertEquality("arguments of incompatible types: argument(s) of type(s) 'int,int,int', cannot be passed to function expecting parameter(s) of type(s) 'int,int,int,int'", (<error> a1).detail()["message"]);

    a1 = trap function:call(value2, 10, 10);
    assertEquality(true, a1 is error);
    assertEquality("{ballerina/lang.function}IncompatibleArguments", (<error> a1).message());
    assertEquality("arguments of incompatible types: argument(s) of type(s) 'int,int,int', cannot be passed to function expecting parameter(s) of type(s) 'int,int'", (<error> a1).detail()["message"]);

    a1 = trap function:call(value4, 10, 10);
    assertEquality(true, a1 is error);
    assertEquality("{ballerina/lang.array}IndexOutOfRange", (<error> a1).message());
    assertEquality("array index out of range: index: 0, size: 0", (<error> a1).detail()["message"]);

    a1 = trap function:call(value4, 10, 10, 10);
    assertEquality(true, a1 is error);
    assertEquality("{ballerina/lang.array}IndexOutOfRange", (<error> a1).message());
    assertEquality("array index out of range: index: 1, size: 1", (<error> a1).detail()["message"]);
    
    a1 = trap function:call(value5, 10);
    assertEquality(true, a1 is error);
    assertEquality("{ballerina/lang.array}IndexOutOfRange", (<error> a1).message());
    assertEquality("array index out of range: index: 0, size: 0", (<error> a1).detail()["message"]);

    a1 = trap function:call(value8);
    assertEquality(true, a1 is error);
    assertEquality("{ballerina/lang.array}IndexOutOfRange", (<error> a1).message());
    assertEquality("array index out of range: index: 0, size: 0", (<error> a1).detail()["message"]);

    a1 = trap function:call(value8, "");
    assertEquality(true, a1 is error);
    assertEquality("{ballerina/lang.function}IncompatibleArguments", (<error> a1).message());
    assertEquality("arguments of incompatible types: argument(s) of type(s) 'int...', cannot be passed to function expecting parameter(s) of type(s) 'string'", (<error> a1).detail()["message"]);

    a1 = trap function:call(value8, "", "");
    assertEquality(true, a1 is error);
    assertEquality("{ballerina/lang.function}IncompatibleArguments", (<error> a1).message());
    assertEquality("arguments of incompatible types: argument(s) of type(s) 'int...', cannot be passed to function expecting parameter(s) of type(s) 'string,string'", (<error> a1).detail()["message"]);

    a1 = trap function:call(value1, 10);
    assertEquality(true, a1 is error);
    assertEquality("{ballerina/lang.function}IncompatibleArguments", (<error> a1).message());
    assertEquality("arguments of incompatible types: argument(s) of type(s) 'int,int,int', cannot be passed to function expecting parameter(s) of type(s) 'int'", (<error> a1).detail()["message"]);

    NewPerson person = {firstName: "chiran", secondName: "sachintha"};
    a1 = trap function:call(test10, person);
    assertEquality(true, a1 is error);
    assertEquality("{ballerina/lang.function}IncompatibleArguments", (<error> a1).message());
    assertEquality("arguments of incompatible types: argument(s) of type(s) 'string,NewPerson', cannot be passed to function expecting parameter(s) of type(s) 'NewPerson'", (<error> a1).detail()["message"]);

    a1 = trap function:call(test11, {});
    assertEquality(true, a1 is error);
    assertEquality("{ballerina/lang.function}IncompatibleArguments", (<error> a1).message());
    assertEquality("arguments of incompatible types: argument(s) of type(s) 'NewStudent', cannot be passed to function expecting parameter(s) of type(s) 'map<(any|error)>'", (<error> a1).detail()["message"]);
    
    a1 = trap function:call(test20, 10);
    assertEquality(true, a1 is error);
    assertEquality("{ballerina/lang.function}IncompatibleArguments", (<error> a1).message());
    assertEquality("arguments of incompatible types: argument(s) of type(s) 'string...', cannot be passed to function expecting parameter(s) of type(s) 'int'", (<error> a1).detail()["message"]);

}

function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    string expectedValAsString = expected is error ? expected.toString() : expected.toString();
    string actualValAsString = actual is error ? actual.toString() : actual.toString();
    panic error("expected '" + expectedValAsString + "', found '" + actualValAsString + "'");
}
