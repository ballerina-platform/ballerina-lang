// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

final string FREEZE_ERROR_OCCURRED = "error occurred on freeze: ";
final string FREEZE_SUCCESSFUL = "freeze successful";

function testBooleanFreeze(boolean a) returns (boolean, boolean) {
    boolean b = a.freeze();
    return (a == b, (a.isFrozen() && b.isFrozen()));
}

function testIntFreeze(int a) returns (boolean, boolean) {
    int b = a.freeze();
    return (a == b, (a.isFrozen() && b.isFrozen()));
}

function testByteFreeze(byte a) returns (boolean, boolean) {
    byte b = a.freeze();
    return (a == b, (a.isFrozen() && b.isFrozen()));
}

function testFloatFreeze(float a) returns (boolean, boolean) {
    float b = a.freeze();
    return (a == b, (a.isFrozen() && b.isFrozen()));
}

function testDecimalFreeze(decimal a) returns (boolean, boolean) {
    decimal b = a.freeze();
    return (a == b, (a.isFrozen() && b.isFrozen()));
}

function testStringFreeze(string a) returns (boolean, boolean) {
    string b = a.freeze();
    return (a == b, (a.isFrozen() && b.isFrozen()));
}

function testBasicTypeNullableUnionFreeze() returns (boolean, boolean) {
    int? i = 5;
    anydata j = i.freeze();
    boolean equals = i == j;
    boolean isFrozen = i.isFrozen() && j.isFrozen();

    string? k = "hello world";
    string? l = k.freeze();
    equals = l == k;
    isFrozen = l.isFrozen() && k.isFrozen();

    float? f = -1.29;
    j = f.freeze();
    equals = f == j;
    isFrozen = f.isFrozen() && j.isFrozen();

    decimal dec = 123.9;
    decimal? d = dec;
    j = d.freeze();
    equals = d == j;
    isFrozen = d.isFrozen() && j.isFrozen();

    boolean? b = ();
    j = b.freeze();
    equals = d == b;
    isFrozen = d.isFrozen() && b.isFrozen();

    int|string? m = "hello ballerina";
    j = m.freeze();
    equals = m == j;
    isFrozen = m.isFrozen() && j.isFrozen();

    return (equals, isFrozen);
}

function testBasicTypeUnionFreeze() returns (boolean, boolean) {
    int|string i = 5;
    anydata j = i.freeze();
    boolean equals = i == j;
    boolean isFrozen = i.isFrozen() && j.isFrozen();

    boolean|float|int k = 123.2;
    boolean|float|int|decimal l = k.freeze();
    equals = l == k;
    isFrozen = k.isFrozen() && l.isFrozen();
    return (equals, isFrozen);
}

function testBasicTypesAsJsonFreeze() returns boolean {
    json a = 5;
    json b = a.freeze();
    boolean equals = a == b;

    a = 5.1;
    b = a.freeze();
    equals = equals && a == b;

    a = "Hello from Ballerina";
    b = a.freeze();
    equals = equals && a == b;

    a = true;
    b = a.freeze();
    return equals && a == b;
}

function testIsFrozenOnStructuralTypes() returns (boolean, boolean)|error {
    Employee e = { id: 0, name: "Em" };
    map<string|int|()|Employee> m = { one: "1", two: 2, three: (), rec: e };

    anydata[] a = [1, "hi", 2.0, false, m, (), e];
    (int, string, Employee) t = (1, "Em", e);
    
    json j = { name: "Em", dataType: "json" };
    xml x = xml `<bookItem>The Lost World</bookItem>`;

    table<Employee> empTable = table {
        { key id, name },
        [
            { 1, "Mary" },
            { 2, "John" },
            { 3, "Jim" }
        ]
    };

    byte byteVal = 255;
    map<anydata> m1 = { intVal: 1, byteVal: byteVal, floatVal: 200.1, stringVal: "Ballerina says freeze",
        booleanVal: false, arrayVal: a, mapVal: m, tupleVal: t, jsonVal: j, xmlVal: x, tableVal: empTable };

    boolean isFrozenBeforeFreeze = m.isFrozen() || a.isFrozen() || m1.isFrozen() || e.isFrozen() || t.isFrozen() ||
                                    j.isFrozen() || x.isFrozen() || empTable.isFrozen();

    map<anydata> m2 = m1.freeze();
    boolean isFrozenAfterFreeze = m.isFrozen() && a.isFrozen() && m1.isFrozen() && e.isFrozen() && t.isFrozen() &&
                                    m2.isFrozen() && j.isFrozen() && x.isFrozen() && empTable.isFrozen();
    return (isFrozenBeforeFreeze, isFrozenAfterFreeze);
}

