// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

type Foo record {
    string x = "test string";
    Bar bar = {};
    Bar bar2 = {};
    Baz baz = new;
    string s?;
};

type FooTwo record {
    int x = 1;
    Bar bar = {};
    Bar bar2 = {};
    Baz baz = new;
    string s = "hello";
};

type Bar record {
    int x = 1;
    float f = 2.0;
};

class Baz {
    string x;
    Qux q;

    isolated function init() {
        self.x = "string value";
        self.q = new;
    }
}

class BazTwo {
    int x;
    Qux q;

    function init() {
        self.x = 1;
        self.q = new;
    }
}

class Qux {
    string x = "test string";
}

int gi = 1;
int|string gsi = 2;

function testBasicValueStoreForVariable() returns boolean {
    int i;

    // a store on an uninitialized lvalue will initialize it
    i = 10;

    gi = 2;

    return i == 10 && gi == 2;
}

function testValueStoreForMap() returns boolean {
    map<string> m1 = { one: "string one", two: "string two" };
    map<map<int>> m2 = { one: { x: 1, y: 2 }, two: { z: 45 } };

    // a successful store operation on an undefined lvalue will result in the addition of a member to the container
    m1["three"] = "string three";
    m1["four"] = "string four";

    m2["one"]["z"] = 3;
    m2["two"]["four"] = 4;

    return m1["three"] == "string three" && m1["four"] == "string four" && m2["one"]["z"] == 3 &&
            m2["two"]["four"] == 4;
}

function testValueStoreForRecord() returns boolean {
    Foo f = {};

    f.x = "hello";
    f["z"] = 2;

    f.bar = { x: 2 };
    f["bar2"] = { x: 3 };

    f.bar.f = 3.0;
    f["bar2"]["y"] = 4;

    f.s = "string for optional field";

    return f["x"] == "hello" && f["z"] == 2 && f.bar.x == 2 && f.bar2.x == 3 && f.bar.f == 3.0 && f.bar2["y"] == 4 &&
            f?.s == "string for optional field";
}

function testValueStoreForObject() returns boolean {
    Baz b = new;

    b.x = "hello world";
    b.q.x = "world";
    return b.x == "hello world" && b.q.x == "world";
}

function testBasicValueStoreForUnionVariable() returns boolean {
    int|boolean i;

    // a store on an uninitialized lvalue will initialize it
    i = 120;

    gsi = "hello";

    return i == 120 && gsi == "hello";
}

function testValueStoreForMapUnion() returns boolean {
    map<string> m = { one: "string one", two: "string two" };
    map<string>|map<float> m1 = m;
    map<map<int>> m0 = { one: { x: 1, y: 2 }, two: { z: 45 } };
    map<map<int>>|map<map<map<boolean>>> m2 = m0;

    // a successful store operation on an undefined lvalue will result in the addition of a member to the container
    m1["three"] = "string three";
    m1["four"] = "string four";

    m2["one"]["z"] = 3;
    m2["two"]["four"] = 4;

    return m1["three"] == "string three" && m1["four"] == "string four" && m2["one"]["z"] == 3 &&
            m2["two"]["four"] == 4;
}

function testValueStoreForRecordUnion() returns boolean {
    Foo f0 = {};
    Foo|FooTwo f = f0;

    f.x = "hello";
    f["z"] = 2;

    f.bar = { x: 2 };
    f["bar2"] = { x: 3 };

    f.bar.f = 3.0;
    f["bar2"]["y"] = 4;

    f.s = "string for optional field";

    return f["x"] == "hello" && f["z"] == 2 && f.bar.x == 2 && f.bar2.x == 3 && f.bar.f == 3.0 && f.bar2["y"] == 4 &&
        f?.s == "string for optional field";
}

function testValueStoreForObjectUnion() returns boolean {
    Baz b0 = new;
    Baz|BazTwo b = b0;

    b.x = "hello world";
    b.q.x = "world";
    return b.x == "hello world" && b.q.x == "world";
}

// TODO: Maryam add tests for records via objects and vice versa

type ARec record {
    string|int i;
};

type BRec record {
    int i;
};

function testInherentTypeViolatingUpdate1() {
    BRec b = { i: 120 };
    ARec a = b;
    a.i = "hello";
}

class AObj {
    boolean|int i;

    function init(boolean|int i) {
        self.i = i;
    }
}

class BObj {
    boolean i;

    function init(boolean i) {
        self.i = i;
    }
}

function testInherentTypeViolatingUpdate2() {
    BObj b = new(true);
    AObj a = b;
    a.i = 10;
}

