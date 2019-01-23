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
import utils;

// A member of a list can be referenced by an integer index representing its position in the list.
// For a list of length n, the indices of the members of the list, from first to last, are 0,1,...,n - 1.

@test:Config {}
function testBasicTypeTupleMemberReferenceByValidIntegerIndex() {
    string a1 = "test string 1";
    string a2 = "test string 2";
    int a3 = 100;
    int a4 = 200;
    boolean a5 = true;
    int a6 = 300;
    (string, string, int, int, boolean) tuple = (a1, a2, a3, a4, a5);

    string b1 = tuple[0];
    string b2 = tuple[1];
    int b3 = tuple[2];
    int b4 = tuple[3];
    boolean b5 = tuple[4];

    test:assertEquals(b1, a1, msg = EXPECTED_VALUE_NOT_FOUND_AT_INDEX_FAILURE_MESSAGE + "0");
    test:assertEquals(b2, a2, msg = EXPECTED_VALUE_NOT_FOUND_AT_INDEX_FAILURE_MESSAGE + "1");
    test:assertEquals(b3, a3, msg = EXPECTED_VALUE_NOT_FOUND_AT_INDEX_FAILURE_MESSAGE + "2");
    test:assertEquals(b4, a4, msg = EXPECTED_VALUE_NOT_FOUND_AT_INDEX_FAILURE_MESSAGE + "3");
    test:assertEquals(b5, a5, msg = EXPECTED_VALUE_NOT_FOUND_AT_INDEX_FAILURE_MESSAGE + "4");

    tuple[2] = a6;
    int b6 = tuple[2];
    test:assertEquals(b6, a6, msg = EXPECTED_VALUE_NOT_FOUND_AT_INDEX_FAILURE_MESSAGE + "2");
}

@test:Config {}
function testRecordTupleMemberReferenceByValidIntegerIndex() {
    utils:FooRecord a7 = { fooFieldOne: "valueOne" };
    utils:BarRecord a8 = { barFieldOne: 100 };
    utils:FooRecord a9 = { fooFieldOne: "valueTwo" };
    utils:BarRecord a10 = { barFieldOne: 50 };
    (utils:FooRecord, utils:BarRecord, utils:FooRecord, utils:BarRecord) tuple2 = (a7, a8, a9, a10);

    utils:FooRecord b7 = tuple2[0];
    utils:BarRecord b8 = tuple2[1];
    utils:FooRecord b9 = tuple2[2];
    utils:BarRecord b10 = tuple2[3];

    test:assertEquals(b7, a7, msg = EXPECTED_VALUE_NOT_FOUND_AT_INDEX_FAILURE_MESSAGE + "0");
    test:assertEquals(b8, a8, msg = EXPECTED_VALUE_NOT_FOUND_AT_INDEX_FAILURE_MESSAGE + "1");
    test:assertEquals(b9, a9, msg = EXPECTED_VALUE_NOT_FOUND_AT_INDEX_FAILURE_MESSAGE + "2");
    test:assertEquals(b10, a10, msg = EXPECTED_VALUE_NOT_FOUND_AT_INDEX_FAILURE_MESSAGE + "3");
}

@test:Config {}
function testObjectTupleMemberReferenceByValidIntegerIndex() {
    utils:FooObject a11 = new("valueOne");
    utils:BarObject a12 = new(200);
    utils:FooObject a13 = new("valueTwo");
    utils:BarObject a14 = new(180);

    (utils:FooObject, utils:BarObject, utils:FooObject, utils:BarObject) tuple3 = (a11, a12, a13, a14);

    utils:FooObject b11 = tuple3[0];
    utils:BarObject b12 = tuple3[1];
    utils:FooObject b13 = tuple3[2];
    utils:BarObject b14 = tuple3[3];

    test:assertEquals(b11, a11, msg = EXPECTED_VALUE_NOT_FOUND_AT_INDEX_FAILURE_MESSAGE + "0");
    test:assertEquals(b12, a12, msg = EXPECTED_VALUE_NOT_FOUND_AT_INDEX_FAILURE_MESSAGE + "1");
    test:assertEquals(b13, a13, msg = EXPECTED_VALUE_NOT_FOUND_AT_INDEX_FAILURE_MESSAGE + "2");
    test:assertEquals(b14, a14, msg = EXPECTED_VALUE_NOT_FOUND_AT_INDEX_FAILURE_MESSAGE + "3");
}
