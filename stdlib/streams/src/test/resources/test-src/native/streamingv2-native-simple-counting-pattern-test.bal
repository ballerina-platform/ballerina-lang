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
import ballerina/streams;
import ballerina/io;

type AInfo record {
    int id;
    string name;
};

type BInfo record {
    int id;
    string name;
};

type CInfo record {
    int id;
    string name;
};

type ABCInfo record {
    int aId;
    int bId;
    int cId;
};

ABCInfo[] abcArray = [];
int index = 0;
stream<AInfo> aStream = new;
stream<BInfo> bStream = new;
stream<CInfo> cStream = new;
stream<ABCInfo> abcStream = new;

function addToGlobalArray(ABCInfo a) {
    abcArray[index] = a;
    index = index + 1;
}

function reset() {
    abcArray = [];
    index = 0;
    aStream = new;
    bStream = new;
    cStream = new;
    abcStream = new;
    runtime:sleep(200);
}

function waitAndGetEvents() returns (ABCInfo[]) {
    int count = 0;
    while(true) {
        runtime:sleep(100);
        count += 1;
        if((abcArray.length()) > 0 || count == 10) {
            break;
        }
    }
    return abcArray;
}

// A[2..3] -> B, select a[1].id as aId, b.id as bId, 0 as cID
public function runPatternQuery1() returns (ABCInfo[]) {
    reset();

    // create output function
    function (map<anydata>[]) outputFunc = function (map<anydata>[] events) {
        foreach map<anydata> m in events {
            // just cast input map into the output type
            ABCInfo o = <ABCInfo>ABCInfo.constructFrom(m);
            abcStream.publish(o);
        }
    };

    // create output processor
    streams:OutputProcess outputProcess = streams:createOutputProcess(outputFunc);

    // create selector
    streams:Select select = streams:createSelect(function (streams:StreamEvent?[] e) {outputProcess.process(e);},
        [], (), function (streams:StreamEvent e, streams:Aggregator[] aggregatorArr1) returns map<anydata> {
            // A[2..3] -> B, select a[1].id as aId, b.id as bId, 0 as cID
            return {
                "aId": e.get("a[1].id"),
                "bId": e.get("b.id"),
                "cId": 0
            };
        }
    );

    // A[2..3] -> B, select a[1].id as aId, b.id as bId, 0 as cID
    streams:CompoundPatternProcessor root = streams:createCompoundPatternProcessor();
    streams:FollowedByProcessor fab = streams:createFollowedByProcessor();
    streams:OperandProcessor a = streams:createOperandProcessor("a", (), minOccurs = 2, maxOccurs = 3);
    streams:OperandProcessor b = streams:createOperandProcessor("b", ());

    root.setProcessor(fab);
    fab.setLHSProcessor(a);
    fab.setRHSProcessor(b);
    io:println(root.getAlias());

    // create state machine.
    streams:StateMachine stateMachine = streams:createStateMachine(root, function (streams:StreamEvent?[] e) {
            select.process(e);
        }
    );

    // subscribe to input/output streams.
    aStream.subscribe(function (AInfo i) {
            map<anydata> keyVal = <map<anydata>>map<anydata>.constructFrom(i);
            streams:StreamEvent?[] eventArr = streams:buildStreamEvent(keyVal, "a");
            stateMachine.process(eventArr);
        }
    );
    bStream.subscribe(function (BInfo i) {
            map<anydata> keyVal = <map<anydata>>map<anydata>.constructFrom(i);
            streams:StreamEvent?[] eventArr =  streams:buildStreamEvent(keyVal, "b");
            stateMachine.process(eventArr);
        }
    );
    abcStream.subscribe(addToGlobalArray);

    // generate and send events.
    AInfo a1 = {id:11, name:"a1"};
    AInfo a2 = {id:12, name:"a2"};
    AInfo a3 = {id:13, name:"a3"};
    AInfo a4 = {id:14, name:"a4"};
    AInfo a5 = {id:15, name:"a5"};
    AInfo a6 = {id:16, name:"a6"};

    BInfo b1 = {id:21, name:"b1"};
    BInfo b2 = {id:22, name:"b2"};
    BInfo b3 = {id:23, name:"b3"};

    // success events.
    aStream.publish(a1);
    runtime:sleep(100);
    aStream.publish(a2);
    runtime:sleep(100);
    aStream.publish(a3);
    runtime:sleep(100);
    bStream.publish(b1);
    runtime:sleep(100);

    aStream.publish(a4);
    runtime:sleep(100);
    aStream.publish(a5);
    runtime:sleep(100);
    bStream.publish(b2);
    runtime:sleep(100);

    // un-success events.
    aStream.publish(a6);
    runtime:sleep(100);
    bStream.publish(b3);
    runtime:sleep(100);

    return waitAndGetEvents();
}