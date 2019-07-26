// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ballerina/artemis;

function testSimpleTransaction() returns @tainted string|error {
    string txt = "";
    var consumer = createSimpleConsumer();
    testSimpleTransactionSend();
    if (consumer is artemis:Consumer) {
        txt = transactionConsumerReceive(consumer);
    }
    return txt;
}

public function createSimpleConsumer() returns artemis:Consumer|error {
    artemis:Listener lis = new artemis:Listener({host: "localhost", port: 61616});
    return lis.createAndGetConsumer({queueName: "example"});
}

public function testSimpleTransactionSend() {
    artemis:Producer prod = new({host: "localhost", port: 61616}, "example");
    send(prod);
    transaction {
        send(prod);
    }
}

public function transactionConsumerReceive(artemis:Consumer consumer) returns @tainted string {
    string msgTxt = "";
    transaction {
       msgTxt += receiveAndGetText(consumer);
       msgTxt += receiveAndGetText(consumer);
    }
    return msgTxt;
}

function send(artemis:Producer prod) {
    var err = prod->send("Example ");
    if (err is error) {
        panic err;
    }
}

public function receiveAndGetText(artemis:Consumer consumer) returns @tainted string {
    string msgTxt = "";
    var msg = consumer->receive();
    if(msg is artemis:Message) {
        var payload = msg.getPayload();
        if(payload is string) {
            msgTxt = payload;
        }
    }
    return msgTxt;
}
