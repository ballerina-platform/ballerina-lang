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

// Server endpoint configuration
listener grpc:Listener server7 = new (9095);

@grpc:ServiceConfig {name:"chat",
    requestType: ChatMessage,
    responseType: string,
    clientStreaming:true,
    serverStreaming:true}
service Chat on server7 {

    resource function onOpen(grpc:Caller caller) {
    }

    resource function onMessage(grpc:Caller caller, ChatMessage chatMsg) {
    }

    resource function onError(grpc:Caller caller, error err) {
    }

    resource function onComplete(grpc:Caller caller) {
    }
}

type ChatMessage record {
    string name = "";
    string message = "";
};
