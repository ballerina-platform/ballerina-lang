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

import ballerina/lang.array;
import ballerina/jballerina.java;

class IsolatedClassWithNoMutableFields {
    public final int[] & readonly a;
    final readonly & record {record {int i;} val;} b;

    function init(int[] & readonly a, record {record {int i;} val;} b) {
        self.a = a;
        b.val.i = 1;
        b.val["i"] = 1;
        record {record {int i;} val;} & readonly c = b.cloneReadOnly();
        b.val = {i: 2};
        self.b = c;
    }
}

class IsolatedClassWithPrivateMutableFields {
    public final int[] & readonly a = [10, 20, 30];
    final readonly & record {int i;} b;

    private int c;
    private map<int>[] d;

    isolated function init(record {int i;} & readonly b, int c) {
        self.b = b;
        self.c = c;
    }
}

final readonly & string[] immutableStringArray = ["hello", "world"];

type ObjectType object {
    int a;
};

class IsolatedClassOverridingMutableFieldsInIncludedObject {
    *ObjectType;

    final byte a = 100;
    private string[] b;

    function init() {
        self.b = [];
    }

    function accessImmutableField() returns int => self.a + 1;

    function accessMutableField() returns int {
        lock {
            self.b.push(...immutableStringArray);
            return self.b.length();
        }
    }
}

function testIsolatedObjectOverridingMutableFieldsInIncludedObject() {
    object {} isolatedObjectOverridingMutableFieldsInIncludedObject = object ObjectType {

        final byte a = 100;
        private string[] b = [];

        function accessImmutableField() returns int => self.a + 1;

        isolated function accessMutableField() returns int {
            lock {
                self.b.push(...immutableStringArray);
                return self.b.length();
            }
        }
    };

    assertTrue(isolatedObjectOverridingMutableFieldsInIncludedObject is isolated object {});
    assertTrue(isMethodIsolated(isolatedObjectOverridingMutableFieldsInIncludedObject, "accessImmutableField"));
    assertTrue(isMethodIsolated(isolatedObjectOverridingMutableFieldsInIncludedObject, "accessMutableField"));
}

class IsolatedClassWithMethodsAccessingPrivateMutableFieldsWithinLockStatements {
    public final int[] & readonly a = [10, 20, 30];
    final readonly & record {int i;} b;

    private int c;
    private map<int[]> d;

    function init(record {int i;} & readonly b, int c, map<int[]> d) {
        self.b = b;
        self.c = c;
        self.d = d.clone();
    }

    function accessMutableFieldInLockOne() returns int[] {
        int i = 1;

        lock {
            self.c = i;

            int j = i;
            self.c = i;

            map<int[]> k = {
                a: [1, 2, i, j, i + j],
                b: self.a
            };
            self.d = k;
            k = self.d;
        }

        return self.a;
    }

    function accessMutableFieldInLockTwo() returns int[] {
        int i = 1;
        int[] x;

        lock {
            int j = i;

            lock {
                int[] y = [1, 2, 3];
                x = y.clone();
                self.c = i;
            }

            map<int[]> k = {
                a: [1, 2, i, j, i + j],
                b: self.a
            };
            self.d = k;
            k = self.d;
        }

        return self.a;
    }

    function accessImmutableFieldOutsideLock() returns int[] {
        int[] arr = [];

        arr.push(...self.a);
        arr[5] = self.b.i;
        return arr;
    }
}

object {} isolatedObjectWithMethodsAccessingPrivateMutableFieldsWithinLockStatements = object {
    public final int[] & readonly a = [10, 20, 30];
    final readonly & record {int i;} b = {i: 101};

    private int c = 1;
    private map<int[]> d = {};

    function accessMutableFieldInLockOne() returns int[] {
        int i = 1;

        lock {
            self.c = i;

            int j = i;
            self.c = i;

            map<int[]> k = {
                a: [1, 2, i, j, i + j],
                b: self.a
            };
            self.d = k;
            k = self.d;
        }

        return self.a;
    }

    function accessMutableFieldInLockTwo() returns int[] {
        int i = 1;
        int[] x;

        lock {
            int j = i;

            lock {
                int[] y = [1, 2, 3];
                x = y.clone();
                self.c = i;
            }

            map<int[]> k = {
                a: [1, 2, i, j, i + j],
                b: self.a
            };
            self.d = k;
            k = self.d;
        }

        return self.a;
    }

