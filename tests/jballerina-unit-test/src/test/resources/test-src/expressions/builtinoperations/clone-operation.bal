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

import ballerina/lang.'xml;
import ballerina/lang.value;
import ballerina/test;

type Person record {|
    int id;
    string name;
    float salary;
    string...;
|};

type Employee record {
    readonly int id;
    string name;
    float salary;
};

type ConstrainedEmp record {|
    int id;
    string name;
    float salary;
|};

type A record {
    int a;
    int[] arr;
};

type B record {
    int b;
    A aa;
};

type C record {
    B b;
    A a;
};

function cloneInt() {
    int a = 10;
    int x = a.clone();
    int y = a.clone();
    a = 12;
    y = 13;
    test:assertEquals(a, 12);
    test:assertEquals(x, 10);
    test:assertEquals(y, 13);
    test:assertTrue(a != x && a != y && x != y);
}

function cloneFloat() {
    float a = 10.01;
    float x = a.clone();
    float y = a.clone();
    a = 12.01;
    y = 13.01;
    test:assertEquals(a, 12.01);
    test:assertEquals(x, 10.01);
    test:assertEquals(y, 13.01);
    test:assertTrue(a != x && a != y && x != y);
}

function cloneByte() {
    byte a = 100;
    byte x = a.clone();
    byte y = a.clone();
    a = 234;
    y = 133;
    test:assertEquals(a, 234);
    test:assertEquals(x, 100);
    test:assertEquals(y, 133);
    test:assertTrue(a != x && a != y && x != y);
}

function cloneBoolean() {
    boolean a = true;
    boolean x = a.clone();
    boolean y = a.clone();
    a = false;
    y = true;
    test:assertEquals(a, false);
    test:assertEquals(x, true);
    test:assertEquals(y, true);
    test:assertTrue(a != x && a != y && x == y);
}

function cloneString() {
    string a = "AAAA";
    string x = a.clone();
    string y = a.clone();
    a = "BBBB";
    y = "CCCC";
    test:assertEquals(a, "BBBB");
    test:assertEquals(x, "AAAA");
    test:assertEquals(y, "CCCC");
    test:assertTrue(a != x && a != y && x != y);
}

function cloneXML() {
    'xml:Element a = <'xml:Element>xml `<root><name>Alex</name></root>`;
    xml newName = xml `<name>Charlos</name>`;

    'xml:Element x = <'xml:Element>a.clone();
    a.setChildren(newName);
    test:assertEquals(a, xml `<root><name>Charlos</name></root>`);
    test:assertEquals(x, xml `<root><name>Alex</name></root>`);
    test:assertTrue(a != x);
}

function cloneMap() {

    map<map<map<map<json>>>> a = {};
    map<map<map<json>>> b = {};
    map<map<json>> c = {};
    c["xxx"] = {"id": 123, "name": "Alex", "age": 21};
    b["yyy"] = c;
    a["zzz"] = b;

    map<map<map<map<json>>>> x = a.clone();
    map<map<map<map<json>>>> y = a.clone();

    a["zzz"]["yyy"]["xxx"]["name"] = "Charlos";
    y["zzz"]["yyy"]["xxx"]["id"] = 5000;

    test:assertEquals(a, {"zzz": {"yyy": {"xxx": {"id": 123, "name": "Charlos", "age": 21}}}});
    test:assertEquals(x, {"zzz": {"yyy": {"xxx": {"id": 123, "name": "Alex", "age": 21}}}});
    test:assertEquals(y, {"zzz": {"yyy": {"xxx": {"id": 5000, "name": "Alex", "age": 21}}}});
    test:assertTrue(a != x && a != y && x != y);
}

function cloneTable() {

    Employee e1 = {id: 1, name: "Jane", salary: 300.50};
    Employee e2 = {id: 2, name: "Anne", salary: 100.50};
    Employee e3 = {id: 3, name: "John", salary: 400.50};
    table<Employee> key(id) a = table [];
    a.add(e1);
    a.add(e2);
    table<Employee> key(id) x = a.clone();
    table<Employee> key(id) y = a.clone();
    a.add(e3);
    y.add(e3);

    test:assertEquals(a[1], {"id": 1, "name": "Jane", "salary": 300.5});
    test:assertEquals(a[2], {"id": 2, "name": "Anne", "salary": 100.50});
    test:assertEquals(a[3], {"id": 3, "name": "John", "salary": 400.50});
    test:assertEquals(x[1], {"id": 1, "name": "Jane", "salary": 300.5});
    test:assertEquals(x[2], {"id": 2, "name": "Anne", "salary": 100.50});
    test:assertEquals(y[1], {"id": 1, "name": "Jane", "salary": 300.5});
    test:assertEquals(y[2], {"id": 2, "name": "Anne", "salary": 100.50});
    test:assertEquals(y[3], {"id": 3, "name": "John", "salary": 400.50});
    test:assertTrue(a != x);
    test:assertTrue(a !== y);
    test:assertTrue(x != y);
}

