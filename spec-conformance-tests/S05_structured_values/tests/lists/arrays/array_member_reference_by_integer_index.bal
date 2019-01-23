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

const string INDEX_OUT_OF_RANGE_REASON = "{ballerina}IndexOutOfRange";
const string INVALID_REASON_ON_ACCESS_BY_NEGATIVE_INDEX_FAILURE_MESSAGE = "invalid reason on access by negative index";
const string INVALID_REASON_ON_ACCESS_BY_ARRAY_LENGTH_INDEX_FAILURE_MESSAGE = 
                                                                "invalid reason on access by index == array length";
const string INVALID_REASON_ON_ACCESS_BY_LARGER_INDEX_FAILURE_MESSAGE = 
                                                                "invalid reason on access by index > array length";

// A member of a list can be referenced by an integer index representing its position in the list.
// For a list of length n, the indices of the members of the list, from first to last, are 0,1,...,n - 1.

@test:Config {}
function testStringArrayMemberReferenceByValidIntegerIndex() {
    string a1 = "test string 1";
    string a2 = "test string 2";
    string a3 = "test string 3";
    string a4 = "test string 4";
    string a5 = "test string 5";
    string a6 = "test string 6";
    string[] stringArray = [a1, a2, a3, a4, a5];

    string b1 = stringArray[0];
    string b2 = stringArray[1];
    string b3 = stringArray[2];
    string b4 = stringArray[3];
    string b5 = stringArray[4];

    test:assertEquals(b1, a1, msg = EXPECTED_VALUE_NOT_FOUND_AT_INDEX_FAILURE_MESSAGE + "0");
    test:assertEquals(b2, a2, msg = EXPECTED_VALUE_NOT_FOUND_AT_INDEX_FAILURE_MESSAGE + "1");
    test:assertEquals(b3, a3, msg = EXPECTED_VALUE_NOT_FOUND_AT_INDEX_FAILURE_MESSAGE + "2");
    test:assertEquals(b4, a4, msg = EXPECTED_VALUE_NOT_FOUND_AT_INDEX_FAILURE_MESSAGE + "3");
    test:assertEquals(b5, a5, msg = EXPECTED_VALUE_NOT_FOUND_AT_INDEX_FAILURE_MESSAGE + "4");

    stringArray[2] = a6;
    string b6 = stringArray[2];
    test:assertEquals(b6, a6, msg = EXPECTED_VALUE_NOT_FOUND_AT_INDEX_FAILURE_MESSAGE + "2");
}

@test:Config {}
function testRecordArrayMemberReferenceByValidIntegerIndex() {
    utils:FooRecord a7 = { fooFieldOne: "test string 0" };
    utils:FooRecord a8 = { fooFieldOne: "test string 1" };
    utils:FooRecord a9 = { fooFieldOne: "test string 2" };
    utils:FooRecord a10 = { fooFieldOne: "test string 3" };
    utils:FooRecord[] fooRecordArray = [a7, a8, a9];

    utils:FooRecord b7 = fooRecordArray[0];
    utils:FooRecord b8 = fooRecordArray[1];
    utils:FooRecord b9 = fooRecordArray[2];

    test:assertEquals(b7, a7, msg = EXPECTED_VALUE_NOT_FOUND_AT_INDEX_FAILURE_MESSAGE + "0");
    test:assertEquals(b8, a8, msg = EXPECTED_VALUE_NOT_FOUND_AT_INDEX_FAILURE_MESSAGE + "1");
    test:assertEquals(b9, a9, msg = EXPECTED_VALUE_NOT_FOUND_AT_INDEX_FAILURE_MESSAGE + "2");

    fooRecordArray[0] = a10;
    utils:FooRecord b10 = fooRecordArray[0];
    test:assertEquals(b10, a10, msg = EXPECTED_VALUE_NOT_FOUND_AT_INDEX_FAILURE_MESSAGE + "0");
}

