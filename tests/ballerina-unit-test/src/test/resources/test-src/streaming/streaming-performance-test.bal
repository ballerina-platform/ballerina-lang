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
};

Employee[] globalEmployeeArray = [];
int employeeIndex = 0;

int outputEventCount = 0;
int startTime = 0;
stream<Employee> employeeStream;
stream<Teacher> teacherStream1;


//Siddhi based Streaming query
function testFilterQuery() {
    forever {
        from teacherStream1
        where age > 30
        select name, age, status
        => (Employee[] emp) {
            employeeStream.publish(emp);
        }
    }
}

//Ballerina based Stream query
//function testFilterQuery() {
//    forever {
//        from teacherStream1
//        where teacherStream1.age > 30
//        select teacherStream1.name, teacherStream1.age, teacherStream1.status
//        => (Employee[] emp) {
//            employeeStream.publish(emp);
//        }
//    }
//}


function startFilterQuery() returns (Employee[]) {

    testFilterQuery();
    Teacher t1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    Teacher t2 = { name: "Shareek", age: 33, status: "single", batch: "LK1998", school: "Thomas College" };
    Teacher t3 = { name: "Nimal", age: 45, status: "married", batch: "LK1988", school: "Ananda College" };
    employeeStream.subscribe(printEmployeeNumber);
    time:Time time = time:currentTime();
    startTime = time.time;

    int i = 0;
    while (i < 1000000) {
        teacherStream1.publish(t1);
        teacherStream1.publish(t2);
        teacherStream1.publish(t3);
    }

    runtime:sleep(100000);

    return globalEmployeeArray;
}

function printEmployeeNumber(Employee e) {

    outputEventCount++;
    if (outputEventCount % 10000 == 0) {
        time:Time time = time:currentTime();
        int endTime = time.time;
        float throughput = (endTime - startTime) / 10000.0;

        io:println("Throughput : ", throughput);
        time = time:currentTime();
        startTime = time.time;
    }
}
