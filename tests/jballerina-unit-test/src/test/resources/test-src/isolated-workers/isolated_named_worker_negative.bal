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

isolated function testNonIsolatedNamedWorkerInIsolatedFunc(string msg) {
    _ = f2();
    worker sampleWorker1 {
        f1(msg);
    }
}

function f1(string m) {
}

isolated function f2() {
}

int intVal = 12;

isolated function testNonIsolatedNamedWorkerInIsolatedFunc2() {
    _ = f2();
    worker sampleWorker1 {
        int _ = intVal;
    }
}

int[] arr = [];

isolated function testNonIsolatedNamedWorkerInIsolatedFunc3() {
    _ = f2();
    worker sampleWorker1 {
        arr = [0, 1];
    }
}

isolated function testNonIsolatedNamedWorkerInIsolatedFunc4() {
    _ = f2();
    int a = 12;
    worker sampleWorker1 {
        int _ = a;
    }
}

isolated function testNonIsolatedNamedWorkerInIsolatedFunc5() {
    _ = f2();
    worker sampleWorker1 {
        fork {
            worker A returns int {
                return intVal;
            }
        }
    }
}

isolated function testNonIsolatedNamedWorkerInIsolatedFunc6() {
    _ = f2();
    worker sampleWorker1 {
        future<()> _ = start f1("");
    }
}

isolated function testNonIsolatedNamedWorkerInIsolatedFunc7() {
    _ = f2();
    worker sampleWorker1 {
        future<()> _ = start f3(arr);
    }
}

isolated function f3(int[] a) {
}

isolated function testNamedWorkerWithStrandAnnotInIsolatedFunc() {
    @strand{thread:"parent"}
    worker sampleWorker1 {
    }
}

isolated function testNamedWorkerWithStrandAnnotInIsolatedFunc2() {
    fork {
        @strand {thread: "parent"}
        worker sampleWorker1 {
        }

        @strand {thread: "any"}
        worker sampleWorker2 {
        }
    }
}
