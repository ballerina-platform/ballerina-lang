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
import ballerina/time;

type Teacher record {
    int timestamp;
    string name;
    int age;
    string school;
};

type TeacherOutput record{
    string name;
    int sumAge;
};

int index = 0;
stream<Teacher> inputStreamTimeOrderTest = new;
stream<TeacherOutput > outputStreamTimeOrderTest = new;
TeacherOutput[] globalEmployeeArray = [];

function startTimeOrderWindowTest() returns (TeacherOutput[]) {

    Teacher[] teachers = [];
    Teacher t1 = { timestamp: time:currentTime().time, name: "Mohan", age: 30, school: "Hindu College" };
    Teacher t2 = { timestamp: time:currentTime().time, name: "Raja", age: 45, school: "Hindu College" };
    Teacher t3 = { timestamp: time:currentTime().time - 1500, name: "Naveen", age: 35, school: "Hindu College" };
    Teacher t4 = { timestamp: time:currentTime().time, name: "Amal", age: 50, school: "Hindu College" };

    teachers[0] = t1;
    teachers[1] = t2;
    teachers[2] = t3;
    teachers[3] = t4;

    testTimeOrderWindow();

    outputStreamTimeOrderTest.subscribe(function(TeacherOutput e) {printTeachers(e);});
    foreach var t in teachers {
        inputStreamTimeOrderTest.publish(t);
    }

    int count = 0;
    while(true) {
        runtime:sleep(500);
        count += 1;
        if((globalEmployeeArray.length()) == 4 || count == 10) {
            break;
        }
    }
    return globalEmployeeArray;
}

function testTimeOrderWindow() {
    forever {
        from inputStreamTimeOrderTest window timeOrder(inputStreamTimeOrderTest.timestamp, 1000, false)
        select inputStreamTimeOrderTest.name, sum(inputStreamTimeOrderTest.age) as sumAge
        => (TeacherOutput [] teachers) {
            foreach var t in teachers {
                outputStreamTimeOrderTest.publish(t);
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