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

class Foo {
    function getInt() returns int {
        return 1;
    }
}

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

class Baz {
    int i;

    isolated function init(int j) {
        self.i = j + a;
    }

    isolated function val() returns int {
        return self.i + d[0] + 100;
    }
}

public class Listener {

    *'object:Listener;

    public function __attach(service s, string? name = ()) returns error? { }

    public function __detach(service s) returns error? { }

    public function __start() returns error? { }

    public function __gracefulStop() returns error? { }

    public function __immediateStop() returns error? { }
}

service s1 on new Listener() {
    isolated resource function res1(map<int> j) {
        int x = a + <int> j["val"];
    }

    resource isolated function res2(string str) returns error? {
        self.res3();
        return error(str + c);
    }

    isolated function res3() {
        int i = d[1];
    }
}

service s2 = service {
    isolated resource function res1(map<int> j) {
        int x = a + <int> j["val"];
    }

    resource isolated function res2(string str) returns error? {
        self.res3();
        return error(str + c);
    }

    isolated function res3() {
        int i = d[1];
    }
};

boolean bool = true;

isolated function testInvalidNonIsolatedAccessAsArgs() {
    diffParamKinds(a);
    diffParamKinds(a, c, ...[bool, false]);
    diffParamKinds(a, b = c);
    diffParamKinds(a, c, ...d);
    diffParamKinds(a, ...[c, bool, 1, a]);
}

isolated function diffParamKinds(int a, string b = "hello", boolean|int... c) {
}

var testInvalidIsolatedModuleAnonFunc = isolated function () {
    int y = nonIsolated() + d[0];
    string s = c + "world";
};

function testInvalidIsolatedAnonFunc() {
    var testInvalidIsolatedModuleAnonFunc = isolated function () {
        int y = nonIsolated() + d[0];
        string s = c + "world";
    };
}

// Invalid arrow functions as isolated functions.
type ISOLATED_FUNCTION isolated function (int) returns int;

ISOLATED_FUNCTION af1 = x => x + a;

function testInvalidArrowFuncAsIsolatedFunction() {
    ISOLATED_FUNCTION af2 = x => d[0] + x;

    int y = 100;
    isolated function (int) returns int af3 = x => x + y;
}

public isolated function testInvalidNonIsolatedRemoteMethodCalls() {
    ClientClass f = new;
    f->bar();
    int j = f->baz(a);
    int k = f->baz(a, c, c);
}

client class ClientClass {
    remote function bar() {
    }

    remote function baz(int i, string... s) returns int => 1;
}

final int[] & readonly immutableIntArr = [0, 1];

isolated function invalidIsolatedFunctionWithForkStatement(int i) {
    int j = immutableIntArr[1] + i;

    fork {
        worker w1 {

        }
    }
}

listener Listener ln = new;

isolated function testInvalidIsolatedFunctionAccessingNonIsolatedListener() {
    Listener ln2 = ln;
}

final map<int> intMap = {a: 1, b: 2};

isolated function testInvalidNonIsolatedFunctionWithNonIsolatedDefaults(int w = a, int[] x = getIntArray()) {
}

var testInvalidNonIsolatedAnonFunctionWithNonIsolatedDefaults =
    isolated function (boolean b, int[] y = getIntArray(), map<int> z = intMap) {
        if b {
            foreach var val in z {
                y.push(val);
            }
        }
    };

function getIntArray() returns int[] => d;

class NonIsolatedClass {
    final string a = "hello";
    private int[] b = [1, 2];

    isolated function getArray() returns int[] {
        lock {
            int[] x = self.b.clone();
            x.push(self.a.length());
            return x.clone();
        }
    }
}

final NonIsolatedClass nonIsolatedObject = new;

isolated function testInvalidAccessOfFinalNonIsolatedObjectInIsolatedFunction() {
    NonIsolatedClass cl = nonIsolatedObject;
    int[] arr = nonIsolatedObject.getArray();
}
