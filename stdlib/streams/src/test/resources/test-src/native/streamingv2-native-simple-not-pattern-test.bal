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
    io:println("ABCInfo Array: ", abcArray);
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

// A -> not B for 2 sec
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
            return {
                "aId": e.get("a.id"),
                "bId": 0,
                "cId": 0
            };
        }
    );

    // A -> not B for 2 sec
    streams:CompoundPatternProcessor root = streams:createCompoundPatternProcessor();
    streams:FollowedByProcessor fab = streams:createFollowedByProcessor();
    streams:NotOperatorProcessor not = streams:createNotOperatorProcessor(2000);
    streams:OperandProcessor a = streams:createOperandProcessor("a", ());
    streams:OperandProcessor b = streams:createOperandProcessor("b", ());

    root.setProcessor(fab);
    fab.setLHSProcessor(a);
    fab.setRHSProcessor(not);
    not.setProcessor(b);
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

    BInfo b1 = {id:21, name:"b1"};
    BInfo b2 = {id:22, name:"b2"};

    // success events.
    aStream.publish(a1);
    runtime:sleep(3000);
    bStream.publish(b1);
    runtime:sleep(500);

    // failed events.
    aStream.publish(a2);
    runtime:sleep(500);
    bStream.publish(b2);

    return waitAndGetEvents();
}

// A -> not B and C
public function runPatternQuery2() returns (ABCInfo[]) {
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
            return {
                "aId": e.get("a.id"),
                "bId": 0,
                "cId": e.get("c.id")
            };
        }
    );

    // A -> not B and C
    streams:CompoundPatternProcessor root = streams:createCompoundPatternProcessor();
    streams:CompoundPatternProcessor bc = streams:createCompoundPatternProcessor();

    streams:FollowedByProcessor fabc = streams:createFollowedByProcessor();

    streams:AndOperatorProcessor and = streams:createAndOperatorProcessor();
    streams:NotOperatorProcessor not = streams:createNotOperatorProcessor(());

    streams:OperandProcessor a = streams:createOperandProcessor("a", ());
    streams:OperandProcessor b = streams:createOperandProcessor("b", ());
    streams:OperandProcessor c = streams:createOperandProcessor("c", ());

    root.setProcessor(fabc);
    fabc.setLHSProcessor(a);
    fabc.setRHSProcessor(bc);
    bc.setProcessor(and);
    and.setLHSProcessor(not);
    and.setRHSProcessor(c);
    not.setProcessor(b);
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
    cStream.subscribe(function (CInfo i) {
            map<anydata> keyVal = <map<anydata>>map<anydata>.constructFrom(i);
            streams:StreamEvent?[] eventArr = streams:buildStreamEvent(keyVal, "c");
            stateMachine.process(eventArr);
        }
    );
    abcStream.subscribe(addToGlobalArray);

    // generate and send events.
    AInfo a1 = {id:11, name:"a1"};
    AInfo a2 = {id:12, name:"a2"};

    BInfo b1 = {id:21, name:"b1"};
    BInfo b2 = {id:22, name:"b2"};

    CInfo c1 = {id:31, name:"c1"};
    CInfo c2 = {id:32, name:"c2"};

    // successful
    aStream.publish(a1);
    runtime:sleep(100);
    cStream.publish(c1);
    runtime:sleep(100);

    // un-successful
    aStream.publish(a2);
    runtime:sleep(100);
    bStream.publish(b2);
    runtime:sleep(100);
    cStream.publish(c2);
    runtime:sleep(100);

    return waitAndGetEvents();
}