    function accessImmutableFieldOutsideLock() returns int[] {
        int[] arr = [];

        arr.push(...self.a);
        arr[5] = self.b.i;
        return arr;
    }
};

class IsolatedClassWithNonPrivateIsolatedObjectFields {
    final isolated object {} a = isolated object {
        final int i = 1;
        private map<int> j = {};
    };
    final IsolatedClassWithMethodsAccessingPrivateMutableFieldsWithinLockStatements b;
    private final IsolatedClassWithMethodsAccessingPrivateMutableFieldsWithinLockStatements c = new ({i: 1}, 102, {});
    private int[] d = [1, 2];

    function init() {
        self.b = new ({i: 1}, 102, {});
    }

    function accessNonPrivateIsolatedObjectFieldOutsideLock() {
        int[] x = self.b.accessMutableFieldInLockOne();
    }
}

object {} isolatedObjectWithNonPrivateIsolatedObjectFields = object {
    final isolated object { function foo() returns int; } a = isolated object {
        final int i = 1;
        private map<int> j = {};

        isolated function foo() returns int {
            lock {
                return self.i + (self.j["a"] ?: 1);
            }
        }
    };
    final int|IsolatedClassWithMethodsAccessingPrivateMutableFieldsWithinLockStatements b = new ({i: 1}, 102, {});
    private final IsolatedClassWithMethodsAccessingPrivateMutableFieldsWithinLockStatements c = new ({i: 1}, 102, {});
    private int[] d = [1, 2];
    final isolated object { isolated function foo() returns int; } e = isolated object {
        final int i = 1;
        private map<int> j = {};

        isolated function foo() returns int {
            lock {
                return self.i + (self.j["a"] ?: 1);
            }
        }
    };

    function init() {
    }

    function accessNonPrivateIsolatedObjectFieldOutsideLock() {
        int x = self.a.foo();
    }

    function accessNonPrivateIsolatedObjectFieldOutsideLockTwo() {
        int x = self.e.foo();
    }
};

const INT = 100;

class IsolatedClassWithUniqueInitializerExprs {
    private map<int> a = <map<int>> {};
    final int[] & readonly b = [1, INT];
    private table<record {readonly int i; string name;}> c;

    function init(table<record {readonly int i; string name;}> tb) {
        self.c = tb.clone();
    }
}

object {} isolatedObjectWithUniqueInitializerExprs = object {
    private map<int> a = <map<int>> {};
    final int[] & readonly b = [1, INT];
    private table<record {readonly int id; string name;}> c;

    isolated function init() {
        self.c = table [
            {id: INT, name: "foo"},
            {id: INT + 100, name: "bar"}
        ];
    }
};

class IsolatedClassWithValidCopyInInsideBlock {
    private string[] uniqueGreetings = [];

    function add(string[] greetings) {
        lock {
            if self.uniqueGreetings.length() == 0 {
                self.uniqueGreetings = greetings.clone();
            }
        }
    }
}

class IsolatedClassWithValidCopyyingWithClone {
    private anydata[] arr = [];
    private anydata[] arr2 = [];

    function add(anydata val) {
        lock {
            self.arr.push(val.clone());

            self.addAgain(val.clone());
            anydata clonedVal = val.cloneReadOnly();
            self.addAgain(clonedVal.clone());
        }
    }

    function addAgain(anydata val) {
        lock {
            self.arr2.push(val.clone());
            self.arr2.push(val.cloneReadOnly());
        }
    }
}

class IsolatedClassPassingCopiedInVarsToIsolatedFunctions {

    private anydata[] arr = [];

    function add(anydata val) {
        lock {
            anydata val2 = val.clone();
            self.arr.push(val2);
            self.addAgain(val2);
            outerAdd(val.clone());

            if val is anydata[] {
                self.arr.push(val[0].clone());
                self.addAgain(val[0].clone());
                anydata val3 = val[0].cloneReadOnly();
                outerAdd(val3);
            }

            lock {
                if val2 is anydata[] {
                    self.arr.push(val2[0].clone());
                    self.addAgain(val2[0].clone());
                    anydata val3 = val2[0].cloneReadOnly();
                    outerAdd(val3);
                }
            }
        }
    }

    function addAgain(anydata val) {

    }
}

function outerAdd(anydata val) {

}

class ArrayGen {
    function getArray() returns int[] => [];
}

ArrayGen|(int[] & readonly) unionVal = [1, 2, 3];

