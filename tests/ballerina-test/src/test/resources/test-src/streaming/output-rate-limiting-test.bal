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

import ballerina/io;
import ballerina/runtime;

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

stream<Employee> employeeStream1;
stream<Teacher> teacherStream2;

function testOutputRateLimitQuery() {

    forever {
        from teacherStream2
        select name, age, status
        output first every 3 events
        => (Employee[] emp) {
            employeeStream1.publish(emp);
        }
    }
}

function startOutputRateLimitQuery() returns (Employee[]) {

    testOutputRateLimitQuery();

    Teacher t1 = {name:"Raja", age:25, status:"single", batch:"LK2014", school:"Hindu College"};
    Teacher t2 = {name:"Shareek", age:33, status:"single", batch:"LK1998", school:"Thomas College"};
    Teacher t3 = {name:"Nimal", age:45, status:"married", batch:"LK1988", school:"Ananda College"};

    Teacher t4 = {name:"Praveen", age:29, status:"single", batch:"LK2013", school:"Hindu College"};
    Teacher t5 = {name:"Azraar", age:30, status:"married", batch:"LK1989", school:"Thomas College"};
    Teacher t6 = {name:"Kasun", age:30, status:"married", batch:"LK1987", school:"Ananda College"};


    employeeStream1.subscribe(printEmployeeNumber);

    teacherStream2.publish(t1);
    teacherStream2.publish(t2);
    teacherStream2.publish(t3);
    teacherStream2.publish(t4);
    teacherStream2.publish(t5);
    teacherStream2.publish(t6);

    runtime:sleepCurrentWorker(1000);

    return globalEmployeeArray;
}

function printEmployeeNumber(Employee e) {
    io:println("printEmployeeName function invoked for Employee event for Employee employee name:" + e.name);
    addToGlobalEmployeeArray(e);
}

function addToGlobalEmployeeArray(Employee e) {
    globalEmployeeArray[employeeIndex] = e;
    employeeIndex = employeeIndex + 1;
}