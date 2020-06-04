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
import testorg/selectively_immutable as se;

function testImmutableTypes() {
    testXmlComment();
    testXmlPI();
    testXmlElements();
    testImmutableLists();
    testImmutableMappings();
}

function testXmlComment() {
    ['xml:Comment, 'xml:Comment] [a, b] = se:getXmlComments();

    assertTrue(a is 'xml:Comment & readonly);
    assertTrue(a.isReadOnly());
    readonly r1 = <readonly> <any> a;
    assertTrue(r1 is 'xml:Comment & readonly);
    assertEquality(<'xml:Comment> xml `<!--I'm a comment-->`, r1);

    assertFalse(b.isReadOnly());
    assertFalse(b is 'xml:Comment & readonly);
    assertEquality(<'xml:Comment> xml `<!--I'm another comment-->`, b);
}

function testXmlPI() {
    ['xml:ProcessingInstruction, 'xml:ProcessingInstruction] [a, b] = se:getXmlPIs();

    assertTrue(a is 'xml:ProcessingInstruction & readonly);
    assertTrue(a.isReadOnly());
    readonly r1 = <readonly> <any> a;
    assertTrue(r1 is 'xml:ProcessingInstruction & readonly);
    assertEquality(<'xml:ProcessingInstruction> xml `<?pi a="1234" bc="def"?>`, r1);

    assertFalse(b.isReadOnly());
    assertFalse(b is 'xml:ProcessingInstruction & readonly);
    assertEquality(<'xml:ProcessingInstruction> xml `<?pi a="4567" bc="def"?>`, b);
}

function testXmlElements() {
    ['xml:Element, 'xml:Element] [a, b] = se:getXmlElements();

    assertTrue(a is 'xml:Element & readonly);
    assertTrue(a.isReadOnly());
    readonly r1 = <readonly> <any> a;
    assertTrue(r1 is 'xml:Element & readonly);
    assertEquality(<'xml:Element> xml `<Student><name>Emma</name><id>6040</id></Student>`, r1);

    assertFalse(b.isReadOnly());
    assertFalse(b is 'xml:Element & readonly);
    assertEquality(<'xml:Element> xml `<Student><name>Jo</name><id>6040</id></Student>`, b);
}

function testImmutableLists() {
    [any, any, any, any] [a, b, c, d] = se:getSelectivelyImmutableListTypes();

    assertTrue(a is int[] & readonly);
    assertTrue(a is int[2] & readonly);
    anydata ad = <anydata> a;
    assertTrue(ad.isReadOnly());
    readonly r1 = <readonly> a;
    assertTrue(r1 is int[] & readonly);
    assertEquality(<int[]> [1, 2], r1);

    ad = <anydata> b;
    assertFalse(ad.isReadOnly());
    assertTrue(b is int[]);
    assertFalse(b is int[] & readonly);
    assertEquality(<int[]> [1, 2], b);

    se:Employee & readonly emp = {
        details: {
            name: "Emma",
            id: 1234
        },
        department: "finance"
    };

    assertTrue(c is [se:Employee, se:Employee]  & readonly);
    assertTrue(c is se:Employee[2]  & readonly);
    ad = <anydata> c;
    assertTrue(ad.isReadOnly());
    readonly r2 = <readonly> c;
    assertTrue(r2 is se:Employee[] & readonly);
    assertEquality(<se:Employee[]> [emp, {details: {name: "Jo", id: 5678}, department: "IT"}], r2);

    ad = <anydata> d;
    assertFalse(ad.isReadOnly());
    assertTrue(d is [se:Employee, se:Employee]);
    assertFalse(d is [se:Employee, se:Employee] & readonly);
    assertEquality(<se:Employee[]> [{details: {name: "Amy", id: 1234}, department: "IT"},
                                      {details: {name: "Jo", id: 5678}, department: "IT"}], d);
}

function testImmutableMappings() {
    [any, any, any, any, any] [a, b, c, d, e] = se:getSelectivelyImmutableMappingTypes();

    assertTrue(a is map<boolean> & readonly);
    readonly r1 = <readonly> a;
    anydata ad = <anydata> a;
    assertTrue(ad.isReadOnly());
    assertTrue(a is map<boolean>);
    assertEquality(<map<boolean>> {a: true, bool: false, c: false}, r1);

    ad = <anydata> b;
    assertFalse(ad.isReadOnly());
    assertTrue(b is map<int>);
    assertFalse(b is map<int> & readonly);
    assertEquality(<map<int>> {a: 1, b: 2}, b);

    se:Employee & readonly emp = {
        details: {
            name: "Emma",
            id: 1234
        },
        department: "finance"
    };

    assertTrue(c is se:Employee & readonly);
    readonly r2 = <readonly> c;

    assertEquality(emp, r2);
    any val = r2;
    se:Employee rec = <se:Employee> val;
    assertTrue(rec is se:Employee & readonly);
    assertTrue(rec.isReadOnly());

    se:Details det = rec.details;
    assertTrue(det is se:Details & readonly);
    assertTrue(det.isReadOnly());

    se:Employee & readonly emp2 = {
        details: {
            name: "Mary",
            id: 4567
        },
        department: "IT"
    };

    assertFalse(d is se:Employee & readonly);
    assertTrue(d is se:Employee);

    assertEquality(emp2, d);
    val = d;
    rec = <se:Employee> val;
    assertFalse(rec is se:Employee & readonly);
    assertFalse(rec.isReadOnly());

    det = rec.details;
    assertFalse(det is se:Details & readonly);
    assertFalse(det.isReadOnly());

    assertTrue(e is se:Student & readonly);
    readonly r3 = <readonly> e;
    assertTrue(r3 is se:Student & readonly);

    val = r3;
    se:Student stVal = <se:Student> val;
    assertTrue(stVal.isReadOnly());
    assertTrue(stVal.details.isReadOnly());
    assertEquality(<se:Details> {name: "Jo", id: 4567}, stVal.details);

    assertTrue(stVal["math"] is [se:RESULT, int] & readonly);
    assertTrue(stVal["math"].isReadOnly());
    assertEquality(<[se:RESULT, int]> ["P", 75], stVal["math"]);

    assertTrue(stVal["science"] is [se:RESULT, int] & readonly);
    assertTrue(stVal["science"].isReadOnly());
    assertEquality(<[se:RESULT, int]> ["P", 65], stVal["science"]);
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