class IsolatedClassAccessingSubTypeOfReadOnlyOrIsolatedObjectUnion {
    private int[] a;

    function testAccessingSubTypeOfReadOnlyOrIsolatedObjectUnionInIsolatedClass() {
        lock {
            var val = unionVal;
            self.a = val is ArrayGen ? val.getArray() : val;
        }
    }
}

int[] a = [];
readonly & int[] b = [];
final readonly & int[] c = [];

class IsolatedClassWithValidVarRefs {
    private any[] w = [];
    private int[][] x = [];
    private int[] y;
    private int[] z = b;

    function init() {
        self.y = b;
    }

    function updateFields() {
        lock {
            self.x = let int[] u = b, int[] v = a.clone() in [b, u, v, c];
            self.y = (let int[]? u = b in ((u is int[]) ? u : <int[]> []));
            self.z = let int[] u = a.clone() in u;
        }
    }

    function isolatedUpdateFields() {
        lock {
            self.x = let int[] u = c, int[] v = c in [c, u, v];
            self.y = (let int[]? u = c in ((u is int[]) ? u : <int[]> []));
            self.z = let int[] u = c.clone() in u;
        }
    }

    function nested() {
        any[] arr = let int[] u = c in [u];

        lock {
            self.w = let int[] u = c in [u, let int[] v = c.clone() in isolated function () returns int[2][] { return [c, []]; }];
        }
    }
}

type IsolatedObjectType isolated object {
    int a;
};

function testObjectConstrExprImplicitIsolatedness() {
    var ob = object {
        final int[] & readonly x = [];
        final IsolatedObjectType y = object {
            final int a = 1;
            final readonly & string[] b = [];
        };
        final (readonly & string[])|IsolatedClassWithPrivateMutableFields z = new IsolatedClassWithPrivateMutableFields({i: 2}, 3);
    };

    object {} isolatedOb = ob;
    assertTrue(<any> isolatedOb is isolated object {});

    var ob2 = object {
        final int[] x = [];
        final object {} y = object {
            final int a = 1;
            final string[] b = [];
        };
        final IsolatedClassWithPrivateMutableFields z = new ({i: 2}, 3);
    };

    assertFalse(<any> ob2 is isolated object {});

    var ob3 = object {
        final int[] & readonly x = [];
        final IsolatedObjectType y = object {
            final int a = 1;
            final readonly & string[] b = [];
        };
        final string[]|IsolatedClassWithPrivateMutableFields z = new IsolatedClassWithPrivateMutableFields({i: 2}, 3);
    };

    assertFalse(<any> ob3 is isolated object {});
}

class NonIsolatedClass {
    int i = 1;
}

function testRuntimeIsolatedFlag() {
    IsolatedClassWithPrivateMutableFields x = new ({i: 2}, 3);
    assertTrue(<any> x is isolated object {});

    NonIsolatedClass y = new;
    assertFalse(<any> y is isolated object {});

    testObjectConstrExprImplicitIsolatedness();

    object {} ob1 = object {
        private int i = 1;

        function updateI() {
            lock {
                self.i = 2;
            }
        }
    };
    assertTrue(<any> ob1 is isolated object {});

    object {} ob2 = object {
        private int i = 1;

        function updateI() {
            self.i = 2;
        }
    };
    assertFalse(<any> ob2 is isolated object {});
}

service class IsolatedServiceClassWithNoMutableFields {
    public final int[] & readonly a;
    final readonly & record {int i;} b;

    function init(int[] & readonly a, record {int i;} & readonly b) {
        self.a = a;
        self.b = b;
    }
}

client class IsolatedClientClassWithPrivateMutableFields {
    public final int[] & readonly a = [10, 20, 30];
    final readonly & record {int i;} b;

    private int c;
    private map<int>[] d;

    isolated function init(record {int i;} & readonly b, int c) {
        self.b = b;
        self.c = c;
    }
}

int[] isolatedArr = [];

service "inferredIsolated" on new Listener() {
    private int[] x = [];

    remote function foo() {
        lock {
            self.x = [2, 3];
        }
    }

    resource function get bar() {
        lock {
            self.x = [2, 3];
        }
    }

    function baz() {
        lock {
            self.x = [2, 3];
        }
    }

    resource function get corge() {
        lock {
            self.x = [2, 3];
        }
    }
}

