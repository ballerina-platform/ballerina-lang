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
stream<Teacher> inputStream;
stream<TeacherOutput> outputStream;

TeacherOutput[] globalTeacherOutputArray = [];

function startGroupByQueryWithFunc() returns TeacherOutput[] {

    Teacher[] teachers = [];
    Teacher t1 = { name: "Mohan", age: 30, status: "single", batch: "LK2014", school: "Hindu College" };
    Teacher t2 = { name: "Raja", age: 45, status: "single", batch: "LK2014", school: "Royal College" };
    Teacher t3 = { name: "Naveen", age: 30, status: "single", batch: "LK2014", school: "Hindu College"};
    Teacher t4 = { name: "Amal", age: 50, status: "single", batch: "LK2014", school: "Hindu College"};


    teachers[0] = t1;
    teachers[1] = t2;
    teachers[2] = t3;
    teachers[3] = t4;

    foo();

    outputStream.subscribe(printTeachers);
    foreach t in teachers {
        inputStream.publish(t);
    }

    int count = 0;
    while(true) {
        runtime:sleep(500);
        count += 1;
        if((lengthof globalTeacherOutputArray) == 4 || count == 10) {
            break;
        }
    }
    return globalTeacherOutputArray;
}

function getGroupByField(int a) returns boolean {
    if (a % 2 == 0) {
        return true;
    } else {
        return false;
    }
}

function foo() {
    forever {
        from inputStream where inputStream.age > 25
        select inputStream.name, inputStream.age, count() as count
        group by getGroupByField(inputStream.age)
        => (TeacherOutput [] teachers) {
            foreach t in teachers {
                outputStream.publish(t);
            }
        }
    }
}

function printTeachers(TeacherOutput e) {
    addToGlobalEmployeeArray(e);
}

function addToGlobalEmployeeArray(TeacherOutput e) {
    globalTeacherOutputArray[index] = e;
    index = index + 1;
}
