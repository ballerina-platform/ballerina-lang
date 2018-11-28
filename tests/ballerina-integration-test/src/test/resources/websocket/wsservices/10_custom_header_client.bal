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

final string REMOTE_BACKEND_URL2 = "ws://localhost:15100/websocket";
final string ASSOCIATED_CONNECTION2 = "ASSOCIATED_CONNECTION";
final string strData1 = "data";
final byte[] APPLICATION_DATA = strData1.toByteArray("UTF-8");

@http:WebSocketServiceConfig {
    path: "/pingpong/ws"
}
service PingPongTestService1 on new http:WebSocketListener(9092) {

    resource function onOpen(http:WebSocketCaller wsEp) {
        http:WebSocketClient wsClientEp = new (
            REMOTE_BACKEND_URL2,
            config = {callbackService: clientCallbackService,
            readyOnConnect: false,
            customHeaders: { "X-some-header": "some-header-value" }});
        wsEp.attributes[ASSOCIATED_CONNECTION2] = wsClientEp;
        wsClientEp.attributes[ASSOCIATED_CONNECTION2] = wsEp;
        var returnVal = wsClientEp->ready();
        if (returnVal is error) {
             panic returnVal;
        }
    }

    resource function onText(http:WebSocketCaller wsEp, string text) {
        http:WebSocketClient clientEp;
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

service clientCallbackService = @http:WebSocketServiceConfig {} service {

    resource function onText(http:WebSocketClient wsEp, string text) {
        http:WebSocketCaller serverEp = getAssociatedListener1(wsEp);
        var returnVal = serverEp->pushText(text);
        if (returnVal is error) {
             panic returnVal;
        }
    }
};

public function getAssociatedClientEndpoint1(http:WebSocketCaller wsServiceEp) returns (http:WebSocketClient) {
    var returnVal = <http:WebSocketClient>wsServiceEp.attributes[ASSOCIATED_CONNECTION2];
    return returnVal;
}

public function getAssociatedListener1(http:WebSocketClient wsClientEp) returns (http:WebSocketCaller) {
    var returnVal = <http:WebSocketCaller>wsClientEp.attributes[ASSOCIATED_CONNECTION2];
    return returnVal;
}
