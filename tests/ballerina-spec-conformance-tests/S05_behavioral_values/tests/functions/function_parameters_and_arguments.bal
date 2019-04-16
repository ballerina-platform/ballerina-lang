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

const EXPECTED_RETURN_VALUE_TO_BE_EQUAL_TO_SUM_FAILURE_MESSAGE = "expected return value to be equal to the sum";
const EXPECTED_GLOBAL_STRING_VAR_TO_BE_UPDATED_FAILURE_MESSAGE = "expected globalStringVar to have been updated";

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
//
// If there is no named argument for a defaultable parameter then the default value is assigned to that parameter.
string globalStringVar = "";

@test:Config {}
function testFunctionWithNoParamsAndNoReturn() {
    funcWithNoParamsAndNoReturn();
    test:assertEquals(globalStringVar, NO_PARAMS_NO_RETURN,
        msg = EXPECTED_GLOBAL_STRING_VAR_TO_BE_UPDATED_FAILURE_MESSAGE);
}

const NO_PARAMS_NO_RETURN = "no params no return";

function funcWithNoParamsAndNoReturn() {
    globalStringVar = NO_PARAMS_NO_RETURN;
}

@test:Config {}
function testFunctionWithParamsAndNoReturn() {
    string s1 = "update to: ";
    string s2 = WITH_PARAMS_NO_RETURN;
    funcWithParamsAndNoReturn(s1, s2);
    test:assertEquals(globalStringVar, s1 + s2, msg = EXPECTED_GLOBAL_STRING_VAR_TO_BE_UPDATED_FAILURE_MESSAGE);
}

const WITH_PARAMS_NO_RETURN = "with params no return";

function funcWithParamsAndNoReturn(string s1, string s2) {
    globalStringVar = s1 + s2;
}

int a = 1;
int b = 2;
int c = 3;
int d = 4;
int e = 5;
int f = 6;
const int INT_CONST = 10;

@test:Config {}
function testFunctionInvocationWithoutNamedAndRestArgs() {
    test:assertEquals(funcWithNamedAndRestParams(a, b), a + b + INT_CONST,
        msg = EXPECTED_RETURN_VALUE_TO_BE_EQUAL_TO_SUM_FAILURE_MESSAGE);
}

@test:Config {}
function testFunctionInvocationWithSingleNamedArgAndWithoutRestArg() {
    test:assertEquals(funcWithNamedAndRestParams(a, j = c, b), a + b + c + INT_CONST,
        msg = EXPECTED_RETURN_VALUE_TO_BE_EQUAL_TO_SUM_FAILURE_MESSAGE);
}

@test:Config {}
function testFunctionInvocationWithAllNamedArgsAndWithoutRestArg() {
    test:assertEquals(funcWithNamedAndRestParams(a, j = c, b, k = d), a + b + c + d,
        msg = EXPECTED_RETURN_VALUE_TO_BE_EQUAL_TO_SUM_FAILURE_MESSAGE);
}

@test:Config {}
function testFunctionInvocationWithRestArgsAndWithoutNamedArgs() {
    test:assertEquals(funcWithNamedAndRestParams(a, b, e, f), a + b + e + f + INT_CONST,
        msg = EXPECTED_RETURN_VALUE_TO_BE_EQUAL_TO_SUM_FAILURE_MESSAGE);

    int[] arr = [e, f];
    test:assertEquals(funcWithNamedAndRestParams(a, b, ...arr), a + b + e + f + INT_CONST,
        msg = EXPECTED_RETURN_VALUE_TO_BE_EQUAL_TO_SUM_FAILURE_MESSAGE);
}

@test:Config {}
function testFunctionInvocationWithAllArgs() {
    test:assertEquals(funcWithNamedAndRestParams(a, b, j = c, k = d, e, f), a + b + c + d + e + f,
        msg = EXPECTED_RETURN_VALUE_TO_BE_EQUAL_TO_SUM_FAILURE_MESSAGE);
}

function funcWithNamedAndRestParams(int i, int j = 0, int k = 10, int l, int... m) returns int {
    int sum = i + j + k + l;
    foreach int intVal in m {
        sum += intVal;
    }
    return sum;
}
