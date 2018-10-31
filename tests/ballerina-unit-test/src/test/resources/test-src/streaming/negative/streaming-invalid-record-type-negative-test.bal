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
import ballerina/time;

type Employee record {
    string name;
    int age;
    string status;
};

type Teacher record {
    string name;
    int age;
    string status;
    string batch;
    string school;
    time:Time pea;
};

Employee[] globalEmployeeArray = [];
int employeeIndex = 0;

stream<Employee> employeeStream;
stream<Teacher> teacherStream1;

function testFilterQuery() {
    forever {
        from teacherStream1
        where age > 30
        select name, age, status
        => (Employee[] emp) {
            foreach e in emp {
                employeeStream.publish(e);
            }
        }
    }
}


function startFilterQuery() returns (Employee[]) {

    testFilterQuery();
    return globalEmployeeArray;
}

function printEmployeeNumber(Employee e) {
    addToGlobalEmployeeArray(e);
}

function addToGlobalEmployeeArray(Employee e) {
    globalEmployeeArray[employeeIndex] = e;
    employeeIndex = employeeIndex + 1;
}