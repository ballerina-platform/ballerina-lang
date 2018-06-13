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

import ballerina/io;
import ballerina/http;

@final string REMOTE_BACKEND_URL = "ws://localhost:15500/websocket";
@final string ASSOCIATED_CONNECTION = "ASSOCIATED_CONNECTION";

@http:WebSocketServiceConfig {
    path: "/proxy/ws"
}
service<http:WebSocketService> simpleProxy bind { port: 9090 } {

    onOpen(endpoint wsEp) {
        endpoint http:WebSocketClient wsClientEp {
            url: REMOTE_BACKEND_URL,
            callbackService: clientCallbackService,
            readyOnConnect: false
        };
        wsEp.attributes[ASSOCIATED_CONNECTION] = wsClientEp;
        wsClientEp.attributes[ASSOCIATED_CONNECTION] = wsEp;
        wsClientEp->ready() but {
            error e => io:println(e.message)
        };
    }

    onText(endpoint wsEp, string text) {
        endpoint http:WebSocketClient clientEp = getAssociatedClientEndpoint(wsEp);
        clientEp->pushText(text) but {
            error e => io:println("server text error")
        };
    }

    onBinary(endpoint wsEp, blob data) {
        endpoint http:WebSocketClient clientEp = getAssociatedClientEndpoint(wsEp);
        clientEp->pushBinary(data) but {
            error e => io:println("server binary error")
        };
    }

    onClose(endpoint wsEp, int statusCode, string reason) {
        endpoint http:WebSocketClient clientEp = getAssociatedClientEndpoint(wsEp);
        clientEp->close(statusCode, reason) but {
            error e => io:println("server closing error")
        };
    }

}

service<http:WebSocketClientService> clientCallbackService {
    onText(endpoint wsEp, string text) {
        endpoint http:WebSocketListener serviceEp = getAssociatedListener(wsEp);
        serviceEp->pushText(text) but {
            error e => io:println("client text error")
        };
    }

    onBinary(endpoint wsEp, blob data) {
        endpoint http:WebSocketListener serviceEp = getAssociatedListener(wsEp);
        serviceEp->pushBinary(data) but {
            error e => io:println("client binary error")
        };
    }

    onClose(endpoint wsEp, int statusCode, string reason) {
        endpoint http:WebSocketListener serviceEp = getAssociatedListener(wsEp);
        serviceEp->close(statusCode, reason) but {
            error e => io:println("client closing error")
        };
    }
}

public function getAssociatedClientEndpoint(http:WebSocketListener wsServiceEp) returns (http:WebSocketClient) {
    return check <http:WebSocketClient>wsServiceEp.attributes[ASSOCIATED_CONNECTION];
}

public function getAssociatedListener(http:WebSocketClient wsClientEp) returns (http:WebSocketListener) {
    return check <http:WebSocketListener>wsClientEp.attributes[ASSOCIATED_CONNECTION];
}