function testInherentTypeViolatingUpdate3() {
    int[2] a = [1, 2];
    any[] b = a;
    b[0] = "hello";
}

type CRec record {
    float f;
};

type DRec record {|
    float f;
|};

function testInvalidUpdateOnClosedRecord() {
    DRec d = { f: 2.0 };
    CRec c = d;
    c["g"] = 10;
}

function testInvalidUpdateOnClosedArray() {
    int[2] a = [1, 2];
    any[] b = a;
    b[2] = 1;
}

function testFrozenValueUpdate() {
    BRec b = { i: 120 };
    BRec b2 = b.cloneReadOnly();
    b2.i = 1;
}

function testArrayFillSuccess1() returns boolean {
    int[] i = [0, 1];
    i[4] = 4;
    return i.length() == 5 && i[0] == 0 && i[1] == 1 && i[4] == 4 && i[2] == i[3] && i[2] == 0;
}

type E record {
    int i = 10;
    string s?;
    F f = {};

};

type F record {
    float f = 1.0;
};

function testArrayFillSuccess2() returns boolean {
    E e1 = { i: 120, s: "hello", f: { f: 1.4 } };
    E e2 = { i: 345, f: { f: 1.4 } };
    E[] e = [e1];
    e[3] = e2;

    return e.length() == 4 && e[0] == e1 && e[3] == e2 && e[1] == e[2] && e[1].i == 10 && e[1]?.s is () &&
            e[1].f.f == 1.0;
}

class G {

    string s;

    function init(string s) {
        self.s = s;
    }
}

function testArrayFillFailure() {
    G[] a = [new("hello"), new("world")];
    a[3] = new("test");
}

function testArrayFillOnFilledContainer() returns boolean {
    map<map<string[]>> m = {};
    m["one"]["two"][3] = "test";

    if !(m["one"]["two"] is string[]) {
        return false;
    }

    string[] s = <string[]> m["one"]["two"];
    return m.length() == 1 && s.length() == 4 && s[0] == "" && s[1] == "" && s[2] == "" && s[3] == "test";
}

type H record {
    map<int> m?;
};

class I {
    H h = {};
}

function testFillingReadOnInitializedObjectField() returns boolean {
    I i = new;
    i.h.m["one"] = 1;
    i.h.m["two"] = 2;

    map<int>? rm = i.h?.m;

    if (rm is () || rm.length() != 2) {
        return false;
    }

    return i.h?.m["one"] == 1 && i.h?.m["two"] == 2;
}

type J record {
    string s = "default value";
};

function testFillingReadOnMapPositive() returns boolean {
    map<map<int>> m1 = {};
    m1["one"]["id"] = 10;

    map<J> m2 = {};
    m2["one"]["s"] = "value";

    map<int>? m3 = m1["one"];
    if (m3 is () || m3.length() != 1) {
        return false;
    }

    J? j = m2["one"];
    if (j is () || j.length() != 1) {
        return false;
    }

    return m1["one"]["id"] == 10 && m2["one"]["s"] == "value";
}

type K record {
    J j?;
};

function testFillingReadOnRecordPositive() returns boolean {
    K k1 = {};
    K k2 = {};

    k1.j.s = "new value 1";
    k2.j.s = "new value 2";

    J? j1 = k1?.j;
    J? j2 = k2?.j;

    if (j1 is () || j2 is ()) {
        return false;
    }

    return k1?.j?.s == "new value 1" && k2["j"]["s"] == "new value 2";
}

type L record {
    int i;
};

function testFieldUpdateOfElementForMapWithNoFillerValue() returns boolean {
    map<L> m = { one: { i: 10 } };
    m["one"]["i"] = 1;
    return m["one"]["i"] == 1;
}

function testFillingReadOnMappingNegative() {
    map<L> m = {};
    m["one"]["i"] = 1;
}

type M record {
    L l?;
};

function testFieldUpdateOfElementForRecordWithNoFillerValue() returns boolean {
    M m1 = { l: { i: 10 } };
    m1["l"]["i"] = 150;

    M m2 = { l: { i: 10 } };
    m2.l.i = 250;

    return m1["l"]["i"] == 150 && m2?.l?.i == 250;
}

function testFillingReadOnRecordNegativeFieldAccessLvExpr() {
    M m = {};
    m.l.i = 1;
}

function testFillingReadOnRecordNegativeMemberAccessLvExpr() {
    M m = {};
    m["l"]["i"] = 1;
}