// A -> not B for 2 sec and C
public function runPatternQuery3() returns (ABCInfo[]) {
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
            return {
                "aId": e.get("a.id"),
                "bId": 0,
                "cId": e.get("c.id")
            };
        }
    );

    // A -> not B for 2 sec and C
    streams:CompoundPatternProcessor root = streams:createCompoundPatternProcessor();
    streams:CompoundPatternProcessor bc = streams:createCompoundPatternProcessor();

    streams:FollowedByProcessor fabc = streams:createFollowedByProcessor();

    streams:AndOperatorProcessor and = streams:createAndOperatorProcessor();
    streams:NotOperatorProcessor not = streams:createNotOperatorProcessor(2000);

    streams:OperandProcessor a = streams:createOperandProcessor("a", ());
    streams:OperandProcessor b = streams:createOperandProcessor("b", ());
    streams:OperandProcessor c = streams:createOperandProcessor("c", ());

    root.setProcessor(fabc);
    fabc.setLHSProcessor(a);
    fabc.setRHSProcessor(bc);
    bc.setProcessor(and);
    and.setLHSProcessor(not);
    and.setRHSProcessor(c);
    not.setProcessor(b);
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
    cStream.subscribe(function (CInfo i) {
            map<anydata> keyVal = <map<anydata>>map<anydata>.constructFrom(i);
            streams:StreamEvent?[] eventArr = streams:buildStreamEvent(keyVal, "c");
            stateMachine.process(eventArr);
        }
    );
    abcStream.subscribe(addToGlobalArray);

    // generate and send events.
    AInfo a1 = {id:11, name:"a1"};
    AInfo a2 = {id:12, name:"a2"};

    BInfo b1 = {id:21, name:"b1"};
    BInfo b2 = {id:22, name:"b2"};

    CInfo c1 = {id:31, name:"c1"};
    CInfo c2 = {id:32, name:"c2"};

    // successful
    aStream.publish(a1);
    runtime:sleep(3000);
    cStream.publish(c1);
    runtime:sleep(100);

    // un-successful
    aStream.publish(a2);
    runtime:sleep(1000);
    bStream.publish(b2);
    runtime:sleep(100);
    cStream.publish(c2);
    runtime:sleep(100);

    return waitAndGetEvents();
}

// A -> not B for 2 sec or C
public function runPatternQuery4() returns (ABCInfo[]) {
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
            return {
                "aId": e.get("a.id"),
                "bId": 0,
                "cId": 0
            };
        }
    );

    // A -> not B for 2 sec or C
    streams:CompoundPatternProcessor root = streams:createCompoundPatternProcessor();
    streams:CompoundPatternProcessor bc = streams:createCompoundPatternProcessor();

    streams:FollowedByProcessor fabc = streams:createFollowedByProcessor();

    streams:OrOperatorProcessor or = streams:createOrOperatorProcessor();
    streams:NotOperatorProcessor not = streams:createNotOperatorProcessor(2000);

    streams:OperandProcessor a = streams:createOperandProcessor("a", ());
    streams:OperandProcessor b = streams:createOperandProcessor("b", ());
    streams:OperandProcessor c = streams:createOperandProcessor("c", ());

    root.setProcessor(fabc);
    fabc.setLHSProcessor(a);
    fabc.setRHSProcessor(bc);
    bc.setProcessor(or);
    or.setLHSProcessor(not);
    or.setRHSProcessor(c);
    not.setProcessor(b);
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
    cStream.subscribe(function (CInfo i) {
            map<anydata> keyVal = <map<anydata>>map<anydata>.constructFrom(i);
            streams:StreamEvent?[] eventArr = streams:buildStreamEvent(keyVal, "c");
            stateMachine.process(eventArr);
        }
    );
    abcStream.subscribe(addToGlobalArray);

    // generate and send events.
    AInfo a1 = {id:11, name:"a1"};
    AInfo a2 = {id:12, name:"a2"};

    BInfo b1 = {id:21, name:"b1"};
    BInfo b2 = {id:22, name:"b2"};

    CInfo c1 = {id:31, name:"c1"};
    CInfo c2 = {id:32, name:"c2"};

    // successful
    aStream.publish(a1);
    runtime:sleep(3000);

    // successful
    aStream.publish(a2);
    runtime:sleep(1000);
    bStream.publish(b2);
    runtime:sleep(100);
    cStream.publish(c2);

    return waitAndGetEvents();
}

