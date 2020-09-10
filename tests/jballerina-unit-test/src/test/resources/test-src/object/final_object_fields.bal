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
