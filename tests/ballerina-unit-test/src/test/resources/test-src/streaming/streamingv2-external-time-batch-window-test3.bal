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
stream<Teacher> inputStreamExternalTimeBatchTest3;
stream<TeacherOutput > outputStreamExternalTimeBatchTest3;
TeacherOutput[] globalEmployeeArray = [];

function startExternalTimeBatchwindowTest3() returns (TeacherOutput[]) {

    Teacher[] teachers = [];
    Teacher t1 = { timestamp: 1366335804341, name: "Mohan", age: 30, status: "single", school: "Hindu College" };
    Teacher t2 = { timestamp: 1366335804599, name: "Raja", age: 45, status: "single", school: "Hindu College" };
    Teacher t3 = { timestamp: 1366335804600, name: "Naveen", age: 35, status: "single", school: "Hindu College" };
    Teacher t4 = { timestamp: 1366335804607, name: "Amal", age: 50, status: "married", school: "Hindu College" };

    teachers[0] = t1;
    teachers[1] = t2;
    teachers[2] = t3;
    teachers[3] = t4;

    testExternalTimeBatchwindow3();

    outputStreamExternalTimeBatchTest3.subscribe(printTeachers);
    foreach t in teachers {
        inputStreamExternalTimeBatchTest3.publish(t);
    }

    int count = 0;
    while(true) {
        runtime:sleep(500);
        count += 1;
        if((globalEmployeeArray.length()) == 1 || count == 10) {
            break;
        }
    }
    return globalEmployeeArray;
}

function testExternalTimeBatchwindow3() {

    forever {
        from inputStreamExternalTimeBatchTest3 window externalTimeBatchWindow(
                                                          [inputStreamExternalTimeBatchTest3.timestamp, 1000, (),3000])
        select inputStreamExternalTimeBatchTest3.timestamp, inputStreamExternalTimeBatchTest3.name, count() as count
        group by inputStreamExternalTimeBatchTest3.school
        => (TeacherOutput [] teachers) {
            foreach t in teachers {
                outputStreamExternalTimeBatchTest3.publish(t);
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