service object {} ser = service object {
    private int[] x = [];

    remote function foo() {
        lock {
            self.x = [2, 3];
        }
    }

    resource function get bar() {
        lock {
            self.x = [2, 3];
        }
    }

    function baz() {
        lock {
            self.x = [2, 3];
        }
    }

    resource function get corge() {
        lock {
            isolatedArr = [];
        }
    }
};

service class ServiceClass {
    private int[] x = [];

    remote function foo() {
        lock {
            self.x = [2, 3];
        }
    }

    resource function get bar() {
        lock {
            self.x = [2, 3];
        }
    }

    function baz() {
        lock {
            self.x = [2, 3];
        }
    }

    resource function get corge() {
        lock {
            isolatedArr = [];
        }
    }

    function quuz() {
        lock {
            self.x = [2, 3];
        }
    }
}

class Listener {

    public function 'start() returns error? {}

    public function gracefulStop() returns error? {}

    public function immediateStop() returns error? {}

    public function detach(service object {} s) returns error? {}

    public function attach(service object {} s, string|string[]? name = ()) returns error? {
        if name == "inferredIsolated" {
            assertTrue(s is isolated service object {});
            assertTrue(isRemoteMethodIsolated(s, "foo"));
            assertTrue(isResourceIsolated(s, "get", "bar"));
            assertTrue(isMethodIsolated(s, "baz"));
            assertTrue(isResourceIsolated(s, "get", "corge"));
            return;
        }

        assertEquality("serviceUsingClient", name);
        assertTrue(s is isolated service object {});
        //assertTrue(isResourceIsolated(s, "post", "foo"));
    }
}

class IsolatedClassUsingSelf {
    private int[][] arr = [[], []];

    function getMember(boolean bool) returns int[] {
        lock {
            if bool {
                return getMember(self);
            }

            return self.getMemberInternal();
        }
    }

    private function getMemberInternal() returns int[] {
        lock {
            return self.arr[0].clone();
        }
    }
}

function getMember(IsolatedClassUsingSelf foo) returns int[] {
    return foo.getMember(false);
}

class IsolatedClassWithBoundMethodAccess {
    public function bar() {
        lock {
            function () fn = self.baz;
            assertTrue(fn is isolated function ());
        }
    }

    function baz() {
    }
}

class IsolatedClassReferringSelfOutsideLock {
    final int a = 1;
    private int[] b = [];

    function foo() {
        f1(self);
        self.baz();
    }

    function baz() {
        f2(1, self);
    }
}

function f1(IsolatedClassReferringSelfOutsideLock x) {

}

isolated function f2(int i, IsolatedClassReferringSelfOutsideLock x) {

}

class IsolatedClassWithBoundMethodAccessOutsideLock {

    public function bar() {
        function () fn = self.baz;
        assertTrue(fn is isolated function ());
    }

    function baz() {
    }
}

class IsolatedClassWithInvalidCopyInInMethodCall {
    private map<int> m = {};

    function baz() returns map<int>[] {
        map<int>[] y = [];

        lock {
            map<int>[] y2 = y.cloneReadOnly();
            y2[0] = self.m.cloneReadOnly();
            y.clone().push(self.m);
            array:push(y.clone(), self.m);
        }

        return y;
    }

    function qux(map<int[]> y) {
        lock {
            _ = y.clone().remove(self.m["a"].toString());
        }
    }
}

var isolatedObjectWithInvalidCopyInInMethodCall = object {
    private map<int> m = {};

    function baz() returns map<int>[] {
        map<int>[] y = [];

        lock {
            map<int>[] y2 = y.clone();
            y2[0] = self.m.clone();
            y.clone().push(self.m);
            array:push(y2, self.m);
        }

        return y;
    }

    function qux(map<int[]> y) {
        lock {
            _ = y.clone().remove(self.m["a"].toString());
        }
    }
};

class IsolatedClassAssigningProtectedFieldsToLocalVars {
    private map<int> m = {};

    function baz() returns map<int>[] {
        map<int>[] y = [];
        map<int> z;
        lock {
            map<int>[] y2 = [self.m];
            y2[0] = self.m;
            y2.push(self.m);

            map<int>[] y3 = y.clone();
            y3[0] = self.m;

            z = self.m.clone();

            return y2.cloneReadOnly();
        }
    }
}

const fromMobile = "";
configurable string toMobile = "";

NonIsolatedClient cl = new;

service "serviceUsingClient" on new Listener() {
    resource function post foo() returns error? {
        Response resp;
        lock {
            Response val = check cl->sendMessage(fromMobile, toMobile, "Hi!");
            resp = val.clone();
        }
    }
}

