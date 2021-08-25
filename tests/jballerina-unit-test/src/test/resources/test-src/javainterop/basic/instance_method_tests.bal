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

import ballerina/jballerina.java;
import ballerina/test;

function testAcceptNothingAndReturnNothing(handle receiver) {
    increaseCounterByOne(receiver);
}

function testAcceptNothingAndReturnVoidThrows(handle receiver) returns error? {
    return testThrowsWithVoid(receiver);
}

function testAcceptNothingAndReturnVoidThrowsReturn(handle receiver) returns error? {
    return testThrowsWithVoidReturn(receiver);
}

function testHandleOrErrorReturn(handle receiver) returns handle|error {
    return handleOrErrorReturn(receiver);
}

function testHandleOrErrorReturnThrows(handle receiver) returns handle|error {
    return handleOrErrorReturnThrows(receiver);
}

function testInteropFunctionWithDifferentName(handle receiver) {
    interopFunctionWithDifferentName(receiver);
}

function testAcceptNothingButReturnSomething(handle receiver) returns handle {
    return getCounter(receiver);
}

function testAcceptSomethingButReturnNothing(handle receiver, handle h) {
    setCounterValue(receiver, h);
}

function testAcceptSomethingAndReturnSomething(handle receiver, handle h) returns handle {
    return setAndGetCounterValue(receiver, h);
}

function testAcceptTwoParamsAndReturnSomething(handle receiver, handle h1, handle h2) returns handle {
    return setTwiceAndGetCounterValue(receiver, h1, h2);
}

function testHandleOrErrorWithObjectReturn(handle receiver) returns error|handle {
     return handleOrErrorWithObjectReturn(receiver);
}

function testPrimitiveOrErrorReturn(handle receiver) returns error|float {
     return primitiveOrErrorReturn(receiver);
}

function testPrimitiveOrErrorReturnThrows(handle receiver) returns error|float {
     return primitiveOrErrorReturnThrows(receiver);
}

function testUnionWithErrorReturnByte(handle receiver) returns error|int|boolean|byte|handle {
     return unionWithErrorReturnByte(receiver);
}

function testUnionWithErrorReturnThrows(handle receiver) returns error|int|boolean|byte|handle {
     return unionWithErrorReturnThrows(receiver);
}

function testUnionWithErrorReturnHandle(handle receiver) returns error|int|boolean|byte|handle {
     return unionWithErrorReturnHandle(receiver);
}

function testUnionWithErrorReturnByteArray(handle receiver) {
    handle|error handleOrError = unionWithErrorReturnByteArray(receiver);
    test:assertEquals(handleOrError is handle, true);
    test:assertEquals(handleOrError is error, false);
}

function testAnyOrErrorReturnStringArray(handle receiver) {
    any|error anyOrError = anyOrErrorReturnStringArray(receiver);
    test:assertEquals(anyOrError is any, true);
    test:assertEquals(anyOrError is error, false);
}

function testInstanceResolve() {
    int val = hashCode(newByte(2));
    assertEquals(val, 2);
}

public function testGetCurrentModule(handle receiver) {
     string moduleString =  getCurrentModule(receiver, 4);
     assertEquals(moduleString, "$anon#.#0#4");
     // calling overloaded method using self as a parameter.
     moduleString =  getCurrentModuleAndOverloadParams(receiver, 4);
     assertEquals(moduleString, "$anon#.#0#8");
     moduleString =  getCurrentModuleAndOverloadParamsWithReceiver(receiver, receiver, 4);
     assertEquals(moduleString, "$anon#.#0#16");
}

// Interop functions

public function increaseCounterByOne(handle receiver) = @java:Method{
    'class:"org/ballerinalang/nativeimpl/jvm/tests/InstanceMethods"
} external;

public function interopFunctionWithDifferentName(handle receiver) = @java:Method{
    'class:"org/ballerinalang/nativeimpl/jvm/tests/InstanceMethods",
    name:"increaseCounterByOne"
} external;

public function getCounter(handle receiver) returns handle = @java:Method{
    'class:"org/ballerinalang/nativeimpl/jvm/tests/InstanceMethods"
} external;

public function setCounterValue(handle receiver, handle h) = @java:Method{
    'class:"org/ballerinalang/nativeimpl/jvm/tests/InstanceMethods"
} external;

public function setAndGetCounterValue(handle receiver, handle h) returns handle = @java:Method{
    'class:"org/ballerinalang/nativeimpl/jvm/tests/InstanceMethods"
} external;

public function setTwiceAndGetCounterValue(handle receiver, handle h1, handle h2) returns handle = @java:Method{
    'class:"org/ballerinalang/nativeimpl/jvm/tests/InstanceMethods"
} external;

public function setAndGetCounterValueWhichThrowsCheckedException(handle receiver, handle h) returns handle | error = @java:Method{
    'class:"org/ballerinalang/nativeimpl/jvm/tests/InstanceMethods"
} external;

