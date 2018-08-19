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
// This is server implementation for bidirectional streaming scenario

import ballerina/grpc;
import ballerina/io;

// Server endpoint configuration
endpoint grpc:Listener ep5 {
    host:"localhost",
    port:9095
};

@grpc:ServiceConfig {name:"chat",
    clientStreaming:true,
    serverStreaming:true}
service Chat bind ep5 {
    map consMap;
    onOpen(endpoint client) {
        consMap[<string>client.id] = client;
    }

    onMessage(endpoint client, ChatMessage5 chatMsg) {
        endpoint grpc:Listener con;
        string msg = string `{{chatMsg.name}}: {{chatMsg.message}}`;
        io:println(msg);
        string[] conKeys = consMap.keys();
        int len = lengthof conKeys;
        int i = 0;
        while (i < len) {
            con = check <grpc:Listener>consMap[conKeys[i]];
            error? err = con->send(msg);
            io:println(err.message but { () => "" });
            i = i + 1;
        }
    }

    onError(endpoint client, error err) {
        if (err != ()) {
            io:println("Something unexpected happens at server : " + err.message);
        }
    }

    onComplete(endpoint client) {
        endpoint grpc:Listener con;
        string msg = string `{{client.id}} left the chat`;
        io:println(msg);
        var v = consMap.remove(<string>client.id);
        string[] conKeys = consMap.keys();
        int len = lengthof conKeys;
        int i = 0;
        while (i < len) {
            con = check <grpc:Listener>consMap[conKeys[i]];
            error? err = con->send(msg);
            io:println(err.message but { () => "" });
            i = i + 1;
        }
    }
}

type ChatMessage5 record {
    string name;
    string message;
};
