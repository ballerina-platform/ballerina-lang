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


import ballerina/io;
@artemis:ServiceConfig {
    queueConfig: {
        queueName: "multicast_queue",
        addressName: "multicast_address",
        routingType: artemis:MULTICAST
    }
}
service multiCastConsumer on artemisListener {
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
        io:print(payload);
        io:println(" multicast ");
    }
}
