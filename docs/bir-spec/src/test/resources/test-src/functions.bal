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

function foo() {

}

function bar(int a) {
    int i = a;
}

function baz(string s, float f) returns byte {
    return 4;
}

function printValue(string value) {
    // do nothing
}

function add(int a, int b) returns int {
    return a + b;
}

function printAndReturnValue(string s) returns string {
    string t = "Hello ".concat(s);
    return t;
}

public function functionWithInvocations() {
    printValue("This is a sample text");
    int result = add(5, 6);
    _ = printAndReturnValue("World");
}

function calculate(int a, int b, int c) returns int {
    return a + 2 * b + 3 * c;
}

public function functionInvocationWithRequiredParams() {
    int result = calculate(5, 6, 7);
    result = calculate(5, c = 7, b = 6);
}

function printSalaryDetails(int baseSalary,
                            int annualIncrement = 20,
                            float bonusRate = 0.02) {

    // do nothing
}
public function functionInvocationWithDefaultParams() {
    printSalaryDetails(2500);
    printSalaryDetails(2500, annualIncrement = 100);
    printSalaryDetails(2500, 100);
    printSalaryDetails(2500, bonusRate = 0.1);
    printSalaryDetails(2500, 20, 0.1);
    printSalaryDetails(2500, annualIncrement = 100, bonusRate = 0.1);
    printSalaryDetails(annualIncrement = 100, baseSalary = 2500, bonusRate = 0.1);
}

function printDetails(string name,
                      int age = 18,
                      string... modules) {
    string detailString = "Name: " + name + ", Age: " + age.toString();

    if (modules.length() == 0) {
        return;
    }
    int index = 0;
    string moduleString = "Module(s): ";
    foreach string module in modules {
        if (index == 0) {
            moduleString += module;
        } else {
            moduleString += ", " + module;
        }
        index += 1;
    }
}
public function functionWithRestParams() {
    printDetails("Alice");
    printDetails("Bob", 20);
    printDetails("Corey", 19, "Math");
    printDetails("Diana", 20, "Math", "Physics");
    string[] modules = ["Math", "Physics"];
    printDetails("Diana", 20, ...modules);
}

function testFunctionPointer(string s, int... x) returns float {
    int|error y = 'int:fromString(s);
    float f = 0.0;

    if (y is int) {
        foreach var item in x {
            f += item * 1.0 * y;
        }
    } else {
        panic y;
    }
    return f;
}

function invokeFunctionPointer(int x, function (string, int...) returns float bar)
             returns float {
    return x * bar("2", 2, 3, 4, 5);
}

function getFunctionPointer() returns
                    (function (string, int...) returns float) {
    return testFunctionPointer;
}

public function functionWithFunctionPointers() {
    _ = invokeFunctionPointer(10, testFunctionPointer);
    _ = invokeFunctionPointer(10, getFunctionPointer());

    function (string, int...) returns float f = getFunctionPointer();

    _ = invokeFunctionPointer(10, f);
}

public function anonymousFunctions() {
    function (string, string) returns string anonFunction =
            function (string x, string y) returns string {
                return x + y;
            };
    _ = anonFunction("Hello ", "World.!!!");

    var anonFunction2 = function (string x, string y, string... z) returns string {
                            string value = x + y;
                            foreach var item in z {
                                value += item;
                            }
                            return value;
                        };
    _ = anonFunction2("Ballerina ", "is ", "an ", "open ", "source ", "programming ", "language.");

    function (string, string) returns string arrowExpr = (x, y) => x + y;
    _ = arrowExpr("Hello ", "World.!!!");
}

public function expressionBodiedFunction() {

    var toEmployee = function (PersonRec p, string pos) returns EmployeeRec => {
        name: p.fname + " " + p.lname,
        designation: pos
    };

    PersonRec john = { fname: "John", lname: "Doe", age: 25 };
    EmployeeRec johnEmp = toEmployee(john, "Software Engineer");
}

type PersonRec record {|
    string fname;
    string lname;
    int age;
|};

type EmployeeRec record {|
    string name;
    string designation;
|};

public function functionWithIteration() {
    map<string> words = {
        a: "ant",
        b: "bear",
        c: "cat",
        d: "dear",
        e: "elephant"
    };


    map<string> animals = words.map(toUpper);

    int[] numbers = [-5, -3, 2, 7, 12];

    int[] positive = numbers.filter(function (int i) returns boolean {
        return i >= 0;
    });


    numbers.forEach(function(int i) {
        int x = i;
    });

    int total = numbers.reduce(sum, 0);

    int totalWithInitialValue = numbers.reduce(sum, 5);
    map<json> j = {name: "apple", colors: ["red", "green"], price: 5};
    j.map(function (json value) returns string {
        string result = value.toString();
        return result;
    }).forEach(function (string s) {
    });

}
function toUpper(string value) returns string {
    return value.toUpperAscii();
}
function sum(int accumulator, int currentValue) returns int {
    return accumulator + currentValue;
}

function functionWithAllTypesParams(int a, float b, string c = "John", int d = 5, string e = "Doe", int... z)
        returns [int, float, string, int, string, int[]] {
    return [a, b, c, d, e, z];
}

function testInvokeFunctionInOrder1() returns [int, float, string, int, string, int[]] {
    return functionWithAllTypesParams(10, 20.0, c="Alex", d=30, e="Bob");
}

function testInvokeFunctionInOrder2() returns [int, float, string, int, string, int[]] {
    int[] array = [40, 50, 60];
    return functionWithAllTypesParams(10, 20.0, c="Alex", d=30, e="Bob");
}

