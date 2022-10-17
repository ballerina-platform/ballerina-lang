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

type DeptPerson record {|
   string fname;
   string lname;
   string? dept;
|};

type Department record {|
   int id;
   string name;
|};

type Person record {|
   int id;
   string fname;
   string lname;
|};

function testJoinClauseWithInvalidType() returns DeptPerson[]{
    Person p1 = {id: 1, fname: "Alex", lname: "George"};
    Person p2 = {id: 2, fname: "Ranjan", lname: "Fonseka"};

    Department d1 = {id: 1, name:"HR"};
    Department d2 = {id: 2, name:"Operations"};

    Person[] personList = [p1, p2];
    Department[] deptList = [d1, d2];

    DeptPerson[] deptPersonList =
       from var person in personList
       join Person dept in deptList
       on person.id equals dept.id
       select {
           fname : person.fname,
           lname : person.lname,
           dept : dept.name
       };

    return deptPersonList;
}

function testJoinClauseWithUndefinedType() returns DeptPerson[]{
    Person p1 = {id: 1, fname: "Alex", lname: "George"};
    Person p2 = {id: 2, fname: "Ranjan", lname: "Fonseka"};

    Department d1 = {id: 1, name:"HR"};
    Department d2 = {id: 2, name:"Operations"};

    Person[] personList = [p1, p2];
    Department[] deptList = [d1, d2];

    DeptPerson[] deptPersonList =
       from var person in personList
       join XYZ dept in deptList
       on person.id equals dept.id
       select {
           fname : person.fname,
           lname : person.lname,
           dept : dept.name
       };

    return deptPersonList;
}

function testSimpleJoinWithInvalidEqualsClause1() returns DeptPerson[]{
    Person p1 = {id: 1, fname: "Alex", lname: "George"};
    Person p2 = {id: 2, fname: "Ranjan", lname: "Fonseka"};

    Department d1 = {id: 1, name:"HR"};
    Department d2 = {id: 2, name:"Operations"};

    Person[] personList = [p1, p2];
    Department[] deptList = [d1, d2];

    DeptPerson[] deptPersonList =
       from var person in personList
       join var {id: deptId, name: deptName} in deptList
       on deptId equals person.id
       select {
           fname : person.fname,
           lname : person.lname,
           dept : deptName
       };

    return deptPersonList;
}

function testSimpleJoinWithInvalidEqualsClause2() returns DeptPerson[]{
    Person p1 = {id: 1, fname: "Alex", lname: "George"};
    Person p2 = {id: 2, fname: "Ranjan", lname: "Fonseka"};

    Department d1 = {id: 1, name:"HR"};
    Department d2 = {id: 2, name:"Operations"};

    Person[] personList = [p1, p2];
    Department[] deptList = [d1, d2];

    DeptPerson[] deptPersonList =
       from var person in personList
       join var {id,name} in deptList
       on id equals person.id
       select {
           fname : person.fname,
           lname : person.lname,
           dept : name
       };

    return deptPersonList;
}

function testSimpleJoinWithInvalidEqualsClause3() returns DeptPerson[]{
    Person p1 = {id: 1, fname: "Alex", lname: "George"};
    Person p2 = {id: 2, fname: "Ranjan", lname: "Fonseka"};

    Department d1 = {id: 1, name:"HR"};
    Department d2 = {id: 2, name:"Operations"};

    Person[] personList = [p1, p2];
    Department[] deptList = [d1, d2];

    DeptPerson[] deptPersonList =
       from var person in personList
       let string deptName = "HR"
       join var {id,name} in deptList
       on name equals deptName
       select {
           fname : person.fname,
           lname : person.lname,
           dept : name
       };

    return deptPersonList;
}

function testOuterJoinWithInvalidEqualsClause1() returns DeptPerson[]{
    Person p1 = {id: 1, fname: "Alex", lname: "George"};
    Person p2 = {id: 2, fname: "Ranjan", lname: "Fonseka"};

    Department d1 = {id: 1, name:"HR"};
    Department d2 = {id: 2, name:"Operations"};

    Person[] personList = [p1, p2];
    Department[] deptList = [d1, d2];

    DeptPerson[] deptPersonList =
       from var person in personList
       outer join var {id: deptId, name: deptName} in deptList
       on deptId equals person.id
       select {
           fname : person.fname,
           lname : person.lname,
           dept : deptName
       };

    return deptPersonList;
}

function testOuterJoinWithInvalidEqualsClause2() returns DeptPerson[]{
    Person p1 = {id: 1, fname: "Alex", lname: "George"};
    Person p2 = {id: 2, fname: "Ranjan", lname: "Fonseka"};

    Department d1 = {id: 1, name:"HR"};
    Department d2 = {id: 2, name:"Operations"};

    Person[] personList = [p1, p2];
    Department[] deptList = [d1, d2];

    DeptPerson[] deptPersonList =
       from var person in personList
       outer join var {id,name} in deptList
       on id equals person.id
       select {
           fname : person.fname,
           lname : person.lname,
           dept : name
       };

    return deptPersonList;
}

