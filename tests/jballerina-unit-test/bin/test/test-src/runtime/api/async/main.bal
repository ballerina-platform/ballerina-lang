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

import ballerina/test;
import ballerina/jballerina.java;
import ballerina/lang.runtime;

int globalVar = 2;

public class ObjectMethodsCallClass {
    int n = 5;

    function getFieldValWithNoArgs() returns int {
        return self.n;
    }

    function getFieldValWithRequiredArg(int num) returns int {
        self.n += num;
        return self.n;
    }

    function getFieldValWithOptionalArg(string fieldName = "n") returns int {
        if (fieldName == "n") {
            return self.n;
        } else {
            return -1;
        }
    }

    function getFieldValWithMultipleOptionalArgs(int a = 3, int b = a, int c = a + b) returns int {
        return a + b + c;
    }

    function getFieldValWithMultipleOptionalArgsAsync(int a = asyncFunction(3), int b = asyncFunction(a),
                                                      int c = asyncFunction(a + b)) returns int {
        return a + b + c;
    }

    function getFieldValWithDefaultValSpecialChars(int param\.a = 3, int param\:b = param\.a, int param\;c = param\.a + param\:b) returns int {
        return param\.a + param\:b + param\;c;
    }

    function getFieldValWithDefaultValSpecialCharsAsync(int param\.a = asyncFunction(3), int param\:b = asyncFunction(param\.a),
                                                      int param\;c = asyncFunction(param\.a + param\:b)) returns int {
        return param\.a + param\:b + param\;c;
    }

    public function callGetFieldValWithNoArgs() returns int = @java:Method {
        name: "getFieldValWithNoArgs",
        'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Async"
    } external;

    public function callGetFieldValWithRequiredArg(int num) returns int = @java:Method {
        name: "getFieldValWithRequiredArg",
        'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Async"
    } external;

    public function callGetFieldValWithOptionalArgDefaultVal() returns int = @java:Method {
        name: "getFieldValWithOptionalArgDefaultVal",
        'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Async"
    } external;

    public function callGetFieldValWithMultipleOptionalArgsDefaultVal() returns int = @java:Method {
        name: "getFieldValWithMultipleOptionalArgsDefaultVal",
        'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Async"
    } external;

    public function callGetFieldValWithMultipleOptionalArgsDefaultValAsync() returns int = @java:Method {
        name: "getFieldValWithMultipleOptionalArgsDefaultValAsync",
        'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Async"
    } external;

    public function callGetFieldValWithProvidedOptionalArgVal(string fieldName) returns int = @java:Method {
        name: "getFieldValWithProvidedOptionalArgVal",
        'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Async"
    } external;

    public function callGetFieldValWithDefaultValSpecialChars() returns int = @java:Method {
        name: "getFieldValWithDefaultValSpecialChars",
        'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Async"
    } external;

    public function callGetFieldValWithDefaultValSpecialCharsAsync() returns int = @java:Method {
        name: "getFieldValWithDefaultValSpecialCharsAsync",
        'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Async"
    } external;
}

public isolated class IsolatedClass {

    public final int a = 1;

    isolated function getA() returns int {
        return self.a;
    }

    public function callGetA() returns int = @java:Method {
        name: "isolatedGetA",
        'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Async"
    } external;

    public function asyncGetA() returns int = @java:Method {
        name: "getA",
        'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Async"
    } external;

    public function isIsolated() returns boolean = @java:Method {
        name: "isolatedClassIsIsolated",
        'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Async"
    } external;

    public function isIsolatedFunction() returns boolean = @java:Method {
        name: "isolatedClassIsIsolatedFunction",
        'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Async"
    } external;

    public function isIsolatedFunctionWithName(string method) returns boolean = @java:Method {
        name: "isIsolatedFunctionWithName",
        'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Async"
    } external;
}

class NonIsolatedClass {

    public int a = 5;

    function getA() returns int {
        self.a = globalVar;
        return self.a;
    }

    public function callGetA() returns int = @java:Method {
        name: "nonIsolatedGetA",
        'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Async"
    } external;

    public function asyncGetA() returns int = @java:Method {
        name: "getA",
        'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Async"
    } external;

    public function isIsolated() returns boolean = @java:Method {
        name: "nonIsolatedClassIsIsolated",
        'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Async"
    } external;

    public function isIsolatedFunction() returns boolean = @java:Method {
        name: "nonIsolatedClassIsIsolatedFunction",
        'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Async"
    } external;

