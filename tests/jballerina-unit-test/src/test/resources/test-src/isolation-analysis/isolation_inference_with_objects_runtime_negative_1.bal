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

class NonIsolatedClassWithNonPrivateMutableFields {
    int a;
    public map<int> b;
    private final string c = "invalid";

    function init(int a, map<int> b) {
        self.a = a;
        self.b = b.clone();
    }
}

object {} nonIsolatedObjectConstructorWithNonPrivateMutableFields = object {
    int a;
    public map<int> b;
    private final string c = "invalid";

    function init() {
        self.a = 1;
        self.b = {};
    }
};

type ObjectType object {
    int a;
    string[] b;
};

class NonIsolatedClassNotOverridingMutableFieldsInIncludedIsolatedObject {
    *ObjectType;

    function init() {
        self.a = 1;
        self.b = [];
    }
}

object {} nonIsolatedObjectNotOverridingMutableFieldsInIncludedIsolatedObject = object ObjectType {
    function init() {
        self.a = 1;
        self.b = [];
    }
};

class NonIsolatedClassAccessingMutableFieldsOutsideLock {
    final int a = 1;
    private string b = "hello";
    private int[] c;

    function init(int[] c) {
        self.c = c.clone();
    }

    function getB() returns string => self.b;

    function setB(string s) {
        self.b = s;
    }

    function updateAndGetC(int i) returns int[] {
        lock {
            self.c.push(i); // OK
        }
        return self.c;
    }
}

function testNonIsolatedObjectConstructorAccessingMutableFieldsOutsideLock() {
    object {} nonIsolatedObjectConstructorAccessingMutableFieldsOutsideLock = object {
        final int a = 1;
        private string b = "hello";
        private int[] c = [];

        function getB() returns string => self.b;

        function setB(string s) {
            self.b = s;
        }

        function updateAndGetC(int i) returns int[] {
            lock {
                self.c.push(i); // OK
            }
            return self.c;
        }
    };

    assertFalse(<any> nonIsolatedObjectConstructorAccessingMutableFieldsOutsideLock is isolated object {});
    assertTrue(isMethodIsolated(nonIsolatedObjectConstructorAccessingMutableFieldsOutsideLock, "getB"));
    assertTrue(isMethodIsolated(nonIsolatedObjectConstructorAccessingMutableFieldsOutsideLock, "setB"));
    assertTrue(isMethodIsolated(nonIsolatedObjectConstructorAccessingMutableFieldsOutsideLock, "updateAndGetC"));
}

int[] globIntArr = [1200, 12, 12345];
int[] globIntArrCopy = globIntArr;
int[] globIntArrCopy2 = globIntArrCopy;
string globStr = "1";
string globStr2 = "1.0";
map<boolean> globBoolMap = {a: true, b: false};

function accessGlobBoolMap(string s) {
    _ = (globBoolMap["a"] ?: true) && (globStr == s);
}

class NonIsolatedClassWithNonUniqueInitializerExprs {
    private int[][] a;
    private map<boolean> b = globBoolMap;
    private record {} c = {[globStr]: accessGlobBoolMap(globStr)};
    final string d = globStr;
    private table<record{ readonly int id; }> e = table key (id) [
        {id: 1, "name": "foo"},
        {id: 2, "name": string `str ${globStr}`}
    ];
    final readonly & xml[] f = [xml `hello ${globStr}`, xml `<!-- int: ${globIntArr[0]} -->`, xml `ok`,
                                xml `?pi ${globBoolMap["a"] is boolean ? "true" : globStr}?`];
    final int g = <int> checkpanic trap 'int:fromString(globStr);
    private float h;

    function init(int[][]? a) returns error? {
        self.a = a ?: [globIntArr, globIntArr];
        record {} rec = {"a": 1, "b": 2.0};
        anydata ad = rec;
        self.c = rec;
        self.h = check 'float:fromString(globStr2);
    }
}

