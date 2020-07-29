// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import ballerina/lang.test as test;

function testMax(int n, int... ns) returns int {
    return ints:max(n, ...ns);
}

function testMaxDataProvider(){
    test:assertEquals(testMax(54, 23, 34), 54);
    test:assertEquals(testMax(23, 34, 47), 47);
    test:assertEquals(testMax(-1, -20, -4), -1);
}

function testMin(int n, int... ns) returns int {
    return ints:min(n, ...ns);
}

function testMinDataProvider(){
    test:assertEquals(testMin(54, 23, 34), 23);
    test:assertEquals(testMin(23, 34, 47), 23);
    test:assertEquals(testMin(-1, -20, -4), -20);
}

function testFromString() {
    int|error v1 = ints:fromString("123");
    int|error v2 = ints:fromString("12invalid34");
    test:assertEquals(<int>v1, 123);
    test:assertError(v2);
    test:assertEquals(v2.toString(), "error {ballerina/lang.int}NumberParsingError message='string' value '12invalid34' cannot be converted to 'int'");
}

function testSum() returns int {
    return ints:sum(10, 25, 35, 40);
}

function testAbs() {
    int x = -123;
    int y = 234;
    test:assertEquals(x.abs(), 123);
    test:assertEquals(y.abs(), 234);
}

function testToHexString() {
    int x = 123456789;
    int y = -12345;
    test:assertEquals(x.toHexString(), "75bcd15");
    test:assertEquals(y.toHexString(), "ffffffffffffcfc7");
}

function testFromHexString() {
    int|error v1 = ints:fromHexString("aBCd45");
    int|error v2 = ints:fromHexString("12invalid34");
    test:assertEquals(<int>v1, 11259205);
    test:assertError(v2);
    test:assertEquals(v2.toString(), "error {ballerina/lang.int}NumberParsingError message=For input string: \"12invalid34\"");
}