// A -> not B for 2 sec and not C for 2 sec
public function runPatternQuery5() returns (ABCInfo[]) {
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
            return {
                "aId": e.get("a.id"),
                "bId": 0,
                "cId": 0
            };
        }
    );

    // A -> not B for 2 sec and not C for 2 sec
    streams:CompoundPatternProcessor root = streams:createCompoundPatternProcessor();
    streams:CompoundPatternProcessor bc = streams:createCompoundPatternProcessor();

    streams:FollowedByProcessor fabc = streams:createFollowedByProcessor();

    streams:AndOperatorProcessor and = streams:createAndOperatorProcessor();
    streams:NotOperatorProcessor notb = streams:createNotOperatorProcessor(2000);
    streams:NotOperatorProcessor notc = streams:createNotOperatorProcessor(2000);

    streams:OperandProcessor a = streams:createOperandProcessor("a", ());
    streams:OperandProcessor b = streams:createOperandProcessor("b", ());
    streams:OperandProcessor c = streams:createOperandProcessor("c", ());

    root.setProcessor(fabc);
    fabc.setLHSProcessor(a);
    fabc.setRHSProcessor(bc);
    bc.setProcessor(and);
    and.setLHSProcessor(notb);
    and.setRHSProcessor(notc);
    notb.setProcessor(b);
    notc.setProcessor(c);
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
    cStream.subscribe(function (CInfo i) {
            map<anydata> keyVal = <map<anydata>>map<anydata>.constructFrom(i);
            streams:StreamEvent?[] eventArr = streams:buildStreamEvent(keyVal, "c");
            stateMachine.process(eventArr);
        }
    );
    abcStream.subscribe(addToGlobalArray);

    // generate and send events.
    AInfo a1 = {id:11, name:"a1"};
    AInfo a2 = {id:12, name:"a2"};

    BInfo b1 = {id:21, name:"b1"};
    BInfo b2 = {id:22, name:"b2"};

    CInfo c1 = {id:31, name:"c1"};
    CInfo c2 = {id:32, name:"c2"};

    // successful
    aStream.publish(a1);
    runtime:sleep(2500);

    // un-successful
    aStream.publish(a2);
    runtime:sleep(100);
    cStream.publish(c2);

    return waitAndGetEvents();
}

// A -> not B for 2 sec or not C for 2 sec
public function runPatternQuery6() returns (ABCInfo[]) {
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
            return {
                "aId": e.get("a.id"),
                "bId": 0,
                "cId": 0
            };
        }
    );

    // A -> not B for 2 sec or not C for 2 sec
    streams:CompoundPatternProcessor root = streams:createCompoundPatternProcessor();
    streams:CompoundPatternProcessor bc = streams:createCompoundPatternProcessor();

    streams:FollowedByProcessor fabc = streams:createFollowedByProcessor();

    streams:OrOperatorProcessor or = streams:createOrOperatorProcessor();
    streams:NotOperatorProcessor notb = streams:createNotOperatorProcessor(2000);
    streams:NotOperatorProcessor notc = streams:createNotOperatorProcessor(2000);

    streams:OperandProcessor a = streams:createOperandProcessor("a", ());
    streams:OperandProcessor b = streams:createOperandProcessor("b", ());
    streams:OperandProcessor c = streams:createOperandProcessor("c", ());

    root.setProcessor(fabc);
    fabc.setLHSProcessor(a);
    fabc.setRHSProcessor(bc);
    bc.setProcessor(or);
    or.setLHSProcessor(notb);
    or.setRHSProcessor(notc);
    notb.setProcessor(b);
    notc.setProcessor(c);
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
    cStream.subscribe(function (CInfo i) {
            map<anydata> keyVal = <map<anydata>>map<anydata>.constructFrom(i);
            streams:StreamEvent?[] eventArr = streams:buildStreamEvent(keyVal, "c");
            stateMachine.process(eventArr);
        }
    );
    abcStream.subscribe(addToGlobalArray);

    // generate and send events.
    AInfo a1 = {id:11, name:"a1"};
    AInfo a2 = {id:12, name:"a2"};

    BInfo b1 = {id:21, name:"b1"};
    BInfo b2 = {id:22, name:"b2"};

    CInfo c1 = {id:31, name:"c1"};
    CInfo c2 = {id:32, name:"c2"};

    // A -> not B for 2 sec or not C for 2 sec

    // successful
    aStream.publish(a1);
    runtime:sleep(500);
    // publish an event for b, but not for c
    bStream.publish(b2);
    runtime:sleep(3000);

    // un-successful
    aStream.publish(a2);
    runtime:sleep(500);
    bStream.publish(b2);
    runtime:sleep(100);
    cStream.publish(c2);
    runtime:sleep(100);

    return waitAndGetEvents();
}

