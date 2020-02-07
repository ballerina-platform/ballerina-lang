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

import eq;
import eq2;
import req;

public type person1 object {
    public int age = 0;
    public string name = "";
    public string address = "";
    string zipcode = "95134";
    string ssn = "";
    int id = 0;

    public function __init () {}

    public function getName () returns string {
        return self.name;
    }

    public function getAge () returns int {
        return self.age;
    }

    function getSSN () returns string {
        return self.ssn;
    }

    function setSSN (string s) {
        self.ssn = s;
    }
};

public type employee1 object {
    public int age = 0;
    public string name = "";
    public string address = "";
    public int employeeId = 123456;
    string zipcode = "95134";
    string ssn = "";
    int id = 0;

    public function __init (int age, string name) {
        self.age = age;
        self.name = name;
    }

    public function getName () returns string {
        return self.name;
    }

    public function getAge () returns int {
        return self.age;
    }

    function getSSN () returns string {
        return self.ssn + ":employee";
    }

    function setSSN (string s) {
        self.ssn = s;
    }

    public function getEmployeeId () returns int {
        return self.employeeId;
    }
};

function testObjectEquivalenceWhenFieldsHaveModuleVisibility() returns string {
    employee1 e = new (14, "rat");
    e.setSSN("234-56-7890");

    person1 p = e;

    return p.getSSN();
}

public type person2 object {
    public int age = 0;
    public string name = "";
    public string address = "";
    public string zipcode = "95134";
    public string ssn = "";
    public int id = 0;

    public function getName () returns string {
        return self.name;
    }

    public function getAge () returns int {
        return self.age;
    }

    public function getSSN () returns string {
        return self.ssn;
    }

    public function setSSN (string s) {
        self.ssn = s;
    }
};

public type employee2 object {
    public int age = 0;
    public string name = "";
    public string address = "";
    public string zipcode = "95134";
    public string ssn = "";
    public int id = 0;
    public int employeeId = 123456;

    public function getName () returns string {
        return self.name;
    }

    public function getAge () returns int {
        return self.age;
    }

    public function getSSN () returns string {
        return self.ssn + ":employee";
    }

    public function setSSN (string s) {
        self.ssn = s;
    }

    public function getEmployeeId () returns int {
        return self.employeeId;
    }
};

function testObjectEquivalenceWhenFieldsHavePublicVisibility() returns string {
    employee2 e = new;
    e.age = 14;
    e.name = "rat";
    e.setSSN("234-56-7890");

    person2 p = e;

    return p.getSSN();
}


function testEqOfPublicObjectsInBalo() returns string {
    eq:employee e = new (14, "rat");
    e.setSSN("234-56-7890");

    eq:person p = e;

    return p.getSSN();
}


public type employee3 object {
    public int age = 0;
    public string name = "";
    public string address = "";
    public string zipcode = "95134";
    public string ssn = "";
    public int id = 0;
    public int employeeId = 123456;

    public function getName () returns string {
        return self.name;
    }

    public function getAge () returns int {
        return self.age;
    }

    public function getSSN () returns string {
        return self.ssn + ":employee";
    }

    public function setSSN (string s) {
        self.ssn = s;
    }

    public function getEmployeeId () returns int {
        return self.employeeId;
    }
};

function testEqOfPublicObjects() returns string {
    employee3 e = new;
    e.age = 14;
    e.name = "rat";
    e.setSSN("234-56-1234");

    eq:person p = e;

    return p.getSSN();
}

function testEqOfPublicObjects2() returns string {
    eq2:employee e = new;
    e.age = 14;
    e.name = "rat";
    e.setSSN("234-56-3345");

    eq:person p = e;

    return p.getSSN();
}




type userA object {
    public int age = 0;
    public string name = "";
    public string address = "";

    function getName () returns string {
        return self.name;
    }

    function getAge () returns int {
        return self.age;
    }
};

type userB object {
    public int age = 0;
    public string name = "";
    public string address = "";

    function getName () returns string {
        return self.name;
    }

    function getAge () returns int {
        return self.age;
    }
};

type userFoo object {
    public int age = 0;
    public string name = "";
    public string address = "";
    public string zipcode = "23468";

    function getName () returns string {
        return self.name;
    }

    function getAge () returns int {
        return self.age;
    }
};


function testNonPublicTypedescEq() returns string|error {
    userFoo uFoo = new;
    uFoo.age = 10;
    uFoo.name = "ttt";
    uFoo.address = "102 Skyhigh street #129, San Jose";

    // This is a safe assignment
    userA uA = uFoo;

    userB uB = uA;
    return uB.name;
}


public type userPA object {
    public int age = 0;
    public string name = "";
    public string address = "";
    public string zipcode = "";

    public function getName () returns string {
        return self.name;
    }

    public function getAge () returns int {
        return self.age;
    }
};

public type userPB object {
    public int age = 0;
    public string name = "";
    public string address = "";
    public string zipcode = "";

    public function getName () returns string {
        return self.name;
    }

    public function getAge () returns int {
        return self.age;
    }
};

public type userPFoo object {
    public int age = 0;
    public string name = "";
    public string address = "";
    public string zipcode = "23468";

    public function getName () returns string {
        return self.name;
    }

    public function getAge () returns int {
        return self.age;
    }
};


function testEqOfPublicObjectsInSamePackage () returns string {
    userPFoo uFoo = new;
    uFoo.age = 10;
    uFoo.name = "Skyhigh";
    uFoo.address = "102 Skyhigh street #129, San Jose";

    // This is a safe assignment
    userPA uA = uFoo;

    userPB uB = uA;
    return uB.name;
}

