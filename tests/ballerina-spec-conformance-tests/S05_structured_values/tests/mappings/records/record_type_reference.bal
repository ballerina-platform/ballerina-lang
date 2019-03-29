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

// A record-type-reference pulls in fields from a named record type. The
// type-descriptor-reference must reference a type defined by a
// record-type-descriptor. The field-descriptors from the referenced type are
// copied into the type being defined; the meaning is the same as if they had been specified
// explicitly. Note that it does not pull in the record-rest-descriptor from the referenced
// type.
public type ClosedRecord record {|
    *OpenRecordTwo;
|};

@test:Config {}
function testRecordTypeReference() {
    ClosedRecord r1 = { fieldOne: s1 };
    test:assertEquals(r1.fieldOne, s1);

    utils:assertPanic(function () { updateOpenRecordTwo(r1, i1); }, "{ballerina}KeyNotFound",
                            "invalid reason on inherent type violating record update");
}

public type OpenRecordThree record {
    boolean fieldThree;
    *OpenRecordTwo;
};

@test:Config {}
function testRecordTypeReferenceOpenRestFieldOverride() {
    boolean bTrue = true;
    boolean bFalse = false;

    OpenRecordThree r1 = { fieldOne: s1, fieldThree: bTrue, fieldFour: bFalse };
    r1.fieldFour = s1;
    r1.fieldFive = bFalse;

    test:assertEquals(r1.fieldOne, s1);
    test:assertEquals(r1.fieldThree, bTrue);
    test:assertEquals(r1.fieldFour, s1);
    test:assertEquals(r1.fieldFive, bFalse);
}

public type OpenRecordFour record {
    boolean fieldFour;
    *ClosedRecord;
};

@test:Config {}
function testRecordTypeReferenceClosedRestFieldOverride() {
    boolean bTrue = true;
    boolean bFalse = false;

    OpenRecordFour r1 = { fieldOne: s1, fieldFour: bTrue, fieldThree: bFalse };
    r1.fieldTwo = s1;

    test:assertEquals(r1.fieldOne, s1);
    test:assertEquals(r1.fieldFour, bTrue);
    test:assertEquals(r1.fieldTwo, s1);
    test:assertEquals(r1.fieldThree, bFalse);
}
