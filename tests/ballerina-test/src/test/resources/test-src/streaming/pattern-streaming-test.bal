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

type RoomTempInfo {
    int deviceID;
    int roomNo;
    float temp;
};

type RegulatorInfo {
    int deviceID;
    int roomNo;
    float tempSet;
    boolean isOn;
};

type TempDiffInfo {
    int roomNo;
    float tempDifference;
};

TempDiffInfo[] tempDiffInfoArray = [];
int index = 0;
stream<RoomTempInfo> tempStream;
stream<RegulatorInfo> regulatorStream;
stream<TempDiffInfo> tempDiffStream;

function testPatternQuery () {

    forever {
        from every regulatorStream as e1 followed by tempStream where e1.roomNo == roomNo [1..2) as e2
        followed by regulatorStream where e1.roomNo == roomNo as e3
        select e1.roomNo, e2[1].temp - e2[0].temp as tempDifference
        => (TempDiffInfo[] emp) {
                tempDiffStream.publish(emp);
        }
    }
}

function printTempDifference(TempDiffInfo tempDiff) {
    io:println("printTemoDifference function invoked for Room:" + tempDiff.roomNo + " and temp difference :" +
        + tempDiff.tempDifference);
    addToGlobalTempDiffArray(tempDiff);
}

function addToGlobalTempDiffArray(TempDiffInfo s) {
    tempDiffInfoArray[index] = s;
    index = index + 1;
}