type Response record {|
   string message;
   int id;
|};

client class NonIsolatedClient {
    int i = 1;

    remote function sendMessage(string x, string y, string z)
      returns Response|error => {message: "Hello", id: 0};
}

class IsolatedClassWithRawTemplateTransfer {
    private int[] arr = [];
    private isolated object {}[] arr2 = [];

    function getArrOne() returns any {
        lock {
            return `values: ${self.arr.clone()}`;
        }
    }

    function getArrTwo() returns any {
        lock {
            return `values: ${self.arr2.pop()}`;
        }
    }

    function getArrs(int[] intArr) returns any {
        lock {
            return `values: ${self.arr2[0]}, ${self.arr.clone()}, ${intArr.clone()}`;
        }
    }
}

class IsolatedObjectWithIsolatedFunctionCallAccessingModuleVarOfSameTypeAsDefaultValueExpr {
    final IsolatedObjectWithIsolatedFunctionCallAccessingModuleVarOfSameTypeAsDefaultValueExpr x = f3();
    private IsolatedObjectWithIsolatedFunctionCallAccessingModuleVarOfSameTypeAsDefaultValueExpr y = f3();
    private int z = 1;

    function init() {
        f4();
    }
}

final IsolatedObjectWithIsolatedFunctionCallAccessingModuleVarOfSameTypeAsDefaultValueExpr f = new;

function f3() returns IsolatedObjectWithIsolatedFunctionCallAccessingModuleVarOfSameTypeAsDefaultValueExpr {
    lock {
        return f;
    }
}

function f4() {
    lock {
        _ = f;
    }
}

type Obj object {
    int i;
    int[] j;
};

class Class {
    string k = "";
    final string[] & readonly l = [];
}

class IsolatedClassIncludingObjectAndClass {
    *Obj;
    *Class;

    final int i = 1;
    final readonly & int[] j = [];
    final string k;

    function init(string[] arg) {
        self.k = "default";
        self.l = arg.cloneReadOnly();
    }

    function access() returns anydata {
        lock {
            return self.k + self.l.toString();
        }
    }
}

readonly class ReadOnlyClass {
    int i = 1;
    int[] j = [];

    function f1() {
        _ = self.i;
    }

    function f2() returns int[] => self.j;
}

class IsolatedClassWithMultipleLocks {
    private int[] x = [];
    final readonly & int[] y = [];

    function fn() {
        lock {
            lock {
                f5();
            }
            self.x.push(1);
        }
    }

    function fn2() {
        lock {
            self.x.push(1);
        }

        lock {
            int[] a = self.y;
            boolean b = f6(a);
        }
    }
}

function f5() {
}

int x = 1;

function f6(int[] arr) returns boolean => arr[0] == x;

listener Listener ln = new;

function testAccessingListenerOfIsolatedObjectType() {
    any x = ln;
}

