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

import ballerina/http;
import ballerina/log;

@http:WebSocketServiceConfig {
    path: "/failover/ws"
}
service FailoverTestService on new http:WebSocketListener(9201) {

    resource function onOpen(http:WebSocketCaller wsEp) {
        http:FailoverwebSocketClient wsClientEp = new(config = { callbackService:clientCallbackService8, targetURLs:
        ["ws://localhost:1520/websocket", "ws://localhost:1520/websocket1", "ws://localhost:15200/websocket"]});
        wsEp.attributes[ASSOCIATED_CONNECTION] = wsClientEp;
        wsClientEp.attributes[ASSOCIATED_CONNECTION] = wsEp;
        var returnVal = wsClientEp->ready();
        if (returnVal is error) {
             panic returnVal;
        }
    }

    resource function onPing(http:WebSocketCaller wsEp, byte[] localData) {
        var returnVal = wsEp->pong(localData);
        if (returnVal is error) {
             panic returnVal;
        }
    }
}

service clientCallbackService8 = @http:WebSocketServiceConfig {} service {

    resource function onPing(http:WebSocketClient wsEp, byte[] localData) {
        http:WebSocketCaller serverEp = getAssociatedListener(wsEp);
        var returnVal = serverEp->pushText("ping-from-remote-server-received");
        if (returnVal is error) {
             panic returnVal;
        }
    }
};
