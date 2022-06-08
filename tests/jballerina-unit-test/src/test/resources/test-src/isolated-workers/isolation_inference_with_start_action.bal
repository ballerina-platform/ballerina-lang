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

import ballerina/jballerina.java;

function f1() {
    future<int> _ = start g1();
}

function g1() returns int {
    return 1;
}

function f2() {
    future<string> _ = start g2();
}

function g2(string d = "D", string... e) returns string {
    return "ABC" + d + (e.length() > 0 ? e[0] : "");
}

function f3() {
    var obj1 = object {
        final int[] & readonly a = [1, 2, 3];
        final string b = "A";

        function f1() returns int {
            return 10;
        }
    };
    future<int> _ = start obj1.f1();

    object {
        int[] & readonly a;
        string b;
        isolated function f1() returns int;
    } & readonly obj2 = object {
        final int[] & readonly a = [1, 2, 3];
        final string b = "A";

        isolated function f1() returns int {
            return 50;
        }
    };
    future<int> _ = start obj2.f1();
}

type ObjectType1 readonly & client object {
    int[] & readonly a;
    string b;
    isolated remote function f1() returns int;
};

function f4() {
    ObjectType1 obj1 = client object {
        final int[] & readonly a = [1, 2, 3];
        final string b = "A";
        isolated remote function f1() returns int {
            return 20;
        }
    };
    future<int> _ = start obj1->f1();
}

function testIsolationInferenceWithStartAction() {
    assertTrue(<any>f1 is isolated function);
    assertTrue(<any>f2 is isolated function);
    assertTrue(<any>f3 is isolated function);
    assertTrue(<any>f4 is isolated function);
}

function f5() {
    future<int> _ = @strand {thread: "parent"} start g1();
}

function f6() {
    future<string> _ = @strand {thread: "any"} start g2();
}

function f7() {
    var obj1 = object {
        final int[] & readonly a = [1, 2, 3];
        final string b = "A";

        function f1() returns int {
            return 10;
        }
    };
    future<int> _ = @strand {thread: "parent"} start obj1.f1();

    object {
        int[] & readonly a;
        string b;
        isolated function f1() returns int;
    } & readonly obj2 = object {
        final int[] & readonly a = [1, 2, 3];
        final string b = "A";

        isolated function f1() returns int {
            return 50;
        }
    };
    future<int> _ = @strand {thread: "any"} start obj2.f1();
}

function f8() {
    ObjectType1 obj1 = client object {
        final int[] & readonly a = [1, 2, 3];
        final string b = "A";
        isolated remote function f1() returns int {
            return 20;
        }
    };
    future<int> _ = @strand {thread: "parent"} start obj1->f1();
}

function testNonIsolationInferenceWithStartAction() {
    assertFalse(<any>f5 is isolated function);
    assertFalse(<any>f6 is isolated function);
    assertFalse(<any>f7 is isolated function);
    assertFalse(<any>f8 is isolated function);
}

function hello() returns string => "hello";

string helloGlobString = "hello!";

function g3() returns string {
    return helloGlobString;
}

isolated client class NonPublicIsolatedClass {
    private string str = "hello";

    remote function foo() returns int {
        _ = hello();
        future<int> _ = start g1();
        return 1;
    }

    function bar(string str) returns string {
        future<int> _ = start g1();
        return "";
    }
}

final string[] & readonly strArrr = [""];

service class NonPublicServiceClass {
    resource function get foo() {
        _ = hello();
        future<int> _ = start g1();
    }

    remote function bar() {
        NonPublicIsolatedClass cl = new;
        _ = cl.bar(strArrr[0]);
        future<string> _ = start cl.bar(strArrr[0]);
    }

    function func(string str) {
        int _ = str.length();
        future<int> _ = start g1();
    }

    public function func2(string str) {
        future<int> _ = start g1();
    }

    function func3(string str) {
        future<string> _ = start g3();
    }
}

