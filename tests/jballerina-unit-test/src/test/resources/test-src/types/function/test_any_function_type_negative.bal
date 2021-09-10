// Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

function bar(int b) returns int {
    return b;
}

isolated function bar2(int b) returns int {
    return b;
}

function baz = bar;

isolated function baz2 = bar2;

function functionWithFunctionReturnType() returns function {
    string a = "hello";
    return a;
}

function testFunctionTypedescWithIsolatedQualifier() {
    isolated function func = bar;
}

function testFunctionTypedescWithTransactionalQualifier() {
    transactional function func;
}

function functionPointerInvocation() {
    function func = bar;
    function (int) returns string val = func();
    function (int) returns string val2 = func(2, 2, "name");
}

function testFunctionTypedesc1() {
    function f = (i, j) => i + j;
}

function testFunctionWithNeverOrNeverEqualReturnType() {
    function () returns string x2 = blowUp1;
}

function blowUp1() returns int|never {
    panic error("Error!");
}

function testAnyFunction() {
    var v = <object {function a;}> object {
                function a = 1;
    };
}
