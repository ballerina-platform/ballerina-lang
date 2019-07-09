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

// For a mapping value shape and a record-type-descriptor, let the extra field shapes be the
// field shapes of the mapping value shapes whose names are not the same as field-name of any 
// individual-field-descriptor. The record-rest-descriptor specifies the constraint that the extra 
// fields shapes must satisfy in order for the mapping value shape to be a member of the described type, as follows:

// if the record-rest-descriptor is empty, then the value shape of the extra field shapes must be pure
type OpenRecord record {
    string fieldOne;
};

@test:Config {}
function testDefaultOpenRecord() {
    string s2 = "test string 2";
    int i2 = 2;
    OpenRecord r1 = { fieldOne: s1, fieldTwo: s2 };
    r1.fieldThree = i1;
    test:assertEquals(r1.fieldOne, s1);
    test:assertEquals(r1.fieldTwo, s2);
    test:assertEquals(r1.fieldThree, i1);

    r1.fieldTwo = i2;
    test:assertEquals(r1.fieldTwo, i2);
}

// if the record-rest-type is !, then there must not be any extra field shapes
@test:Config {}
function testClosedRecord() {
    ClosedRecordWithOneField r1 = { strField: "test string 1" };
    utils:assertPanic(function () { updateClosedRecordWithOneField(r1, 1); }, "{ballerina}KeyNotFound",
                            "invalid reason on inherent type violating record insertion");
}

function updateClosedRecordWithOneField(record{} rec, anydata value) {
    rec.newField = value;
}

public type ClosedRecordWithOneField record {|
    string strField;
|};

// if the record-rest-type is a type descriptor T, then the value shape of 
// every extra field shape must be a member of T
public type OpenRecordTwo record {|
    string fieldOne;
    int...;
|};

@test:Config {}
function testOpenRecordWithSpecifiedRestType() {
    int i2 = 2;
    OpenRecordTwo r1 = { fieldOne: s1, fieldTwo: i1 };
    r1.fieldthree = i2;

    utils:assertPanic(function () { updateOpenRecordTwo(r1, s1); }, INHERENT_TYPE_VIOLATION_REASON,
                            "invalid reason on inherent type violating record update");
}

function updateOpenRecordTwo(record {| any...; |} r, any val) {
    r.fieldFour = val;
}