function cloneJSON() {
    map<json> a = {"name": "Alex", "age": 21, "id": 123, "otherData": [1, "EE", 12.3]};
    map<json> x = a.clone();
    map<json> y = a.clone();

    a["name"] = "Charlos";
    y["id"] = 5000;

    test:assertEquals(a, {"name": "Charlos", "age": 21, "id": 123, "otherData": [1, "EE", 12.3]});
    test:assertEquals(x, {"name": "Alex", "age": 21, "id": 123, "otherData": [1, "EE", 12.3]});
    test:assertEquals(y, {"name": "Alex", "age": 21, "id": 5000, "otherData": [1, "EE", 12.3]});
    test:assertTrue(a != x && a != y && x != y);
}

function cloneJSONArray() {
    json[] a = [1, "EE", 12.3];
    json[] x = a.clone();
    json[] y = a.clone();
    a[0] = 100;
    y[2] = 300.5;

    test:assertEquals(a, [100, "EE", 12.3]);
    test:assertEquals(x, [1, "EE", 12.3]);
    test:assertEquals(y, [1, "EE", 300.5]);
    test:assertTrue(a != x && a != y && x != y);
}

function cloneIntArray() {
    int[] a = [1, 2, 3];
    int[] x = a.clone();
    int[] y = a.clone();
    a[0] = 100;
    y[2] = 300;

    test:assertEquals(a, [100, 2, 3]);
    test:assertEquals(x, [1, 2, 3]);
    test:assertEquals(y, [1, 2, 300]);
    test:assertTrue(a != x && a != y && x != y);
}

function cloneFloatArray() {
    float[] a = [1.0, 2.0, 3.0];
    float[] x = a.clone();
    float[] y = a.clone();
    a[0] = 100.5;
    y[2] = 300.5;

    test:assertEquals(a, [100.5, 2.0, 3.0]);
    test:assertEquals(x, [1.0, 2.0, 3.0]);
    test:assertEquals(y, [1.0, 2.0, 300.5]);
    test:assertTrue(a != x && a != y && x != y);
}

function cloneByteArray() {
    byte[] a = [1, 2, 3];
    byte[] x = a.clone();
    byte[] y = a.clone();
    a[0] = 100;
    y[2] = 234;

    test:assertEquals(a, [100, 2, 3]);
    test:assertEquals(x, [1, 2, 3]);
    test:assertEquals(y, [1, 2, 234]);
    test:assertTrue(a != x && a != y && x != y);
}

function cloneStringArray() {
    string[] a = ["A", "B", "C"];
    string[] x = a.clone();
    string[] y = a.clone();
    a[0] = "XX";
    y[2] = "YY";

    test:assertEquals(a, ["XX", "B", "C"]);
    test:assertEquals(x, ["A", "B", "C"]);
    test:assertEquals(y, ["A", "B", "YY"]);
    test:assertTrue(a != x && a != y && x != y);
}

function cloneUnionArray() {
    (int|string|float)?[] a = [1, "EE", 12.3];
    (int|string|float)?[] x = a.clone();
    (int|string|float)?[] y = a.clone();
    a[0] = 100;
    y[2] = 300.5;

    test:assertEquals(a, [100, "EE", 12.3]);
    test:assertEquals(x, [1, "EE", 12.3]);
    test:assertEquals(y, [1, "EE", 300.5]);
    test:assertTrue(a != x && a != y && x != y);
}

function cloneUnion() {
    int|string|float a = 1;
    int|string|float x = a.clone();
    int|string|float y = a.clone();
    a = 100;
    y = 300.5;

    test:assertEquals(a, 100);
    test:assertEquals(x, 1);
    test:assertEquals(y, 300.5);
    test:assertTrue(a != x && a != y && x != y);
}

