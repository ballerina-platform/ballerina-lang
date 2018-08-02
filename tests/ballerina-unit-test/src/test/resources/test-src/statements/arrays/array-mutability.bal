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
    string name,
    !...
};

// Assignable to Person type
type Employee record {
    string name,
    boolean intern,
    !...
};

// Assignable to Employee type and Person Type
type Intern record {
    string name,
    boolean intern,
    int salary,
    !...
};

// Assignable to Person type
type Student record {
    string name,
    int studentId,
    !...
};

Person[] personArray;
Employee[] employeeArray;

Person person1 = { name: "John" };
Employee employee1 = { name: "John", intern: true };
Intern intern1 = { name: "John", intern: true, salary: 100 };
Student student1 = { name: "John", studentId: 001 };

function testValidArrayAssignment() returns (boolean, int) {
    personArray = employeeArray;
    personArray[0] = employee1;
    personArray[1] = intern1;

    Employee e = check <Employee> personArray[0];
    Intern i = check <Intern> personArray[1];
    return (e.intern, i.salary);
}

function testInvalidCast()  {
    personArray = employeeArray;
    personArray[0] = employee1;

    Intern e = check <Intern> personArray[0]; // Runtime Exception
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
    (boolean|float)[] x = [true, 2.0, true, 15.0];
    (boolean|float|Person)[] y = x;
    y[0] = 1.0;
    y[1] = person1;  // Runtime Exception
}

function testSealedArrays() {
    int[3] x;
    int[] y = x;
    y[3] = 12; // Runtime Exception
}

function testMultiDimensionalSealedArrays() {
    int[3][3] x;
    int[3][] y = x;
    y[0][0] = 10;
    y[0][3] = 12; // Runtime Exception
}

function testOpenSealedArrays() {
    int[!...] x19 = [1, 2, 3, 4];
    int[] x20 = x19;
    x20[4] = 5; // Runtime Exception
}

type Animal object {
    public string name;

    public function getName() returns string {
        return name;
    }
};

type Dog object { // Assignable to Animal Object
    public string name;
    public int age;

    public function getName() returns string {
        return name;
    }

    public function getNameAndAge() returns (string, int) {
        return (name, age);
    }
};

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
    match x2 {
        int[] => return "INT";
        boolean[] => return "BOOL";
    }
}

function testUnionOfArrays2() {
    int[]|boolean[] x2 = [true];

    (int|boolean)[] x1 = x2;

    x1[0] = 3; // Runtime Exception
}
