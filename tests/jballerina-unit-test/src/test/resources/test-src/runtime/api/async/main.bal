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

int globalVar = 2;
public isolated class IsolatedClass {

    public final int a = 1;

    isolated function getA() returns int {
        return self.a;
    }

    public function callGetA() returns int = @java:Method {
        name: "isolatedGetA",
        'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Async"
    } external;

    public function callGetAWithoutConcurrent() returns int = @java:Method {
        name: "isolatedGetAWithoutConcurrent",
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

    public function callGetAWithoutConcurrent() returns int = @java:Method {
        name: "nonIsolatedGetAWithoutConcurrent",
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

    public function callGetAWithoutConcurrent() returns int = @java:Method {
        name: "isolatedServiceGetAWithoutConcurrent",
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

    public function callGetAWithoutConcurrent() returns int = @java:Method {
        name: "nonIsolatedServiceGetAWithoutConcurrent",
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

}

public function callAsyncNullObject() returns int|error = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Async"
} external;

public function callAsyncNullObjectMethod(IsolatedClass s) returns int|error = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Async"
} external;

public function callAsyncInvalidObjectMethod(IsolatedClass s) returns int|error = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Async"
} external;

public function callAsyncNullObjectWithoutConcurrent() returns int|error = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Async"
} external;

public function callAsyncNullObjectMethodWithoutConcurrent(IsolatedClass s) returns int|error = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Async"
} external;

public function callAsyncInvalidObjectMethodWithoutConcurrent(IsolatedClass s) returns int|error = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Async"
} external;

public function main() {
    IsolatedClass isolatedClass = new ();
    test:assertEquals(isolatedClass.callGetA(), 1);
    test:assertEquals(isolatedClass.callGetAWithoutConcurrent(), 1);
    test:assertTrue(isolatedClass.isIsolated());
    test:assertTrue(isolatedClass.isIsolatedFunction());

    NonIsolatedClass nonIsolatedClass = new ();
    test:assertEquals(nonIsolatedClass.callGetA(), 2);
    test:assertEquals(nonIsolatedClass.callGetAWithoutConcurrent(), 2);
    test:assertFalse(nonIsolatedClass.isIsolated());
    test:assertFalse(nonIsolatedClass.isIsolatedFunction());

    IsolatedServiceClass isolatedServiceClass = new ();
    test:assertEquals(isolatedServiceClass.callGetA(), 3);
    test:assertEquals(isolatedServiceClass.callGetAWithoutConcurrent(), 3);
    test:assertTrue(isolatedServiceClass.isIsolated());
    test:assertTrue(isolatedServiceClass.isIsolatedFunction());

    NonIsolatedServiceClass nonIsolatedServiceClass = new ();
    test:assertEquals(nonIsolatedServiceClass.callGetA(), 4);
    test:assertEquals(nonIsolatedServiceClass.callGetAWithoutConcurrent(), 4);
    test:assertFalse(nonIsolatedServiceClass.isIsolated());
    test:assertFalse(nonIsolatedServiceClass.isIsolatedFunction());

    // invokeAsync api calls negative test cases
    error|int r1 = trap callAsyncNullObject();
    test:assertTrue(r1 is error);
    error e1 = <error> r1;
    test:assertEquals(e1.message(), "object cannot be null");

    error|int r2 = trap callAsyncNullObjectMethod(isolatedClass);
    test:assertTrue(r2 is error);
    error e2 = <error> r2;
    test:assertEquals(e2.message(), "method name cannot be null");

    error|int r3 = trap callAsyncInvalidObjectMethod(isolatedClass);
    test:assertTrue(r3 is error);
    error e3 = <error> r3;
    test:assertEquals(e3.message(), "No such method: foo");

    // invokeAsync api calls deprecated method negative test cases
    error|int r4 = trap callAsyncNullObjectWithoutConcurrent();
    test:assertTrue(r4 is error);
    error e4 = <error> r4;
    test:assertEquals(e4.message(), "object cannot be null");

    error|int r5 = trap callAsyncNullObjectMethodWithoutConcurrent(isolatedClass);
    test:assertTrue(r5 is error);
    error e5 = <error> r5;
    test:assertEquals(e5.message(), "method name cannot be null");

    error|int r6 = trap callAsyncInvalidObjectMethodWithoutConcurrent(isolatedClass);
    test:assertTrue(r6 is error);
    error e6 = <error> r6;
    test:assertEquals(e6.message(), "No such method: foo");
}
