// Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ballerina/jballerina.java;

function hello() returns string => "hello";

function count() returns int => hello().length();

function countExpression() returns int => 1 + hello().length();

string helloGlobString = "hello!";
int globInt = 1;

function helloMutableGlobalVar() returns string => helloGlobString;

function countMutableGlobalVar() returns int {
    return helloGlobString.length();
}

function countHelloMutableGlobalVar() returns int => helloMutableGlobalVar().length();

function countExprMutableGlobalVar() returns int => globInt + helloMutableGlobalVar().length();

public function helloPublic() returns string => "hello";

public function countPublic() returns int => hello().length();

public function countExpressionPublic() returns int => 1 + hello().length();

public function helloMutableGlobalVarPublic() returns string => helloGlobString;

public function countMutableGlobalVarPublic() returns int {
    return helloGlobString.length();
}

public function countHelloMutableGlobalVarPublic() returns int => helloMutableGlobalVar().length();

public function countExprMutableGlobalVarPublic() returns int => globInt + helloMutableGlobalVar().length();

function testBasicFunctionIsolationInference() {
    assertTrue(<any> hello is isolated function);
    assertTrue(count is isolated function () returns int);
    assertTrue(countExpression is isolated function () returns int);
    assertFalse(<any> helloMutableGlobalVar is isolated function);
    assertFalse(countMutableGlobalVar is isolated function () returns int);
    assertFalse(countHelloMutableGlobalVar is isolated function () returns int);
    assertFalse(countExprMutableGlobalVar is isolated function () returns int);
    assertFalse(<any> helloPublic is isolated function);
    assertFalse(countPublic is isolated function () returns int);
    assertFalse(countExpressionPublic is isolated function () returns int);
    assertFalse(<any> helloMutableGlobalVarPublic is isolated function);
    assertFalse(countMutableGlobalVarPublic is isolated function () returns int);
    assertFalse(countHelloMutableGlobalVarPublic is isolated function () returns int);
    assertFalse(countExprMutableGlobalVarPublic is isolated function () returns int);
}

function func1(string str) returns string {
    return func2(str);
}

function func2(string str) returns string {
    if str.length() < 4 {
        return func1(str + "!");
    }
    return str;
}

function func3(string str) returns string {
    return func4(helloGlobString + str);
}

function func4(string str) returns string {
    if str.length() < 4 {
        return func3(str + "!");
    }
    return str;
}

function func5() returns string {
    boolean b = true;

    if b {
        return func6("hello");
    }
    return func1("hello");
}

function func6(string str) returns string {
    return func5() + helloGlobString;
}

function testRecursiveFunctionIsolationInference() {
    assertTrue(<any> func1 is isolated function);
    assertTrue(func2 is isolated function (string str) returns string);
    assertTrue(<any> func3 is function (string str) returns string);
    assertFalse(func3 is isolated function (string str) returns string);
    assertTrue(<any> func4 is function (string str) returns string);
    assertFalse(func4 is isolated function (string str) returns string);
    assertTrue(<any> func5 is function () returns string);
    assertFalse(<any> func5 is isolated function () returns string);
    assertTrue(<any> func6 is function (string str) returns string);
    assertFalse(func6 is isolated function (string str) returns string);
}

client class NonPublicNonIsolatedClass {
    string str = "hello";

    remote function foo() returns int => self.bar().length();

    function bar() returns string => self.str;
}

isolated client class NonPublicIsolatedClass {
    private string str = "hello";

    remote function foo() returns int {
        lock {
            return 1;
        }
    }

    function bar() returns string {
        lock {
            return self.str;
        }
    }
}

public isolated client class PublicIsolatedClass {
    private string str = "hello";

    remote function foo() returns int {
        lock {
            return 1;
        }
    }

    function bar() returns string {
        lock {
            return self.str;
        }
    }
}

