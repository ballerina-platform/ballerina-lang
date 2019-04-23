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

import ballerina/nats;
import ballerina/io;

// Initialize NATS listener.
listener nats:Listener subscription = new({ host: "localhost",
                                            port: 4222,
                                            clientId: "s0" });

// Bind consumer to listen to messages published to 'demo' subject.
@nats:ConsumerConfig {
    subject: "demo"
}
service demo on subscription {

    resource function onMessage(nats:Message msg) {
        // Print the incoming message in the std out.
        io:println("Received message : " + msg.getData());
    }

}