// not A for 2 sec -> B
public function runPatternQuery7() returns (ABCInfo[]) {
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
            return {
                "aId": 0,
                "bId": e.get("b.id"),
                "cId": 0
            };
        }
    );

    // not A for 2 sec -> B
    streams:CompoundPatternProcessor root = streams:createCompoundPatternProcessor();
    streams:FollowedByProcessor fab = streams:createFollowedByProcessor();
    streams:NotOperatorProcessor not = streams:createNotOperatorProcessor(2000);

    streams:OperandProcessor a = streams:createOperandProcessor("a", ());
    streams:OperandProcessor b = streams:createOperandProcessor("b", ());

    root.setProcessor(fab);
    fab.setLHSProcessor(not);
    fab.setRHSProcessor(b);
    not.setProcessor(a);
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
    cStream.subscribe(function (CInfo i) {
            map<anydata> keyVal = <map<anydata>>map<anydata>.constructFrom(i);
            streams:StreamEvent?[] eventArr = streams:buildStreamEvent(keyVal, "c");
            stateMachine.process(eventArr);
        }
    );
    abcStream.subscribe(addToGlobalArray);

    // generate and send events.
    AInfo a1 = {id:11, name:"a1"};
    AInfo a2 = {id:12, name:"a2"};

    BInfo b1 = {id:21, name:"b1"};
    BInfo b2 = {id:22, name:"b2"};

    // un-successful
    runtime:sleep(500);
    aStream.publish(a1);

    // successful
    runtime:sleep(2200);
    bStream.publish(b2);
    runtime:sleep(100);

    return waitAndGetEvents();
}

// not A and B -> C
public function runPatternQuery8() returns (ABCInfo[]) {
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
            return {
                "aId": 0,
                "bId": e.get("b.id"),
                "cId": e.get("c.id")
            };
        }
    );

    // not A and B -> C
    streams:CompoundPatternProcessor root = streams:createCompoundPatternProcessor();
    streams:CompoundPatternProcessor ab = streams:createCompoundPatternProcessor();

    streams:FollowedByProcessor fabc = streams:createFollowedByProcessor();

    streams:AndOperatorProcessor and = streams:createAndOperatorProcessor();
    streams:NotOperatorProcessor not = streams:createNotOperatorProcessor(());

    streams:OperandProcessor a = streams:createOperandProcessor("a", ());
    streams:OperandProcessor b = streams:createOperandProcessor("b", ());
    streams:OperandProcessor c = streams:createOperandProcessor("c", ());

    root.setProcessor(fabc);
    fabc.setLHSProcessor(ab);
    fabc.setRHSProcessor(c);
    ab.setProcessor(and);
    and.setLHSProcessor(not);
    and.setRHSProcessor(b);
    not.setProcessor(a);
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
    cStream.subscribe(function (CInfo i) {
            map<anydata> keyVal = <map<anydata>>map<anydata>.constructFrom(i);
            streams:StreamEvent?[] eventArr = streams:buildStreamEvent(keyVal, "c");
            stateMachine.process(eventArr);
        }
    );
    abcStream.subscribe(addToGlobalArray);

    // generate and send events.
    AInfo a1 = {id:11, name:"a1"};
    AInfo a2 = {id:12, name:"a2"};

    BInfo b1 = {id:21, name:"b1"};
    BInfo b2 = {id:22, name:"b2"};

    CInfo c1 = {id:31, name:"c1"};
    CInfo c2 = {id:32, name:"c2"};

    // successful
    bStream.publish(b1);
    runtime:sleep(200);
    cStream.publish(c1);
    runtime:sleep(200);

    // un-successful
    aStream.publish(a2);
    runtime:sleep(100);

    return waitAndGetEvents();
}

