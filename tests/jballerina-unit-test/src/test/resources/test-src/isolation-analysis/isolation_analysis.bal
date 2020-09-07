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

function testIsolatedFunctionWithOnlyLocalVars() {
    int x = isolatedFunctionWithOnlyLocalVars();
    assertEquality(4, x);
}

isolated function isolatedFunctionWithOnlyLocalVars() returns int {
    int i = 1;
    int j = i + 1;
    return j + 2;
}

function testIsolatedFunctionWithLocalVarsAndParams() {
    int x = isolatedFunctionWithOnlyLocalVarsAndParams({"two": 2});
    assertEquality(6, x);
}

isolated function isolatedFunctionWithOnlyLocalVarsAndParams(map<int> m) returns int {
    int i = 1;
    int j = i + <int> m["two"];
    return j + 3;
}

final int i = 1;

final readonly & map<string> ms = {
    "first": "hello",
    "second": "world"
};

isolated function testIsolatedFunctionAccessingImmutableGlobalStorage() {
    string concat = <string> ms["first"] + <string> ms["second"];
    assertEquality("helloworld", concat);
}

final int[] & readonly arr = [1, 2, 3];

type Baz object {
    int iv;

    isolated function init(int j) {
        self.iv = j + i;
    }

    isolated function val() returns int {
        return self.iv + arr[0] + 100;
    }
};

isolated function testIsolatedObjectMethods() {
    Baz b = new (100);
    assertEquality(101, b.iv);

    assertEquality(202, b.val());
}

//// Service tests, only testing definition since dispatching is not possible atm ////

public type Listener object {

    *'object:Listener;

    public function __attach(service s, string? name = ()) returns error? { }

    public function __detach(service s) returns error? { }

    public function __start() returns error? { }

    public function __gracefulStop() returns error? { }

    public function __immediateStop() returns error? { }
};

service s1 on new Listener() {
    isolated resource function res1(map<int> j) {
        int x = i + <int> j["val"];
    }

    resource isolated function res2(string str) returns error? {
        self.res3();
        return error(str + <string> ms["first"]);
    }

    isolated function res3() {

    }
}

service s2 = service {
    isolated resource function res1(map<int> j) {
        int x = i + <int> j["val"];
    }

    resource isolated function res2(string str) returns error? {
        self.res3();
        return error(str + <string> ms["first"]);
    }

    isolated function res3() {

    }
};

/////////////////////////////////////////////////////////////////////////////

type Qux abstract object {
    isolated function qux() returns int;
};

function testNonIsolatedMethodAsIsolatedMethodRuntimeNegative() {
    object {
        int i;

        function init(int i) {
            self.i = i;
        }

        function qux() returns int {
            return self.i;
        }
    } obj = new (123);

    assertEquality(false, <any> obj is Qux);
}

//var fn1 = isolated function (int j, map<int> m) returns int {
//    record {
//        int i;
//    } rec = {
//        i: 1,
//        "str": "val"
//    };
//
//    return rec.i + j + <int> m["first"] + i;
//};
//
//function (int, map<int>) returns int fn2 = function (int j, map<int> m) returns int {
//    record {
//        int i;
//    } rec = {
//        i: 1,
//        "str": "val"
//    };
//
//    return rec.i + j + <int> m["first"] + i;
//};
//
//function testIsolatedFunctionAsIsolatedFunctionRuntime() {
//    assertEquality(true, <any> fn1 is isolated function (int, map<int>) returns int);
//}
//
//function testIsolatedFunctionAsIsolatedFunctionRuntimeNegative() {
//    assertEquality(false, <any> fn2 is isolated function (int, map<int>) returns int);
//
//    var res = trap <isolated function (int, map<int>) returns int> fn2;
//    assertEquality(true, res is error);
//
//    error err = <error> res;
//    assertEquality("incompatible types: 'function (int,map) returns (int)' cannot be cast to " +
//                        "'isolated function (int, map<int>) returns int'", err.detail()["message"]);
//}

isolated function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    panic error("AssertionError");
    //panic error(string `expected '${expected.toString()}', found '${actual.toString()}'`);
}
