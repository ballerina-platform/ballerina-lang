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

class Student {
    final string name;
    final int id;
    float avg = 80.0;
    function init(string n, int i) {
        self.name = n;
        self.id = i;
    }
}

function testInvalidUpdateOfObjectWithSimpleFinalFields() {
    Student st = new ("Maryam", 5);

    st.name = "Mary";
}

class Employee {
    final Details details;
    string department;

    function init(Details & readonly details, string department) {
        self.details = details;
        self.department = department;
    }
}

type Details record {
    string name;
    int id;
};

function testObjectWithStructuredFinalFields() {
    Details & readonly details = {
        name: "Kim",
        id: 1000
    };

    Employee e = new (details, "finance");

    e.details = details;
    e.details.name = "Jo"; // OK at compile time
}

class Customer {
    final string name;
    int id;

    function init(string n, int i) {
        self.name = n;
        self.id = i;
    }
}

function testInvalidUpdateOfFinalFieldInUnion() {
    Customer customer = new ("Jo", 1234);

    Student|Customer sd = customer;
    sd.name = "May";
    sd.id = 123; // OK at compile time
}

final Student[] students = [new ("Amy", 5), new ("Jo", 5)];

class Class {
    final Student[] allStudents = students;
    Student[] absentees;

    function init(Student[] absentees) {
        self.absentees = absentees;
    }
}

function testInvalidUpdateOfFinalFieldViaNestedAccess() {
    Class cl = new ([]);
    cl.allStudents[0].name = "May";
    cl.absentees[0].name = "May";
}
