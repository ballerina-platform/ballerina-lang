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

import ballerina/test;

# When executing tests, the tests related to assertTrue should be executed
# first since the other tests use the assertTrue function.
# The tests labelled with group 'p1' therefore should be executed first.

@test:Config {
    groups: ["p1"]
}
function testAssertTrue() {
    test:assertTrue(true, msg = "assertTrue failed");
}

@test:Config {
    groups: ["p1"]
}
function testAssertTrueNegative() {
    error? err = trap test:assertTrue(false, msg = "assertTrue failed");
    test:assertTrue(err is error);
}

@test:Config {}
function testAssertFalse() {
    test:assertFalse(false, msg = "assertFalse failed");
}

@test:Config {}
function testAssertFalseNegative() {
    error? err = trap test:assertFalse(true, msg = "assertFalse failed");
    test:assertTrue(err is error);
}

function testAssertFail() {
    error? err = trap test:assertFail(msg = "assertFailed");
    test:assertTrue(err is error);
    error result = <error>err;
    test:assertTrue(result.message().toString().startsWith("assertFailed"));
}

@test:Config {}
function testAssertNilEquals() {
    () expected = ();
    test:assertEquals((), expected);
}

@test:Config {}
function testAssertNilEqualsNegative() {
    error? err = trap test:assertEquals(false, ());
    test:assertTrue(err is error);
}

@test:Config {}
function testAssertIntEquals() {
    int answer = 0;
    answer = intAdd(6, 2);
    test:assertEquals(answer, 8, msg = "intAdd function failed");
}

@test:Config {}
function testAssertIntEqualsNegative() {
    int answer = 0;
    answer = intAdd(6, 2);
    error? err = trap test:assertEquals(answer, 9, msg = "intAdd function failed");
    test:assertTrue(err is error);
}

@test:Config {}
function testAssertFloatEquals() {
    float float1 = 10.000;
    float float2 = 20.050;
    float answer = floatAdd(float1, float2);
    test:assertEquals(answer, 30.050);
}

@test:Config {}
function testAssertFloatEqualsNegative() {
    float float1 = 10.000;
    float float2 = 20.050;
    float answer = floatAdd(float1, float2);
    error? err = trap test:assertEquals(answer, 40.050);
    test:assertTrue(err is error);
}

@test:Config {}
function testAssertDecimalEquals() {
    decimal d1 = 10.000;
    decimal d2 = 20.050;
    decimal answer = decimalAdd(d1, d2);
    decimal expected = 30.050;
    test:assertEquals(answer, expected);
}

@test:Config {}
function testAssertDecimalEqualsNegative() {
    decimal d1 = 10.000;
    decimal d2 = 20.050;
    decimal answer = decimalAdd(d1, d2);
    decimal expected = 40.05;
    error? err = trap test:assertEquals(answer, expected);
    test:assertTrue(err is error);
}

@test:Config {}
function testAssertJsonEquals() {
    json a = {"a": "b"};
    json b = {"a": "b"};
    test:assertEquals(a, b);
}

@test:Config {}
function testAssertJsonEqualsNegative() {
    json a = {"a": "b"};
    json b = {"b": "a"};
    error? err = trap test:assertEquals(a, b);
    test:assertTrue(err is error);
}

@test:Config {}
function testAssertBooleanEquals() {
    boolean a = true;
    test:assertEquals(a, true);
}

@test:Config {}
function testAssertBooleanEqualsNegative() {
    boolean a = true;
    error? err = trap test:assertEquals(a, false);
    test:assertTrue(err is error);
}

@test:Config {}
function testAssertByteEquals() {
    byte a = 10;
    byte b = 10;
    test:assertEquals(a, b);
}

@test:Config {}
function testAssertByteEqualsNegative() {
    byte a = 10;
    error? err = trap test:assertEquals(a, false);
    test:assertTrue(err is error);
}

@test:Config {}
function testAssertNilNotEquals() {
    () expected = ();
    test:assertNotEquals(false, expected);
}

@test:Config {}
function testAssertNilNotEqualsNegative() {
    () expected = ();
    error? err = trap test:assertNotEquals((), expected);
    test:assertTrue(err is error);
}

@test:Config {}
function testAssertIntNotEquals() {
    int num = 1;
    test:assertNotEquals(num, 11);
}

@test:Config {}
function testAssertIntNotEqualsNegative() {
    int num = 1;
    error? err = trap test:assertNotEquals(num, 1);
    test:assertTrue(err is error);
}

@test:Config {}
function testAssertFloatNotEquals() {
    float num = 1.1;
    test:assertNotEquals(num, 1.11);
}

@test:Config {}
function testAssertFloatNotEqualsNegative() {
    float num = 1.1;
    error? err = trap test:assertNotEquals(num, 1.1);
    test:assertTrue(err is error);
}

@test:Config {}
function testAssertDecimalNotEquals() {
    decimal num = 1.2;
    test:assertNotEquals(num, 1.22);
}

@test:Config {}
function testAssertDecimalNotEqualsNegative() {
    decimal num = 1.2;
    decimal expected = 1.20;
    error? err = trap test:assertNotEquals(num, expected);
    test:assertTrue(err is error);
}

@test:Config {}
function testAssertByteNotEquals() {
    byte a = 10;
    byte b = 11;
    test:assertNotEquals(a, b);
}

@test:Config {}
function testAssertByteNotEqualsNegative() {
    byte a = 10;
    error? err = trap test:assertNotEquals(a, 10);
    test:assertTrue(err is error);
}

@test:Config {}
function testAssertJsonNotEquals() {
    json a = {"a": "b"};
    json b = {"b": "a"};
    test:assertNotEquals(a, b);
}

@test:Config {}
function testAssertJsonNotEqualsNegative() {
    json a = {"a": "b"};
    json b = {"a": "b"};
    error? err = trap test:assertNotEquals(a, b);
    test:assertTrue(err is error);
}

@test:Config {}
function testAssertBooleanNotEquals() {
    boolean a = true;
    test:assertNotEquals(a, false);
}

@test:Config {}
function testAssertBooleanNotEqualsNegative() {
    boolean a = true;
    error? err = trap test:assertNotEquals(a, true);
    test:assertTrue(err is error);
}

@test:Config {}
function testAssertTypeNotEquals() {
    boolean a = true;
    test:assertNotEquals(a, "true");

    json obj = {"a": "b"};
    test:assertNotEquals(obj, "{\"a\" : \"b\"}");

    int num = 1;
    test:assertNotEquals(num, "1");

    [string|int, decimal, boolean] t1 = [1, 1.0, false];
    [int, float|string, boolean] t2 = [1, 1.0, false];
    test:assertNotEquals(t1, t2);

    float x = 1.1;
    decimal y = 1.1;
    (anydata|error)[] arr1 = ["test", "array", x];
    (anydata|error)[] arr2 = ["test", "array", y];
    test:assertNotEquals(arr1, arr2);
}

function intAdd(int a, int b) returns (int) {
    return a + b;
}

function floatAdd(float a, float b) returns (float) {
    return a + b;
}

function decimalAdd(decimal a, decimal b) returns (decimal) {
    return a + b;
}

function stringConcat(string a, string b) returns (string) {
    return a + b;
}
