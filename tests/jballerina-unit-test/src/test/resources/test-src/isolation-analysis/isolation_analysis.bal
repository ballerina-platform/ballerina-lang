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

    public function attach(service object {} s, string[]|string? name = ()) returns error? { }

    public function detach(service object {} s) returns error? { }

    public function 'start() returns error? { }

    public function gracefulStop() returns error? { }

    public function immediateStop() returns error? { }
}

service /s1 on new Listener() {
    isolated resource function get res1(map<int> j) {
        int x = i + <int> j["val"];
    }

    resource isolated function get res2(string str) returns error? {
        lock {
            self.res3();
        }
        return error(str + <string> ms["first"]);
    }

    isolated function res3() {

    }
}

service object {} s2 = service object {
    isolated resource function get res1(map<int> j) {
        int x = i + <int> j["val"];
    }

    resource isolated function get res2(string str) returns error? {
        lock {
            self.res3();
        }
        return error(str + <string> ms["first"]);
    }

    isolated function res3() {

    }
};

/////////////////////////////////////////////////////////////////////////////

type Qux object {
    isolated function qux() returns int;
};

int globInt = 1;

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
            return self.i + globInt;
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

function (int, map<int>) returns int fn3 = isolated function (int j, map<int> m) returns int {
    record {
        int i;
    } rec = {
        i: 1,
        "str": "val"
    };
    return rec.i + j + <int> m["first"] + i;
};

isolated function testIsolatedFunctionPointerInvocation() {
    int sum = fn1(100, {first: 123, second: 234}) + fn4();
    assertEquality(sum, 240);
    assertEquality(fn1(101, {first: 123, second: 234}), 226);
}

IsolatedFunction fn4 = isolated function () returns int {
    return 15;
};

function testIsolatedFunctionAsIsolatedFunctionRuntime() {
    assertEquality(true, <any> fn1 is isolated function (int, map<int>) returns int);
    assertEquality(true, <any> fn2 is isolated function (int, map<int>) returns int);
    assertEquality(true, <any> fn3 is isolated function (int, map<int>) returns int);
}
int k = 8;
function (int, map<int>) returns int fn5 = function (int j, map<int> m) returns int {
    return j + <int> m["first"] + i + k;
};

function testIsolatedFunctionAsIsolatedFunctionRuntimeNegative() {
    assertEquality(false, <any> fn5 is isolated function (int, map<int>) returns int);

    var res = trap <isolated function (int, map<int>) returns int> fn5;
    assertEquality(true, res is error);

    error err = <error> res;
    assertEquality("incompatible types: 'function (int,map<int>) returns (int)' cannot be cast to " +
                        "'isolated function (int,map<int>) returns (int)'", err.detail()["message"]);
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

    assertEquality(<int[]> [1, 2, 5], arr);
    assertEquality(<int[]> [1, 2, 5], cl.getArray());
}

final (map<int> & readonly)|isolated object {} unionOne = {a: 1, b: 2};
final (map<int> & readonly)|IsolatedClass unionTwo = isolatedObject;

isolated function testIsolatedFuncAccessingSubTypeOfReadOnlyOrIsolatedObjectUnion() {
    var x = unionOne;
    var y = unionTwo;
}

isolated function getIntArray() returns int[] => arr;

isolated function getMutableIntArray() returns int[] => [2, 3, 4];

isolated class IsolatedClassWithIsolatedMethod {
    private int i = 0;

    isolated function foo(int i) returns int {
        lock {
            int j = i + self.i;
            self.i += 1;
            return j;
        }
    }
}

isolated class IsolatedClassWithIsolatedMethodTwo {
    isolated function foo(int i) returns int => i;
}

class NonIsolatedClassWithIsolatedMethod {
    int i = 0;

    isolated function foo(int i) returns int {
        int j = i + self.i;
        self.i += 1;
        return j;
    }
}

function testIsolationOfBoundMethods() {
    IsolatedClassWithIsolatedMethod ob1 = new;
    isolated function (int) returns int func1 = ob1.foo;
    assertEquality(true, <any> func1 is isolated function (int) returns int);
    assertEquality(1, func1(1));
    assertEquality(3, func1(2));

    NonIsolatedClassWithIsolatedMethod ob2 = new;
    function (int) returns int func2 = ob2.foo;
    assertEquality(true, <any> func2 is function (int) returns int);
    assertEquality(false, <any> func2 is isolated function (int) returns int);
    assertEquality(1, func2(1));
    assertEquality(3, func2(2));

    // See https://github.com/ballerina-platform/ballerina-lang/issues/26726
    //IsolatedClassWithIsolatedMethod|IsolatedClassWithIsolatedMethodTwo ob3 = new IsolatedClassWithIsolatedMethodTwo();
    //isolated function (int) returns int func3 = ob3.foo;
    //assertEquality(true, <any> func3 is isolated function (int) returns int);
    //assertEquality(1, func3(1));
    //assertEquality(2, func3(2));
    //
    //IsolatedClassWithIsolatedMethodTwo|NonIsolatedClassWithIsolatedMethod ob4 = new NonIsolatedClassWithIsolatedMethod();
    //function (int) returns int func4 = ob4.foo;
    //assertEquality(true, <any> func4 is function (int) returns int);
    //assertEquality(false, <any> func4 is isolated function (int) returns int);
    //assertEquality(1, func4(1));
    //assertEquality(3, func4(2));
    //
    //IsolatedClassWithIsolatedMethodTwo|NonIsolatedClassWithIsolatedMethod ob5 = new IsolatedClassWithIsolatedMethodTwo();
    //function (int) returns int func5 = ob5.foo;
    //assertEquality(true, <any> func5 is function (int) returns int);
    //assertEquality(false, <any> func5 is isolated function (int) returns int);
    //assertEquality(1, func5(1));
    //assertEquality(2, func5(2));
}

