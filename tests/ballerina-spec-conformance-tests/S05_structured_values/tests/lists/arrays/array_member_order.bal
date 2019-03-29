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

const EXPECTED_VALUE_NOT_FOUND_AT_INDEX_FAILURE_MESSAGE = "expected value not found at index ";

// A list value is a container that keeps its members in an ordered list.

@test:Config {}
function testIntArrayMemberOrder() {
    int a1 = 30;
    int a2 = 10;
    int a3 = 20;
    int a4 = 5;
    int a5 = 11;
    int[] intArray = [a1, a2, a3];

    test:assertEquals(intArray[0], a1, msg = EXPECTED_VALUE_NOT_FOUND_AT_INDEX_FAILURE_MESSAGE + "0");
    test:assertEquals(intArray[1], a2, msg = EXPECTED_VALUE_NOT_FOUND_AT_INDEX_FAILURE_MESSAGE + "1");
    test:assertEquals(intArray[2], a3, msg = EXPECTED_VALUE_NOT_FOUND_AT_INDEX_FAILURE_MESSAGE + "2");

    intArray[3] = a4;
    test:assertEquals(intArray[0], a1, msg = EXPECTED_VALUE_NOT_FOUND_AT_INDEX_FAILURE_MESSAGE + "0");
    test:assertEquals(intArray[1], a2, msg = EXPECTED_VALUE_NOT_FOUND_AT_INDEX_FAILURE_MESSAGE + "1");
    test:assertEquals(intArray[2], a3, msg = EXPECTED_VALUE_NOT_FOUND_AT_INDEX_FAILURE_MESSAGE + "2");
    test:assertEquals(intArray[3], a4, msg = EXPECTED_VALUE_NOT_FOUND_AT_INDEX_FAILURE_MESSAGE + "3");

    intArray[0] = a5;
    test:assertEquals(intArray[0], a5, msg = EXPECTED_VALUE_NOT_FOUND_AT_INDEX_FAILURE_MESSAGE + "0");
    test:assertEquals(intArray[1], a2, msg = EXPECTED_VALUE_NOT_FOUND_AT_INDEX_FAILURE_MESSAGE + "1");
    test:assertEquals(intArray[2], a3, msg = EXPECTED_VALUE_NOT_FOUND_AT_INDEX_FAILURE_MESSAGE + "2");
    test:assertEquals(intArray[3], a4, msg = EXPECTED_VALUE_NOT_FOUND_AT_INDEX_FAILURE_MESSAGE + "3");
}

