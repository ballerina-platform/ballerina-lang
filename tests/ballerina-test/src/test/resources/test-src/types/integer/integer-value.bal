//
// Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
//

function testIntegerValue () (int) {
    int b;
    b = 10;
    return b;
}

function testNegativeIntegerValue () (int) {
    int y;
    y = -10;
    return y;
}

function testHexValue () (int) {
    int b;
    b = 0xa;
    return b;
}

function testNegativeHaxValue () (int) {
    int b;
    b = -0xa;
    return b;
}

function testOctalValue () (int) {
    int b;
    b = 0_12;
    return b;
}

function testNegativeOctalValue () (int) {
    int b;
    b = -0_12;
    return b;
}

function testBinaryValue () (int) {
    int b;
    b = 0b1010;
    return b;
}

function testNegativeBinaryValue () (int) {
    int b;
    b = -0b1010;
    return b;
}

function testIntegerValueAssignmentByReturnValue () (int) {
    int x;
    x = testIntegerValue();
    return x;
}

function testIntegerAddition () (int) {
    int b;
    int a;
    a = 9;
    b = 10;
    return a + b;
}

function testIntegerMultiplication () (int) {
    int b;
    int a;
    a = 2;
    b = 5;
    return a * b;
}

function testIntegerSubtraction () (int) {
    int b;
    int a;
    a = 25;
    b = 15;
    return a - b;
}

function testIntegerDivision () (int) {
    int b;
    int a;
    a = 25;
    b = 5;
    return a / b;
}

function testIntegerParameter (int a) (int) {
    int b;
    b = a;
    return b;
}
