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

import ballerina/lang.array;

function testBoundRestParamAsCET() {
    testNumericTypeAsBoundRestParam();
    testStructedTypeAsBoundRestParam();
}

function testNumericTypeAsBoundRestParam() {
    byte[] bytes = [];
    bytes.push(0);
    bytes.push(1, 2);

    byte[] arr = [3, 4];
    bytes.push(...arr);

    array:unshift(bytes, 255);
    array:unshift(bytes, 254, 253);

    byte[] arr2 = [252, 251];
    array:unshift(bytes, ...arr2);

    assertValueEquality(10, bytes.length());

    assertValueEquality(252, bytes[0]);
    assertValueEquality(251, bytes[1]);
    assertValueEquality(254, bytes[2]);
    assertValueEquality(253, bytes[3]);
    assertValueEquality(255, bytes[4]);
    assertValueEquality(0, bytes[5]);
    assertValueEquality(1, bytes[6]);
    assertValueEquality(2, bytes[7]);
    assertValueEquality(3, bytes[8]);
    assertValueEquality(4, bytes[9]);
}

function testStructedTypeAsBoundRestParam() {
    [function() returns int, string][] funcs = [];
    funcs.push([function () returns int => 1, "a"]);
    funcs.push([function () returns int => 2, "b"], [function () returns int => 3, "c"]);

    [function() returns int, string][] arr = [[function () returns int => 4, "d"], [function () returns int => 5, "e"]];
    funcs.push(...arr);

    array:push(funcs, [function () returns int => 6, "f"]);
    array:push(funcs, [function () returns int => 7, "g"], [function () returns int => 8, "h"]);

    [function() returns int, string][] arr2 = [[function () returns int => 9, "i"], 
                                               [function () returns int => 10, "j"]];
    array:push(funcs, ...arr2);

    assertValueEquality(10, funcs.length());

    int sum = 0;
    string concat = "";

    foreach [function() returns int, string] [func, str] in funcs {
        sum += func();
        concat += str;
    }

    assertValueEquality(55, sum);
    assertValueEquality("abcdefghij", concat);
}

function testIntLiteralAsByte() {
    byte[] b = [0, 1, 2, 100, 101, 255];
    b.push(4);

    assertValueEquality(0, b.indexOf(0));
    assertValueEquality(5, b.indexOf(255));
    assertValueEquality(3, b.indexOf(100));
    assertValueEquality(6, b.indexOf(4));
    assertValueEquality((), b.indexOf(102));

    map<byte> m = {
        a: 3,
        b: 5,
        c: 1
    };

    int f = m.reduce(function (byte x, byte y) returns byte {
                         int res = x * y;
                         return <byte> res;
                     }, 2);
    assertValueEquality(30, f);
}

function testIntAndFloatLiteralAsDecimal() {
    decimal[] d = [0, 1.0, 2, 10.01, 101, 2550.1d];
    d.push(4);
    d.push(23.2);

    assertValueEquality(0, d.indexOf(0));
    assertValueEquality(1, d.indexOf(1));
    assertValueEquality(5, d.indexOf(2550.1));
    assertValueEquality(7, d.indexOf(23.2));
    assertValueEquality((), d.indexOf(10.2));

    map<decimal> m = {
        a: 30,
        b: 500.1,
        c: 1.01d
    };

    float f = m.reduce(function (float x, decimal y) returns float {
                           int xi = <int> x;
                           int yi = <int> y;
                           return <float> (xi + yi);
                       }, 2);
    assertValueEquality(533.0, f);
}

function testBroadTypeAsCET() {
    any[] arr = [];

    arr.push("foo");
    arr.push([1, "bar", 2.0]);
    arr.push({a: "abc", b: 1, c: true});

    assertValueEquality(3, arr.length());

    assertTrue(arr[0] is string);
    assertValueEquality("foo", <string> arr[0]);

    assertTrue(arr[1] is (any|error)[]);

    (any|error)[] anyArr = <(any|error)[]> arr[1];

    any|error val = anyArr[0];
    assertTrue(val is int && val == 1);

    val = anyArr[1];
    assertTrue(val is string && val == "bar");

    val = anyArr[2];
    assertTrue(val is float && val == 2.0);

    assertTrue(arr[2] is map<any|error>);

    map<any|error> anyMap = <map<any|error>> arr[2];

    val = anyMap["a"];
    assertTrue(val is string && val == "abc");

    val = anyMap["b"];
    assertTrue(val is int && val == 1);

    val = anyMap["c"];
    assertTrue(val is boolean && val);
}

const ASSERTION_ERROR_REASON = "AssertionError";

function assertTrue(any|error actual) {
    if actual is boolean && actual {
        return;
    }

    string actualValAsString = "";
    if (actual is error) {
        actualValAsString = actual.toString();
    } else {
        actualValAsString = actual.toString();
    }

    panic error(ASSERTION_ERROR_REASON,
                message = "expected 'true', found '" + actualValAsString + "'");
}

function assertValueEquality(anydata|error expected, anydata|error actual) {
    if isEqual(expected, actual) {
        return;
    }

    string expectedValAsString = expected is error ? expected.toString() : expected.toString();
    string actualValAsString = actual is error ? actual.toString() : actual.toString();
    panic error(ASSERTION_ERROR_REASON,
                message = "expected '" + expectedValAsString + "', found '" + actualValAsString + "'");
}

isolated function isEqual(anydata|error val1, anydata|error val2) returns boolean {
    return val1 is anydata && val2 is anydata && val1 == val2 || val1 === val2;
}
