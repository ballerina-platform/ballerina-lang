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
}

type ObjectType1 readonly & client object {
    int[] & readonly a;
    string b;
    isolated remote function f1() returns int;
};

function f4() {
    object {
        isolated function f1();
    } obj1 = object {
        isolated function f1() {
        }
    };
    future<()> _ = start obj1.f1();
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

function f9() {
    int[] a = [];
    future<()> _ = start g4(a);
}

function g4(int[] arr) {
}

function f10() {
    int[] a = [];
    future<()> _ = start g5([1, 2], a);
}

function g5(int[] arr, int[]... v) {
}

function f11() {
    client object {
        remote function f1();
    } obj1 = client object {
        remote function f1() {
        }
    };
    future<()> _ = start obj1->f1();
}

function f12() {
    object {
        function f1();
    } obj1 = object {
        function f1() {
        }
    };
    future<()> _ = start obj1.f1();
}

function f13() {
    int[] arr = [];
    var obj1 = object {
        function f1(int[] a, int[]... b) {
        }
    };
    future<()> _ = start obj1.f1(arr);
}

function f14() {
    int[] arr = [];
    var obj1 = object {
        function f1(int[] a, int[]... b) {
        }
    };
    future<()> _ = start obj1.f1([], arr);
}

function f15() {
    int[] arr = [];
    var obj1 = client object {
        remote function f1(int[] a, int[]... b) {
        }
    };
    future<()> _ = start obj1->f1(arr);
}

function f16() {
    int[] arr = [];
    var obj1 = client object {
        remote function f1(int[] a, int[]... b) {
        }
    };
    future<()> _ = start obj1->f1([], arr);
}

function testNonIsolationInferenceWithStartAction() {
    assertFalse(<any>f5 is isolated function);
    assertFalse(<any>f6 is isolated function);
    assertFalse(<any>f7 is isolated function);
    assertFalse(<any>f8 is isolated function);
    assertFalse(<any>f9 is isolated function);
    assertFalse(<any>f10 is isolated function);
    assertFalse(<any>f11 is isolated function);
    assertFalse(<any>f12 is isolated function);
    assertFalse(<any>f13 is isolated function);
    assertFalse(<any>f14 is isolated function);
    assertFalse(<any>f15 is isolated function);
    assertFalse(<any>f16 is isolated function);
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

service class NonPublicServiceClass2 {
    resource function get foo() {
        string[] tt = [];
        NonPublicIsolatedClass2 cl = new;
        future<string> _ = start cl.bar(tt);
    }

    remote function bar() {
        string[] tt = [];
        NonPublicIsolatedClass2 cl = new;
        future<int> _ = start cl->foo(tt);
    }

    function func(string str) {
        string[] tt = [];
        NonPublicIsolatedClass2 cl = new;
        future<int> _ = start cl->foo(tt);
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

    assertFalse(isResourceIsolated(NonPublicServiceClass2, "get", "foo"));
    assertFalse(isRemoteMethodIsolated(NonPublicServiceClass2, "bar"));
    assertFalse(isMethodIsolated(NonPublicServiceClass2, "func"));

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

isolated client class NonPublicIsolatedClass2 {
    private string str = "hello";

    remote function foo(string[] str) returns int {
        _ = hello();
        future<int> _ = start g1();
        return 1;
    }

    function bar(string[] str) returns string {
        future<int> _ = start g1();
        return "";
    }
}

client class NonPublicNonIsolatedClass2 {
    remote function foo(string str) {
        string[] tt = [];
        NonPublicIsolatedClass2 cl = new;
        future<int> _ = start cl->foo(tt);
    }

    function bar(string[] str) returns string|error {
        string[] tt = [];
        NonPublicIsolatedClass2 cl = new;
        future<string> res1 = start cl.bar(tt);
        string res2 = checkpanic wait res1;
        return res2;
    }
}

function testClientClassMethodIsolationInference() {
    NonPublicNonIsolatedClass c1 = new;
    NonPublicIsolatedClass c2 = new;
    PublicIsolatedClass c3 = new;
    NonPublicNonIsolatedClass2 c4 = new;

    assertTrue(isMethodIsolated(c1, "foo"));
    assertTrue(isMethodIsolated(c1, "bar"));

    assertTrue(isMethodIsolated(c2, "foo"));
    assertTrue(isMethodIsolated(c2, "bar"));

    assertFalse(isMethodIsolated(c4, "foo"));
    assertFalse(isMethodIsolated(c4, "bar"));

    assertFalse(isMethodIsolated(c3, "foo"));
    assertFalse(isMethodIsolated(c3, "bar"));
}

function f17() {
    _ = start invoke();
}

function f18() {
    _ = start invoke2();
}

public isolated function invoke() {
}

public function invoke2() {
}


function testIsolationInferenceWithStarActionInvokingPublicFunction() {
    assertTrue(<any>f17 is isolated function ());
    assertFalse(<any>f18 is isolated function ());
}

listener Listener ep = new ();

service on ep {
    resource function get foo() returns string {
        _ = start invoke();
        return "Complete";
    }

    resource function get bar() returns string {
        _ = start invoke2();
        return "Complete";
    }

    remote function baz() returns string {
        _ = start invoke();
        return "Complete";
    }

    remote function bam() returns string {
        _ = start invoke2();
        return "Complete";
    }
}

class Listener {
    public function attach(service object {} s, string|string[]? name = ()) returns error?  = @java:Method {
                                       name: "testServiceDeclarationMethodIsolationInference",
                                       'class: "org.ballerinalang.test.isolation.IsolatedWorkerTest"
                                   } external;

    public function detach(service object {} s) returns error? {
    }

    public function 'start() returns error? {
    }

    public function gracefulStop() returns error? {
    }

    public function immediateStop() returns error? {
    }
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
