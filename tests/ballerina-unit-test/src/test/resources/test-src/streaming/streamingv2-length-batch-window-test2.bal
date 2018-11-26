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
stream<Teacher> inputStreamLengthbatchTest2;
stream<TeacherOutput > outputStreamLengthBatchTest2;
TeacherOutput[] globalEmployeeArray = [];

function startLengthBatchwindowTest2() returns (TeacherOutput[]) {

    Teacher[] teachers = [];
    Teacher t1 = { name: "Mohan", age: 30, status: "single", school: "Hindu College" };
    Teacher t2 = { name: "Raja", age: 45, status: "single", school: "Hindu College" };
    Teacher t3 = { name: "Naveen", age: 35, status: "single", school: "Hindu College" };
    Teacher t4 = { name: "Amal", age: 50, status: "married", school: "Hindu College" };
    Teacher t5 = { name: "Nimal", age: 55, status: "married", school: "Hindu College" };
    Teacher t6 = { name: "Kavindu", age: 55, status: "married", school: "Hindu College" };

    teachers[0] = t1;
    teachers[1] = t2;
    teachers[2] = t3;
    teachers[3] = t4;
    teachers[4] = t5;
    teachers[5] = t6;

    testLengthBatchwindow();

    outputStreamLengthBatchTest2.subscribe(printTeachers);
    foreach t in teachers {
        inputStreamLengthbatchTest2.publish(t);
    }

    int count = 0;
    while(true) {
        runtime:sleep(500);
        count += 1;
        if((lengthof globalEmployeeArray) == 3 || count == 10) {
            break;
        }
    }

    return globalEmployeeArray;
}

function testLengthBatchwindow() {

    forever {
        from inputStreamLengthbatchTest2 window lengthBatchWindow(2)
        select inputStreamLengthbatchTest2.name, count() as count
        group by inputStreamLengthbatchTest2.school
        => (TeacherOutput [] emp) {
            foreach e in emp {
                outputStreamLengthBatchTest2.publish(e);
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