function testNonIsolatedObjectWithNonUniqueInitializerExprs() {
    object {} nonIsolatedObjectWithNonUniqueInitializerExprs = object {
        private int[][] a = [globIntArr, globIntArr];
        private map<boolean> b = globBoolMap;
        private record {} c = {[globStr]: accessGlobBoolMap(globStr)};
        final string d = globStr;
        private table<record{ readonly int id; }> e = table key (id) [
            {id: 1, "name": "foo"},
            {id: 2, "name": string `str ${globStr}`}
        ];
        final readonly & xml[] f = [xml `hello ${globStr}`, xml `<!-- int: ${globIntArr[0]} -->`, xml `ok`,
                                    xml `?pi ${globBoolMap["a"] is boolean ? "true" : globStr}?`];
        final int g = <int> checkpanic trap 'int:fromString(globStr);
        private float h;

        function init() {
            record {} rec = {"a": 1, "b": 2.0};
            anydata ad = rec;
            self.c = rec;
            self.h = checkpanic 'float:fromString(globStr);
        }
    };

    assertFalse(<any> nonIsolatedObjectWithNonUniqueInitializerExprs is isolated object {});
    // https://github.com/ballerina-platform/ballerina-lang/issues/31371
    // assertFalse(isMethodIsolated(nonIsolatedObjectWithNonUniqueInitializerExprs, "init"));
}

class NonIsolatedClassWithInvalidCopyIn {
    public final record {} & readonly a;
    private int b;
    private map<boolean>[] c;

    function init(record {} & readonly a, int b, map<boolean>[] c) {
        self.a = a;
        self.b = b;
        self.c = c.clone();
    }

    function invalidCopyInOne(map<boolean> boolMap) {
        map<boolean> bm1 = {};
        lock {
            map<boolean> bm2 = {a: true, b: false};

            self.c[0] = globBoolMap;
            self.c.push(boolMap);
            self.c = [bm1, bm2];
        }
    }

    isolated function invalidCopyInTwo(map<boolean> boolMap) {
        map<boolean> bm1 = {};
        lock {
            map<boolean> bm2 = {};
            lock {
                map<boolean> bm3 = boolMap;
                bm2 = bm3;
            }

            self.c.push(boolMap);
            self.c[0] = boolMap;
            self.c = [bm1, bm2];
        }
    }
}

object {} nonIsolatedObjectWithInvalidCopyIn = object {
    public final record {} & readonly a = {"type": "final"};
    private int b = 0;
    private map<boolean>[] c = [];

    isolated function invalidCopyInOne(map<boolean> boolMap) {
        map<boolean> bm1 = {};
        lock {
            map<boolean> bm2 = {a: true, b: false};

            self.c[0] = boolMap;
            self.c.push(boolMap);
            self.c = [bm1, bm2];
        }
    }

    function invalidCopyInTwo(map<boolean> boolMap) {
        map<boolean> bm1 = {};
        lock {
            map<boolean> bm2 = {};
            lock {
                map<boolean> bm3 = boolMap;
                bm2 = bm3;
            }

            self.c.push(boolMap);
            self.c[0] = globBoolMap;
            self.c = [bm1, bm2];
        }
    }
};

class NonIsolatedClassWithInvalidCopyOut {
    public final record {} & readonly a = {"type": "final"};
    private int b = 1;
    private map<boolean>[] c;

    function init() {
        self.c = [];
    }

    function invalidCopyOutOne(map<boolean>[] boolMaps) returns map<boolean>[] {
        map<boolean>[] bm1 = boolMaps;
        lock {
            map<boolean>[] bm2 = [{a: true, b: false}];

            bm1 = self.c;
            globBoolMap = bm2[0];
            bm1 = [self.c[0]];
            return self.c;
        }
    }

    isolated function invalidCopyOutTwo(map<boolean>[] boolMaps) {
        map<boolean> bm1 = {};
        lock {
            map<boolean> bm2 = {};
            lock {
                map<boolean> bm3 = boolMaps[0].clone();
                bm1 = bm3;
            }

            bm1 = self.c[0];
            bm1 = bm2;
        }
    }
}

function testNonIsolatedObjectWithInvalidCopyOut() {
    object {} nonIsolatedObjectWithInvalidCopyOut = object {
        private map<boolean>[] c = [];

        isolated function invalidCopyOutOne(map<boolean>[] boolMaps) returns map<boolean> {
            map<boolean>[] bm1 = boolMaps;
            lock {
                map<boolean>[] bm2 = [{a: true, b: false}];

                bm1 = self.c;
                bm1 = bm2;
                bm1 = [self.c[0]];
                return self.c.pop();
            }
        }

        function invalidCopyOutTwo(map<boolean>[] boolMaps) {
            map<boolean> bm1 = {};
            lock {
                map<boolean> bm2 = {};
                lock {
                    map<boolean> bm3 = boolMaps[0].clone();
                    bm1 = bm3;
                }

                globBoolMap = self.c[0];
                bm1 = bm2;
            }
        }
    };

    assertFalse(<any> nonIsolatedObjectWithInvalidCopyOut is isolated object {});
    assertTrue(isMethodIsolated(nonIsolatedObjectWithInvalidCopyOut, "invalidCopyOutOne"));
    assertFalse(isMethodIsolated(nonIsolatedObjectWithInvalidCopyOut, "invalidCopyOutTwo"));
}

