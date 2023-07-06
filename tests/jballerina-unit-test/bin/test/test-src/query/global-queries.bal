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

type Department record {|
   int id;
   string name;
|};

type Person record {|
   int id;
   string fname;
   string lname;
|};

type Employee record {|
   int personId;
   int deptId;
|};

type DeptPerson record {|
   string fname;
   string lname;
   string? dept;
|};

type DeptPersonValue record {|
    DeptPerson value;
|};

function getDeptPersonValue((record {| DeptPerson value; |}|error?)|(record {| DeptPerson value; |}?) returnedVal)
        returns DeptPersonValue? {
    var result = returnedVal;
    if (result is DeptPersonValue) {
        return result;
    } else {
        return ();
    }
}

function getDeptName(int id) returns string {
    if (id == 1) {
        return "HR";
    } if (id == 2) {
        return "Operations";
    } else {
        return "Engineering";
    }
}

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

DeptPerson[] globalQuery1 = from var person in personList
       join var {id: deptId, name: deptName} in deptList
       on person.id equals deptId
       select {
           fname : person.fname,
           lname : person.lname,
           dept : deptName
       };

function testGlobalQuery1() returns boolean {
    DeptPerson[] deptPersonList = globalQuery1;
    boolean testPassed = true;
    DeptPerson dp;
    any res = deptPersonList;
    testPassed = testPassed && res is DeptPerson[];
    testPassed = testPassed && res is (any|error)[];
    testPassed = testPassed && deptPersonList.length() == 3;
    dp = deptPersonList[0];
    testPassed = testPassed && dp.fname == "Alex" && dp.lname == "George" && dp.dept == "HR";
    dp = deptPersonList[1];
    testPassed = testPassed && dp.fname == "Ranjan" && dp.lname == "Fonseka" && dp.dept == "Operations";
    return testPassed;
}

DeptPerson[] globalQuery2 = from var person in personList
       let string deptName = "HR"
       join var {id,name} in deptList
       on deptName equals getDeptName(id)
       select {
           fname : person.fname,
           lname : person.lname,
           dept : name
       };

function testGlobalQuery2() returns boolean {
    DeptPerson[] deptPersonList = globalQuery2;
    boolean testPassed = true;
    DeptPerson dp;
    any res = deptPersonList;
    testPassed = testPassed && res is DeptPerson[];
    testPassed = testPassed && res is (any|error)[];
    testPassed = testPassed && deptPersonList.length() == 4;
    dp = deptPersonList[0];
    testPassed = testPassed && dp.fname == "Alex" && dp.lname == "George" && dp.dept == "HR";
    dp = deptPersonList[1];
    testPassed = testPassed && dp.fname == "Ranjan" && dp.lname == "Fonseka" && dp.dept == "HR";
    dp = deptPersonList[2];
    testPassed = testPassed && dp.fname == "Idris" && dp.lname == "Elba" && dp.dept == "HR";
    dp = deptPersonList[3];
    testPassed = testPassed && dp.fname == "Dermot" && dp.lname == "Crowley" && dp.dept == "HR";
    return testPassed;
}

DeptPerson[] globalQuery3 = from var emp in
            (stream from var e in
                (table key() from var e1 in
                    (from var e2 in empList select e2)
                select e1)
             select e)
        join Person psn in (table key() from var p in personList select p)
            on emp.personId equals psn.id
        join Department dept in (from var d in deptList
                //let string deptName = "Engineering"
                where d.name == "Engineering"
                select d)
            on emp.deptId equals dept.id
        select {
            fname : psn.fname,
            lname : psn.lname,
            dept : dept.name
        };

function testGlobalQuery3() returns boolean {
    DeptPerson[] deptPersonList = globalQuery3;
    boolean testPassed = true;
    DeptPerson dp;
    any res = deptPersonList;
    testPassed = testPassed && res is DeptPerson[];
    testPassed = testPassed && res is (any|error)[];
    testPassed = testPassed && deptPersonList.length() == 2;
    dp = deptPersonList[0];
    testPassed = testPassed && dp.fname == "Idris" && dp.lname == "Elba" && dp.dept == "Engineering";
    dp = deptPersonList[1];
    testPassed = testPassed && dp.fname == "Dermot" && dp.lname == "Crowley" && dp.dept == "Engineering";
    return testPassed;
}

DeptPerson[] globalQuery4 = from var person in personList
       let string hrDep = "HR"
       join var {id,name} in deptList
       on hrDep equals getDeptName(id)
       let string deptName = "WSO2_".concat(hrDep)
       select {
           fname : person.fname,
           lname : person.lname,
           dept : deptName
       };

function testGlobalQuery4() returns boolean {
    DeptPerson[] deptPersonList = globalQuery4;
    boolean testPassed = true;
    DeptPerson dp;
    any res = deptPersonList;
    testPassed = testPassed && res is DeptPerson[];
    testPassed = testPassed && res is (any|error)[];
    testPassed = testPassed && deptPersonList.length() == 4;
    dp = deptPersonList[0];
    testPassed = testPassed && dp.fname == "Alex" && dp.lname == "George" && dp.dept == "WSO2_HR";
    dp = deptPersonList[3];
    testPassed = testPassed && dp.fname == "Dermot" && dp.lname == "Crowley" && dp.dept == "WSO2_HR";
    return testPassed;
}

int[] ints = from var i in from var j in [1, 2, 3] select j select i;

function testGlobalQuery5() returns boolean {
    return ints == [1, 2, 3];
}