    public function isIsolatedFunctionWithName(string method) returns boolean = @java:Method {
        name: "isIsolatedFunctionWithName",
        'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Async"
    } external;
}

isolated service class IsolatedServiceClass {

    public final int a = 3;
    isolated resource function getA .() returns int {
        return self.a;
    }

    public function callGetA() returns int = @java:Method {
        name: "isolatedServiceGetA",
        'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Async"
    } external;

    public function asyncGetA() returns int = @java:Method {
        name: "getResourceA",
        'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Async"
    } external;

    public function isIsolated() returns boolean = @java:Method {
        name: "isolatedServiceIsIsolated",
        'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Async"
    } external;

    public function isIsolatedFunction() returns boolean = @java:Method {
        name: "isolatedServiceIsIsolatedFunction",
        'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Async"
    } external;

    public function isIsolatedFunctionWithName(string method) returns boolean = @java:Method {
        name: "isIsolatedFunctionWithName",
        'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Async"
    } external;
}

service class NonIsolatedServiceClass {

    public int a = 2;
    resource function getA .() returns int {
        self.a = globalVar + 2;
        return self.a;
    }

    public function callGetA() returns int = @java:Method {
        name: "nonIsolatedServiceGetA",
        'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Async"
    } external;

    public function asyncGetA() returns int = @java:Method {
        name: "getResourceA",
        'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Async"
    } external;

    public function isIsolated() returns boolean = @java:Method {
        name: "nonIsolatedServiceIsIsolated",
        'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Async"
    } external;

    public function isIsolatedFunction() returns boolean = @java:Method {
        name: "nonIsolatedServiceIsIsolatedFunction",
        'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Async"
    } external;

    public function isIsolatedFunctionWithName(string method) returns boolean = @java:Method {
        name: "isIsolatedFunctionWithName",
        'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Async"
    } external;

}

function asyncFunction(int x) returns int {
    runtime:sleep(0.1);
    return x;
}

public function callAsyncNullObjectSequentially() returns int|error = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Async"
} external;

public function callAsyncNullObjectMethodSequentially(IsolatedClass s) returns int|error = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Async"
} external;

public function callAsyncInvalidObjectMethodSequentially(IsolatedClass s) returns int|error = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Async"
} external;

public function callAsyncNullObjectConcurrently() returns int|error = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Async"
} external;

public function callAsyncNullObjectMethodConcurrently(IsolatedClass s) returns int|error = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Async"
} external;

public function callAsyncInvalidObjectMethodConcurrently(IsolatedClass s) returns int|error = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Async"
} external;


public function callAsyncNullObject() returns int|error = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Async"
} external;

public function callAsyncNullObjectMethod(IsolatedClass s) returns int|error = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Async"
} external;

public function callAsyncInvalidObjectMethod(IsolatedClass s) returns int|error = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Async"
} external;


int globalVar1 = 2;

public service class NonIsolatedServiceClass1 {

    resource function getA .() returns int {
        globalVar1 = globalVar1 + 2;
        return globalVar1;
    }

    public function getNonIsolatedResourceA() returns int = @java:Method {
        name: "getNonIsolatedResourceA",
        'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Async"
    } external;
}

public service class NonIsolatedServiceClass2 {
    resource function getB .() returns int {
        globalVar1 = globalVar1 + 2;
        return globalVar1;
    }

    public function getNonIsolatedResourceB() returns int = @java:Method {
        name: "getNonIsolatedResourceB",
        'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Async"
    } external;

}

