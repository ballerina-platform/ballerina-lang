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

import ballerinax/java.jms;

jms:Connection jmsConnection = new ({
        initialContextFactory: "org.apache.activemq.artemis.jndi.ActiveMQInitialContextFactory",
        providerUrl: "tcp://localhost:61616"
    });

jms:Session jmsSession = new (jmsConnection, {
        acknowledgementMode: "AUTO_ACKNOWLEDGE"
    });

jms:QueueSender prod = new(jmsSession, "message_queue");

public function testSendXml() {
    jms:Message msg = checkpanic new jms:Message(jmsSession, jms:TEXT_MESSAGE);
    xml book = xml `<book>The Lost World</book>`;
    checkpanic msg.setPayload(book);
    var err = prod->send(msg);
}

public function testSendJson() {
    jms:Message msg = checkpanic new jms:Message(jmsSession, jms:TEXT_MESSAGE);
    json payload = {"hello": 1.2};
    checkpanic msg.setPayload(payload);
    var err = prod->send(msg);
}

public function testSendIntAsText() {
    jms:Message msg = checkpanic new jms:Message(jmsSession, jms:TEXT_MESSAGE);
    checkpanic msg.setPayload(123);
    var err = prod->send(msg);
}

public function testSendBytesMessage() {
    jms:Message msg = checkpanic new jms:Message(jmsSession, jms:BYTES_MESSAGE);
    byte[6] bytesVal = [1, 2, 2, 3, 3, 2];
    checkpanic msg.setPayload(bytesVal);    //BYTES
    checkpanic msg.setPayload(1);           //INT
    checkpanic msg.setPayload(1.2);         //FLOAT
    byte singleByte = 2;
    checkpanic msg.setPayload(singleByte);  //BYTE
    checkpanic msg.setPayload(true);        //BOOLEAN
    checkpanic msg.setPayload("hello");     //STRING
    map<string | int> mapMsg = {
            "name": "Riyafa",
            "hello": 123
    };
    checkpanic msg.setPayload(mapMsg);      //STRING
    xml book = xml `<book>The Lost World</book>`;
    checkpanic msg.setPayload(book);        //XML
    json payload = {"hello": true};
    checkpanic msg.setPayload(payload);     //JSON
    var err = prod->send(msg);
}

public function testSendMapMessage() {
    jms:Message msg = checkpanic new jms:Message(jmsSession, jms:MAP_MESSAGE);
    byte singleByte = 1;
    byte[] byteArr = [1, 2, 3, 4];
    map<string | byte | int | float | boolean | byte[] | ()> mapMsg = {
        "name": "Riyafa",
        "hello": singleByte,
        "int": 123,
        "float": 1.2,
        "boolean": true,
        "byteArr": byteArr,
        "null": ()
    };
    checkpanic msg.setPayload(mapMsg);
    var err = prod->send(msg);
}

public function testSendStreamMessage() {
    jms:Message msg = checkpanic new jms:Message(jmsSession, jms:STREAM_MESSAGE);
    byte[6] bytesVal = [1, 2, 2, 3, 3, 2];
    checkpanic msg.setPayload(bytesVal);    //BYTES
    checkpanic msg.setPayload(1);           //INT
    checkpanic msg.setPayload(1.2);         //FLOAT
    byte singleByte = 2;
    checkpanic msg.setPayload(singleByte);  //BYTE
    checkpanic msg.setPayload(true);        //BOOLEAN
    checkpanic msg.setPayload("hello");     //STRING
    map<string | int> mapMsg = {
            "name": "Riyafa",
            "hello": 123
    };
    checkpanic msg.setPayload(mapMsg);      //STRING
    xml book = xml `<book>The Lost World</book>`;
    checkpanic msg.setPayload(book);        //XML
    json payload = {"hello": true};
    checkpanic msg.setPayload(payload);     //JSON
    var err = prod->send(msg);
}

public function testSendCustomHeaders() {
    jms:Message msg = checkpanic new jms:Message(jmsSession, jms:MESSAGE);
    jms:CustomHeaders headers = {
        replyTo: checkpanic jmsSession.createTemporaryQueue(),
        correlationId: "123",
        jmsType: "Message"
    };
    checkpanic msg.setCustomHeaders(headers);
    checkpanic prod->send(msg);
}

public function testSendProperties() {
    jms:Message msg = checkpanic new jms:Message(jmsSession, jms:TEXT_MESSAGE);
    checkpanic msg.setProperty("string", "Hello again!");
    checkpanic msg.setProperty("int", 123);
    checkpanic msg.setProperty("float", 1.111);
    checkpanic msg.setProperty("boolean", true);
    byte byteVal = 1;
    checkpanic msg.setProperty("byte", byteVal);
    json jsonPayload = {"hello": true};
    checkpanic msg.setProperty("json", jsonPayload);
    xml book = xml `<book>The Lost World</book>`;
    checkpanic msg.setProperty("xml", book);
    checkpanic prod->send(msg);
}
