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

function checkEqualityOfTwoTypes() returns boolean {
    int a = 0;
    string b = "";
    return a == b && !(a != b);
}

function checkEqualityOfArraysOfDifferentTypes() returns boolean {
    int[2] a = [0, 0];
    string[2] b = ["", ""];
    boolean bool1 = a == b && !(a != b);

    (float|int)?[] c = [];
    (boolean|xml)?[] d = [];
    boolean bool2 = c == d && !(c != d);

    return bool1 && bool2;
}

function checkEqualityOfMapsOfIncompatibleConstraintTypes() returns boolean {
    map<int> a = {};
    map<float> b = {};
    boolean bool1 = a == b && !(a != b);

    map<string|int> c = {};
    map<float> d = {};
    boolean bool2 = c == d && !(c != d);

    return bool1 && bool2;
}

function checkEqualityOfTuplesOfDifferentTypes() returns boolean {
    [string, int] a = ["", 0];
    [boolean, float] b = [false, 0.0];
    boolean bool1 = a == b && !(a != b);

    [float|int, int] c = [0, 0];
    [boolean, int] d = [false, 0];
    boolean bool2 = c == d && !(c != d);

    return bool1 && bool2;
}

function checkEqualityOfRecordsOfIncompatibleTypes() returns boolean {
    Employee e = { name: "Maryam" };
    Person p = { name: "Maryam" };
    boolean b = e == p && !(e != p);

    EmployeeWithOptionalId e1 = { name: "Maryam" };
    PersonWithOptionalId p1 = { name: "Maryam" };
    return b && e1 == p1 && !(e1 != p1);
}

function checkEqualityWithJsonRecordMapForIncompatibleType() returns boolean {
    json a = { name: "Em" };
    map<boolean> b = {};
    boolean 'equals = a == b && !(b != a);

    ClosedDept c = { code: "FN101" };
    return 'equals && b == c && !(c != b) && c == a && !(a != c);
}

function testArrayTupleEqualityOfIncompatibleTypes() returns boolean {
    int[] a = [1, 2];
    [float, float] b = [1.0, 2.0];

    boolean 'equals = a == b && !(a != b);

    [int, float] c = [1, 2.0];
    return 'equals && a == c && !(c != a);

    // Uncomment once closed list comparison is fixed
    //Employee e = { name: "Em", id: 1234 };
    //(Employee|int)[3] d = [e, 2, 3];
    //(Employee, int) f = (e, 2);
    //
    //return 'equals && f == d && !(d != f);
}

//function testEqualityWithTable() {
//    table<ClosedDept> t1 = table{};
//    table<ClosedDept> t2 = table{};
//    table<EmployeeWithOptionalId> t3 = table{};
//
//    table<ClosedDept>|map<string> t4 = t1;
//    table<ClosedDept>|map<string> t5 = t2;
//
//    boolean b = t1 == t2;
//    b = t4 != t5;
//}

type Employee record {
    string name;
    int id = 0;
};

type Person record {
    string name;
    string id = "";
};

type ClosedDept record {|
    string code;
|};

type EmployeeWithOptionalId record {|
    string name;
    float id?;
|};

type PersonWithOptionalId record {|
    string name;
    string id?;
|};

class Foo {
    string s = "";
}

function refAndNilEqualityCheck() {
    Employee emp = {name: "John", id: 1};

    if (emp == ()) {
        // do nothing
    }

    Foo f = new;

    if (f == ()) {
        // do nothing
    }

    var func = function () returns string { return "foobar"; };

    if (func == ()) {
        // do nothing
    }
}

function readonlyEquality() returns boolean {
    map<int> & readonly immutableMarks = {
        math: 80,
        physics: 85,
        chemistry: 75
    };
    readonly readonlyMarks = immutableMarks;

    map<int> marks = {
        math: 80,
        physics: 85,
        chemistry: 75
    };
    return readonlyMarks != marks;
}

type MyObject object {
    int i;
};

function testEqualityWithNonAnydataType() returns boolean {
    map<int> s = {};
    [int, map<int>] a = [1, {}];
    [int, float] b = [3, 23.9];
    boolean 'equals = a == b && !(b != a);

    MyObject obj = object {int i = 10;};
    'equals = obj == () && !(obj != ());
    MyObject obj2 = object {int i = 10;};
    'equals = obj == obj2 && !(obj != obj2);
}