class NonIsolatedClassWithNonIsolatedFunctionInvocation {
    private int[] x = [];

    function testInvalidNonIsolatedInvocation() {
        lock {
            int[] a = self.x;

            IsolatedClass ic = new;
            a[0] = ic.nonIsolatedFunc();
            a.push(ic.nonIsolatedFunc());

            a = nonIsolatedFunc();
        }
    }
}

object {} nonIsolatedObjectWithNonIsolatedFunctionInvocation = object {
    private int[] x = [];

    function testInvalidNonIsolatedInvocation() {
        lock {
            int[] a = self.x;

            IsolatedClass ic = new;
            a[0] = ic.nonIsolatedFunc();
            a.push(ic.nonIsolatedFunc());

            a = nonIsolatedFunc();
        }
    }
};

class NonIsolatedClassWithInvalidObjectFields {
    IsolatedClass a = new; // Should be `final`
    isolated object {} b = isolated object { // Should be `final`
        final int i = 1;
        private map<int> j = {};
    };
    final object {} c = object {}; // should be an `isolated object`
}

int globInt = 1;

isolated class IsolatedClass {
    function nonIsolatedFunc() returns int => globInt;
}

function nonIsolatedFunc() returns int[] {
    return [1, 2, 3];
}

class NonIsolatedClassWithCopyInInsideBlock {
    private string[] uniqueGreetings = [];

    isolated function add(string[] greetings) {
        lock {
            if self.uniqueGreetings.length() == 0 {
                self.uniqueGreetings = greetings;
            }
        }
    }
}

class NonIsolatedClassWithInvalidCopyingWithClone {
    private anydata[] arr = [];
    private anydata[] arr2 = [];

    isolated function add(anydata val) {
        lock {
            self.arr.push(val);

            self.addAgain(val);
            outerAdd(val);
            anydata clonedVal = val;
            self.addAgain(clonedVal); // OK
            if val is anydata[] {
                self.arr.push(val.pop());

                lock {
                    self.arr.push(val.pop());
                }
            }

            if clonedVal is anydata[] {
                self.arr.push(clonedVal.pop()); // OK

                lock {
                    self.arr.push(clonedVal.pop());
                }
            }
        }
    }

    isolated function addAgain(anydata val) { }
}

isolated function outerAdd(anydata val) { }

decimal globDecimal = 0;

class CurrentConfig {
    private decimal[2] x = [1, 2.0];
    private record {
        int[] a;
        decimal b;
        boolean c;
    } y = {a: [1, 2], b: 1.0, c: true};

    function foo(decimal arr) {
        decimal a = 0;
        decimal a2 = 0;

        lock {
            [a, globDecimal] = self.x.clone();
            [a2, globDecimal] = self.x;
        }
    }

    function bar() {
        int[] a;
        decimal b;

        lock {
            boolean c;
            {a, b, c} = self.y;
            {a, b, c} = self.y.cloneReadOnly();
        }
    }
}

int[] y = [];
readonly & int[] z = [];

class NonIsolatedClassWithInvalidVarRefs {
    private int[][] x;
    private int[] y;
    private any[] z;

    function init() {
        self.y = y;
    }

    function push() {
        lock {
            self.x = let int[] z = y in [y, z];
            self.y = (let int[]? z = y in ((z is int[]) ? z : <int[]> []));
            self.x = let int[] v = z, int[] w = y in <int[][]> [z, v, w];
        }
    }

    function nested() {
        any[] arr = let int[] v = y in [v];

        lock {
            self.z = let int[] v = y in [v, let int[] w = y in function () returns int[2][] { return [w, v]; }];
        }
    }
}

service class NonIsolatedServiceClassWithNonPrivateMutableFields {
    int a;
    public map<int> b;
    private final string c = "invalid";

    function init(int a, map<int> b) {
        self.a = a;
        self.b = b.clone();
    }
}

