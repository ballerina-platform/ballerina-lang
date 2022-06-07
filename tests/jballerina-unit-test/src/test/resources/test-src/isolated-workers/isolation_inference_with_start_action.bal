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
    assertEquality(true, <any>f1 is isolated function);
    assertEquality(true, <any>f2 is isolated function);
    assertEquality(true, <any>f3 is isolated function);
    assertEquality(true, <any>f4 is isolated function);
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
