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

import ballerina/http;
import ballerina/io;

@http:WebSocketServiceConfig {
}
service on new http:Listener(21034) {

    resource function onOpen(http:WebSocketCaller wsEp) {
        http:WebSocketClient wsClientEp = new("ws://localhost:15300/websocket", { callbackService:
            readyOnConnectService, readyOnConnect: true, retryConfig: {}});
        wsEp.setAttribute(ASSOCIATED_CONNECTION, wsClientEp);
        wsClientEp.setAttribute(ASSOCIATED_CONNECTION, wsEp);
    }

    resource function onText(http:WebSocketCaller wsEp, string text) {
        http:WebSocketClient clientEp = getAssociatedClientEndpoint(wsEp);
        var returnVal = clientEp->ready();
        if (returnVal is error) {
            checkpanic wsEp->pushText(returnVal.toString());
        }
    }
}

service readyOnConnectService = @http:WebSocketServiceConfig {} service {
    resource function onError(http:WebSocketClient clientCaller, error err) {
        http:WebSocketCaller? caller = serverCaller;
        if (caller is http:WebSocketCaller) {
            checkpanic caller->pushText(err.toString());
        } else {
            io:println("serverCaller has not been set");
        }
    }
};