// not A for 2 sec and B -> C
public function runPatternQuery9() returns (ABCInfo[]) {
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
            return {
                "aId": 0,
                "bId": e.get("b.id"),
                "cId": e.get("c.id")
            };
        }
    );

    // not A for 2 sec and B -> C
    streams:CompoundPatternProcessor root = streams:createCompoundPatternProcessor();
    streams:CompoundPatternProcessor ab = streams:createCompoundPatternProcessor();

    streams:FollowedByProcessor fabc = streams:createFollowedByProcessor();

    streams:AndOperatorProcessor and = streams:createAndOperatorProcessor();
    streams:NotOperatorProcessor not = streams:createNotOperatorProcessor(2000);

    streams:OperandProcessor a = streams:createOperandProcessor("a", ());
    streams:OperandProcessor b = streams:createOperandProcessor("b", ());
    streams:OperandProcessor c = streams:createOperandProcessor("c", ());

    root.setProcessor(fabc);
    fabc.setLHSProcessor(ab);
    fabc.setRHSProcessor(c);
    ab.setProcessor(and);
    and.setLHSProcessor(not);
    and.setRHSProcessor(b);
    not.setProcessor(a);
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
    cStream.subscribe(function (CInfo i) {
            map<anydata> keyVal = <map<anydata>>map<anydata>.constructFrom(i);
            streams:StreamEvent?[] eventArr = streams:buildStreamEvent(keyVal, "c");
            stateMachine.process(eventArr);
        }
    );
    abcStream.subscribe(addToGlobalArray);

    // generate and send events.
    AInfo a1 = {id:11, name:"a1"};
    AInfo a2 = {id:12, name:"a2"};

    BInfo b1 = {id:21, name:"b1"};
    BInfo b2 = {id:22, name:"b2"};

    CInfo c1 = {id:31, name:"c1"};
    CInfo c2 = {id:32, name:"c2"};

    // successful
    runtime:sleep(2100);
    bStream.publish(b1);
    runtime:sleep(100);
    cStream.publish(c1);
    runtime:sleep(500);

    // un-successful
    aStream.publish(a2);
    runtime:sleep(100);
    bStream.publish(b1);
    runtime:sleep(100);
    cStream.publish(c1);
    runtime:sleep(500);

    return waitAndGetEvents();
}

// not A for 2 sec and not B for 2 sec -> C
public function runPatternQuery10() returns (ABCInfo[]) {
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
            return {
                "aId": 0,
                "bId": 0,
                "cId": e.get("c.id")
            };
        }
    );

    // not A for 2 sec and not B for 2 sec -> C
    streams:CompoundPatternProcessor root = streams:createCompoundPatternProcessor();
    streams:CompoundPatternProcessor ab = streams:createCompoundPatternProcessor();

    streams:FollowedByProcessor fabc = streams:createFollowedByProcessor();

    streams:AndOperatorProcessor and = streams:createAndOperatorProcessor();
    streams:NotOperatorProcessor nota = streams:createNotOperatorProcessor(2000);
    streams:NotOperatorProcessor notb = streams:createNotOperatorProcessor(2000);

    streams:OperandProcessor a = streams:createOperandProcessor("a", ());
    streams:OperandProcessor b = streams:createOperandProcessor("b", ());
    streams:OperandProcessor c = streams:createOperandProcessor("c", ());

    root.setProcessor(fabc);
    fabc.setLHSProcessor(ab);
    fabc.setRHSProcessor(c);
    ab.setProcessor(and);
    and.setLHSProcessor(nota);
    and.setRHSProcessor(notb);
    nota.setProcessor(a);
    notb.setProcessor(b);
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
    cStream.subscribe(function (CInfo i) {
            map<anydata> keyVal = <map<anydata>>map<anydata>.constructFrom(i);
            streams:StreamEvent?[] eventArr = streams:buildStreamEvent(keyVal, "c");
            stateMachine.process(eventArr);
        }
    );
    abcStream.subscribe(addToGlobalArray);

    // generate and send events.
    AInfo a1 = {id:11, name:"a1"};
    AInfo a2 = {id:12, name:"a2"};

    BInfo b1 = {id:21, name:"b1"};
    BInfo b2 = {id:22, name:"b2"};

    CInfo c1 = {id:31, name:"c1"};
    CInfo c2 = {id:32, name:"c2"};

    // successful
    runtime:sleep(2300);
    cStream.publish(c1);

    // un-successful
    aStream.publish(a2);
    runtime:sleep(200);
    cStream.publish(c2);
    runtime:sleep(100);

    return waitAndGetEvents();
}