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

struct RoomTempInfo {
    int deviceID;
    int roomNo;
    float temp;
}

struct RegulatorInfo {
    int deviceID;
    int roomNo;
    float tempSet;
    boolean isOn;
}

struct TempDiffInfo {
    int roomNo;
    float temp1;
    float temp2;
}

TempDiffInfo [] tempDiffInfoArray = [];
int index = 0;

stream<RoomTempInfo> tempStream = {};
stream<RegulatorInfo> regulatorStream = {};
stream<TempDiffInfo> tempDiffStream = {};

function testPatternQuery () {

    forever{
        from  every regulatorStream as e1 followed by tempStream where e1.roomNo==roomNo [1..2) as e2
        followed by regulatorStream where e1.roomNo==roomNo as e3
        select e1.roomNo, e2[1].temp - e2[0].temp  as tempDifference
        => (TempDiffInfo [] emp) {
            tempDiffStream.publish(emp);
        }
    }
}

function runtPatternQuery () returns (TempDiffInfo []) {

    testPatternQuery();

    RoomTempInfo t1 = {deviceID: 1, roomNo: 23, temp: 23.0};
    RoomTempInfo t2 = {deviceID: 8, roomNo: 23, temp: 30.0};

    RegulatorInfo r1 = {deviceID:1, roomNo:23, tempSet:15.0, isOn:true};
    RegulatorInfo r2 = {deviceID:3, roomNo:23, tempSet:25.0, isOn:true};


    tempDiffStream.subscribe(printTempDifference);

    regulatorStream.publish(r1);
    runtime:sleepCurrentWorker(100);

    tempStream.publish(t1);
    tempStream.publish(t2);
    runtime:sleepCurrentWorker(200);

    regulatorStream.publish(r2);
    runtime:sleepCurrentWorker(1000);

    return tempDiffInfoArray;
}

function printTempDifference (TempDiffInfo tempDiff) {
    io:println("printTemoDifference function invoked for Room:" + tempDiff.roomNo +" and temp difference :" +
               tempDiff.temp1 + ",,," + tempDiff.temp2);
    addToGlobalTempDiffArray(tempDiff);
}

function addToGlobalTempDiffArray (TempDiffInfo s) {
    tempDiffInfoArray[index] = s;
    index = index + 1;
}