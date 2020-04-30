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
}

function testSimpleAssignmentForSelectivelyImmutableTypes() {
    testSimpleAssignmentForSelectivelyImmutableXmlTypes();
    //testSimpleAssignmentForSelectivelyImmutableListTypes();
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

    Employee[] & readonly b = [emp, {details: {name: "Jo", id: 5678}, department: "IT"}];
    readonly r2 = b;
    assertTrue(r2 is int[] & readonly);

    Employee[] & readonly empArr = <Employee[] & readonly> r2;
    assertEquality(2, empArr.length());

    assertEquality(emp, empArr[0]);
    record {} rec = empArr[0];
    assertTrue(rec is Employee & readonly);
    assertTrue(rec.isReadOnly());

    rec = empArr[1];
    assertTrue(rec is Employee & readonly);
    assertTrue(rec.isReadOnly());
    assertEquality("IT", rec["department"]);
    assertTrue(rec["details"] is Details & readonly);
    assertTrue(rec["details"].isReadOnly());
}

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
