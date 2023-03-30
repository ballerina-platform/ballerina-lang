 // Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 //
 // WSO2 Inc. licenses this file to you under the Apache License,
 // Version 2.0 (the "License"); you may not use this file except
 // in compliance with the License.
 // You may obtain a copy of the License at
 //
 //   http://www.apache.org/licenses/LICENSE-2.0
 //
 // Unless required by applicable law or agreed to in writing,
 // software distributed under the License is distributed on an
 // "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 // KIND, either express or implied.  See the License for the
 // specific language governing permissions and limitations
 // under the License.
import ballerina/lang.value;
function f1() {
    int i = 0; // unused `i`

    int j; // unused `j`

    int k; // unused `k`
    k = 0;
}

function f2() {
    int l; // used `l`
    l = 0;
    int[] arr = []; // used `arr`
    arr[l] = 1;

    int m = 0; // used `m`
    arr[m] = 1;

    string n = "x"; // used `n`

    record {int x;} o = {x: 12}; // used `o`
    o.x = 1;
    o[n] = 1;

    int[] arr2 = []; // used `arr2`
    int l2 = 1; // used `l2`
    int i = arr2[l2]; // unused `i`
    arr2[l] = 1;

    record {int x;} o2 = {x: 12}; // used `o2`
    int _ = o2.x;

    string n2 = "x"; // used `n2`
    record {int x;} o3 = {x: 12}; // used `o3`
    anydata a = o3[n2]; // unused `a`
}

function f3() {
    int[3] [a, b, c] = [1, 2, 3]; // unused `a`, `b`, `c`

    [int, record { boolean e; string f; }, boolean...] [d, {e, ...f}, ...g] = // unused `d`, `e`, `f`, `g`
        [1, {e: true, f: "str"}, true, false];
    [d, f] = [1, {f: ""}];

    [int, error<record { int i; string j?; }>, [int, string]] [h, error(m, i = i, ...k), l] = // unused `h`, `m`, `i`, `k`, `l`
        [1, error("err", i = 1), [1, ""]];
    h = 1;
    m = "";
    i = 2;
    k = {};
    l = [];
}

function f4() {
    int[3] [a, b, c] = [1, 2, 3]; // unused `c`
    int _ = a + b;

    [int, record { boolean e; string f; }, boolean...] [d, {e, ...f}, ...g] = // unused `f`
        [1, {e: true, f: "str"}, true, false];
    _ = [d, e, g];

    [int, error<record { int i; string j?; }>, [int, string]] [h, error(m, i = i, ...k), l] = // unused `m`, `k`
        [1, error("err", i = 1), [1, ""]];
    h = l[0] + i;
}

function f5() {
    int i = 1; // used within anon function
    int j = 2; // used within anon function
    int k = 3; // used within anon function
    int l = 4; // unused

    var _ = function () returns int {
        int m; // unused
        int n = 1; // used within anon function
        int o = 2; // used within anon function

        function () returns int _ = () => n;

        var p = function () {
            _ = o;
        };

        return i;
    };

    function (int v) returns string _ = function (int v) returns string => (v + j).toString();

    function () returns int _ = () => k * 1;
}

function f6() {
    worker w1 { // Transformed to an async call, warnings shouldn't be logged for those variables.
    }

    future<()> a = start fn(); // unused
}

function fn() {
}

function f7() returns int {
    do {
        check foo();
        check bar();
    } on fail error e { // unused
        return 1;
    }

    return 0;
}

function foo() returns error? => ();
function bar() returns error? => ();

function f8() {
    error? err = ();
    if err is () { // used

    }
}

int globalVar = 0; // OK, warning only for local variables

record {
    int a;
    error b;
    string[2] c;
} {a: ga, b: error(gbm, ...gbr), c: [gc1, gc2]} = {a: 1, b: error(""), c: []}; // OK, warning only for local variables

function f9(int a, int b = 1, int... c) { // OK, warning only for local variables
}

function f10() {
    var l = 0; // used `l`
    var arr = <int[]> []; // used `arr`
    arr[l] = 1;

    var n = "x"; // used `n`

    var o = <record {int x;}> {x: 12}; // used `o`
    o.x = 1;
    o[n] = 1;

    var arr2 = <int[]> []; // used `arr2`
    var l2 = 1; // used `l2`
    var i = arr2[l2]; // unused `i`
    arr2[l] = 1;

    var o2 = <record {int x;}> {x: 12}; // used `o2`
    int _ = o2.x;

    var n2 = "x"; // used `n2`
    var o3 = <record {int x;}> {x: 12}; // used `o3`
    var a = o3[n2]; // unused `a`
}

type Foo record {
    int i;
    string[5] j;
    error<record {| int i; string j; |}> k;
};

