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
import ballerina/test;

// function-type-descriptor := function function-signature
// function-signature := ( param-list ) return-type-descriptor
// return-type-descriptor := [ returns annots type-descriptor ]
// param-list :=
// [ individual-param-list [, rest-param]]
// | rest-param
// individual-param-list := individual-param (, individual-param)*
// individual-param :=
// annots type-descriptor param-name [= default-value]
// default-value := const-expr
// rest-param := type-descriptor ... [param-name]

// A function is a part of a program that can be explicitly executed.
// In Ballerina, a function is also a value, implying that it can be stored in variables,
// and passed to or returned from functions.
// When a function is executed, it is passed argument values as input and returns a value as
// output.
@test:Config {}
function testFunctionsAsValues() {
    int i = 1;
    int j = 2;

    int r1 = add(i, j);
    int r2 = execFunction(getAddFunction(), i, j);

    test:assertEquals(r1, i + j, msg = "expected value to be equal to addition of i and j");
    test:assertEquals(r2, i + j, msg = "expected value to be equal to addition of i and j");
}

function getAddFunction() returns (function (int i, int j) returns int) {
    function (int i, int j) returns int func = add;
    return func;
}

function execFunction(function (int i, int j) returns int func, int i, int j) returns int {
    return func.call(i, j);
}

function add(int i, int j) returns int {
    return i + j;
}

@test:Config {}
function testFunctionsWithAnnotations() {
    string s1 = "test string 1";
    string s2 = funcWithAnnotatedReturn(s1);
    string s3 = funcWithAnnotatedParam(s2);
    test:assertEquals(s1, s3, msg = "expected values to be unchanged");
}

function funcWithAnnotatedReturn(string s) returns @untained string {
    return untaint s;
}

function funcWithAnnotatedParam(@sensitive string s) returns string {
    return s;
}

// A function that would in other programming
// languages not return a value is represented in Ballerina by a function returning (). (Note
// that the function definition does not have to explicitly return (); a return statement or falling of
// the end of the function body will implicitly return ().)
const int CONST_INT_VAL = 1000;
int globalIntVar = 0;

@test:Config {}
function testNilReturningFunctions() {
    var res1 = nilRetFuncOne();
    test:assertEquals(res1, (), msg = "expected return value to be ()");
    test:assertEquals(globalIntVar, CONST_INT_VAL, msg = "expected globalIntVar to have been updated");
    globalIntVar = 0;

    var res2 = nilRetFuncTwo();
    test:assertEquals(res2, (), msg = "expected return value to be ()");
    test:assertEquals(globalIntVar, CONST_INT_VAL, msg = "expected globalIntVar to have been updated");
    globalIntVar = 0;

    var res3 = nilRetFuncThree();
    test:assertEquals(res3, (), msg = "expected return value to be ()");
    test:assertEquals(globalIntVar, CONST_INT_VAL, msg = "expected globalIntVar to have been updated");
}

# A nil returning function that has `return ();` to return.
function nilRetFuncOne() {
    updateGloalVar();
    return ();
}

# A nil returning function that has `return;` to return.
function nilRetFuncTwo() {
    updateGloalVar();
    return;
}

# A nil returning function that has no return statement.
function nilRetFuncThree() {
    updateGloalVar();
}

function updateGloalVar() {
    globalIntVar = CONST_INT_VAL;    
}

// A function can be passed any number of arguments. Each argument is passed in one of
// three ways: as a positional argument, as a named argument or as a rest argument. A
// positional argument is identified by its position relative to other positional arguments. A
// named argument is identified by name. Only one rest argument can be passed and its value
// must be an array.
//
// A function definition defines a number of named parameters, which can be referenced like
// variables within the body of the function definition. The parameters of a function definition
// are of three kinds: required, defaultable and rest. The relative order of required parameters
// is significant, but not for defaultable parameters. There can be at most one rest parameter.
string globalStringVar = "";

@test:Config {}
function testFunctionWithNoParamsAndNoReturn() {
    funcWithNoParamsAndNoReturn();
    test:assertEquals(globalStringVar, NO_PARAMS_NO_RETURN, msg = "expected globalStringVar to have been updated");
}

