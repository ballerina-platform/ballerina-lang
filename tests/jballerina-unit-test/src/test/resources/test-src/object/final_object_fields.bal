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

const INHERENT_TYPE_VIOLATION_REASON = "{ballerina/lang.object}InherentTypeViolation";
const INVALID_UPDATE_REASON = "{ballerina/lang.object}InvalidUpdate";

function testFinalObjectFields() {
    testObjectWithSimpleFinalFields();
    testInvalidObjectSimpleFinalFieldUpdate();
    testValidUpdateOfPossiblyFinalFieldInUnion();
    testInvalidUpdateOfPossiblyFinalFieldInUnion();
    testObjectWithStructuredFinalFields();
    testFinalFieldWithDefaultValue();
    testTypeReadOnlyFlagForAllFinalFields();
    testSubTypingWithFinalFields();
    testSubTypingWithFinalFieldsViaReadOnlyType();
    testSubTypingWithFinalFieldsNegative();
    testSubTypingWithReadOnlyFieldsPositiveComposite();
    testSubTypingWithReadOnlyFieldsNegativeComposite();
    testSubTypingWithMutableFinalFields();
    testSubTypingWithMutableFinalFieldsNegative();
    testUpdatingMutableFinalField();
}

public class Student {
    final string name;
    final int id;
    float avg = 80.0;
    public function init(string n, int i) {
        self.name = n;
        self.id = i;
    }
}

public class NonReadOnlyStudent {
    string name;
    int id;
    int yob;

    public function init(string n, int i, int y) {
        self.name = n;
        self.id = i;
        self.yob = y;
    }
}

function testObjectWithSimpleFinalFields() {
    Student|NonReadOnlyStudent st = new ("Maryam", 1234);
    assertEquality("Maryam", st.name);
    assertEquality(1234, st.id);
}

function testInvalidObjectSimpleFinalFieldUpdate() {
    Student st1 = new("Maryam", 1234);

    // Invalid updates.
    var fn1 = function () {
        object { string name; } rec = st1;
        rec.name = "Jo";
    };
    error? res = trap fn1();
    assertTrue(res is error);

    error err = <error> res;
    assertEquality(INVALID_UPDATE_REASON, err.message());
    assertEquality("cannot update 'final' field 'name' in object of type 'Student'", err.detail()["message"]);
}

class ReadonlyNamedPerson {
    final string name;
    int id;

    function init(string name, int id) {
        self.name = name;
        self.id = id;
    }
}

class NonReadonlyNamedPerson {
    string name;
    int id;

    function init(string name, int id) {
        self.name = name;
        self.id = id;
    }
}

function testValidUpdateOfPossiblyFinalFieldInUnion() {
    NonReadonlyNamedPerson a = new ("Jo", 1234);

    assertEquality("Jo", a.name);
    assertEquality(1234, a.id);

    ReadonlyNamedPerson|NonReadonlyNamedPerson b = a;
    b.name = "May";
    b.id = 4567;

    assertEquality("May", b.name);
    assertEquality(4567, b.id);

    ReadonlyNamedPerson c = new ("Emma", 1234);

    assertEquality("Emma", c.name);
    assertEquality(1234, c.id);

    ReadonlyNamedPerson|NonReadonlyNamedPerson d = c;
    d.id = 2525;

    assertEquality(2525, d.id);
}

function testInvalidUpdateOfPossiblyFinalFieldInUnion() {
    ReadonlyNamedPerson c = new ("Emma", 1234);

    assertEquality("Emma", c.name);
    assertEquality(1234, c.id);

    ReadonlyNamedPerson|NonReadonlyNamedPerson d = c;

    var fn1 = function () {
        d.name = "May";
    };
    error? res = trap fn1();
    assertTrue(res is error);

    error err = <error> res;
    assertEquality(INVALID_UPDATE_REASON, err.message());
    assertEquality("cannot update 'final' field 'name' in object of type 'ReadonlyNamedPerson'",
                   err.detail()["message"]);
}

class Employee {
    final Details details;
    string department = "IT";

    function init(Details & readonly details) {
        self.details = details;
    }
}

type Details record {
    string name;
    int id;
};

