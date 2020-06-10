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

    st.id = 1234; // Valid since first update.
    assertEquality("Maryam", st.name);
    assertEquality(1234, st?.id);
}

function testInvalidRecordSimpleReadonlyFieldUpdate() {
    Student st1 = {
        name: "Maryam"
    };
    st1.id = 1234; // Valid since first update.

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
        st1.id = 4567;
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

const ASSERTION_ERROR_REASON = "AssertionError";

function assertTrue(any|error actual) {
    assertEquality(true, actual);
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