function cloneNilableInt() {
    int? a = 10;
    int? b = ();
    int? x = a.clone();
    int? y = a.clone();
    int? xx = b.clone();
    int? yy = b.clone();
    a = 4;
    y = 5;
    b = 4;
    yy = 5;

    test:assertEquals(a, 4);
    test:assertEquals(x, 10);
    test:assertEquals(y, 5);
    test:assertEquals(b, 4);
    test:assertEquals(xx, ());
    test:assertEquals(yy, 5);
    test:assertTrue(a != x && a != y && x != y);
    test:assertTrue(b != yy);
}

function cloneTuple() {
    map<int> m = {};
    m["one"] = 100;
    int[] arr = [200];
    [map<int>, int[]] a = [m, arr];
    [map<any>, int[]] x = a.clone();
    [map<any>, int[]] y = a.clone();
    a[1][0] = 400;
    y[1][0] = 500;

    test:assertEquals(a[0], {"one": 100});
    test:assertEquals(a[1], [400]);

    test:assertTrue(x[0] == {"one": 100});
    test:assertEquals(x[1], [200]);

    test:assertTrue(y[0] == {"one": 100});
    test:assertEquals(y[1], [500]);
    test:assertTrue(a != x && a != y && x !== y);
}

function cloneArrayOfArrays() {
    int[][] a = [[200]];
    int[][] x = a.clone();
    int[][] y = a.clone();
    a[0][0] = 400;
    y[0][0] = 500;

    test:assertEquals(a, [[400]]);
    test:assertEquals(x, [[200]]);
    test:assertEquals(y, [[500]]);
    test:assertTrue(a != x && a != y && x != y);
}

function getIntValue(int[] val) returns int[] {
    int[] x = val;
    x[0] = x[0] * 2;
    return x;
}

function cloneDecimalValue() {
    decimal a = 10.000;
    decimal b = a.clone();
    decimal c = a.clone();
    a = 20.0;
    c = 30.0;
    test:assertEquals(a, <decimal>20.0);
    test:assertEquals(b, <decimal>10.000);
    test:assertEquals(c, <decimal>30.0);
    test:assertTrue(a != b && a != c && b != c);
}

function cloneDecimalArray() {
    decimal[] a = [1, 2, 3, 4];
    decimal[] b = a.clone();
    decimal[] c = a.clone();
    a[2] = 30;
    c[2] = 300;

    decimal[] result = [1, 2, 30, 4];
    test:assertEquals(a, result);

    result = [1, 2, 3, 4];
    test:assertEquals(b, result);

    result = [1, 2, 300, 4];
    test:assertEquals(c, result);
    test:assertTrue(a != b && a != c && b != c);
}

function cloneReturnValues() {
    int[] a = [10];
    int[] x = getIntValue(a).clone();
    int[] y = getIntValue(a).clone();
    a[0] = 100;
    y[0] = 1000;

    test:assertEquals(a, [100]);
    test:assertEquals(x, [20]);
    test:assertEquals(y, [1000]);
    test:assertTrue(a != x && a != y && x != y);
}

function cloneAnydataRecord() {
    Person a = {id: 100, name: "Alex", salary: 300.5};
    Person x = a.clone();
    Person y = a.clone();
    a.name = "Charlos";
    y.salary = 400.5;

    test:assertEquals(a, {id: 100, name: "Charlos", salary: 300.5});
    test:assertEquals(x, {id: 100, name: "Alex", salary: 300.5});
    test:assertEquals(y, {id: 100, name: "Alex", salary: 400.5});
    test:assertTrue(a != x && a != y && x != y);
}

function cloneCyclicRecord() returns [any, any] {
    A a = {a: 10, arr: [1, 2, 3, 4]};
    B b = {b: 11, aa: a};
    C c = {b: b, a: a};
    C cc = c.clone();
    cc.a.arr[0] = 10;
    return [cc, c];
}

function cloneCyclicArray() returns [any, any] {
    A[] arr = [];
    int[] x = [10, 20, 30, 40];
    arr[0] = {a: 1, arr: [1, 2, 3, 4]};
    arr[1] = {a: 2, arr: [1, 2, 3, 4, 5]};
    arr[2] = {a: 3, arr: x};
    arr[3] = {a: 4, arr: x};
    A[] copy = arr.clone();
    copy[2].arr[0] = 100;
    copy[2].arr[1] = 200;
    copy[2].arr[2] = 300;
    copy[2].arr[3] = 400;
    return [arr, copy];
}

function cloneCyclicMapsArray() returns [any, any] {
    map<anydata> x = {};
    map<anydata> y = {};
    x["1"] = y;
    y["1"] = x;
    map<anydata>[] xx = [x, y];
    map<anydata>[] yy = xx.clone();
    return [xx, yy];
}

