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
import testorg/records;
import testorg/selectively_immutable as se;

function testImmutableTypes() {
    testXmlComment();
    testXmlPI();
    testXmlElements();
    testImmutableLists();
    testImmutableMappings();
    testMutability();
    testSameNamedConstructFromDifferentModules();
    testDefaultValuesOfFields();
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
}

function testMutability() {
    record {| any...; |} rec = se:getMixedRecord();

    any a = rec["a"];
    assertTrue(a is 'xml:Comment);
    assertTrue(a is 'xml:Comment & readonly);
    readonly r1 = <readonly> a;
    anydata ad1 = <anydata> a;
    assertTrue(ad1.isReadOnly());
    assertEquality(xml `<!--Comment 1-->`, ad1);

    any b = rec["b"];
    assertTrue(b is 'xml:Comment);
    assertFalse(b is 'xml:Comment & readonly);
    anydata ad2 = <anydata> b;
    assertFalse(ad2.isReadOnly());
    assertEquality(xml `<!--Comment 2-->`, ad2);

    any c = rec["c"];
    assertTrue(c is 'xml:ProcessingInstruction);
    assertTrue(c is 'xml:ProcessingInstruction & readonly);
    readonly r3 = <readonly> c;
    anydata ad3 = <anydata> c;
    assertTrue(ad3.isReadOnly());
    assertEquality(xml `<?pi a="1234" bc="def"?>`, ad3);

    any d = rec["d"];
    assertTrue(d is 'xml:ProcessingInstruction);
    assertFalse(d is 'xml:ProcessingInstruction & readonly);
    anydata ad4 = <anydata> d;
    assertFalse(ad4.isReadOnly());
    assertEquality(xml `<?pi b="342" a="qwe"?>`, ad4);

    any e = rec["e"];
    assertTrue(e is 'xml:Element);
    assertTrue(e is 'xml:Element & readonly);
    readonly r5 = <readonly> e;
    anydata ad5 = <anydata> e;
    assertTrue(ad5.isReadOnly());
    assertEquality(xml `<Student><name>Emma</name><id>6040</id></Student>`, ad5);

    any f = rec["f"];
    assertTrue(f is 'xml:Element);
    assertFalse(f is 'xml:Element & readonly);
    anydata ad6 = <anydata> f;
    assertFalse(ad6.isReadOnly());
    assertEquality(xml `<Student><name>Jo</name><id>1234</id></Student>`, ad6);

    any g = rec["g"];
    assertTrue(g is 'xml:Text);
    assertTrue(g is xml);
    assertTrue(g is 'xml:Text & readonly);
    assertTrue(g is xml & readonly);
    readonly r7 = <readonly> g;
    anydata ad7 = <anydata> g;
    assertTrue(ad5.isReadOnly());
    assertEquality(xml `Text value`, ad7);

    any h = rec["h"];
    assertTrue(h is 'xml:Element);
    assertTrue(h is xml);
    assertFalse(h is 'xml:Element & readonly);
    assertFalse(h is xml & readonly);
    anydata ad8 = <anydata> h;
    assertFalse(ad8.isReadOnly());
    assertEquality(xml `<name>Jo</name>`, ad8);

    any i = rec["i"];
    assertTrue(i is json[]);
    assertTrue(i is json[] & readonly);
    readonly r9 = <readonly> i;
    anydata ad9 = <anydata> i;
    assertTrue(ad9.isReadOnly());
    assertEquality(<json[]> [{a: 1, b: "str"}, true], ad9);
    json[] jsonArr = <json[]> i;
    assertTrue(jsonArr[0].isReadOnly());
    assertTrue(jsonArr[0] is map<json> & readonly);

    any j = rec["j"];
    assertTrue(j is int[]);
    assertFalse(j is int[] & readonly);
    anydata ad10 = <anydata> j;
    assertFalse(ad10.isReadOnly());
    assertEquality(<int[]> [1, 2, 3], ad10);

    any k = rec["k"];
    assertTrue(k is [se:Identifier, int]);
    assertTrue(k is [se:Identifier, int] & readonly);
    readonly r11 = <readonly> k;
    anydata ad11 = <anydata> k;
    assertTrue(ad11.isReadOnly());
    assertEquality(<[se:Identifier, int]> [{name: "Maryam", id: 1234}, 20], ad11);
    [se:Identifier, int] idTuple = <[se:Identifier, int]> k;
    assertTrue(idTuple[0] is se:Identifier & readonly);

    any l = rec["l"];
    assertTrue(l is [string, float]);
    assertFalse(l is [string, float] & readonly);
    anydata ad12 = <anydata> l;
    assertFalse(ad12.isReadOnly());
    assertEquality(<[string, float]> ["Brad", 20], ad12);

    any m = rec["m"];
    assertTrue(m is se:Student);
    assertTrue(m is se:Student & readonly);
    readonly r13 = <readonly> m;
    anydata ad13 = <anydata> m;
    assertTrue(ad13.isReadOnly());
    assertEquality(<se:Student> {details: {name: "Amy", id: 1234}, "math": ["P", 80]}, ad13);
    se:Student student = <se:Student> m;
    assertTrue(student.details is se:Details & readonly);
    assertTrue(student.details.isReadOnly());

    any n = rec["n"];
    assertTrue(n is se:Details);
    assertFalse(n is se:Details & readonly);
    anydata ad14 = <anydata> n;
    assertFalse(ad14.isReadOnly());
    assertEquality(<se:Details> {name: "Kim", id: 234789}, ad14);

    any o = rec["o"];
    assertTrue(o is map<string>);
    assertTrue(o is map<string> & readonly);
    readonly r15 = <readonly> o;
    anydata ad15 = <anydata> o;
    assertTrue(ad15.isReadOnly());
    assertEquality(<map<string>> {a: "123", b: "234", "c": "name"}, ad15);

    any p = rec["p"];
    assertTrue(p is map<json>);
    assertFalse(p is map<json> & readonly);
    anydata ad16 = <anydata> p;
    assertFalse(ad16.isReadOnly());
    assertEquality(<map<json>> {a: 1, b: true, c: "name"}, ad16);

    any q = rec["q"];
    assertTrue(q is table<se:Identifier> key(name));
    assertTrue(q is table<se:Identifier> key(name) & readonly);
    readonly r17 = <readonly> q;
    anydata ad17 = <anydata> q;
    assertTrue(ad17.isReadOnly());
    table<se:Identifier> key(name) qVal = <table<se:Identifier> key(name)> ad17;
    assertEquality(3, qVal.length());

    any r = rec["r"];
    assertTrue(r is table<map<string>>);
    assertFalse(r is table<map<string>> & readonly);
    anydata ad18 = <anydata> r;
    assertFalse(ad18.isReadOnly());
    table<map<string>> rVal = <table<map<string>>> ad18;
    assertEquality(2, rVal.length());

    any s = rec["s"];
    assertTrue(s is se:OwnerA);
    assertTrue(s is se:Owner & readonly);
    se:Owner ownerA = <se:Owner> s;
    assertEquality(222, ownerA.getId());

    any t = rec["t"];
    assertTrue(t is se:OwnerB);
    assertFalse(t is se:Owner & readonly);
    se:Owner ownerB = <se:OwnerB> t;
    assertEquality(2468, ownerB.getId());
}

function testSameNamedConstructFromDifferentModules() {
    se:Employee & readonly seEmp = {
        details: {
            name: "Jo",
            id: 1234
        },
        department: "IT"
    };

    records:Employee & readonly recEmp = {
        details: {
            name: "May",
            yob: 1990
        },
        id: 2314
    };

    readonly r1 = seEmp;
    readonly r2 = recEmp;

    record {} rec1 = seEmp;
    record {} rec2 = recEmp;

    assertTrue(rec1 is se:Employee);
    assertTrue(rec1 is se:Employee & readonly);
    assertTrue(rec1.isReadOnly());
    assertTrue(rec1["details"] is se:Details);
    assertTrue(rec1["details"] is se:Details & readonly);
    assertTrue(rec2.isReadOnly());

    se:Employee e1 = <se:Employee> rec1;
    assertEquality("Jo", e1.details.name);
    assertEquality(1234, e1.details.id);
    assertEquality("IT", e1.department);

    assertTrue(rec2 is records:Employee);
    assertTrue(rec2 is records:Employee & readonly);
    assertTrue(rec2.isReadOnly());
    assertTrue(rec2["details"] is records:Details);
    assertTrue(rec2["details"] is records:Details & readonly);
    assertTrue(rec2.isReadOnly());

    records:Employee e2 = <records:Employee> rec2;
    assertEquality("May", e2.details.name);
    assertEquality(1990, e2.details.yob);
    assertEquality(2314, e2.id);
}

type IdentifierRec record {
    string id = "record";
};

type IdentifierAbstractObj object {
    function getId() returns string;
};

class IdentifierObj {
    final string id;

    function init() {
        self.id = "object";
    }

    function getId() returns string {
        return self.id;
    }
}

type ConfigRec record {
    se:Versioning versioning;
    records:Quota quota;
    IdentifierRec rec?;
    IdentifierAbstractObj obj;
};

function testDefaultValuesOfFields() {
    ConfigRec & readonly cr = {versioning: {}, quota: {initial: 5}, rec: {}, obj: new IdentifierObj()};

    any a = cr;
    assertTrue(a is ConfigRec);
    assertTrue(a is ConfigRec & readonly);

    ConfigRec cr2 = cr;

    assertTrue(cr2.versioning is se:Versioning & readonly);
    assertTrue(cr2.versioning.isReadOnly());
    assertEquality(<se:Versioning> {pattern: "v{major}.{minor}", allow: true, matchMajor: false}, cr2.versioning);

    assertTrue(cr2.quota is records:Quota & readonly);
    assertTrue(cr2.quota.isReadOnly());
    assertEquality(<records:Quota> {initial: 5, factor: 2.0}, cr2.quota);

    assertTrue(cr2?.rec is IdentifierRec & readonly);
    assertTrue(cr2?.rec.isReadOnly());
    assertEquality(<IdentifierRec> {id: "record"}, cr2?.rec);

    assertTrue(cr2.obj is IdentifierAbstractObj & readonly);
    assertTrue(cr2.obj is IdentifierObj);
    IdentifierObj obj = <IdentifierObj> cr2.obj;
    assertEquality("object", obj.getId());
}

type AssertionError error;

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

    panic AssertionError(ASSERTION_ERROR_REASON,
            message = "expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}

