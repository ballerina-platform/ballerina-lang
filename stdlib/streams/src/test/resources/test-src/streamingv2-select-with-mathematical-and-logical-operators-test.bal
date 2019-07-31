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
    int indexNo;
    string name;
    int age;
    string status;
    string | () batch;
    string? school;
};

type TeacherOutput record{
    string teacherName;
    int netSalary;
    boolean isOlder;
    boolean isNotOlder;
    int fakeAge;
    boolean isMarried;
    string isYounger;
    string school;
    boolean isString;
    string companyName;
    string concat;
};

int index = 0;
stream<Teacher> inputStream = new;
stream<TeacherOutput> outputStream = new;
TeacherOutput[] globalEmployeeArray = [];
int grossSalary = 10000;
int deductions = 1000;
int|() x = ();
string[] companyArr = ["wso2", "google", "microsoft"];

function startSelectQuery() returns (TeacherOutput[]) {

    Teacher[] teachers = [];
    Teacher t1 = { indexNo: 0, name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Ananda College" };
    Teacher t2 = { indexNo: 1, name: "Mohan", age: 45, status: "single", batch: "LK2014", school: "Hindu College" };
    Teacher t3 = { indexNo: 2, name: "Shareek", age: 50, status: "single", batch: "LK2014", school: () };
    teachers[0] = t1;
    teachers[1] = t2;
    teachers[2] = t3;

    testSelectQuery();

    outputStream.subscribe(function(TeacherOutput e) {printTeachers(e);});
    foreach var t in teachers {
        inputStream.publish(t);
    }

    int count = 0;
    while(true) {
        runtime:sleep(500);
        count += 1;
        if((globalEmployeeArray.length()) == 3 || count == 10) {
            break;
        }
    }
    return globalEmployeeArray;
}

function testSelectQuery() {

    forever {
        from inputStream
        select inputStream.name as teacherName,
                (grossSalary - deductions) as netSalary, inputStream.age > 30 as isOlder,
                !(inputStream.age > 30) as isNotOlder, (10 * inputStream.age - 10) / 10 as fakeAge,
                (inputStream.age >= 50) && inputStream.status == "single" as isMarried,
                inputStream.age > 30 ? "older" : "younger" as isYounger,
                inputStream.school ?: "School not found" as school, inputStream.batch is string as isString,
                companyArr[inputStream.indexNo] as companyName, inputStream.name + " " +
                    inputStream.age.toString() as concat
        => (TeacherOutput[] teachers) {

            foreach var t in teachers {
                outputStream.publish(t);
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
