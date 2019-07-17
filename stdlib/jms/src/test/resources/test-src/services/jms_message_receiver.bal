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
import ballerina/io;

string msgVal = "";
jms:Connection conn = new ({
        initialContextFactory: "org.apache.activemq.artemis.jndi.ActiveMQInitialContextFactory",
        providerUrl: "tcp://localhost:61616"
    });

jms:Session jmsSession = new (conn, {
        // Optional property. Defaults to AUTO_ACKNOWLEDGE
        acknowledgementMode: "AUTO_ACKNOWLEDGE"
    });

listener jms:QueueListener queueConsumer = new(jmsSession, "message_queue");

service simpleConsumer on queueConsumer {
    resource function onMessage(jms:QueueReceiverCaller consumer, jms:Message message) returns error? {
        if (message.getType() == jms:TEXT_MESSAGE) {
            var payload = message.getPayload();
            if (payload is string) {
                msgVal = "received: " + <@untainted> payload;
            } else {
                msgVal = io:sprintf("%s", message.getPropertyNames());
                msgVal += <@untainted> <string>message.getProperty("string");
                msgVal += <@untainted> io:sprintf("%s", <int>message.getProperty("int"));
                msgVal += <@untainted> io:sprintf("%s", <float>message.getProperty("float"));
                msgVal += <@untainted> io:sprintf("%s", <boolean>message.getProperty("boolean"));
                msgVal += <@untainted> io:sprintf("%s", <byte>message.getProperty("byte"));
                msgVal += <@untainted> <string>message.getProperty("json");
                msgVal += <@untainted> <string>message.getProperty("xml");
            }

        } else if (message.getType() == jms:BYTES_MESSAGE) {
            msgVal = "bytes ";
            processStreamOrBytesMessage(message);
        } else if (message.getType() == jms:MAP_MESSAGE) {
            var mapPayload = message.getPayload();
            if (mapPayload is map<any>) {
                msgVal = io:sprintf("%s", mapPayload);
            }
        } else if (message.getType() == jms:STREAM_MESSAGE) {
            msgVal = "stream ";
            processStreamOrBytesMessage(message);
        } else if (message.getType() == jms:MESSAGE) {
            msgVal = io:sprintf("%s", message.getCustomHeaders());
        }
        io:println(msgVal);
    }
}

function processStreamOrBytesMessage(jms:Message message) {
    msgVal += io:sprintf("%s", <byte[]>message.getPayload(jms:BYTES, 6));
    msgVal += io:sprintf("%s", <int>message.getPayload(jms:INT));
    msgVal += io:sprintf("%s", <float>message.getPayload(jms:FLOAT));
    msgVal += io:sprintf("%s", <byte>message.getPayload(jms:BYTE));
    msgVal += io:sprintf("%s", <boolean>message.getPayload(jms:BOOLEAN));
    msgVal += io:sprintf("%s", <string>message.getPayload(jms:STRING));
    msgVal += io:sprintf("%s", <string>message.getPayload(jms:STRING));
    msgVal += io:sprintf("%s", <xml>message.getPayload(jms:XML));
    msgVal += io:sprintf("%s", <json>message.getPayload(jms:JSON));
}

function getMsgVal() returns string {
    return msgVal;
}
