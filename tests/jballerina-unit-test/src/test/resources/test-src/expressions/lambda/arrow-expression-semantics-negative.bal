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

function testInvalidUsageOfParamType() returns int {
    function (string) returns int lambda = param1 => param1/2; // operator '/' not defined for 'string' and 'int'
    return lambda("12");
}

function testInvalidParamType() returns boolean {
    function (boolean) returns boolean lambda = param1 => !param1;
    return lambda("false"); // incompatible types: expected 'boolean', found 'string'
}

function testInvalidNumberOfParams() {
    // invalid number of parameters used in arrow expression. expected: '2' but found '1'
    function (boolean, string) returns boolean lambda1 = param1 => !param1;
    // invalid number of parameters used in arrow expression. expected: '1' but found '2'
    function (boolean) returns boolean lambda2 = (param1, param2) => !param1;
}

function testInvalidReturnType() {
    function (boolean) returns int lambda1 = param1 => !param1; // incompatible types: expected 'int', found 'boolean'
}

function testWithUnknownExpectedType() {
    var lambda1 = param1 => !param1; // arrow expression can only be defined with known invokable types
    any lambda2 = param1 => !param1; // arrow expression can only be defined with known invokable types
}

function testArrowExprVariableScope() {
    function (boolean) returns boolean lambda1 = param1 => !param1;
    param1 = 12; // undefined symbol 'param1'
}

function testArrowExprVariableScope2() {
    int param1 = 12;
    function (boolean) returns boolean lambda1 = param1 => !param1; // redeclared symbol 'param1'
}

function testAccessInvalidClosure() {
    function (boolean) returns boolean lambda1 = param1 => !closureVar; // undefined symbol 'closureVar'
    boolean closureVar = true;
}

function() foo = function () returns () {
    int i = 34;
    function (int) returns (int) addFunc1 = a => a + p + m + i; // undefined symbol 'm'
    int m = 3;
    int k = addFunc1(6);
};

int p = 2;

function testArrowExprWithNoInputs() {
    function() returns string lambda = (i) => i + p;
}

function testArrowExprWithUninitializedClosureVars() {
    int i;
    int m;
    function (int) returns (int) addFunc1 = a => a + i + m ;
}

function testInvalidBinaryExprInExprFuncBody() {
    _ = ["foo","bar","baz"].map(s => s + 1);

    int[] intArr = [1, 2];
    _ = intArr.map(i => "s" + i);

    string[] strArr = [];
    _ = strArr.map(s => 1 + s);

    [int, int] intTup = [1, 2];
    _ = intTup.map(i => "s" + i);

    [string] strTup = ["str"];
    _ = strTup.map(s => 1 + s);

    _ = [].map(m => m + 1);

    // [] emptyTup = [];
    // _ = emptyTup.map(m => 1 + m);
}