function f11() {
    int[] a1 = []; // used

    foreach var i in a1 { // unused `i`
    }

    foreach var i in a1 { // used `i`
        _ = 1 + i;
    }

    error[] a2 = []; // used

    foreach error e in a2 { // unused `e`
    }

    foreach error e in a2 { // used `e`
        int m; // unused `m`
        _ = e is error<record {int i;}>;
    }

    Foo[] a3 = [];

    foreach Foo {i, j: [j1, j2, ...jr], k: error(km, i = kd1, ...kd2)} in a3 { // unused `i`, `j1`, `j2`, `jr`, `km`, `kd1`, `kd2`
    }

    foreach Foo {i, j: [j1, j2, ...jr], k: error(km, i = kd1, ...kd2)} in a3 { // unused `jr`, `km`, `kd2`
        int x = i + kd1;
        string _ = j1 + j2;
    }
}

function f12() {
    int l = 9; // used
    l += 1;
}

type Customer record {
    string name;
};

type Person record {
    string firstName;
    string lastName;
};

function f13() {
    int _ = let int x = 1, int y = 2 in 1; // unused `x`, `y`

    int _ = let int x = 1, int y = 2 in x + y; // used `x`
}

function f14() {
    int[] arr = [];

    int[] _ = from int i in arr // unused `i`
                select 1;

    int[] _ = from int i in arr // used `i`
                select i;

    int[] _ = from int i in arr  // used `i`
                let int j = 2 * i  // unused `j`
                select 1;

    int[] _ = from int i in arr // used `i`
                let int j = 2 * i // used `j`
                select j;

    Customer[] customerList = [];
    Person[] personList = [];

    record {}[] _ = from var customer in customerList // unused `customer`
                        join Person person in personList // unused `person`
                        on 1 equals 1
                        select {
                            "name": ""
                        };

    record {}[] _ = from Customer customer in customerList // unused `customer`
                        join var person in personList // used `person`
                        on 1 equals 1
                        select {
                            "name": person.firstName
                        };

    record {}[] _ = from var customer in customerList // unused `customer`
                        join Person person in personList // used `person`
                        on customer.name equals person.firstName + person.lastName
                        select {
                            "name": person.firstName
                        };
}

function f15() {
    int[] x = [];

    match x {
        var [a, b]|var [a, ...b] => { // unused `a`, `b`
            int _ = 1;
        }
        var [d, e, f] => { // unused `f`
            int _ = d + e;
        }
        [var g]|var [...g] => { // unused `g`
        }
    }

    any|error y = 1;

    match y {
        error(var a, var b, ...var c)|var [a, b, ...c] => { // unused `b`
            string s = a + c.toBalString();
        }
        var {d}|[var d] => { // unused `d`
        }
        var m => { // unused `m`
        }
    }
}

service on new Listener(10) {
    resource function get f1() {
        int i = 1;
    }

    remote function fn2() {
        int[] a = [];
        int b = 1;
        a[b] = 2;
    }

    function fn3(int i) {
        int j;
        j = 0;
    }
}

class Listener {
    int i;

    function init(int i) {
        int j = 1;
        self.i = i;
    }

    public function attach(service object {} s, string|string[]? name = ()) returns error? {
    }

    public function detach(service object {} s) returns error? {
    }

    public function 'start() returns error? {
        int x = 1;
    }

    public function gracefulStop() returns error? {
    }

    public function immediateStop() returns error? {
    }
}

function init() {
    int i = 1;
    int j = 2;
    int k = j + 1;
}

function f16() {
    record {} logRecord = {};
    string format = "";
    string s = format == "JSON" ? logRecord.toJsonString() : value:toString(logRecord); // Warning should be only for `s`.
}

function f17() {
    foreach int i in 0 ... 9 { // OK to not use `i`.

    }

    int[] m = [];

    foreach int j in 0 ..< 9 { // OK to not use `j`.
        m = []; // Unused `m`.
    }

    int[] n = from int k in 0 ..< 8 select 0; // Unused `n`, OK to not use `k`.

    int[] _ = from int l in 0 ... 8 select 0; // OK to not use `l`.

    foreach int i in 0 ... 9 { // OK to not use `i`.
        _ = i; // `i` is used anyway.
    }

    int[] _ = from int l in 0 ... 8 select l; // OK to not use `l`, `l` is used anyway.
}

type Bar record { int i?; };

function f18(Foo foo) {
    Bar f = {i: 1}; // used
    f.i = 2;

    int[] g = [];  // used
    g[0] = 1;

    int h = 1; // unused
    h = 2;

    int[] i; // unused
    i = [];

    string[] j = foo.j; // used
    j[0] = "str";
}

service / on new Listener(1234) {
    resource function get foo/[string... s](int a, string b, float... c) { // Shouldn't log warnings for these.
        json j = 1; // unused `j`
    }

    resource function get bar/[string s](int a, string b, float... c) { // Shouldn't log warnings for these.
        int[] x = []; // used
        x[0] = 1;

        int[] y = x.clone();
        int k = y[0]; // unused `k`
        int _ = y[0];
    }
}

type AnnotTupleOne record {|
   string value;
|};

annotation AnnotTupleOne annotOne on type, field;

function f19() {
    string n3 = "n3"; // used `n3`
    [@annotOne {value: n3} int] k1 = []; // unused `k1`

    string n4 = "n4"; // used `n4`
    record {@annotOne {value: n4} string x;} k2 = {x: ""}; // unused `k2`
}