function testObjectWithStructuredFinalFields() {
    Details & readonly details = {
        name: "Maryam",
        id: 1234
    };

    Employee emp = new (details);
    emp.department = "finance";

    assertEquality("Maryam", emp.details.name);
    assertEquality(1234, emp.details.id);
    assertEquality("finance", emp.department);

    Employee emp2 = new ({name: "Ziyad", id: 8910});

    assertEquality("Ziyad", emp2.details.name);
    assertEquality(8910, emp2.details.id);
    assertEquality("IT", emp2.department);

    var fn = function () {
        object { Details? details; } empVal = emp;
        empVal.details = {
            name: "Jo",
            id: 3456
        };
    };
    error? res = trap fn();
    assertTrue(res is error);

    error err = <error> res;
    assertEquality(INVALID_UPDATE_REASON, err.message());
    assertEquality("cannot update 'final' field 'details' in object of type 'Employee'", err.detail()["message"]);
}

class Identifier {
    final string id;
    string code;

    function init(string code, string? id = ()) {
        self.code = code;

        if id is string {
            self.id = id;
        } else {
            self.id = "Identifier";
        }
    }
}

function testFinalFieldWithDefaultValue() {
    string k = "id";

    Identifier i1 = new ("ABC", "new id");

    assertEquality("new id", i1.id);
    assertEquality("ABC", i1.code);

    Identifier i2 = new ("QWE");
    assertEquality("Identifier", i2.id);
    assertEquality("QWE", i2.code);

    var fn1 = function () {
        object { string id; } obj = i2;
        obj.id = "new identifier";
    };
    error? res = trap fn1();
    assertTrue(res is error);

    error err = <error> res;
    assertEquality(INVALID_UPDATE_REASON, err.message());
    assertEquality("cannot update 'final' field 'id' in object of type 'Identifier'", err.detail()["message"]);
}

type Foo object {
    string name;
    int id;

    function baz() returns string;
};

class Bar {
    final string name = "str";
    final int id = 1234;
    final int? oth = ();

    function baz() returns string {
        return string `${self.id}: ${self.name}`;
    }
}

function testTypeReadOnlyFlagForAllFinalFields() {
    Bar st = new;

    Foo & readonly pr = st;
    assertTrue(pr is Bar);
    assertEquality("str", pr.name);
    assertEquality(1234, pr.id);
    assertEquality("1234: str", pr.baz());

    readonly rd = st;
    assertTrue(rd is Bar);

    var ob = object {
        final map<int> & readonly config;

        function init() {
            self.config = {
                a: 1,
                b: 2
            };
        }
    };
    readonly rd2 = ob;
    assertTrue(rd2 is readonly & object {map<int> config;});
}

class Person {
    final readonly & Particulars particulars;
    int id;

    function init(Particulars & readonly particulars) {
        self.particulars = particulars;
        self.id = 1021;
    }
}

class Undergraduate {
    Particulars & readonly particulars;
    int id = 1234;

    function init(Particulars & readonly particulars) {
        self.particulars = particulars;
    }
}

class Graduate {
    Particulars particulars;
    int id;

    function init(Particulars particulars, int id) {
        self.particulars = particulars;
        self.id = id;
    }
}

type Particulars record {|
    string name;
|};

function testSubTypingWithFinalFields() {
    Person p1 = new ({name: "Jo"});
    Undergraduate u = p1;
    assertTrue(<any> u is Person);

    var fn1 = function () {
        u.particulars = {name: "May"};
    };
    error? res = trap fn1();
    assertTrue(res is error);
    error err = <error> res;
    assertEquality("cannot update 'final' field 'particulars' in object of type 'Person'", err.detail()["message"]);

    Person p2 = new ({name: "Amy"});
    Graduate g = p2;

    var fn2 = function () {
        u.particulars = {name: "May"};
    };
    res = trap fn2();
    assertTrue(res is error);
    err = <error> res;
    assertEquality("cannot update 'final' field 'particulars' in object of type 'Person'", err.detail()["message"]);

    assertTrue(g is Person);
    var fn3 = function () {
        g.particulars.name = "Anne";
    };
    res = trap fn3();
    assertTrue(res is error);
    err = <error> res;
    assertEquality("cannot update 'readonly' field 'name' in record of type '(Particulars & readonly)'",
                   err.detail()["message"]);

    Undergraduate u2 = new ({name: "Jo"});
    Person p3 = u2; // also valid since `final` fields don't affect subtyping.
    assertTrue(<any> p3 is Undergraduate);
}

