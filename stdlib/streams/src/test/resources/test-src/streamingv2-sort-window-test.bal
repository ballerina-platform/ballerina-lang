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
    string batch;
    string school;
};

type TeacherOutput record {
    string name;
    int age;
    int count;
};

int index = 0;
stream<Teacher> inputStreamSortWindowTest1 = new;
stream<TeacherOutput> outputStreamSortWindowTest1 = new;
TeacherOutput[] globalEmployeeArray = [];

function startSortWindowTest1() returns TeacherOutput[] {

    Teacher[] teachers = [];
    Teacher t1 = { name: "Mohan", age: 30, status: "single", batch: "LK2014", school: "Hindu College"};
    Teacher t2 = { name: "Raja", age: 90, status: "single", batch: "LK2014", school: "Hindu College"};
    Teacher t3 = { name: "Naveen", age: 35, status: "single", batch: "LK2014", school: "Hindu College"};
    Teacher t4 = { name: "Amal", age: 90, status: "single", batch: "LK2014", school: "Hindu College"};
    Teacher t5 = { name: "Nimal", age: 50, status: "single", batch: "LK2014", school: "Hindu College" };
    Teacher t6 = { name: "Kamal", age: 60, status: "single", batch: "LK2014", school: "Hindu College" };

    teachers[0] = t1;
    teachers[1] = t2;
    teachers[2] = t3;
    teachers[3] = t4;
    teachers[4] = t5;
    teachers[5] = t6;

    testSortWindow();

    outputStreamSortWindowTest1.subscribe(printTeachers);
    foreach var t in teachers {
        inputStreamSortWindowTest1.publish(t);
        runtime:sleep(500);
    }

    int count = 0;
    while(true) {
        runtime:sleep(500);
        count += 1;
        if((globalEmployeeArray.length()) == 6 || count == 10) {
            break;
        }
    }

    return globalEmployeeArray;
}

function testSortWindow() {

    forever {
        from inputStreamSortWindowTest1 window sort(3, inputStreamSortWindowTest1.age, "ascending",
            inputStreamSortWindowTest1.name, "descending")
        select inputStreamSortWindowTest1.name, inputStreamSortWindowTest1.age, count() as count
        group by inputStreamSortWindowTest1.school
        => (TeacherOutput [] emp) {
            foreach var e in emp {
                outputStreamSortWindowTest1.publish(e);
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
