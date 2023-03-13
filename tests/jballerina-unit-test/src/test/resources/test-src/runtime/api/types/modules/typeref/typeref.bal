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
import types.objects as o;

public annotation IntConstraints Int on type, record field;

public annotation ArrayConstraints Array on type, record field;

public annotation X on type;

public annotation Y on type;

type IntConstraints record {|
    int minValue?;
|};

public type ArrayConstraints record {|
    int maxLength?;
|};

@Int {minValue: 0}
type PositiveInt int;

@Int {minValue: 4}
type PositiveIntRef PositiveInt;

@Int {minValue: 5}
type PositiveIntRO PositiveInt & readonly;

@Int {minValue: 6}
type PositiveIntRef2 o:PositiveInt;

@Int {minValue: 0}
type '\ \/\:\@\[\`\{\~\u{03C0}_123_ƮέŞŢ_Int int;

@Int {minValue: 0}
type Foo anydata[];

type Person record {
    PositiveInt age;
};

type Person2 record {
    int age = 25;
};

type Employee record {
    int id;
    Person profile;
};

type Employee2 record {
    int id;
    Person2 profile?;
};

type EmpError error<Employee>;

public class Service {
    function testFunction(PositiveInt intVal, Foo arr = []) returns string {
        return intVal.toString();
    }

}

public function validateTypeRef() {
    validateType();
    validateRecordField();
    validateTypeAnnotations();
    testRuntimeTypeRef();
    validateArray();
    validateFunctionParameters();
    validateRuntimeAPIs();
    validateValueWithUnion();
    validateTableMultipleKey();
    validateInlineRecordField();
}

function validateFunctionParameters() {
    test:assertEquals(testFunction(25), "25");
    test:assertEquals(validateFunctionParameterExtern(testFunction), ());

    Service s = new ();
    test:assertEquals(validateFunctionParameterFromObject(s), ());
}

function testFunction(PositiveInt intVal, Foo arr = []) returns string {
    return intVal.toString();
}

function testRuntimeTypeRef() {
    Person2 val1 = {"age": 25};
    test:assertTrue(val1 is Person);

    Employee val2 = {"id": 101, "profile": {"age": 22}};
    test:assertEquals(val2.id, 101);
    test:assertEquals(val2.profile, {age: 22});

    Employee & readonly val3 = {"id": 101, "profile": {"age": 22}};
    test:assertEquals(val3.id, 101);
    test:assertEquals(val3.profile, {age: 22});

    error er = error EmpError("Error", id = 101, profile = {age: 22});
    test:assertEquals(er.message(), "Error");
    test:assertEquals(er.detail(), {id: 101, profile: {age: 22}});

    json jsonVal = {"id": 101, "profile": {"age": 22}};
    json clonedJsonVal = jsonVal.cloneReadOnly();
    Employee|error val4 = trap clonedJsonVal.ensureType(Employee);
    test:assertTrue(val4 is Employee);
    test:assertEquals(val3, {id: 101, profile: {age: 22}});

    Employee2 emp = {id: 111};
    emp.profile.age = 12;
    test:assertFalse(emp.profile is ());
    test:assertEquals(emp.profile, {age: 12});
}

@X
type Nil ();

@X
type Integer int:Signed32;

@X
type String string;

@X
type XML xml;

@X
type Array string[];

@X
type Tuple [string, int, decimal];

@X
type Map map<string>;

@X
type Record Person;

@X
type Table table<Person>;

@X
type Error error<Person>;

@X
type Future future;

class Fruit {
    string color;

    public function init(string color) {
        self.color = color;
    }
}

@X
type Object Fruit;

@X
type TypeDesc typedesc<anydata>;

@X
type Handle handle;

@X
type Stream stream;

@X
type Singleton 2;

@X
type ReadOnly readonly;

@X
type Any any;

@X
type Never never;

@X
type Union int|string;

@X
type Intersection map<anydata> & readonly;

@X
type Optional string?;

@X
type AnyData anydata;

@X
type JSON json;

@X
type Byte byte;

@Y
type TupleRef Tuple;

