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

function testSimpleInitializationForSelectivelyImmutableXmlTypes() {
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

type EmployeeFoo record {|
    DetailsBar details;
    string department;
|};

type DetailsBar record {|
    string name;
    int id;
|};

function testSimpleInitializationForSelectivelyImmutableListTypes() {
    int[] & readonly a = [1, 2];
    readonly r1 = a;
    assertTrue(r1 is int[] & readonly);
    assertEquality(<int[]>[1, 2], r1);

    EmployeeFoo & readonly emp = {
        details: {
            name: "Emma",
            id: 1234
        },
        department: "finance"
    };

    [EmployeeFoo, EmployeeFoo] & readonly b = [emp, {details: {name: "Jo", id: 5678}, department: "IT"}];
    readonly r2 = b;
    assertTrue(r2 is [EmployeeFoo, EmployeeFoo] & readonly);
    assertTrue(r2 is EmployeeFoo[2] & readonly);

    [EmployeeFoo, EmployeeFoo] & readonly empTup = <[EmployeeFoo, EmployeeFoo] & readonly>checkpanic r2;

    assertEquality(emp, empTup[0]);
    record {} rec = empTup[0];
    assertTrue(rec is EmployeeFoo & readonly);
    assertTrue(rec.isReadOnly());

    rec = empTup[1];
    assertTrue(rec is EmployeeFoo & readonly);
    assertTrue(rec.isReadOnly());
    assertEquality("IT", rec["department"]);
    assertTrue(rec["details"] is DetailsBar & readonly);
    assertTrue(rec["details"].isReadOnly());

    DetailsBar & readonly details = {
        name: "Jo",
        id: 9876
    };
    [DetailsBar[], EmployeeFoo...] & readonly detEmpTup = [
        [{name: "May", id: 1234}, details],
        {details, department: "finance"}
    ];
    readonly r3 = detEmpTup;
    assertTrue(r3 is [DetailsBar[], EmployeeFoo...] & readonly);
    assertTrue(r3 is [[DetailsBar, DetailsBar], EmployeeFoo] & readonly);

    [[DetailsBar, DetailsBar], EmployeeFoo] & readonly vals = <[[DetailsBar, DetailsBar], EmployeeFoo] & readonly>checkpanic r3;
    assertTrue(vals[0].isReadOnly());

    DetailsBar d1 = vals[0][0];
    assertEquality(<DetailsBar>{name: "May", id: 1234}, d1);
    assertTrue(d1.isReadOnly());

    DetailsBar d2 = vals[0][1];
    assertEquality(details, d2);
    assertTrue(d2.isReadOnly());

    EmployeeFoo e = vals[1];
    assertEquality(<EmployeeFoo>{details, department: "finance"}, e);
    assertTrue(e.isReadOnly());
    assertTrue(e.details.isReadOnly());

    (int[] & readonly)|string[] arr = [1, 2];
    assertEquality(<int[]>[1, 2], arr);
    assertTrue(arr is int[] & readonly);
    assertTrue(arr.isReadOnly());

    arr = ["hello"];
    assertEquality(<string[]>["hello"], arr);
    assertTrue(arr is string[]);
    assertFalse(arr is string[] & readonly);
    assertFalse(arr.isReadOnly());
}

function testSimpleInitializationForSelectivelyImmutableMappingTypes() {
    boolean bool = false;

    map<boolean> & readonly a = {
        a: true,
        bool,
        c: false
    };
    readonly r1 = a;
    assertTrue(r1 is map<boolean> & readonly);
    assertEquality(<map<boolean>>{a: true, bool: false, c: false}, r1);

    EmployeeFoo & readonly emp = {
        details: {
            name: "Emma",
            id: 1234
        },
        department: "finance"
    };

    readonly r2 = emp;
    assertTrue(r2 is EmployeeFoo & readonly);

    assertEquality(emp, r2);
    any|error val = r2;
    assertFalse(val is error);
    EmployeeFoo rec = <EmployeeFoo>checkpanic val;
    assertTrue(rec is EmployeeFoo & readonly);
    assertTrue(rec.isReadOnly());

    DetailsBar det = rec.details;
    assertTrue(det is DetailsBar & readonly);
    assertTrue(det.isReadOnly());

    StudentFoo & readonly st = {
        details: {
            name: "Jo",
            id: 4567
        },
        "math": ["P", 75],
        "science": ["P", 65]
    };
    readonly r3 = st;
    assertTrue(r3 is StudentFoo & readonly);

    val = r3;
    assertFalse(val is error);
    StudentFoo stVal = <StudentFoo>checkpanic val;
    assertTrue(stVal.isReadOnly());
    assertTrue(stVal.details.isReadOnly());
    assertEquality(<DetailsBar>{name: "Jo", id: 4567}, stVal.details);

    assertTrue(stVal["math"] is [RESULTFoo, int] & readonly);
    assertTrue(stVal["math"].isReadOnly());
    assertEquality(<[RESULTFoo, int]>["P", 75], stVal["math"]);

    assertTrue(stVal["science"] is [RESULTFoo, int] & readonly);
    assertTrue(stVal["science"].isReadOnly());
    assertEquality(<[RESULTFoo, int]>["P", 65], stVal["science"]);
}

type IdentifierFoo record {|
    readonly string name;
    int id;
|};

function testSimpleInitializationForSelectivelyImmutableTableTypes() {
    table<map<string>> & readonly a = table [
        {x: "x", y: "y"},
        {z: "z"}
    ];

    readonly r1 = a;
    assertTrue(r1 is table<map<string>> & readonly);

    table<map<string>> tbString = <table<map<string>>><any>checkpanic r1;
    assertEquality(2, tbString.length());

    map<string>[] mapArr = tbString.toArray();
    assertTrue(mapArr[0] is map<string> & readonly);
    assertEquality(<map<string>>{x: "x", y: "y"}, mapArr[0]);
    assertTrue(mapArr[1] is map<string> & readonly);
    assertEquality(<map<string>>{z: "z"}, mapArr[1]);

    table<IdentifierFoo> key(name) & readonly b = table [
        {name: "Jo", id: 4567},
        {name: "Emma", id: 1234},
        {name: "Amy", id: 678}
    ];
    readonly r2 = b;
    assertTrue(r2 is table<IdentifierFoo> key(name) & readonly);
    assertTrue(r2 is table<IdentifierFoo> & readonly);

    table<IdentifierFoo> tbDetails = <table<IdentifierFoo>><any>checkpanic r2;
    assertEquality(3, tbDetails.length());

    IdentifierFoo[] detailsArr = tbDetails.toArray();
    assertTrue(detailsArr[0] is IdentifierFoo & readonly);
    assertEquality(<IdentifierFoo>{name: "Jo", id: 4567}, detailsArr[0]);
    assertTrue(detailsArr[1] is IdentifierFoo & readonly);
    assertEquality(<IdentifierFoo>{name: "Emma", id: 1234}, detailsArr[1]);
    assertTrue(detailsArr[2] is IdentifierFoo & readonly);
    assertEquality(<IdentifierFoo>{name: "Amy", id: 678}, detailsArr[2]);
}

type RESULTFoo "P"|"F";

type StudentFoo record {|
    DetailsBar details;
    [RESULTFoo, int]...;
|};

type recursiveTuple [int, string|xml, recursiveTuple...];

function testRuntimeIsTypeForSelectivelyImmutableBasicTypes() {
    xml a = xml `<foo><bar>Text</bar></foo>`;
    any|error b = a;
    any|error c = a.cloneReadOnly();
    assertFalse(b is readonly);
    assertTrue(c is readonly);

    int[] d = [1, 2];
    any|error e = d;
    any|error f = d.cloneReadOnly();
    assertFalse(e is readonly);
    assertTrue(f is readonly);

    [boolean, int] g = [true, 2];
    any|error h = g;
    any|error i = g.cloneReadOnly();
    assertFalse(h is readonly);
    assertTrue(i is readonly);

    [int, string|xml, int...] q = [1, ""];
    [int, string|xml, int...] r = q.cloneReadOnly();
    assertTrue(r is json);
    assertFalse(q is json);

    recursiveTuple p = [1, ""];
    recursiveTuple s = p.cloneReadOnly();
    assertTrue(s is json);
    assertFalse(p is json);

    map<string> j = {a: "a", b: "b"};
    any|error k = j;
    any|error l = j.cloneReadOnly();
    assertFalse(k is readonly);
    assertTrue(l is readonly);

    EmployeeFoo m = {
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
}

function testRuntimeIsTypeNegativeForSelectivelyImmutableTypes() {
    int[] a = [1, 2];
    anydata a1 = a;
    any|error an1 = a;
    assertFalse(an1 is int[] & readonly);
    assertFalse(an1 is readonly);
    assertFalse(a1.isReadOnly());

    EmployeeFoo emp = {
        details: {
            name: "Emma",
            id: 1234
        },
        department: "finance"
    };

    [EmployeeFoo, EmployeeFoo] b = [emp, {details: {name: "Jo", id: 5678}, department: "IT"}];
    anydata a2 = b;
    any an2 = b;
    assertFalse(an2 is [EmployeeFoo, EmployeeFoo] & readonly);
    assertFalse(an2 is readonly);
    assertFalse(a2.isReadOnly());

    [EmployeeFoo, EmployeeFoo] empTup = <[EmployeeFoo, EmployeeFoo]>a2;

    assertEquality(emp, empTup[0]);
    record {} rec = empTup[0];
    assertTrue(rec is EmployeeFoo);
    assertFalse(rec is EmployeeFoo & readonly);
    assertFalse(rec.isReadOnly());

    rec = empTup[1];
    assertTrue(rec is EmployeeFoo);
    assertFalse(rec is EmployeeFoo & readonly);
    assertFalse(rec.isReadOnly());
    assertTrue(rec["details"] is DetailsBar);
    assertFalse(rec["details"] is DetailsBar & readonly);
    assertFalse(rec["details"].isReadOnly());

    DetailsBar & readonly details = {
        name: "Jo",
        id: 9876
    };
    [DetailsBar[], EmployeeFoo...] detEmpTup = [
        [{name: "May", id: 1234}, details],
        {details, department: "finance"}
    ];
    anydata a3 = detEmpTup;
    assertTrue(a3 is [DetailsBar[], EmployeeFoo...]);
    assertFalse(a3 is [DetailsBar[], EmployeeFoo...] & readonly);
    assertFalse(a3 is [[DetailsBar, DetailsBar], EmployeeFoo] & readonly);

    [DetailsBar[], EmployeeFoo...] vals = <[DetailsBar[], EmployeeFoo...]>a3;
    assertFalse(vals[0].isReadOnly());

    DetailsBar d1 = vals[0][0];
    assertFalse(d1.isReadOnly());

    DetailsBar d2 = vals[0][1];
    assertEquality(details, d2);
    assertTrue(d2.isReadOnly());

    EmployeeFoo e = vals[1];
    assertEquality(<EmployeeFoo>{details, department: "finance"}, e);
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

    json[] jsonVal = <json[]>an5;
    map<json> a8 = <map<json>>jsonVal[1];
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

    table<IdentifierFoo> key(name) j = table [
        {name: "Jo", id: 4567},
        {name: "Emma", id: 1234},
        {name: "Amy", id: 678}
    ];
    anydata a9 = j;
    any an9 = j;
    assertTrue(an9 is table<IdentifierFoo>);
    assertFalse(an9 is table<IdentifierFoo> & readonly);
    assertFalse(an9 is readonly);
    assertFalse(a9.isReadOnly());

    MyMutableController mmc = new (new MyOwnerTSI(), new MyMutablePrinter());
    ControllerTSI k = mmc;
    any an10 = k;
    assertTrue(an10 is ControllerTSI);
    assertFalse(an10 is ControllerTSI & readonly);
    assertTrue(k.owner is OwnerTSI & readonly);
    assertFalse(k.printer is PrinterTSI & readonly);
}

class MyMutableController {
    OwnerTSI owner;
    PrinterTSI printer;

    function init(OwnerTSI & readonly owner, PrinterTSI printer) {
        self.owner = owner;
        self.printer = printer;
    }
}

class MyMutablePrinter {
    int id = 1234;

    function getPrintString(string s) returns string {
        return string `ID[${self.id}]: ${s}`;
    }
}

function testImmutabilityOfNestedXmlWithAttributes() {
    xml x1 = xml `<book status="available" count="5">Book One<name lang="english">Great Expectations</name><!-- This is a classic--><author gender="male"><?action concat?><firstName index="C">Charles</firstName><lastName>Dickens</lastName></author></book>`;
    checkXmlValueAndMutability(<'xml:Element> x1, false);

    xml & readonly x2 = xml `<book status="available" count="5">Book One<name lang="english">Great Expectations</name><!-- This is a classic--><author gender="male"><?action concat?><firstName index="C">Charles</firstName><lastName>Dickens</lastName></author></book>`;
    checkXmlValueAndMutability(<'xml:Element & readonly> x2, true);

    'xml:Element x3 = xml `<book status="available" count="5">Book One<name lang="english">Great Expectations</name><!-- This is a classic--><author gender="male"><?action concat?><firstName index="C">Charles</firstName><lastName>Dickens</lastName></author></book>`;
    checkXmlValueAndMutability(x3, false);

    'xml:Element & readonly x4 = xml `<book status="available" count="5">Book One<name lang="english">Great Expectations</name><!-- This is a classic--><author gender="male"><?action concat?><firstName index="C">Charles</firstName><lastName>Dickens</lastName></author></book>`;
    checkXmlValueAndMutability(x4, true);
}

function checkXmlValueAndMutability('xml:Element value, boolean isReaodnly) {
    function (any|error) func = isReaodnly ? assertTrue : assertFalse;

    any anyVal = value;
    anydata anydataVal = value;
    func(anyVal is readonly);
    func(anydataVal.isReadOnly());
    func(value.isReadOnly());
    func(value is 'xml:Element & readonly);

    map<string> attribs = value.getAttributes();
    assertEquality(2, attribs.length());
    assertEquality(attribs["status"], "available");
    assertEquality(attribs["count"], "5");
    func(attribs is map<string> & readonly);

    if isReaodnly {
        map<string> & readonly readonlyAttribs = <map<string> & readonly> attribs;
        assertEquality(2, readonlyAttribs.length());
    }

    xml children = value/*;
    assertEquality(4, children.length());

    xml c1 = children.get(0);
    assertEquality(xml `Book One`, c1);
    assertTrue(c1 is 'xml:Text);
    assertTrue(c1.isReadOnly());

    'xml:Element c2 = <'xml:Element> children.get(1);
    assertEquality(xml `<name lang="english">Great Expectations</name>`, c2);
    func(c2.isReadOnly());
    func(c2 is 'xml:Element & readonly);

    attribs = c2.getAttributes();
    assertEquality(1, attribs.length());
    assertEquality(attribs["lang"], "english");
    func(attribs is map<string> & readonly);

    xml c3 = children.get(2);
    assertEquality(xml `<!-- This is a classic-->`, c3);
    assertTrue(c3 is 'xml:Comment);
    func(c3.isReadOnly());
    func(c3 is 'xml:Comment & readonly);

    'xml:Element c4 = <'xml:Element> children.get(3);
    assertEquality(xml `<author gender="male"><?action concat?><firstName index="C">Charles</firstName><lastName>Dickens</lastName></author>`, c4);
    func(c4.isReadOnly());
    func(c4 is 'xml:Element & readonly);

    attribs = c4.getAttributes();
    assertEquality(1, attribs.length());
    assertEquality(attribs["gender"], "male");
    func(attribs is map<string> & readonly);

    xml nestedChildren = c4/*;
    assertEquality(3, nestedChildren.length());

    xml nc1 = nestedChildren.get(0);
    assertEquality(xml `<?action concat?>`, nc1);
    assertTrue(nc1 is 'xml:ProcessingInstruction);
    func(nc1.isReadOnly());
    func(nc1 is 'xml:ProcessingInstruction & readonly);

    'xml:Element nc2 = <'xml:Element> nestedChildren.get(1);
    assertEquality(xml `<firstName index="C">Charles</firstName>`, nc2);
    func(nc2.isReadOnly());
    func(nc2 is 'xml:Element & readonly);

    attribs = nc2.getAttributes();
    assertEquality(1, attribs.length());
    assertEquality(attribs["index"], "C");
    func(attribs is map<string> & readonly);

    'xml:Element nc3 = <'xml:Element> nestedChildren.get(2);
    assertEquality(xml `<lastName>Dickens</lastName>`, nc3);
    func(nc3.isReadOnly());
    func(nc3 is 'xml:Element & readonly);

    attribs = nc3.getAttributes();
    assertEquality(0, attribs.length());
    func(attribs is map<string> & readonly);
}

type Foo record {|
    Bar & readonly b;
|};

type Bar record {|
    int i;
    Baz & readonly...;
|};

type Baz record {|
    float f;
|};

function testImmutableTypedRecordFields() {
    Baz & readonly immBaz = {f: 2};

    Bar & readonly immBar = {
        i: 1,
        "c": {
            f: 2
        }
    };

    Foo f1 = {
        b: immBar
    };

    assertFalse(f1.isReadOnly());
    assertTrue(f1.b.isReadOnly());
    anydata val = f1.b["c"];
    assertTrue(val is Baz & readonly);
    assertTrue(val.isReadOnly());
    assertEquality(immBaz, val);

    Foo & readonly f2 = {
        b: immBar
    };

    assertTrue(f2.isReadOnly());
    assertTrue(f2.b.isReadOnly());
    val = f2.b["c"];
    assertTrue(val is Baz & readonly);
    assertTrue(val.isReadOnly());
    assertEquality(immBaz, val);
}

type Qux record {|
    Qux a?;
    Qux? b;
    int c;
    Qux|boolean...;
|};

function testImmutabilityForSelfReferencingType() {
    Qux & readonly q1 = {b: {c: 2, "f": true, b: ()}, c: 1, "d": true, "e": {c: 3, b: ()}};
    any a1 = q1;
    anydata ad1 = q1;

    assertTrue(a1 is Qux & readonly);
    assertTrue(ad1.isReadOnly());
    Qux qVal = <Qux & readonly> a1;
    assertEquality(<Qux> {b: {c: 2, "f": true, b: ()}, c: 1, "d": true, "e": {c: 3, b: ()}}, qVal);
    assertTrue(qVal?.a is ());
    assertTrue(qVal.b is Qux & readonly);
    assertTrue(qVal["d"] is boolean & readonly);
    assertTrue(qVal["e"] is Qux & readonly);

    Qux q2 = {a: q1, b: (), c: 23, "d": true, "e": {c: 4, b: ()}};
    any a2 = q2;
    anydata ad2 = q2;

    assertTrue(a2 is Qux);
    assertFalse(a2 is Qux & readonly);
    assertFalse(ad2.isReadOnly());
    qVal = <Qux> a2;
    assertEquality(<Qux> {a: q1, b: (), c: 23, "d": true, "e": {c: 4, b: ()}}, qVal);
    assertTrue(qVal?.a is Qux & readonly);

    Qux aQux =  <Qux> qVal?.a;
    assertTrue(aQux.isReadOnly());

    assertTrue(qVal.b is ());
    assertTrue(qVal["e"] is Qux);
    assertFalse(qVal["e"] is Qux & readonly);
    Qux eQux =  <Qux> qVal["e"];
    assertFalse(eQux.isReadOnly());
}

type Quux record {|
    string name = "xyz";
    int id;
|};

function testImmutableRecordWithDefaultValues() {
    Quux & readonly q1 = {id: 1};
    any a1 = q1;
    anydata ad1 = q1;

    assertTrue(a1 is Quux);
    assertTrue(a1 is Quux & readonly);
    assertTrue(ad1.isReadOnly());
    assertEquality(q1.name, "xyz");
    assertEquality(q1.id, 1);

    Quux & readonly q2 = {id: 2, name: "abc"};
    any a2 = q2;
    anydata ad2 = q2;

    assertTrue(a2 is Quux);
    assertTrue(a2 is Quux & readonly);
    assertTrue(ad2.isReadOnly());
    assertEquality(q2.name, "abc");
    assertEquality(q2.id, 2);
}

readonly class MyOwnerTSI {
    final int id = 238475;

    function getId() returns int {
        return self.id;
    }
}

readonly class MyControllerTSI {
    final OwnerTSI & readonly owner;
    final readonly & PrinterTSI printer;

    function init(OwnerTSI & readonly ow, PrinterTSI pr) {
        self.owner = ow;
        self.printer = <PrinterTSI & readonly> pr;
    }
}

type PrinterTSI object {
    function getPrintString(string s) returns string;
};

type ControllerTSI object {
    OwnerTSI owner;
    PrinterTSI printer;
};

type OwnerTSI object {
    function getId() returns int;
};

readonly class MyPrinterTSI {
    final int id;

    function init(int id) {
        self.id = id;
    }

    function getPrintString(string s) returns string {
        return string `ID[${self.id}]: ${s}`;
    }
}

function testImmutableObjects() {
    ControllerTSI & readonly cr = new MyControllerTSI(new MyOwnerTSI(), new MyPrinterTSI(1234));

    any x = cr;
    assertTrue(x is ControllerTSI);
    assertTrue(x is MyControllerTSI);
    assertTrue(x is ControllerTSI & readonly);

    ControllerTSI cr2 = cr;
    assertTrue(cr2.owner is OwnerTSI & readonly);
    assertTrue(cr2.owner is MyOwnerTSI);
    assertTrue(cr2.printer is PrinterTSI & readonly);
    assertTrue(cr2.printer is MyPrinterTSI);
    assertEquality(238475, cr.owner.getId());
    assertEquality("ID[1234]: str to print", cr.printer.getPrintString("str to print"));
}

function testImmutableJson() {
    json & readonly j = {a: 1, b: "hello"};
    anydata a = j;
    any an = j;
    assertTrue(an is json & readonly);
    assertTrue(an is map<json> & readonly);
    assertTrue(an is readonly);
    assertTrue(a.isReadOnly());
    assertEquality(<map<json>> {a: 1, b: "hello"}, a);

    json & readonly k = 1;
    a = k;
    an = k;
    assertTrue(an is json & readonly);
    assertTrue(an is int);
    assertTrue(an is readonly);
    assertTrue(a.isReadOnly());
    assertEquality(1, a);
}

function testImmutableAnydata() {
    anydata & readonly j = {a: 1, b: "hello"};
    anydata a = j;
    any an = j;
    assertTrue(an is anydata & readonly);
    assertTrue(an is map<anydata> & readonly);
    assertTrue(an is readonly);
    assertTrue(a.isReadOnly());
    assertEquality(<map<anydata>> {a: 1, b: "hello"}, a);

    anydata & readonly k = 12.0d;
    a = k;
    an = k;
    assertTrue(an is anydata & readonly);
    assertTrue(an is decimal);
    assertTrue(an is readonly);
    assertTrue(a.isReadOnly());
    assertEquality(12.0d, a);
}

function testImmutableAny() {
    any & readonly j = {a: 1, b: "hello"};
    any an = j;
    assertTrue(an is any & readonly);
    assertTrue(an is map<any> & readonly);
    assertTrue(an is readonly);
    map<any> anyMap = <map<any>> an;
    assertEquality(anyMap["a"], 1);
    assertEquality(anyMap["b"], "hello");

    any & readonly k = "string value";
    an = k;
    assertTrue(an is any & readonly);
    assertTrue(an is string);
    assertTrue(an is readonly);
    assertEquality("string value", an);
}

function testImmutableUnion() {
    (json|xml|future<int>) & readonly j = {a: 1, b: "hello"};
    any an = j;
    assertTrue(an is json & readonly);
    assertTrue(an is map<json> & readonly);
    assertTrue(an is readonly);
    anydata a = <anydata> j;
    assertTrue(a.isReadOnly());
    assertEquality(<map<json>> {a: 1, b: "hello"}, a);

    (float|string[]) & readonly k = 12.0;
    a = k;
    an = k;
    assertTrue(an is float);
    assertTrue(an is readonly);
    assertTrue(a.isReadOnly());
    assertEquality(12.0f, an);
}

type Versioning record {|
    string pattern = "v{major}.{minor}";
    boolean allow = true;
    boolean matchMajor = false;
|};

type Quota record {
    int initial = 10;
    float factor = 2.0;
};

type IdentifierRec record {
    string id = "record";
};

type IdentifierAbstractObj object {
    function getId() returns string;
};

readonly class IdentifierObj {
    final string id;

    function init() {
        self.id = "object";
    }

    function getId() returns string {
        return self.id;
    }
}

type ConfigRec record {
    Versioning versioning;
    Quota quota;
    IdentifierRec rec?;
    IdentifierAbstractObj obj;
};

function testDefaultValuesOfFields() {
    ConfigRec & readonly cr = {versioning: {}, quota: {initial: 5}, rec: {}, obj: new IdentifierObj()};

    any a = cr;
    assertTrue(a is ConfigRec);
    assertTrue(a is ConfigRec & readonly);

    ConfigRec cr2 = cr;

    assertTrue(cr2.versioning is Versioning & readonly);
    assertTrue(cr2.versioning.isReadOnly());
    assertEquality(<Versioning> {pattern: "v{major}.{minor}", allow: true, matchMajor: false}, cr2.versioning);

    assertTrue(cr2.quota is Quota & readonly);
    assertTrue(cr2.quota.isReadOnly());
    assertEquality(<Quota> {initial: 5, factor: 2.0}, cr2.quota);

    assertTrue(cr2?.rec is IdentifierRec & readonly);
    assertTrue(cr2?.rec.isReadOnly());
    assertEquality(<IdentifierRec> {id: "record"}, cr2?.rec);

    assertTrue(cr2.obj is IdentifierAbstractObj & readonly);
    assertTrue(cr2.obj is IdentifierObj);
    IdentifierObj obj = <IdentifierObj> cr2.obj;
    assertEquality("object", obj.getId());
}

public type ConfigArray record {|
    string identifier;
    (Array & readonly)? config = ();
    Array? keys = ();
|};

public type Array record {|
    int count;
    readonly string[] values;
|};

function testUnionReadOnlyFields() {
    ConfigArray cArr = {
        identifier: "MyConfig",
        config: {
            count: 2,
            values: ["foo", "bar"]
        },
        keys: {
            count: 1,
            values: ["baz"]
        }
    };

    record {
        string identifier;
        Array? config;
        Array? keys;
    } rec = cArr;

    assertTrue(rec.config is readonly);
    assertTrue(rec.config is Array & readonly);
    assertEquality(<Array> {count: 2, values: ["foo", "bar"]}, rec.config);

    assertFalse(rec.keys is readonly);
    assertTrue(rec.keys is Array);
    assertFalse(rec.keys is Array & readonly);
    assertEquality(<Array> {count: 1, values: ["baz"]}, rec.keys);

    Array arr = <Array> rec.keys;
    assertTrue(arr.values.isReadOnly());
}

function testReadOnlyCastConstructingReadOnlyValues() {
    map<int> readOnlyMap = <readonly> {q: 1, w: 2, e: 3};
    map<int> mutableMap = {q: 1, w: 2, e: 3};

    assertTrue(readOnlyMap is map<int> & readonly);
    assertTrue(readOnlyMap.isReadOnly());
    assertFalse(mutableMap is map<int> & readonly);
    assertFalse(mutableMap.isReadOnly());

    int[]|boolean[][] arr = [
        <readonly> [true, false],
        [true, true, false]
    ];

    assertTrue(arr is boolean[][]);
    assertFalse(arr is boolean[][] & readonly);

    boolean[][] bArr = <boolean[][]> arr;

    boolean[] b1 = bArr[0];
    assertTrue(b1.isReadOnly());
    assertTrue(b1 is readonly & boolean[]);
    assertEquality(<boolean[]> [true, false], b1);

    boolean[] b2 = bArr[1];
    assertFalse(b2.isReadOnly());
    assertFalse(b2 is readonly & boolean[]);
    assertEquality(<boolean[]> [true, true, false], b2);

    Quota|future<int> qf = <readonly> {initial: 25, factor: 3.4, "mode": "default"};
    assertTrue(qf is Quota);

    Quota q = <Quota> qf;
    assertTrue(q is Quota & readonly);
    assertTrue(q.isReadOnly());
    assertEquality(25, q.initial);
    assertEquality(3.4, q.factor);
    assertEquality("default", q["mode"]);
    assertEquality((), q["missing"]);
}

function testReadOnlyCastConstructingReadOnlyValuesPropagation() {
    anydata[] & readonly c = [1, 2];

    record {
        int i;
        string[] s;
    } rec = <readonly> {
        i: 1,
        s: ["hello", "ballerina"],
        "b": {
            x: false,
            y: 2.0
        },
        "c": c
    };

    assertTrue(rec is readonly & record {|int i; string[] s; map<anydata> b; anydata[] c;|});
    var val = <record {|int i; string[] s; map<anydata> b; anydata[] c;|}> rec;

    assertEquality(1, val.i);

    assertTrue(val.s is string[] & readonly);
    assertEquality(<string[]> ["hello", "ballerina"], val.s);

    assertTrue(val.b is map<anydata> & readonly);
    assertEquality(<map<anydata>> {x: false, y: 2.0}, val.b);

    assertTrue(val.c is anydata[] & readonly);
    assertEquality(<anydata[]> [1, 2], val.c);
}

readonly class ReadOnlyClass {
    int i = 1;
}

ReadOnlyClass & readonly rc1 = new ReadOnlyClass();

function testValidInitializationOfReadOnlyClassIntersectionWithReadOnly() {
    assertTrue(<any> rc1 is ReadOnlyClass);
    ReadOnlyClass v1 = <ReadOnlyClass> rc1;
    assertEquality(1, v1.i);

    ReadOnlyClass & readonly rc2 = object {
        int i = 123;
        int j;

        function init() {
            self.j = 234;
        }
    };
    assertTrue(<any> rc2 is object { int i; int j; } & readonly);
    object { int i; int j; } v2 = <object { int i; int j; } & readonly> rc2;
    assertEquality(123, v2.i);
    assertEquality(234, v2.j);
}

class NonReadOnlyClass {
    int i = 2;
}

NonReadOnlyClass & readonly nrc1 = object {
                                      int i = 1234;
                                      int j = 2345;
                                   };

function testValidInitializationOfNonReadOnlyClassIntersectionWithReadOnly() {
    assertTrue(<any> nrc1 is object { int i; int j; } & readonly);
    object { int i; int j; } v1 = <object { int i; int j; }> <any> nrc1;
    assertEquality(1234, v1.i);
    assertEquality(2345, v1.j);

    NonReadOnlyClass & readonly nrc2 = new ReadOnlyClass();
    assertTrue(<any> nrc2 is ReadOnlyClass);
    ReadOnlyClass v2 = <ReadOnlyClass> nrc2;
    assertEquality(1, v2.i);
}

function testFunctionWithReturnTypeAnyToReadonly() {
    any a = foo;
    assertFalse(a is function () returns any);
    assertTrue(a is function () returns any|error);
}

function foo() returns readonly => 1;

function testReadOnlyIntersectionWithNever() {
    error? a = trap panicFn();

    assertTrue(a is error);
    assertEquality("err!", (<error> a).message());

    record { never i?; int j; } & readonly b = {j: 1, "k": "str"};
    assertEquality(2, b.length());
    assertEquality(1, b.j);
    assertEquality("str", b["k"]);
    assertFalse(b.hasKey("i"));

    record { never i?; int j; } c = {j: 21};
    record {} d = c.cloneReadOnly();
    assertEquality(1, d.length());
    assertEquality(21, d["j"]);
    assertFalse(d.hasKey("i"));
}

function panicFn() returns never & readonly {
    panic error("err!");
}

type Grault record {
    int|never x;
};

function testReadOnlyIntersectionWithNeverExplicitlyInType() {
    Grault & readonly a = {x: 2};
    assertEquality(1, a.length());
    assertEquality(2, a.x);
    assertTrue(a.hasKey("x"));
}

type R1 record {
    stream<int> a?;
};

function testReadOnlyIntersectionWithRecordThatHasAnOptionalNeverReadOnlyField() {
    R1 & readonly a = {"b": 1};
    assertEquality(1, a.length());
    assertFalse(a.hasKey("a"));
    assertTrue(a.hasKey("b"));

    any b = a;
    assertFalse(b is record {| never a?; |});
    assertTrue(b is record {| never a?; int b; |});
    assertTrue(b is record { never a?; });

    record { never a?; } _ = a;
}

type R2 record {|
    int a;
    stream<int>...;
|};

function testReadOnlyIntersectionWithRecordThatHasANeverReadOnlyRestField() {
    R2 & readonly a = {a: 1};
    assertEquality(1, a.length());
    assertTrue(a.hasKey("a"));

    any b = a;
    assertFalse(b is record {| never a?; |});
    assertTrue(b is record {| int a?; int b?; |});
    assertTrue(b is record {| int a?; |});
    assertTrue(b is record {| int a?; never...; |});

    record {| int a; |} _ = a;

    record {| int a; never...; |} _ = a;
    record { int a; } _ = a;
}

type ImmutableXmlElement xml:Element & readonly;

function testTypeDefinitionForReadOnlyIntersectionWithBuiltinType() {
    ImmutableXmlElement a = xml `<foo>hello</foo>`;
    xml:Element b = a;

    assertTrue(b is readonly & xml:Element);
    assertTrue(b is ImmutableXmlElement);

    error? c = trap b.setChildren(xml `text`);
    assertTrue(c is error);

    error d = <error> c;
    assertEquality("{ballerina/lang.xml}XMLOperationError", d.message());
    assertEquality("Failed to set children to xml element: modification not allowed on readonly value",
                   <string> checkpanic d.detail()["message"]);
}

function testIsReadonlyWithInBuiltUnionType() returns error? {
    json j = {a: 1};
    string s = j.toJsonString();
    json & readonly x = check s.fromJsonStringWithType();
    assertTrue(x is readonly);

    anydata k = {b: 2};
    string t = k.toJsonString();
    anydata & readonly y = check t.fromJsonStringWithType();
    assertTrue(y is readonly);
}

type MyJson int|float|decimal|boolean|string|()|map<MyJson>|MyJson[];

function testReadOnlyIntersectionWithJsonAndAnydata() {
    anydata & readonly a = 1;
    any b = a;
    assertFalse(b is string|error);
    assertTrue(b is any|error);
    assertTrue(b is anydata & readonly);
    assertTrue(b is json & readonly);

    json & readonly c = 1;
    any d = c;
    assertFalse(d is string|error);
    assertTrue(d is any|error);
    assertTrue(d is json & readonly);
    assertTrue(d is anydata & readonly);

    (MyJson & readonly)[] e = [1,  "str"];
    any g = e;
    assertTrue(g is json[]);
    assertTrue(g is MyJson[]);
    assertTrue(g is (json & readonly)[]);
    assertTrue(g is ((json & readonly)|xml)[]);
    assertFalse(g is string[]);
    assertFalse(g is string[] & readonly);

    (json & readonly)[] f = [1, 2];
    any h = f;
    assertTrue(h is MyJson[]);
    assertTrue(h is (MyJson & readonly)[]);
    assertTrue(h is (xml|(MyJson & readonly))[]);
    assertFalse(h is int[]);
    assertFalse(h is string[]);

    (json & readonly)[] i = [1, {a: 123}];
    any j = i;
    assertTrue(j is (json & readonly)[]);
    assertTrue(j is (anydata & readonly)[]);
    assertTrue((<json[]> j)[1] is record {| 123 a; |});
    assertFalse((<json[]> j)[1] is record {| 1234 a; |});
    assertFalse(j is (string|error)[]);

    (anydata & readonly)[] k = [1, "hello", xml `text`];
    any l = k;
    assertTrue(l is (anydata & readonly)[]);
    assertFalse(l is (json & readonly)[]);
    assertFalse(l is (string|error)[]);
}

function testSelectivelyImmutabilityWithRegexp() {
    (stream<int>|string:RegExp) & readonly x = re `pattern`;
    assertTrue(x is (string:RegExp & readonly));
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

    string expectedValAsString = expected is error ? expected.toString() : expected.toString();
    string actualValAsString = actual is error ? actual.toString() : actual.toString();
    panic error(ASSERTION_ERROR_REASON,
                            message = "expected '" + expectedValAsString + "', found '" + actualValAsString + "'");
}
