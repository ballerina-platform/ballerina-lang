// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

const ASSERTION_ERROR_REASON = "AssertionError";

type Employee record {
    string name;
    int id?;
    float salary?;
};

type Person record {
    string name;
    float id;
    float salary?;
};

type Foo record {
    int a;
    boolean b;
    float c?;
};

type Bar record {
    float a;
    boolean b;
    decimal c;
};

function testOptionalFieldAccessOnRequiredRecordField() returns boolean {
    string s = "Anne";
    Employee e = { name: s, id: 100 };
    string name = e?.name;
    return name == s;
}

function testOptionalFieldAccessOnRequiredRecordFieldInRecordUnion() returns boolean {
    Foo f = { a: 1, b: true };
    Foo|Bar fb = f;

    int|float a = fb?.a;
    boolean b = fb?.b;

    return a == 1 && b;
}

function testOptionalFieldAccessOnRequiredRecordFieldInNillableUnion() returns boolean {
    Foo f = { a: 1, b: true };
    Foo|Bar? fb = f;

    int|float? a = fb?.a;
    boolean? b = fb?.b;

    boolean assertion = a == 1 && b == true;

    fb = ();
    a = fb?.a;
    b = fb?.b;

    return assertion && a is () && b is ();
}

function testOptionalFieldAccessOnOptionalRecordField1() returns boolean {
    int i = 1100;
    Employee e1 = { name: "John", id: i };
    int? id1 = e1?.id;

    Employee e2 = { name: "John" };
    int? id2 = e2?.id;

    return id1 == i && id2 is ();
}

function testOptionalFieldAccessOnOptionalRecordField2() returns boolean {
    float f = 110.0;
    Employee e1 = { name: "John", id: 100, salary: f };
    float? salary1 = e1?.salary;

    Employee e2 = { name: "John" };
    float? salary2 = e2?.salary;

    return salary1 == f && salary2 is ();
}

function testOptionalFieldAccessOnOptionalRecordFieldInRecordUnion1() returns boolean {
    float f = 110.0;
    Person p = { name: "John", id: f };
    Employee|Person ep = p;

    int|float? id = ep?.id;
    float? salary = ep?.salary;

    return id == f && salary is ();
}

function testOptionalFieldAccessOnOptionalRecordFieldInRecordUnion2() returns boolean {
    Employee e = { name: "Anne" };
    Employee|Person ep = e;

    int|float? id = ep?.id;
    float? salary = ep?.salary;

    return id is () && salary is ();
}

function testOptionalFieldAccessOnOptionalRecordFieldInNillableRecordUnion1() returns boolean {
    string s = "Bob";
    float f = 110.0;

    Person p = { name: s, id: f };
    Employee|Person? ep = p;

    string? name = ep?.name;
    int|float? id = ep?.id;

    return id == f && name == s;
}

function testOptionalFieldAccessOnOptionalRecordFieldInNillableRecordUnion2() returns boolean {
    Employee? ep = ();

    string? name = ep?.name;
    int? id = ep?.id;

    return id is () && name is ();
}

function testOptionalFieldAccessNilLiftingOnJson1() returns boolean {
    json j = ();
    json|error j2 = j?.a;
    json|error j3 = j?.a?.b;
    return j2 is () && j3 is ();
}

function testOptionalFieldAccessNilLiftingOnJson2() returns boolean {
    json x = { c: 3, d: () };
    json j = { a: 1, b: x };
    return j?.b?.d?.onNil is ();
}

function testOptionalFieldAccessNilLiftingOnMapJson() returns boolean {
    map<json> j = { a: (), b: { c: 3, d: () } };
    json j1 = j?.a;
    json|error j2 = j?.a?.b;
    json|error j3 = j?.b?.d;
    json|error j4 = j?.b?.d?.x;

    return j1 is () && j2 is () && j3 is () && j4 is ();
}

function testOptionalFieldAccessErrorOnNonMappingJson() returns boolean {
    json j = 1;
    json|error j2 = j?.a;
    return assertNonMappingJsonError(j2);
}

function testOptionalFieldAccessErrorLiftingOnNonMappingJson() returns boolean {
    json j = 1;
    return assertNonMappingJsonError(j?.a?.b);
}

function testOptionalFieldAccessErrorLiftingOnMapJson() returns boolean {
    map<json> j = { a: 1 };
    return assertNonMappingJsonError(j?.a?.b?.c);
}