public function setAndGetCounterValueWhichThrowsUncheckedException(handle receiver, handle h) returns handle = @java:Method{
    'class:"org/ballerinalang/nativeimpl/jvm/tests/InstanceMethods"
} external;

public function setGetCounterValueWhichThrowsCheckedException(handle receiver, float f) returns int | error = @java:Method{
    'class:"org/ballerinalang/nativeimpl/jvm/tests/InstanceMethods"
} external;

public function setGetCounterValueWhichThrowsUncheckedException(handle receiver, float f) returns int = @java:Method{
    'class:"org/ballerinalang/nativeimpl/jvm/tests/InstanceMethods"
} external;

public function testThrowsWithVoid(handle receiver) returns error? = @java:Method{
    'class:"org/ballerinalang/nativeimpl/jvm/tests/InstanceMethods"
} external;

public function testThrowsWithVoidReturn(handle receiver) returns error? = @java:Method{
    'class:"org/ballerinalang/nativeimpl/jvm/tests/InstanceMethods"
} external;

public function handleOrErrorReturn(handle receiver) returns handle|error = @java:Method{
    'class:"org/ballerinalang/nativeimpl/jvm/tests/InstanceMethods"
} external;

public function handleOrErrorReturnThrows(handle receiver) returns handle|error = @java:Method{
    'class:"org/ballerinalang/nativeimpl/jvm/tests/InstanceMethods"
} external;

public function handleOrErrorWithObjectReturn(handle receiver) returns handle|error = @java:Method{
    'class:"org/ballerinalang/nativeimpl/jvm/tests/InstanceMethods"
} external;

public function handleOrErrorWithObjectReturnThrows(handle receiver) returns handle|error = @java:Method{
    'class:"org/ballerinalang/nativeimpl/jvm/tests/InstanceMethods"
} external;

public function primitiveOrErrorReturn(handle receiver) returns float|error = @java:Method{
    'class:"org/ballerinalang/nativeimpl/jvm/tests/InstanceMethods"
} external;

public function primitiveOrErrorReturnThrows(handle receiver) returns float|error = @java:Method{
    'class:"org/ballerinalang/nativeimpl/jvm/tests/InstanceMethods"
} external;

public function unionWithErrorReturnByte(handle receiver) returns error|int|boolean|byte|handle = @java:Method{
    'class:"org/ballerinalang/nativeimpl/jvm/tests/InstanceMethods"
} external;

public function unionWithErrorReturnThrows(handle receiver) returns error|int|boolean|byte|handle = @java:Method{
    'class:"org/ballerinalang/nativeimpl/jvm/tests/InstanceMethods"
} external;

public function unionWithErrorReturnHandle(handle receiver) returns error|int|boolean|byte|handle = @java:Method{
    'class:"org/ballerinalang/nativeimpl/jvm/tests/InstanceMethods"
} external;

public function unionWithErrorReturnByteArray(handle receiver) returns handle|error = @java:Method{
    'class:"org/ballerinalang/nativeimpl/jvm/tests/InstanceMethods"
} external;

public function anyOrErrorReturnStringArray(handle receiver) returns any|error = @java:Method{
    'class:"org/ballerinalang/nativeimpl/jvm/tests/InstanceMethods"
} external;

public function errorDetail(handle receiver) returns error? = @java:Method{
    'class:"org/ballerinalang/nativeimpl/jvm/tests/InstanceMethods"
} external;

public function uncheckedErrorDetail(handle receiver) returns int = @java:Method{
    'class:"org/ballerinalang/nativeimpl/jvm/tests/InstanceMethods"
} external;

function getCurrentModule(handle receiver, int a) returns string  = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.tests.InstanceMethods",
     paramTypes: ["long"]
} external;

function getCurrentModuleAndOverloadParams(handle receiver, int a) returns string  = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.tests.InstanceMethods",
     paramTypes: ["long"]
} external;

function getCurrentModuleAndOverloadParamsWithReceiver(handle receiver, handle receiverParam, int a)
                                                        returns string  =  @java:Method {
    name : "getCurrentModuleAndOverloadParams",
    'class: "org.ballerinalang.nativeimpl.jvm.tests.InstanceMethods",
    paramTypes: ["org.ballerinalang.nativeimpl.jvm.tests.InstanceMethods", "long"]
} external;

function hashCode(handle receiver) returns int = @java:Method {
    name: "hashCode",
    'class: "java.lang.Byte",
    paramTypes: []
} external;

function newByte(int val) returns handle = @java:Constructor {
   'class: "java.lang.Byte"
} external;

function assertEquals(anydata|error expected, anydata|error actual) {
    if isEqual(actual, expected) {
        return;
    }

    string expectedValAsString = expected is error ? expected.toString() : expected.toString();
    string actualValAsString = actual is error ? actual.toString() : actual.toString();
    panic error("expected '" + expectedValAsString + "', found '" + actualValAsString + "'");
}

isolated function isEqual(anydata|error actual, anydata|error expected) returns boolean {
    if (actual is anydata && expected is anydata) {
        return (actual == expected);
    } else {
        return (actual === expected);
    }
}
