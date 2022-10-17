// Copyright (c) 2019, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
//
// WSO2 LLC. licenses this file to you under the Apache License,
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

function testStaticFieldAccess() returns handle {
    return getContractId();
}

function testStaticFieldMutate(string value) {
    setContractId(value);
}

function testStaticPrimitiveFieldAccess() returns int {
    return getAge();
}

function testStaticPrimitiveFieldMutate(int shortValue) {
    return setShortValue(shortValue);
}

function testStaticBooleanFieldMutate() {
    boolean bool = true;
    setBoolean(bool);
    test:assertEquals(getBoolean(), true);
}

public type bool true|false|5;

function testBFiniteToJBooleanCast() {
    bool arg = true;
    setFiniteToBoolean(arg);
    test:assertEquals(getBoolean(), true);
}

function testInstanceFieldAccess(handle receiver) returns handle {
    return getCreatedAt(receiver);
}

function testInstanceFieldMutate(handle receiver, handle value) {
    setUUID(receiver, value);
}

function testInstancePrimitiveFieldAccess(handle receiver) returns boolean {
    return isEmpty(receiver);
}

function testInstancePrimitiveFieldMutate(handle receiver, float value) {
    setLKR(receiver, value);
}

function testFieldSetAndGetWithDefaultValues() {
    setAgeWithDefaultParameters(789);
    test:assertEquals(getAge(), 789);
    setAgeWithDefaultParameters();
    test:assertEquals(getAge(), 19);

    setContractIdWithDefaultParameters("789");
    test:assertEquals(getStaticStringContractId(), "789");
    setContractIdWithDefaultParameters();
    test:assertEquals(getStaticStringContractId(), "12bacb");

    handle jObject = newJavaFieldAccessMutate();

    setCountWithDefaultParameters(jObject, 789);
    test:assertEquals(getCount(jObject), 789);
    setCountWithDefaultParameters(jObject);
    test:assertEquals(getCount(jObject), 19);

    setBalStringWithDefaultParameters(jObject, "789");
    test:assertEquals(getBalString(jObject), "789");
    setBalStringWithDefaultParameters(jObject);
    test:assertEquals(getBalString(jObject), "12bacb");

    setCountWithDefaultParameters();
    setBalStringWithDefaultParameters();
    test:assertEquals(getCount(), 101);
    test:assertEquals(getBalString(), "String value");
}

function getInt1() returns int {
    worker w1 returns int {
        runtime:sleep(0.01);
        return 3;
    }

    future<int> futureIntVal1 = start getInt2();
    int intVal1 = checkpanic wait futureIntVal1;
    int intVal2 = wait w1;

    return intVal1 + intVal2 + 5;
}

function getInt2() returns int {
    return 4;
}

function getStr1() returns string {
    worker w1 returns string {
        runtime:sleep(0.01);
        return "a";
    }

    future<string> futureStringVal1 = start getStr2();
    string stringVal1 = checkpanic wait futureStringVal1;
    string stringVal2 = wait w1;

    return stringVal1 + stringVal2 + "c";
}

function getStr2() returns string {
    return "b";
}

// Java interoperability external functions

// Static field access and mutate
function setAgeWithDefaultParameters(int age = (1 + 2 + getInt1() + getInt2())) = @java:FieldSet {
    name: "age",
    'class: "org/ballerinalang/nativeimpl/jvm/tests/JavaFieldAccessMutate"
} external;

function setContractIdWithDefaultParameters(string contractId = ("1" + "2" + getStr1() + getStr2())) = @java:FieldSet {
    name: "contractId",
    'class: "org/ballerinalang/nativeimpl/jvm/tests/JavaFieldAccessMutate"
} external;

function getStaticStringContractId() returns string = @java:FieldGet {
    name: "contractId",
    'class: "org/ballerinalang/nativeimpl/jvm/tests/JavaFieldAccessMutate"
} external;

public function getContractId() returns handle = @java:FieldGet {
    name: "contractId",
    'class: "org/ballerinalang/nativeimpl/jvm/tests/JavaFieldAccessMutate"
} external;

public function setContractId(string contractId) = @java:FieldSet {
    name: "contractId",
    'class: "org/ballerinalang/nativeimpl/jvm/tests/JavaFieldAccessMutate"
} external;

public function getAge() returns int = @java:FieldGet {
    name: "age",
    'class: "org/ballerinalang/nativeimpl/jvm/tests/JavaFieldAccessMutate"
} external;

public function setShortValue(int shortValue) = @java:FieldSet {
    name: "aShort",
    'class: "org/ballerinalang/nativeimpl/jvm/tests/JavaFieldAccessMutate"
} external;

// Set and Get boolean fields
public function setBoolean(boolean bool) = @java:FieldSet {
    name: "isAvailable",
    'class: "org/ballerinalang/nativeimpl/jvm/tests/JavaFieldAccessMutate"
} external;

public function getBoolean() returns boolean = @java:FieldGet {
    name: "isAvailable",
    'class: "org/ballerinalang/nativeimpl/jvm/tests/JavaFieldAccessMutate"
} external;

public function setFiniteToBoolean(bool arg) = @java:FieldSet {
    name: "isAvailable",
    'class: "org/ballerinalang/nativeimpl/jvm/tests/JavaFieldAccessMutate"
} external;

// Instance field access and mutate
function setCountWithDefaultParameters(handle receiver = newJavaFieldAccessMutate(),
    int age = (1 + 2 + getInt1() + getInt2())) = @java:FieldSet {
    name: "count",
    'class: "org/ballerinalang/nativeimpl/jvm/tests/JavaFieldAccessMutate"
} external;

function setBalStringWithDefaultParameters(handle receiver = newJavaFieldAccessMutate(),
    string contractId = ("1" + "2" + getStr1() + getStr2())) = @java:FieldSet {
    name: "balString",
    'class: "org/ballerinalang/nativeimpl/jvm/tests/JavaFieldAccessMutate"
} external;

function getCount(handle receiver = newJavaFieldAccessMutate()) returns int = @java:FieldGet {
    name: "count",
    'class: "org/ballerinalang/nativeimpl/jvm/tests/JavaFieldAccessMutate"
} external;

function getBalString(handle receiver = newJavaFieldAccessMutate()) returns string = @java:FieldGet {
    name: "balString",
    'class: "org/ballerinalang/nativeimpl/jvm/tests/JavaFieldAccessMutate"
} external;

public function getCreatedAt(handle receiver) returns handle = @java:FieldGet {
    name: "createdAt",
    'class: "org/ballerinalang/nativeimpl/jvm/tests/JavaFieldAccessMutate"
} external;

public function setUUID(handle receiver, handle uuid) = @java:FieldSet {
    name: "uuid",
    'class: "org/ballerinalang/nativeimpl/jvm/tests/JavaFieldAccessMutate"
} external;

public function isEmpty(handle receiver) returns boolean = @java:FieldGet {
    name: "isEmpty",
    'class: "org/ballerinalang/nativeimpl/jvm/tests/JavaFieldAccessMutate"
} external;

public function setLKR(handle receiver, float value) = @java:FieldSet {
    name: "lkr",
    'class: "org/ballerinalang/nativeimpl/jvm/tests/JavaFieldAccessMutate"
} external;

// Constructor
function newJavaFieldAccessMutate() returns handle = @java:Constructor {
    'class: "org/ballerinalang/nativeimpl/jvm/tests/JavaFieldAccessMutate"
} external;