public function main() {
    ObjectMethodsCallClass objectMethodsCallClass = new ();
    test:assertEquals(objectMethodsCallClass.callGetFieldValWithNoArgs(), 5);
    test:assertEquals(objectMethodsCallClass.callGetFieldValWithRequiredArg(10), 15);
    test:assertEquals(objectMethodsCallClass.callGetFieldValWithOptionalArgDefaultVal(), 15);
    test:assertEquals(objectMethodsCallClass.callGetFieldValWithMultipleOptionalArgsDefaultVal(), 12);
    test:assertEquals(objectMethodsCallClass.callGetFieldValWithMultipleOptionalArgsDefaultValAsync(), 12);
    test:assertEquals(objectMethodsCallClass.callGetFieldValWithProvidedOptionalArgVal("not a field"), -1);
    test:assertEquals(objectMethodsCallClass.callGetFieldValWithDefaultValSpecialChars(), 12);
    test:assertEquals(objectMethodsCallClass.callGetFieldValWithDefaultValSpecialCharsAsync(), 12);

    IsolatedClass isolatedClass = new ();
    test:assertEquals(isolatedClass.callGetA(), 1);
    test:assertEquals(isolatedClass.asyncGetA(), 1);
    test:assertTrue(isolatedClass.isIsolated());
    test:assertTrue(isolatedClass.isIsolatedFunction());
    test:assertTrue(isolatedClass.isIsolatedFunctionWithName("getA"));

    NonIsolatedClass nonIsolatedClass = new ();
    test:assertEquals(nonIsolatedClass.callGetA(), 2);
    test:assertEquals(nonIsolatedClass.asyncGetA(), 2);
    test:assertFalse(nonIsolatedClass.isIsolated());
    test:assertFalse(nonIsolatedClass.isIsolatedFunction());
    test:assertFalse(nonIsolatedClass.isIsolatedFunctionWithName("getA"));

    IsolatedServiceClass isolatedServiceClass = new ();
    test:assertEquals(isolatedServiceClass.callGetA(), 3);
    test:assertEquals(isolatedServiceClass.asyncGetA(), 3);
    test:assertTrue(isolatedServiceClass.isIsolated());
    test:assertTrue(isolatedServiceClass.isIsolatedFunction());
    test:assertTrue(isolatedServiceClass.isIsolatedFunctionWithName("$gen$$getA$$0046"));

    NonIsolatedServiceClass nonIsolatedServiceClass = new ();
    test:assertEquals(nonIsolatedServiceClass.callGetA(), 4);
    test:assertEquals(nonIsolatedServiceClass.asyncGetA(), 4);
    test:assertFalse(nonIsolatedServiceClass.isIsolated());
    test:assertFalse(nonIsolatedServiceClass.isIsolatedFunction());
    test:assertFalse(nonIsolatedServiceClass.isIsolatedFunctionWithName("$gen$$getA$$0046"));

    // invokeAsync api calls negative test cases

    // deprecated a
    error|int r1 = trap callAsyncNullObjectSequentially();
    test:assertTrue(r1 is error);
    error e1 = <error> r1;
    test:assertEquals(e1.message(), "object cannot be null");

    error|int r2 = trap callAsyncNullObjectMethodSequentially(isolatedClass);
    test:assertTrue(r2 is error);
    error e2 = <error> r2;
    test:assertEquals(e2.message(), "method name cannot be null");

    error|int r3 = trap callAsyncInvalidObjectMethodSequentially(isolatedClass);
    test:assertTrue(r3 is error);
    error e3 = <error> r3;
    test:assertEquals(e3.message(), "No such method: foo");

    error|int r4 = trap callAsyncNullObjectConcurrently();
    test:assertTrue(r4 is error);
    error e4 = <error> r4;
    test:assertEquals(e4.message(), "object cannot be null");

    error|int r5 = trap callAsyncNullObjectMethodConcurrently(isolatedClass);
    test:assertTrue(r5 is error);
    error e5 = <error> r5;
    test:assertEquals(e5.message(), "method name cannot be null");

    error|int r6 = trap callAsyncInvalidObjectMethodConcurrently(isolatedClass);
    test:assertTrue(r6 is error);
    error e6 = <error> r6;
    test:assertEquals(e6.message(), "No such method: foo");

    // invokeAsync api calls deprecated method negative test cases
    error|int r7 = trap callAsyncNullObject();
    test:assertTrue(r7 is error);
    error e7 = <error> r7;
    test:assertEquals(e7.message(), "object cannot be null");

    error|int r8 = trap callAsyncNullObjectMethod(isolatedClass);
    test:assertTrue(r8 is error);
    error e8 = <error> r8;
    test:assertEquals(e8.message(), "method name cannot be null");

    error|int r9 = trap callAsyncInvalidObjectMethod(isolatedClass);
    test:assertTrue(r9 is error);
    error e9 = <error> r9;
    test:assertEquals(e9.message(), "No such method: foo");

    testNonIsolatedSequentialCall();
}

function testNonIsolatedSequentialCall() {
    NonIsolatedServiceClass1 obj1 = new ();
    NonIsolatedServiceClass2 obj2 = new ();

    test:assertEquals(obj1.getNonIsolatedResourceA(), 4);
    test:assertEquals(obj2.getNonIsolatedResourceB(), 6);
}
