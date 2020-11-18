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

import ballerina/runtime;
import ballerina/java;
import ballerina/lang.'value;
import ballerina/test;

function testAcceptNothingAndReturnNothing() {
    acceptNothingAndReturnNothing();
}

function testInteropFunctionWithDifferentName() {
    interopFunctionWithDifferentName();
}

function testAcceptNothingButReturnDate() returns handle {
    return acceptNothingButReturnDate();
}

function testAcceptNothingButReturnString() returns string {
    return acceptNothingButReturnString();
}

function testAcceptSomethingAndReturnSomething(handle h) returns handle {
    return acceptSomethingAndReturnSomething(h);
}

function testAcceptTwoParamsAndReturnSomething(handle h1, handle h2) returns handle {
    return acceptTwoParamsAndReturnSomething(h1, h2);
}

function testAcceptThreeParamsAndReturnSomething(handle h1, handle h2, handle h3) returns handle {
    return acceptThreeParamsAndReturnSomething(h1, h2, h3);
}

function testErrorOrTupleReturn() returns error|[string,string] {
   [string,string] ret = check getArrayValue();
   return ret;
}

function testFuncWithAsyncDefaultParamExpression() returns int {
    return funcWithAsyncDefaultParamExpression() + funcWithAsyncDefaultParamExpression(5) + funcWithAsyncDefaultParamExpression(50, 20);
}

function asyncRet() returns int {
    runtime:sleep(50);
    return 10;
}

function asyncRetWithVal(int a = 30) returns int {
    runtime:sleep(50);
    return a + 20;
}

function testUsingParamValues() returns int {
    return usingParamValues() + usingParamValues(5) + usingParamValues(50, 20);
}

function testDecimalParamAndReturn(decimal a1) returns decimal {
    return decimalParamAndReturn(a1);
}

function testStaticResolve() {
    int val = hashCode(2);
    test:assertEquals(val, 2);
}

// Interop functions
public function acceptNothingAndReturnNothing() = @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

public function interopFunctionWithDifferentName() = @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods",
    name:"acceptNothingAndReturnNothing"
} external;

public function acceptNothingButReturnDate() returns handle = @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

public function acceptNothingButReturnString() returns string = @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

function stringParamAndReturn(string a1) returns string = @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

public function acceptSomethingAndReturnSomething(handle h) returns handle = @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

public function acceptTwoParamsAndReturnSomething(handle h1, handle h2) returns handle = @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

public function acceptThreeParamsAndReturnSomething(handle h1, handle h2, handle h3) returns handle = @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

public function acceptNothingReturnNothingAndThrowsCheckedException() returns error? = @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

public function acceptNothingReturnNothingAndThrowsMultipleCheckedException() returns error? = @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

public function acceptNothingReturnNothingAndThrowsUncheckedException() = @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

public function acceptNothingReturnNothingAndThrowsCheckedAndUncheckedException() returns error? = @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

public function acceptNothingReturnSomethingAndThrowsCheckedException() returns handle | error = @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

public function acceptNothingReturnSomethingAndThrowsMultipleCheckedException() returns handle | error = @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

public function acceptNothingReturnSomethingAndThrowsCheckedAndUncheckedException() returns handle | error = @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

public function acceptNothingReturnSomethingAndThrowsUncheckedException() returns handle = @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

public function acceptSomethingReturnSomethingAndThrowsCheckedAndUncheckedException(handle h1) returns handle | error = @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

public function acceptSomethingReturnSomethingAndThrowsCheckedException(handle h1) returns handle | error = @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

public function acceptSomethingReturnSomethingAndThrowsMultipleCheckedException(handle h1) returns handle | error = @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

public function acceptSomethingReturnSomethingAndThrowsUncheckedException(handle h1) returns handle = @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

public class Person {
    int age = 9;
    public function init(int age) {
        self.age = age;
    }
}

public type ResourceDefinition record {|
    string path;
    string method;
|};

public type ApiDefinition record {|
    ResourceDefinition[] resources;
|};

public function testUnionReturn() returns string {
    ResourceDefinition resourceDef = {path:"path", method:"method"};
    ResourceDefinition[] resources = [resourceDef];
    ApiDefinition apiDef = {resources:resources};
    return value:toString(getMapOrError("swagger", apiDef));
}

function getMapOrError(string swaggerFilePath, ApiDefinition apiDef) returns ApiDefinition | error  = @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

public function getObjectOrError() returns Person|error = @java:Method {
    name: "returnObjectOrError",
    'class: "org.ballerinalang.test.javainterop.basic.StaticMethodTest"
} external;

function getArrayValue() returns [string, string] | error = @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

function funcWithAsyncDefaultParamExpression(int a1 = asyncRet(), int a2 = asyncRet()) returns int = @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

function usingParamValues(int a1 = asyncRet(), int a2 = asyncRetWithVal(a1)) returns int = @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

function decimalParamAndReturn(decimal a1) returns decimal = @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

 public function addTwoNumbersSlowAsyncVoidSig(int a, int b) returns int = @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

 public function addTwoNumbersFastAsyncVoidSig(int a, int b) returns int = @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

 public function addTwoNumbersSlowAsync(int a, int b) returns int = @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods",
    paramTypes: ["long" ,"long"]
} external;

 public function addTwoNumbersFastAsync(int a, int b) returns int = @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

public function testBalEnvSlowAsyncVoidSig() {
    int added = addTwoNumbersSlowAsyncVoidSig(1, 2);
    assertEquality(3, added);
}

public function testBalEnvFastAsyncVoidSig() {
    int added = addTwoNumbersFastAsyncVoidSig(1, 2);
    assertEquality(3, added);
}

public function testBalEnvSlowAsync() {
    int added = addTwoNumbersSlowAsync(1, 2);
    assertEquality(3, added);
}

public function testBalEnvFastAsync() {
    int added = addTwoNumbersFastAsync(1, 2);
    assertEquality(3, added);
}

function hashCode(int receiver) returns int = @java:Method {
    name: "hashCode",
    'class: "java.lang.Byte",
    paramTypes: ["byte"]
} external;

const ASSERTION_ERROR_REASON = "AssertionError";

function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }
    if expected === actual {
        return;
    }
    panic error(ASSERTION_ERROR_REASON,
                message = "expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}
