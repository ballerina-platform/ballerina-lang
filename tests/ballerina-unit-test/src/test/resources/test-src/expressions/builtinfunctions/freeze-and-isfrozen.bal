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

function testBooleanFreeze(boolean a) returns boolean {
    boolean b = a.freeze();
    //return a === b;
    return a == b;
}

function testIntFreeze(int a) returns boolean {
    int b = a.freeze();
    return a == b;
}

function testByteFreeze(byte a) returns boolean {
    byte b = a.freeze();
    return a == b;
}

function testFloatFreeze(float a) returns boolean {
    float b = a.freeze();
    return a == b;
}

function testStringFreeze(string a) returns boolean {
    string b = a.freeze();
    return a == b;
}

function testBasicTypesAsJson() returns boolean {
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

function testIsFrozenOnStructuralTypes() returns (boolean, boolean) {
    Employee e = { name: "Em" };
    map m = { one: "1", two: 2, three: (), rec: e };

    any[] a = [1, "hi", 2.0, false, m, (), e];
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
    map m1 = { intVal: 1, byteVal: byteVal, floatVal: 200.1, stringVal: "Ballerina says freeze", booleanVal: false,
                arrayVal: a, mapVal: m, tupleVal: t, jsonVal: j, xmlVal: x, tableVal: empTable };
    boolean isFrozenBeforeFreeze = m.isFrozen() || a.isFrozen() || m1.isFrozen() || e.isFrozen() || t.isFrozen() ||
                                    j.isFrozen() || x.isFrozen() || empTable.isFrozen();

    map m2 = m1.freeze();
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
    map m = { attr1: "one", attr2: "two"};
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
    map m1 = { one: "1", two: 2 };
    map m2 = { one: "21", two: 22, mapVal: m1 };

    _ = m2.freeze();
    m2.one = 22;
}

function testFrozenMapRemoval() {
    map m1 = { one: "1", two: 2 };
    map m2 = { one: "21", two: 22, mapVal: m1 };

    _ = m2.freeze();
    _ = m2.remove("one");
}

function testFrozenMapClear() {
    map m1 = { one: "1", two: 2 };

    _ = m1.freeze();
    m1.clear();
}

function testFrozenInnerMapUpdate() {
    map m1 = { one: "1", two: 2 };
    map m2 = { one: "21", two: 22, mapVal: m1 };

    _ = m2.freeze();
    m1["one"] = 12;
}

function testFrozenInnerMapRemoval() {
    map m1 = { one: "1", two: 2 };
    map m2 = { one: "21", two: 22, mapVal: m1 };

    _ = m2.freeze();
    map m3 = check <map> m2.mapVal;
    _ = m3.remove("one");
}

function testFrozenInnerMapClear() {
    map m1 = { one: "1", two: 2 };
    map m2 = { one: "21", two: 22, mapVal: m1 };

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

function testFrozenAnyArrayUpdate() {
    Employee e1 = { name: "Em", id: 1000 };
    int[] i = [1, 2];
    any[] i1 = [i, e1];
    _ = i1.freeze();
    Employee e2 = check <Employee> i1[1];
    i1[1] = 100;
}

function testFrozenAnyArrayElementUpdate() {
    Employee e1 = { name: "Em", id: 1000 };
    int[] i = [1, 2];
    any[] i1 = [i, e1];
    _ = i1.freeze();
    Employee e2 = check <Employee> i1[1];
    e2["name"] = "Zee";
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

function isIdTwo(Employee e) returns boolean {
    return e.id == 2;
}

type Employee record {
    int id;
    string name;
};

type DeptEmployee record {
    int id;
    string name;
    Dept dept;
};

type Dept record {
    string code;
    string name;

};
