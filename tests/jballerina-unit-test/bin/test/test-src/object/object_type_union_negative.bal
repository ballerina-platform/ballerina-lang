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

class Obj {
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

Obj4 aOb = new(55, 66, 77);

Obj|Obj2|Obj3|Obj4 a = new(5, 6, 7);
Obj|Obj2|Obj3|Obj4 ab = new(5); // ambiguous type '(Obj|Obj2|Obj3|Obj4)'
Obj|Obj2|Obj3|Obj4 ac = new(); // ambiguous type '(Obj|Obj2|Obj3|Obj4)'
Obj|Obj2|Obj3|Obj4 wrongDefaultableArgType = new(5, j="zero"); // cannot infer type of the object from '(Obj|Obj2|Obj3|Obj4)'

function getA() returns Obj|Obj2|Obj3|Obj4 {
    return a;
}

function getB() returns Obj|Obj2|Obj3|Obj4 {
    return ab;
}

function getLocals() returns [(Obj|Obj2|Obj3|Obj4),(Obj|Obj2|Obj3|Obj4)] {
    Obj|Obj2|Obj3|Obj4 localA = new(5, 6, 7);
    Obj|Obj2|Obj3|Obj4 localAB = new(5, j=0);
    return [localA, localAB];
}

class Foo {
    Bar? bar = ();

    function test() {
        string p = "John Doe";
        self.bar = new(p); // incompatible types: expected '(PersonRec|EmployeeRec)', found 'string'
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


class InitObjOne {

    public function init(int i, string f = "str") {

    }
}

class InitObjTwo {

    public function init(int i, boolean f = true) {

    }
}

class InitObjThree {

    public function init(int i, string s, int j = 10, string... k) {

    }
}

function testAmbiguousObjectTypes() {
    InitObjOne|InitObjTwo|float f1 = new(f = false, 2); // positional argument not allowed after named arguments
    InitObjOne|InitObjTwo|float f2 = new(1, false); // valid
    InitObjOne|InitObjTwo|float f3 = new(1, "str2"); // valid
    InitObjOne|InitObjTwo|float f4 = new(1, f = false); // valid
    InitObjOne|InitObjTwo|float f5 = new(1, f = "str2"); // valid
    InitObjOne|InitObjTwo|int f6 = new(); // cannot infer type of the object from '(InitObjOne|InitObjTwo|int)'
    InitObjOne|InitObjTwo|float f7 = new(1); // ambiguous type '(InitObjOne|InitObjTwo|float)'
    InitObjOne|InitObjTwo|float f8 = new(1, 1.1); // cannot infer type of the object from '(InitObjOne|InitObjTwo|float)'

    InitObjThree|boolean|string f9 = new(1, "s"); // valid
    InitObjThree|boolean|string f10 = new(1, "s", 12, "a", "b", "c"); // valid
    InitObjThree|boolean|string f11 = new(1, "s", j = 12); // valid
    InitObjThree|boolean|string f12 = new(1, "s", "a", "b", "c"); // incompatible types: expected 'int', found 'string'
    InitObjOne|InitObjThree|boolean|string f13 = new(1, "s", "a", "b", "c"); // cannot infer type of the object from '(InitObjOne|InitObjThree|boolean|string)'
    InitObjThree|boolean|string f14 = new(1, "s", j = 20, "a"); // positional argument not allowed after named arguments
    InitObjThree|InitObjOne|boolean|string f15 = new(1, "s", j = 20, "a", "b", "c"); // cannot infer type of the object from '(InitObjThree|InitObjOne|boolean|string)'
}
