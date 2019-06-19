// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

type Person record {
    string name;
    int age;
    string status;
    string school;
    int count;
};

int index = 0;
stream<Teacher> inputStreamTimeAccumulatingWindow = new;
stream<Person> outputStreamTimeAccumulatingWindow = new;
Teacher[] globalEmployeeArray = [];

function startAccumulatingTimeWindowTest() returns (Teacher[]) {

    Teacher[] teachers = [];
    Teacher t1 = { name: "Mohan", age: 30, status: "single", school: "Hindu College" };
    Teacher t2 = { name: "Raja", age: 45, status: "single", school: "Hindu College" };
    Teacher t3 = { name: "Naveen", age: 35, status: "single", school: "Hindu College" };
    Teacher t4 = { name: "Amal", age: 50, status: "married", school: "Hindu College" };
    Teacher t5 = { name: "Nimal", age: 55, status: "married", school: "Hindu College" };
    Teacher t6 = { name: "Kavindu", age: 55, status: "married", school: "Hindu College" };

    testAccumulatingTimeWindow();

    outputStreamTimeAccumulatingWindow.subscribe(printTeachers);

    inputStreamTimeAccumulatingWindow.publish(t1);
    inputStreamTimeAccumulatingWindow.publish(t2);
    runtime:sleep(1200);
    inputStreamTimeAccumulatingWindow.publish(t3);
    inputStreamTimeAccumulatingWindow.publish(t4);
    inputStreamTimeAccumulatingWindow.publish(t5);
    runtime:sleep(1200);
    inputStreamTimeAccumulatingWindow.publish(t6);

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

function testAccumulatingTimeWindow() {

    forever {
        from inputStreamTimeAccumulatingWindow window timeAccum(1000)
        select inputStreamTimeAccumulatingWindow.name, inputStreamTimeAccumulatingWindow.age,
                inputStreamTimeAccumulatingWindow.status, inputStreamTimeAccumulatingWindow.school, count() as count
        => (Person [] emp) {
            foreach var e in emp {
                outputStreamTimeAccumulatingWindow.publish(e);
            }
        }
    }
}

function printTeachers(Person e) {
    addToGlobalEmployeeArray(e);
}

function addToGlobalEmployeeArray(Person e) {
    globalEmployeeArray[index] = e;
    index = index + 1;
}
