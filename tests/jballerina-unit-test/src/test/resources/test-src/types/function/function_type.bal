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

type Foo record {|
    int val;
|};

int gVal = 10;

function testFunctionTypewithRequiredParams() {
    function(int a, int b, int c) returns int func = bar;
    int val = func(10, 10, 10);
    assertEquality(30, val);
}

function testFunctionTypewithDefaultParams() {
    function(int a, int b = 10, int c = 15) returns int func = bar;
    int val = func(10);
    assertEquality(35, val);
}

function testFunctionTypewithDefaultParams2() {
    function(int a = 30, int b = 10, int c = 15) returns int func = bar;
    int val1 = func();
    int val2 = func(2);
    int val3 = func(2, 3);
    assertEquality(55, val1);
    assertEquality(27, val2);
    assertEquality(20, val3);
}

function testFunctionTypewithDefaultParams3() {
    int k = 10;
    int l = 20;
    int m = 30;
    function(int a = k + l, int b = l + m, int c = k + m) returns int func = bar;
    int val1 = func();
    int val2 = func(2);
    int val3 = func(2, 3);
    assertEquality(120, val1);
    assertEquality(92, val2);
    assertEquality(45, val3);
}

function(int a = 13, int l = 12, int c = 15) returns int gFunc1 = bar;

int gVar2 = 10;

function(int a = gVar2, int l = gVar2 + gVar3, int c = 15) returns int gFunc2 = bar;

function(int a = gVar2, int l = gVar2 + gVar3 + a, int c = 15) returns int gFunc3 = bar;


function testGlobalVarWithFunctionType() {
    int k = gFunc1();
    int l = gFunc2();
    int m = gFunc3();
    
    int n = gFunc1(100);
    int o = gFunc2(100, 100);
    int p = gFunc3(100);
    int q = gFunc4(100, 100);

    int r = gFunc4(100, 100, 100);

    assertEquality(40, k);
    assertEquality(60, l);
    assertEquality(70, m);
    assertEquality(127, n);
    assertEquality(215, o);
    assertEquality(250, p);
    assertEquality(400, q);
    assertEquality(300, r);
}

function testFunctionType() {
    testFunctionTypewithRequiredParams();
    testFunctionTypewithDefaultParams();
    testFunctionTypewithDefaultParams2();
    testFunctionTypewithDefaultParams3();
    testGlobalVarWithFunctionType();

}

function(int a = gVar2, int l = gVar2 + gVar3 + a, int c = a + l) returns int gFunc4 = bar;

int gVar3 = 25;

function bar(int a, int b, int c) returns int {
    return a + b + c;    
}

const ASSERTION_ERROR_REASON = "AssertionError";

function assertEquality(any expected, any actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    panic error(ASSERTION_ERROR_REASON,
                message = "expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}
