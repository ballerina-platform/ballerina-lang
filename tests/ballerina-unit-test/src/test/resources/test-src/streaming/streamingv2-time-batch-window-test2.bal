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
stream<Teacher> inputStreamTimeBatchTest2;
stream<TeacherOutput > outputStreamTimeBatchTest2;
TeacherOutput[] globalEmployeeArray = [];

function startTimeBatchwindowTest2() returns (TeacherOutput[]) {

    Teacher[] teachers = [];
    Teacher t1 = { name: "Mohan", age: 30, status: "single", school: "Hindu College" };
    Teacher t2 = { name: "Raja", age: 45, status: "single", school: "Hindu College" };
    Teacher t3 = { name: "Naveen", age: 35, status: "single", school: "Hindu College" };
    Teacher t4 = { name: "Amal", age: 50, status: "married", school: "Hindu College" };
    Teacher t5 = { name: "Nimal", age: 55, status: "married", school: "Hindu College" };

    testTimeBatchwindow();

    outputStreamTimeBatchTest2.subscribe(printTeachers);

    inputStreamTimeBatchTest2.publish(t1);
    inputStreamTimeBatchTest2.publish(t2);
    runtime:sleep(1200);
    inputStreamTimeBatchTest2.publish(t3);
    inputStreamTimeBatchTest2.publish(t4);
    runtime:sleep(3000);
    inputStreamTimeBatchTest2.publish(t5);

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

function testTimeBatchwindow() {

    forever {
        from inputStreamTimeBatchTest2 window timeBatchWindow(1000)
        select inputStreamTimeBatchTest2.name, count() as count
        group by inputStreamTimeBatchTest2.school
        => (TeacherOutput [] emp) {
            foreach e in emp {
                outputStreamTimeBatchTest2.publish(e);
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