function testMethodIsolationInference() {
    NonPublicNonIsolatedClass c1 = new;
    NonPublicIsolatedClass c2 = new;
    PublicIsolatedClass c3 = new;

    // https://github.com/ballerina-platform/ballerina-lang/issues/27917
    // assertFalse(<any> c1.foo is isolated function);
    // assertFalse(<any> c1.bar is isolated function);

    // assertTrue(<any> c2.foo is isolated function);
    // assertTrue(<any> c2.bar is isolated function);

    // assertFalse(<any> c3.foo is isolated function);
    // assertFalse(<any> c3.bar is isolated function);

    assertTrue(isMethodIsolated(c1, "foo"));
    assertTrue(isMethodIsolated(c1, "bar"));

    assertTrue(isMethodIsolated(c2, "foo"));
    assertTrue(isMethodIsolated(c2, "bar"));

    assertFalse(isMethodIsolated(c3, "foo"));
    assertFalse(isMethodIsolated(c3, "bar"));
}

function functionWithFunctioPointerCall1() {
    string[] strArr = [];

    var closure = function (string val) {
        strArr[0] = val;
    };
    closure("foo");
}

function functionWithFunctioPointerCall2() {
    var closure = isolated function (string val) {
        boolean b = val.length() == 0;
    };
    closure("foo");
}

function functionWithFunctioPointerCall3() {
    string[] strArr = [];

    var closure = function (string val) { // Not called.
        strArr[0] = val;
    };
}

function testFunctionPointerIsolationInference() {
    assertFalse(<any> functionWithFunctioPointerCall1 is isolated function);
    assertTrue(<any> functionWithFunctioPointerCall2 is isolated function);
    assertTrue(<any> functionWithFunctioPointerCall3 is isolated function);
}

service class NonPublicServiceClass {
    resource function get foo() {
        self.func(hello());
    }

    remote function bar() {
        NonPublicIsolatedClass cl = new;
        self.func(cl.bar());
    }

    function func(string str) {
        int length = str.length();
    }

    public function func2(string str) {
        int length = str.length();
    }

    function func3(string str) {
        helloGlobString = str;
    }
}

public service class PublicServiceClass {
    resource function get foo() {
        self.func(hello());
    }

    remote function bar() {
        NonPublicIsolatedClass cl = new;
        self.func(cl.bar());
    }

    function func(string str) {
        int length = str.length();
    }

    public function func2(string str) {
        int length = str.length();
    }

    function func3(string str) {
        helloGlobString = str;
    }
}

function testServiceClassMethodIsolationInference() {
    assertTrue(isResourceIsolated(NonPublicServiceClass, "get", "foo"));
    assertTrue(isRemoteMethodIsolated(NonPublicServiceClass, "bar"));
    assertTrue(isMethodIsolated(NonPublicServiceClass, "func"));
    assertTrue(isMethodIsolated(NonPublicServiceClass, "func2"));
    assertFalse(isMethodIsolated(NonPublicServiceClass, "func3"));

    assertFalse(isResourceIsolated(PublicServiceClass, "get", "foo"));
    assertFalse(isRemoteMethodIsolated(PublicServiceClass, "bar"));
    assertFalse(isMethodIsolated(PublicServiceClass, "func"));
    assertFalse(isMethodIsolated(PublicServiceClass, "func2"));
    assertFalse(isMethodIsolated(PublicServiceClass, "func3"));
}

service on new Listener() {
    private string str = "abc";

    resource function get foo() returns string => hello();

    resource function get bar() returns string => helloMutableGlobalVar();

    remote function baz() returns string => self.quux();

    remote function qux() returns string => self.quuz();

    function quux() returns string => self.str;

    function quuz() returns string => helloMutableGlobalVar();
}

