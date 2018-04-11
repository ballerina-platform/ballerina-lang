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

type DeviceTempInfo {
    int deviceID;
    int roomNo;
    float temp;
};

type TempDiffInfo {
    float initialTemp;
    float peakTemp;
};

stream<DeviceTempInfo> tempStream;
stream<TempDiffInfo> tempDiffInfoStream;

TempDiffInfo[] tempDiffInfoArray = [];
int index = 0;

function deployStreamingRules() {
    forever {
        from every tempStream as e1, tempStream where e1.temp <= temp [1..] as e2,
        tempStream where e2[e2.length-1].temp > temp as e3
        select e1.temp as initialTemp, e2[e2.length-1].temp as peakTemp
        => (TempDiffInfo[] tempDiffInfos) {
            tempDiffInfoStream.publish(tempDiffInfos);
        }
    }
}

function runSequenceQuery1() returns(TempDiffInfo[]) {
    deployStreamingRules();
    tempDiffInfoStream.subscribe(printInitalAndPeakTemp);

    DeviceTempInfo t1 = {deviceID:1, roomNo:23, temp:20.0};
    DeviceTempInfo t2 = {deviceID:1, roomNo:23, temp:22.5};
    DeviceTempInfo t3 = {deviceID:1, roomNo:23, temp:23.0};
    DeviceTempInfo t4 = {deviceID:1, roomNo:23, temp:21.0};
    DeviceTempInfo t5 = {deviceID:1, roomNo:23, temp:24.0};
    DeviceTempInfo t6 = {deviceID:1, roomNo:23, temp:23.9};

    tempStream.publish(t1);
    runtime:sleepCurrentWorker(200);

    tempStream.publish(t2);
    runtime:sleepCurrentWorker(200);

    tempStream.publish(t3);
    runtime:sleepCurrentWorker(200);

    tempStream.publish(t4);
    runtime:sleepCurrentWorker(200);

    tempStream.publish(t5);
    runtime:sleepCurrentWorker(200);

    tempStream.publish(t6);

    int count = 0;
    while(true) {
        runtime:sleepCurrentWorker(500);
        count++;
        if((lengthof tempDiffInfoArray) > 1 || count == 10) {
            break;
        }
    }
    return tempDiffInfoArray;
}

function printInitalAndPeakTemp(TempDiffInfo tempDiff) {
    io:println("printInitalAndPeakTemp InitialTemp:" + tempDiff.initialTemp + " and Peak temp :" +
            + tempDiff.peakTemp);
    addToGlobalTempDiffArray(tempDiff);
}

function addToGlobalTempDiffArray(TempDiffInfo s) {
    tempDiffInfoArray[index] = s;
    index = index + 1;
}