function testInvokeFunctionInMixOrder1() returns [int, float, string, int, string, int[]] {
    return functionWithAllTypesParams(e="Bob", b=20.0, c="Alex", a=10, d=30);
}

function testInvokeFunctionInMixOrder2() returns [int, float, string, int, string, int[]] {
    return functionWithAllTypesParams(10, c="Alex", e="Bob", d=30, b=20.0);
}

function testInvokeFunctionWithoutSomeNamedArgs() returns [int, float, string, int, string, int[]] {
    return functionWithAllTypesParams(10, 20.0, c="Alex");
}

function testInvokeFunctionWithRequiredArgsOnly() returns [int, float, string, int, string, int[]] {
    return functionWithAllTypesParams(10, 20.0);
}

function testInvokeFunctionWithAllParamsAndRestArgs() returns [int, float, string, int, string, int[]] {
    return functionWithAllTypesParams(10, 20.0, "John1", 6, "Doe1", 40, 50, 60);
}

function funcInvocAsRestArgs() returns [int, float, string, int, string, int[]] {
    return functionWithAllTypesParams(10, 20.0, "Alex", 30, "Bob", ...getIntArray());
}

function getIntArray() returns (int[]) {
    return [1,2,3,4];
}

function functionWithoutRestParams(int a, float b, string c = "John", int d = 5, string e = "Doe") returns
            [int, float, string, int, string] {
    return [a, b, c, d, e];
}

function testInvokeFuncWithoutRestParamsAndMissingDefaultableParam() returns [int, float, string, int, string] {
    return functionWithoutRestParams(10, b=20.0, d=30, e="Bob");
}

function functionWithOnlyNamedParams(int a=5, float b=6.0, string c = "John", int d = 7, string e = "Doe")
                                                                            returns [int, float, string, int, string] {
    return [a, b, c, d, e];
}

function testInvokeFuncWithOnlyNamedParams1() returns [int, float, string, int, string] {
    return functionWithOnlyNamedParams(b = 20.0, e="Bob", d=30, a=10, c="Alex");
}

function testInvokeFuncWithOnlyNamedParams2() returns [int, float, string, int, string] {
    return functionWithOnlyNamedParams(e="Bob", d=30, c="Alex");
}

function testInvokeFuncWithOnlyNamedParams3() returns [int, float, string, int, string] {
    return functionWithOnlyNamedParams();
}

function functionWithOnlyRestParam(int... z) returns (int[]) {
    return z;
}

function testInvokeFuncWithOnlyRestParam1() returns (int[]) {
    return functionWithOnlyRestParam();
}

function testInvokeFuncWithOnlyRestParam2() returns (int[]) {
    return functionWithOnlyRestParam(10, 20, 30);
}

function testInvokeFuncWithOnlyRestParam3() returns (int[]) {
    int[] a = [10, 20, 30];
    return functionWithOnlyRestParam(...a);
}

function functionAnyRestParam(any... z) returns (any[]) {
    return z;
}

function testInvokeFuncWithAnyRestParam1() returns (any[]) {
    int[] a = [10, 20, 30];
    json j = {"name":"John"};
    return functionAnyRestParam(a, j);
}

function funcWithUnionTypedDefaultParam(string|int? s = "John") returns string|int? {
    return s;
}

function testFuncWithUnionTypedDefaultParam() returns json {
    return funcWithUnionTypedDefaultParam();
}

function funcWithNilDefaultParamExpr_1(string? s = ()) returns string? {
    return s;
}

type Student record {|
    int a = 0;
    anydata...;
|};

function funcWithNilDefaultParamExpr_2(Student? s = ()) returns Student? {
    return s;
}

function testFuncWithNilDefaultParamExpr() returns [any, any] {
    return [funcWithNilDefaultParamExpr_1(), funcWithNilDefaultParamExpr_2()];
}

public class Employee {

    public string name = "";
    public int salary = 0;

    function init(string name = "supun", int salary = 100) {
        self.name = name;
        self.salary = salary;
    }

    public function getSalary (string n, int b = 0) returns int {
        return self.salary + b;
    }
}

function testAttachedFunction() returns [int, int] {
    Employee emp = new;
    return [emp.getSalary("Alex"), emp.getSalary("Alex", b = 10)];
}


function testDefaultableParamInnerFunc () returns [int, string] {
    Person p = new;
    return p.test1(a = 50);
}

class Person {
    public int age = 0;

    function test1(int a = 77, string n = "inner default") returns [int, string] {
        string val = n + " world";
        int intVal = a + 10;
        return [intVal, val];
    }

    function test2(int a = 89, string n = "hello") returns [int, string] {
        string val = n + " world";
        int intVal = a + 10;
        return [intVal, val];
    }
}

function functionOfFunctionTypedParamWithRest(int[] x, function (int...) returns int bar) returns [int, int] {
    return [x[0], bar(x[0])] ;
}

function sampleFunctionTypedParam(int... a) returns int {
    if (a.length() > 0) {
        return a[0];
    } else {
        return 0;
    }
}

function testFunctionOfFunctionTypedParamWithRest1() {
    int[] x = [4];
    int[] y = functionOfFunctionTypedParamWithRest(x, sampleFunctionTypedParam);
}

function testFunctionOfFunctionTypedParamWithRest2() {
    int[] x = [10, 20, 30];
    int[] y = functionOfFunctionTypedParamWithRest(x, sampleFunctionTypedParam);
}