function testFrozenIntArrayModification() {
    int[] a = [1, 2, 3];
    _ = a.freeze();
    a[4] = 200;
}

function testFrozenBooleanArrayModification() {
    boolean[] a = [false, true];
    _ = a.freeze();
    a[1] = true;
}

function testFrozenByteArrayModification() {
    byte[] a = [1, 2, 3];
    _ = a.freeze();
    a[5] = 200;
}

function testFrozenDecimalArrayModification() {
    decimal[4] d = [1.0, 0.2, 310.0, 22222.3];
    _ = d.freeze();
    d[3] = 200.23;
}

function testFrozenFloatArrayModification() {
    float[] a = [1.0, 0.2, 300.23];
    _ = a.freeze();
    a[1] = 200.23;
}

function testFrozenStringArrayModification() {
    string[] a = ["ballerina", "2", "hello world"];
    _ = a.freeze();
    a[1] = "addition to frozen array";
}

function testFrozenJsonArrayModification() {
    json j = { hello: "world "};
    json[] a = [j, "ballerina", 2, 10.3];
    _ = a.freeze();
    a[8] = j;
}

json j1 = { name: "Em", dataType: "json", pos: "inner" };
json j2 = { name: "Zee", dataType: "json", pos: "outer", innerJson: j1 };

function testFrozenJsonModification() {
    _ = j2.freeze();
    j2.name = "Em";
}

function testAdditionToFrozenJson() {
    _ = j2.freeze();
    j2.newField = "Em";
}

function testRemovalFromFrozenJson() {
    json j3 = j2.freeze();
    _ = j3.remove("name");
}

function testFrozenInnerJsonModification() {
    _ = j2.freeze();
    j2.innerJson.name = "Em";
}

function testAdditionToFrozenInnerJson() {
    _ = j2.freeze();
    j2.innerJson.newField = "Em";
}

function testRemovalFromFrozenInnerJson() {
    json j3 = j2.freeze();
    _ = j3.innerJson.remove("name");
}

function testFrozenXmlAppendChildren() {
    xml x1 = xml `<book>The Lost World</book>`;
    xml x2 = xml `<author>Doyle</author>`;

    xml x3 = x1.freeze();
    x3.appendChildren(x2);
}

function testFrozenXmlRemoveChildren() {
    xml x1 = xml `<book>The Lost World<author>Doyle</author></book>`;
    xml x2 = x1.freeze();
    x2.removeChildren("author");
}

function testFrozenXmlRemoveAttribute() {
    xml x1 = xml `<book attr="one">The Lost World</book>`;
    _ = x1.freeze();
    x1.removeAttribute("attr");
}

function testFrozenXmlSetAttributes() {
    map<any> m = { attr1: "one", attr2: "two"};
    xml x1 = xml `<book>The Lost World</book>`;
    _ = x1.freeze();
    x1.setAttributes(m);
}

function testFrozenXmlSetChildren() {
    xml x1 = xml `<book>The Lost World</book>`;
    xml x2 = xml `<author>Doyle</author>`;

    xml x3 = x1.freeze();
    x3.setChildren(x2);
}

function testFrozenMapUpdate() {
    map<any> m1 = { one: "1", two: 2 };
    map<any> m2 = { one: "21", two: 22, mapVal: m1 };

    _ = m2.freeze();
    m2.one = 22;
}