function testOptionalFieldAccessOnJsonMappingPositive() returns boolean {
    json x = { c: 3, d: () };
    json j = { a: 1, b: x };
    json|error j2 = j?.a;
    json|error j3 = j?.b;
    json|error j4 = j?.b?.c;
    return j2 === 1 && j3 === x && j4 === 3;
}

function testOptionalFieldAccessOnMapJsonPositive() returns boolean {
    map<json> x = { c: 3, d: () };
    json j = x?.c;

    map<map<json>> y = { a: { b: 1, c: "hello" }, d: { e: 2.0 } };
    json j2 = y?.a;
    json j3 = y?.a?.b;
    json j4 = y?.d;
    json|error j5 = y?.d?.e;

    map<json> m1 = { b: 1, c: "hello" };
    map<json> m2 = { e: 2.0 };

    return j === 3 && j2 == m1 && j3 === 1 && j4 == m2 && j5 === 2.0;
}

function testOptionalFieldAccessNilReturnOnMissingKey() returns boolean {
    json j = { a: 1, b: { c: "foo" } };

    json|error j2 = j?.x;
    json|error j3 = j?.y?.z;
    json|error j4 = j?.b?.d;
    return j2 === () && j3 === () && j4 === ();
}

function testOptionalFieldAccessNilReturnOnMissingKeyInJsonMap() returns boolean {
    map<json> j = { a: 1, b: { c: "foo" } };

    json j2 = j?.x;
    json|error j3 = j?.y?.z;
    json|error j4 = j?.b?.d;
    return j2 === () && j3 === () && j4 === ();
}

function testOptionalFieldAccessOnLaxUnionPositive() returns boolean {
    map<json> w = { c: 3, d: () };
    map<json>|json x = w;
    json|error j = x?.c;

    map<map<json>> y = { a: { b: 1, c: "hello" }, d: { e: 2.0 } };
    map<json>|map<map<json>> z = y;

    json j2 = z?.a;
    json|error j3 = z?.a?.b;
    json j4 = z?.d;
    json|error j5 = z?.d?.e;

    map<json> m1 = { b: 1, c: "hello" };
    map<json> m2 = { e: 2.0 };

    return j === 3 && j2 == m1 && j3 === 1 && j4 == m2 && j5 === 2.0;
}

function testOptionalFieldAccessNilReturnOnLaxUnion() returns boolean {
    json j = { a: 1, b: { c: "foo" } };
    map<json>|json j1 = j;

    json|error j2 = j1?.d;
    json|error j3 = j1?.b?.e;

    return j2 is () && j3 is ();
}

function testOptionalFieldAccessNilLiftingOnLaxUnion() returns boolean {
    map<json> j = { a: 1, b: { c: () } };
    map<json>|map<map<json>> j1 = j;

    json|error j2 = j1?.d?.e;
    json|error j3 = j1?.b?.c?.e;

    return j2 is () && j3 is ();
}

function testOptionalFieldAccessErrorReturnOnLaxUnion() returns boolean {
    json j = { a: 1, b: { c: "foo" } };
    map<json>|json j1 = j;

    json|error j2 = j1?.a?.b;
    json|error j3 = j1?.b?.c?.d;

    return assertNonMappingJsonError(j2) && assertNonMappingJsonError(j3);
}

function testOptionalFieldAccessErrorLiftingOnLaxUnion() returns boolean {
    map<json> j = { a: 1, b: { c: 44.4 } };
    map<json>|map<map<json>> j1 = j;

    json|error j2 = j1?.a?.e?.f;
    json|error j3 = j1?.b?.c?.e;

    return assertNonMappingJsonError(j2) && assertNonMappingJsonError(j3);
}

function assertNonMappingJsonError(json|error je) returns boolean {
    if (je is error) {
        var detailMessage = je.detail()["message"];
        string detailMessageString = detailMessage is error? detailMessage.toString(): detailMessage.toString();
        return je.message() == "{ballerina}JSONOperationError" && detailMessageString == "JSON value is not a mapping";
    }
    return false;
}

function assertKeyNotFoundError(json|error je, string key) returns boolean {
    if (je is error) {
        var detailMessage = je.detail()["message"];
        string detailMessageString = detailMessage is error? detailMessage.toString(): detailMessage.toString();
        return je.message() == "{ballerina}KeyNotFound" &&
                                detailMessageString == "Key '" + key + "' not found in JSON mapping";
    }
    return false;
}

