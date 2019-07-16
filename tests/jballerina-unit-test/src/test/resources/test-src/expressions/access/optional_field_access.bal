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
    return j2 == () && j3 == ();
}

function testOptionalFieldAccessNilLiftingOnJson2() returns boolean {
    json x = { c: 3, d: () };
    json j = { a: 1, b: x };
    return j?.b?.d?.onNil == ();
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
    return j2 == 1 && j3 == x && j4 == 3;
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

    return j == 3 && j2 == m1 && j3 == 1 && j4 == m2 && j5 == 2.0;
}

function testOptionalFieldAccessNilReturnOnMissingKey() returns boolean {
    json j = { a: 1, b: { c: "foo" } };

    json|error j2 = j?.x;
    json|error j3 = j?.y?.z;
    json|error j4 = j?.b?.d;
    return j2 == () && j3 == () && j4 == ();
}

function testOptionalFieldAccessNilReturnOnMissingKeyInJsonMap() returns boolean {
    map<json> j = { a: 1, b: { c: "foo" } };

    json j2 = j?.x;
    json|error j3 = j?.y?.z;
    json|error j4 = j?.b?.d;
    return j2 == () && j3 == () && j4 == ();
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

    return j == 3 && j2 == m1 && j3 == 1 && j4 == m2 && j5 == 2.0;
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
        return je.reason() == "{ballerina}JSONOperationError" && je.detail()?.message == "JSON value is not a mapping";
    }
    return false;
}

function assertKeyNotFoundError(json|error je, string key) returns boolean {
    if (je is error) {
        return je.reason() == "{ballerina}KeyNotFound" &&
                                je.detail()?.message == "Key '" + key + "' not found in JSON mapping";
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
    return v == 1;
}

function getJson() returns json {
    return { x: { y: 1 } };
}