function testObjectConstructorIsolatedInference() {
    object {
        int i;

        function qux() returns int;
    } a = object {
        int i;

        function init() {
            self.i = 123;
        }

        function qux() returns int {
            return self.i + globInt;
        }
    };

    assertFalse(<any> a is object { int i; isolated function qux() returns int; });

    object {
        int i;

        function qux() returns int;
    } b = object {
        int i;

        function init() {
            self.i = 123;
        }

        function qux() returns int {
            return self.i;
        }
    };

    assertTrue(<any> b is object { int i; isolated function qux() returns int; });

    var  c = service object {
        int i = 2;

        function a() returns int {
            return self.i + globInt;
        }

        function b() returns int {
            return self.i;
        }

        remote function c() {
            int x = self.b();
        }

        remote function d() {
            int x = self.a();
        }

        resource function get e() returns int {
            return self.b() + self.i;
        }

        resource function update f() {
            int x = self.a() + self.i;
        }
    };

    assertFalse(isMethodIsolated(c, "a"));
    assertTrue(isMethodIsolated(c, "b"));
    assertTrue(isRemoteMethodIsolated(c, "c"));
    assertFalse(isRemoteMethodIsolated(c, "d"));
    assertTrue(isResourceIsolated(c, "get", "e"));
    assertFalse(isResourceIsolated(c, "update", "f"));
}

class Listener {

    public function attach(service object {} s, string|string[]? name = ()) returns error?  = @java:Method {
                                       name: "testServiceDeclarationMethodIsolationInference",
                                       'class: "org.ballerinalang.test.isolation.IsolationInferenceTest"
                                   } external;

    public function detach(service object {} s) returns error? { }

    public function 'start() returns error? { }

    public function gracefulStop() returns error? { }

    public function immediateStop() returns error? { }
}

isolated int isolatedVar = 1;
int nonIsolatedVar = 1;

service on new ListenerTwo() {
    resource function get a() {
        lock {
            isolatedVar += 1;
        }
    }

    resource function get b() {
        lock {
            nonIsolatedVar += 1;
        }
    }

    resource function get c() {
        nonIsolatedVar += 1;
    }

    remote function d() {
        lock {
            isolatedVar += 1;
        }
    }

    remote function e() {
        lock {
            nonIsolatedVar += 1;
        }
    }

    remote function f() {
        nonIsolatedVar += 1;
    }
}

function funcAccessingIsolatedVar() {
    lock {
        isolatedVar += 1;
    }
}

function funcAccessingNonIsolatedVarInLockStmt() {
    lock {
        nonIsolatedVar += 1;
    }
}

function funcAccessingNonIsolatedVar() {
    nonIsolatedVar += 1;
}

public function publicFuncAccessingIsolatedVar() {
    lock {
        isolatedVar += 1;
    }
}

public function publicFuncAccessingNonIsolatedVarInLockStmt() {
    lock {
        nonIsolatedVar += 1;
    }
}

public function publicFuncAccessingNonIsolatedVar() {
    nonIsolatedVar += 1;
}

function testFunctionsAccessingModuleLevelVarsIsolatedInference() {
    assertTrue(funcAccessingIsolatedVar is isolated function ());
    assertFalse(funcAccessingNonIsolatedVarInLockStmt is isolated function ());
    assertFalse(funcAccessingNonIsolatedVar is isolated function ());
    assertFalse(publicFuncAccessingIsolatedVar is isolated function ());
    assertFalse(publicFuncAccessingNonIsolatedVarInLockStmt is isolated function ());
    assertFalse(publicFuncAccessingNonIsolatedVar is isolated function ());
}

function isolatedConcat(string s) {

}

string m = "";

function nonIsolatedConcat(string s) {
    m += s;
}

function functionCallingFunctionWithIsolatedParamAnnotatedParam1() {
    string[] numbers = ["one", "two", "three"];
    numbers.forEach((s) => isolatedConcat(s));
}

function functionCallingFunctionWithIsolatedParamAnnotatedParam2() {
    string[] numbers = ["one", "two", "three"];
    numbers.forEach((s) => nonIsolatedConcat(s));
}

function functionCallingFunctionWithIsolatedParamAnnotatedParam3() {
    string[] numbers = ["one", "two", "three"];
    numbers.forEach(function(string s) {
        isolatedConcat(s);
    });
}

function functionCallingFunctionWithIsolatedParamAnnotatedParam4() {
    string[] numbers = ["one", "two", "three"];
    numbers.forEach(function(string s) {
        nonIsolatedConcat(s);
    });
}

