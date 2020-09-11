// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ballerina/test;

# Execute tests with Behavioral type value assertions

class Person {
    public string name = "";
    public int age = 0;
    public Person? parent = ();
    private string email = "default@abc.com";
    string address = "No 20, Palm grove";
}

type EnrolledStudent record {
    int id;
    Person person;
};

@test:Config {}
function testAssertObjectEquals() {
    Person p1 = new;
    Person p2 = p1;
    test:assertExactEquals(p1, p2);
}

@test:Config {}
function testAssertObjectEqualsNegative() {
    Person p1 = new;
    Person p2 = new ();
    error? err = trap test:assertExactEquals(p1, p2);
    test:assertTrue(err is error);
}

@test:Config {}
function testAssertObjectNotEquals() {
    Person p1 = new;
    Person p2 = new ();
    test:assertNotExactEquals(p1, p2);
}

@test:Config {}
function testAssertObjectNotEqualsNegative() {
    Person p1 = new;
    Person p2 = p1;
    error? err = trap test:assertNotExactEquals(p1, p2);
    test:assertTrue(err is error);
}

@test:Config {}
function testAssertObjectArrayEquals() {
    Person p1 = new Person();
    Person p2 = new Person();
    Person[] family1 = [p1, p2];
    Person[] family2 = family1;
    test:assertExactEquals(family1, family2);
}

@test:Config {}
function testAssertObjectArrayEqualsNegative() {
    Person p1 = new Person();
    Person p2 = new Person();
    Person[] family1 = [p1, p2];
    Person[] family2 = [p1, p2];
    error? err = trap test:assertExactEquals(family1, family2);
    test:assertTrue(err is error);
}

@test:Config {}
function testAssertObjectArrayNotEquals() {
    Person p1 = new Person();
    Person p2 = new Person();
    Person[] family1 = [p1, p2];
    Person[] family2 = [p1, p2];
    test:assertNotExactEquals(family1, family2);
}

@test:Config {}
function testAssertObjectArrayNotEqualsNegative() {
    Person p1 = new Person();
    Person p2 = new Person();
    Person[] family1 = [p1, p2];
    Person[] family2 = family1;
    error? err = trap test:assertNotExactEquals(family1, family2);
    test:assertTrue(err is error);
}

@test:Config {}
function testAssertObjectRecordEquals() {
    Person p1 = new Person();
    EnrolledStudent student1 = {
        id: 1,
        person: p1
    };
    EnrolledStudent student2 = student1;
    test:assertExactEquals(student1, student2);
}

@test:Config {}
function testAssertObjectRecordEqualsNegative() {
    Person p1 = new Person();
    EnrolledStudent student1 = {
        id: 1,
        person: p1
    };
    EnrolledStudent student2 = {
        id: 1,
        person: p1
    };
    error? err = trap test:assertExactEquals(student1, student2);
    test:assertTrue(err is error);
}

@test:Config {}
function testAssertObjectRecordNotEquals() {
    Person p1 = new Person();
    EnrolledStudent student1 = {
        id: 1,
        person: p1
    };
    EnrolledStudent student2 = {
        id: 1,
        person: p1
    };
    test:assertNotExactEquals(student1, student2);
}

@test:Config {}
function testAssertObjectRecordNotEqualsNegative() {
    Person p1 = new Person();
    EnrolledStudent student1 = {
        id: 1,
        person: p1
    };
    EnrolledStudent student2 = student1;
    error? err = trap test:assertNotExactEquals(student1, student2);
    test:assertTrue(err is error);
}
