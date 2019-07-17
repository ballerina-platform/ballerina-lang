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

// References make it possible for distinct members of a structure to refer to values that are
// identical, in the sense that they are stored in the same location.
@test:Config {}
function testDistinctArrayMembersReferringToSameValue() {
    int[4] s1 = [12, 13, 14, 15];
    int[4][4] s2 = [s1, [1, 2, 3, 4], s1, [21, 22, 23, 24]];
    test:assertTrue(s2[0] === s2[2], msg = EXPECTED_VALUES_TO_BE_AT_SAME_LOCATION_FAILURE_MESSAGE);
}

public type FooRecordOne record {|
    string fooOneFieldOne;
|};

@test:Config {}
function testDistinctTupleMembersReferringToSameValue() {
    FooRecordOne f1 = { fooOneFieldOne: "test string 1" };
    (int, FooRecordOne, FooRecordOne) s3 = (1, f1, f1);
    test:assertTrue(s3[1] === s3[2], msg = EXPECTED_VALUES_TO_BE_AT_SAME_LOCATION_FAILURE_MESSAGE);
}

@test:Config {}
function testDistinctMapMembersReferringToSameValue() {
    FooObject f2 = new("test string 2");
    FooObject f3 = new("test string 3");
    map<FooObject> s4 = { one: f2, two: f3, three: f2 };
    test:assertTrue(s4.one === s4.three, msg = EXPECTED_VALUES_TO_BE_AT_SAME_LOCATION_FAILURE_MESSAGE);
}

public type FooRecordTwo record {
    float fooTwoFieldOne;
};

@test:Config {}
function testDistinctRecordMembersReferringToSameValue() {
    FooRecordOne f4 = { fooOneFieldOne: "test string 4" };
    FooRecordTwo b1 = { fooRecFieldOne: f4, fooTwoFieldOne: 1.0, fooRecFieldTwo: f4 };
    test:assertTrue(b1.fooRecFieldOne === b1.fooRecFieldTwo, msg = EXPECTED_VALUES_TO_BE_AT_SAME_LOCATION_FAILURE_MESSAGE);
}
