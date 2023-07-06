// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

// Super Type
type Person record {
    string name = "";
};

// Assignable to Person type
type Employee record {
    string name = "";
    boolean intern = false;
};

// Assignable to Employee type and Person Type
type Intern record {|
    string name = "";
    boolean intern = false;
    int salary = 0;
|};

// Assignable to Person type
type Student record {|
    string name = "";
    int studentId = 0;
|};

Person[] personArray = [];
Employee[] employeeArray = [];

Person person1 = { name: "John" };
Employee employee1 = { name: "John", intern: true };
Intern intern1 = { name: "John", intern: true, salary: 100 };
Student student1 = { name: "John", studentId: 1 };

function testValidArrayAssignment() returns [boolean, int]|error {
    personArray = employeeArray;
    personArray[0] = employee1;
    personArray[1] = intern1;

    Employee e =  <Employee> personArray[0];
    Intern i =  <Intern> personArray[1];
    return [e.intern, i.salary];
}

function testInvalidCast() returns error? {
    personArray = employeeArray;
    personArray[0] = employee1;

    var e = trap <Intern> personArray[0]; // Runtime Exception
    if e is error {
        panic e;
    }
    return ();
}

function testAssignmentOfSuperTypeMember() {
    personArray = employeeArray;
    personArray[1] = person1; // Runtime Exception
}

function testInvalidAssignment() {
    personArray = employeeArray;
    personArray[1] = student1; // Runtime Exception
}

function testCovarianceIntOrNilArray() {
    int[] x = [1, 2, 3, 4];
    int?[] y = x;
    y[0] = 5;
    y[1] = (); // Runtime Exception
}

function testCovarianceBooleanOrFloatOrRecordArray() {
    (boolean|float)?[] x = [true, 2.0, true, 15.0];
    (boolean|float|Person)?[] y = x;
    y[0] = 1.0;
    y[1] = person1;  // Runtime Exception
}

function testSealedArrays() {
    int[3] x = [0, 0, 0];
    int[] y = x;
    y[3] = 12; // Runtime Exception
}

function testMultiDimensionalSealedArrays() {
    int[3][3] x = [[0, 0, 0], [0, 0, 0], [0, 0, 0]];
    int[3][] y = x;
    y[0][0] = 10;
    y[0][3] = 12; // Runtime Exception
}

function testOpenSealedArrays() {
    int[*] x19 = [1, 2, 3, 4];
    int[] x20 = x19;
    x20[4] = 5; // Runtime Exception
}

class Animal {
    public string name = "";

    public function getName() returns string {
        return self.name;
    }
}

class Dog { // Assignable to Animal Object
    public string name = "";
    public int age = 0;

    public function getName() returns string {
        return self.name;
    }

    public function getNameAndAge() returns [string, int] {
        return [self.name, self.age];
    }
}

function testObjectTypes() {
    Animal animal1 = new;
    Dog dog1 = new;
    Animal a1 = dog1;

    Dog[] dogArray = [dog1, new];
    Animal[] animalArray = dogArray;

    animalArray[0] = animal1; // Runtime Exception
}

function testUnionOfArrays() returns string {

    boolean[] boolArray = [true, true];

    int[]|boolean[] x2 = boolArray;

    if (x2 is int[]) {
        return "INT";
    } else {
        return "BOOL";
    }
}

function testUnionOfArrays2() {
    int[]|boolean[] x2 = [true];

    (int|boolean)?[] x1 = x2;

    x1[0] = 3; // Runtime Exception
}

function testJsonArrayMutability() {
    int[] x1 = [1, 2];
    json[] x2 = x1;
    x2[2] = 3;
    x2[3] = "json"; // Runtime Exception
}

function testJsonArrayMutability2() {
    boolean[] x1 = [true, true];
    json[] x2 = x1;
    x2[2] = true;
    x2[3] = "json"; // Runtime Exception
}

function testChainingAssignment() {
    (int|string)?[] first = [];
    (int|string?)[] second = first;
    (int|string|boolean?)[] thrid = second;
    (int|string|boolean|Person?)[] fourth = thrid;

    fourth[0] = 1;
    fourth[1] = "string";
    fourth[2] = true; // Runtime Exception
}