public function testIsolatedInference() {
    IsolatedClassWithNoMutableFields a = new ([], {val: {i: 1}});
    assertTrue(<any> a is isolated object {});
    assertTrue(isMethodIsolated(a, "init"));

    IsolatedClassWithPrivateMutableFields b = new ({i: 1}, 2);
    assertTrue(<any> b is isolated object {});
    assertTrue(isMethodIsolated(b, "init"));

    IsolatedClassOverridingMutableFieldsInIncludedObject c = new;
    assertTrue(<any> c is isolated object {});
    assertTrue(isMethodIsolated(c, "init"));
    assertTrue(isMethodIsolated(c, "accessImmutableField"));
    assertTrue(isMethodIsolated(c, "accessMutableField"));

    testIsolatedObjectOverridingMutableFieldsInIncludedObject();

    IsolatedClassWithMethodsAccessingPrivateMutableFieldsWithinLockStatements d = new ({i: 1}, 2, {});
    assertTrue(<any> d is isolated object {});
    assertTrue(isMethodIsolated(d, "init"));
    assertTrue(isMethodIsolated(d, "accessMutableFieldInLockOne"));
    assertTrue(isMethodIsolated(d, "accessMutableFieldInLockTwo"));
    assertTrue(isMethodIsolated(d, "accessImmutableFieldOutsideLock"));

    assertTrue(<any> isolatedObjectWithMethodsAccessingPrivateMutableFieldsWithinLockStatements is isolated object {});
    assertTrue(isMethodIsolated(isolatedObjectWithMethodsAccessingPrivateMutableFieldsWithinLockStatements, "accessMutableFieldInLockOne"));
    assertTrue(isMethodIsolated(isolatedObjectWithMethodsAccessingPrivateMutableFieldsWithinLockStatements, "accessMutableFieldInLockTwo"));
    assertTrue(isMethodIsolated(isolatedObjectWithMethodsAccessingPrivateMutableFieldsWithinLockStatements, "accessImmutableFieldOutsideLock"));

    IsolatedClassWithNonPrivateIsolatedObjectFields e = new;
    assertTrue(<any> e is isolated object {});
    assertTrue(isMethodIsolated(e, "init"));
    assertTrue(isMethodIsolated(e, "accessNonPrivateIsolatedObjectFieldOutsideLock"));

    assertTrue(<any> isolatedObjectWithNonPrivateIsolatedObjectFields is isolated object {});
    // The `init` method of object constructor expressions isn't available atm.
    // assertTrue(isMethodIsolated(isolatedObjectWithNonPrivateIsolatedObjectFields, "init"));
    assertFalse(isMethodIsolated(isolatedObjectWithNonPrivateIsolatedObjectFields, "accessNonPrivateIsolatedObjectFieldOutsideLock"));
    assertTrue(isMethodIsolated(isolatedObjectWithNonPrivateIsolatedObjectFields, "accessNonPrivateIsolatedObjectFieldOutsideLockTwo"));

    IsolatedClassWithUniqueInitializerExprs f = new (table []);
    assertTrue(<any> f is isolated object {});
    assertTrue(isMethodIsolated(f, "init"));

    assertTrue(<any> isolatedObjectWithUniqueInitializerExprs is isolated object {});
    // The `init` method of object constructor expressions isn't available atm.
    // assertTrue(isMethodIsolated(isolatedObjectWithUniqueInitializerExprs, "init"));

    assertTrue(<any> isolatedObjectWithUniqueInitializerExprs is isolated object {});
    // The `init` method of object constructor expressions isn't available atm.
    // assertTrue(isMethodIsolated(isolatedObjectWithUniqueInitializerExprs, "init"));

    IsolatedClassWithValidCopyInInsideBlock g = new;
    assertTrue(<any> g is isolated object {});
    assertTrue(isMethodIsolated(g, "add"));

    IsolatedClassWithValidCopyyingWithClone h = new;
    assertTrue(<any> h is isolated object {});
    assertTrue(isMethodIsolated(h, "add"));
    assertTrue(isMethodIsolated(h, "addAgain"));

    IsolatedClassPassingCopiedInVarsToIsolatedFunctions i = new;
    assertTrue(<any> i is isolated object {});
    assertTrue(isMethodIsolated(i, "add"));
    assertTrue(isMethodIsolated(i, "addAgain"));

    assertTrue(outerAdd is isolated function (anydata val));

    ArrayGen k = new;
    assertTrue(<any> k is isolated object {});
    assertTrue(isMethodIsolated(k, "getArray"));

    IsolatedClassAccessingSubTypeOfReadOnlyOrIsolatedObjectUnion l = new;
    // TODO: fallback to one isolated constrcut with two potentially protected vars
    // assertTrue(<any> l is isolated object {});
    assertFalse(isMethodIsolated(l, "testAccessingSubTypeOfReadOnlyOrIsolatedObjectUnionInIsolatedClass"));

    IsolatedClassWithValidVarRefs m = new;
    // TODO: fallback to one isolated constrcut with two potentially protected vars
    // assertTrue(<any> m is isolated object {});
    assertFalse(isMethodIsolated(m, "init"));
    assertFalse(isMethodIsolated(m, "updateFields"));
    assertTrue(isMethodIsolated(m, "isolatedUpdateFields"));
    assertTrue(isMethodIsolated(m, "nested"));

    testObjectConstrExprImplicitIsolatedness();

    NonIsolatedClass n = new;
    assertFalse(<any> n is isolated object {});

    testRuntimeIsolatedFlag();

    IsolatedServiceClassWithNoMutableFields o = new ([], {i: 1});
    assertTrue(<any> o is isolated object {});
    assertTrue(isMethodIsolated(o, "init"));

    IsolatedClientClassWithPrivateMutableFields p = new ({i: 1}, 2);
    assertTrue(<any> p is isolated object {});
    assertTrue(isMethodIsolated(p, "init"));

    assertTrue(ser is isolated service object {});
    assertTrue(isRemoteMethodIsolated(ser, "foo"));
    assertTrue(isResourceIsolated(ser, "get", "bar"));
    assertTrue(isMethodIsolated(ser, "baz"));
    assertTrue(isResourceIsolated(ser, "get", "corge"));

    ServiceClass q = new;
    assertTrue(<any> q is isolated service object {});
    assertTrue(isRemoteMethodIsolated(q, "foo"));
    assertTrue(isResourceIsolated(q, "get", "bar"));
    assertTrue(isMethodIsolated(q, "baz"));
    assertTrue(isResourceIsolated(q, "get", "corge"));
    assertTrue(isMethodIsolated(q, "quuz"));

    IsolatedClassUsingSelf r = new;
    assertTrue(<any> r is isolated object {});
    assertTrue(isMethodIsolated(r, "getMember"));
    assertTrue(isMethodIsolated(r, "getMemberInternal"));

    assertTrue(<function> getMember is isolated function);

    IsolatedClassWithBoundMethodAccess s = new;
    assertTrue(<any> s is isolated object {});
    assertTrue(isMethodIsolated(s, "bar"));
    assertTrue(isMethodIsolated(s, "baz"));
    s.bar();

    IsolatedClassReferringSelfOutsideLock t = new;
    assertTrue(<any> t is isolated object {});
    assertTrue(isMethodIsolated(t, "foo"));
    assertTrue(isMethodIsolated(t, "baz"));

    assertTrue(<function> f1 is isolated function);

    IsolatedClassWithBoundMethodAccessOutsideLock u = new;
    assertTrue(<any> u is isolated object {});
    assertTrue(isMethodIsolated(u, "bar"));
    assertTrue(isMethodIsolated(u, "baz"));

    IsolatedClassWithInvalidCopyInInMethodCall v = new;
    assertTrue(<any> v is isolated object {});
    assertTrue(isMethodIsolated(v, "baz"));
    assertTrue(isMethodIsolated(v, "qux"));

    assertTrue(<any> isolatedObjectWithInvalidCopyInInMethodCall is isolated object {});
    assertTrue(isMethodIsolated(v, "baz"));
    assertTrue(isMethodIsolated(v, "qux"));

    IsolatedClassAssigningProtectedFieldsToLocalVars w = new;
    assertTrue(isMethodIsolated(w, "baz"));

    NonIsolatedClient x = new;
    assertFalse(<any> x is isolated object {});
    assertTrue(isRemoteMethodIsolated(x, "sendMessage"));

    IsolatedClassWithRawTemplateTransfer y = new;
    assertTrue(<any> y is isolated object {});
    assertTrue(isMethodIsolated(y, "getArrOne"));
    assertTrue(isMethodIsolated(y, "getArrTwo"));
    assertTrue(isMethodIsolated(y, "getArrs"));

    IsolatedObjectWithIsolatedFunctionCallAccessingModuleVarOfSameTypeAsDefaultValueExpr z = new;
    assertTrue(<any> z is isolated object {});
    assertTrue(<any> f is isolated object {});
    assertTrue(<any> f3 is isolated function);
    assertTrue(<any> f4 is isolated function);
    assertTrue(isMethodIsolated(z, "init"));

    IsolatedClassIncludingObjectAndClass a2 = new ([]);
    assertTrue(isMethodIsolated(a2, "init"));
    assertTrue(isMethodIsolated(a2, "access"));

    ReadOnlyClass b2 = new;
    assertTrue(<any> b2 is isolated object {});
    assertTrue(isMethodIsolated(b2, "f1"));
    assertTrue(isMethodIsolated(b2, "f2"));

    IsolatedClassWithMultipleLocks c2 = new;
    assertTrue(<any> c2 is isolated object {});
    assertTrue(isMethodIsolated(c2, "fn"));
    assertFalse(isMethodIsolated(c2, "fn2"));
    assertTrue(<any> f5 is isolated function);
    assertFalse(<any> f6 is isolated function);

    assertTrue(<any> testAccessingListenerOfIsolatedObjectType is isolated function);
}

isolated function assertTrue(any|error actual) {
    assertEquality(true, actual);
}

isolated function assertFalse(any|error actual) {
    assertEquality(false, actual);
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
    panic error("expected '" + expectedValAsString + "', found '" + actualValAsString + "'");
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