function cloneAnydata() {
    Person p = {id: 100, name: "Alex", salary: 300.5};
    anydata a = p;
    anydata x = a.clone();
    anydata y = a.clone();
    p.name = "Charlos";

    if (y is Person) {
        y.salary = 400.5;
    }

    test:assertEquals(a, {id: 100, name: "Charlos", salary: 300.5});
    test:assertEquals(x, {id: 100, name: "Alex", salary: 300.5});
    test:assertEquals(y, {id: 100, name: "Alex", salary: 400.5});
    test:assertTrue(a != x && a != y && x != y);
}

type BddPath record {|
    int[] pos = [];
    int[] neg = [];
|};

function cloneRecordWithArrayField() {
    BddPath path = {};
    BddPath clone = path.clone();

    test:assertFalse(path.pos === path.neg);
    test:assertFalse(clone.pos === clone.neg);
    test:assertTrue(path.pos == path.neg);
    test:assertTrue(clone.pos == clone.neg);

    clone.pos.push(1);
    clone.pos.push(2);
    clone.pos.push(3);
    test:assertEquals(clone.pos, [1, 2, 3]);
    test:assertEquals(clone.neg, []);
    test:assertFalse(clone.pos == clone.neg);
}

type Rec record {
};

function cloneArrayWithRecordElement() {
    Rec[] arr = [{}, {}];
    Rec[] clone = arr.clone();

    test:assertFalse(arr[0] === arr[1]);
    test:assertFalse(clone[0] === clone[1]);
    test:assertTrue(arr[0] == arr[1]);
    test:assertTrue(clone[0] == clone[1]);

    clone[1] = {"a": "A"};
    test:assertEquals(clone[0], {});
    test:assertEquals(clone[1], {"a": "A"});
    test:assertFalse(clone[0] == clone[1]);
}

function cloneFrozenAnydata() {
    Person p = {id: 100, name: "Alex", salary: 300.5};
    Person q = p.cloneReadOnly();
    Person r = q.clone();
    test:assertTrue(q === r);
}

function cloneNullJson() {
    json c = ();
    json x = c.clone();
    test:assertEquals(c, ());
    test:assertEquals(x, ());
}

function cloneNilAnydata() {
    anydata x = ();
    anydata y = x.clone();
    test:assertEquals(x, ());
    test:assertEquals(y, ());
}

type MyError error<record {|string message?; error cause?; string one?; string two?;|}>;

string reason1 = "err reason 1";
string reason2 = "err reason 2";
string reason3 = "err reason 3";
string reason4 = "err reason 4";
string[*] reasonArray = [reason1, reason2, reason3, reason4];

error err1 = error(reason1);
error err2 = error(reason2, one = 1, two = "2");
MyError err3 = error MyError(reason3, one = "first");
MyError err4 = error MyError(reason4, one = "first", two = "second");

function testCloneArrayWithError() {
    error[*] errArray = [err1, err2, err3, err4];
    error[4] clonedErrArray = errArray.clone();

    boolean cloneSuccessful = errArray !== clonedErrArray;

    foreach int i in 0 ... 3 {
        cloneSuccessful = cloneSuccessful &&
                            errArray[i].message() == clonedErrArray[i].message() &&
                            errArray[i].message() == reasonArray[i] &&
                            errArray[i].detail() === clonedErrArray[i].detail();
    }
    test:assertTrue(cloneSuccessful);
}

function testCloneMapWithError() {
    map<error> errMap = {
        e1: err1,
        e2: err2,
        e3: err3,
        e4: err4
    };

    map<value:Cloneable> ma = {
        one: 1,
        two: "two",
        errMap: errMap
    };
    map<error> errMapFromValue = <map<error>>checkpanic ma["errMap"];

    map<value:Cloneable> clonedMap = ma.clone();

    boolean cloneSuccessful = ma !== clonedMap && <int>checkpanic ma["one"] == <int>checkpanic clonedMap["one"] &&
                                <string>checkpanic ma["two"] == <string>checkpanic clonedMap["two"];

    map<error> clonedErrorMap = <map<error>>checkpanic clonedMap["errMap"];
    foreach [string, error] [x, y] in errMapFromValue.entries() {
        cloneSuccessful = cloneSuccessful && y === clonedErrorMap[x];
    }
    test:assertTrue(cloneSuccessful);
}
