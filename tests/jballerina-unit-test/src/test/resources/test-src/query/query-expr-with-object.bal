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

public function testObjectBasedQueryExpr() {
    testQueryExprForObject();
    testQueryExprForObjectV2();
    testQueryExprInObjectMethod();
}

class Student {
    public string firstName;
    public string lastName;
    public int age;
    string address;

    function init(string firstName, string lastName, int age, string address = "No 20, Palm grove") {
        self.age = age;
        self.firstName = firstName;
        self.lastName = lastName;
        self.address = address;
    }

    function getFullName() returns string {
        return self.firstName + " " + self.lastName;
    }
}

public function testQueryExprForObject() {

    Student p1 = new ("Alex", "George", 23, "Kandy");
    Student p2 = new ("Ranjan", "Fonseka", 30);
    Student p3 = new ("John", "David", 33);

    Student[] studentList = [p1, p2, p3];

    Student[] outputStudentList =
            from var student in studentList
            select new (student.firstName, student.lastName, student.age, student.address);

    assertEquality("No 20, Palm grove", outputStudentList[1].address);
    assertEquality("Kandy", outputStudentList[0].address);
}

public function testQueryExprForObjectV2() {

    Student p1 = new ("Alex", "George", 23, "Kandy");
    Student p2 = new ("Ranjan", "Fonseka", 30);
    Student p3 = new ("John", "David", 33);

    Student[] studentList = [p1, p2, p3];

    Student[] outputStudentList =
            from var student in studentList
            where student.address != "Kandy"
            select new (student.firstName, student.lastName, student.age, student.address);


    assertEquality(2 , outputStudentList.length());
    assertEquality("No 20, Palm grove", outputStudentList[1].address);

}

type Person record {
    string firstName;
    string lastName;
    int age;
    string? address;
};

class Employee {
    public string firstName;
    public string lastName;
    public int age;
    string address;

    function init(string firstName, string lastName, int age, string address = "No 20, Palm grove") {
        self.age = age;
        self.firstName = firstName;
        self.lastName = lastName;
        self.address = address;
    }

    function runQueryExpr() returns Person[] {
        Person p1 = {firstName: "Alex", lastName: "George", age: 23, address: ()};
        Person p2 = {firstName: "Ranjan", lastName: "Fonseka", age: 30, address: "Kandy"};
        Person p3 = {firstName: "John", lastName: "David", age: 33, address: ()};

        Person[] personList = [p1, p2, p3];

        Person[] outputPersonList =
                from var person in personList
                select {
                       firstName: person.firstName,
                       lastName: person.lastName,
                       age: person.age,
                       address: person.address
                 };

        return outputPersonList;
    }
}

public function testQueryExprInObjectMethod() {

    Employee employee = new ("Alex", "George", 23, "Kandy");
    Person[] outputPersonList = employee.runQueryExpr();

    string? ad = outputPersonList[0]?.address;
    assertEquality((), ad);

    assertEquality("Kandy", outputPersonList[1]?.address);
}

//---------------------------------------------------------------------------------------------------------
const ASSERTION_ERROR_REASON = "AssertionError";

function assertTrue(any|error actual) {
    assertEquality(true, actual);
}

function assertFalse(any|error actual) {
    assertEquality(false, actual);
}

function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    string expectedValAsString = expected is error ? expected.toString() : expected.toString();
    string actualValAsString = actual is error ? actual.toString() : actual.toString();
    panic error(ASSERTION_ERROR_REASON,
                      message = "expected '" + expectedValAsString + "', found '" + actualValAsString + "'");
}
