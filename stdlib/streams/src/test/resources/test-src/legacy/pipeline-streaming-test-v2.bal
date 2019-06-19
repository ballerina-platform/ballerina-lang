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

type StatusCount record {
    string status;
    int totalCount;
};

type Teacher record {
    string name;
    int age;
    string status;
    string batch;
    string school;
};

StatusCount[] globalStatusCountArray = [];
int index = 0;

stream<StatusCount> filteredStatusCountStream2 = new;
stream<Teacher> preProcessedStatusCountStream = new;
stream<Teacher> teacherStream6 = new;

function testPipelineQuery() {

    forever {
        from teacherStream6 where age > 18
        select *
        => (Teacher[] teachers) {
            foreach var teacher in teachers {
                preProcessedStatusCountStream.publish(teacher);
            }
        }

        from preProcessedStatusCountStream window lengthBatch(3)
        select status, count(status) as totalCount
        group by status
        having totalCount > 1
        => (StatusCount[] emp) {
            foreach var e in emp {
                filteredStatusCountStream2.publish(e);
            }
        }
    }
}

function startPipelineQuery() returns (StatusCount[]) {

    testPipelineQuery();
    Teacher t1 = {name:"Raja", age:25, status:"single", batch:"LK2014", school:"Hindu College"};
    Teacher t2 = {name:"Shareek", age:33, status:"single", batch:"LK1998", school:"Thomas College"};
    Teacher t3 = {name:"Nimal", age:45, status:"married", batch:"LK1988", school:"Ananda College"};

    filteredStatusCountStream2.subscribe(printStatusCount);

    teacherStream6.publish(t1);
    teacherStream6.publish(t2);
    teacherStream6.publish(t3);

    int count = 0;
    while(true) {
        runtime:sleep(500);
        count += 1;
        if((globalStatusCountArray.length()) == 1 || count == 10) {
            break;
        }
    }

    return globalStatusCountArray;
}

function printStatusCount(StatusCount s) {
    addToGlobalStatusCountArray(s);
}

function addToGlobalStatusCountArray(StatusCount s) {
    globalStatusCountArray[index] = s;
    index = index + 1;
}
