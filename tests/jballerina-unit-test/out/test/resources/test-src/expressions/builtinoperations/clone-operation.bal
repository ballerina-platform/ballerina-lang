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
public type Person record {|
    int id;
    string name;
    float salary;
    string...;
|};

type Employee record {
    int id;
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

public function cloneInt() returns [int, int, int] {
    int a = 10;
    int x = a.clone();
    int y = a.clone();
    a = 12;
    y = 13;
    return [a, x, y];
}

public function cloneFloat() returns [float, float, float] {
    float a = 10.01;
    float x = a.clone();
    float y = a.clone();
    a = 12.01;
    y = 13.01;
    return [a, x, y];
}

public function cloneByte() returns [byte, byte, byte] {
    byte a = 100;
    byte x = a.clone();
    byte y = a.clone();
    a = 234;
    y = 133;
    return [a, x, y];
}


public function cloneBoolean() returns [boolean, boolean, boolean] {
    boolean a = true;
    boolean x = a.clone();
    boolean y = a.clone();
    a = false;
    y = true;
    return [a, x, y];
}

public function cloneString() returns [string, string, string] {
    string a = "AAAA";
    string x = a.clone();
    string y = a.clone();
    a = "BBBB";
    y = "CCCC";
    return [a, x, y];
}

public function cloneXML() returns [xml, xml, xml] {
    xml a = xml `<root><name>Alex</name><id>123</id><age>21</age></root>`;
    xml newName = xml `<name>Charlos</name>`;
    xml newId = xml `<id>5000</id>`;
    xml x = a.clone();
    xml y = a.clone();
    a.removeChildren("name");
    a.appendChildren(newName);
    y.removeChildren("id");
    y.appendChildren(newId);
    return [a, x, y];
}

public function cloneMap() returns [map<any>, map<any>, map<any>] {

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

    return [a, x, y];
}

public function cloneTable() returns [table<Employee>, table<Employee>, table<Employee>] {

    Employee e1 = { id: 1, name: "Jane", salary: 300.50 };
    Employee e2 = { id: 2, name: "Anne", salary: 100.50 };
    Employee e3 = { id: 3, name: "John", salary: 400.50 };

    table<Employee> a = table {
        { key id, name, salary },
        [e1, e2]
    };
    table<Employee> x = a.clone();
    table<Employee> y = a.clone();
    checkpanic a.add(e3);
    checkpanic y.add(e3);
    return [a, x, y];
}

public function cloneJSON() returns [json, json, json] {
    map<json> a = {"name": "Alex", "age": 21, "id": 123, "otherData":[1, "EE", 12.3]};
    map<json> x = a.clone();
    map<json> y = a.clone();

    a["name"] = "Charlos";
    y["id"] = 5000;

    return [a, x, y];
}

public function cloneJSONArray() returns [json, json, json] {
    json[] a = [1, "EE", 12.3];
    json[] x = a.clone();
    json[] y = a.clone();
    a[0] = 100;
    y[2] = 300.5;
    return [a, x, y];
}

public function cloneIntArray() returns [int[], int[], int[]] {
    int[] a = [1, 2, 3];
    int[] x = a.clone();
    int[] y = a.clone();
    a[0] = 100;
    y[2] = 300;
    return [a, x, y];
}

public function cloneFloatArray() returns [float[], float[], float[]] {
    float[] a = [1.0, 2.0, 3.0];
    float[] x = a.clone();
    float[] y = a.clone();
    a[0] = 100.5;
    y[2] = 300.5;
    return [a, x, y];
}

public function cloneByteArray() returns [byte[], byte[], byte[]] {
    byte[] a = [1, 2, 3];
    byte[] x = a.clone();
    byte[] y = a.clone();
    a[0] = 100;
    y[2] = 234;
    return [a, x, y];
}

public function cloneStringArray() returns [string[], string[], string[]] {
    string[] a = ["A", "B", "C"];
    string[] x = a.clone();
    string[] y = a.clone();
    a[0] = "XX";
    y[2] = "YY";
    return [a, x, y];
}

public function cloneUnionArray() returns [any[], any[], any[]] {
    (int|string|float)?[] a = [1, "EE", 12.3];
    (int|string|float)?[] x = a.clone();
    (int|string|float)?[] y = a.clone();
    a[0] = 100;
    y[2] = 300.5;
    return [a, x, y];
}

public function cloneUnion() returns [any, any, any] {
    int|string|float a = 1;
    int|string|float x = a.clone();
    int|string|float y = a.clone();
    a = 100;
    y = 300.5;
    return [a, x, y];
}

public function cloneNilableInt() returns [any, any, any, any, any, any] {
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
    return [a, x, y, b, xx, yy];
}

public function cloneTuple() returns [[map<any>, int[]], [map<any>, int[]], [map<any>, int[]]] {
    map<int> m = {};
    m["one"] = 100;
    int[] arr = [200];
    [map<int>, int[]] a = [m, arr];
    [map<any>, int[]] x = a.clone();
    [map<any>, int[]] y = a.clone();
    a[1][0] = 400;
    y[1][0] = 500;
    return [a, x, y];
}

public function cloneArrayOfArrays() returns [int[][], int[][], int[][]] {
    int[][] a = [[200]];
    int[][] x = a.clone();
    int[][] y = a.clone();
    a[0][0] = 400;
    y[0][0] = 500;
    return [a, x, y];
}

function getIntValue(int[] val) returns int[] {
    int[] x = val;
    x[0] = x[0] * 2;
    return x;
}

function cloneDecimalValue() returns [any, any, any] {
    decimal a = 10.000;
    decimal b = a.clone();
    decimal c = a.clone();
    a = 20.0;
    c = 30.0;
    return [a, b, c];
}
function cloneDecimalArray() returns [any, any, any] {
    decimal[] a = [1, 2, 3, 4];
    decimal[] b = a.clone();
    decimal[] c = a.clone();
    a[2] = 30;
    c[2] = 300;
    return [a, b, c];
}

public function cloneReturnValues() returns [int[], int[], int[]] {
    int[] a = [10];
    int[] x = getIntValue(a).clone();
    int[] y = getIntValue(a).clone();
    a[0] = 100;
    y[0] = 1000;
    return [a, x, y];
}

public function cloneAnydataRecord() returns [Person, Person, Person] {
    Person a = {id: 100, name: "Alex", salary: 300.5};
    Person x = a.clone();
    Person y = a.clone();
    a.name = "Charlos";
    y.salary = 400.5;
    return [a, x, y];
}

public function cloneCyclicRecord() returns [any, any] {
    A a = { a: 10, arr: [1, 2, 3, 4] };
    B b = { b: 11, aa: a };
    C c = { b: b, a: a };
    C cc = c.clone();
    cc.a.arr[0] = 10;
    return [cc, c];
}

public function cloneCyclicArray() returns [any, any] {
    A[] arr = [];
    int[] x = [10, 20, 30, 40];
    arr[0] = { a: 1, arr: [1, 2, 3, 4] };
    arr[1] = { a: 2, arr: [1, 2, 3, 4, 5] };
    arr[2] = { a: 3, arr: x};
    arr[3] = { a: 4, arr: x};
    A[] copy = arr.clone();
    copy[2].arr[0] = 100;
    copy[2].arr[1] = 200;
    copy[2].arr[2] = 300;
    copy[2].arr[3] = 400;
    return [arr, copy];
}

public function cloneCyclicMapsArray() returns [any, any] {
    map<anydata> x = {};
    map<anydata> y = {};
    x["1"] = y;
    y["1"] = x;
    map<anydata>[] xx = [x, y];
    map<anydata>[] yy = xx.clone();
    return [xx, yy];
}

public function cloneAnydata() returns [any, any, any] {
    Person p = {id: 100, name: "Alex", salary: 300.5};
    anydata a = p;
    anydata x = a.clone();
    anydata y = a.clone();
    p.name = "Charlos";

    if (y is Person) {
        y.salary = 400.5;
    }
    return [a, x, y];
}

public function cloneFrozenAnydata() returns boolean {
    Person p = {id: 100, name: "Alex", salary: 300.5};
    Person q = p.cloneReadOnly();
    Person r = q.clone();
    return q === r;
}

public function cloneNullJson() returns [any, any] {
    json c = ();
    json x = c.clone();
    return [c, x];
}

public function cloneNilAnydata() returns [any, any] {
    anydata x = ();
    anydata y = x.clone();
    return [x, y];
}

type MyError error<string, record {| string message?; error cause?; string...; |}>;

string reason1 = "err reason 1";
string reason2 = "err reason 2";
string reason3 = "err reason 3";
string reason4 = "err reason 4";
string[*] reasonArray = [reason1, reason2, reason3, reason4];

error err1 = error(reason1);
error err2 = error(reason2, one = 1, two = "2");
MyError err3 = error(reason3, one = "first");
MyError err4 = error(reason4, one = "first", two = "second");

public function testCloneArrayWithError() returns boolean {
    error[*] errArray = [err1, err2, err3, err4];
    error[4] clonedErrArray = errArray.clone();

    boolean cloneSuccessful = errArray !== clonedErrArray;

    foreach int i in 0 ... 3 {
        cloneSuccessful = cloneSuccessful &&
                            errArray[i].reason() == clonedErrArray[i].reason() &&
                            errArray[i].reason() == reasonArray[i] &&
                            errArray[i].detail() === clonedErrArray[i].detail();
    }
    return cloneSuccessful;
}

public function testCloneMapWithError() returns boolean {
    map<error> errMap = {
        e1: err1,
        e2: err2,
        e3: err3,
        e4: err4
    };

    map<anydata> ma = {
        one: 1,
        two: "two",
        errMap: errMap
    };
    map<error> errMapFromValue = <map<error>> ma["errMap"];

    map<anydata> clonedMap = <map<anydata>> ma.clone();

    boolean cloneSuccessful = ma !== clonedMap && <int> ma["one"] == <int> clonedMap["one"] &&
                                <string> ma["two"] == <string> clonedMap["two"];

    map<error> clonedErrorMap = <map<error>> clonedMap["errMap"];
    foreach [string, error] [key, value] in errMapFromValue.entries() {
        cloneSuccessful = cloneSuccessful && value === clonedErrorMap[key];
    }
    return cloneSuccessful;
}
