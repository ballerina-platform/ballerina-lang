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
stream<ABCDInfo> abcdStream = new;

public function startPatternQuery() returns (ABCDInfo[]) {
    runPatternQuery();

    AInfo a1 = {id:11, name:"a1"};
    AInfo a2 = {id:12, name:"a2"};

    BInfo b1 = {id:21, name:"b1"};
    BInfo b2 = {id:22, name:"b2"};

    CInfo c1 = {id:31, name:"c1"};
    CInfo c2 = {id:32, name:"c2"};

    abcdStream.subscribe(printABCD);

    aStream.publish(a1);
    runtime:sleep(4000);

    //bStream.publish(b1);
    //runtime:sleep(1000);

    cStream.publish(c1);
    cStream.publish(c1);
    //runtime:sleep(1000);

    //cStream.publish(c2);
    //runtime:sleep(1000);

    //bStream.publish(b1);
    //runtime:sleep(1000);

    int count = 0;
    while(true) {
        runtime:sleep(1000);
        count += 1;
        if((abcdInfoArray.length()) > 0 || count == 10) {
            break;
        }
    }
    return abcdInfoArray;
}

//forever {
//    A => !B (for 4 seconds) => C
//}
function runPatternQuery() {

    function (map<anydata>[]) outputFunc = function (map<anydata>[] events) {
        foreach map<anydata> m in events {
            // just cast input map into the output type
            ABCDInfo o = <ABCDInfo>ABCDInfo.convert(m);
            abcdStream.publish(o);
        }
    };

    // Output processor
    streams:OutputProcess outputProcess = streams:createOutputProcess(outputFunc);

    // Selector
    streams:Select select = streams:createSelect(function (streams:StreamEvent?[] e) {outputProcess.process(e);},
        [], (), function (streams:StreamEvent e, streams:Aggregator[] aggregatorArr1) returns map<anydata> {
            io:println("data: ", e.data);
            return {
                //"aId": e.data["e1.0.roomNo"],
                "aId": 4,
                "bId": 4,
                "cId": e.data["a.id"],
                "dId": e.data["c.id"],
                "abcd": "abcd" //<int> e.data["e3.0.tempSet"] - <int> e.data["e2.0.temp"]
            };
        }
    );

    // A => !B (for 4 sec) => C
    streams:CompoundPatternProcessor abc = streams:createCompoundPatternProcessor();
    streams:CompoundPatternProcessor bc = streams:createCompoundPatternProcessor();

    streams:FollowedByProcessor fabc = streams:createFollowedByProcessor();
    streams:FollowedByProcessor fbc = streams:createFollowedByProcessor();

    streams:NotOperatorProcessor not = streams:createNotOperatorProcessor(4000);

    streams:OperandProcessor a = streams:createOperandProcessor("a", ());
    streams:OperandProcessor b = streams:createOperandProcessor("b", ());
    streams:OperandProcessor c = streams:createOperandProcessor("c", ());

    abc.setProcessor(fabc);

    fabc.setLHSProcessor(a);
    fabc.setRHSProcessor(bc);

    bc.setProcessor(fbc);
    fbc.setLHSProcessor(not);
    fbc.setRHSProcessor(c);

    not.setProcessor(b);

    io:println(abc.getAlias());

    // Create state machine.
    streams:StateMachine stateMachine = streams:createStateMachine(abc, function (streams:StreamEvent?[] e) {
            select.process(e);
        }
    );

    // Subscribe to input streams.
    // aStream as a
    aStream.subscribe(function (AInfo i) {
            map<anydata> keyVal = <map<anydata>>map<anydata>.convert(i);
            streams:StreamEvent?[] eventArr = streams:buildStreamEvent(keyVal, "a");
            stateMachine.process(eventArr);
        }
    );
    // bStream as b
    bStream.subscribe(function (BInfo i) {
            map<anydata> keyVal = <map<anydata>>map<anydata>.convert(i);
            streams:StreamEvent?[] eventArr =  streams:buildStreamEvent(keyVal, "b");
            stateMachine.process(eventArr);
        }
    );
    // cStream as c
    cStream.subscribe(function (CInfo i) {
            map<anydata> keyVal = <map<anydata>>map<anydata>.convert(i);
            streams:StreamEvent?[] eventArr = streams:buildStreamEvent(keyVal, "c");
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