client class InvalidIsolatedClientClassWithCopyInInsideBlock {
    private string[] uniqueGreetings = [];

    isolated function add(string[] greetings) {
        lock {
            if self.uniqueGreetings.length() == 0 {
                self.uniqueGreetings = greetings;
            }
        }
    }
}

type ServiceObjectType service object {
    int a;
    string[] b;
};

service class NonIsolatedServiceClassNotOverridingMutableFieldsInIncludedIsolatedServiceObject {
    *ServiceObjectType;

    function init() {
        self.a = 1;
        self.b = [];
    }
}

int[] isolatedArr = [];

service "s1" on new Listener() {
    int[] x = [];

    remote function foo() {
        self.x = [2, 3];
    }

    resource function get bar() {
        self.x = [2, 3];
    }

    function baz() {
        self.x = [2, 3];
    }

    function qux() {
        globIntArr = [];
    }

    resource function get quux() {
        globIntArr = [];
    }

    resource function get corge() {
        lock {
            self.x = [2, 3];
            isolatedArr = [];
        }
    }
}

service object {} ser = service object {
    int[] x = [];

    remote function foo() {
        self.x = [2, 3];
    }

    resource function get bar() {
        self.x = [2, 3];
    }

    function baz() {
        self.x = [2, 3];
    }

    function qux() {
        globIntArr = [];
    }

    resource function get quux() {
        globIntArr = [];
    }

    resource function get corge() {
        lock {
            self.x = [2, 3];
            isolatedArr = [];
        }
    }
};

service class ServiceClass {
    int[] x = [];

    remote function foo() {
        self.x = [2, 3];
    }

    resource function get bar() {
        self.x = [2, 3];
    }

    function baz() {
        self.x = [2, 3];
    }

    function qux() {
        globIntArr = [];
    }

    resource function get quux() {
        globIntArr = [];
    }

    resource function get corge() {
        lock {
            self.x = [2, 3];
            isolatedArr = [];
        }
    }

    function quuz() {
        lock {
            self.x = [2, 3];
            isolatedArr = [];
        }
    }
}

public class Listener {

    public function 'start() returns error? {}

    public function gracefulStop() returns error? {}

    public function immediateStop() returns error? {}

    public function detach(service object {} s) returns error? {}

    public function attach(service object {} s, string name) returns error? {
        if name == "s1" {
            assertFalse(<any> s is isolated object {});
            assertTrue(isRemoteMethodIsolated(s, "foo"));
            assertTrue(isResourceIsolated(s, "get", "bar"));
            assertTrue(isMethodIsolated(s, "baz"));
            assertFalse(isMethodIsolated(s, "qux"));
            assertFalse(isResourceIsolated(s, "get", "quux"));
            assertFalse(isResourceIsolated(s, "get", "corge"));
        } else if name == "s2" {
            assertTrue(<any> s is isolated object {});
            assertFalse(isResourceIsolated(s, "post", "foo"));
        } else {
            panic error(string `expected s1 or s2, found ${name}`);
        }
    }
}

int[] x = [];

function testInvalidCopyInWithNonObjectSelf1(int[] 'self) {
    lock {
        x = 'self;
    }
}

function testInvalidCopyInWithNonObjectSelf2() {
    lock {
        int[] 'self = [];
        x = 'self;
    }
}

class NonIsolatedClassWithInvalidCopyInInMethodCall {
    private map<int> m = {};

    isolated function baz() returns map<int>[] {
        map<int>[] y = [];

        lock {
            y[0] = self.m;
            y.push(self.m);
            array:push(y, self.m);
        }

        return y;
    }

    function qux(map<int[]> y) {
        lock {
            _ = y.remove(self.m["a"].toString());
        }
    }
}

var nonIsolatedObjectWithInvalidCopyInInMethodCall = object {
    private map<int> m = {};

    function baz() returns map<int>[] {
        map<int>[] y = [];

        lock {
            y[0] = self.m;
            y.push(self.m);
            array:push(y, self.m);
            return y;
        }
    }

    function qux(map<int[]> y) {
        lock {
            _ = y.remove(self.m["a"].toString());
        }
    }
};

class IsolatedClassWithInvalidCopyOut2 {
    private map<int> m = {};

    function baz() returns map<int>[] {
        map<int>[] y = [];
        map<int> z;
        lock {
            map<int>[] y2 = [];
            y[0] = self.m;
            z = self.m;
            return y2;
        }
    }
}

const fromMobile = "";
configurable string toMobile = "";

NonIsolatedClient cl = new;