function testFrozenMapRemoval() {
    map<any> m1 = { one: "1", two: 2 };
    map<any> m2 = { one: "21", two: 22, mapVal: m1 };

    _ = m2.freeze();
    _ = m2.remove("one");
}

function testFrozenMapClear() {
    map<any> m1 = { one: "1", two: 2 };

    _ = m1.freeze();
    m1.clear();
}

function testFrozenInnerMapUpdate() {
    map<any> m1 = { one: "1", two: 2 };
    map<any> m2 = { one: "21", two: 22, mapVal: m1 };

    _ = m2.freeze();
    m1["one"] = 12;
}

function testFrozenInnerMapRemoval() returns error? {
    map<any> m1 = { one: "1", two: 2 };
    map<any> m2 = { one: "21", two: 22, mapVal: m1 };

    _ = m2.freeze();
    map<any> m3 = check trap <map<any>> m2.mapVal;
    _ = m3.remove("one");
    return ();
}

function testFrozenInnerMapClear() {
    map<any> m1 = { one: "1", two: 2 };
    map<any> m2 = { one: "21", two: 22, mapVal: m1 };

    _ = m2.freeze();
    m1.clear();
}

function testFrozen2DBasicTypedArrayUpdate() {
    int[][] i1 = [[1, 2], [3]];
    _ = i1.freeze();
    i1[0][0] = 9;
}

function testFrozenAnyArrayAddition() {
    Employee e1 = { name: "Em", id: 1000 };
    int[] i = [1, 2];
    any[] i1 = [i, e1];
    _ = i1.freeze();
    i1[3] = "freeze addition";
}

function testFrozenAnyArrayUpdate() returns error? {
    Employee e1 = { name: "Em", id: 1000 };
    int[] i = [1, 2];
    any[] i1 = [i, e1];
    _ = i1.freeze();
    Employee e2 = check trap <Employee> i1[1];
    i1[1] = 100;
    return ();
}

function testFrozenAnyArrayElementUpdate() returns error? {
    Employee e1 = { name: "Em", id: 1000 };
    int[] i = [1, 2];
    any[] i1 = [i, e1];
    _ = i1.freeze();
    Employee e2 = check trap <Employee> i1[1];
    e2["name"] = "Zee";
    return ();
}

function testFrozenTupleUpdate() {
    Employee e1 = { name: "Em", id: 1000 };
    (int, Employee) t1 = (1, e1);
    _ = t1.freeze();
    Employee e2 = { name: "Zee", id: 1200 };
    t1[1] = e2;
}

function testFrozenRecordUpdate() {
    Dept d1 = { code: "fe11", name: "finance" };
    DeptEmployee e1 = { name: "Em", id: 1000, dept: d1 };
    DeptEmployee e3 = e1.freeze();
    e3.name = "Zee";
}

function testFrozenInnerRecordUpdate() {
    Dept d1 = { code: "fe11", name: "finance" };
    DeptEmployee e1 = { name: "Em", id: 1000, dept: d1 };
    _ = e1.freeze();
    Dept d2 = e1.dept;
    d2.code = "fe12";
}

function testFrozenTableAddition() {
    table<Employee> empTable = table {
        { key id, name },
        [
            { 1, "Mary" },
            { 2, "John" },
            { 3, "Jim" }
        ]
    };
    Employee e = { id: 5, name: "Anne" };
    _ = empTable.freeze();
    _ = empTable.add(e);
}

function testFrozenTableRemoval() {
    table<Employee> empTable = table {
        { key id, name },
        [
            { 1, "Mary" },
            { 2, "John" },
            { 3, "Jim" }
        ]
    };
    _ = empTable.freeze();
    _ = empTable.remove(isIdTwo);
}

function testSimpleUnionFreeze() returns boolean {
    int|string u1 = "hello world";

    int|string u2 = u1.freeze();
    return u1.isFrozen() && u2.isFrozen();
}

