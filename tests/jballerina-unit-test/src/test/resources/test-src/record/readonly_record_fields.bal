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

const INHERENT_TYPE_VIOLATION_REASON = "{ballerina/lang.map}InherentTypeViolation";

function testReadonlyRecordFields() {
    testRecordWithSimpleReadonlyFields();
    testInvalidRecordSimpleReadonlyFieldUpdate();
    testValidUpdateOfPossiblyReadonlyFieldInUnion();
    testInvalidUpdateOfPossiblyReadonlyFieldInUnion();
    testRecordWithStructuredReadonlyFields();
    testReadOnlyFieldWithDefaultValue();
    testTypeReadOnlyFlagForAllReadOnlyFields();
    testSubTypingWithReadOnlyFields();
    testSubTypingWithReadOnlyFieldsViaReadOnlyType();
    testSubTypingWithReadOnlyFieldsNegative();
}

type Student record {
    readonly string name;
    readonly int id?;
};

function testRecordWithSimpleReadonlyFields() {
    Student st = {
        name: "Maryam"
    };
    assertEquality("Maryam", st.name);
    assertEquality((), st?.id);
}

function testInvalidRecordSimpleReadonlyFieldUpdate() {
    Student st1 = {
        name: "Maryam"
    };

    // Invalid updates.
    var fn1 = function () {
        record {} rec = st1;
        string str = "name";
        rec[str] = "Jo";
    };
    error? res = trap fn1();
    assertTrue(res is error);

    error err = <error> res;
    assertEquality(INHERENT_TYPE_VIOLATION_REASON, err.message());
    assertEquality("cannot update 'readonly' field 'name' in record of type 'Student'", err.detail()["message"]);

    var fn2 = function () {
        record {} rec = st1;
        rec["id"] = 4567;
    };
    res = trap fn2();
    assertTrue(res is error);

    err = <error> res;
    assertEquality(INHERENT_TYPE_VIOLATION_REASON, err.message());
    assertEquality("cannot update 'readonly' field 'id' in record of type 'Student'", err.detail()["message"]);
}

type Employee record {
    readonly Details details;
    string department;
};

type ReadonlyName record {
    readonly string name;
};

type Details record {
    string name;
    int id;
};

function testValidUpdateOfPossiblyReadonlyFieldInUnion() {
    Details d = {
        name: "Jo",
        id: 1234
    };

    Student|Details sd = d;
    sd.name = "May";
    sd.id = 4567;

    assertEquality("May", sd.name);
    assertEquality(4567, sd?.id);

    ReadonlyName|Details rnd = d;
    rnd.name = "Sue";
    rnd["id"] = 2525;

    assertEquality("Sue", rnd.name);
    assertEquality(2525, rnd?.id);

}

function testInvalidUpdateOfPossiblyReadonlyFieldInUnion() {
    Student s = {
        name: "Jo",
        id: 1234
    };

    Student|Details sd = s;

    var fn1 = function () {
        sd["name"] = "May";
    };
    error? res = trap fn1();
    assertTrue(res is error);

    error err = <error> res;
    assertEquality(INHERENT_TYPE_VIOLATION_REASON, err.message());
    assertEquality("cannot update 'readonly' field 'name' in record of type 'Student'", err.detail()["message"]);

    var fn2 = function () {
        sd.id = 4567;
    };
    res = trap fn2();
    assertTrue(res is error);

    err = <error> res;
    assertEquality(INHERENT_TYPE_VIOLATION_REASON, err.message());
    assertEquality("cannot update 'readonly' field 'id' in record of type 'Student'", err.detail()["message"]);
}

function testRecordWithStructuredReadonlyFields() {
    Details & readonly details = {
        name: "Maryam",
        id: 1234
    };

    Employee emp = {
        details,
        department: "finance"
    };

    assertEquality("Maryam", emp.details.name);
    assertEquality(1234, emp.details.id);
    assertEquality("finance", emp.department);

    Employee emp2 = {
        details: {
            name: "Ziyad",
            id: 8910
        },
        department: "IT"
    };

    assertEquality("Ziyad", emp2.details.name);
    assertEquality(8910, emp2.details.id);
    assertEquality("IT", emp2.department);

    var fn = function () {
        record {} empVal = emp;
        empVal["details"] = {
            name: "Jo",
            id: 3456
        };
    };
    error? res = trap fn();
    assertTrue(res is error);

    error err = <error> res;
    assertEquality(INHERENT_TYPE_VIOLATION_REASON, err.message());
    assertEquality("cannot update 'readonly' field 'details' in record of type 'Employee'", err.detail()["message"]);
}

