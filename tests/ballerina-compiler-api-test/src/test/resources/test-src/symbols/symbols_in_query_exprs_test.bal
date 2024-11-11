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

function testQueryExpr() {
    Student[] students = getStudents();
    Person[] people = getPeople();

    // basic/minimal
    Person[] v1 = from var st in students select {id: st.id, name: st.fname, age: st.age};
    var v2 = from var st in students select <Person>{id: st.id, name: st.fname, age: st.age};

    // where clause
    var v3 = from var st in students
                where st.fname == "Jon"
             select {name: st.fname};

    // let clause
    var v4 = from var st in students
                let string name = st.fname + " " + st.lname
             select {id: st.id, name: name};

    // join clause
    var v5 = from var st in students
                join var {id, name} in people on st.id equals id
             select {name: name, gpa: st.gpa};

    // order by clause
    var v6 = from var st in students
                order by st.id
             select st;

    // limit clause
    var v7 = from var st in students
                limit LIMIT
             select st;

    // on conflict clause
    table<Person> key(id)|error v8 = table key(id) from var st in students.toStream()
                               select <Person>{id: st.id, name: st.fname, age: st.age}
                               on conflict error("Conflicted Key", cKey = st.id);

    // group by clause
    var v9 = from var {name, age} in people
                group by age
             select age;
}

// utils

type Person record {|
    readonly int id;
    string name;
    int age?;
    Person parent?;
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
    Person p1 = {id: 1, name: "Jon Doe"};
    Person p2 = {id: 2, name: "Jane Doe"};
    return [p1, p2];
}



Student[] moduleStudents = getStudents();
Person[] modulePeople = getPeople();

// basic/minimal
() v11 = from var st in moduleStudents
    select {id: st.id, name: st.fname, age: st.age};
() v12 = from var st in moduleStudents
    select <Person>{id: st.id, name: st.fname, age: st.age};

// where clause
Student v13 = from var st in moduleStudents
    where st.fname == "Jon"
    select {name: st.fname};

// let clause
Person v14 = from var st in moduleStudents
    let string name = st.fname + " " + st.lname
    select {id: st.id, name: name};

// join clause
Person v15 = from var st in moduleStudents
    join var {id, name} in modulePeople on st.id equals id
    select {name: name, gpa: st.gpa};

// order by clause
Person v16 = from var st in moduleStudents
    order by st.id
    select st;

// limit clause
() v17 = from var st in moduleStudents
    limit LIMIT
    select st;

// on conflict clause
() v18 = table key(id) from var st in moduleStudents.toStream()
    select <Person>{id: st.id, name: st.fname, age: st.age}
    on conflict error("Conflicted Key", cKey = st.id);

// group by clause
Student v19 = from var {name, age} in modulePeople
    group by age
    select age;

function testQueryExprWithCollect() {
    return from var {age, gpa} in getStudents()
        let var ageScore = (50 - age) * gpa
        collect sum(ageScore);
}