function functionCallingFunctionWithIsolatedParamAnnotatedParam5() {
    string[][] numbers = [["one", "two"], ["three"]];
    numbers.forEach(s => s.forEach(t => nonIsolatedConcat(t)));
}

function functionCallingFunctionWithIsolatedParamAnnotatedParam6() {
    string[][] numbers = [["one", "two"], ["three"]];
    numbers.forEach(s => s.forEach(t => isolatedConcat(t)));
}

function functionCallingFunctionWithIsolatedParamAnnotatedParam7() {
    string[][] numbers = [["one", "two"], ["three"]];
    numbers.forEach(function (string[] s) {
        s.forEach(function (string t) {
            boolean b = true;
            _ = b ? isolatedConcat(t) : "";
        });
    });
}

function functionCallingFunctionWithIsolatedParamAnnotatedParam8() {
    string[][] numbers = [["one", "two"], ["three"]];
    numbers.forEach(function (string[] s) {
        s.forEach(function (string t) {
            boolean b = true;
            _ = b ? nonIsolatedConcat(t) : isolatedConcat(t);
        });
    });
}

function functionCallingFunctionWithIsolatedParamAnnotatedParam9() {
    int[] x = [1, 2];
    int[] y = [];
    x.forEach(i => y.push(i));
}

function functionCallingFunctionWithIsolatedParamAnnotatedParam10() {
    int[] x = [1, 2];
    int[] y = [];
    x.forEach(function (int i) {
        y.push(i);
    });
}

int[] isolatedIntArr = [];

function functionCallingFunctionWithIsolatedParamAnnotatedParam11() {
    int[] x = [1, 2];
    x.forEach(function (int i) {
        lock {
            isolatedIntArr.push(i);
        }
    });
}

int[] nonIsolatedIntArr = [];

int[] nonIsolatedIntArrCopy = nonIsolatedIntArr;

function functionCallingFunctionWithIsolatedParamAnnotatedParam12() {
    int[] x = [1, 2];
    x.forEach(function (int i) {
        lock {
            nonIsolatedIntArr.push(i);
        }
    });
}

function functionCallingFunctionWithIsolatedParamAnnotatedParam13() {
    int[] x = [1, 2];
    x.forEach(function (int i) {
        nonIsolatedIntArr.push(i);
    });
}

function testFunctionCallingFunctionWithIsolatedParamAnnotatedParam() {
    assertTrue(<any>functionCallingFunctionWithIsolatedParamAnnotatedParam1 is isolated function);
    assertFalse(<any>functionCallingFunctionWithIsolatedParamAnnotatedParam2 is isolated function);
    assertTrue(<any>functionCallingFunctionWithIsolatedParamAnnotatedParam3 is isolated function);
    assertFalse(<any>functionCallingFunctionWithIsolatedParamAnnotatedParam4 is isolated function);
    assertFalse(<any>functionCallingFunctionWithIsolatedParamAnnotatedParam5 is isolated function);
    assertTrue(<any>functionCallingFunctionWithIsolatedParamAnnotatedParam6 is isolated function);
    assertTrue(<any>functionCallingFunctionWithIsolatedParamAnnotatedParam7 is isolated function);
    assertFalse(<any>functionCallingFunctionWithIsolatedParamAnnotatedParam8 is isolated function);
    assertFalse(<any>functionCallingFunctionWithIsolatedParamAnnotatedParam9 is isolated function);
    assertFalse(<any>functionCallingFunctionWithIsolatedParamAnnotatedParam10 is isolated function);
    assertTrue(<any>functionCallingFunctionWithIsolatedParamAnnotatedParam11 is isolated function);
    assertFalse(<any>functionCallingFunctionWithIsolatedParamAnnotatedParam12 is isolated function);
    assertFalse(<any>functionCallingFunctionWithIsolatedParamAnnotatedParam13 is isolated function);
    assertTrue(<any> isolatedConcat is isolated function);
    assertFalse(<any> nonIsolatedConcat is isolated function);
}

isolated function inferAnonFuncWithParamTypeNarrowingForIsolatedParam() {
    (int|string)[] a = [];
    string[] _ = a.map(n => n is int ? "int" : n);

    (int|string|boolean)[] c = [];
    string[] _ = c.map(n => n is int|boolean ? (n is int ? n.toString() : "boolean") : n);
}

