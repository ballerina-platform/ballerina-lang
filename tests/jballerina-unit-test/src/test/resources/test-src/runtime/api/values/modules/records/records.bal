// Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ballerina/jballerina.java;
import ballerina/lang.runtime;
import ballerina/test;

public type Address record {
    string city;
    string country;
    int postalCode;
};

public function getRecord(string recordName) returns record {} = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Values"
} external;

type Details record {
    string name;
    int id;
};

type Student record {|
    string name;
|};

public type Foo record {
    int[] y;
};

public type Bar readonly & record {|
    int[] x;
|};

type DetailsInUnion Details|map<anydata>|table<map<json>>;

type DefaultableRecord record {|
    int intVal = 3;
    string strVal = "hello";
    float floatVal = 2.3;
    boolean booleanval?;
|};

type DefaultableRecordAsync record {|
    int intVal = getAsyncInt(3);
    string strVal = getAsyncString("hello");
    float floatVal = getAsyncFloat(2.3);
    boolean booleanval?;
|};

type DefaultableOpenRecord record {
    int intVal = 3;
    string strVal1 = "hello";
};

isolated function getAsyncInt(int x) returns int {
    runtime:sleep(0.1);
    return x;
}

isolated function git getAsyncString(string x) returns string {
    runtime:sleep(0.1);
    return x;
}

isolated function getAsyncFloat(float x) returns float {
    runtime:sleep(0.1);
    return x;
}

public function validateAPI() {
    anydata recordVal1 = getRecordValue();
    test:assertTrue(recordVal1 is Student);
    test:assertEquals(recordVal1, {"name": "nameOfStudent"});

    Details recordVal2 = getRecordValueWithInitialValues();
    test:assertEquals(recordVal2, {"name": "studentName", "id": 123});

    json jsonValue = {name: "Jane", id: 234};
    anydata recordVal3 = getRecordValueFromJson(jsonValue, Details);
    test:assertEquals(recordVal3, {"name": "Jane", "id": 234});
    test:assertTrue(recordVal3 is Details);

    Details|map<anydata>|anydata recordVal4 = getRecordValueFromJson(jsonValue, DetailsInUnion);
    test:assertEquals(recordVal4, {"name": "Jane", "id": 234});
    test:assertTrue(recordVal4 is Details);

    Address addressRec1 = getRecordWithInitialValues();
    addressRec1.postalCode += 1;
    addressRec1["district"] = "Colombo";
    addressRec1["postalCode"] += 2;
    test:assertEquals(addressRec1, {"city": "Nugegoda", "country": "Sri Lanka", "postalCode": 10253, "district": "Colombo"});

    Address|error addressRec2 = trap getRecordWithInvalidInitialValues();
    test:assertTrue(addressRec2 is error);
    if (addressRec2 is error) {
        test:assertEquals("{ballerina/lang.map}InherentTypeViolation", addressRec2.message());
        test:assertEquals("invalid value for record field 'postalCode': expected value of type 'int', found 'string'",
        <string>checkpanic addressRec2.detail()["message"]);
    }

    Student & readonly studentRec1 = getReadOnlyRecordWithInitialValues();
    test:assertTrue(studentRec1.isReadOnly());
    test:assertEquals(studentRec1, {"name": "NameOfStudent1"});

    Student & readonly|error studentRec2 = trap getReadOnlyRecordWithInvalidInitialValues();
    test:assertTrue(studentRec2 is error);
    if (studentRec2 is error) {
        test:assertEquals("{ballerina/lang.map}KeyNotFound", studentRec2.message());
        test:assertEquals("invalid field access: field 'postalCode' not found in record type " +
        "'values.records:Student'", <string>checkpanic studentRec2.detail()["message"]);
    }

    anydata rec = getRecordValueWithDefaultFields("DefaultableRecord");
    test:assertTrue(rec is DefaultableRecord);
    test:assertEquals(rec, {"intVal": 3, "strVal": "hello", "floatVal": 2.3});

    rec = getRecordValueWithFieldValues("DefaultableRecord", "John");
    test:assertTrue(rec is DefaultableRecord);
    test:assertEquals(rec, {"intVal": 3, "strVal": "John", "floatVal": 2.3});

    rec = getRecordValueWithDefaultFields("DefaultableRecordAsync");
    test:assertTrue(rec is DefaultableRecordAsync);
    test:assertEquals(rec, {"intVal": 3, "strVal": "hello", "floatVal": 2.3});

    rec = getRecordValueWithFieldValues("DefaultableRecordAsync", "Bob");
    test:assertTrue(rec is DefaultableRecordAsync);
    test:assertEquals(rec, {"intVal": 3, "strVal": "Bob", "floatVal": 2.3});

    rec = getOpenRecordValueWithDefaultFields("DefaultableOpenRecord");
    test:assertTrue(rec is DefaultableOpenRecord);
    test:assertEquals(rec, {"intVal": 3, "strVal1": "hello", "floatVal": 2.3, "strVal2": "world"});

    record {|int a = 2; string b = getAsyncString("b");|} recValue = {};
    rec = getInlineRecordValueWithDefaultFields(recValue);
    test:assertEquals(rec, {"a": 2, "b": "b"});

    record {int a = 4; string b = getAsyncString("bbb"); float c;} recValue2 = {c: 5.6};
    rec = getInlineRecordValueWithDefaultFields2(recValue2);
    test:assertEquals(rec, {"a": 4, "b": "bbb", "c": 1.9, "e": true, "d": "test string"});
}

function getRecordWithInitialValues() returns Address = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Values"
} external;

function getRecordWithInvalidInitialValues() returns Address = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Values"
} external;

function getReadOnlyRecordWithInitialValues() returns Student & readonly = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Values"
} external;

function getReadOnlyRecordWithInvalidInitialValues() returns Student & readonly = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Values"
} external;

function getRecordValue() returns anydata = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Values"
} external;

function getRecordValueWithInitialValues() returns Details = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Values"
} external;

function getRecordValueFromJson(json value, typedesc<anydata> recType) returns anydata = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Values"
} external;

public function getRecordNegative(string recordName) returns record {} = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Values"
} external;

public function getRecordNegative2(string recordName) returns record {} = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Values"
} external;

public function getReadonlyRecordNegative(string recordName) returns record {} = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Values"
} external;

public function getRecordWithRestFieldsNegative() returns record {} = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Values"
} external;

function getRecordValueWithDefaultFields(string recordTypeName) returns anydata = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Values"
} external;

function getRecordValueWithFieldValues(string recordTypeName, string strVal) returns anydata = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Values"
} external;

function getOpenRecordValueWithDefaultFields(string recordTypeName) returns anydata = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Values"
} external;

function getInlineRecordValueWithDefaultFields(record {} recValue) returns anydata = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Values"
} external;

function getInlineRecordValueWithDefaultFields2(record {} recValue) returns anydata = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Values"
} external;

