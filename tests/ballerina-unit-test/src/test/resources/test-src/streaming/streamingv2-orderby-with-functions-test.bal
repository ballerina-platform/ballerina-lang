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
    string status;
    int sumAge;
    int count;
};

int index = 0;
stream<Teacher> inputStream;
stream<TeacherOutput> outputStream;

TeacherOutput[] globalTeacherOutputArray = [];

function getAge(int a, int b) returns int {
    return a * b;
}
function startOrderByQuery() returns TeacherOutput[] {

    Teacher[] teachers = [];
    teachers[0] = { name: "A", age: 12, status: "a", batch: "LK2014", school: "X College" };
    teachers[1] = { name: "g", age: 3, status: "b", batch: "LK2014", school: "X College" };
    teachers[2] = { name: "f", age: 34, status: "d", batch: "LK2014", school: "X College" };
    teachers[3] = { name: "C", age: 56, status: "f", batch: "LK2014", school: "X College" };
    teachers[4] = { name: "E", age: 6, status: "a", batch: "LK2014", school: "X College" };
    teachers[5] = { name: "B", age: 12, status: "c", batch: "LK2014", school: "X College" };
    teachers[6] = { name: "m", age: 87, status: "g", batch: "LK2014", school: "X College" };
    teachers[7] = { name: "D", age: 21, status: "b", batch: "LK2014", school: "X College" };
    teachers[8] = { name: "i", age: 13, status: "e", batch: "LK2014", school: "X College" };
    teachers[9] = { name: "h", age: 43, status: "g", batch: "LK2014", school: "X College" };

    foo();

    outputStream.subscribe(printTeachers);
    foreach t in teachers {
        inputStream.publish(t);
    }
    int count = 0;
    while(true) {
        runtime:sleep(500);
        count += 1;
        if((lengthof globalTeacherOutputArray) == 10 || count == 10) {
            break;
        }
    }
    return globalTeacherOutputArray;
}

function foo() {
    forever {
        from inputStream where inputStream.age > 2 window lengthBatchWindow(5)
        select inputStream.name, inputStream.age, inputStream.status, sum (inputStream.age) as sumAge, count() as count
        group by inputStream.name
        order by status ascending, getAge(age, getAge(age, 2)) descending => (TeacherOutput[] o) {
            foreach x in o {
                outputStream.publish(x);
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