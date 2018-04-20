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
stream<Employee> employeeStream4;
stream<Teacher> teacherStream7;

function testFilterQuery() {

    int i = 1;

    forever {
        from teacherStream7
        where age > 30
        select name, age, status
        => (Employee[] emp) {
            io:println("Filterted event received #: "+ i);
            employeeStream4.publish(emp);
        }
    }
}

function startInlineOperationQuery() returns (Employee[]) {

    testFilterQuery();
    Teacher t1 = {name:"Raja", age:25, status:"single", batch:"LK2014", school:"Hindu College"};
    Teacher t2 = {name:"Shareek", age:33, status:"single", batch:"LK1998", school:"Thomas College"};
    Teacher t3 = {name:"Nimal", age:45, status:"married", batch:"LK1988", school:"Ananda College"};

    employeeStream4.subscribe(printEmployeeNumber);

    teacherStream7.publish(t1);
    teacherStream7.publish(t2);
    teacherStream7.publish(t3);

    runtime:sleep(1000);

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