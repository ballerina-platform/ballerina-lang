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
    string batch;
    string school;
};

type TeacherOutput record{
    string TeacherName;
    int age;
};

int index = 0;
TeacherOutput[] globalEmployeeArray = [];

function startSelectQuery() returns (TeacherOutput[]) {


    stream<Teacher> inputStream1 = new;
    stream<TeacherOutput> outputStream1 = new;
    Teacher[] teachers = [];
    Teacher t1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Ananda College" };
    Teacher t2 = { name: "Mohan", age: 45, status: "single", batch: "LK2014", school: "Hindu College" };
    Teacher t3 = { name: "Shareek", age: 50, status: "single", batch: "LK2014", school: "Zahira College" };
    teachers[0] = t1;
    teachers[1] = t2;
    teachers[2] = t3;

    testSelectQuery(inputStream1, outputStream1);

    outputStream1.subscribe(printTeachers);
    foreach var t in teachers {
        inputStream1.publish(t);
    }

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

function testSelectQuery(stream<Teacher> inputStream, stream<TeacherOutput> outputStream) {
    int age = 25;
    map<string> names = {};
    map<int> ageMap = {};
    ageMap["1"] = 25;
    names["45"] = "Mohan";
    names["50"] = "Shareek";
    forever {
        from inputStream where inputStream.age > age
        select <string>names[inputStream.age.toString()] as TeacherName, inputStream.age as age
        having age > <int>ageMap["1"]
        => (TeacherOutput[] emp) {
            foreach var e in emp {
                outputStream.publish(e);
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