service readonly class ReadOnlyService {
    int[] x = [1, 2, 3];

    resource function get foo() returns int[] => self.x;
}

final ReadOnlyService s = new;

isolated function testFinalReadOnlyServiceAccessInIsolatedFunction() {
    ReadOnlyService rs = s;
    assertEquality(<int[]> [1, 2, 3], rs.x);
}

type RawTemplateType object:RawTemplate;

type Template1 object {
    *object:RawTemplate;
    public (readonly & string[]) strings;
    public int[] insertions;
};

final object:RawTemplate & readonly tmp1 = `Count: ${10}, ${20}`;

final Template1 & readonly tmp2 = `Count: ${10}, ${20}`;

final RawTemplateType & readonly tmp3 = `Count: ${10}, ${20}`;

isolated function testFinalReadOnlyRawTemplateAccessInIsolatedFunction() {
    assertEquality(<string[]>["Count: ", ", ", ""], tmp1.strings);
    assertEquality(<int[]>[10, 20], tmp1.insertions);

    assertEquality(<string[]>["Count: ", ", ", ""], tmp2.strings);
    assertEquality(<int[]>[10, 20], tmp2.insertions);

    assertEquality(<string[]>["Count: ", ", ", ""], tmp3.strings);
    assertEquality(<int[]>[10, 20], tmp3.insertions);
}

readonly class IsolatedFunctionWithReadOnlySelfAsCapturedVariable {
    string[] words;
    int length;

    isolated function init(string[] & readonly words, int length) {
        self.words = words;
        self.length = length;
    }

    isolated function getCount() returns int =>
        self.words.filter(
            isolated function (string word) returns boolean => word.length() == self.length).length();
}

isolated class IsolatedFunctionWithIsolatedSelfAsCapturedVariable {
    private string[] words;
    private int min = 10;

    isolated function init(string[] words) {
        self.words = words.clone();
    }

    isolated function compare() returns boolean {
        lock {
            return self.words.some(isolated function (string s) returns boolean {
                                       lock {
                                           return s.length() > self.min;
                                       }
                                   });
        }
    }
}

function testIsolatedFunctionWithSelfAsCapturedVariable() {
    string[] words = ["hello", "world", "this", "is", "a", "test"];

    IsolatedFunctionWithReadOnlySelfAsCapturedVariable a = new (words.cloneReadOnly(), 4);
    assertEquality(2, a.getCount());

    IsolatedFunctionWithIsolatedSelfAsCapturedVariable b = new (words);
    assertEquality(false, b.compare());
}

isolated function testIsolatedFPCallInIsolatedFunction() {
    var f = isolatedFunctionWithOnlyLocalVars;
    int r = f();
    assertEquality(4, r);

    isolated function () returns int g = isolatedFunctionWithOnlyLocalVars;
    assertEquality(4, g());

    BoundMethodTestClass h = new;
    isolated function () returns int m = h.isolatedFn;
    int v = m();
    assertEquality(10, v);

    FunctionFieldTestClass i = new;
    var if1 = i.f1;
    int w = if1();
    assertEquality(123, w);

    i.testFunctionFieldFPCall();
}

isolated class BoundMethodTestClass {
    isolated function testBoundMethodFPCall() {
        var f = self.isolatedFn;
        int v = f();
        assertEquality(10, v);
    }

    public function nonIsolatedFn() {
    }

    public isolated function isolatedFn() returns int => 10;
}

public class FunctionFieldTestClass {
    public final isolated function () returns int f1 = () => 123;
    private final isolated function () returns string f2 = () => "hello";

    isolated function testFunctionFieldFPCall() {
        isolated function () returns int f1 = self.f1;
        assertEquality(123, f1());

        var f2 = self.f2;
        string r = f2();
        assertEquality("hello", r);
    }
}

isolated function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    string expectedValAsString = expected is error ? expected.toString() : expected.toString();
    string actualValAsString = actual is error ? actual.toString() : actual.toString();
    panic error(string `expected '${expectedValAsString}', found '${actualValAsString}'`);
}

type IsolatedFunction isolated function () returns int;
