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

import ballerina/lang.'int as ints;

function testMultiplicativeExpr() {
    int res = a * b;
    res = (a * getOp());
    res = b / a;
    res = getOp() % b;
}

function testAdditiveExpr() {
    int res = a + b;
    res = getOp() + b;
    res = a - b;
}

function testShiftExpr() {
    ints:Unsigned8 shift = 8;
    int res = a << shift;
    res = a >> 4;
    res = shift >>> 2;
}

function testRangeExpr() {
    ints:Signed16 x = 256;
    var v1 = x...1024
    var v2 = b...x;
    var v3 = a..<b;
}

function testRelationalExpr() {
    boolean res = a > b;
    res = a < 100;
    res = b <= a;
    res = b >= a;
}

function testEqualityExpr() {
    boolean res = a == b;
    res = a != 10;
    res = a === b;
    res = getOp() !=== b;
}

function testBitwiseExpr() {
    ints:Unsigned16 x = 16284;
    int res = x & getOp();
    res = x ^ 2048;
    res = 12345 | x;
}

function testLogicalExpr() {
    boolean expr = 10 < 20;
    boolean res = expr && true;
    res = (getOp() > 15) || expr;
}

// utils

int a = 10;
const b = 30;

function getOp() returns int => 30;
