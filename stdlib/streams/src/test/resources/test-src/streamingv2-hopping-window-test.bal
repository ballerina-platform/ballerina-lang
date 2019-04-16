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
stream<Teacher> inputStreamHoppingWindow = new;
stream<Person> outputStreamHoppingWindow = new;
Teacher[] globalEmployeeArray = [];

function startHoppingWindowTest() returns (Teacher[]) {

    Teacher[] teachers = [];
    Teacher t1 = { name: "Mohan", age: 30, status: "single", school: "Hindu College" };
    Teacher t2 = { name: "Raja", age: 45, status: "single", school: "Hindu College" };
    Teacher t3 = { name: "Naveen", age: 35, status: "single", school: "Hindu College" };

    testHoppingWindow();

    outputStreamHoppingWindow.subscribe(printTeachers);

    inputStreamHoppingWindow.publish(t1);
    inputStreamHoppingWindow.publish(t2);
    runtime:sleep(1000);
    inputStreamHoppingWindow.publish(t3);

    int count = 0;
    while(true) {
        runtime:sleep(500);
        count += 1;
        if((globalEmployeeArray.length()) == 2 || count == 10) {
            break;
        }
    }

    return globalEmployeeArray;
}

function testHoppingWindow() {

    forever {
        from inputStreamHoppingWindow window hopping(1000, 800)
        select inputStreamHoppingWindow.name, inputStreamHoppingWindow.age, inputStreamHoppingWindow.status,
        inputStreamHoppingWindow.school, count() as count
        => (Person [] emp) {
            foreach var e in emp {
                outputStreamHoppingWindow.publish(e);
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