type AbstractPerson object {
    Particulars particulars;
    int id;
};

readonly class ReadOnlyPerson {
    Particulars particulars;
    int id;

    function init() {
        self.particulars = {name: "Rob"};
        self.id = 1234;
    }
}

function testSubTypingWithFinalFieldsViaReadOnlyType() {
    var lrp = object {
        final Particulars & readonly particulars = {
            name: "Jo"
        };
        final int id = 1234;
    };

    AbstractPerson & readonly ap = lrp;
    Person p1 = ap;
    assertTrue(p1 is AbstractPerson & readonly);

    Person p2 = new ReadOnlyPerson();
    assertTrue(p2 is ReadOnlyPerson);
}

class StringOrIntIdUndergraduate {
    Particulars & readonly particulars;
    final int|string id;

    function init(Particulars & readonly particulars, string|int id) {
        self.particulars = particulars;
        self.id = id;
    }
}

function testSubTypingWithFinalFieldsNegative() {
    StringOrIntIdUndergraduate u = new ({name: "Jo"}, "string ID");
    assertFalse(u is Undergraduate);
}

const HUNDRED = 100;

type Baz object {
    HUNDRED i;
    float|string f;
    object {} a1;
    json a2;
    decimal? d;
    () ad;
    map<int>|boolean[] u;
    readonly r;
    Quuz q;
    int z;
};

class Qux {
    final string|int i;
    float f;
    final readonly & int|Quux a1;
    final readonly & any a2;
    final readonly & int[]? d;
    final readonly & anydata ad;
    final readonly & map<any> u;
    final readonly & int[] r;
    final readonly & anydata|object {} q;
    final readonly & json z = 1111;

    function init(string|int i, float f, readonly & int|Quux a1, readonly & anydata ad, map<any> & readonly u,
                  readonly & int[] r, readonly & anydata|object {} q, readonly & (int[]?) d = ()) {
        self.i = i;
        self.f = f;
        self.a1 = a1;
        self.a2 = "anydata value";
        self.d = d;
        self.ad = ad;
        self.u = u;
        self.r = r;
        self.q = q;
    }
}

type Quux object {
    map<string> m;

    function getMap() returns map<string>;
};

type Quuz record {|
    int i;
    float f;
|};

readonly class ReadonlyQuux {
    map<string> & readonly m;

    function init(map<string> & readonly m) {
        self.m = m;
    }

    function getMap() returns map<string> & readonly {
        return self.m;
    }
}

function testSubTypingWithReadOnlyFieldsPositiveComposite() {
    int[] & readonly arr = [1, 2];
    readonly & record {|json|xml...;|} rec = {
        "i": 123,
        "f": 988.42
    };

    Qux b = new (100, 12.0, new ReadonlyQuux({a: "hello", b: "world"}), (),  {a: 1, b: 2}, arr, rec);

    any a = b;
    assertTrue(a is Qux);
    assertTrue(a is Baz);

    Baz f = <Baz> a;
    assertEquality(100, f.i);
    assertEquality(12.0, f.f);
    assertTrue(f.a1 is ReadonlyQuux);
    ReadonlyQuux rq = <ReadonlyQuux> f.a1;
    assertEquality(<map<string>> {a: "hello", b: "world"}, rq.getMap());
    assertEquality("anydata value", f.a2);
    assertEquality((), f.d);
    assertEquality((), f.ad);
    assertTrue(f.u is map<int> & readonly);
    assertEquality(<map<int>> {a: 1, b: 2}, f.u);
    assertTrue(f.r is int[] & readonly);
    assertEquality(arr, f.r);
    assertEquality(rec, f.q);
    assertEquality(<Quuz> {i: 123, f: 988.42}, f.q);
    assertEquality(1111, f.z);
}

