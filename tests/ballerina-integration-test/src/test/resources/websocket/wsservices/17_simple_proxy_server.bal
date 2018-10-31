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

@final string REMOTE_BACKEND_URL = "ws://localhost:15300/websocket";
@final public string ASSOCIATED_CONNECTION5 = "ASSOCIATED_CONNECTION";

@http:WebSocketServiceConfig {
    path: "/"
}
service<http:WebSocketService> simpleProxy9 bind { port: 9099 } {

    onOpen(endpoint wsEp) {
        endpoint http:WebSocketClient wsClientEp {
            url: REMOTE_BACKEND_URL,
            callbackService: clientCallbackService9,
            readyOnConnect: false
        };
        wsEp.attributes[ASSOCIATED_CONNECTION5] = wsClientEp;
        wsClientEp.attributes[ASSOCIATED_CONNECTION5] = wsEp;
        check wsClientEp->ready();
    }

    onText(endpoint wsEp, string text) {
        endpoint http:WebSocketClient clientEp = getAssociatedClientEndpoint3(wsEp);
        check clientEp->pushText(text);
    }

    onBinary(endpoint wsEp, byte[] data) {
        endpoint http:WebSocketClient clientEp = getAssociatedClientEndpoint3(wsEp);
        check clientEp->pushBinary(data);
    }

    onClose(endpoint wsEp, int statusCode, string reason) {
        endpoint http:WebSocketClient clientEp = getAssociatedClientEndpoint3(wsEp);
        check clientEp->close(statusCode = statusCode, reason = reason);
    }

}

service<http:WebSocketClientService> clientCallbackService9 {
    onText(endpoint wsEp, string text) {
        endpoint http:WebSocketListener serviceEp = getAssociatedListener3(wsEp);
        check serviceEp->pushText(text);
    }

    onBinary(endpoint wsEp, byte[] data) {
        endpoint http:WebSocketListener serviceEp = getAssociatedListener3(wsEp);
        check serviceEp->pushBinary(data);
    }

    onClose(endpoint wsEp, int statusCode, string reason) {
        endpoint http:WebSocketListener serviceEp = getAssociatedListener3(wsEp);
        check serviceEp->close(statusCode = statusCode, reason = reason);
    }
}

public function getAssociatedClientEndpoint3(http:WebSocketListener wsServiceEp) returns (http:WebSocketClient) {
    return check <http:WebSocketClient>wsServiceEp.attributes[ASSOCIATED_CONNECTION5];
}

public function getAssociatedListener3(http:WebSocketClient wsClientEp) returns (http:WebSocketListener) {
    return check <http:WebSocketListener>wsClientEp.attributes[ASSOCIATED_CONNECTION5];
}
