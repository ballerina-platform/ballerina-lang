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

// annots type-descriptor param-name [= default-value]
// default-value := const-expr
// TODO: allow any const-expr as the default value for a defaultable param
// https://github.com/ballerina-platform/ballerina-lang/issues/13187
@test:Config {
    groups: ["deviation"]
}
function testFunctionDefaultableParamsBroken() {
    // TODO: add tests for the different types of const-expr
    // e.g., `functionWithDefaultableArrayParam`
}

// function functionWithDefaultableArrayParam(int[] arr = [1, 2]) {
// }

// A function value declared as returning type T will belong to a
// function type declared as returning type T’ only if T is a subset of T’.
// TODO: fix is check failing for function pointers with union type params/return types
// https://github.com/ballerina-platform/ballerina-lang/issues/13179
@test:Config {
    groups: ["deviation"]
}
function testFunctionBelongsToByReturnTypeBroken() {
    any func = funcReturningString;
    // test:assertTrue(func is (function() returns string|int), 
    //                   msg = "expected function to be of expected type");
    test:assertTrue(func is (function() returns string), msg = "expected function to be of expected type");
    any execResult = funcReturningString();
    test:assertTrue(execResult is string|int, msg = "expected result to be of expected type");
}

function funcReturningString() returns string  {
    return "string";
}
