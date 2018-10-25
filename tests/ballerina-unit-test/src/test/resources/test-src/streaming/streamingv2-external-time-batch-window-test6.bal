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
stream<Teacher> inputStreamExternalTimeBatchTest6;
stream<TeacherOutput > outputStreamExternalTimeBatchTest6;
TeacherOutput[] globalEmployeeArray = [];

function startExternalTimeBatchwindowTest6() returns (TeacherOutput[]) {

    Teacher[] teachers = [];
    Teacher t1 = { timestamp: 1000, name: "Mohan", age: 30, status: "single", school: "Hindu College" };
    Teacher t2 = { timestamp: 1200, name: "Raja", age: 45, status: "single", school: "Hindu College" };
    Teacher t3 = { timestamp: 1800, name: "Naveen", age: 35, status: "single", school: "Hindu College" };
    Teacher t4 = { timestamp: 2200, name: "Amal", age: 50, status: "married", school: "Hindu College" };
    Teacher t5 = { timestamp: 2600, name: "Nimal", age: 55, status: "married", school: "Hindu College" };

    teachers[0] = t1;
    teachers[1] = t2;
    teachers[2] = t3;
    teachers[3] = t4;
    teachers[4] = t5;

    testExternalTimeBatchwindow6();

    outputStreamExternalTimeBatchTest6.subscribe(printTeachers);
    foreach t in teachers {
        inputStreamExternalTimeBatchTest6.publish(t);
        runtime:sleep(450);
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

function testExternalTimeBatchwindow6() {

    forever {
        from inputStreamExternalTimeBatchTest6 window externalTimeBatchWindow(inputStreamExternalTimeBatchTest6.timestamp, 1000, startTime = 500, timeOut = 1200)
        select inputStreamExternalTimeBatchTest6.timestamp, inputStreamExternalTimeBatchTest6.name, count() as count
        group by inputStreamExternalTimeBatchTest6.school
        => (TeacherOutput [] teachers) {
            foreach t in teachers {
                outputStreamExternalTimeBatchTest6.publish(t);
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