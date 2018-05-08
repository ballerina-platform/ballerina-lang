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
};

Employee[] globalEmployeeArray = [];
int employeeIndex = 0;

stream<Employee> employeeStream3;
stream<Teacher> teacherStream6;

function testPassthroughQuery() {

    forever {
        from teacherStream6
        select *
        => (Employee[] emp) {
            employeeStream3.publish(emp);
        }
    }
}

function startPassthroughQuery() returns (Employee[]) {

    testPassthroughQuery();

    Teacher t1 = {name:"Raja", age:25, status:"single"};
    Teacher t2 = {name:"Shareek", age:33, status:"single"};
    Teacher t3 = {name:"Nimal", age:45, status:"married"};

    employeeStream3.subscribe(printEmployeeNumber);

    teacherStream6.publish(t1);
    teacherStream6.publish(t2);
    teacherStream6.publish(t3);

    runtime:sleep(1000);

    return globalEmployeeArray;
}

function printEmployeeNumber(Employee e) {
    addToGlobalEmployeeArray(e);
}

function addToGlobalEmployeeArray(Employee e) {
    globalEmployeeArray[employeeIndex] = e;
    employeeIndex = employeeIndex + 1;
}