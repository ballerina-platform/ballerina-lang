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

type Employee record {|
   int personId;
   int deptId;
|};

type Person record {|
   int id;
   string fname;
   string lname;
|};

type Department record {|
   int id;
   string name;
|};

type DeptPerson record {|
   string fname;
   string lname;
   string? dept;
|};

type DeptPersonValue record {|
    DeptPerson value;
|};

Person[] personList = [
    {id: 1, fname: "Alex", lname: "George"},
    {id: 2, fname: "Ranjan", lname: "Fonseka"},
    {id: 3, fname: "Idris", lname: "Elba"},
    {id: 4, fname: "Dermot", lname: "Crowley"}
];

Department[] deptList = [
    {id: 1, name:"HR"},
    {id: 2, name:"Operations"},
    {id: 3, name:"Engineering"}
];

Employee[] empList = [
    {personId: 1, deptId: 2},
    {personId: 2, deptId: 1},
    {personId: 3, deptId: 3},
    {personId: 4, deptId: 3}
];

function testScopeVisibilityOfLetClause() {

    DeptPerson[] deptPersonList =
        from var emp in (stream from var e in empList select e)
        join Person psn in (table key() from var p in personList select p)
            on emp.personId equals psn.id
        let string deptName = "Engineering"
        join Department dept in (from var d in deptList
            where d.name == deptName
            select d)
            on emp.deptId equals dept.id
        select {
            fname : psn.fname,
            lname : psn.lname,
            dept : dept.name
        };

}


function testScopeVisibilityOfJoinOnClose() {

    DeptPerson[] deptPersonList =
        from var emp in (stream from var e in empList select e)
        join Person psn in (table key() from var p in personList select p)
            on psn.id equals emp.personId
        join Department dept in (from var d in deptList
            let string deptName = "Engineering"
            where d.name == deptName
            select d)
            on emp.deptId equals dept.id
        select {
            fname : psn.fname,
            lname : psn.lname,
            dept : dept.name
        };

}