function validateTypeAnnotations() {
    typedesc<any> anyTd = Nil;
    test:assertTrue(anyTd.@X is true);
    test:assertTrue(Nil.@X is true);

    anyTd = Integer;
    test:assertTrue(anyTd.@X is true);
    test:assertTrue(Integer.@X is true);

    anyTd = String;
    test:assertTrue(anyTd.@X is true);
    test:assertTrue(String.@X is true);

    anyTd = XML;
    test:assertTrue(anyTd.@X is true);
    test:assertTrue(XML.@X is true);

    anyTd = Array;
    test:assertTrue(anyTd.@X is true);
    test:assertTrue(Array.@X is true);

    anyTd = Tuple;
    test:assertTrue(anyTd.@X is true);
    test:assertTrue(Tuple.@X is true);

    anyTd = TupleRef;
    test:assertTrue(anyTd.@Y is true);
    test:assertTrue(TupleRef.@Y is true);
    test:assertTrue(TupleRef.@X is ());
    test:assertTrue(Tuple.@X is true);

    anyTd = Map;
    test:assertTrue(anyTd.@X is true);
    test:assertTrue(Map.@X is true);

    anyTd = Record;
    test:assertTrue(anyTd.@X is true);
    test:assertTrue(Record.@X is true);

    anyTd = Table;
    test:assertTrue(anyTd.@X is true);
    test:assertTrue(Table.@X is true);

    typedesc<error> eTd = Error;
    test:assertTrue(eTd.@X is true);
    test:assertTrue(Error.@X is true);

    anyTd = Future;
    test:assertTrue(anyTd.@X is true);
    test:assertTrue(Future.@X is true);

    anyTd = Object;
    test:assertTrue(anyTd.@X is true);
    test:assertTrue(Object.@X is true);

    anyTd = TypeDesc;
    test:assertTrue(anyTd.@X is true);
    test:assertTrue(TypeDesc.@X is true);

    anyTd = Handle;
    test:assertTrue(anyTd.@X is true);
    test:assertTrue(Handle.@X is true);

    anyTd = Stream;
    test:assertTrue(anyTd.@X is true);
    test:assertTrue(Stream.@X is true);

    anyTd = Singleton;
    test:assertTrue(anyTd.@X is true);
    test:assertTrue(Singleton.@X is true);

    test:assertTrue(ReadOnly.@X is true);

    anyTd = Any;
    test:assertTrue(anyTd.@X is true);
    test:assertTrue(Any.@X is true);

    anyTd = Never;
    test:assertTrue(anyTd.@X is true);
    test:assertTrue(Never.@X is true);

    anyTd = Union;
    test:assertTrue(anyTd.@X is true);
    test:assertTrue(Union.@X is true);

    anyTd = Intersection;
    test:assertTrue(anyTd.@X is true);
    test:assertTrue(Intersection.@X is true);

    anyTd = Optional;
    test:assertTrue(anyTd.@X is true);
    test:assertTrue(Optional.@X is true);

    anyTd = AnyData;
    test:assertTrue(anyTd.@X is true);
    test:assertTrue(AnyData.@X is true);

    anyTd = JSON;
    test:assertTrue(anyTd.@X is true);
    test:assertTrue(JSON.@X is true);

    anyTd = Byte;
    test:assertTrue(anyTd.@X is true);
    test:assertTrue(Byte.@X is true);

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

    PositiveIntRO value2 = 6;
    PositiveIntRO|error validation1 = validate(value2);
    if validation1 is PositiveIntRO {
        test:assertEquals(validation1, 6);
    } else {
        test:assertFail("Expected error not found.");
    }

    PositiveIntRef value3 = 6;
    PositiveIntRef|error validation2 = validate(value3);
    if validation2 is PositiveIntRef {
        test:assertEquals(validation2, 6);
    } else {
        test:assertFail("Expected error not found.");
    }

    PositiveIntRef2 value4 = 7;
    PositiveIntRef2|error validation3 = validate(value4);
    if validation3 is PositiveIntRef2 {
        test:assertEquals(validation3, 7);
    } else {
        test:assertFail("Expected error not found.");
    }

    o:PositiveInt addVal = 2;
    PositiveIntRef2 value5 = 7 + addVal;
    PositiveIntRef2|error validation4 = validate(value5);
    if validation4 is PositiveIntRef2 {
        test:assertEquals(validation4, 9);
    } else {
        test:assertFail("Expected error not found.");
    }

    anydata[] a = [1, 2, 3];
    Foo|error val = validateArrayElements(a);
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

    person = {age: -15};
    validation = validateRecord(person);
    if validation is error {
        test:assertEquals(validation.message(), "Validation failed for 'minValue' constraint(s).");
    } else {
        test:assertFail("Expected error not found.");
    }
}

type CustomInt PositiveInt;

@Array {
    maxLength: 10
}
type CustomIntArray CustomInt[];

function validateArray() {
    CustomIntArray arr = [1, 2, -5];
    CustomIntArray|error validation = validateArrayConstraint(arr);
    if validation is error {
        test:assertEquals(validation.message(), "Validation failed for 'minValue' constraint(s).");
    } else {
        test:assertFail("Expected error not found.");
    }
}

public type StringConstraints record {|
    int length?;
    int minLength?;
    int maxLength?;
|};

public annotation StringConstraints String on record field;

type InlineAlbum readonly & record {|
    @String {
        minLength: 1
    }
    string title;
    string artist;
|};

type MutableAlbum record {|
    @String {
        maxLength: 5,
        minLength: 1
    }
    string title;
    string artist;
|};

MutableAlbum mutableAlbum = {
    artist: "testA",
    title: "testssA"
};

InlineAlbum inlineImmutableAlbum = {
    artist: "testB",
    title: "testssB"
};

record {|
    @String {
        minLength: 1
    }
    string title;
    string artist;
|} inlineAlbum = {
    artist: "testC",
    title: "testssC"
};

function validateInlineRecordField() {
    boolean s1 = checkInlineRecordAnnotations(MutableAlbum, StringConstraints);
    boolean s2 = checkInlineRecordAnnotations(InlineAlbum, StringConstraints);
    boolean s3 = checkInlineRecordAnnotations(typeof inlineAlbum, StringConstraints);
    test:assertTrue(s1);
    test:assertTrue(s2);
    test:assertTrue(s3);
}

public isolated function checkInlineRecordAnnotations(typedesc<any> t1, typedesc<any> t2) returns boolean = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Values"
} external;

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
public isolated function validateArrayElements(anydata value, typedesc<anydata> td = <>) returns td|error = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Values"
} external;

# Validates the provided array value against the configured annotations for field.
#
# + value - The `anydata` type value to be constrained
# + td - The type descriptor of the value to be constrained
# + return - The type descriptor of the value which is validated or else an `constraint:Error` in case of an error
public isolated function validateArrayConstraint(anydata value, typedesc<anydata> td = <>) returns td|error = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Values"
} external;

public isolated function validateFunctionParameterExtern(function value) returns error? = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Values"
} external;

public isolated function validateFunctionParameterFromObject(Service value) returns error? = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Values"
} external;
