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

import ballerina/http;
import ballerina/log;

@final string REMOTE_BACKEND_URL200 = "ws://localhost:15000/websocket";

@http:WebSocketServiceConfig {
    path: "/client/service"
}
service<http:WebSocketService> clientFailure200 bind { port: 9200 } {

    onOpen(endpoint wsEp) {
        endpoint http:WebSocketClient wsClientEp {
            url: REMOTE_BACKEND_URL200
        };
        _ = wsEp->pushText("Client worked");
    }

    onText(endpoint caller, string text) {
        endpoint http:WebSocketClient wsClientEp {
            url: REMOTE_BACKEND_URL200,
            callbackService: ClientService200
        };
        _ = caller->pushText("Client worked");
    }

    onBinary(endpoint caller, byte[] data) {
        endpoint http:WebSocketClient wsClientEp {
            url: REMOTE_BACKEND_URL200,
            callbackService: callback200
        };
    }
}
service<http:WebSocketService> callback200 {
}
service<http:WebSocketClientService> ClientService200 {
}