const string NO_PARAMS_NO_RETURN = "no params no return";

function funcWithNoParamsAndNoReturn() {
    globalStringVar = NO_PARAMS_NO_RETURN;
}

@test:Config {}
function testFunctionWithParamsAndNoReturn() {
    string s1 = "update to: ";
    string s2 = WITH_PARAMS_NO_RETURN;
    funcWithParamsAndNoReturn(s1, s2);
    test:assertEquals(globalStringVar, s1 + s2, msg = "expected globalStringVar to have been updated");
}

const string WITH_PARAMS_NO_RETURN = "with params no return";

function funcWithParamsAndNoReturn(string s1, string s2) {
    globalStringVar = s1 + s2;
}

int a = 1;
int b = 2;
int c = 3;
int d = 4;
int e = 5;
int f = 6;

@test:Config {}
function testFunctionInvocationWithoutNamedAndRestArgs() {
    test:assertEquals(funcWithNamedAndRestParams(a, b), a + b, 
                      msg = "expected return value to be equal to the sum");
}

@test:Config {}
function testFunctionInvocationWithSingleNamedArgAndWithoutRestArg() {
    test:assertEquals(funcWithNamedAndRestParams(a, j = c, b), a + b + c, 
                      msg = "expected return value to be equal to the sum");
}

@test:Config {}
function testFunctionInvocationWithAllNamedArgsAndWithoutRestArg() {
    test:assertEquals(funcWithNamedAndRestParams(a, j = c, b, k = d), a + b + c + d, 
                      msg = "expected return value to be equal to the sum");
}

@test:Config {}
function testFunctionInvocationWithRestArgsAndWithoutNamedArgs() {
    test:assertEquals(funcWithNamedAndRestParams(a, b, e, f), a + b + e + f, 
                      msg = "expected return value to be equal to the sum");
    
    int[] arr = [e, f];
    test:assertEquals(funcWithNamedAndRestParams(a, b, ...arr), a + b + e + f, 
                      msg = "expected return value to be equal to the sum");
}

@test:Config {}
function testFunctionInvocationWithAllArgs() {
    test:assertEquals(funcWithNamedAndRestParams(a, b, j = c, k = d, e, f), a + b + c + d + e + f, 
                      msg = "expected return value to be equal to the sum");
}

function funcWithNamedAndRestParams(int i, int j = 0, int k = 0, int l, int... m) returns int {
    int sum = i + j + k + l;
    foreach int intVal in m {
        sum += intVal;
    }
    return sum;
}

// The type system classifies functions based only on the arguments they are declared to
// accept and the values they are declared to return. Other aspects of a function value, such as
// how the return value depends on the argument, or what the side-effects of calling the
// function are, are not considered as part of the function’s type.
@test:Config {}
function testFunctionTypeClassification() {
    any func1 = funcRetFloatOne;
    any func2 = funcRetFloatTwo;

    test:assertTrue(func1 is function (float f) returns float, 
                      msg = "expected function to be of expected type");
    test:assertTrue(func2 is function (float f) returns float, 
                      msg = "expected function to be of expected type");
}

float globalFloatVar = 0.0;

function funcRetFloatOne(float f1) returns float {
    return globalFloatVar + 1.0;
}

function funcRetFloatTwo(float fl) returns float {
    globalFloatVar = fl;
    return 0.0;
}

// For return values, typing is straightforward: returns T means that the value returned by
// the function is always of type T.

@test:Config {}
function testFunctionReturnType() {
    any res = funcWithUnionReturnType("string");
    test:assertTrue(res is string|int, 
                      msg = "expected return type to be of expected type");

    res = funcWithUnionReturnType("int");
    test:assertTrue(res is string|int, 
                      msg = "expected return type to be of expected type");
}

type STRING_OR_INT "string"|"int";

function funcWithUnionReturnType(STRING_OR_INT s) returns string|int {
    match s {
        "string" => return "string";
        "int" => return 1;
    }
    return "won't reach here";
}

// TODO: add tests for const-expr
// annots type-descriptor param-name [= default-value]
// default-value := const-expr
