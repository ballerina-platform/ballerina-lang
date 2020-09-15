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

public class person1 {
    public int age = 0;
    public string name = "";
    public string address = "";
    string zipcode = "95134";
    string ssn = "";
    int id = 0;

    public function init () {}

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
}

public class employee1 {
    public int age = 0;
    public string name = "";
    public string address = "";
    public int employeeId = 123456;
    string zipcode = "95134";
    string ssn = "";
    int id = 0;

    public function init (int age, string name) {
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
}

function testObjectEquivalenceWhenFieldsHaveModuleVisibility() returns string {
    employee1 e = new (14, "rat");
    e.setSSN("234-56-7890");

    person1 p = e;

    return p.getSSN();
}

public class person2 {
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
}

public class employee2 {
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
}

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


public class employee3 {
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
}

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




class userA {
    public int age = 0;
    public string name = "";
    public string address = "";

    function getName () returns string {
        return self.name;
    }

    function getAge () returns int {
        return self.age;
    }
}

class userB {
    public int age = 0;
    public string name = "";
    public string address = "";

    function getName () returns string {
        return self.name;
    }

    function getAge () returns int {
        return self.age;
    }
}

class userFoo {
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
}


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


public class userPA {
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
}

public class userPB {
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
}

public class userPFoo {
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
}


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

class personC {
    public string name = "";
    public addressStruct address = new;

    function setContact(addressStruct ad){
        self.address = ad;
    }

    function getAddress() returns string{
        return self.address.toString();
    }
}

class addressStruct {
    public int no = 0;
    public string city = "";

    function toString() returns string {
        return self.no.toString() + self.city;
    }
}

class officeAddressStruct {
    public int no = 0;
    public string city = "";
    public string department = "";

    function toString() returns string{
        return self.department + self.no.toString() + self.city;
    }
}

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

class Person {
    string name = "";

    function init (string name) {
        self.name = name;
    }

    function getPerson() returns Person {
        error err = error("Unsupported operation");
        panic err;
    }
}

class Employee {
    string name = "";
    private string id = "";

    function init (string name, string id) {
        self.id = id;
        self.name = name;
    }

    function getPerson() returns Person {
        return self;
    }
}

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

public class ObjectWithoutNew {
    public string name = "";
    public string id = "";

    public function getPerson() returns ObjectWithoutNew {
        return self;
    }
}

public class ObjectWithNew {
    public string name = "";
    public string id = "";

    public function init () {
    }

    public function getPerson() returns ObjectWithNew {
        return self;
    }
}

function testObjectEqualityWithDefaultConstructor() returns [ObjectWithNew, ObjectWithoutNew] {
    ObjectWithoutNew obj1 = new();
    ObjectWithNew obj2 = new();

    ObjectWithNew obj3 = obj1;
    ObjectWithoutNew obj4 = obj2;
    
    return [obj3, obj4];
}

class A {

    public string 'field = "";

    function init () {
        self.'field = "value A";
    }

    function foo(C c) returns A {
        return new ();
    }
}

class B {

    public string 'field = "";

    function init () {
        self.'field = "value B";
    }

    function foo(D d) returns B {
        return new ();
    }
}

class C {
    function foo(A c) returns C {
        return new ();
    }
}

class D {
    function foo(B a) returns D {
        return new ();
    }
}

function testObjectEqualityWithRecursiveTypes() returns [A, B] {
    A obj1 = new();
    B obj2 = new();

    B obj3 = obj1;
    A obj4 = obj2;
    
    return [obj3, obj4];
}

public class PersonInOrder {
    public int age = 0;
    public string name = "";
    public string address = "";

    public function init (string name, int age) {
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
}

public class PersonNotInOrder {

    public function getName() returns string {
        return self.name;
    }

    public int age = 0;

    public function getAge() returns int {
        return self.age;
    }

    public function init (string name, int age) {
        self.age = age;
        self.name = name;
    }

    public string name = "";

    public function getAddress() returns string {
        return self.address;
    }

