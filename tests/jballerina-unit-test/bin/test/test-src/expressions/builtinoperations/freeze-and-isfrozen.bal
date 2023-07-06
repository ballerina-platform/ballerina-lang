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
import ballerina/lang.'xml as xmllib;
import ballerina/lang.value;

final string FREEZE_ERROR_OCCURRED = "error occurred on freeze: ";
final string FREEZE_SUCCESSFUL = "freeze successful";

function testFreezeOnNilTypedValue() {
    () n = ();
    _ = n.cloneReadOnly();
}

function testBooleanFreeze(boolean a) returns [boolean, boolean] {
    boolean b = a.cloneReadOnly();
    return [a == b, (a.isReadOnly() && b.isReadOnly())];
}

function testIntFreeze(int a) returns [boolean, boolean] {
    int b = a.cloneReadOnly();
    return [a == b, (a.isReadOnly() && b.isReadOnly())];
}

function testByteFreeze(byte a) returns [boolean, boolean] {
    byte b = a.cloneReadOnly();
    return [a == b, (a.isReadOnly() && b.isReadOnly())];
}

function testFloatFreeze(float a) returns [boolean, boolean] {
    float b = a.cloneReadOnly();
    return [a == b, (a.isReadOnly() && b.isReadOnly())];
}

function testDecimalFreeze(decimal a) returns [boolean, boolean] {
    decimal b = a.cloneReadOnly();
    return [a == b, (a.isReadOnly() && b.isReadOnly())];
}

function testStringFreeze(string a) returns [boolean, boolean] {
    string b = a.cloneReadOnly();
    return [a == b, (a.isReadOnly() && b.isReadOnly())];
}

enum Foo {
    X
}

type Bar record {
    Foo f;
};

function testRecordWithEnumFreeze() {
    Bar b = {f: X};
    anydata g = b;
    anydata readOnlyBar = g.cloneReadOnly();
    assertTrue(readOnlyBar.isReadOnly());
    assertTrue(readOnlyBar is Bar);
}