function runPatternQuery1() returns (TempDiffInfo[]) {

    testPatternQuery();

    RoomTempInfo t1 = {deviceID:1, roomNo:23, temp:23.0};
    RoomTempInfo t2 = {deviceID:8, roomNo:23, temp:30.0};

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

type RegulatorState {
    int deviceId;
    int roomNo;
    float tempSet;
    string userAction;
};

type RoomKeyAction {
    int roomNo;
    string userAction;
};


RoomKeyAction[] roomActions = [];
RoomKeyAction[] roomActions2 = [];
RoomKeyAction[] roomActions3 = [];
RoomKeyAction[] roomActions4 = [];

function runPatternQuery2() returns (RoomKeyAction[]) {
    index = 0;

    stream<RegulatorState> regulatorStateChangeStream;
    stream<RoomKeyAction> roomKeyStream;
    stream<RoomKeyAction> regulatorActionStream;

    forever {
        from every regulatorStateChangeStream where userAction == "on" as e1
        followed by roomKeyStream where e1.roomNo == roomNo && userAction == "removed" as e2
        || regulatorStateChangeStream where e1.roomNo == roomNo && userAction == "off" as e3
        select e1.roomNo as roomNo, e2 == null ? "none" : "stop" as userAction having userAction != "none"
        => (RoomKeyAction[] keyAction) {
            regulatorActionStream.publish(keyAction);
        }
    }

    RegulatorState regulatorState1 = {deviceId:1, roomNo:2, tempSet:23.56, userAction:"on"};
    RegulatorState regulatorState2 = {deviceId:1, roomNo:2, tempSet:23.56, userAction:"off"};

    RoomKeyAction roomKeyAction1 = {roomNo:2, userAction:"removed"};

    regulatorActionStream.subscribe(alertRoomAction1);
    regulatorStateChangeStream.publish(regulatorState1);
    runtime:sleepCurrentWorker(200);
    roomKeyStream.publish(roomKeyAction1);
    runtime:sleepCurrentWorker(500);

    regulatorStateChangeStream.publish(regulatorState1);
    runtime:sleepCurrentWorker(200);
    regulatorStateChangeStream.publish(regulatorState2);
    runtime:sleepCurrentWorker(500);

    return roomActions;
}

function alertRoomAction1(RoomKeyAction action) {
    io:println("alertRoomAction function invoked for Room:" + action.roomNo + " and the action :" +
        action.userAction);
    addToGlobalRoomActions(action);

}

function addToGlobalRoomActions(RoomKeyAction s) {
    roomActions[index] = s;
    index = index + 1;
}

function runPatternQuery3() returns (RoomKeyAction[]) {
    index = 0;

    stream<RegulatorState> regulatorStateChangeStream2;
    stream<RoomKeyAction> roomKeyStream2;
    stream<RoomKeyAction> regulatorActionStream2;

    forever {
        from every regulatorStateChangeStream2 where userAction == "on" as e1
        followed by roomKeyStream2 where e1.roomNo == roomNo && userAction == "removed" as e2
        && regulatorStateChangeStream2 where e1.roomNo == roomNo && userAction == "off" as e3
        select e1.roomNo as roomNo, e2 != null ? "RoomClosedWithRegulatorOff" : "other" as userAction having userAction
        != "other"
        => (RoomKeyAction[] keyAction) {
            regulatorActionStream2.publish(keyAction);
        }
    }

    RegulatorState regulatorState1 = {deviceId:1, roomNo:2, tempSet:23.56, userAction:"on"};
    RegulatorState regulatorState2 = {deviceId:1, roomNo:2, tempSet:23.56, userAction:"off"};

    RoomKeyAction roomKeyAction1 = {roomNo:2, userAction:"removed"};

    regulatorActionStream2.subscribe(alertRoomAction2);
    regulatorStateChangeStream2.publish(regulatorState1);
    runtime:sleepCurrentWorker(200);
    roomKeyStream2.publish(roomKeyAction1);
    regulatorStateChangeStream2.publish(regulatorState2);
    runtime:sleepCurrentWorker(500);

    return roomActions2;
}


function alertRoomAction2(RoomKeyAction action) {
    io:println("alertRoomAction function invoked for Room:" + action.roomNo + " and the action :" +
        action.userAction);
    addToGlobalRoomActions2(action);

}

function addToGlobalRoomActions2(RoomKeyAction s) {
    roomActions2[index] = s;
    index = index + 1;
}

function runPatternQuery4() returns (RoomKeyAction[]) {
    index = 0;

    stream<RegulatorState> regulatorStateChangeStream3;
    stream<RoomKeyAction> roomKeyStream3;
    stream<RoomKeyAction> regulatorActionStream3;

    forever {
        from every regulatorStateChangeStream3 where userAction == "on" as e1
        followed by !roomKeyStream3 where e1.roomNo == roomNo && userAction == "removed"
        && regulatorStateChangeStream3 where e1.roomNo == roomNo && userAction == "off" as e2
        select e1.roomNo as roomNo, e2 != null ? "RoomNotClosedWithRegulatorNotOff" : "other" as userAction
        having userAction != "other"
        => (RoomKeyAction[] keyAction) {
            regulatorActionStream3.publish(keyAction);
        }
    }

    RegulatorState regulatorState1 = {deviceId:1, roomNo:2, tempSet:23.56, userAction:"on"};
    RegulatorState regulatorState2 = {deviceId:1, roomNo:2, tempSet:35.56, userAction:"off"};

    RoomKeyAction roomKeyAction1 = {roomNo:2, userAction:"inserted"};

    regulatorActionStream3.subscribe(alertRoomAction3);
    regulatorStateChangeStream3.publish(regulatorState1);
    runtime:sleepCurrentWorker(200);
    roomKeyStream3.publish(roomKeyAction1);
    regulatorStateChangeStream3.publish(regulatorState2);
    runtime:sleepCurrentWorker(500);

    return roomActions3;
}


function alertRoomAction3(RoomKeyAction action) {
    io:println("alertRoomAction function invoked for Room:" + action.roomNo + " and the action :" +
        action.userAction);
    addToGlobalRoomActions3(action);

}

function addToGlobalRoomActions3(RoomKeyAction s) {
    roomActions3[index] = s;
    index = index + 1;
}

function runPatternQuery5() returns (RoomKeyAction[]) {
    index = 0;

    stream<RegulatorState> regulatorStateChangeStream4;
    stream<RoomKeyAction> roomKeyStream4;
    stream<RoomKeyAction> regulatorActionStream4;

    forever {
        from every regulatorStateChangeStream4 where userAction == "on" as e1
        followed by !roomKeyStream4 where e1.roomNo == roomNo && userAction == "removed" for "2 sec"
        select e1.roomNo as roomNo, "CloseRoomAfter2Sec" as userAction
        => (RoomKeyAction[] keyAction) {
            regulatorActionStream4.publish(keyAction);
        }
    }

    RegulatorState regulatorState1 = {deviceId:1, roomNo:2, tempSet:23.56, userAction:"on"};

    RoomKeyAction roomKeyAction1 = {roomNo:2, userAction:"inserted"};

    regulatorActionStream4.subscribe(alertRoomAction4);
    regulatorStateChangeStream4.publish(regulatorState1);
    runtime:sleepCurrentWorker(200);
    roomKeyStream4.publish(roomKeyAction1);
    runtime:sleepCurrentWorker(2500);

    return roomActions4;
}


function alertRoomAction4(RoomKeyAction action) {
    io:println("alertRoomAction function invoked for Room:" + action.roomNo + " and the action :" +
        action.userAction);
    addToGlobalRoomActions4(action);

}

function addToGlobalRoomActions4(RoomKeyAction s) {
    roomActions4[index] = s;
    index = index + 1;
}