function anonFuncWithParamTypeNarrowingForIsolatedParam() {
    (int|string)[] a = [];
    string[] _ = a.map(function (int|string n) returns string => n is int ? "int" : n);

    (int|string|boolean)[] c = [];
    string[] _ = c.map(n => n is int|boolean ? (n is int ? n.toString() : "boolean") : n);
}

function nonIsolatedAnonFuncWithTypeNarrowingForIsolatedParam() {
    (int|string)[] a = [];
    int|string a2 = 1;
    string[] _ = a.map(function (int|string n) returns string => a2 is int ? "int" : n.toString());

    (int|string|boolean)[] c = [];
    string d = "";
    string[] _ = c.map(n => n is int|boolean ? (n is int ? n.toString() : "boolean") : d);
}

function testInferringIsolatedForAnonFuncArgForIsolatedParamAnnotatedParam() {
    assertTrue(inferAnonFuncWithParamTypeNarrowingForIsolatedParam is isolated function ());
    assertTrue(anonFuncWithParamTypeNarrowingForIsolatedParam is isolated function ());
    assertFalse(nonIsolatedAnonFuncWithTypeNarrowingForIsolatedParam is isolated function ());
}

class ListenerTwo {

    public function attach(service object {} s, string|string[]? name = ()) returns error?  {
        assertTrue(isResourceIsolated(s, "get", "a"));
        assertFalse(isResourceIsolated(s, "get", "b"));
        assertFalse(isResourceIsolated(s, "get", "c"));
        assertTrue(isRemoteMethodIsolated(s, "d"));
        assertFalse(isRemoteMethodIsolated(s, "e"));
        assertFalse(isRemoteMethodIsolated(s, "f"));
    }

    public function detach(service object {} s) returns error? { }

    public function 'start() returns error? { }

    public function gracefulStop() returns error? { }

    public function immediateStop() returns error? { }
}

isolated function isResourceIsolated(service object {}|typedesc val, string resourceMethodName,
     string resourcePath) returns boolean = @java:Method {
                        'class: "org.ballerinalang.test.isolation.IsolationInferenceTest",
                        paramTypes: ["java.lang.Object", "io.ballerina.runtime.api.values.BString",
                                        "io.ballerina.runtime.api.values.BString"]
                    } external;

isolated function isRemoteMethodIsolated(object {}|typedesc val, string methodName) returns boolean = @java:Method {
                                            'class: "org.ballerinalang.test.isolation.IsolationInferenceTest",
                                             paramTypes: ["java.lang.Object", "io.ballerina.runtime.api.values.BString"]
                                        } external;

isolated function isMethodIsolated(object {}|typedesc val, string methodName) returns boolean = @java:Method {
                                            'class: "org.ballerinalang.test.isolation.IsolationInferenceTest",
                                            paramTypes: ["java.lang.Object", "io.ballerina.runtime.api.values.BString"]
                                        } external;

class IsolatedClassWithValidAccessInInitMethodAfterInitialization {
    private int[][] arr;

    function init(int[] node) {
        self.arr = [];

        lock {
            self.arr[0] = node.cloneReadOnly();
            self.arr.push(node.clone());
        }
    }
}

class IsolatedClassWithOnlyInitializationInInitMethod {
    private int[][] arr;

    function init(int[][] node) {
        self.arr = node.clone();
    }
}

class NonIsolatedClassWithValidAccessInInitMethodAfterInitialization1 {
    private int[][] arr;

    function init(int[] node) {
        self.arr = [];

        lock {
            self.arr[0] = node;
        }
    }
}

class NonIsolatedClassWithValidAccessInInitMethodAfterInitialization2 {
    private int[][] arr;

    function init(int[] node) {
        self.arr = [];

        lock {
            self.arr.push(node);
        }
    }
}

function getIntArray() returns int[] => [1, 2];