function testOuterJoinWithInvalidEqualsClause3() returns DeptPerson[]{
    Person p1 = {id: 1, fname: "Alex", lname: "George"};
    Person p2 = {id: 2, fname: "Ranjan", lname: "Fonseka"};

    Department d1 = {id: 1, name:"HR"};
    Department d2 = {id: 2, name:"Operations"};

    Person[] personList = [p1, p2];
    Department[] deptList = [d1, d2];

    DeptPerson[] deptPersonList =
       from var person in personList
       let string deptName = "HR"
       outer join var {id,name} in deptList
       on name equals deptName
       select {
           fname : person.fname,
           lname : person.lname,
           dept : name
       };

    return deptPersonList;
}

function testOuterJoinWithInvalidEqualsClause4() returns DeptPerson[]{
    Person p1 = {id: 1, fname: "Alex", lname: "George"};
    Person p2 = {id: 2, fname: "Ranjan", lname: "Fonseka"};

    Department d1 = {id: 1, name:"HR"};
    Department d2 = {id: 2, name:"Operations"};

    Person[] personList = [p1, p2];
    Department[] deptList = [d1, d2];

    DeptPerson[] deptPersonList =
       from var person in personList
       let string deptName = "HR"
       join var {id,name} in deptList
       on getDeptName(id) equals deptName
       select {
           fname : person.fname,
           lname : person.lname,
           dept : name
       };

    return deptPersonList;
}

function getDeptName(int id) returns string {
    if (id == 1) {
        return "HR";
    } else {
        return "Operations";
    }
}

function testOnClauseWithFunction() returns DeptPerson[]{
    Person p1 = {id: 1, fname: "Alex", lname: "George"};
    Person p2 = {id: 2, fname: "Ranjan", lname: "Fonseka"};

    Department d1 = {id: 1, name:"HR"};
    Department d2 = {id: 2, name:"Operations"};

    Person[] personList = [p1, p2];
    Department[] deptList = [d1, d2];

    DeptPerson[] deptPersonList =
       from var person in personList
       join Department dept in deptList
       on condition(person.fname)
       select {
           fname : person.fname,
           lname : person.lname,
           dept : dept.name
       };

    return deptPersonList;
}

function testOuterJoinWithOnClauseWithFunction() returns boolean {
    Person p1 = {id: 1, fname: "Alex", lname: "George"};
    Person p2 = {id: 2, fname: "Ranjan", lname: "Fonseka"};

    Department d1 = {id: 1, name:"HR"};
    Department d2 = {id: 2, name:"Operations"};

    Person[] personList = [p1, p2];
    Department[] deptList = [d1, d2];

    DeptPerson[] deptPersonList =
       from var person in personList
       outer join var dept in deptList
       on condition(person.fname)
       select {
           fname : person.fname,
           lname : person.lname,
           dept : dept?.name
       };
}

function testOuterJoinWithOutOnClause() returns boolean {
    Person p1 = {id: 1, fname: "Alex", lname: "George"};
    Person p2 = {id: 2, fname: "Ranjan", lname: "Fonseka"};

    Department d1 = {id: 1, name:"HR"};
    Department d2 = {id: 2, name:"Operations"};

    Person[] personList = [p1, p2];
    Department[] deptList = [d1, d2];

    DeptPerson[] deptPersonList =
       from var person in personList
       outer join var dept in deptList
       select {
           fname : person.fname,
           lname : person.lname,
           dept : dept?.name
       };
}

function testOnClauseWithoutEquals() returns boolean {
    Person p1 = {id: 1, fname: "Alex", lname: "George"};
    Person p2 = {id: 2, fname: "Ranjan", lname: "Fonseka"};

    Department d1 = {id: 1, name:"HR"};
    Department d2 = {id: 2, name:"Operations"};

    Person[] personList = [p1, p2];
    Department[] deptList = [d1, d2];

    DeptPerson[] deptPersonList =
       from var person in personList
       outer join var dept in deptList
       on person.id == dept.id
       select {
           fname : person.fname,
           lname : person.lname,
           dept : dept?.name
       };
}

function condition(string name) returns boolean{
    return name == "Alex";
}

function testOuterJoinWithoutVar() returns boolean {
    Person p1 = {id: 1, fname: "Alex", lname: "George"};
    Person p2 = {id: 2, fname: "Ranjan", lname: "Fonseka"};

    Department d1 = {id: 1, name:"HR"};
    Department d2 = {id: 2, name:"Operations"};

    Person[] personList = [p1, p2];
    Department[] deptList = [d1, d2];

    DeptPerson[] deptPersonList =
       from Person person in personList
       outer join Department dept in deptList
       select {
           fname : person.fname,
           lname : person.lname,
           dept : dept?.name
       };
}
