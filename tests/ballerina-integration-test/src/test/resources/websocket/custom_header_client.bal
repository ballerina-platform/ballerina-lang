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

@final string REMOTE_BACKEND_URL = "ws://localhost:15500/websocket";
@final string ASSOCIATED_CONNECTION = "ASSOCIATED_CONNECTION";
@final string data = "data";
@final blob APPLICATION_DATA = data.toBlob("UTF-8");

@http:WebSocketServiceConfig {
    path: "/pingpong/ws"
}
service<http:WebSocketService> PingPongTestService bind { port: 9090 } {

    onOpen(endpoint wsEp) {
        endpoint http:WebSocketClient wsClientEp {
            url: REMOTE_BACKEND_URL,
            callbackService: clientCallbackService,
            readyOnConnect: false,
            customHeaders: { "X-some-header": "some-header-value" }
        };
        wsEp.attributes[ASSOCIATED_CONNECTION] = wsClientEp;
        wsClientEp.attributes[ASSOCIATED_CONNECTION] = wsEp;
        wsClientEp->ready() but {
            error e => log:printError(e.message, err = e)
        };
    }

    onText(endpoint wsEp, string text) {
        endpoint http:WebSocketClient clientEp;
        if (text == "custom-headers") {
            clientEp = getAssociatedClientEndpoint(wsEp);
            clientEp->pushText(text + ":X-some-header") but {
                error e => log:printError("Error sending request headers", err = e)
            };
        }
        if (text == "server-headers") {
            clientEp = getAssociatedClientEndpoint(wsEp);
            clientEp->pushText(clientEp.response.getHeader("X-server-header")) but {
                error e => log:printError("Error sending response headers", err = e)
            };
        }
    }
}

service<http:WebSocketClientService> clientCallbackService {

    onText(endpoint wsEp, string text) {
        endpoint http:WebSocketListener serverEp = getAssociatedListener(wsEp);
        serverEp->pushText(text) but {
            error e => log:printError("Error sending text to client", err = e)
        };
    }
}

public function getAssociatedClientEndpoint(http:WebSocketListener wsServiceEp) returns (http:WebSocketClient) {
    return check <http:WebSocketClient>wsServiceEp.attributes[ASSOCIATED_CONNECTION];
}

public function getAssociatedListener(http:WebSocketClient wsClientEp) returns (http:WebSocketListener) {
    return check <http:WebSocketListener>wsClientEp.attributes[ASSOCIATED_CONNECTION];
}
