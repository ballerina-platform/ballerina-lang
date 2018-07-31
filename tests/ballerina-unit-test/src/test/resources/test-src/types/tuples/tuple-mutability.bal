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

Person person1 = { name: "John" };
Employee employee1 = { name: "John", intern: true };
Intern intern1 = { name: "John", intern: true, salary: 100 };
Student student1 = { name: "John", studentId: 001 };

function testValidTupleAssignment() returns (boolean, int) {


    (Employee, Employee) employeeTuple;
    (Person, Person) personTuple = employeeTuple; // covariance

    personTuple[0] = employee1;
    personTuple[1] = intern1;
    Employee e = check <Employee> personTuple[0];
    Intern i = check <Intern> personTuple[1];
    return (e.intern, i.salary);
}

function testInvalidCast()  {
    (Employee, Employee) employeeTuple;
    (Person, Person) personTuple = employeeTuple;
    personTuple[0] = employee1;

    Intern e = check <Intern> personTuple[0]; // Runtime Exception
}

function testAssignmentOfSuperTypeMember() {
    (Employee, Employee) employeeTuple;
    (Person, Person) personTuple = employeeTuple;
    personTuple[0] = person1; // Runtime Exception
}

function testInvalidAssignment() {
    (Employee, Employee) employeeTuple;
    (Person, Person) personTuple = employeeTuple;
    personTuple[0] = student1; // Runtime Exception
}

function testDifferentTypeCovariance() returns int {
    (Employee, Employee) tuple;
    (Employee, Person) tuple2 = tuple;
    (int, Intern, Intern) tuple3;
    (int|string, Person, Employee) tuple4 = tuple3;
    tuple4[0] = 12;
    tuple4[1] = intern1;
    tuple4[2] = intern1;
    return tuple4[0] but {string => 0};
}

function testCovarianceIntOrNilArray() {
    (int, int, ()) x = (1, 2, ());
    (int, int?, ()) y = x;
    y[0] = 5;
    y[1] = 6;
    y[1] = (); // Runtime Exception
}

function testCovarianceBooleanOrFloatOrRecordArray() {
    (boolean|float, int) x = (true, 5);
    (boolean|float|Person, int?) y = x;
    y[0] = 1;
    y[0] = person1;  // Runtime Exception
}