@test:Config {}
function testObjectArrayMemberReferenceByValidIntegerIndex() {
    utils:FooObject a11 = new("test string 1");
    utils:FooObject a12 = new("test string 2");
    utils:FooObject a13 = new("test string 3");
    utils:FooObject[] fooObjectArray = [a11, a12];

    utils:FooObject b11 = fooObjectArray[0];
    utils:FooObject b12 = fooObjectArray[1];
    test:assertEquals(b11, a11, msg = EXPECTED_VALUE_NOT_FOUND_AT_INDEX_FAILURE_MESSAGE + "0");
    test:assertEquals(b12, a12, msg = EXPECTED_VALUE_NOT_FOUND_AT_INDEX_FAILURE_MESSAGE + "1");

    fooObjectArray[1] = a13;
    utils:FooObject b13 = fooObjectArray[1];
    test:assertEquals(b13, a13, msg = EXPECTED_VALUE_NOT_FOUND_AT_INDEX_FAILURE_MESSAGE + "1");
}

@test:Config {}
function testFloatArrayMemberReferenceByInvalidIntegerIndex() {
    float[] floatArray = [1.1, 0.0, 2.20];
    
    int index = -1;
    utils:assertErrorReason(trap floatArray[index], INDEX_OUT_OF_RANGE_REASON,
                            INVALID_REASON_ON_ACCESS_BY_NEGATIVE_INDEX_FAILURE_MESSAGE);
    
    index = floatArray.length();
    utils:assertErrorReason(trap floatArray[index], INDEX_OUT_OF_RANGE_REASON,
                            INVALID_REASON_ON_ACCESS_BY_ARRAY_LENGTH_INDEX_FAILURE_MESSAGE);
    
    index = floatArray.length() + 3;
    utils:assertErrorReason(trap floatArray[index], INDEX_OUT_OF_RANGE_REASON,
                            INVALID_REASON_ON_ACCESS_BY_LARGER_INDEX_FAILURE_MESSAGE);
}

@test:Config {}
function testRecordArrayMemberReferenceByInvalidIntegerIndex() {
    int index = -1;
    utils:BarRecord[] barRecordArray = [<utils:BarRecord>{ barFieldOne: 1 }, <utils:BarRecord>{ barFieldOne: 2 }];
    utils:assertErrorReason(trap barRecordArray[index], INDEX_OUT_OF_RANGE_REASON,
                            INVALID_REASON_ON_ACCESS_BY_NEGATIVE_INDEX_FAILURE_MESSAGE);
    
    index = barRecordArray.length();
    utils:assertErrorReason(trap barRecordArray[index], INDEX_OUT_OF_RANGE_REASON,
                            INVALID_REASON_ON_ACCESS_BY_ARRAY_LENGTH_INDEX_FAILURE_MESSAGE);
    
    index = barRecordArray.length();
    utils:assertErrorReason(trap barRecordArray[index], INDEX_OUT_OF_RANGE_REASON,
                            INVALID_REASON_ON_ACCESS_BY_LARGER_INDEX_FAILURE_MESSAGE);
}

@test:Config {}
function testObjectArrayMemberReferenceByInvalidIntegerIndex() {
    int index = -1;
    utils:BarObject b1 = new(1);
    utils:BarObject b2 = new(2);
    utils:BarObject b3 = new(3);
    utils:BarObject[] barObjectArray = [b1, b2, b3];
    utils:assertErrorReason(trap barObjectArray[index], INDEX_OUT_OF_RANGE_REASON,
                            INVALID_REASON_ON_ACCESS_BY_NEGATIVE_INDEX_FAILURE_MESSAGE);
    
    index = barObjectArray.length();
    utils:assertErrorReason(trap barObjectArray[index], INDEX_OUT_OF_RANGE_REASON,
                            INVALID_REASON_ON_ACCESS_BY_ARRAY_LENGTH_INDEX_FAILURE_MESSAGE);
    
    index = barObjectArray.length();
    utils:assertErrorReason(trap barObjectArray[index], INDEX_OUT_OF_RANGE_REASON,
                            INVALID_REASON_ON_ACCESS_BY_LARGER_INDEX_FAILURE_MESSAGE);
}
