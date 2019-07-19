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

final string REMOTE_BACKEND_URL200 = "ws://localhost:15000/websocket";

@http:WebSocketServiceConfig {
    path: "/client/service"
}
service clientFailure200 on new http:WebSocketListener(21020) {

    resource function onOpen(http:WebSocketCaller wsEp) {
        http:WebSocketClient wsClientEp = new(
             REMOTE_BACKEND_URL200);
        var returnVal = wsEp->pushText("Client worked");
        if (returnVal is error) {
            panic <error> returnVal;
        }
    }

    resource function onText(http:WebSocketCaller caller, string text) {
        http:WebSocketClient wsClientEp = new(REMOTE_BACKEND_URL200, {callbackService: ClientService200});
        var returnVal = caller->pushText("Client worked");
        if (returnVal is error) {
            panic <error> returnVal;
        }
    }

    resource function onBinary(http:WebSocketCaller caller, byte[] data) {
        http:WebSocketClient wsClientEp = new(
            REMOTE_BACKEND_URL200,
            {callbackService: callback200});
    }
}
service callback200 = @http:WebSocketServiceConfig {} service {
    resource function onText(http:WebSocketCaller caller, string text) {
    }
};
service ClientService200 = @http:WebSocketServiceConfig {} service{
    resource function onText(http:WebSocketClient caller, string text) {
    }
};
