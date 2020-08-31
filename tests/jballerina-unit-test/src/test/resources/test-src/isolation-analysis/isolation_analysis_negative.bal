// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

int a = 12;

var b = {
    a: "Hello",
    b: "world",
    c: "from Ballerina"
};

string c = "c";

int[] d = [1, 2];

isolated function invalidIsolatedFunctionWithMutableGlobalVarRead() returns int {
    int w = a;
    record {} x = b;

    string? y = b.a;
    string? z = b[c];

    return d[a];
}

isolated function invalidIsolatedFunctionWithMutableGlobalVarWrite() {
    a = 321;
    b = {
        a: "a",
        b: "b",
        c: "c"
    };

    b.a = "aa";
    b["c"] = "cc";

    d[a] = 8765;
}

isolated function invalidIsolatedFunctionWithNonIsolatedFunctionCall() {
    int x = 1;
    int y = nonIsolated();
}

function nonIsolated() returns int => a;

type Foo object {
    function getInt() returns int {
        return 1;
    }
};

isolated function invalidIsolatedFunctionWithNonIsolatedMethodCall() {
    Foo f = new;
    int x = f.getInt();
}

isolated function invalidIsolatedFunctionWithWorkerDeclaration(int i) {
    int j = i + 1;

    worker w1 {
        string s = "hello world";
    }
}

isolated function invalidIsolatedFunctionWithStartCall() {
    future<int> ft = start nonIsolated();
}

type Bar record {
    int a;
    int b;
};

final Bar bar = {
    a: 100,
    b: 200
};

isolated function invalidIsolationFunctionAccessingMutableStorageViaFinalVar() {
    int a = bar.a;
}
