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

function testSumFunctionInDecimal(decimal p1, decimal p2) returns decimal {
    return 'decimal:sum(p1, p2);
}

function testOneArgMaxFunctionInDecimal(decimal arg) returns decimal {
    return 'decimal:max(arg);
}

function testSumFunctionInFloat() returns float {
    return 'float:sum(12.34, 23.45, 34.56);
}

function testFloatConsts() returns float {
    return 'float:NaN;
}

function testSumFunctionInInt() returns int {
    return 'int:sum(10, 25, 35, 40);
}

function testMaxFunctionInInt(int n, int... ns) returns int {
    return 'int:max(n, ...ns);
}

string str = "Hello Ballerina!";

function testSubString() returns string {
    return 'string:substring(str,6);
}

function testStartsWithFunctionInString() returns boolean {
    return 'string:startsWith(str, "Hello");
}

function testPredeclaredModules() {
    decimal d1 = 22.0;
    decimal d2 = 5.7;
    float f1 = 70.35;
    string s1 = "true";
    string s2 = "Ballerina!";
    assertEquality(d1, testSumFunctionInDecimal(10.5, 11.5));
    assertEquality(d2, testOneArgMaxFunctionInDecimal(5.7));
    assertEquality(f1, testSumFunctionInFloat());
    assertEquality(true, testFloatConsts().isNaN());
    assertEquality(110, testSumFunctionInInt());
    assertEquality(15, testMaxFunctionInInt(10,15));
    assertEquality(true, 'boolean:fromString(s1));
    assertEquality(s2, testSubString());
    assertEquality(true, testStartsWithFunctionInString());
}

function testPredeclaredModules2() {
    decimal d1 = 10.5;
    assertEquality(15, testMax(10, 15));
    assertEquality(10, testMin(10, 15));
    assertEquality(20, testMax1(15, 20));
    assertEquality(15, testMin1(15, 20));
    assertEquality(d1, testSum1(5, 5.5));
    assertEquality(10, testOneArgMax(10));
    assertEquality(d1, testSum2(5, 5.5));
    assertEquality(false, testStartsWith());
    assertEquality("Hello from Ballerina",testStringConcat());
    assertEquality("Hi Chiran Sachintha", testStringConcat1());
    assertEquality(70.35, testSumFunctionInFloat1());
    assertEquality(true, testFloatConsts1().isNaN());
    assertEquality(34.56, testMaxFunctionInFloat1());
}

function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    panic error("expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}
