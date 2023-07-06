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

class Obj0 {
    int val;
    function init(int i, int j = 0) {
        self.val = 0;
    }
}

class Obj2 {
    int val;
    function init() {
        self.val = 2;
    }
}

class Obj3 {
    int val;
    function init(int j = 0) {
        self.val = 3;
    }
}

class Obj4 {
    int val;
    function init(int i, int... restP) {
        self.val = 4;
    }
}

class Obj5 {
    int val;
    function init(int i, string... restP) {
        self.val = 5;
    }
}

Obj0 zero = new(5, j=0);
Obj0|Obj2|Obj3|Obj4 obj0Instance = new(5, j=0);
Obj0|Obj2|Obj3|Obj4 obj4Instance = new(5, 6, 7);

function getObj4() returns Obj0|Obj2|Obj3|Obj4 {
    return obj4Instance;
}

function getObj0() returns Obj0|Obj2|Obj3|Obj4 {
    return obj0Instance;
}

function getLocals() returns [(Obj0|Obj2|Obj3|Obj4), (Obj0|Obj2|Obj3|Obj4), (Obj0|Obj2|Obj3|Obj4)] {
    Obj0|Obj2|Obj3|Obj4 localObj4 = new(5, 6, 7);
    Obj0|Obj2|Obj3|Obj4 localObj0 = new(5, j=0);
    Obj0|Obj2|Obj3|Obj4 localObj3 = new(j=2);
    return [localObj4, localObj0, localObj3];
}

function getMixedUnionMembers() returns (Obj0|Obj2|Obj3|Obj4|int) {
    //Obj0 o = new (5, j=0);
    Obj0|Obj2|Obj3|Obj4|int item = new (5, j=0);
    return item;
}

class Person {
    public int age = 0;

    function init (int age) {
        self.age = age;
    }
}

class Employee {
    public int age = 0;

    function init (int age, int addVal) {
        self.age = age + addVal;
    }
}

function returnDifferentObectInit1() returns Person | () {
    return new(5);
}

function returnDifferentObectInit2() {
    Person | () person = new(5);
    Person? otherOptionalPerson = new(6);
}

Obj0?[] objAr = [new(1), new(2)];

function selectOnRestParam() returns Obj4|Obj5 {
    Obj4|Obj5 localObj5 = new(0, "eka", "deka");
    return localObj5;
}

function selectOnRestParamInReturnType() returns Obj4|Obj5 {
    return new(0, "eka", "deka");
}

class Foo {
    Bar? bar = ();

    function test() {
        PersonRec p = {name: "John Doe"};
        self.bar = new(p);
    }
}

class Bar {
    PersonRec|EmployeeRec p;

    function init(PersonRec|EmployeeRec p) {
        self.p = p;
    }
}

type PersonRec record {|
    string name;
|};

type EmployeeRec record {
    string name;
};

function testUnionsAsAnInitParam() returns Foo {
    Foo f = new;
    f.test();
    return f;
}
