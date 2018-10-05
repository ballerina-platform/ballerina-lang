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
import ballerina/io;
import ballerina/time;
import ballerina/streams;

type Avg record {
    float age;
};

type Teacher record {
    string name;
    int age;
    string status;
    string batch;
    string school;
};

int sumValue = 0;
int employeeIndex = 0;
stream<Teacher> teacherStream1;
stream<streams:StreamEvent> outputStream;
streams:EventType evType = "ALL";
streams:LengthWindow lengthWindow = streams:lengthWindow(5, outputStream, evType);

function startFilterQuery() {

    Teacher[] teachers = [];
    teachers[0] = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    teachers[1] = { name: "Shareek", age: 33, status: "single", batch: "LK1998", school: "Thomas College" };
    teachers[2] = { name: "Nimal", age: 44, status: "married", batch: "LK1988", school: "Ananda College" };
    teachers[3] = { name: "Kamal", age: 34, status: "married", batch: "LK1988", school: "Ananda College" };
    teachers[4] = { name: "Nisala", age: 64, status: "married", batch: "LK1988", school: "Ananda College" };
    teachers[5] = { name: "Supuni", age: 40, status: "married", batch: "LK1988", school: "Ananda College" };
    teachers[6] = { name: "Joe", age: 45, status: "married", batch: "LK1988", school: "Ananda College" };
    teachers[7] = { name: "Micheal", age: 60, status: "married", batch: "LK1988", school: "Ananda College" };
    teachers[8] = { name: "Rambo", age: 30, status: "married", batch: "LK1988", school: "Ananda College" };
    teachers[9] = { name: "Alex", age: 25, status: "married", batch: "LK1988", school: "Ananda College" };

    teacherStream1.subscribe(filterTeachers);
    outputStream.subscribe(nextProcess);
    foreach t in teachers {
        teacherStream1.publish(t);
    }

    runtime:sleep(1000);
}


function filterTeachers(Teacher t) {
    float sum = 0;
    lock {
        if (lengthWindow.eventType == "CURRENT"){
            outputStream.publish(createStreamEvent("CURRENT", t));
        } else if (lengthWindow.eventType == "EXPIRED") {
            if (lengthWindow.counter > 4){
                any event = lengthWindow.getEventToBeExpired();
                Teacher teacher = check <Teacher>event;
                outputStream.publish(createStreamEvent("EXPIRED", teacher));
            }
        } else if (lengthWindow.eventType == "ALL") {
            outputStream.publish(createStreamEvent("CURRENT", t));
            if (lengthWindow.counter > 4){
                any event = lengthWindow.getEventToBeExpired();
                Teacher teacher = check <Teacher>event;
                outputStream.publish(createStreamEvent("EXPIRED", teacher));
            }
        }

        lengthWindow.add(t);
    }
}

function createStreamEvent(streams:EventType evType, any evObject) returns (streams:StreamEvent) {
    streams:StreamEvent streamEvent = { eventType: evType, eventObject: evObject };
    return streamEvent;
}

// Below code segment is written to perform the aggregations
function nextProcess(streams:StreamEvent t) {

    Teacher teacher = check <Teacher>t.eventObject;
    if (t.eventType == "CURRENT") {
        sumValue += teacher.age;
    } else if (t.eventType == "EXPIRED"){
        sumValue -= teacher.age;
    } else if (t.eventType == "RESET"){
        sumValue = 0;
    }

    io:println(t);
    io:println(sumValue);
}