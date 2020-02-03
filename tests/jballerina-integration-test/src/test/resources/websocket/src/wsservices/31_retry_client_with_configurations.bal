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

@http:WebSocketServiceConfig {
}
service on new http:Listener(21031) {

    resource function onOpen(http:WebSocketCaller wsEp) {
        http:WebSocketClient wsClientEp = new("ws://localhost:15300/websocket", { callbackService:
        retryCallbackService, readyOnConnect: false, retryConfig: {maxCount: 10, intervalInMillis: 500,
        backOffFactor: 0.5}});
        wsEp.setAttribute(ASSOCIATED_CONNECTION, wsClientEp);
        wsClientEp.setAttribute(ASSOCIATED_CONNECTION, wsEp);
        checkpanic wsClientEp->ready();
    }

    resource function onText(http:WebSocketCaller wsEp, string text) {
        http:WebSocketClient clientEp = getAssociatedClientEndpoint(wsEp);
        checkpanic clientEp->pushText(text);
    }

    resource function onBinary(http:WebSocketCaller wsEp, byte[] data) {
        http:WebSocketClient clientEp = getAssociatedClientEndpoint(wsEp);
        checkpanic clientEp->pushBinary(data);
    }
}

service retryCallbackService = @http:WebSocketServiceConfig {} service {
    resource function onText(http:WebSocketClient wsEp, string text) {
        http:WebSocketCaller serviceEp = getAssociatedListener(wsEp);
        checkpanic serviceEp->pushText(text);
    }

    resource function onBinary(http:WebSocketClient wsEp, byte[] data) {
        http:WebSocketCaller serviceEp = getAssociatedListener(wsEp);
        checkpanic serviceEp->pushBinary(data);
    }
};
