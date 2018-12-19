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
    int timestamp;
    string name;
    int age;
    string status;
    string school;
};

type TeacherOutput record{
    int timestamp;
    string name;
    int count;
};

int index = 0;
stream<Teacher> inputStreamTimeLengthWindowTest2 = new;
stream<TeacherOutput > outputStreamTimeLengthTest2 = new;
TeacherOutput[] globalEmployeeArray = [];

function startTimeLengthwindowTest2() returns (TeacherOutput[]) {

    Teacher[] teachers = [];
    Teacher t1 = {timestamp:1234, name: "Mohan", age: 30, status: "single", school: "Hindu College" };
    Teacher t2 = {timestamp:1234, name: "Raja", age: 45, status: "single", school: "Hindu College" };
    Teacher t3 = {timestamp:1235, name: "Naveen", age: 45, status: "single", school: "Hindu College" };
    Teacher t4 = {timestamp:1236, name: "Amal", age: 55, status: "married", school: "Hindu College" };
    Teacher t5 = {timestamp:1237, name: "Nimal", age: 55, status: "married", school: "Hindu College" };
    Teacher t6 = {timestamp:1238, name: "Kavindu", age: 55, status: "married", school: "Hindu College" };

    teachers[0] = t1;
    teachers[1] = t2;
    teachers[2] = t3;
    teachers[3] = t4;
    teachers[4] = t5;
    teachers[5] = t6;

    testTimeLengthwindow();

    outputStreamTimeLengthTest2.subscribe(function(TeacherOutput e) {printTeachers(e);});
    foreach var t in teachers {
        inputStreamTimeLengthWindowTest2.publish(t);
        runtime:sleep(450);
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

function testTimeLengthwindow() {

    forever {
        from inputStreamTimeLengthWindowTest2 window timeLength(2000, 3)
        select inputStreamTimeLengthWindowTest2.timestamp, inputStreamTimeLengthWindowTest2.name, count() as count
        group by inputStreamTimeLengthWindowTest2.school
        => (TeacherOutput [] teachers) {
            foreach var t in teachers {
                outputStreamTimeLengthTest2.publish(t);
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