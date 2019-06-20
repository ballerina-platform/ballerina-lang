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
    string name;
};

// Assignable to Person type
type Employee record {
    string name;
    boolean intern;
};

// Assignable to Employee type and Person Type
type Intern record {|
    string name;
    boolean intern;
    int salary;
|};

// Assignable to Person type
type Student record {|
    string name;
    int studentId;
|};

Person person1 = { name: "John" };
Employee employee1 = { name: "John", intern: true };
Intern intern1 = { name: "John", intern: true, salary: 100 };
Student student1 = { name: "John", studentId: 1 };

function testValidTupleAssignment() returns [boolean, int] {

    [Employee, Employee] employeeTuple = [employee1, employee1];
    [Person, Person] personTuple = employeeTuple; // covariance

    personTuple[0] = employee1;
    personTuple[1] = intern1;
    Employee e = <Employee> personTuple[0];
    Intern i = <Intern> personTuple[1];
    return [e.intern, i.salary];
}

function testInvalidCast()  {
    [Employee, Employee] employeeTuple = [employee1, employee1];
    [Person, Person] personTuple = employeeTuple;
    personTuple[0] = employee1;

    Intern e = <Intern> personTuple[0]; // Runtime Exception
}

function testAssignmentOfSuperTypeMember() {
    [Employee, Employee] employeeTuple = [employee1, employee1];
    [Person, Person] personTuple = employeeTuple;
    personTuple[0] = person1; // Runtime Exception
}

function testInvalidAssignment() {
    [Employee, Employee] employeeTuple = [employee1, employee1];
    [Person, Person] personTuple = employeeTuple;
    personTuple[0] = student1; // Runtime Exception
}

function testDifferentTypeCovariance() returns int {
    [Employee, Employee] tuple = [employee1, employee1];
    [Employee, Person] tuple2 = tuple;
    [int, Intern, Intern] tuple3 = [12, intern1, intern1];
    [int|string, Person, Employee] tuple4 = tuple3;
    tuple4[0] = 12;
    tuple4[1] = intern1;
    tuple4[2] = intern1;
    var result = tuple4[0];
    return result is int ? result : 0;
}

function testCovarianceIntOrNilTuple() {
    [int, int, ()] x = [1, 2, ()];
    [int, int?, ()] y = x;
    y[0] = 5;
    y[1] = 6;
    y[1] = (); // Runtime Exception
}

function testCovarianceBooleanOrFloatOrRecordTuple() {
    [boolean|float, int] x = [true, 5];
    [boolean|float|Person, int?] y = x;
    y[0] = 1.0;
    y[0] = person1;  // Runtime Exception
}

function testComplexTupleTypes() returns [float, json, boolean, json, float] {
    [(float|boolean), float] var1 = [12.0, 5.0];
    [json|int, Person] var2 = [true, person1];
    [[int|boolean, float], [float|json, string]] var3 = [[true, 6.0], ["json", "string"]];
    [[[float|boolean, int], float], boolean] var4 = [[[12.0, 2], 3.0], true];

    float|boolean x1 = var1[0];
    float x2 = var1[1];
    json|int x3 = var2[0];
    [int|boolean, float] x4 = var3[0];
    int|boolean x5 = var3[0][0];
    float|json x6 = var3[1][0];
    float|boolean x7 = var4[0][0][0];

    return [x1 is float ? x1 : 0.0, x3, x5 is boolean ? x5 : false, x6, x7 is float ? x7 : 0.0];
}

function testWithTryCatch() returns int {
    [int, int, ()] x = [1, 2, ()];
    [int, int?, ()] y = x;

    y[0] = 5;
    y[1] = 5;
    var result = trap nilValueAssignment(y);
    return y[1] ?: 1;
}

function nilValueAssignment([int, int?, ()] y) {
    y[1] = (); // Runtime Exception
}
