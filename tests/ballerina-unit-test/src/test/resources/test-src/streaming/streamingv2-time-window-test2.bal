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
    int count;
};

int index = 0;
stream<Teacher> inputStreamTimeWindowTest2;
stream<Teacher > outputStreamTimeWindowTest2;
Teacher[] globalEmployeeArray = [];

function startTimeWindowTest2() returns (Teacher[]) {

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

    testTimeWindow();

    outputStreamTimeWindowTest2.subscribe(printTeachers);
    foreach t in teachers {
        inputStreamTimeWindowTest2.publish(t);
        runtime:sleep(1200);
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

function testTimeWindow() {

    forever {
        from inputStreamTimeWindowTest2 window timeWindow([2000])
        select inputStreamTimeWindowTest2.name, inputStreamTimeWindowTest2.age, inputStreamTimeWindowTest2.status, inputStreamTimeWindowTest2
        .school, count() as count
        group by inputStreamTimeWindowTest2.school
        => (Teacher [] emp) {
            foreach e in emp {
                outputStreamTimeWindowTest2.publish(e);
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