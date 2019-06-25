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
import ballerina/io;

artemis:Connection con = new("tcp://localhost:61616");
artemis:Session session = new(con);
listener artemis:Listener artemisListener = new(session);

@artemis:ServiceConfig {
    queueConfig: {
        queueName: "anycast_queue"
    }
}
service anyCastConsumer on artemisListener {
    resource function onMessage(artemis:Message message) returns error? {
        var payload = message.getPayload();
        if (payload is byte[]) {
            io:print("byte[] ");
        } else if (payload is map<string>) {
            io:print("map<string> ");
        } else if (payload is map<int>) {
            io:print("map<int> ");
        } else if (payload is map<float>) {
            io:print("map<float> ");
        } else if (payload is map<byte>) {
            io:print("map<byte> ");
        } else if (payload is map<boolean>) {
            io:print("map<boolean> ");
        } else if (payload is map<byte[]>) {
            io:print("map<byte[]> ");
        } else if (payload is string) {
            if (payload == "Properties' test") {
                printProperty(message);
            } else {
                io:print("string ");
            }
        }
        io:print("message ");
        io:println(payload);
    }
}

function printProperty(artemis:Message message) {
    var stringProperty = message.getProperty("string");
    if (stringProperty is string) {
        io:print("string property ");
        io:print(stringProperty);
    }
    var intProperty = message.getProperty("int");
    if (intProperty is int) {
        io:print(", int property ");
        io:print(intProperty);
    }
    var floatProperty = message.getProperty("float");
    if (floatProperty is float) {
        io:print(", float property ");
        io:print(floatProperty);
    }
    var booleanProperty = message.getProperty("boolean");
    if (booleanProperty is boolean) {
        io:print(", boolean property ");
        io:print(booleanProperty);
    }
    var byteProperty = message.getProperty("byte");
    if (byteProperty is byte) {
        io:print(", byte property ");
        io:print(byteProperty);
    }
    var byteArrayProperty = message.getProperty("byteArray");
    if (byteArrayProperty is byte[]) {
        io:print(", byteArray property ");
        io:print(byteArrayProperty);
    }
    io:print(" ");
}