type Qux record {
    Corge corge;
};

type Corge record {
    int i;
};

function getQux() returns Qux {
    return { corge: { i: 10 } };
}

function getQuxOrNil(boolean getQux) returns Qux? {
    return getQux ? { corge: { i: 10 } } : ();
}

function testOptionalFieldAccessForRequiredFieldOnInvocation() returns boolean {
    int ri = getQux()?.corge?.i;
    return ri == 10;
}

function testOptionalFieldAccessOnNillableTypeInvocation() returns boolean {
    int? ri = getQuxOrNil(true)?.corge?.i;
    return ri == 10;
}

function testNilLiftingWithOptionalFieldAccessOnNillableTypeInvocation() returns boolean {
    int? ri = getQuxOrNil(false)?.corge?.i;
    return ri == ();
}

function testJsonOptionalFieldAccessOnInvocation() returns boolean {
    json|error v = getJson()?.x?.y;
    return v === 1;
}

function getJson() returns json {
    return { x: { y: 1 } };
}

type Foo1 record {|
    string s1;
    int i1;
|};

type Bar1 record {|
    string s2;
    int i2;
|};

function testOptionalFieldAccessInUnionType1() {
    Foo1 f = { s1: "s", i1: 1};
    Foo1|Bar1 fb = f;

    string|int? x1 = fb?.s1;
    string|int? x2 = fb?.i1;
    string|int? x3 = fb?.s2;
    string|int? x4 = fb?.i2;

    if !(x1 == "s" && x2 == 1 && x3 == () && x4 == ()) {
        panic error(ASSERTION_ERROR_REASON, message = "expected 'true', found 'false'");
    }
}

function testOptionalFieldAccessInUnionType2() {
    Bar1 b = { s2: "s", i2: 1};
    Foo1|Bar1 fb = b;

    string|int? x1 = fb?.s1;
    string|int? x2 = fb?.i1;
    string|int? x3 = fb?.s2;
    string|int? x4 = fb?.i2;

    if !(x1 == () && x2 == () && x3 == "s" && x4 == 1) {
        panic error(ASSERTION_ERROR_REASON, message = "expected 'true', found 'false'");
    }
}

function testOptionalFieldAccessInUnionType3() {
    Bar1 b = { s2: "s", i2: 1};
    Foo1|Bar1? fb = b;

    string|int? x1 = fb?.s1;
    string|int? x2 = fb?.i1;
    string|int? x3 = fb?.s2;
    string|int? x4 = fb?.i2;

    if !(x1 == () && x2 == () && x3 == "s" && x4 == 1) {
        panic error("ASSERTION_ERROR_REASON", message = "expected 'true', found 'false'");
    }
}

function testOptionalFieldAccessInUnionType() {
    testOptionalFieldAccessInUnionType1();
    testOptionalFieldAccessInUnionType2();
    testOptionalFieldAccessInUnionType3();
}

class Student {
    public Details? details = ();

    function init(Details? details) {
        self.details = details;
    }

    public function getDetails() returns Details? {
        return self.details;
    }
}

public type Details record {
    Address addr?;
};

public type Address record {
    string street;
};

function testOptionalFieldAccessOnMethodCall() {
    Address addr = {street: "Colombo"};
    Details details = {addr: addr};
    Student person = new Student(details);
    string? c1 = person.getDetails()?.addr?.street;
    if (c1 != "Colombo") {
        panic error("ASSERTION_ERROR_REASON", message = "expected 'Colombo', found '" + c1.toString() + "'");
    }

    Details nilDetails = {};
    Student newPerson = new Student(nilDetails);
    string? c2 = newPerson.getDetails()?.addr?.street;
    if !(c2 is ()) {
        panic error("ASSERTION_ERROR_REASON", message = "expected '()', found '" + c2.toString() + "'");
    }
}

type Config record {|
    StartUp b?;
|};

type StartUp record {|
    int i?;
|};

