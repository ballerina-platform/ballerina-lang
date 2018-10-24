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

int index = 0;
stream<Teacher> inputStreamTimeWindowTest;
stream<Teacher > outputStreamTimeWindowTest;
Teacher[] globalEmployeeArray = [];

function startTimeWindowTest() returns (Teacher[]) {

    Teacher[] teachers = [];
    Teacher t1 = { name: "Mohan", age: 30, status: "single", school: "Hindu College" };
    Teacher t2 = { name: "Raja", age: 45, status: "single", school: "Hindu College" };

    teachers[0] = t1;
    teachers[1] = t2;

    testTimeWindow();

    outputStreamTimeWindowTest.subscribe(printTeachers);
    foreach t in teachers {
        inputStreamTimeWindowTest.publish(t);
    }

    int count = 0;
    while(true) {
        runtime:sleep(500);
        count += 1;
        if((lengthof globalEmployeeArray) == 2 || count == 10) {
            break;
        }
    }

    return globalEmployeeArray;
}

function testTimeWindow() {

    forever {
        from inputStreamTimeWindowTest window timeWindow("1000")
        select inputStreamTimeWindowTest.name, inputStreamTimeWindowTest.age, inputStreamTimeWindowTest.status, inputStreamTimeWindowTest
        .school
        => (Teacher [] emp) {
            foreach e in emp {
                outputStreamTimeWindowTest.publish(e);
            }
        }
    }
}

function printTeachers(Teacher e) {
    addToGlobalEmployeeArray(e);
}

function addToGlobalEmployeeArray(Teacher e) {
    globalEmployeeArray[index] = e;
    index = index + 1;
}
