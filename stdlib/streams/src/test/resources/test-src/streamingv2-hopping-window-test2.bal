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
stream<Teacher> inputStreamHoppingWindow2 = new;
stream<Person> outputStreamHoppingWindow2 = new;
Teacher[] globalEmployeeArray = [];

function startHoppingWindowTest2() returns (Teacher[]) {

    Teacher[] teachers = [];
    Teacher t1 = { name: "Mohan", age: 30, status: "single", school: "Hindu College" };
    Teacher t2 = { name: "Raja", age: 45, status: "single", school: "Hindu College" };
    Teacher t3 = { name: "Naveen", age: 35, status: "single", school: "Hindu College" };
    Teacher t4 = { name: "Amal", age: 50, status: "married", school: "Hindu College" };
    Teacher t5 = { name: "Nimal", age: 55, status: "married", school: "Hindu College" };
    Teacher t6 = { name: "Kavindu", age: 55, status: "married", school: "Hindu College" };

    testHoppingWindow2();

    outputStreamHoppingWindow2.subscribe(printTeachers);

    inputStreamHoppingWindow2.publish(t1);
    inputStreamHoppingWindow2.publish(t2);
    runtime:sleep(600);
    inputStreamHoppingWindow2.publish(t3);
    runtime:sleep(1100);
    inputStreamHoppingWindow2.publish(t4);
    inputStreamHoppingWindow2.publish(t5);
    inputStreamHoppingWindow2.publish(t6);

    runtime:sleep(1000);

    return globalEmployeeArray;
}

function testHoppingWindow2() {

    forever {
        from inputStreamHoppingWindow2 window hopping(100, 500)
        select inputStreamHoppingWindow2.name, inputStreamHoppingWindow2.age, inputStreamHoppingWindow2.status,
        inputStreamHoppingWindow2.school, count() as count
        => (Person [] emp) {
            foreach var e in emp {
                outputStreamHoppingWindow2.publish(e);
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
