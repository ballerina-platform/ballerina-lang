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

class Baz {
    int iv;

    isolated function init(int j) {
        self.iv = j + i;
    }

    isolated function val() returns int {
        return self.iv + arr[0] + 100;
    }
}

isolated function testIsolatedObjectMethods() {
    Baz b = new (100);
    assertEquality(101, b.iv);

    assertEquality(202, b.val());
}

//// Service tests, only testing definition since dispatching is not possible atm ////

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

type Qux object {
    isolated function qux() returns int;
};

function testNonIsolatedMethodAsIsolatedMethodRuntimeNegative() {
    object {
        int i;

        function qux() returns int;
    } obj = object {
        int i;

        function init() {
            self.i = 123;
        }

        function qux() returns int {
            return self.i;
        }
    };

    assertEquality(false, <any> obj is Qux);
}

var fn1 = isolated function (int j, map<int> m) returns int {
    record {
        int i;
    } rec = {
        i: 1,
        "str": "val"
    };

    return rec.i + j + <int> m["first"] + i;
};

function (int, map<int>) returns int fn2 = function (int j, map<int> m) returns int {
    record {
        int i;
    } rec = {
        i: 1,
        "str": "val"
    };

    return rec.i + j + <int> m["first"] + i;
};

isolated function testIsolatedFunctionPointerInvocation() {
    int sum = fn1(100, {first: 123, second: 234}) + fn3();
    assertEquality(sum, 240);
    assertEquality(fn1(101, {first: 123, second: 234}), 226);
}

isolated function () returns int fn3 = isolated function () returns int {
    return 15;
};

function testIsolatedFunctionAsIsolatedFunctionRuntime() {
    assertEquality(true, <any> fn1 is isolated function (int, map<int>) returns int);
}

function testIsolatedFunctionAsIsolatedFunctionRuntimeNegative() {
    assertEquality(false, <any> fn2 is isolated function (int, map<int>) returns int);

    var res = trap <isolated function (int, map<int>) returns int> fn2;
    assertEquality(true, res is error);

    error err = <error> res;
    assertEquality("incompatible types: 'function (int,map) returns (int)' cannot be cast to " +
                        "'isolated function (int,map) returns (int)'", err.detail()["message"]);
}

const FLOAT = 1.23;

const map<float> FLOAT_MAP = {
    a: 1.0,
    b: 2.0
};

isolated function testConstantRefsInIsolatedFunctions() {
    assertEquality(4.23, FLOAT + <float> FLOAT_MAP["a"] + <float> FLOAT_MAP["b"]);
}

final int recI = 111222;

isolated function recJ() returns int => 234;

type RecWithDefaults record {|
    int i = recI;
    int j = recJ();
|};

isolated function testIsolatedClosuresAsRecordDefaultValues() {
    RecWithDefaults r = {};

    assertEquality(111222, r.i);
    assertEquality(234, r.j);
}

type ISOLATED_FUNCTION isolated function (int) returns int;

ISOLATED_FUNCTION af1 = intVal => intVal + i;

isolated function testIsolatedArrowFunctions() {
    isolated function (int) returns int af2 = intVal => intVal + 2 * i;

    int sum = af1(90) + af2(10);
    assertEquality(103, sum);
}

class ClassWithDefaultsWithoutInitFunc {
    int i = recI;
    int j = recJ();
    object {
        string k;
    } ob = object {
        string k = <string> ms["first"];
    };
}

class ClassWithDefaultsWithInitFunc {
    int i;
    int j;
    object {
        string k;
    } ob;

    isolated function init(int i) {
        self.i = i;
        self.j = af1(1);
        self.ob = object {
            string k;

            isolated function init() {
                self.k = <string> ms["second"];
            }
        };
    }
}

isolated function testIsolatedObjectFieldInitializers() {
    ClassWithDefaultsWithoutInitFunc c1 = new;
    assertEquality(111222, c1.i);
    assertEquality(234, c1.j);
    assertEquality("hello", c1.ob.k);

    ClassWithDefaultsWithInitFunc c2 = new (123);
    assertEquality(123, c2.i);
    assertEquality(2, c2.j);
    assertEquality("world", c2.ob.k);

    ClassWithDefaultsWithInitFunc c3 = object {
        int i = 21212;
        int j = 999;
        object {
            string k;
        } ob = object {
            string k;

            isolated function init() {
                self.k = "ballerina";
            }
        };
    };
    assertEquality(21212, c3.i);
    assertEquality(999, c3.j);
    assertEquality("ballerina", c3.ob.k);
}

client class ClientClass {
    int i = 1;

    remote isolated function bar() {
        self.i = 2;
    }

    isolated remote function baz(int j, string... s) returns int {
        int tot = self.i + j;

        foreach string strVal in s {
            tot = tot + strVal.length();
        }
        return tot;
    }
}

final string[] & readonly strArr = ["hello", "world"];

isolated function testIsolationAnalysisWithRemoteMethods() {
    ClientClass cc = new;

    cc->bar();
    assertEquality(2, cc.i);

    int x = cc->baz(1);
    assertEquality(3, x);

    x = cc->baz(arr[2], "hello");
    assertEquality(10, x);

    x = cc->baz(arr[1], ...strArr);
    assertEquality(14, x);
}

final map<int> & readonly intMap = {a: 1, b: 2};

isolated function testIsolatedFunctionWithDefaultableParams() {
    isolatedFunctionWithDefaultableParams();
    
    // https://github.com/ballerina-platform/ballerina-lang/issues/10639#issuecomment-699952927
    //assertEquality(<int[]> [2, 3, 4, 1, 2], isolatedAnonFunctionWithDefaultableParams(true));
    //assertEquality(<int[]> [1, 2], isolatedAnonFunctionWithDefaultableParams(false, y = [1, 2]));
    //assertEquality(<int[]> [1, 2, 1, 2], isolatedAnonFunctionWithDefaultableParams(true, y = [1, 2]));

    assertEquality(<int[]> [1, 2, 3, 4], isolatedAnonFunctionWithDefaultableParams(true, [1, 2], {c: 3, d: 4}));
}

isolated function isolatedFunctionWithDefaultableParams(int w = i, int[] x = getIntArray()) {
    assertEquality(1, w);
    assertEquality(<int[]> [1, 2, 3], x);
}

var isolatedAnonFunctionWithDefaultableParams =
    isolated function (boolean b, int[] y = getMutableIntArray(), map<int> z = intMap) returns any {
        if b {
            foreach var val in z {
                y.push(val);
            }
        }
        return y;
    };

isolated class IsolatedClass {
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

final IsolatedClass isolatedObject = new;

isolated function testAccessingFinalIsolatedObjectInIsolatedFunction() {
    IsolatedClass cl = isolatedObject;
    int[] arr = isolatedObject.getArray();

    assertEquality(<int[]> [1, 2], arr);
    assertEquality(<int[]> [1, 2], cl.getArray());
}

isolated function getIntArray() returns int[] => arr;

isolated function getMutableIntArray() returns int[] => [2, 3, 4];

isolated function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    panic error(string `expected '${expected.toString()}', found '${actual.toString()}'`);
}
