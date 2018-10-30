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
endpoint grpc:Listener ep {
    port:9095
};

@grpc:ServiceConfig {name:"chat",
    requestType: ChatMessage,
    responseType: string,
    clientStreaming:true,
    serverStreaming:true}
service Chat bind ep {

    onOpen(endpoint client) {
    }

    onMessage(endpoint client, ChatMessage chatMsg) {
    }

    onError(endpoint client, error err) {
    }

    onComplete(endpoint client) {
    }
}

type ChatMessage record {
    string name;
    string message;
};