function testUnavailableFinalAccessInNestedAccess() {
    Config f = {b: {}}; // `b` is present, but `b` doesn't have `i`.
    int? i = f?.b?.i;
    int? j = f?.b["i"];
    int? k = f["b"]["i"];
    int? l = f["b"]?.i;
    int? m = (f["b"])?.i;
    int? n = ((f?.b))["i"];
    int? o = (f["b"]?.i);
    int? p = ((f?.b["i"]));

    assertTrue(i is ());
    assertTrue(j is ());
    assertTrue(k is ());
    assertTrue(l is ());
    assertTrue(m is ());
    assertTrue(n is ());
    assertTrue(o is ());
    assertTrue(p is ());
}

function testAvailableFinalAccessInNestedAccess() {
    Config f = {b: {i: 1234}}; // `b` is present, and has `i`.
    int? i = f?.b?.i;
    int? j = f?.b["i"];
    int? k = f["b"]["i"];
    int? l = f["b"]?.i;
    int? m = (f["b"])?.i;
    int? n = ((f?.b))["i"];
    int? o = (f["b"]?.i);
    int? p = ((f?.b["i"]));

    assertEquality(1234, i);
    assertEquality(1234, j);
    assertEquality(1234, k);
    assertEquality(1234, l);
    assertEquality(1234, m);
    assertEquality(1234, n);
    assertEquality(1234, o);
    assertEquality(1234, p);
}

function testUnavailableIntermediateAccessInNestedAccess() {
    Config f = {};
    int? i = f?.b?.i;
    int? j = f?.b["i"];
    int? k = f["b"]["i"];
    int? l = f["b"]?.i;
    int? m = (f["b"])?.i;
    int? n = ((f?.b))["i"];
    int? o = (f["b"]?.i);
    int? p = ((f?.b["i"]));

    assertTrue(i is ());
    assertTrue(j is ());
    assertTrue(k is ());
    assertTrue(l is ());
    assertTrue(m is ());
    assertTrue(n is ());
    assertTrue(o is ());
    assertTrue(p is ());
}

type RecordWithNilableFieldConfig record {
    NilableFieldConfig? rec;
};

type NilableFieldConfig record {|
    int? i = ();
|};

function testNilValuedFinalAccessInNestedAccess() {
    RecordWithNilableFieldConfig f = {rec: {}};
    int? i = f?.rec?.i;
    int? j = f?.rec["i"];
    int? k = f["rec"]["i"];
    int? l = f["rec"]?.i;
    int? m = (f["rec"])?.i;
    int? n = ((f?.rec))["i"];
    int? o = (f["rec"]?.i);
    int? p = ((f?.rec["i"]));

    assertTrue(i is ());
    assertTrue(j is ());
    assertTrue(k is ());
    assertTrue(l is ());
    assertTrue(m is ());
    assertTrue(n is ());
    assertTrue(o is ());
    assertTrue(p is ());
}

public type Quux record {|
    int i;
    Baz baz?;
|};

public type Baz record {|
    string a;
    int i?;
|};

public function testNestedOptionalFieldAccessOnIntersectionTypes() {
    Quux & readonly q1 = {i: 1, baz: {a: "hello", i: 2}};
    var v1 = q1?.baz?.i;
    assertTrue(v1 is int);
    assertEquality(2, v1);

    Quux & readonly q2 = {i: 1, baz: {a: "hello"}};
    int? v2 = q2?.baz?.i;
    assertTrue(v2 is ());
    assertEquality((), v2);

    Quux & readonly q3 = {i: 1};
    var v3 = q3?.baz?.i;
    assertTrue(v3 is ());
    assertEquality((), v3);

    Quux & readonly q4 = {i: 1, baz: {a: "hello", i: 2}};
    var v4 = q4?.baz["i"];
    assertTrue(v4 is int);
    assertEquality(2, v4);

    Quux & readonly q5 = {i: 1, baz: {a: "hello"}};
    int? v5 = q5["baz"]?.i;
    assertTrue(v5 is ());
    assertEquality((), v5);
}

function assertTrue(anydata actual) {
    assertEquality(true, actual);
}

function assertEquality(anydata expected, anydata actual) {
    if expected == actual {
        return;
    }

    panic error("expected '" + expected.toString() + "', found '" + actual.toString() + "'");
}

type AllOptionalBasicTypeField record {|
    int val?;
|};

function getOptionalField1() returns int? {
    AllOptionalBasicTypeField r = {};
    return r.val;
}

function getOptionalField2() returns int? {
    AllOptionalBasicTypeField r = {val: 5};
    return r.val;
}