function testInvalidComplexMapFreeze() returns (string, boolean) {
    map<string|PersonObj> m1 = {};
    PersonObj p = new("John");

    m1.one = "one";
    m1.two = p;

    map<string|PersonObj>|error res = m1.freeze();
    string errorOrSuccessMsg = (res is error) ? FREEZE_ERROR_OCCURRED + <string>res.detail().message : FREEZE_SUCCESSFUL;
    return (errorOrSuccessMsg, m1.isFrozen());
}

function testInvalidComplexArrayFreeze() returns (string, boolean) {
    (string|typedesc|float)?[] a1 = [];
    typedesc p = int;

    a1[0] = 2.0;
    a1[1] = "hello world";
    a1[2] = p;

    (string|typedesc|float)?[]|error res = a1.freeze();
    string errorOrSuccessMsg = (res is error) ? FREEZE_ERROR_OCCURRED + <string>res.detail().message : FREEZE_SUCCESSFUL;
    return (errorOrSuccessMsg, a1.isFrozen());
}

function testInvalidComplexRecordFreeze() returns (string, boolean) {
    PersonObj p = new("Anne");
    PersonObj p1 = new("Harry");
    PersonObj p2 = new("John");
    FreezeAllowedDepartment fd = { head: p, e1: p1, e2: 10 };

    FreezeAllowedDepartment|error res = fd.freeze();
    string errorOrSuccessMsg = (res is error) ? FREEZE_ERROR_OCCURRED + <string>res.detail().message : FREEZE_SUCCESSFUL;
    return (errorOrSuccessMsg, fd.isFrozen());
}

function testInvalidComplexTupleFreeze() returns (string, boolean) {
    PersonObj p = new("John");
    (int, string|PersonObj|float, boolean) t1 = (1, p, true);

    any|error res = t1.freeze();
    string errorOrSuccessMsg = (res is error) ? FREEZE_ERROR_OCCURRED + <string>res.detail().message : FREEZE_SUCCESSFUL;
    return (errorOrSuccessMsg, t1.isFrozen());
}

function testInvalidComplexUnionFreeze() returns (string, boolean) {
    PersonObj p = new("John");
    int|Dept|PersonObj u1 = p;

    int|Dept|PersonObj|error res = u1.freeze();
    string errorOrSuccessMsg = (res is error) ? FREEZE_ERROR_OCCURRED + <string>res.detail().message : FREEZE_SUCCESSFUL;
    return (errorOrSuccessMsg, u1.isFrozen());
}

function testInvalidSelfReferencingValueFreeze() returns (string, boolean) {
    map<any> m = { one: 1 };
    map<any> m2 = { two: 2 };
    m.m2 = m2;
    m2.m = m;

    PersonObj p = new("Em");
    m2.p = p;

    map<any>|error res = m.freeze();
    string errorOrSuccessMsg = (res is error) ? FREEZE_ERROR_OCCURRED + <string>res.detail().message : FREEZE_SUCCESSFUL;
    return (errorOrSuccessMsg, m.isFrozen() || m2.isFrozen());
}

function testValidComplexMapFreeze() returns (string, boolean) {
    map<string|PersonObj> m1 = {};

    m1.one = "one";
    m1.two = "2";

    map<string|PersonObj>|error res = m1.freeze();
    string errorOrSuccessMsg = (res is error) ? FREEZE_ERROR_OCCURRED + <string>res.detail().message : FREEZE_SUCCESSFUL;
    return (errorOrSuccessMsg, m1.isFrozen());
}

function testValidComplexArrayFreeze() returns (string, boolean) {
    (string|PersonObj|float)?[] a1 = [];

    a1[0] = 2.0;
    a1[1] = "hello world";

    (string|PersonObj|float)?[]|error res = a1.freeze();
    string errorOrSuccessMsg = (res is error) ? FREEZE_ERROR_OCCURRED + <string>res.detail().message : FREEZE_SUCCESSFUL;
    return (errorOrSuccessMsg, a1.isFrozen());
}

function testValidComplexRecordFreeze() returns (string, boolean) {
    FreezeAllowedDepartment fd = { head: "John", e1: 234, e2: 10 };

    any|error res = fd.freeze();
    string errorOrSuccessMsg = (res is error) ? FREEZE_ERROR_OCCURRED + <string>res.detail().message : FREEZE_SUCCESSFUL;
    return (errorOrSuccessMsg, fd.isFrozen());
}

