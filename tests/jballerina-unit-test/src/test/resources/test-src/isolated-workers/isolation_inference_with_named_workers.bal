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
    worker A {
        _ = g1();
    }
}

function g1() returns int {
    return 1;
}

function f2() {
    worker A {
        _ = g1();
    }

    worker B {
        _ = g1();
    }
}

final readonly & string[] globalArr = ["B"];

function f3(string[] & readonly arr) {
    final string a = "A";
    worker A {
        string _ = a + arr[0];
        _ = g1();
    }

    worker B {
        string _ = globalArr[0];
        _ = g1();
    }
}

function f4(string[] & readonly arr) {
    final string a = "A";
    worker A {
        string _ = a + arr[0];
        future<int> _ = start g1();
    }

    worker B {
        string _ = globalArr[0];
        var obj1 = object {
            function f1() returns string {
                return globalArr[0] + a;
            }

            function f2() returns string {
                return "A";
            }
        };
        future<string> _ = start obj1.f1();
        future<string> _ = start obj1.f2();
    }
}

function testIsolationInferenceWithNamedWorkers() {
    assertTrue(<any>f1 is isolated function);
    assertTrue(<any>f2 is isolated function);
    assertTrue(<any>f3 is isolated function);
    assertTrue(<any>f4 is isolated function);
}

function f5() {
    @strand {thread: "parent"}
    worker A {
        _ = g1();
    }
}

function f6() {
    @strand {thread: "any"}
    worker A {
        _ = g1();
    }

    worker B {
        _ = g1();
    }
}

function f7(string[] & readonly arr) {
    final string a = "A";
    worker A {
        string _ = a + arr[0];
        _ = g1();
    }

    @strand {thread: "parent"}
    worker B {
        string _ = globalArr[0];
        _ = g1();
    }
}

function f8(string[] & readonly arr) {
    final string a = "A";

    @strand {thread: "parent"}
    worker A {
        string _ = a + arr[0];
        future<int> _ = start g1();
    }

    worker B {
        string _ = globalArr[0];
        var obj1 = object {
            function f1() returns string {
                return globalArr[0] + a;
            }

            function f2() returns string {
                return "A";
            }
        };
        future<string> _ = start obj1.f1();
        future<string> _ = start obj1.f2();
    }
}

function f9() {
    int[] tt = [];
    worker A {
        future<()> _ = start g2(tt);
    }
}

function f10() {
    int[] tt = [];
    worker A {
        future<()> _ = start g2([1, 2]);
    }

    worker B {
        future<()> _ = start g2(tt);
    }
}

function f11() {
    int[] tt = [];
    fork {
        worker A {
            future<()> _ = start g2([1, 2]);
        }

        worker B {
            future<()> _ = start g2(tt);
        }
    }
}

function g2(int[] arr) {
}

function f12() {
    int[] tt = [];
    worker A {
        future<()> _ = start g3([], tt);
    }
}

function f13() {
    int[] tt = [];
    worker A {
        future<()> _ = start g3([1, 2]);
    }

    worker B {
        future<()> _ = start g3([], tt);
    }
}

function f14() {
    int[] tt = [];
    fork {
        worker A {
            future<()> _ = start g2([1, 2]);
        }

        worker B {
            future<()> _ = start g2(tt);
        }
    }
}

function g3(int[] arr, int[]... v) {
}

isolated client class NonPublicIsolatedClass2 {
    private string str = "hello";

    remote function foo(int[] str) returns int {
        _ = hello();
        future<int> _ = start g1();
        return 1;
    }

    function bar(int[] str, int[]... m) returns int {
        future<int> _ = start g1();
        return 2;
    }
}

function f15() {
    int[] tt = [];
    worker A {
        NonPublicIsolatedClass2 cl = new;
        future<int> _ = start cl->foo(tt);
    }
}

function f16() {
    int[] tt = [];
    worker A {
        NonPublicIsolatedClass2 cl = new;
        future<int> _ = start cl->foo([]);
    }

    worker B {
        NonPublicIsolatedClass2 cl = new;
        future<int> _ = start cl.bar([], tt);
    }
}

function f17() {
    int[] tt = [];
    fork {
        worker A {
            NonPublicIsolatedClass2 cl = new;
            future<int> _ = start cl->foo(tt);
        }

        worker B {
            NonPublicIsolatedClass2 cl = new;
            future<int> _ = start cl.bar([]);
        }
    }
}

function testNonIsolationInferenceWithNamedWorkersWithStrandAnnotation() {
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
    assertFalse(<any>f17 is isolated function);
}

function hello() returns string => "hello";

string helloGlobString = "hello!";

isolated client class NonPublicIsolatedClass {
    private string str = "hello";

    remote function foo() returns int {
        final string a = "A";
        worker A {
            if a == "A" {
                return;
            }
        }
        return 1;
    }

    function bar(string str) returns string {
        worker B returns string {
            return str;
        }
        return "";
    }
}

final string[] & readonly strArrr = [""];

service class NonPublicServiceClass {
    resource function get foo() {
        worker A {
            _ = hello();
        }
    }

    remote function bar() {
        worker B {
            NonPublicIsolatedClass cl = new;
            _ = cl.bar(strArrr[0]);
        }
    }

    function func(string str) {
        worker C {
            int length = str.length();
        }
    }

    public function func2(string str) {
        worker D {
            int length = str.length();
        }
    }

    function func3(string str) {
        worker E {
            helloGlobString = str;
        }
    }
}

public service class PublicServiceClass {
    resource function get foo() {
        worker A {
            _ = hello();
        }
    }

    remote function bar() {
        worker B {
            NonPublicIsolatedClass cl = new;
            _ = cl.bar(strArrr[0]);
        }
    }

    function func(string str) {
        worker C {
            int length = str.length();
        }
    }

    public function func2(string str) {
        worker D {
            int length = str.length();
        }
    }

    function func3(string str) {
        worker E {
            helloGlobString = str;
        }
    }
}

function testServiceClassMethodIsolationInference() {
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
    remote function foo() {
        worker A {
            NonPublicIsolatedClass cl = new;
            _ = cl.bar(strArrr[0]);
        }
    }

    function bar(string str) returns string => str;
}

public isolated client class PublicIsolatedClass {
    remote function foo() {
        worker A {
            NonPublicIsolatedClass cl = new;
            _ = cl.bar(strArrr[0]);
        }
    }

    function bar(string str) returns string => str;
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
