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

final string strData1 = "data";
final byte[] APPLICATION_DATA = strData1.toBytes();

@http:WebSocketServiceConfig {
    path: "/pingpong/ws"
}
service PingPongTestService1 on new http:Listener(21011) {

    resource function onOpen(http:WebSocketCaller wsEp) {
        http:WebSocketClient wsClientEp = new("ws://localhost:15100/websocket", { callbackService:
            clientCallbackService, readyOnConnect: false, customHeaders: { "X-some-header": "some-header-value" } });
        wsEp.setAttribute(ASSOCIATED_CONNECTION, wsClientEp);
        wsClientEp.setAttribute(ASSOCIATED_CONNECTION, wsEp);
        var returnVal = wsClientEp->ready();
        if (returnVal is http:WebSocketError) {
            panic <error> returnVal;
        }
    }

    resource function onText(http:WebSocketCaller wsEp, string text) {
        http:WebSocketClient clientEp;
        if (text == "custom-headers") {
            clientEp = getAssociatedClientEndpoint(wsEp);
            var returnVal = clientEp->pushText(text + ":X-some-header");
            if (returnVal is http:WebSocketError) {
                panic <error> returnVal;
            }
        }
        if (text == "server-headers") {
            clientEp = getAssociatedClientEndpoint(wsEp);
            var resp = clientEp.getHttpResponse();
            if (resp is http:Response) {
                var returnVal = clientEp->pushText(resp.getHeader("X-server-header"));
                if (returnVal is http:WebSocketError) {
                    panic <error> returnVal;
                }
            }
        }
    }
}

service clientCallbackService = @http:WebSocketServiceConfig {} service {

    resource function onText(http:WebSocketClient wsEp, string text) {
        http:WebSocketCaller serverEp = getAssociatedListener(wsEp);
        var returnVal = serverEp->pushText(text);
        if (returnVal is http:WebSocketError) {
            panic <error> returnVal;
        }
    }
};
