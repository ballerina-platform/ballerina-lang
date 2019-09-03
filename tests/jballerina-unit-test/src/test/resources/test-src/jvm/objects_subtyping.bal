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

import testorg/subtyping;
import ballerinax/java.jdbc;

type Person1 object {
    public string name = "sample name";
    public int age = 10;

    int year = 50;
    string month = "february";

    function __init(string name, int age) {
        self.age = age;
        self.name = name;
    }
};

type Employee1 object {
    public string name = "no name";
    public int age = 3;

    int year = 2;
    string month = "idk";
    
    function __init(string name, int age) {
        self.age = age;
        self.name = name;
        self.year = 50;
        self.month = "february";
    }

    function getName() returns string {
        return self.name;
    }

    function getYear() returns int {
        return self.year;
    }
};

function testAdditionalMethodsInSourceType() returns Person1 {
    Employee1 e = new("John Doe", 25);
    Person1 p = e;
    return p;
}

function testCastingRuntimeError() returns Person1 {
    Person1 p = new("John Doe", 25);
    Employee1 e = <Employee1>p;
    return e;
}

type AbstractPerson abstract object {
    public string name;
    public int age;

    public function toString() returns string;
};

type Student1 object {
    public string name = "";
    public int age = 0;
    public string school = "";

    public function __init(string name, int age, string school) {
        self.name = name;
        self.age = age;
        self.school = school;
    }

    public function toString() returns string {
        return "Student1{" + self.name + ", " + self.age.toString() + ", " + self.school + "}";
    }
};

function testSubtypingAPublicAbstractObject() returns string {
    AbstractPerson ap = new Student1("John Doe", 25, "Ballerina Academy");
    return ap.toString();
}

function testSubtypingAPublicAbsObjectInAnotherModule() returns string {
    AbstractPerson ap = new subtyping:Student("Jane Doe", "BA", 22);
    return ap.toString();
}

public type UniStudent1 object {
    public string name = "";
    public string school = "";
    public int age = 0;
    public string major;

    public function __init(string name, string school, int age, string major) {
        self.name = name;
        self.age = age;
        self.school = school;
        self.major = major;
    }

    public function toString() returns string {
        return "Student{" + self.name + ", " + self.age.toString() + ", " + self.school + ", " + self.major + "}";
    }

    public function getSchool() returns string {
        return self.school;
    }
};

function testSubtypingAPublicObjectInAnotherModule() returns string {
    subtyping:Student s = new UniStudent1("Jane Doe", "BA", 22, "CS");
    return s.toString();
}

type AbstractAnimal abstract object {
    float weight;

    function move(int distance) returns string;
};

type Dog object {
    string name;
    float weight;

    function __init(string name, float weight) {
        self.name = name;
        self.weight = weight;
    }

    function move(int distance) returns string {
        return self.name + " walked " + distance.toString() + " meters";
    }
};

function testSubtypingAnAbsObjectInSameModule() returns string {
    AbstractAnimal a = new Dog("Rocky", 10);
    return a.move(50);
}

// NOTE: There isn't a test case associated with this. Just want to ensure this scenario compiles fine.
public function testReferencingObjectTypesAcrossModules() returns jdbc:Client {
    jdbc:Client dbClient = subtyping:getClient();
    return dbClient;
}
