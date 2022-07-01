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

isolated function f1() returns string {
    worker sampleWorker {
        string m = "";
        m = <- function;
        string v = m + "result from sampleWorker";
        v -> function;
    }

    "result from function -> " -> sampleWorker;
    string result = "";
    result = <- sampleWorker;
    return result;
}

isolated function f2(string msg) returns string {
    worker sampleWorker1 returns string {
        "result from sampleWorker1 -> " -> sampleWorker2;
        string result = "";
        result = <- sampleWorker2;
        return result;
    }

    worker sampleWorker2 {
        string m = "";
        m = <- sampleWorker1;
        string v = msg + " -> " + m + "result from sampleWorker2";
        v -> sampleWorker1;
    }

    return wait sampleWorker1;
}

function testIsolatedWorkerInIsolatedFunction() {
    string v1 = f1();
    assertEquality("result from function -> result from sampleWorker", v1);

    string v2 = f2("hello");
    assertEquality("hello -> result from sampleWorker1 -> result from sampleWorker2", v2);
}

isolated function f3(string msg) returns string {
    final string a = "hello";
    worker sampleWorker1 returns string {
        "result from sampleWorker1 -> " -> sampleWorker2;
        string result = "";
        result = <- sampleWorker2;
        return a + f4() + result;
    }

    worker sampleWorker2 {
        string m = "";
        m = <- sampleWorker1;
        string v = msg + " -> " + m + "result from sampleWorker2";
        v -> sampleWorker1;
    }

    return wait sampleWorker1;
}

isolated function f4() returns string {
    return " -> result from f4 -> ";
}

final string str = "hello";

isolated function f5(string msg) returns string {
    final string a = str;
    worker sampleWorker1 returns string {
        "result from sampleWorker1 -> " -> sampleWorker2;
        string result = "";
        result = <- sampleWorker2;
        return a + f4() + result;
    }

    worker sampleWorker2 returns error? {
        string m = "";
        m = <- sampleWorker1;
        future<string> a1 = start f4();
        string v1 = checkpanic wait a1;
        string v = v1 + msg + " -> " + m + "result from sampleWorker2";
        v -> sampleWorker1;
    }

    return wait sampleWorker1;
}

function testIsolatedWorkerInIsolatedFunction2() {
    string v1 = f3("hi");
    assertEquality("hello -> result from f4 -> hi -> result from sampleWorker1 -> result from sampleWorker2", v1);

    string v2 = f5("hi");
    assertEquality("hello -> result from f4 ->  -> result from f4 -> hi -> result from sampleWorker1 -> result from sampleWorker2", v2);
}

public client class Client1 {
    isolated function f1(string[] a, string... b) returns string {
        final string a1 = a[0];
        final string b1 = b[0];
        worker sampleWorker {
            string m = "";
            m = <- function;
            string v = m + a1 + " from " + b1;
            v -> function;
        }

        "result from function -> " -> sampleWorker;
        string result = "";
        result = <- sampleWorker;
        return result;
    }

    isolated remote function f2(string[] a, string... b) returns string {
        final string a1 = a[0];
        final string b1 = b[0];
        worker sampleWorker {
            string m = "";
            m = <- function;
            string v = m + a1 + " from " + b1;
            v -> function;
        }

        "result from function -> " -> sampleWorker;
        string result = "";
        result = <- sampleWorker;
        return result;
    }
}

final readonly & string[] strArr = ["result"];

function testIsolatedWorkerInIsolatedFunctionInClient() {
    string str = "sampleWorker";
    Client1 client1 = new();
    string v1 = client1.f1(strArr, str);
    assertEquality("result from function -> result from sampleWorker", v1);

    string v2 = client1->f2(strArr, str);
    assertEquality("result from function -> result from sampleWorker", v2);
}

final string[] & readonly arr = ["result from module"];

function testIsolatedWorkerInIsolatedFunctionInClient2() {
    var obj1 = client object {
        isolated function f1(string[] a, string... b) returns string {
            final string a1 = a[0];
            final string b1 = b[0];
            worker sampleWorker {
                string m = "";
                m = <- function;
                string v = m + a1 + " from " + b1 + f4() + arr[0];
                v -> function;
            }

            "result from function -> " -> sampleWorker;
            string result = "";
            result = <- sampleWorker;
            return result;
        }

        isolated remote function f2(string[] a, string... b) returns string {
            final string a1 = a[0];
            final string b1 = b[0];
            worker sampleWorker {
                string m = "";
                m = <- function;
                string v = m + a1 + " from " + b1 + f4() + arr[0];
                v -> function;
            }

            "result from function -> " -> sampleWorker;
            string result = "";
            result = <- sampleWorker;
            return result;
        }
    };

    string v1 = obj1.f1(strArr, str);
    assertEquality("result from function -> result from hello -> result from f4 -> result from module", v1);

    string v2 = obj1->f2(strArr, str);
    assertEquality("result from function -> result from hello -> result from f4 -> result from module", v2);
}

isolated function f6(string msg) returns string {
    final string[] & readonly str1 = ["result from local"];
    fork {
        worker sampleWorker1 returns string {
            "result from sampleWorker1 -> " + arr[0] + " -> " -> sampleWorker2;
            string result = "";
            result = <- sampleWorker2;
            return result;
        }

        worker sampleWorker2 {
            string m = "";
            m = <- sampleWorker1;
            string v = msg + " -> " + m + "result from sampleWorker2 -> " + str1[0];
            v -> sampleWorker1;
        }
    }

    return wait sampleWorker1;
}

isolated function f7(string[] msgs) returns string|error {
    final string[] & readonly str1 = ["result from local"];
    final string msg = msgs[0];
    string str = "sampleWorker";
    fork {
        worker sampleWorker1 returns string|error {
            "result from sampleWorker1 -> " + arr[0] + " -> " -> sampleWorker2;
            future<string> futureStr = start f4();
            string someStr = checkpanic wait futureStr;
            string result = "";
            result = <- sampleWorker2;
            return result + someStr;
        }

        worker sampleWorker2 {
            string m = "";
            m = <- sampleWorker1;
            string v = msg + " -> " + m + "result from sampleWorker2 -> " + str1[0];
            v -> sampleWorker1;
        }
    }

    return wait sampleWorker1;
}

function testIsolatedWorkerInForkStmtInIsolatedFunction() {
    string v1 = f6("hi");
    assertEquality("hi -> result from sampleWorker1 -> result from module -> result from sampleWorker2 -> result from local", v1);

    string|error v2 = f7(["hi"]);
    assertEquality(true, v2 is string);
    if v2 is string {
        assertEquality("hi -> result from sampleWorker1 -> result from module -> result from sampleWorker2 -> result from local -> result from f4 -> ", v2);
    }
}

const ASSERTION_ERROR_REASON = "AssertionError";

function assertEquality(anydata expected, anydata actual) {
    if expected == actual {
        return;
    }

    panic error(ASSERTION_ERROR_REASON,
                message = "expected '" + expected.toString() + "', found '" + actual.toString() + "'");
}
