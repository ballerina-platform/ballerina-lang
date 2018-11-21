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

@final string REMOTE_BACKEND_URL2 = "ws://localhost:15100/websocket";
@final string ASSOCIATED_CONNECTION2 = "ASSOCIATED_CONNECTION";
@final string strData1 = "data";
@final byte[] APPLICATION_DATA = strData1.toByteArray("UTF-8");

@http:WebSocketServiceConfig {
    path: "/pingpong/ws"
}
service<http:WebSocketService> PingPongTestService1 bind { port: 9092 } {

    onOpen(endpoint wsEp) {
        endpoint http:WebSocketClient wsClientEp {
            url: REMOTE_BACKEND_URL2,
            callbackService: clientCallbackService,
            readyOnConnect: false,
            customHeaders: { "X-some-header": "some-header-value" }
        };
        wsEp.attributes[ASSOCIATED_CONNECTION2] = wsClientEp;
        wsClientEp.attributes[ASSOCIATED_CONNECTION2] = wsEp;
        var returnVal = wsClientEp->ready();
        if (returnVal is error) {
             panic returnVal;
        }
    }

    onText(endpoint wsEp, string text) {
        endpoint http:WebSocketClient clientEp;
        if (text == "custom-headers") {
            clientEp = getAssociatedClientEndpoint1(wsEp);
            var returnVal = clientEp->pushText(text + ":X-some-header");
            if (returnVal is error) {
                 panic returnVal;
            }
        }
        if (text == "server-headers") {
            clientEp = getAssociatedClientEndpoint1(wsEp);
            var returnVal = clientEp->pushText(clientEp.response.getHeader("X-server-header"));
            if (returnVal is error) {
                 panic returnVal;
            }
        }
    }
}

service<http:WebSocketClientService> clientCallbackService {

    onText(endpoint wsEp, string text) {
        endpoint http:WebSocketListener serverEp = getAssociatedListener1(wsEp);
        var returnVal = serverEp->pushText(text);
        if (returnVal is error) {
             panic returnVal;
        }
    }
}

public function getAssociatedClientEndpoint1(http:WebSocketListener wsServiceEp) returns (http:WebSocketClient) {
    var returnVal = <http:WebSocketClient>wsServiceEp.attributes[ASSOCIATED_CONNECTION2];
    if (returnVal is error) {
         panic returnVal;
    } else {
         return returnVal;
    }
}

public function getAssociatedListener1(http:WebSocketClient wsClientEp) returns (http:WebSocketListener) {
    var returnVal = <http:WebSocketListener>wsClientEp.attributes[ASSOCIATED_CONNECTION2];
    if (returnVal is error) {
         panic returnVal;
    } else {
         return returnVal;
    }
}
