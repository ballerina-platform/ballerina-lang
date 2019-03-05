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
    int marks;
    int credits;
};

int index = 0;
stream<Teacher> inputStream = new;
stream<Teacher> outputStream = new;
Teacher[] globalEmployeeArray = [];

function startFilterQuery() returns (Teacher[]) {

    Teacher[] teachers = [];
    Teacher t1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", marks: 4, credits: 2 };
    Teacher t2 = { name: "Mohan", age: 18, status: "single", batch: "LK2013", marks: 3, credits: 2 };
    Teacher t3 = { name: "Shareek", age: 18, status: "single", batch: "LK2014", marks: 2, credits: 2 };
    teachers[0] = t1;
    teachers[1] = t2;
    teachers[2] = t3;

    testFilterQuery();

    outputStream.subscribe(function(Teacher e) {printTeachers(e);});
    foreach var t in teachers {
        inputStream.publish(t);
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

function testFilterQuery() {

    forever {
        from inputStream where inputStream.age > 20 || ((inputStream.marks * inputStream.credits > 5)
                && inputStream.batch != "LK2013")
        select inputStream.name, inputStream.age, inputStream.status, inputStream.batch, inputStream.marks,
        inputStream.credits
        => (Teacher[] teachers) {
            foreach var t in teachers {
                outputStream.publish(t);
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