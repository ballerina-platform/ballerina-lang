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

// A mapping is iterable as sequence of fields, where each field is represented by a 2-tuple (s,
// val) where s is a string for the name of a field, and val is its value. The order of the fields
// in the sequence is implementation-dependent, but implementations are encouraged to
// preserve and use the order in which the fields were added.
@test:Config {}
function testRecordIteration() {
    // tested in `testIterableTypes()` in `05-values-types-variables/tests/values_types_variables.bal`
}

// record-type-descriptor :=
// record { field-descriptor+ record-rest-descriptor }
// field-descriptor :=
// individual-field-descriptor | record-type-reference
// individual-field-descriptor := type-descriptor field-name [?];
// record-type-reference := * type-descriptor-reference ;
// record-rest-descriptor := [ record-rest-type ... ; ]
// record-rest-type := type-descriptor | !

// Each individual-field-descriptor specifies an additional constraint that a mapping
// value must satisfy for it to belong to the described type. The constraint depends on whether
// ? is present:
// ● if ? is not present, then the constraint is that the mapping value must have a field with
// the specified field-name and with a value belonging to the specified type-descriptor;
// this is called a required field;
// ● if ? is present, then the constraint is that if the mapping value has a field with the
// specified field-name, then its value must belong to the specified type-descriptor; this
// is called an optional field.

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
    utils:assertErrorReason(trap updateRecordFieldOne(f, i1), "{ballerina}InherentTypeViolation",
                            "invalid reason on inherent type violating record update");

    BarRecord b = { fieldOne: s1, fieldThree: 1.0 };
    utils:assertErrorReason(trap updateRecordFieldOne(b, i1), "{ballerina}InherentTypeViolation",
                            "invalid reason on inherent type violating record update");
}

function updateRecordFieldOne(record{ any... } r, any val) {
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
    test:assertTrue(result is error, 
                    msg = "expected conversion to fail since all required fields are not present");
    utils:assertErrorReason(result, "{ballerina}StampError", "expected conversion to faising fields");
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

// The order of the individual-field-descriptors within a
// record-type-descriptor is not significant. Note that the delimited identifier syntax
// allows the field name to be any non-empty string.
type BazRecord record {
    string ^"string";
    int ^"int field";
};

@test:Config {}
function testDifferentFieldDescriptorsAndOrder() {
    BazRecord b = { ^"int field": i1, ^"string": s1 };
    test:assertEquals(b.^"string", s1);
    test:assertEquals(b.^"int field", i1);
}

// The record-rest-descriptor determines whether a value of the described type may
// contain extra fields, that is fields other than those named by individual type descriptors, and,
// if so, the type of the values of the extra fields

// ● if the record-rest-descriptor is empty, then the value may contain extra fields
// belonging to any pure type
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

// ● if the record-rest-type is a type descriptor, then the value may contain extra
// fields, and the type descriptor specifies the type of the values of the extra fields
public type OpenRecordTwo record {
    string fieldOne;
    int...
};

@test:Config {}
function testOpenRecordWithSpecifiedRestType() {
    int i2 = 2;
    OpenRecordTwo r1 = { fieldOne: s1, fieldTwo: i1 };
    r1.fieldthree = i2;

    utils:assertErrorReason(trap updateOpenRecordTwo(r1, s1), "{ballerina}InherentTypeViolation",
                            "invalid reason on inherent type violating record update");
}

function updateOpenRecordTwo(record{ any... } r, any val) {
    r.fieldFour = val;
}

// A record-type-reference pulls in fields from a named record type. The
// type-descriptor-reference must reference a type defined by a
// record-type-descriptor. The field-descriptors from the referenced type are
// copied into the type being defined; the meaning is the same as if they had been specified
// explicitly. Note that it does not pull in the record-rest-descriptor from the referenced
// type.
public type ClosedRecord record {
    *OpenRecordTwo;
    !...
};
@test:Config {}
function testRecordTypeReference() {
    ClosedRecord r1 = { fieldOne: s1 };
    test:assertEquals(r1.fieldOne, s1);

    utils:assertErrorReason(trap updateOpenRecordTwo(r1, i1), "{ballerina}KeyNotFound",
                            "invalid reason on inherent type violating record update");
}

public type OpenRecordThree record {
    boolean fieldThree;
    *OpenRecordTwo;
};

@test:Config {}
function testRecordTypeReferenceRestFieldOverride() {
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

// Note that records are covariant with respect to the field types. Thus a closed record type
// record { T1’ f1; ... ; Tn’ fn; !...;} is a subtype of type record { T1
// f1; ... ; Tn fn; !...;} if and only Ti’ is a subtype of Ti for 1 <= i <= n. In
// determining whether one record type is a subtype of another, each field in one record type is
// compared to the field with the same name in the other record type, regardless of the order in
// which the fields were specified in the record type descriptors, since field order is not
// significant for record types.
public type ClosedRecordTwo record {
    string|int fieldOne;
    boolean|float fieldTwo;
    !...
};

public type ClosedRecordThree record {
    float fieldTwo;
    string fieldOne;
    !...
};

public type ClosedRecordFour record {
    boolean fieldOne;
    string fieldTwo;
    !...
};

@test:Config {}
function testRecordCovariance() {
    any r = <ClosedRecordThree>{ fieldOne: s1, fieldTwo: 100.0 };
    test:assertTrue(r is ClosedRecordTwo, msg = "expected record to be identified as a sub-type"); 

    r = <ClosedRecordFour>{ fieldOne: false, fieldTwo: s1 };
    test:assertTrue(!(r is ClosedRecordTwo), msg = "expected record to not be identified as a sub-type"); 
}
