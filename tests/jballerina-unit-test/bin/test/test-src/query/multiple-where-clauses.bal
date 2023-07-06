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

type Person record {|
   string firstName;
   string lastName;
   string deptAccess;
|};

type Department record {|
   string name;
|};

type Student record{|
    string firstName;
    string lastName;
    float score;
|};

function testMultipleWhereClausesWithSimpleVariable() returns Person[]{

    Person p1 = {firstName: "Alex", lastName: "George", deptAccess: "XYZ"};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", deptAccess: "XYZ"};
    Person p3 = {firstName: "John", lastName: "David", deptAccess: "XYZ"};

     Department d1 = {name:"HR"};
     Department d2 = {name:"Operations"};

    Person[] personList = [p1, p2, p3];
    Department[] deptList = [d1, d2];

    Person[] outputPersonList =
            from var person in personList
            from var dept in deptList
            where person.firstName == "Alex"
            where person.deptAccess == "XYZ"
            select {
                   firstName: person.firstName,
                   lastName: person.lastName,
                   deptAccess: dept.name
            };
    return  outputPersonList;
}

function testMultipleWhereClausesWithRecordVariable() returns Person[]{

    Person p1 = {firstName: "Alex", lastName: "George", deptAccess: "XYZ"};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", deptAccess: "XYZ"};
    Person p3 = {firstName: "John", lastName: "David", deptAccess: "XYZ"};

    Department d1 = {name:"HR"};
    Department d2 = {name:"Operations"};

    Person[] personList = [p1, p2, p3];
    Department[] deptList = [d1, d2];

    Person[] outputPersonList =
            from var { firstName: nm1, lastName: nm2, deptAccess: d } in personList
            from var { name: deptName } in deptList
            where nm1 == "Alex"
            where deptName == "Operations"
            select {
                   firstName: nm1,
                   lastName: nm2,
                   deptAccess: deptName
            };
    return  outputPersonList;
}

function testLogicalOperandsWithWhere() returns Student[]{

    Student s1 = {firstName: "Alex", lastName: "George", score: 82.5};
    Student s2 = {firstName: "Ranjan", lastName: "Fonseka", score: 90.6};

    Student[] studentList = [s1, s2];

    Student[] outputStudentList =
            from var student in studentList
            where student.firstName == "Ranjan" || student.firstName == "Alex" && student.score >= 82.5
            where  student.lastName != "George"
            select {
                   firstName: student.firstName,
                   lastName: student.lastName,
                   score: student.score

            };

    return  outputStudentList;
}
