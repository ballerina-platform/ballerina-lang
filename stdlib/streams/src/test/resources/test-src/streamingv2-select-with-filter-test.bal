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
import ballerina/time;
import ballerina/io;

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

stream<Teacher> inputStream = new;
stream<TeacherOutput> outputStream = new;
TeacherOutput[] globalEmployeeArray = [];
int startTime = 0;
function startSelectQuery() returns (TeacherOutput[]) {

    Teacher[] teachers = [];
    Teacher t1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Ananda College" };
    Teacher t2 = { name: "Mohan", age: 45, status: "single", batch: "LK2014", school: "Hindu College" };
    Teacher t3 = { name: "Shareek", age: 50, status: "single", batch: "LK2014", school: "Zahira College" };
    teachers[0] = t1;
    teachers[1] = t2;
    teachers[2] = t3;

    testSelectQuery();

    startTime = time:currentTime().time;
    outputStream.subscribe(printTeachers);

    worker x {
        int i = 0;
        while (i < 3000000) {
		    inputStream.publish(teachers[i % 3]);
		    i += 1;
		}
	}

    int count = 0;
    while(true) {
        runtime:sleep(500);
        if(globalEmployeeArray.length() > 2999000) {
            break;
        }
        //int endTime = time:currentTime().time;
        //io:println("published: ",globalEmployeeArray.length(), " time taken (s): ", (endTime - startTime)/1000,
        //" TPS(/s): "  , globalEmployeeArray.length() * 1000/(endTime - startTime));
    }

    return globalEmployeeArray;
}

function testSelectQuery() {

    forever {
        from inputStream
        select inputStream.name as TeacherName, inputStream.age
        => (TeacherOutput[] emp) {
            foreach TeacherOutput e in emp {
                outputStream.publish(e);
            }
        }
    }
}

function printTeachers(TeacherOutput e) {
    addToGlobalEmployeeArray(e);
}

function addToGlobalEmployeeArray(TeacherOutput e) {
    globalEmployeeArray[globalEmployeeArray.length()] = e;
    if (globalEmployeeArray.length() % 1000 == 0) {
		int endTime = time:currentTime().time;
                    io:println("published: ",globalEmployeeArray.length(), " time taken (s): ", (endTime - startTime)/1000,
                    " TPS(/s): "  , globalEmployeeArray.length() * 1000/(endTime - startTime));
	}
}