function testRuntimeEqPublicObjects() returns string {
    req:userPFoo uFoo = new (10, "Skytop", "102 Skyhigh street #129, San Jose");

    // This is a safe assignment
    userPA uA = uFoo;

    userPB uB  = uA;
    return uB.name;
}

function testRuntimeEqPublicObjects1() returns string {
    req:userPFoo uFoo = new (10, "Brandon", "102 Skyhigh street #129, San Jose");

    // This is a safe assignment
    userPA uA = uFoo;

    return uA.getName();
}

type personC object {
    public string name = "";
    public addressStruct address = new;

    function setContact(addressStruct ad){
        self.address = ad;
    }

    function getAddress() returns string{
        return self.address.toString();
    }
};

type addressStruct object {
    public int no = 0;
    public string city = "";

    function toString() returns string {
        return self.no.toString() + self.city;
    }
};

type officeAddressStruct object {
    public int no = 0;
    public string city = "";
    public string department = "";

    function toString() returns string{
        return self.department + self.no.toString() + self.city;
    }
};

function testObjectEquivalencyWithArguments() returns [string, string, string] {
    personC p = new;
    p.name = "tom";
    addressStruct a = new;
    a.no = 1;
    a.city = "CMB";
    officeAddressStruct o = new;
    o.no = 2;
    o.city = "CMB";
    o.department = "ENG";
    // testing assignment.
    addressStruct b = o;
    string result1 = b.toString();

    p.setContact(a);
    string result2 = p.getAddress();

    // testing value passing.
    p.setContact(o);
    string result3 = p.getAddress();
    return [result1, result2, result3];
}

type Foo "a" | "b" | "c";

type Person object {
    string name = "";

    function __init (string name) {
        self.name = name;
    }

    function getPerson() returns Person {
        error err = error("Unsupported operation");
        panic err;
    }
};

type Employee object {
    string name = "";
    private string id = "";

    function __init (string name, string id) {
        self.id = id;
        self.name = name;
    }

    function getPerson() returns Person {
        return self;
    }
};

function testTupleMatchWithObjectEquivalency() returns string {
  future<[Foo, Person] | () | error> f = start getPerson();
    ([Foo, Person] | () | error) res = wait f;

    int[] i = [1, 2, 3];

    foreach var y in i {
        if res is [Foo, Person] {
            return "SUCCESS";
        } else {
            return "ERROR";
        }
    }
    return "ERROR";
}

function getPerson() returns [Foo, Person] | () | error {
    Foo f = "b";
    Employee p = new("foo","20");
    return [f,p];
}

public type ObjectWithoutNew object {
    public string name = "";
    public string id = "";

    public function getPerson() returns ObjectWithoutNew {
        return self;
    }
};

public type ObjectWithNew object {
    public string name = "";
    public string id = "";

    public function __init () {
    }

    public function getPerson() returns ObjectWithNew {
        return self;
    }
};

function testObjectEqualityWithDefaultConstructor() returns [ObjectWithNew, ObjectWithoutNew] {
    ObjectWithoutNew obj1 = new();
    ObjectWithNew obj2 = new();

    ObjectWithNew obj3 = obj1;
    ObjectWithoutNew obj4 = obj2;
    
    return [obj3, obj4];
}

type A object {

    public string field = "";
    
    function __init () {
        self.field = "value A"; 
    }

    function foo(C c) returns A {
        return new ();
    }
};

type B object {

    public string field = "";
    
    function __init () {
        self.field = "value B"; 
    }

    function foo(D d) returns B {
        return new ();
    }
};

type C object {
    function foo(A c) returns C {
        return new ();
    }
};

type D object {
    function foo(B a) returns D {
        return new ();
    }
};

function testObjectEqualityWithRecursiveTypes() returns [A, B] {
    A obj1 = new();
    B obj2 = new();

    B obj3 = obj1;
    A obj4 = obj2;
    
    return [obj3, obj4];
}

public type PersonInOrder object {
    public int age = 0;
    public string name = "";
    public string address = "";

    public function __init (string name, int age) {
        self.age = age;
        self.name = name;
    }

    public function getName() returns string {
        return self.name;
    }

    public function getAge() returns int {
        return self.age;
    }

    public function getAddress() returns string {
        return self.address;
    }
};

public type PersonNotInOrder object {

    public function getName() returns string {
        return self.name;
    }

    public int age = 0;

    public function getAge() returns int {
        return self.age;
    }

    public function __init (string name, int age) {
        self.age = age;
        self.name = name;
    }

    public string name = "";

    public function getAddress() returns string {
        return self.address;
    }

    public string address = "";
};

function testObjectMemberOrder() returns [PersonInOrder, PersonNotInOrder] {
    PersonInOrder p1 = new("John", 35);
    PersonNotInOrder p2 = p1;

    PersonNotInOrder p3 = new ("Doe", 45);
    PersonInOrder p4 = p3;

    return [p4, p2];
}

type ObjectWithAnyTypeVariables object {
    public any x;
    public any y;

    function __init() {
        self.x = "B";
        self.y = 100;
    }
};

type ObjectWithoutAnyTypeVariables object {
    public string x;
    public int y;

    function __init() {
        self.x = "A";
        self.y = 12;
    }
};

function testInherentTypeViolationWithNilType() {
    ObjectWithoutAnyTypeVariables o1 = new;
    ObjectWithAnyTypeVariables o2 = o1;
    o2.x = (); // panic
}