function testBasicTypeNullableUnionFreeze() returns [boolean, boolean] {
    int? i = 5;
    anydata j = i.cloneReadOnly();
    boolean 'equals = i == j;
    boolean isFrozen = i.isReadOnly() && j.isReadOnly();

    string? k = "hello world";
    string? l = k.cloneReadOnly();
    'equals = l == k;
    isFrozen = l.isReadOnly() && k.isReadOnly();

    float? f = -1.29;
    j = f.cloneReadOnly();
    'equals = f == j;
    isFrozen = f.isReadOnly() && j.isReadOnly();

    decimal dec = 123.9;
    decimal? d = dec;
    j = d.cloneReadOnly();
    'equals = d == j;
    isFrozen = d.isReadOnly() && j.isReadOnly();

    boolean? b = ();
    j = b.cloneReadOnly();
    'equals = d == b;
    isFrozen = d.isReadOnly() && b.isReadOnly();

    int|string? m = "hello ballerina";
    j = m.cloneReadOnly();
    'equals = m == j;
    isFrozen = m.isReadOnly() && j.isReadOnly();

    return ['equals, isFrozen];
}

function testBasicTypeUnionFreeze() returns [boolean, boolean] {
    int|string i = 5;
    anydata j = i.cloneReadOnly();
    boolean 'equals = i == j;
    boolean isFrozen = i.isReadOnly() && j.isReadOnly();

    boolean|float|int k = 123.2;
    boolean|float|int|decimal l = k.cloneReadOnly();
    'equals = l == k;
    isFrozen = k.isReadOnly() && l.isReadOnly();
    return ['equals, isFrozen];
}

function testBasicTypesAsJsonFreeze() returns boolean {
    json a = 5;
    json b = a.cloneReadOnly();
    boolean 'equals = a == b;

    a = 5.1;
    b = a.cloneReadOnly();
    'equals = 'equals && a == b;

    a = "Hello from Ballerina";
    b = a.cloneReadOnly();
    'equals = 'equals && a == b;

    a = true;
    b = a.cloneReadOnly();
    return 'equals && a == b;
}

function testIsFrozenOnStructuralTypes() returns [boolean, boolean]|error {
    Employee e = { id: 0, name: "Em" };
    map<string|int|()|Employee> m = { one: "1", two: 2, three: (), rec: e };

    anydata[] a = [1, "hi", 2.0, false, m, (), e];
    [int, string, Employee] t = [1, "Em", e];

    json j = { name: "Em", dataType: "json" };
    xml x = xml `<bookItem>The Lost World</bookItem>`;

    //table<Employee> empTable = table {
    //    { key id, name },
    //    [
    //        { 1, "Mary" },
    //        { 2, "John" },
    //        { 3, "Jim" }
    //    ]
    //};

    byte byteVal = 255;
    map<anydata> m1 = { intVal: 1, byteVal: byteVal, floatVal: 200.1, stringVal: "Ballerina says freeze",
        booleanVal: false, arrayVal: a, mapVal: m, tupleVal: t, jsonVal: j, xmlVal: x };

    boolean isFrozenBeforeFreeze = m.isReadOnly() || a.isReadOnly() || m1.isReadOnly() || e.isReadOnly() || t.isReadOnly() ||
                                    j.isReadOnly() || x.isReadOnly();

    map<anydata> m2 = m1.cloneReadOnly();
    map<anydata> m3 = <map<anydata>>m2["mapVal"];
    boolean isFrozenAfterFreeze = m3.isReadOnly() && m2["arrayVal"].isReadOnly() && m2.isReadOnly() &&
                                   m3["rec"].isReadOnly() && m2["tupleVal"].isReadOnly() &&
                                   m2["jsonVal"].isReadOnly() && m2["xmlVal"].isReadOnly() ;
    return [isFrozenBeforeFreeze, isFrozenAfterFreeze];
}

function testFrozenIntArrayModification() {
    int[] a = [1, 2, 3];
    int[] b = a.cloneReadOnly();
    b[4] = 200;
}

function testFrozenBooleanArrayModification() {
    boolean[] a = [false, true];
    boolean[] b = a.cloneReadOnly();
    b[1] = true;
}

function testFrozenByteArrayModification() {
    byte[] a = [1, 2, 3];
    byte[] b = a.cloneReadOnly();
    b[5] = 200;
}

function testFrozenDecimalArrayModification() {
    decimal[4] d = [1.0, 0.2, 310.0, 22222.3];
    decimal[4] d2 = d.cloneReadOnly();
    d2[3] = 200.23;
}

function testFrozenFloatArrayModification() {
    float[] a = [1.0, 0.2, 300.23];
    float[] b = a.cloneReadOnly();
    b[1] = 200.23;
}

function testFrozenStringArrayModification() {
    string[] a = ["ballerina", "2", "hello world"];
    string[] b = a.cloneReadOnly();
    b[1] = "addition to frozen array";
}

function testFrozenJsonArrayModification() {
    json j = { hello: "world "};
    json[] a = [j, "ballerina", 2, 10.3];
    json[] b = a.cloneReadOnly();
    b[8] = j;
}

map<json> j1 = { name: "Em", dataType: "json", pos: "inner" };
map<json> j2 = { name: "Zee", dataType: "json", pos: "outer", innerJson: j1 };

function testFrozenJsonModification() {
    map<json> j3 = j2.cloneReadOnly();
    j3["name"] = "Em";
}

function testAdditionToFrozenJson() {
    map<json> j3 = j2.cloneReadOnly();
    j3["newField"] = "Em";
}

function testRemovalFromFrozenJson() {
    map<json> j3 = j2.cloneReadOnly();
    _ = j3.remove("name");
}

function testFrozenInnerJsonModification() {
    map<json> j4 = j2.cloneReadOnly();
    map<json> j5 = <map<json>>j4["innerJson"];
    j5["name"] = "Em";
}

function testAdditionToFrozenInnerJson() {
    map<json> j4  = j2.cloneReadOnly();
    map<json> j5 =  <map<json>>j4["innerJson"];
    j5["newField"] = "Em";
}

function testRemovalFromFrozenInnerJson() {
    map<json> j3 = j2.cloneReadOnly();
    map<json> j4 = <map<json>>j3["innerJson"];
    _ = j4.remove("name");
}

function testFrozenXmlSetChildren() {
    xml x1 = xml `<book>The Lost World</book>`;
    xml x2 = xml `<author>Doyle</author>`;

    xmllib:Element x3 = <xmllib:Element & readonly> x1.cloneReadOnly();
    x3.setChildren(x2);
}

function testFrozenXmlSetChildrenDeep() {
    xml x1 = xml `<book><name>The Lost World</name><authors></authors></book>`;
    xml x2 = xml `<author>Doyle</author>`;

    xmllib:Element x3 = <xmllib:Element & readonly> x1.cloneReadOnly();
    xml author = x3.getChildren().strip()[1];
    xmllib:Element authorEm = <xmllib:Element> author;
    authorEm.setChildren(x2);
}

function testXMLItemsCloneReadOnly() {
    xml x0 = xmllib:concat(xml `<hello>world</hello>`,
                        xml `<!-- comment text -->`,
                        xml `<?PIT data?>`,
                        xml `<item><child>String Content <sub></sub>More Str</child><child></child></item>`);

    assertFalse((x0.<hello>).isReadOnly());
    assertTrue((x0.<hello>/*).isReadOnly()); // Sequence containing text item
    assertFalse(x0[1].isReadOnly());
    assertFalse(x0[2].isReadOnly());
    assertTrue((x0/**/<child>/*)[0].isReadOnly()); // Text item

    xml x1 = x0.cloneReadOnly();
    assertTrue((x1.<hello>).isReadOnly());
    assertTrue((x1.<hello>/*).isReadOnly());
    assertTrue(x1[1].isReadOnly());
    assertTrue(x1[2].isReadOnly());
    assertTrue((x1/**/<child>/*)[0].isReadOnly());
}

function testFrozenMapUpdate() {
    map<anydata> m1 = { one: "1", two: 2 };
    map<anydata> m2 = { one: "21", two: 22, mapVal: m1 };

    map<anydata> m3 = m2.cloneReadOnly();
    m3["one"] = 22;
}

function testFrozenMapRemoval() {
    map<anydata> m1 = { one: "1", two: 2 };
    map<anydata> m2 = { one: "21", two: 22, mapVal: m1 };
    map<anydata> m3 = m2.cloneReadOnly();
    _ = m3.remove("one");
}

function testFrozenMapClear() {
    map<anydata> m1 = { one: "1", two: 2 };
    map<anydata> m2 =  m1.cloneReadOnly();
    m2.removeAll();
}

function testFrozenInnerMapUpdate() {
    map<anydata> m1 = { one: "1", two: 2 };
    map<anydata> m2 = { one: "21", two: 22, mapVal: m1 };

    map<anydata> m3 = m2.cloneReadOnly();
    m3["one"] = 12;
}

function testFrozenInnerMapRemoval() returns error? {
    map<anydata> m1 = { one: "1", two: 2 };
    map<anydata> m2 = { one: "21", two: 22, mapVal: m1 };

    map<anydata> m3 =  m2.cloneReadOnly();
    map<anydata> m4 = check trap <map<anydata>> m3["mapVal"];
    _ = m4.remove("one");
    return ();
}

function testFrozenInnerMapClear() {
    map<anydata> m1 = { one: "1", two: 2 };
    map<anydata> m2 = { one: "21", two: 22, mapVal: m1 };

    map<anydata> m3 = m2.cloneReadOnly();
    map<anydata> m4 = <map<anydata>> m3["mapVal"];
    m4.removeAll();
}

function testFrozen2DBasicTypedArrayUpdate() {
    int[][] i1 = [[1, 2], [3]];
    int[][] i2 = i1.cloneReadOnly();
    i2[0][0] = 9;
}

function testFrozenAnyArrayAddition() {
    Employee e1 = { name: "Em", id: 1000 };
    int[] i = [1, 2];
    anydata[] i1 = [i, e1];
    anydata[] i2 = i1.cloneReadOnly();
    i2[3] = "freeze addition";
}

function testFrozenAnyArrayUpdate() returns error? {
    Employee e1 = { name: "Em", id: 1000 };
    int[] i = [1, 2];
    anydata[] i1 = [i, e1];
    anydata[] i2 = i1.cloneReadOnly();
    Employee e2 = check trap <Employee> i2[1];
    i2[1] = 100;
    return ();
}

function testFrozenAnyArrayElementUpdate() returns error? {
    Employee e1 = { name: "Em", id: 1000 };
    int[] i = [1, 2];
    anydata[] i1 = [i, e1];
    anydata[] i2 = i1.cloneReadOnly();
    Employee e2 = check trap <Employee> i2[1];
    e2["name"] = "Zee";
    return ();
}

function testFrozenTupleUpdate() {
    Employee e1 = { name: "Em", id: 1000 };
    [int, Employee] t1 = [1, e1];
    [int, Employee] t2 = t1.cloneReadOnly();
    Employee e2 = { name: "Zee", id: 1200 };
    t2[1] = e2;
}

type A [int, string|xml, boolean, A...];
type B [int, boolean, B[]...];

function cloneReadonlyTupleNegative(A tupleTemp1) returns A {
    tupleTemp1[2] = true;
    return tupleTemp1;
}

function testFrozenRecursiveTupleUpdate() {
    A tupleTest1 = [1, "text"];
    A tupleTemp1 = tupleTest1.cloneReadOnly();
    error|A cloneErr = trap cloneReadonlyTupleNegative(tupleTemp1);
    assertTrue(cloneErr is error);
    error err = <error> cloneErr;
    assertTrue(err.message() == "{ballerina/lang.array}InvalidUpdate");
}

function testRecursiveTupleFreeze() {
    A tupleTest1 = [1, ""];
    A tupleTemp1 = tupleTest1.cloneReadOnly();
    assertTrue(tupleTemp1.isReadOnly());
    assertFalse(tupleTest1.isReadOnly());

    B tupleTest2 = [1];
    B tupleTemp2 = tupleTest2.cloneReadOnly();
    assertTrue(tupleTemp2.isReadOnly());
    assertFalse(tupleTest2.isReadOnly());
}

function testFrozenRecordUpdate() {
    Dept d1 = { code: "fe11", name: "finance" };
    DeptEmployee e1 = { name: "Em", id: 1000, dept: d1 };
    DeptEmployee e3 = e1.cloneReadOnly();
    e3.name = "Zee";
}

function testFrozenInnerRecordUpdate() {
    Dept d1 = { code: "fe11", name: "finance" };
    DeptEmployee e1 = { name: "Em", id: 1000, dept: d1 };
    DeptEmployee e2 = e1.cloneReadOnly();
    Dept d2 = e2.dept;
    d2.code = "fe12";
}

function testFrozenTableAddition() {
    table<Employee1> key(id) empTable = table [
            { id: 1, name: "Mary" },
            { id: 2, name: "John" },
            { id: 3, name: "Jim" }
        ];
    Employee1 e = { id: 5, name: "Anne" };
    table<Employee1> key(id) empTable2  = empTable.cloneReadOnly();
    empTable2.add(e);
}

function testFrozenTableRemoval() {
    table<Employee1> key(id) empTable = table [
            { id: 1, name: "Mary" },
            { id: 2, name: "John" },
            { id: 3, name: "Jim" }
        ];
    table<Employee1> key(id) empTable2 = empTable.cloneReadOnly();
    _ = empTable2.remove(2);
}

function testSimpleUnionFreeze() returns boolean {
    int|string u1 = "hello world";

    int|string u2 = u1.cloneReadOnly();
    return u1.isReadOnly() && u2.isReadOnly();
}


function testValidComplexMapFreeze() returns [string, boolean] {
    map<string|FreezeAllowedDepartment> m1 = {};

    m1["one"] = "one";
    m1["two"] = "2";

    map<string|FreezeAllowedDepartment> res = m1.cloneReadOnly();
    return [FREEZE_SUCCESSFUL, (res.isReadOnly() && !m1.isReadOnly())];
}

function testValidComplexArrayFreeze() returns [string, boolean] {
    (string|float|FreezeAllowedDepartment)?[] a1 = [];

    a1[0] = 2.0;
    a1[1] = "hello world";

    (string|float|FreezeAllowedDepartment)?[] res = a1.cloneReadOnly();
    string errorOrSuccessMsg = FREEZE_SUCCESSFUL;
    return [errorOrSuccessMsg, (res.isReadOnly() && !a1.isReadOnly())];
}

function testValidComplexRecordFreeze() returns [string, boolean] {
    FreezeAllowedDepartment fd = { head: "John", "e1": 234, "e2": 10 };

    anydata res = fd.cloneReadOnly();
    return [FREEZE_SUCCESSFUL, (res.isReadOnly() && !fd.isReadOnly())];
}

function testValidComplexTupleFreeze() returns [string, boolean] {
    [int, string|FreezeAllowedDepartment|float, boolean] t1 = [1, 3.0, true];

    [int, string|FreezeAllowedDepartment|float, boolean] res = t1.cloneReadOnly();
    return [FREEZE_SUCCESSFUL, (res.isReadOnly() && !t1.isReadOnly())];
}

function testValidComplexUnionFreeze() returns [string, boolean] {
    Dept d = { code: "FN101", name: "Finance" };
    int|Dept|FreezeAllowedDepartment u1 = d;

    int|Dept|FreezeAllowedDepartment res = u1.cloneReadOnly();
    return [FREEZE_SUCCESSFUL, (res.isReadOnly() && !u1.isReadOnly())];
}

function testValidSelfReferencingValueFreeze() returns [string, boolean] {
    map<anydata> m = { one: 1 };
    map<anydata> m2 = { two: 2 };
    m["m2"] = m2;
    m2["m"] = m;

    map<anydata> res = m.cloneReadOnly();
    return [FREEZE_SUCCESSFUL, !m.isReadOnly() && res.isReadOnly()];
}

function testStructureWithErrorValueFreeze() returns boolean {
    string errReason = "test error";
    error e = error(errReason);
    map<anydata|error> m = { err: e };

    value:Cloneable res = m.cloneReadOnly();
    //todo check `res is readonly` once #23691 get fixed
    //return res is readonly && res["err"] === e;
    return res is map<value:Cloneable> && res["err"] === e;
}

function testFrozenValueUpdatePanicWithCheckTrap() returns boolean|error {
    json j = { hello: "world "};
    json[] a = [j, "ballerina", 2, 10.3];
    json[] b = a.cloneReadOnly();
    return check trap insertElement(b, 4, j);
}

function isIdTwo(Employee e) returns boolean {
    return e.id == 2;
}

function insertElement(json[] jArr, int index, json val) returns boolean {
    jArr[index] = val;
    return true;
}

type Employee record {|
    int id;
    string name;
|};

type Employee1 record {
    readonly int id;
    string name;
};

type DeptEmployee record {|
    int id;
    string name;
    Dept dept;
|};

type Dept record {
    string code;
    string name;
};

type FreezeAllowedDepartment record {|
    string head;
    (int)...;
|};

function assertTrue(boolean value) {
    if !(value) {
        error e = error("AssertionError", message = "expected: true, found: " + value.toString());
        panic e;
    }
}

function assertFalse(boolean value) {
    if (value) {
        error e = error("AssertionError", message = "expected: false, found: " + value.toString());
        panic e;
    }
}
