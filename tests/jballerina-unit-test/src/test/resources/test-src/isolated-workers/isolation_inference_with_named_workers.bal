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
    assertEquality(true, <any>f1 is isolated function);
    assertEquality(true, <any>f2 is isolated function);
    assertEquality(true, <any>f3 is isolated function);
    assertEquality(true, <any>f4 is isolated function);
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

function testNonIsolationInferenceWithNamedWorkersWithStrandAnnotation() {
    assertEquality(false, <any>f5 is isolated function);
    assertEquality(false, <any>f6 is isolated function);
    assertEquality(false, <any>f7 is isolated function);
    assertEquality(false, <any>f8 is isolated function);
}

const ASSERTION_ERROR_REASON = "AssertionError";

function assertEquality(anydata expected, anydata actual) {
    if expected == actual {
        return;
    }

    panic error(ASSERTION_ERROR_REASON,
                message = "expected '" + expected.toString() + "', found '" + actual.toString() + "'");
}
