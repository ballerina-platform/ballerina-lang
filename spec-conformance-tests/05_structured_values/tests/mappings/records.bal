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

// A mapping value is a container where each member has a key, which is a string, that
// uniquely identifies within the mapping. We use the term field to mean the member together
// its key; the name of the field is the key, and the value of the field is that value of the member;
// no two fields in a mapping value can have the same name.
@test:Config {}
function testRecordFieldUniqueName() {
    float newFloatVal = 2.0;
    utils:BazRecord b1 = { bazFieldOne: 1.0 };
    b1.bazFieldOne = newFloatVal;
    test:assertEquals(b1.length(), 1, msg = "expected the record to have one field");
    test:assertEquals(b1.bazFieldOne, newFloatVal, msg = "expected the field to have the updated value");

    string stringVal = "test string 1";
    int intVal = 1;

    b1.bazFieldTwo = stringVal;
    test:assertEquals(b1.length(), 2, msg = "expected the record to have two fields");
    test:assertEquals(b1.bazFieldOne, newFloatVal, msg = "expected the field to have the same value");
    test:assertEquals(b1.bazFieldTwo, stringVal, msg = "expected a field with the newly added value");

    b1.bazFieldTwo = intVal;
    test:assertEquals(b1.length(), 2, msg = "expected the record to have two fields");
    test:assertEquals(b1.bazFieldOne, newFloatVal, msg = "expected the field to have the same value");
    test:assertEquals(b1.bazFieldTwo, intVal, msg = "expected the field to have the updated value");
}

// The shape of a mapping value is an unordered collection of field shapes one for each field.
// The field shape for a field f has a name, which is the same as the name of f, and a shape,
// which is the shape of the value of f.
@test:Config {}
function testRecordFieldShape() {
    DefaultOpenRecord r1 = { bazFieldTwo: "test string 1", bazFieldOne: 1.0 };
    var conversionResult = utils:BazRecordTwo.convert(r1);
    test:assertTrue(conversionResult is utils:BazRecordTwo, msg = "expected conversion to succeed");

    // change the value's shape
    r1.bazFieldTwo = 1.0;
    conversionResult = utils:BazRecordTwo.convert(r1);
    test:assertTrue(conversionResult is error, msg = "expected conversion to fail");
    utils:assertErrorReason(conversionResult, "{ballerina}StampError", 
                            "invalid reason on conversion failure due to shape mismatch");

    // create a record without a required field,
    // but with a new field with a different name and matching value shape
    DefaultOpenRecord r2 = { bazFieldThree: "test string 3", bazFieldOne: 1.0 };
    conversionResult = utils:BazRecordTwo.convert(r2);
    test:assertTrue(conversionResult is error, msg = "expected conversion to fail");
    utils:assertErrorReason(conversionResult, "{ballerina}StampError", 
                            "invalid reason on conversion failure due to shape mismatch");
}

public type DefaultOpenRecord record {
};

// A mapping is iterable as sequence of fields, where each field is represented by a 2-tuple (s,
// val) where s is a string for the name of a field, and val is the value of the field. The order
// of the fields in the sequence is implementation-dependent, but implementations are
// encouraged to preserve and use the order in which the fields were added.
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
    utils:assertErrorReason(trap updateRecordFieldOne(f, i1), "{ballerina}InherentTypeViolation",
                            "invalid reason on inherent type violating record update");

    BarRecord b = { fieldOne: s1, fieldThree: 1.0 };
    utils:assertErrorReason(trap updateRecordFieldOne(b, i1), "{ballerina}InherentTypeViolation",
                            "invalid reason on inherent type violating record update");
}

function updateRecordFieldOne(record{ any...; } r, any val) {
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

// The order of the individual-field-descriptors within a record-type-descriptor is not significant. 
// Note that the delimited identifier syntax allows the field name to be any non-empty string.
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
    utils:assertErrorReason(trap updateClosedRecordWithOneField(r1, 1), "{ballerina}KeyNotFound", 
                            "invalid reason on inherent type violating record insertion");
}
function updateClosedRecordWithOneField(record{} rec, anydata value) {
    rec.newField = value;
}

public type ClosedRecordWithOneField record {
    string strField;
    !...;
};

// if the record-rest-type is a type descriptor T, then the value shape of 
// every extra field shape must be a member of T
public type OpenRecordTwo record {
    string fieldOne;
    int...;
};

@test:Config {}
function testOpenRecordWithSpecifiedRestType() {
    int i2 = 2;
    OpenRecordTwo r1 = { fieldOne: s1, fieldTwo: i1 };
    r1.fieldthree = i2;

    utils:assertErrorReason(trap updateOpenRecordTwo(r1, s1), "{ballerina}InherentTypeViolation",
                            "invalid reason on inherent type violating record update");
}

function updateOpenRecordTwo(record{ any...; } r, any val) {
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
    !...;
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
    !...;
};

public type ClosedRecordThree record {
    float fieldTwo;
    string fieldOne;
    !...;
};

public type ClosedRecordFour record {
    boolean fieldOne;
    string fieldTwo;
    !...;
};

@test:Config {}
function testRecordCovariance() {
    any r = <ClosedRecordThree>{ fieldOne: s1, fieldTwo: 100.0 };
    test:assertTrue(r is ClosedRecordTwo, msg = "expected record to be identified as a sub-type"); 

    r = <ClosedRecordFour>{ fieldOne: false, fieldTwo: s1 };
    test:assertTrue(!(r is ClosedRecordTwo), msg = "expected record to not be identified as a sub-type"); 
}

// The inherent type of a mapping value must be a mapping-type-descriptor. The
// inherent type of a mapping value determines a type Tf
// for the value of the field with name f.
// The runtime system will enforce a constraint that a value written to field f will belong to type
// Tf. Note that the constraint is not merely that the value looks like Tf.
@test:Config {}
function testRecordInherentTypeViolation() {
    map<string> m1 = { one: "test string 1", two: "test string 2" };
    ClosedRecordWithMapField r1 = { mapField: m1 };
    utils:assertErrorReason(trap updateClosedRecordWithMapField(r1, 1), "{ballerina}InherentTypeViolation", 
                            "invalid reason on inherent type violating map insertion");

    // `m2` looks like `map<string>`
    map<string|int> m2 = { one: "test string 1", two: "test string 2" };
    utils:assertErrorReason(trap updateClosedRecordWithMapField(r1, m2), "{ballerina}InherentTypeViolation", 
                            "invalid reason on inherent type violating map insertion");
}

function updateClosedRecordWithMapField(record{} rec, anydata value) {
    rec.mapField = value;
}

public type ClosedRecordWithMapField record {
    map<string> mapField;
};