@test:Config {}
function testRecordArrayMemberOrder() {
    FooRecordThree a6 = { fooFieldOne: "test string 1" };
    FooRecordThree a7 = { fooFieldOne: "test string 2" };
    FooRecordThree a8 = { fooFieldOne: "test string 3" };
    FooRecordThree a9 = { fooFieldOne: "test string 4" };
    FooRecordThree a10 = { fooFieldOne: "test string 5" };
    FooRecordThree a11 = { fooFieldOne: "test string 6" };
    FooRecordThree[] fooRecordArray = [a6, a7, a8, a9];

    test:assertEquals(fooRecordArray[0], a6, msg = EXPECTED_VALUE_NOT_FOUND_AT_INDEX_FAILURE_MESSAGE + "0");
    test:assertEquals(fooRecordArray[1], a7, msg = EXPECTED_VALUE_NOT_FOUND_AT_INDEX_FAILURE_MESSAGE + "1");
    test:assertEquals(fooRecordArray[2], a8, msg = EXPECTED_VALUE_NOT_FOUND_AT_INDEX_FAILURE_MESSAGE + "2");
    test:assertEquals(fooRecordArray[3], a9, msg = EXPECTED_VALUE_NOT_FOUND_AT_INDEX_FAILURE_MESSAGE + "3");

    fooRecordArray[4] = a10;
    test:assertEquals(fooRecordArray[0], a6, msg = EXPECTED_VALUE_NOT_FOUND_AT_INDEX_FAILURE_MESSAGE + "0");
    test:assertEquals(fooRecordArray[1], a7, msg = EXPECTED_VALUE_NOT_FOUND_AT_INDEX_FAILURE_MESSAGE + "1");
    test:assertEquals(fooRecordArray[2], a8, msg = EXPECTED_VALUE_NOT_FOUND_AT_INDEX_FAILURE_MESSAGE + "2");
    test:assertEquals(fooRecordArray[3], a9, msg = EXPECTED_VALUE_NOT_FOUND_AT_INDEX_FAILURE_MESSAGE + "3");
    test:assertEquals(fooRecordArray[4], a10, msg = EXPECTED_VALUE_NOT_FOUND_AT_INDEX_FAILURE_MESSAGE + "4");

    fooRecordArray[1] = a11;
    test:assertEquals(fooRecordArray[0], a6, msg = EXPECTED_VALUE_NOT_FOUND_AT_INDEX_FAILURE_MESSAGE + "0");
    test:assertEquals(fooRecordArray[1], a11, msg = EXPECTED_VALUE_NOT_FOUND_AT_INDEX_FAILURE_MESSAGE + "1");
    test:assertEquals(fooRecordArray[2], a8, msg = EXPECTED_VALUE_NOT_FOUND_AT_INDEX_FAILURE_MESSAGE + "2");
    test:assertEquals(fooRecordArray[3], a9, msg = EXPECTED_VALUE_NOT_FOUND_AT_INDEX_FAILURE_MESSAGE + "3");
    test:assertEquals(fooRecordArray[4], a10, msg = EXPECTED_VALUE_NOT_FOUND_AT_INDEX_FAILURE_MESSAGE + "4");
}

@test:Config {}
function testObjectArrayMemberOrder() {
    FooObjectThree a12 = new("test string 1");
    FooObjectThree a13 = new("test string 2");
    FooObjectThree a14 = new("test string 3");
    FooObjectThree a15 = new("test string 4");
    FooObjectThree?[] fooObjectArray = [a12, a13];

    test:assertEquals(<FooObjectThree>fooObjectArray[0], a12, msg = EXPECTED_VALUE_NOT_FOUND_AT_INDEX_FAILURE_MESSAGE + "0");
    test:assertEquals(<FooObjectThree>fooObjectArray[1], a13, msg = EXPECTED_VALUE_NOT_FOUND_AT_INDEX_FAILURE_MESSAGE + "1");

    fooObjectArray[2] = a14;
    test:assertEquals(<FooObjectThree>fooObjectArray[0], a12, msg = EXPECTED_VALUE_NOT_FOUND_AT_INDEX_FAILURE_MESSAGE + "0");
    test:assertEquals(<FooObjectThree>fooObjectArray[1], a13, msg = EXPECTED_VALUE_NOT_FOUND_AT_INDEX_FAILURE_MESSAGE + "1");
    test:assertEquals(<FooObjectThree>fooObjectArray[2], a14, msg = EXPECTED_VALUE_NOT_FOUND_AT_INDEX_FAILURE_MESSAGE + "2");

    fooObjectArray[0] = a15;
    test:assertEquals(<FooObjectThree>fooObjectArray[0], a15, msg = EXPECTED_VALUE_NOT_FOUND_AT_INDEX_FAILURE_MESSAGE + "0");
    test:assertEquals(<FooObjectThree>fooObjectArray[1], a13, msg = EXPECTED_VALUE_NOT_FOUND_AT_INDEX_FAILURE_MESSAGE + "1");
    test:assertEquals(<FooObjectThree>fooObjectArray[2], a14, msg = EXPECTED_VALUE_NOT_FOUND_AT_INDEX_FAILURE_MESSAGE + "2");
}

public type FooRecordThree record {|
    string fooFieldOne;
|};

public type FooObjectThree object {
    public string fooFieldOne;

    public function __init(string fooFieldOne) {
        self.fooFieldOne = fooFieldOne;
    }

    public function getFooFieldOne() returns string {
        return self.fooFieldOne;
    }
};
