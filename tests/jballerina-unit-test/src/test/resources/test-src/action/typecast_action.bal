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

client class Foo1 {
    any val;

    function init(any val) {
        self.val = val;
    }

    remote function getValue() returns any {
        return self.val;
    }

    function setValue(any val) {
        self.val = val;
    }
}

function testTypecastWithRemoteMethodCalls() {
    Foo1 foo = new("Foo");

    string s1 = <string>foo->getValue();
    assert("Foo", s1);

    foo.setValue(10);
    int i1 = <int>foo->getValue();
    assert(10, i1);

    foo.setValue(12.34);
    float f1 = <float>foo->getValue();
    assert(12.34, f1);

    foo.setValue(23.45d);
    decimal d1 = <decimal>foo->getValue();
    assert(23.45d, d1);

    foo.setValue(true);
    boolean b1 = <boolean>foo->getValue();
    assert(true, b1);
}

type Person record {
    string name = "John Doe";
    int age = 20;
};

type Student record {|
    *Person;
    string school = "Hogwarts";
|};

function testCastingRecords() {
    Student s1 = {name: "Jane Doe", age: 25};
    Foo1 foo = new(s1);

    Student s2 = <Student>foo->getValue();
    assert(<Student>{name: "Jane Doe", age: 25, school: "Hogwarts"}, s2);

    Person p1 = <Person>foo->getValue();
    assert(<Person>{name: "Jane Doe", age: 25, "school": "Hogwarts"}, p1);

    Person p2 = <record{ string name; int age; }>foo->getValue();
    assert(<Person>{name: "Jane Doe", age: 25, "school": "Hogwarts"}, p2);
}

class PersonObj {
    string name;
    int age;

    function init(string name, int age) {
        self.name = name;
        self.age = age;
    }

    function getDetails(string 'field) returns string|int {
        if ('field == "name") {
            return self.name;
        } else {
            return self.age;
        }
    }

    function toString() returns string {
        return "name=" + self.name + " age=" + self.age.toString();
    }
}

function testCastingObjects() {
    PersonObj p1 = new("John Doe", 25);
    Foo1 foo = new(p1);

    PersonObj p2 = <PersonObj>foo->getValue();
    assert("name=John Doe age=25", p2.toString());
}

function testTypecastingActions() {
    PersonObj po = new("John Doe", 25);

    future<string|int> f1 = start po.getDetails("name");
    string name = <string>wait f1;
    assert("John Doe", name);

    f1 = start po.getDetails("age");
    int age = <int>wait f1;
    assert(25, age);
}

function testCastingToIncorrectType() {
    Foo1 foo = new(100);
    string s = <string>foo->getValue();
}

// Util functions

function assert(anydata expected, anydata actual) {
    if (expected != actual) {
        typedesc<anydata> expT = typeof expected;
        typedesc<anydata> actT = typeof actual;
        string detail = "expected [" + expected.toString() + "] of type [" + expT.toString()
                            + "], but found [" + actual.toString() + "] of type [" + actT.toString() + "]";
        panic error("{AssertionError}", message = detail);
    }
}
