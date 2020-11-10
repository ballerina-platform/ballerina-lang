// Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

type Person1 object {
    public int age;
    public string name;

    public function getName() returns string;
};

type Employee1 object {
    public float salary;

    public function getSalary() returns float; 
};

class Manager1 {
    *Person1;

    string dpt = "HR";

    *Employee1;

    public function getName() returns string {
        return self.name + " from inner function";
    }

    function init() {
        self.age = 99;
        self.name = "sample name 2";
        self.salary = 8.0;
    }

    public function getSalary() returns float {
        return self.salary;
    }
}

public function testSimpleObjectTypeReference() returns [int, string, float, string] {
    Manager1 mgr = new Manager1();
    return [mgr.age, mgr.getName(), mgr.getSalary(), mgr.dpt];
}

class Manager2 {
    *Person1;

    string dpt = "HR";

    *Employee1;

    public function getName() returns string {
        return self.name + " from inner function";
    }

    function init(int age=20) {
        self.age = age;
        self.name = "John";
        self.salary = 1000.0;
    }

    public function getSalary() returns float {
        return self.salary;
    }
}

public function testInitTypeReferenceObjectWithNew() returns [int, string, float, string] {
    Manager2 mgr = new Manager2();
    return [mgr.age, mgr.getName(), mgr.getSalary(), mgr.dpt];
}

class Manager3 {
    string dpt = "HR";

    *Employee2;

    function init(int age=20) {
        self.age = age;
        self.salary = 2500.0;
        self.name = "Doe";
    }

    public function getName() returns string {
        return self.name + " from outer function";
    }

    public function getSalary() returns float {
        return self.salary;
    }
}

type Employee2 object {
    public float salary;
    *Person1;

    public function getSalary() returns float; 
};

public function testObjectWithChainedTypeReferences() returns [int, string, float, string] {
    Manager3 mgr = new Manager3();
    mgr.name = "John";
    return [mgr.age, mgr.getName(), mgr.getSalary(), mgr.dpt];
}

// Test invoking object member method with default values
class Manager4 {
    string dpt = "HR";

    *Employee3;

    function init(string name, int age=25) {
        self.name = name;
        self.age = age;
        self.salary = 3000.0;
    }

    public function getBonus(float ratio, int months=6) returns float {
        return self.salary*ratio*months;
    }

    public function getName(string greeting = "Hello") returns string {
        return greeting + " " + self.name;
    }
}

type Employee3 object {
    public float salary;
    *Person3;

    public function getBonus(float ratio, int months=12) returns float;
};

type Person3 object {
    public int age;
    public string name;

    public function getName(string greeting = "Hi") returns string;
};

public function testAbstractObjectFuncWithDefaultVal() returns [string, float] {
    Manager4 mgr = new Manager4("Jane");
    return [mgr.getName(), mgr.getBonus(0.1)];
}

// non abstract object inclusion
class Ant {
    int id;

    public function init(int id) {
        self.id = id;
    }

    public function getId() returns int|() {
        if (self.id > 0) {
            return self.id;
        } else {
            return ();
        }
    }
}

class FireAnt {
    *Ant;

    public function init(int id) {
        self.id = id;
    }

    public function getId() returns int {
        return self.id;
    }
}

public function testNonAbstractObjectInclusion() {
    FireAnt notoriousFireAnt = new FireAnt(7);
    assertEquality(notoriousFireAnt.getId(), 7);

    Ant dullAnt = new FireAnt(0);
    assertEquality(dullAnt.getId(), 0);

    Ant nullAnt = new Ant(0);
    assertEquality(nullAnt.getId(), ());
}

// Type inclusion tests

type AgeDataObject object {
    int|float age;
};

class DefaultPerson {
    *AgeDataObject;
    int age;
    string name;

    function init(int age=18, string name = "UNKNOWN") {
       self.age = age;
       self.name = name;
    }
}

function testCreatingObjectWithOverriddenFields() {
    DefaultPerson dummyPerson = new DefaultPerson();
    assertEquality(dummyPerson.age, 18);
    dummyPerson.age = 400;
    assertEquality(dummyPerson.age, 400);
    assertEquality(dummyPerson.name, "UNKNOWN");
}

type NameInterface object {
    public function getName(string greeting = "Hi") returns string;
};

type AgeInterface object {
    *AgeDataObject;
    public function setAge(int age = 0) returns int;
};

class DefaultPersonGreetedName {
    *NameInterface;
    *AgeInterface;
    string name;
    int age;

    function init(int age = 18, string name = "UNKNOWN") {
       self.age = age;
       self.name = name;
    }

    public function getName(string greeting = "Hello") returns string {
        return greeting + " " + self.name;
    }

    public function setAge(int|float age = 0) returns int {
        if (age is int) {
            self.age = age;
            return self.age;
        }
        self.age = <int>age;
        return -1;
    }
}

function testCreatingObjectWithOverriddenMethods() {
    DefaultPersonGreetedName dummyPerson = new DefaultPersonGreetedName(name="Doe");
    assertEquality(dummyPerson.age, 18);
    int age = dummyPerson.setAge(80);
    assertEquality(dummyPerson.age, 80);
    assertEquality(dummyPerson.getName(), "Hello Doe");
}

const ASSERTION_ERROR_REASON = "AssertionError";

function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    panic error(ASSERTION_ERROR_REASON,
                message = "expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}
