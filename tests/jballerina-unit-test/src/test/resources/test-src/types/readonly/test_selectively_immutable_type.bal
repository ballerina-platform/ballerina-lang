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

import ballerina/lang.'xml;

// readonly-type-descriptor := readonly
// A shape belongs to the type readonly if its read-only bit is on.
//
// A value belonging to an inherently immutable basic type will always have its read-only bit on. These basic types are:
//
// - all simple types
//   - nil
//   - boolean
//   - int
//   - float
//   - decimal
// - string
// - error
// - function
// - service
// - typedesc
// - handle
//
// A value belonging to a selectively immutable basic type may have its read-only bit on. These basic types are:
//
// - xml
// - list
// - mapping
// - table

function testReadonlyType() {
    testSimpleAssignmentForSelectivelyImmutableTypes();
    testRuntimeIsTypeForSelectivelyImmutableBasicTypes();
    testRuntimeIsTypeNegativeForSelectivelyImmutableTypes();
}

function testSimpleAssignmentForSelectivelyImmutableTypes() {
    testSimpleAssignmentForSelectivelyImmutableXmlTypes();
    testSimpleAssignmentForSelectivelyImmutableListTypes();
    testSimpleAssignmentForSelectivelyImmutableMappingTypes();
}

function testSimpleAssignmentForSelectivelyImmutableXmlTypes() {
    'xml:Comment & readonly a = xml `<!--I'm a comment-->`;
    readonly r1 = a;
    assertTrue(r1 is 'xml:Comment & readonly);
    assertEquality(<'xml:Comment> xml `<!--I'm a comment-->`, r1);

    'xml:ProcessingInstruction & readonly b = xml `<?pi a="1234" bc="def"?>`;
    readonly r2 = b;
    assertTrue(r2 is 'xml:ProcessingInstruction & readonly);
    assertEquality(b, r2);

    'xml:Element & readonly c = xml `<Student><name>Emma</name><id>6040</id></Student>`;
    readonly r3 = c;
    assertTrue(r3 is 'xml:Element & readonly);
    assertEquality(<'xml:Element> xml `<Student><name>Emma</name><id>6040</id></Student>`, r3);

    xml & readonly d = c;
    readonly r4 = d;
    assertTrue(r4 is xml & readonly);
    assertTrue(r4 is 'xml:Element & readonly);
    assertEquality(c, r4);
}

type Employee record {|
    Details details;
    string department;
|};

type Details record {|
    string name;
    int id;
|};

function testSimpleAssignmentForSelectivelyImmutableListTypes() {
    int[] & readonly a = [1, 2];
    readonly r1 = a;
    assertTrue(r1 is int[] & readonly);
    assertEquality(<int[]> [1, 2], r1);

    Employee & readonly emp = {
        details: {
            name: "Emma",
            id: 1234
        },
        department: "finance"
    };

    [Employee, Employee] & readonly b = [emp, {details: {name: "Jo", id: 5678}, department: "IT"}];
    readonly r2 = b;
    assertTrue(r2 is [Employee, Employee] & readonly);
    assertTrue(r2 is Employee[2] & readonly);

    [Employee, Employee] & readonly empTup = <[Employee, Employee] & readonly> r2;

    assertEquality(emp, empTup[0]);
    record {} rec = empTup[0];
    assertTrue(rec is Employee & readonly);
    assertTrue(rec.isReadOnly());

    rec = empTup[1];
    assertTrue(rec is Employee & readonly);
    assertTrue(rec.isReadOnly());
    assertEquality("IT", rec["department"]);
    assertTrue(rec["details"] is Details & readonly);
    assertTrue(rec["details"].isReadOnly());

    Details & readonly details = {
        name: "Jo",
        id: 9876
    };
    [Details[], Employee...] & readonly detEmpTup = [
                                                        [{name: "May", id: 1234}, details],
                                                        {details, department: "finance"}
                                                    ];
    readonly r3 = detEmpTup;
    assertTrue(r3 is [Details[], Employee...] & readonly);
    assertTrue(r3 is [[Details, Details], Employee] & readonly);

    [[Details, Details], Employee] & readonly vals = <[[Details, Details], Employee] & readonly> r3;
    assertTrue(vals[0].isReadOnly());

    Details d1 = vals[0][0];
    assertEquality(<Details> {name: "May", id: 1234}, d1);
    assertTrue(d1.isReadOnly());

    Details d2 = vals[0][1];
    assertEquality(details, d2);
    assertTrue(d2.isReadOnly());

    Employee e = vals[1];
    assertEquality(<Employee> {details, department: "finance"}, e);
    assertTrue(e.isReadOnly());
    assertTrue(e.details.isReadOnly());
}

function testSimpleAssignmentForSelectivelyImmutableMappingTypes() {
    boolean bool = false;

    map<boolean> & readonly a = {
        a: true,
        bool,
        c: false
    };
    readonly r1 = a;
    assertTrue(r1 is map<boolean> & readonly);
    assertEquality(<map<boolean>> {a: true, bool: false, c: false}, r1);

    Employee & readonly emp = {
        details: {
            name: "Emma",
            id: 1234
        },
        department: "finance"
    };

    readonly r2 = emp;
    assertTrue(r2 is Employee & readonly);

    assertEquality(emp, r2);
    any val = r2;
    Employee rec = <Employee> val;
    assertTrue(rec is Employee & readonly);
    assertTrue(rec.isReadOnly());

    Details det = rec.details;
    assertTrue(det is Details & readonly);
    assertTrue(det.isReadOnly());

    Student & readonly st = {
        details: {
            name: "Jo",
            id: 4567
        },
        "math": ["P", 75],
        "science": ["P", 65]
    };
    readonly r3 = st;
    assertTrue(r3 is Student & readonly);

    val = r3;
    Student stVal = <Student> val;
    assertTrue(stVal.isReadOnly());
    assertTrue(stVal.details.isReadOnly());
    assertEquality(<Details> {name: "Jo", id: 4567}, stVal.details);

    assertTrue(stVal["math"] is [RESULT, int] & readonly);
    assertTrue(stVal["math"].isReadOnly());
    assertEquality(<[RESULT, int]> ["P", 75], stVal["math"]);

    assertTrue(stVal["science"] is [RESULT, int] & readonly);
    assertTrue(stVal["science"].isReadOnly());
    assertEquality(<[RESULT, int]> ["P", 65], stVal["science"]);
}

type RESULT "P"|"F";

type Student record {|
    Details details;
    [RESULT, int]...;
|};

function testRuntimeIsTypeForSelectivelyImmutableBasicTypes() {
    xml a = xml `<foo><bar>Text</bar></foo>`;
    any b = a;
    any c = a.cloneReadOnly();
    assertFalse(b is readonly);
    assertTrue(c is readonly);

    int[] d = [1, 2];
    any e = d;
    any f = d.cloneReadOnly();
    assertFalse(e is readonly);
    assertTrue(f is readonly);

    [boolean, int] g = [true, 2];
    any h = g;
    any i = g.cloneReadOnly();
    assertFalse(h is readonly);
    assertTrue(i is readonly);

    map<string> j = {a: "a", b: "b"};
    any k = j;
    any l = j.cloneReadOnly();
    assertFalse(k is readonly);
    assertTrue(l is readonly);

    Employee m = {
        details: {
            name: "Maryam",
            id: 9876
        },
        department: "HR"
    };
    any n = m;
    any o = m.cloneReadOnly();
    assertFalse(n is readonly);
    assertTrue(o is readonly);

    // TODO: table.
}

function testRuntimeIsTypeNegativeForSelectivelyImmutableTypes() {
    int[] a = [1, 2];
    anydata a1 = a;
    any an1 = a;
    assertFalse(an1 is int[] & readonly);
    assertFalse(an1 is readonly);
    assertFalse(a1.isReadOnly());

    Employee emp = {
        details: {
            name: "Emma",
            id: 1234
        },
        department: "finance"
    };

    [Employee, Employee] b = [emp, {details: {name: "Jo", id: 5678}, department: "IT"}];
    anydata a2 = b;
    any an2 = b;
    assertFalse(an2 is [Employee, Employee] & readonly);
    assertFalse(an2 is readonly);
    assertFalse(a2.isReadOnly());

    [Employee, Employee] empTup = <[Employee, Employee]> a2;

    assertEquality(emp, empTup[0]);
    record {} rec = empTup[0];
    assertTrue(rec is Employee);
    assertFalse(rec is Employee & readonly);
    assertFalse(rec.isReadOnly());

    rec = empTup[1];
    assertTrue(rec is Employee);
    assertFalse(rec is Employee & readonly);
    assertFalse(rec.isReadOnly());
    assertTrue(rec["details"] is Details);
    assertFalse(rec["details"] is Details & readonly);
    assertFalse(rec["details"].isReadOnly());

    Details & readonly details = {
        name: "Jo",
        id: 9876
    };
    [Details[], Employee...] detEmpTup = [
                                            [{name: "May", id: 1234}, details],
                                            {details, department: "finance"}
                                         ];
    anydata a3 = detEmpTup;
    assertTrue(a3 is [Details[], Employee...]);
    assertFalse(a3 is [Details[], Employee...] & readonly);
    assertFalse(a3 is [[Details, Details], Employee] & readonly);

    [Details[], Employee] vals = <[Details[], Employee]> a3;
    assertFalse(vals[0].isReadOnly());

    Details d1 = vals[0][0];
    assertFalse(d1.isReadOnly());

    Details d2 = vals[0][1];
    assertEquality(details, d2);
    assertTrue(d2.isReadOnly());

    Employee e = vals[1];
    assertEquality(<Employee> {details, department: "finance"}, e);
    assertFalse(e.isReadOnly());
    assertTrue(e.details.isReadOnly());

    boolean aBool = true;
    map<boolean> f = {
        aBool,
        b: false
    };
    anydata a4 = f;
    any an4 = f;
    assertFalse(an4 is map<boolean> & readonly);
    assertFalse(an4 is readonly);
    assertFalse(a4.isReadOnly());

    json g = [1, {a: "abc", b: true}];
    anydata a5 = g;
    any an5 = g;
    assertTrue(an5 is json);
    assertFalse(an5 is json & readonly);
    assertFalse(an5 is json[] & readonly);
    assertFalse(an5 is readonly);
    assertFalse(a5.isReadOnly());

    json[] jsonVal = <json[]> an5;
    map<json> a8 = <map<json>> jsonVal[1];
    any an8 = a8;
    assertFalse(a8 is map<json> & readonly);
    assertFalse(an8 is readonly);
    assertFalse(a8.isReadOnly());

    map<int>|boolean[] h = [true, false];
    anydata a6 = h;
    any an6 = h;
    assertTrue(an6 is boolean[]);
    assertTrue(an6 is map<int>|boolean[]);
    assertFalse(an6 is map<int>|boolean[] & readonly);
    assertFalse(an6 is boolean[] & readonly);
    assertFalse(an6 is readonly);
    assertFalse(a6.isReadOnly());

    'xml:Element i = xml `<Student><name>Emma</name><id>6040</id></Student>`;
    anydata a7 = i;
    any an7 = i;
    assertTrue(an7 is 'xml:Element);
    assertFalse(an7 is 'xml:Element & readonly);
    assertFalse(an7 is readonly);
    assertFalse(a7.isReadOnly());
}

type AssertionError error<ASSERTION_ERROR_REASON>;

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

    panic AssertionError(message = "expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}
