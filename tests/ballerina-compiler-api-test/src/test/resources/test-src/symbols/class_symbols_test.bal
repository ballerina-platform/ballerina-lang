// Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

class Person1 {
    string fname;
    string lname;

    public function init(string fname, string lname) {
        self.fname = fname;
        self.lname = lname;
    }

    public function getFullName() returns string {
        return self.fname + " " + self.lname;
    }
}

class Person2 {
    string fname = "John";
    string lname = "Doe";

    public function getFullName() returns string {
        return self.fname + " " + self.lname;
    }
}

function test() {
    Person1 p1 = new("Pubudu", "Fernando");
    Person2 p2 = new;
    p2 = new Person2();
}

distinct class DistinctPerson {
    string name;

    function init(string name) {
        self.name = name;
    }

    function getName() returns string => self.name;
}

class Person3 {
    string fname = "John";
    string lname = "Doe";
    string address = "No 20, Palm Grove";
    string tempAddress;
    int age;

}

function test() {
    Person3 p3 = new;
    p3.fname;
    p3.address;
    p3.tempAddress;
    p3.age;
}

type PersonType Person1;

function testTypeAliasForClass() {
    PersonType pt = new PersonType("John", "Doe");
}

type Rec record {|
    int id;
    int count;
|};

type FooObject object {
    any x;
};

class FooClass {
    any x = 2;

    function hello() returns string {
        return "hello";
    }

    *FooObject;
}

# Class for compiler API test
@classForTest
class TestClass {

    # Name field
    @v1
    public string testName;

    # X field
    @v2
    final any x = "x";

    # Foo field
    @v1
    private Rec foo;

    # Constructor
    @v2
    function init(string testName, int id) {
        self.testName = testName;
        self.foo = {id: id, count: 0};
    }

    # Get name
    # + return - Computed name
    @v1
    public function getName() returns string {
        return self.testName + "/" + self.foo.id.toString();
    }

    # Get X
    # + return - 'x' value
    @v1
    public isolated function getX() returns any => self.x;

    # Hello
    @v2
    function hello() returns string {
        return "";
    }

    *FooClass;
}

const annotation classForTest on source class;

const annotation v1 on source field, function ;

const annotation v2 on field, function;
