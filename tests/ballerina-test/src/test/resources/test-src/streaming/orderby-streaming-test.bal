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

import ballerina/runtime;
import ballerina/io;

type Employee {
    string name;
    int age;
    string status;
};

type Teacher {
    string name;
    int age;
    string status;
    string batch;
    string school;
};

Employee[] globalEmployeeArray = [];
int employeeIndex = 0;

stream<Employee> employeeStream;
stream<Teacher> teacherStream;

function testOrderBy() {
    forever {
        from teacherStream window lengthBatch(10)
        select name, age, status order by age
        => (Employee[] emp) {
            employeeStream.publish(emp);
        }
    }
}


function startOrderBy() returns (Employee[]) {

    int index = 0;
    Teacher[] teachers;
    testOrderBy();

    teachers[0] = {name:"Raja", age:71, status:"single", batch:"LK2014", school:"Hindu College"};
    teachers[1] = {name:"Alex", age:34, status:"single", batch:"LK2014", school:"Hindu College"};
    teachers[2] = {name:"Chris", age:75, status:"single", batch:"LK2014", school:"Hindu College"};
    teachers[3] = {name:"Kamal", age:56, status:"single", batch:"LK2014", school:"Hindu College"};
    teachers[4] = {name:"Shiva", age:19, status:"single", batch:"LK1998", school:"Thomas College"};
    teachers[5] = {name:"Ragu", age:21, status:"single", batch:"LK1998", school:"Thomas College"};
    teachers[6] = {name:"Sarah", age:67, status:"single", batch:"LK1998", school:"Thomas College"};
    teachers[7] = {name:"Sowan", age:33, status:"married", batch:"LK1988", school:"Ananda College"};
    teachers[8] = {name:"Goege", age:12, status:"single", batch:"LK1988", school:"Ananda College"};
    teachers[9] = {name:"Jonathan", age:100, status:"married", batch:"LK1988", school:"Ananda College"};

    employeeStream.subscribe(printEmployeeNumber);

    foreach teacher in teachers {
        teacherStream.publish(teacher);
    }

    while (lengthof globalEmployeeArray != 10 || index < 20) {
        index++;
        runtime:sleep(500);
    }

    return globalEmployeeArray;
}

function printEmployeeNumber(Employee e) {
   addToGlobalEmployeeArray(e);
}

function addToGlobalEmployeeArray(Employee e) {
    globalEmployeeArray[employeeIndex] = e;
    employeeIndex = employeeIndex + 1;
}


Employee[] globalEmployeeArray2 = [];
int employeeIndex2 = 0;

stream<Employee> employeeStream2;
stream<Teacher> teacherStream2;

function testOrderBy2() {
    forever {
        from teacherStream2 window lengthBatch(10)
        select name, age, status order by age ascending
        => (Employee[] emp) {
            employeeStream2.publish(emp);
        }
    }
}


function startOrderBy2() returns (Employee[]) {

    int index = 0;
    Teacher[] teachers;
    testOrderBy2();

    teachers[0] = {name:"Raja", age:71, status:"single", batch:"LK2014", school:"Hindu College"};
    teachers[1] = {name:"Alex", age:34, status:"single", batch:"LK2014", school:"Hindu College"};
    teachers[2] = {name:"Chris", age:75, status:"single", batch:"LK2014", school:"Hindu College"};
    teachers[3] = {name:"Kamal", age:56, status:"single", batch:"LK2014", school:"Hindu College"};
    teachers[4] = {name:"Shiva", age:19, status:"single", batch:"LK1998", school:"Thomas College"};
    teachers[5] = {name:"Ragu", age:21, status:"single", batch:"LK1998", school:"Thomas College"};
    teachers[6] = {name:"Sarah", age:67, status:"single", batch:"LK1998", school:"Thomas College"};
    teachers[7] = {name:"Sowan", age:33, status:"married", batch:"LK1988", school:"Ananda College"};
    teachers[8] = {name:"Goege", age:12, status:"single", batch:"LK1988", school:"Ananda College"};
    teachers[9] = {name:"Jonathan", age:100, status:"married", batch:"LK1988", school:"Ananda College"};

    employeeStream2.subscribe(printEmployeeNumber2);

    foreach teacher in teachers {
        teacherStream2.publish(teacher);
    }

    while (lengthof globalEmployeeArray2 != 10 || index < 20) {
        index++;
        runtime:sleep(500);
    }

    return globalEmployeeArray2;
}

function printEmployeeNumber2(Employee e) {
    addToGlobalEmployeeArray2(e);
}

function addToGlobalEmployeeArray2(Employee e) {
    globalEmployeeArray2[employeeIndex2] = e;
    employeeIndex2 = employeeIndex2 + 1;
}

Employee[] globalEmployeeArray3 = [];
int employeeIndex3 = 0;

stream<Employee> employeeStream3;
stream<Teacher> teacherStream3;

function testOrderBy3() {
    forever {
        from teacherStream3 window lengthBatch(10)
        select name, age, status order by age descending
        => (Employee[] emp) {
            employeeStream3.publish(emp);
        }
    }
}