service "s2" on new Listener() {
    resource function post foo() returns error? {
        Response resp;
        lock {
            resp = check cl->sendMessage(fromMobile, toMobile, "Hi!");
        }
    }
}

type Response record {|
   string message;
   int id;
|};

public client class NonIsolatedClient {
    int i = 1;

    isolated remote function sendMessage(string x, string y, string z)
        returns Response|error => {message: "Hello", id: 0};
}

class NonIsolatedClassWithQueryExprAsTransferOut {
    private isolated object {}[] arr = [];

    function getArr() returns isolated object {}[] {
        lock {
            return from var ob in self.arr select ob;
        }
    }
}

class NonIsolatedClassWithInvalidRawTemplateTransfer {
    private int[] arr = [];
    private isolated object {}[] arr2 = [];

    function getArrOne() returns any {
        lock {
            return `values: ${self.arr}`;
        }
    }

    function getArrTwo() returns any {
        lock {
            return `values: OK ${self.arr.clone()} invalid ${self.arr2}`;
        }
    }

    function getArrs(int[] intArr) returns any {
        lock {
            any val = `arr: ${intArr}`;
            return `values: ${self.arr2}, ${"Hello"}, ${self.arr}, ${val}, ${self.arr.clone()}`;
        }
    }
}

function testIsolatedInference() {
    NonIsolatedClassWithNonPrivateMutableFields a = new NonIsolatedClassWithNonPrivateMutableFields(1, {});
    assertFalse(<any> a is isolated object {});
    assertTrue(isMethodIsolated(a, "init"));

    assertFalse(<any> nonIsolatedObjectConstructorWithNonPrivateMutableFields is isolated object {});
    // https://github.com/ballerina-platform/ballerina-lang/issues/31371
    // assertTrue(isMethodIsolated(nonIsolatedObjectConstructorWithNonPrivateMutableFields, "init"));

    NonIsolatedClassNotOverridingMutableFieldsInIncludedIsolatedObject c = new;
    assertFalse(<any> c is isolated object {});
    assertTrue(isMethodIsolated(c, "init"));

    assertFalse(<any> nonIsolatedObjectNotOverridingMutableFieldsInIncludedIsolatedObject is isolated object {});
    // https://github.com/ballerina-platform/ballerina-lang/issues/31371
    // assertTrue(isMethodIsolated(nonIsolatedObjectNotOverridingMutableFieldsInIncludedIsolatedObject, "init"));

    NonIsolatedClassAccessingMutableFieldsOutsideLock d = new ([]);
    assertFalse(<any> d is isolated object {});
    assertTrue(isMethodIsolated(d, "init"));
    assertTrue(isMethodIsolated(d, "getB"));
    assertTrue(isMethodIsolated(d, "setB"));
    assertTrue(isMethodIsolated(d, "updateAndGetC"));

    testNonIsolatedObjectConstructorAccessingMutableFieldsOutsideLock();

    NonIsolatedClassWithNonUniqueInitializerExprs e = checkpanic new (());
    assertFalse(<any> e is isolated object {});
    assertFalse(isMethodIsolated(e, "init"));

    testNonIsolatedObjectWithNonUniqueInitializerExprs();

    NonIsolatedClassWithInvalidCopyIn f = new ({}, 1, []);
    assertFalse(<any> f is isolated object {});
    assertTrue(isMethodIsolated(f, "init"));
    assertFalse(isMethodIsolated(f, "invalidCopyInOne"));
    assertTrue(isMethodIsolated(f, "invalidCopyInTwo"));

    assertFalse(<any> nonIsolatedObjectWithInvalidCopyIn is isolated object {});
    assertTrue(isMethodIsolated(nonIsolatedObjectWithInvalidCopyIn, "invalidCopyInOne"));
    assertFalse(isMethodIsolated(nonIsolatedObjectWithInvalidCopyIn, "invalidCopyInTwo"));

    NonIsolatedClassWithInvalidCopyOut g = new;
    assertFalse(<any> g is isolated object {});
    assertTrue(isMethodIsolated(g, "init"));
    assertFalse(isMethodIsolated(g, "invalidCopyOutOne"));
    assertTrue(isMethodIsolated(g, "invalidCopyOutTwo"));

    testNonIsolatedObjectWithInvalidCopyOut();

    NonIsolatedClassWithNonIsolatedFunctionInvocation h = new;
    assertFalse(<any> h is isolated object {});
    assertFalse(isMethodIsolated(h, "testInvalidNonIsolatedInvocation"));

    assertFalse(<any> nonIsolatedObjectWithNonIsolatedFunctionInvocation is isolated object {});
    assertFalse(isMethodIsolated(nonIsolatedObjectWithNonIsolatedFunctionInvocation, "testInvalidNonIsolatedInvocation"));

    NonIsolatedClassWithInvalidObjectFields i = new;
    assertFalse(<any> i is isolated object {});

    NonIsolatedClassWithCopyInInsideBlock j = new;
    assertFalse(<any> j is isolated object {});
    assertTrue(isMethodIsolated(j, "add"));

    NonIsolatedClassWithInvalidCopyingWithClone k = new;
    assertFalse(<any> k is isolated object {});
    assertTrue(isMethodIsolated(k, "add"));
    assertTrue(isMethodIsolated(k, "addAgain"));

    CurrentConfig l = new;
    assertFalse(<any> l is isolated object {});
    assertFalse(isMethodIsolated(l, "foo"));
    assertTrue(isMethodIsolated(l, "bar"));

    NonIsolatedClassWithInvalidVarRefs m = new;
    assertFalse(<any> m is isolated object {});
    assertFalse(isMethodIsolated(m, "init"));
    assertFalse(isMethodIsolated(m, "push"));
    assertFalse(isMethodIsolated(m, "nested"));

    NonIsolatedServiceClassWithNonPrivateMutableFields n = new (1, {});
    assertFalse(<any> n is isolated object {});
    assertTrue(isMethodIsolated(n, "init"));

    InvalidIsolatedClientClassWithCopyInInsideBlock o = new;
    assertFalse(<any> o is isolated object {});
    assertTrue(isMethodIsolated(o, "add"));

    NonIsolatedServiceClassNotOverridingMutableFieldsInIncludedIsolatedServiceObject p = new;
    assertFalse(<any> p is isolated object {});
    assertTrue(isMethodIsolated(p, "init"));

    assertFalse(<any> ser is isolated object {});
    assertTrue(isRemoteMethodIsolated(ser, "foo"));
    assertTrue(isResourceIsolated(ser, "get", "bar"));
    assertTrue(isMethodIsolated(ser, "baz"));
    assertFalse(isMethodIsolated(ser, "qux"));
    assertFalse(isResourceIsolated(ser, "get", "quux"));
    assertFalse(isResourceIsolated(ser, "get", "corge"));

    ServiceClass q = new;
    assertFalse(<any> q is isolated object {});
    assertTrue(isRemoteMethodIsolated(q, "foo"));
    assertTrue(isResourceIsolated(q, "get", "bar"));
    assertTrue(isMethodIsolated(q, "baz"));
    assertFalse(isMethodIsolated(q, "qux"));
    assertFalse(isResourceIsolated(q, "get", "quux"));
    assertFalse(isResourceIsolated(q, "get", "corge"));
    assertFalse(isMethodIsolated(q, "quuz"));

    assertFalse(<any> testInvalidCopyInWithNonObjectSelf1 is isolated function);
    assertFalse(<any> testInvalidCopyInWithNonObjectSelf2 is isolated function);

    NonIsolatedClassWithInvalidCopyInInMethodCall r = new;
    assertFalse(<any> r is isolated object {});
    assertTrue(isMethodIsolated(r, "baz"));
    assertTrue(isMethodIsolated(r, "qux"));

    assertFalse(<any> nonIsolatedObjectWithInvalidCopyInInMethodCall is isolated object {});
    assertTrue(isMethodIsolated(nonIsolatedObjectWithInvalidCopyInInMethodCall, "baz"));
    assertTrue(isMethodIsolated(nonIsolatedObjectWithInvalidCopyInInMethodCall, "qux"));

    IsolatedClassWithInvalidCopyOut2 s = new;
    assertFalse(<any> s is isolated object {});
    assertTrue(isMethodIsolated(s, "baz"));

    NonIsolatedClassWithQueryExprAsTransferOut t = new;
    assertFalse(<any> t is isolated object {});
    assertTrue(isMethodIsolated(t, "getArr"));

    NonIsolatedClassWithInvalidRawTemplateTransfer u = new;
    assertFalse(<any> u is isolated object {});
    assertTrue(isMethodIsolated(u, "getArrOne"));
    assertTrue(isMethodIsolated(u, "getArrTwo"));
    assertTrue(isMethodIsolated(u, "getArrs"));
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
