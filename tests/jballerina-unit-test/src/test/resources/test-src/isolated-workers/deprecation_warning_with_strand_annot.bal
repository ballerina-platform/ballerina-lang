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
    future<int> _ = @strand {thread: "parent"} start g1();
}

function f2() {
    future<string> _ = @strand {thread: "any"} start g2();
}

function f3() {
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

function f4() {
    ObjectType1 obj1 = client object {
        final int[] & readonly a = [1, 2, 3];
        final string b = "A";
        isolated remote function f1() returns int {
            return 20;
        }
    };
    future<int> _ = @strand {thread: "parent"} start obj1->f1();
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

function g1() returns int {
    return 1;
}

function g2(string d = "D", string... e) returns string {
    return "ABC" + d + (e.length() > 0 ? e[0] : "");
}

final readonly & string[] globalArr = ["B"];

type ObjectType1 readonly & client object {
    int[] & readonly a;
    string b;
    isolated remote function f1() returns int;
};
