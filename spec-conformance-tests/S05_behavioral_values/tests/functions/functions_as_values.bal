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

const EXPECTED_VALUE_OF_I_PLUS_J_FAILURE_MESSAGE = "expected value to be equal to addition of i and j";

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
function testExplicitFunctionExecution() {
    int i = 1;
    int j = 2;

    int r1 = add(i, j);

    test:assertEquals(r1, i + j, msg = EXPECTED_VALUE_OF_I_PLUS_J_FAILURE_MESSAGE);
}

@test:Config {}
function testFunctionValuePassing() {
    int i = 1;
    int j = 2;

    int r2 = execFunction(getAddFunction(), i, j);

    test:assertEquals(r2, i + j, msg = EXPECTED_VALUE_OF_I_PLUS_J_FAILURE_MESSAGE);
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