function testSubTypingWithReadOnlyFieldsNegativeComposite() {
    int[] & readonly arr = [1, 2];
    readonly & record {|json|xml...;|} rec = {
        "i": 123,
        "f": 988.42
    };

    Qux b1 = new (101, 12.0, new ReadonlyQuux({a: "hello", b: "world"}), (),  {a: 1, b: 2}, arr, rec);
    any a = b1; // doesn't match, invalid `i`, expected `HUNDRED`
    assertTrue(a is Qux);
    assertFalse(a is Baz);

    Qux b2 = new (100, 12.0, 1, (),  {a: 1, b: 2}, arr, rec);
    a = b2; // doesn't match, invalid `a1`, expected `object {}`
    assertTrue(a is Qux);
    assertFalse(a is Baz);

    Qux b3 = new (100, 12.0, new ReadonlyQuux({a: "hello", b: "world"}), [1, 2, 3],  {a: 1, b: 2}, arr, rec);
    a = b3; // doesn't match, invalid `ad`, expected `()`
    assertTrue(a is Qux);
    assertFalse(a is Baz);

    Qux b4 = new (100, 12.0, new ReadonlyQuux({a: "hello", b: "world"}), (),  {a: 1.0, b: 2}, arr, rec);
    a = b4; // doesn't match, invalid `u`, expected `map<int>|boolean[]?`
    assertTrue(a is Qux);
    assertFalse(a is Baz);

    Qux b5 = new (100, 12.0, new ReadonlyQuux({a: "hello", b: "world"}), (),  {a: 1, b: 2}, arr, [1, "str"]);
    a = b5; // doesn't match, invalid `q`, expected `Quuz`
    assertTrue(a is Qux);
    assertFalse(a is Baz);

    Qux b6 = new (100, 12.0, new ReadonlyQuux({a: "hello", b: "world"}), (),  {a: 1, b: 2}, arr, rec, [1, 2, 3]);
    a = b6; // doesn't match, invalid `d`, expected `decimal?`
    assertTrue(a is Qux);
    assertFalse(a is Baz);
}

type Controller object {
    map<int> config;

    function getConfig() returns map<json>;
};

class MyController {
    final map<int>|map<float> config;

    function init(map<int>|map<float> config) {
        self.config = config;
    }

    function getConfig() returns map<json> {
        return self.config;
    }
}

function testSubTypingWithMutableFinalFields() {
    map<int> c1 = {
        factor: 1,
        pressure: 2
    };
    MyController mc = new (c1);
    assertTrue(<any> mc is Controller);

    object {
        map<json> config;

        function getConfig() returns map<json>;
    } ob = object {
        final map<json> config;

        function init() {
            self.config = <map<int>> {
                factor: 10,
                pressure: 5
            };
        }

        function getConfig() returns map<json> {
            return self.config;
        }
    };
    assertTrue(ob is Controller);
}

function testSubTypingWithMutableFinalFieldsNegative() {
    map<float> c1 = {
        factor: 1.1,
        pressure: 2.3
    };
    MyController mc = new (c1);
    assertFalse(<any> mc is Controller);

    object {
        map<json> config;

        function getConfig() returns map<json>;
    } ob = object {
        final map<json> config;

        function init() {
            self.config = <map<json>> {
                factor: 10,
                pressure: 5
            };
        }

        function getConfig() returns map<json> {
            return self.config;
        }
    };
    assertFalse(ob is Controller);
}

function testUpdatingMutableFinalField() {
    map<int> c1 = {
        factor: 1,
        pressure: 2
    };
    MyController mc = new (c1);

    assertEquality(1, mc.config["factor"]);
    assertEquality(2, mc.config["pressure"]);

    mc.config["pressure"] = 3;

    assertEquality(1, mc.config["factor"]);
    assertEquality(3, mc.config["pressure"]);
}

const ASSERTION_ERROR_REASON = "AssertionError";

function assertTrue(any|error actual) {
    assertEquality(true, actual);
}

function assertFalse(any|error actual) {
    assertEquality(false, actual);
}

function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    panic error(ASSERTION_ERROR_REASON,
                message = "expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}