public service class PublicServiceClass {
    resource function get foo() {
        _ = hello();
        future<int> _ = start g1();
    }

    remote function bar() {
        NonPublicIsolatedClass cl = new;
        _ = cl.bar(strArrr[0]);
        future<int> _ = start g1();
    }

    function func(string str) {
        int _ = str.length();
        NonPublicIsolatedClass cl = new;
        future<string> _ = start cl.bar(strArrr[0]);
    }

    public function func2(string str) {
        int _ = str.length();
        future<int> _ = start g1();
    }

    function func3(string str) {
        future<string> _ = start g3();
    }
}

function testServiceClassMethodIsolationInference() {
    assertTrue(isRemoteMethodIsolated(NonPublicIsolatedClass, "foo"));
    assertTrue(isMethodIsolated(NonPublicIsolatedClass, "bar"));

    assertTrue(isResourceIsolated(NonPublicServiceClass, "get", "foo"));
    assertTrue(isRemoteMethodIsolated(NonPublicServiceClass, "bar"));
    assertTrue(isMethodIsolated(NonPublicServiceClass, "func"));
    assertTrue(isMethodIsolated(NonPublicServiceClass, "func2"));
    assertFalse(isMethodIsolated(NonPublicServiceClass, "func3"));

    assertFalse(isResourceIsolated(PublicServiceClass, "get", "foo"));
    assertFalse(isRemoteMethodIsolated(PublicServiceClass, "bar"));
    assertFalse(isMethodIsolated(PublicServiceClass, "func"));
    assertFalse(isMethodIsolated(PublicServiceClass, "func2"));
    assertFalse(isMethodIsolated(PublicServiceClass, "func3"));
}

client class NonPublicNonIsolatedClass {
    remote function foo(string str) {
        int _ = str.length();
        NonPublicIsolatedClass cl = new;
        future<string> _ = start cl.bar(strArrr[0]);
    }

    function bar(string str) returns string|error {
        NonPublicIsolatedClass cl = new;
        future<string> res1 = start cl.bar(strArrr[0]);
        string res2 = checkpanic wait res1;
        return res2;
    }
}

public isolated client class PublicIsolatedClass {
    remote function foo(string str) {
        int _ = str.length();
        NonPublicIsolatedClass cl = new;
        future<string> _ = start cl.bar(strArrr[0]);
    }

    function bar(string str) returns string|error {
        NonPublicIsolatedClass cl = new;
        future<string> res1 = start cl.bar(strArrr[0]);
        string res2 = checkpanic wait res1;
        return res2;
    }
}

function testClientClassMethodIsolationInference() {
    NonPublicNonIsolatedClass c1 = new;
    NonPublicIsolatedClass c2 = new;
    PublicIsolatedClass c3 = new;

    assertTrue(isMethodIsolated(c1, "foo"));
    assertTrue(isMethodIsolated(c1, "bar"));

    assertTrue(isMethodIsolated(c2, "foo"));
    assertTrue(isMethodIsolated(c2, "bar"));

    assertFalse(isMethodIsolated(c3, "foo"));
    assertFalse(isMethodIsolated(c3, "bar"));
}

function assertTrue(anydata actual) => assertEquality(true, actual);

function assertFalse(anydata actual) => assertEquality(false, actual);

const ASSERTION_ERROR_REASON = "AssertionError";

function assertEquality(anydata expected, anydata actual) {
    if expected == actual {
        return;
    }

    panic error(ASSERTION_ERROR_REASON,
                message = "expected '" + expected.toString() + "', found '" + actual.toString() + "'");
}

function isResourceIsolated(service object {}|typedesc val, string resourceMethodName, string resourcePath)
returns boolean = @java:Method {
    'class: "org.ballerinalang.test.isolation.IsolationInferenceTest",
    paramTypes: ["java.lang.Object", "io.ballerina.runtime.api.values.BString", "io.ballerina.runtime.api.values.BString"]
} external;

function isRemoteMethodIsolated(object {}|typedesc val, string methodName) returns boolean = @java:Method {
    'class: "org.ballerinalang.test.isolation.IsolationInferenceTest",
    paramTypes: ["java.lang.Object", "io.ballerina.runtime.api.values.BString"]
} external;

function isMethodIsolated(object {}|typedesc val, string methodName) returns boolean = @java:Method {
    'class: "org.ballerinalang.test.isolation.IsolationInferenceTest",
    paramTypes: ["java.lang.Object", "io.ballerina.runtime.api.values.BString"]
} external;
