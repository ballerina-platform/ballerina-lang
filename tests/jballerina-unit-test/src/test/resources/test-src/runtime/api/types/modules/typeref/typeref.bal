// Copyright (c) 2022 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import ballerina/jballerina.java;

public annotation IntConstraints Int on type, record field;

public annotation X on type;

type IntConstraints record {|
    int minValue?;
|};

@Int {minValue: 0}
type PositiveInt int;

@Int {minValue: 0}
type '\ \/\:\@\[\`\{\~\u{03C0}_123_ƮέŞŢ_Int int;

@X
type Nil ();

@Int {minValue: 0}
type Foo anydata[];

type Person record {
    @Int {
        minValue: 18
    }
    PositiveInt age;
};

public function validateTypeRef() {
    validateType();
    validateRecordField();
    validateTypeAnnotations();
}

function validateTypeAnnotations() {
    typedesc<()> nilTd = Nil;
    test:assertTrue(nilTd.@X is true);
    test:assertTrue(Nil.@X is true);
}

function validateType() {
    PositiveInt value = 2;
    PositiveInt|error validation = validate(value);
    if validation is PositiveInt {
        test:assertEquals(validation, 2);
    } else {
        test:assertFail("Expected error not found.");
    }

    value = 0;
    validation = validate(value);
    if validation is PositiveInt {
        test:assertEquals(validation, 0);
    } else {
        test:assertFail("Expected error not found.");
    }

    value = -2;
    validation = validate(value);
    if validation is error {
        test:assertEquals(validation.message(), "Validation failed for 'minValue' constraint(s).");
    } else {
        test:assertFail("Expected error not found.");
    }

    '\ \/\:\@\[\`\{\~\u{03C0}_123_ƮέŞŢ_Int value1 = -2;
    validation = validate(value1);
    if validation is error {
        test:assertEquals(validation.message(), "Validation failed for 'minValue' constraint(s).");
    } else {
        test:assertFail("Expected error not found.");
    }

    anydata[] a = [1, 2, 3];
    Foo|error val = validateArray(a);
    if val is Foo {
        test:assertEquals(val, [1, 2, 3]);
    } else {
        test:assertFail("Expected error not found.");
    }
}

function validateRecordField() {
    Person person = {age: 23};
    Person|error validation = validateRecord(person);
    if validation is Person {
        test:assertEquals(validation.age, 23);
    } else {
        test:assertFail("Expected error not found." + validation.toString());
    }

    person = {age: 18};
    validation = validateRecord(person);
    if validation is Person {
        test:assertEquals(validation.age, 18);
    } else {
        test:assertFail("Expected error not found.");
    }

    person = {age: 15};
    validation = validateRecord(person);
    if validation is error {
        test:assertEquals(validation.message(), "Validation failed for 'minValue' constraint(s).");
    } else {
        test:assertFail("Expected error not found.");
    }
}

# Validates the provided value against the configured annotations.
#
# + value - The `anydata` type value to be constrained
# + td - The type descriptor of the value to be constrained
# + return - The type descriptor of the value which is validated or else an `constraint:Error` in case of an error
public isolated function validate(anydata value, typedesc<anydata> td = <>) returns td|error = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Values"
} external;

# Validates the provided record value against the configured annotations for field.
#
# + value - The `anydata` type value to be constrained
# + td - The type descriptor of the value to be constrained
# + return - The type descriptor of the value which is validated or else an `constraint:Error` in case of an error
public isolated function validateRecord(anydata value, typedesc<anydata> td = <>) returns td|error = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Values"
} external;

# Validates the provided array value against the configured annotations for field.
#
# + value - The `anydata` type value to be constrained
# + td - The type descriptor of the value to be constrained
# + return - The type descriptor of the value which is validated or else an `constraint:Error` in case of an error
public isolated function validateArray(anydata value, typedesc<anydata> td = <>) returns td|error = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Values"
} external;