type Identifier record {|
    readonly string id = "Identifier";
    string code;
|};

function testReadOnlyFieldWithDefaultValue() {
    string k = "id";

    Identifier i1 = {code: "QWE", id: "new id"};

    assertEquality("new id", i1.id);
    assertEquality("QWE", i1.code);

    Identifier i2 = {code: "QWE"};
    assertEquality("Identifier", i2.id);
    assertEquality("QWE", i2.code);

    var fn1 = function () {
        i2[k] = "new identifier";
    };
    error? res = trap fn1();
    assertTrue(res is error);

    error err = <error> res;
    assertEquality(INHERENT_TYPE_VIOLATION_REASON, err.message());
    assertEquality("cannot update 'readonly' field 'id' in record of type 'Identifier'", err.detail()["message"]);
}

type Foo record {|
    string name;
    int id;
    float...;
|};

type Bar record {|
    readonly string name;
    readonly int id;
|};

function testTypeReadOnlyFlagForAllReadOnlyFields() {
    Bar st = {
        name: "Maryam",
        id: 1234
    };

    Foo & readonly pr = st;
    assertTrue(pr is Bar);
    assertTrue(pr is Bar & readonly);
    assertEquality("Maryam", pr.name);
    assertEquality(1234, pr.id);

    readonly rd = st;
    assertTrue(rd is Bar);
    assertTrue(rd is Bar & readonly);
}

type Person record {|
    readonly Particulars particulars;
    int id;
|};

type Undergraduate record {|
    Particulars & readonly particulars;
    int id;
|};

type Graduate record {|
    Particulars particulars;
    int id;
|};

type Particulars record {|
    string name;
|};

type OptionalId record {|
    readonly map<int>|boolean id?;
    map<int>|boolean...;
|};

function testSubTypingWithReadOnlyFields() {
    Person p1 = {
        particulars: {
            name: "Jo"
        },
        id: 1234
    };
    Undergraduate u = p1;
    assertTrue(u is Person);

    var fn1 = function () {
        u.particulars = {name: "May"};
    };
    error? res = trap fn1();
    assertTrue(res is error);
    error err = <error> res;
    assertEquality("cannot update 'readonly' field 'particulars' in record of type 'Person'", err.detail()["message"]);

    Person p2 = {
        particulars: {
          name: "Amy"
        },
        id: 1121
    };
    Graduate g = p2;

    var fn2 = function () {
        u.particulars = {name: "May"};
    };
    res = trap fn2();
    assertTrue(res is error);
    err = <error> res;
    assertEquality("cannot update 'readonly' field 'particulars' in record of type 'Person'", err.detail()["message"]);

    assertTrue(g is Person);
    var fn3 = function () {
        g.particulars.name = "Anne";
    };
    res = trap fn3();
    assertTrue(res is error);
    err = <error> res;
    assertEquality("cannot update 'readonly' field 'name' in record of type '(Particulars & readonly)'",
                   err.detail()["message"]);

    map<map<int>|boolean> & readonly mp = {
        a: true,
        b: {
            x: 1,
            y: 2
        }
    };
    OptionalId opId = mp;
    assertEquality((), opId?.id);
    assertEquality(<map<int>> {x: 1, y: 2}, opId["b"]);
}

function testSubTypingWithReadOnlyFieldsViaReadOnlyType() {
    Undergraduate & readonly u = {
        particulars: {
            name: "Jo"
        },
        id: 1234
    };
    Person p1 = u;
    assertTrue(p1 is Undergraduate & readonly);

    Graduate & readonly g = {
        particulars: {
          name: "Amy"
        },
        id: 1121
    };
    Person p2 = g;
    assertTrue(p2 is Graduate & readonly);
}

function testSubTypingWithReadOnlyFieldsNegative() {
    Undergraduate u = {
        particulars: {
            name: "Jo"
        },
        id: 1234
    };
    any undergrad = u;

    Graduate g = {
        particulars: {
          name: "Amy"
        },
        id: 1121
    };
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
