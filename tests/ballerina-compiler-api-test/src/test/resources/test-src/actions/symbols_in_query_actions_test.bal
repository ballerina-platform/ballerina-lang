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

function test() returns any? {
    Student[] students = getStudents();
    Person[] people = getPeople();

    Person[] person_list = [];
    record {|string name;|}[] names = [];
    Student[] student_ordered = [];

    // basic/minimal
    error? v1 = from var st in students
        do {
            Person pa = {name: st.fname, age: st.age};
            person_list.push(pa);
        };

    // where clause
    error? v2 = from var st in students
        where st.fname == "Jon"
        do {
            names.push({name: st.fname});
        };

    // let clause
    error? v3 = from var st in students
        let string name = st.fname + " " + st.lname
        do {
            float gpa = st.gpa;
            string full_name = name;
        };

    // join clause
    error? v4 = from var st in students
        join var {name, age} in people on st.fname equals name
        do {
            string reg_name = name + age.toString();
        };

    // order by clause
    error? v5 = from var st in students
        order by st.id
        do {
            student_ordered.push(st);
        };

    // limit clause
    error? v7 = from var st in students
        limit LIMIT
        do {
            int l = 20;
        };
}

// utils

type Person record {|
    string name;
    int age;
|};

type Student record {
    int id;
    string fname;
    string lname;
    int age;
    float gpa;
};

const LIMIT = 1;

function getStudents() returns Student[] {
    Student s3 = {id: 3, fname: "Amy", lname: "Melina", age: 30, gpa: 1.3};
    Student s1 = {id: 1, fname: "Jon", lname: "Doe", age: 21, gpa: 2.1};
    Student s2 = {id: 2, fname: "Jane", lname: "Doe", age: 25, gpa: 3.2};
    return [s1, s2, s3];
}

function getPeople() returns Person[] {
    Person p1 = {name: "Jon Doe", age: 0};
    Person p2 = {name: "Jane Doe", age: 1};
    return [p1, p2];
}