function startOrderBy3() returns (Employee[]) {

    int index = 0;
    Teacher[] teachers;
    testOrderBy3();

    teachers[0] = {name:"Raja", age:71, status:"single", batch:"LK2014", school:"Hindu College"};
    teachers[1] = {name:"Alex", age:34, status:"single", batch:"LK2014", school:"Hindu College"};
    teachers[2] = {name:"Chris", age:75, status:"single", batch:"LK2014", school:"Hindu College"};
    teachers[3] = {name:"Kamal", age:56, status:"single", batch:"LK2014", school:"Hindu College"};
    teachers[4] = {name:"Shiva", age:19, status:"single", batch:"LK1998", school:"Thomas College"};
    teachers[5] = {name:"Ragu", age:21, status:"single", batch:"LK1998", school:"Thomas College"};
    teachers[6] = {name:"Sarah", age:67, status:"single", batch:"LK1998", school:"Thomas College"};
    teachers[7] = {name:"Sowan", age:33, status:"married", batch:"LK1988", school:"Ananda College"};
    teachers[8] = {name:"Goege", age:12, status:"single", batch:"LK1988", school:"Ananda College"};
    teachers[9] = {name:"Jonathan", age:100, status:"married", batch:"LK1988", school:"Ananda College"};

    employeeStream3.subscribe(printEmployeeNumber3);

    foreach teacher in teachers {
        teacherStream3.publish(teacher);
    }

    while (lengthof globalEmployeeArray3 != 10 || index < 20) {
        index++;
        runtime:sleep(500);
    }

    return globalEmployeeArray3;
}

function printEmployeeNumber3(Employee e) {
    addToGlobalEmployeeArray3(e);
}

function addToGlobalEmployeeArray3(Employee e) {
    globalEmployeeArray3[employeeIndex3] = e;
    employeeIndex3 = employeeIndex3 + 1;
}

Employee[] globalEmployeeArray4 = [];
int employeeIndex4 = 0;

stream<Employee> employeeStream4;
stream<Teacher> teacherStream4;

function testOrderBy4() {
    forever {
        from teacherStream4 window lengthBatch(10)
        select name, age, status order by status, age
        => (Employee[] emp) {
            employeeStream4.publish(emp);
        }
    }
}


function startOrderBy4() returns (Employee[]) {

    int index = 0;
    Teacher[] teachers;
    testOrderBy4();

    teachers[0] = {name:"Raja", age:71, status:"single", batch:"LK2014", school:"Hindu College"};
    teachers[1] = {name:"Alex", age:34, status:"single", batch:"LK2014", school:"Hindu College"};
    teachers[2] = {name:"Chris", age:75, status:"single", batch:"LK2014", school:"Hindu College"};
    teachers[3] = {name:"Kamal", age:56, status:"single", batch:"LK2014", school:"Hindu College"};
    teachers[4] = {name:"Shiva", age:19, status:"single", batch:"LK1998", school:"Thomas College"};
    teachers[5] = {name:"Ragu", age:21, status:"single", batch:"LK1998", school:"Thomas College"};
    teachers[6] = {name:"Sarah", age:67, status:"single", batch:"LK1998", school:"Thomas College"};
    teachers[7] = {name:"Sowan", age:33, status:"married", batch:"LK1988", school:"Ananda College"};
    teachers[8] = {name:"Goege", age:12, status:"single", batch:"LK1988", school:"Ananda College"};
    teachers[9] = {name:"Jonathan", age:100, status:"married", batch:"LK1988", school:"Ananda College"};

    employeeStream4.subscribe(printEmployeeNumber4);

    foreach teacher in teachers {
        teacherStream4.publish(teacher);
    }

    while (lengthof globalEmployeeArray4 != 10 || index < 20) {
        index++;
        runtime:sleep(500);
    }

    return globalEmployeeArray4;
}

function printEmployeeNumber4(Employee e) {
    addToGlobalEmployeeArray4(e);
}

function addToGlobalEmployeeArray4(Employee e) {
    globalEmployeeArray4[employeeIndex4] = e;
    employeeIndex4 = employeeIndex4 + 1;
}

Employee[] globalEmployeeArray5 = [];
int employeeIndex5 = 0;

stream<Employee> employeeStream5;
stream<Teacher> teacherStream5;

function testOrderBy5() {
    forever {
        from teacherStream5 window lengthBatch(10)
        select name, age, status order by status ascending, age descending
        => (Employee[] emp) {
            employeeStream5.publish(emp);
        }
    }
}


function startOrderBy5() returns (Employee[]) {

    int index = 0;
    Teacher[] teachers;
    testOrderBy5();

    teachers[0] = {name:"Raja", age:71, status:"single", batch:"LK2014", school:"Hindu College"};
    teachers[1] = {name:"Alex", age:34, status:"single", batch:"LK2014", school:"Hindu College"};
    teachers[2] = {name:"Chris", age:75, status:"single", batch:"LK2014", school:"Hindu College"};
    teachers[3] = {name:"Kamal", age:56, status:"single", batch:"LK2014", school:"Hindu College"};
    teachers[4] = {name:"Shiva", age:19, status:"single", batch:"LK1998", school:"Thomas College"};
    teachers[5] = {name:"Ragu", age:21, status:"single", batch:"LK1998", school:"Thomas College"};
    teachers[6] = {name:"Sarah", age:67, status:"single", batch:"LK1998", school:"Thomas College"};
    teachers[7] = {name:"Sowan", age:33, status:"married", batch:"LK1988", school:"Ananda College"};
    teachers[8] = {name:"Goege", age:12, status:"single", batch:"LK1988", school:"Ananda College"};
    teachers[9] = {name:"Jonathan", age:100, status:"married", batch:"LK1988", school:"Ananda College"};

    employeeStream5.subscribe(printEmployeeNumber5);

    foreach teacher in teachers {
        teacherStream5.publish(teacher);
    }

    while (lengthof globalEmployeeArray5 != 10 || index < 20) {
        index++;
        runtime:sleep(500);
    }

    return globalEmployeeArray5;
}

function printEmployeeNumber5(Employee e) {
    addToGlobalEmployeeArray5(e);
}

function addToGlobalEmployeeArray5(Employee e) {
    globalEmployeeArray5[employeeIndex5] = e;
    employeeIndex5 = employeeIndex5 + 1;
}
