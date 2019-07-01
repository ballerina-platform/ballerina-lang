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

const EXPECTED_VALUES_TO_BE_UNCHANGED_FAILURE_MESSAGE = "expected values to be unchanged";

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
function testFunctionWithAnnotatedParam() {
    string s1 = "test string 1";
    string s2 = funcWithAnnotatedParam(s1);
    test:assertEquals(s1, s2, msg = EXPECTED_VALUES_TO_BE_UNCHANGED_FAILURE_MESSAGE);
}

@test:Config {}
function testFunctionWithAnnotatedReturn() {
    string s1 = "test string 1";
    string s2 = funcWithAnnotatedReturn(s1);
    test:assertEquals(s1, s2, msg = EXPECTED_VALUES_TO_BE_UNCHANGED_FAILURE_MESSAGE);
}

function funcWithAnnotatedParam(@sensitive string s) returns string {
    return s;
}

function funcWithAnnotatedReturn(string s) returns @untainted string {
    return untaint s;
}