function testValidComplexTupleFreeze() returns (string, boolean) {
    (int, string|PersonObj|float, boolean) t1 = (1, 3.0, true);

    (int, string|PersonObj|float, boolean)|error res = t1.freeze();
    string errorOrSuccessMsg = (res is error) ? FREEZE_ERROR_OCCURRED + <string>res.detail().message : FREEZE_SUCCESSFUL;
    return (errorOrSuccessMsg, t1.isFrozen());
}

function testValidComplexUnionFreeze() returns (string, boolean) {
    Dept d = { code: "FN101", name: "Finance" };
    int|Dept|PersonObj u1 = d;

    int|Dept|PersonObj|error res = u1.freeze();
    string errorOrSuccessMsg = (res is error) ? FREEZE_ERROR_OCCURRED + <string>res.detail().message : FREEZE_SUCCESSFUL;
    return (errorOrSuccessMsg, u1.isFrozen());
}

function testValidSelfReferencingValueFreeze() returns (string, boolean) {
    map<any> m = { one: 1 };
    map<any> m2 = { two: 2 };
    m.m2 = m2;
    m2.m = m;

    map<any>|error res = m.freeze();
    string errorOrSuccessMsg = (res is error) ? FREEZE_ERROR_OCCURRED + <string>res.detail().message : FREEZE_SUCCESSFUL;
    return (errorOrSuccessMsg, m.isFrozen() || m2.isFrozen());
}

function testPreservingInnerMapFrozenStatusOnFailedOuterFreeze() returns (string, boolean, boolean) {
    map<any> m = { one: 1 };
    map<any> m2 = { two: 2 };
    _ = m.freeze();

    m2.m = m;
    PersonObj p = new("Em");
    m2.p = p;

    map<any>|error res = m2.freeze();
    string errorOrSuccessMsg = (res is error) ? FREEZE_ERROR_OCCURRED + <string>res.detail().message : FREEZE_SUCCESSFUL;
    return (errorOrSuccessMsg, m2.isFrozen(), m.isFrozen());
}

function testPreservingInnerArrayFrozenStatusOnFailedOuterFreeze() returns (string, boolean, boolean) {
    string[] a = ["hello world", "from Ballerina"];
    json[] a1 = ["hello", "world", true];
    any[] a2 = [1, a1, a, 11.2];
    _ = a.freeze();

    PersonObj p = new("Em");
    a2[4] = p;

    any[]|error res = a2.freeze();
    string errorOrSuccessMsg = (res is error) ? FREEZE_ERROR_OCCURRED + <string>res.detail().message : FREEZE_SUCCESSFUL;
    return (errorOrSuccessMsg, a2.isFrozen(), a.isFrozen());
}

function testErrorValueFreeze() returns string {
    error e = error("test error");
    any|error val = e;

    any|error res = val.freeze();
    return (res is error) ? FREEZE_ERROR_OCCURRED + <string>res.detail().message : FREEZE_SUCCESSFUL;
}

function testFrozenValueUpdatePanicWithCheckTrap() returns boolean|error {
    json j = { hello: "world "};
    json[] a = [j, "ballerina", 2, 10.3];
    _ = a.freeze();
    return check trap insertElement(a, 4, j);
}

function isIdTwo(Employee e) returns boolean {
    return e.id == 2;
}

function insertElement(json[] jArr, int index, json val) returns boolean {
    jArr[index] = val;
    return true;
}

type Employee record {
    int id;
    string name;
    !...;
};

type DeptEmployee record {
    int id;
    string name;
    Dept dept;
    !...;
};

type Dept record {
    string code;
    string name;
};

type PersonObj object {
    string name;

    function __init(string name){
        self.name = name;
    }

    function getName() returns string {
        return self.name;
    }
};

type FreezeAllowedDepartment record {
    PersonObj|string head;
    (PersonObj|int)...;
};
