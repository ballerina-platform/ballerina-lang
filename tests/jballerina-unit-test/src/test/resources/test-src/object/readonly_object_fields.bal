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

function testReadonlyObjectFields() {
    testObjectWithSimpleReadonlyFields();
    testInvalidObjectSimpleReadonlyFieldUpdate();
    testValidUpdateOfPossiblyReadonlyFieldInUnion();
    testInvalidUpdateOfPossiblyReadonlyFieldInUnion();
    testObjectWithStructuredReadonlyFields();
    testReadOnlyFieldWithDefaultValue();
    testTypeReadOnlyFlagForAllReadOnlyFields();
    testSubTypingWithReadOnlyFields();
    testSubTypingWithReadOnlyFieldsViaReadOnlyType();
    testSubTypingWithReadOnlyFieldsNegative();
}

public type Student object {
    readonly string name;
    readonly int id;
    float avg = 80.0;
    public function init(string n, int i) {
        self.name = n;
        self.id = i;
    }
};

public type NonReadOnlyStudent object {
    string name;
    int id;
    int yob;

    public function init(string n, int i, int y) {
        self.name = n;
        self.id = i;
        self.yob = y;
    }
};

function testObjectWithSimpleReadonlyFields() {
    Student|NonReadOnlyStudent st = new ("Maryam", 1234);
    assertEquality("Maryam", st.name);
    assertEquality(1234, st.id);
}

function testInvalidObjectSimpleReadonlyFieldUpdate() {
    Student st1 = new("Maryam", 1234);

    // Invalid updates.
    var fn1 = function () {
        object { string name = "default"; } rec = st1;
        rec.name = "Jo";
    };
    error? res = trap fn1();
    assertTrue(res is error);

    error err = <error> res;
    assertEquality(INHERENT_TYPE_VIOLATION_REASON, err.message());
    assertEquality("cannot update 'readonly' field 'name' in object of type 'Student'", err.detail()["message"]);
}

type ReadonlyNamedPerson object {
    readonly string name;
    int id;

    function init(string name, int id) {
        self.name = name;
        self.id = id;
    }
};

type NonReadonlyNamedPerson object {
    string name;
    int id;

    function init(string name, int id) {
        self.name = name;
        self.id = id;
    }
};

function testValidUpdateOfPossiblyReadonlyFieldInUnion() {
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

function testInvalidUpdateOfPossiblyReadonlyFieldInUnion() {
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
    assertEquality(INHERENT_TYPE_VIOLATION_REASON, err.message());
    assertEquality("cannot update 'readonly' field 'name' in object of type 'ReadonlyNamedPerson'",
                   err.detail()["message"]);
}

type Employee object {
    readonly Details details;
    string department = "IT";

    function init(Details & readonly details) {
        self.details = details;
    }
};

type Details record {
    string name;
    int id;
};

function testObjectWithStructuredReadonlyFields() {
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
        object { Details? details = (); } empVal = emp;
        empVal.details = {
            name: "Jo",
            id: 3456
        };
    };
    error? res = trap fn();
    assertTrue(res is error);

    error err = <error> res;
    assertEquality(INHERENT_TYPE_VIOLATION_REASON, err.message());
    assertEquality("cannot update 'readonly' field 'details' in object of type 'Employee'", err.detail()["message"]);
}

type Identifier object {
    readonly string id = "Identifier";
    string code;

    function init(string code, string? id = ()) {
        self.code = code;

        if id is string {
            self.id = id;
        }
    }
};

function testReadOnlyFieldWithDefaultValue() {
    string k = "id";

    Identifier i1 = new ("ABC", "new id");

    assertEquality("new id", i1.id);
    assertEquality("ABC", i1.code);

    Identifier i2 = new ("QWE");
    assertEquality("Identifier", i2.id);
    assertEquality("QWE", i2.code);

    var fn1 = function () {
        object { string id = "default"; } obj = i2;
        obj.id = "new identifier";
    };
    error? res = trap fn1();
    assertTrue(res is error);

    error err = <error> res;
    assertEquality(INHERENT_TYPE_VIOLATION_REASON, err.message());
    assertEquality("cannot update 'readonly' field 'id' in object of type 'Identifier'", err.detail()["message"]);
}

type Foo abstract object {
    string name;
    int id;

    function baz() returns string;
};

type Bar object {
    readonly string name = "str";
    readonly int id = 1234;
    readonly int? oth = ();

    function baz() returns string {
        return string `${self.id}: ${self.name}`;
    }
};

function testTypeReadOnlyFlagForAllReadOnlyFields() {
    Bar st = new;

    Foo & readonly pr = st;
    assertTrue(pr is Bar);
    assertEquality("str", pr.name);
    assertEquality(1234, pr.id);
    assertEquality("1234: str", pr.baz());

    readonly rd = st;
    assertTrue(rd is Bar);
}

type Person object {
    readonly Particulars particulars;
    int id;

    function init(Particulars & readonly particulars) {
        self.particulars = particulars;
        self.id = 1021;
    }
};

type Undergraduate object {
    Particulars & readonly particulars;
    int id = 1234;

    function init(Particulars & readonly particulars) {
        self.particulars = particulars;
    }
};

type Graduate object {
    Particulars particulars;
    int id;

    function init(Particulars particulars, int id) {
        self.particulars = particulars;
        self.id = id;
    }
};

type Particulars record {|
    string name;
|};

function testSubTypingWithReadOnlyFields() {
    Person p1 = new ({name: "Jo"});
    Undergraduate u = p1;
    assertTrue(u is Person);

    var fn1 = function () {
        u.particulars = {name: "May"};
    };
    error? res = trap fn1();
    assertTrue(res is error);
    error err = <error> res;
    assertEquality("cannot update 'readonly' field 'particulars' in object of type 'Person'", err.detail()["message"]);

    Person p2 = new ({name: "Amy"});
    Graduate g = p2;

    var fn2 = function () {
        u.particulars = {name: "May"};
    };
    res = trap fn2();
    assertTrue(res is error);
    err = <error> res;
    assertEquality("cannot update 'readonly' field 'particulars' in object of type 'Person'", err.detail()["message"]);

    assertTrue(g is Person);
    var fn3 = function () {
        g.particulars.name = "Anne";
    };
    res = trap fn3();
    assertTrue(res is error);
    err = <error> res;
    assertEquality("cannot update 'readonly' field 'name' in record of type '(Particulars & readonly)'",
                   err.detail()["message"]);
}

type AbstractPerson abstract object {
    Particulars particulars;
    int id;
};

type ReadOnlyPerson readonly object {
    Particulars particulars;
    int id;

    function init() {
        self.particulars = {name: "Rob"};
        self.id = 1234;
    }
};

function testSubTypingWithReadOnlyFieldsViaReadOnlyType() {
    object {
        readonly Particulars particulars = {
            name: "Jo"
        };
        readonly int id = 1234;
    } lrp = new ();

    AbstractPerson & readonly ap = lrp;
    Person p1 = ap;
    assertTrue(p1 is AbstractPerson & readonly);

    Person p2 = new ReadOnlyPerson();
    assertTrue(p2 is ReadOnlyPerson);
}

function testSubTypingWithReadOnlyFieldsNegative() {
    Undergraduate u = new ({name: "Jo"});
    any undergrad = u;

    Graduate g = new ({name: "Amy"}, 1121);
    any grad = g;

    assertTrue(undergrad is Undergraduate);
    assertTrue(grad is Graduate);
    assertFalse(undergrad is Person);
    assertFalse(grad is Person);
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