    public string address = "";
}

function testObjectMemberOrder() returns [PersonInOrder, PersonNotInOrder] {
    PersonInOrder p1 = new("John", 35);
    PersonNotInOrder p2 = p1;

    PersonNotInOrder p3 = new ("Doe", 45);
    PersonInOrder p4 = p3;

    return [p4, p2];
}

class ObjectWithAnyTypeVariables {
    public any x;
    public any y;

    function init() {
        self.x = "B";
        self.y = 100;
    }
}

class ObjectWithoutAnyTypeVariables {
    public string x;
    public int y;

    function init() {
        self.x = "A";
        self.y = 12;
    }
}

function testInherentTypeViolationWithNilType() {
    ObjectWithoutAnyTypeVariables o1 = new;
    ObjectWithAnyTypeVariables o2 = o1;
    o2.x = (); // panic
}

class NonClientObject {
    public string name;
    public string id = "";

    function init(string name) {
        self.name = name;
    }
    public function send(string message) returns error? {
    }
    public function receive(string message) {
    }
}

client class ClientObjectWithoutRemoteMethod {
    public string name;
    public string id = "";

    function init(string name) {
        self.name = name;
    }
    public function send(string message) returns error? {
    }
    public function receive(string message) {
    }
}

function testObjectAssignabilityBetweenNonClientAndClientObject() {
    NonClientObject obj1 = new("NonClientObject");
    ClientObjectWithoutRemoteMethod obj2 = new("ClientObjectWithoutRemoteMethod");

    NonClientObject obj3 = obj2;
    ClientObjectWithoutRemoteMethod obj4 = obj1;

    assertEquality("NonClientObject", obj4.name);
    assertEquality("ClientObjectWithoutRemoteMethod", obj3.name);
}

client class Email {
    public remote function send(string message) returns error? {
    }
}

class FakeEmail {
    public function send(string message) returns error? {
    }
}

type Message record {|
    Email f;
|};

type Text record {|
    FakeEmail f;
|};

function subtypingBetweenNonClientAndClientObject1() returns Email {
    FakeEmail p = new();
    any e = p;
    Email email = <Email> e;
    return email;
}

function subtypingBetweenNonClientAndClientObject2() returns any {
    Message b = {f: new};
    record {|
        object {}...;
    |} r = b;

    r["f"] = new FakeEmail();
    return r["f"];
}

function subtypingBetweenNonClientAndClientObject3() returns FakeEmail {
    Email p = new();
    any e = p;
    FakeEmail email = <FakeEmail> e;
    return email;
}

function subtypingBetweenNonClientAndClientObject4() returns any {
    Text b = {f: new};
    record {|
        object {}...;
    |} r = b;

    r["f"] = new Email();
    return r["f"];
}

function testSubtypingBetweenNonClientAndClientObject() {
    Email|error err1 = trap subtypingBetweenNonClientAndClientObject1();
    any|error err2 = trap subtypingBetweenNonClientAndClientObject2();
    FakeEmail|error err3 = trap subtypingBetweenNonClientAndClientObject3();
    any|error err4 = trap subtypingBetweenNonClientAndClientObject4();

    error e1 = <error> err1;
    error e2 = <error> err2;
    error e3 = <error> err3;
    error e4 = <error> err4;

    assertEquality("incompatible types: 'ObjectEquivalencyTest:FakeEmail' cannot be cast to " +
    "'ObjectEquivalencyTest:Email'", e1.detail()["message"]);
    assertEquality("invalid value for record field 'f': expected value of type 'ObjectEquivalencyTest:Email', " +
    "found 'ObjectEquivalencyTest:FakeEmail'", e2.detail()["message"]);
    assertEquality("incompatible types: 'ObjectEquivalencyTest:Email' cannot be cast to " +
    "'ObjectEquivalencyTest:FakeEmail'", e3.detail()["message"]);
    assertEquality("invalid value for record field 'f': expected value of type 'ObjectEquivalencyTest:FakeEmail', " +
    "found 'ObjectEquivalencyTest:Email'", e4.detail()["message"]);
}

const ASSERTION_ERROR_REASON = "AssertionError";

function assertEquality(any|error expected, any|error actual) {
    if (expected is anydata && actual is anydata && expected == actual) {
        return;
    }

    if (expected === actual) {
        return;
    }

    panic error(ASSERTION_ERROR_REASON,
                 message = "expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}
