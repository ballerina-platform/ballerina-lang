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

const EXPECTED_STAMPING_TO_BE_SUCCESSFUL_FAILURE_MESSAGE = "expected stamping to be successful";

// A value looks like a type at a particular point in the execution of a program if its shape
// at that point is a member of the type
// Ballerina also provides mechanisms that take a value that looks like a type
// and use it to create a value that belongs to a type
@test:Config {}
function testLooksLikeArray() {
    (int|boolean)[3] a = [true, false, false];
    var result1 = boolean[].stamp(a);
    test:assertTrue(result1 is boolean[], msg = EXPECTED_STAMPING_TO_BE_SUCCESSFUL_FAILURE_MESSAGE);
}

@test:Config {}
function testLooksLikeTuple() {
    (int|boolean, string|float, decimal) c = (false, 1.0, 5.2d);
    var result2 = (boolean, float, decimal).stamp(c);
    test:assertTrue(result2 is (boolean, float, decimal), msg = EXPECTED_STAMPING_TO_BE_SUCCESSFUL_FAILURE_MESSAGE);
}

@test:Config {}
function testLooksLikeMap() {
    map<string|int> m = {
        zero: "map with strings only",
        one: "test string 1"
    };
    var result3 = map<string>.stamp(m);
    test:assertTrue(result3 is map<string>, msg = EXPECTED_STAMPING_TO_BE_SUCCESSFUL_FAILURE_MESSAGE);
}

public type BazRecordTwo record {
    float bazFieldOne;
    string bazFieldTwo;
};

@test:Config {}
function testLooksLikeRecord() {
    anydata b = <BazRecordTwo>{
        bazFieldOne: 1.0,
        bazFieldTwo: "test string 1"
    };
    var result4 = BazRecord.stamp(b);
    test:assertTrue(result4 is BazRecord, msg = EXPECTED_STAMPING_TO_BE_SUCCESSFUL_FAILURE_MESSAGE);
}
