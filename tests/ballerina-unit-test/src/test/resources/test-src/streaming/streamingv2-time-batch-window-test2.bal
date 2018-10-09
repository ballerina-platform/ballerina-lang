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
import ballerina/streams;

type Teacher record {
    string name;
    int age;
    string status;
    string school;
};

type TeacherOutput record{
    string name;
    int count;
};

int index = 0;
stream<Teacher> inputStream;
stream<TeacherOutput > outputStream;
TeacherOutput[] globalEmployeeArray = [];

function startTimeBatchwindowTest2() returns (TeacherOutput[]) {

    Teacher[] teachers = [];
    Teacher t1 = { name: "Mohan", age: 30, status: "single", school: "Hindu College" };
    Teacher t2 = { name: "Raja", age: 45, status: "single", school: "Hindu College" };
    Teacher t3 = { name: "Naveen", age: 35, status: "single", school: "Hindu College" };
    Teacher t4 = { name: "Amal", age: 50, status: "married", school: "Hindu College" };
    Teacher t5 = { name: "Nimal", age: 55, status: "married", school: "Hindu College" };
    Teacher t6 = { name: "Kavindu", age: 55, status: "married", school: "Hindu College" };

    testTimeBatchwindow();

    outputStream.subscribe(printTeachers);

    inputStream.publish(t1);
    inputStream.publish(t2);
    runtime:sleep(1200);
    inputStream.publish(t3);
    inputStream.publish(t4);
    runtime:sleep(3000);
    inputStream.publish(t5);
    inputStream.publish(t6);


    runtime:sleep(1500);
    io:println(globalEmployeeArray);
    return globalEmployeeArray;
}

function testTimeBatchwindow() {

    forever {
        from inputStream window timeBatchWindow(1000)
        select inputStream.name, count() as count
        group by inputStream.school
        => (TeacherOutput [] emp) {
            foreach e in emp {
                outputStream.publish(e);
            }
        }
    }
}

function printTeachers(TeacherOutput e) {
    addToGlobalEmployeeArray(e);
}

function addToGlobalEmployeeArray(TeacherOutput e) {
    globalEmployeeArray[index] = e;
    index = index + 1;
}