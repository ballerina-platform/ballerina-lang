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

type Obj object {
    int val;
    function __init(int i, int j = 0) {
        self.val = 0;
    }
};

type Obj2 object {
    int val;
    function __init() {
        self.val = 2;
    }
};

type Obj3 object {
    int val;
    function __init(int j = 0) {
        self.val = 3;
    }
};

type Obj4 object {
    int val;
    function __init(int i, int... restP) {
        self.val = 4;
    }
};

Obj4 aOb = new(55, 66, 77);

Obj|Obj2|Obj3|Obj4 a = new(5, 6, 7);
Obj|Obj2|Obj3|Obj4 ab = new(5);
Obj|Obj2|Obj3|Obj4 ac = new();
Obj|Obj2|Obj3|Obj4 wrongDefaultableArgType = new(5, j="zero");

function getA() returns Obj|Obj2|Obj3|Obj4 {
    return a;
}

function getB() returns Obj|Obj2|Obj3|Obj4 {
    return ab;
}

function getLocals() returns ((Obj|Obj2|Obj3|Obj4),(Obj|Obj2|Obj3|Obj4)) {
    Obj|Obj2|Obj3|Obj4 localA = new(5, 6, 7);
    Obj|Obj2|Obj3|Obj4 localAB = new(5, j=0);
    return (localA, localAB);
}

type Foo object {
    Bar? bar = ();

    function test() {
        string p = "John Doe";
        self.bar = new(p);
    }
};

type Bar object {
    PersonRec|EmployeeRec p;

    function __init(PersonRec|EmployeeRec p) {
        self.p = p;
    }
};

type PersonRec record {|
    string name;
|};

type EmployeeRec record {
    string name;
};
