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

string s1 = "test string 1";
int i1 = 100;

// record-type-descriptor :=
// record { field-descriptor+ record-rest-descriptor }
// field-descriptor :=
// individual-field-descriptor | record-type-reference
// individual-field-descriptor := type-descriptor field-name [?];
// record-type-reference := * type-descriptor-reference ;
// record-rest-descriptor := [ record-rest-type ... ; ]
// record-rest-type := type-descriptor | !

// Each individual-field-descriptor specifies an additional constraint that a mapping value shape must 
// satisfy for it to be a member of the described type. The constraint depends on whether ? is present:
// if ? is not present, then the constraint is that the mapping value shape must have a field shape with 
// the specified field-name and with a value shape that is a member of the specified type-descriptor; 
// this is called a required field;
// if ? is present, then the constraint is that if the mapping value shape has a field shape with the 
// specified field-name, then its value shape must be a member of the specified type-descriptor; 
// this is called an optional field.

public type FooRecord record {
    string fieldOne;
    int fieldTwo;
};

public type BarRecord record {
    string fieldOne?;
    int fieldTwo?;
    float fieldThree;
};

@test:Config {}
function testRecordFieldValueTypeConformance() {
    FooRecord f = { fieldOne: s1, fieldTwo: i1 };
    utils:assertPanic(function () { updateRecordFieldOne(f, i1); }, INHERENT_TYPE_VIOLATION_REASON,
                            "invalid reason on inherent type violating record update");

    BarRecord b = { fieldOne: s1, fieldThree: 1.0 };
    utils:assertPanic(function () { updateRecordFieldOne(b, i1); }, INHERENT_TYPE_VIOLATION_REASON,
                            "invalid reason on inherent type violating record update");
}

function updateRecordFieldOne(record {| any|error...; |} r, any val) {
    r.fieldOne = val;
}

@test:Config {}
function testRequiredFields() {
    BarRecord b1 = { fieldOne: s1, fieldTwo: i1, fieldThree: 1.0 };
    var result = FooRecord.convert(b1);
    test:assertTrue(result is FooRecord, 
                    msg = "expected conversion to succeed since all required fields are present");
    FooRecord convertedRecord = <FooRecord> result;
    test:assertEquals(convertedRecord.fieldOne, s1, 
                      msg = "expected converted record to have the corresponding source value");
    test:assertEquals(convertedRecord.fieldTwo, i1, 
                      msg = "expected converted record to have the corresponding source value");

    map<anydata> b2 = { fieldOne: "test string 1" };
    result = FooRecord.convert(b2);
    if (result is error) {
        test:assertEquals(result.reason(), "{ballerina}ConversionError",
            msg = "expected conversion to fail due to missing fields");
    } else {
        test:assertFail(msg = "expected conversion to fail since all required fields are not present");
    }
}

@test:Config {}
function testOptionalFields() {
    float floatValue = 2.0;

    map<anydata> m1 = { fieldThree: floatValue };
    var result = BarRecord.convert(m1);
    test:assertTrue(result is BarRecord, 
                    msg = "expected conversion to succeed since all required fields are present");
    BarRecord convertedRecord = <BarRecord> result;
    test:assertEquals(convertedRecord["fieldOne"], (), 
                      msg = "expected converted record to not have a field with key `fieldOne`");
}

// The order of the individual-field-descriptors within a record-type-descriptor is not significant.
// Note that the delimited identifier syntax allows the field name to be any non-empty string.
type QuuxRecord record {
    string 'string;
    int 'int\ field;
};

@test:Config {}
function testDifferentFieldDescriptorsAndOrder() {
    QuuxRecord b = { 'int\ field: i1, 'string: s1 };
    test:assertEquals(b.'string, s1);
    test:assertEquals(b.'int\ field, i1);
}
