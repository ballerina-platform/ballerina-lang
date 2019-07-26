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

type DInfo record {
    int id;
    string name;
};

type ABCDInfo record {
    int aId;
    int bId;
    int cId;
    int dId;
    string abcd;
};

ABCDInfo[] abcdInfoArray = [];
int index = 0;
stream<AInfo> aStream = new;
stream<BInfo> bStream = new;
stream<CInfo> cStream = new;
stream<DInfo> dStream = new;
stream<ABCDInfo> abcdStream = new;

// (A || B) => (C && D)
public function startPatternQuery() returns (ABCDInfo[]) {
    runPatternQuery();

    AInfo a1 = {id:11, name:"a1"};
    AInfo a2 = {id:12, name:"a2"};

    BInfo b1 = {id:21, name:"b1"};
    BInfo b2 = {id:22, name:"b2"};

    CInfo c1 = {id:31, name:"c1"};
    CInfo c2 = {id:32, name:"c2"};

    DInfo d1 = {id:41, name:"d1"};
    DInfo d2 = {id:42, name:"d2"};

    abcdStream.subscribe(addToGlobalArray);

    aStream.publish(a1);
    cStream.publish(c1);
    dStream.publish(d1);

    runtime:sleep(100);

    bStream.publish(b2);
    cStream.publish(c2);
    dStream.publish(d2);

    runtime:sleep(1000);

    int count = 0;
    while(true) {
        runtime:sleep(500);
        count += 1;
        if((abcdInfoArray.length()) > 0 || count == 10) {
            break;
        }
    }
    return abcdInfoArray;
}

function runPatternQuery() {

    function (map<anydata>[]) outputFunc = function (map<anydata>[] events) {
        foreach map<anydata> m in events {
            // just cast input map into the output type
            ABCDInfo o = <ABCDInfo>ABCDInfo.constructFrom(m);
            abcdStream.publish(o);
        }
    };

    // Output processor
    streams:OutputProcess outputProcess = streams:createOutputProcess(outputFunc);

    // Selector
    streams:Select select = streams:createSelect(function (streams:StreamEvent?[] e) {outputProcess.process(e);},
        [], (), function (streams:StreamEvent e, streams:Aggregator[] aggregatorArr1) returns map<anydata> {
            return {
                "aId": 0,
                "bId": 0,
                "cId": e.get("c.id"),
                "dId": e.get("d.id"),
                "abcd": "abcd"
            };
        }
    );

    // (A || B) => (C && D)
    streams:CompoundPatternProcessor abcd = streams:createCompoundPatternProcessor(withinTimeMillis = 4000);
    streams:CompoundPatternProcessor ab = streams:createCompoundPatternProcessor();
    streams:CompoundPatternProcessor cd = streams:createCompoundPatternProcessor();

    streams:FollowedByProcessor follow = streams:createFollowedByProcessor();

    streams:OrOperatorProcessor or = streams:createOrOperatorProcessor();
    streams:AndOperatorProcessor and = streams:createAndOperatorProcessor();

    streams:OperandProcessor a = streams:createOperandProcessor("a", ());
    streams:OperandProcessor b = streams:createOperandProcessor("b", ());
    streams:OperandProcessor c = streams:createOperandProcessor("c", ());
    streams:OperandProcessor d = streams:createOperandProcessor("d", ());

    abcd.setProcessor(follow);

    follow.setLHSProcessor(ab);
    follow.setRHSProcessor(cd);

    ab.setProcessor(or);
    or.setLHSProcessor(a);
    or.setRHSProcessor(b);

    cd.setProcessor(and);
    and.setLHSProcessor(c);
    and.setRHSProcessor(d);

    io:println(abcd.getAlias());

    // Create state machine.
    streams:StateMachine stateMachine = streams:createStateMachine(abcd, function (streams:StreamEvent?[] e) {
            select.process(e);
        }
    );

    // Subscribe to input streams.
    // aStream as a
    aStream.subscribe(function (AInfo i) {
            map<anydata> keyVal = <map<anydata>>map<anydata>.constructFrom(i);
            streams:StreamEvent?[] eventArr = streams:buildStreamEvent(keyVal, "a");
            stateMachine.process(eventArr);
        }
    );
    // bStream as b
    bStream.subscribe(function (BInfo i) {
            map<anydata> keyVal = <map<anydata>>map<anydata>.constructFrom(i);
            streams:StreamEvent?[] eventArr =  streams:buildStreamEvent(keyVal, "b");
            stateMachine.process(eventArr);
        }
    );
    // cStream as c
    cStream.subscribe(function (CInfo i) {
            map<anydata> keyVal = <map<anydata>>map<anydata>.constructFrom(i);
            streams:StreamEvent?[] eventArr = streams:buildStreamEvent(keyVal, "c");
            stateMachine.process(eventArr);
        }
    );
    // dStream as e3
    dStream.subscribe(function (DInfo i) {
            map<anydata> keyVal = <map<anydata>>map<anydata>.constructFrom(i);
            streams:StreamEvent?[] eventArr = streams:buildStreamEvent(keyVal, "d");
            stateMachine.process(eventArr);
        }
    );
}

function printABCD(ABCDInfo abcd) {
    addToGlobalArray(abcd);
    io:println("final: ", abcdInfoArray);
}

function addToGlobalArray(ABCDInfo a) {
    abcdInfoArray[index] = a;
    index = index + 1;
}