var isolatedObjectConstructorWithValidAccessInInitMethodAfterInitialization = object {
    private int[][] arr;

    function init() {
        self.arr = [];

        lock {
            int[] node = getIntArray();

            self.arr.push(node);
            self.arr[1] = node;
        }
    }
};

var nonIsolatedObjectConstructorWithInvalidAccessInInitMethodAfterInitialization = object {
    private int[][] arr;

    function init() {
        self.arr = [];

        int[] node = getIntArray();
        lock {
            self.arr.push(node);
        }
    }
};

int[] mutableIntArr = [1, 2];

function getMutableIntArray() returns int[] => mutableIntArr;

function testIsolatedObjectsWithNonInitializationSelfAccessInInitMethod() {
    assertTrue(<any> new IsolatedClassWithValidAccessInInitMethodAfterInitialization([]) is isolated object {});
    assertTrue(<any> new IsolatedClassWithOnlyInitializationInInitMethod([]) is isolated object {});
    assertTrue(<any> isolatedObjectConstructorWithValidAccessInInitMethodAfterInitialization is isolated object {});
    var x = object {
        private int[][] arr;

        function init() {
            self.arr = [];

            int[] node = getIntArray();

            lock {
                self.arr.push(node.clone());
                self.arr[1] = node.cloneReadOnly();
            }
        }
    };
    assertTrue(<any> x is isolated object {});

    assertFalse(<any> new NonIsolatedClassWithValidAccessInInitMethodAfterInitialization1([]) is isolated object {});
    assertFalse(<any> new NonIsolatedClassWithValidAccessInInitMethodAfterInitialization2([]) is isolated object {});
    assertFalse(<any> nonIsolatedObjectConstructorWithInvalidAccessInInitMethodAfterInitialization is isolated object {});
    var y = object {
        private int[][] arr = [];

        function init() {
            lock {
                self.arr[1] = getMutableIntArray();
            }
        }
    };
    assertFalse(<any> y is isolated object {});

    var z = object {
        private int[][] arr;

        function init() {
            self.arr = [];
        }
    };
    assertTrue(<any> z is isolated object {});
}

class ObjectWithSelfAccessInLocksInAnonFunctions {
    private int[][] arr = [];

    function init(int[] node) {
        function _ = function () {
            lock {
                self.arr.push(node.clone());
            }
        };
    }

    function f1(int[] node) {
        function _ = function () returns int[] {
            lock {
                return self.arr[0].cloneReadOnly();
            }
        };
    }

    function f2(int[] node) returns object {} {
        lock {
            var fn = function () returns object {} {
                return isolated object {
                    private int[][] innerArr = [];

                    function innerF(int[] innerNode) {
                        function _ = function () {
                            lock {
                                self.innerArr.push(innerNode.clone());
                            }
                        };
                    }
                };
            };
            return fn();
        }
    }
}

class ObjectWithSelfAccessOutsideLocksInAnonFunctions {
    private int[][] arr = [];

    function f1(int[] node) {
        function _ = function () returns int[] {
            return self.arr[0].cloneReadOnly();
        };
    }
}

class ObjectWithInvalidTransferInAnonFunctions {
    private int[][] arr = [];

    function f1(int[] node) {
        function _ = function () returns int[] {
            lock {
                return self.arr[0];
            }
        };
    }
}

function testIsolatedInferenceWithAnonFunctions() {
    ObjectWithSelfAccessInLocksInAnonFunctions a = new ([]);
    assertTrue(<any> a is isolated object {});

    object {} b = a.f2([]);
    assertTrue(<any> b is isolated object {});

    any c = new ObjectWithSelfAccessOutsideLocksInAnonFunctions();
    assertFalse(c is isolated object {});

    any d = new ObjectWithInvalidTransferInAnonFunctions();
    assertFalse(d is isolated object {});
}

isolated function assertTrue(anydata actual) => assertEquality(true, actual);

isolated function assertFalse(anydata actual) => assertEquality(false, actual);

isolated function assertEquality(anydata expected, anydata actual) {
    if expected == actual {
        return;
    }

    panic error(string `expected '${expected.toString()}', found '${actual.toString()}'`);
}
