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

final string strData2 = "data";
final byte[] APPLICATION_DATA3 = strData2.toBytes();

service clientCallbackService2 = @http:WebSocketServiceConfig {} service {

    resource function onText(http:WebSocketClient wsEp, string text) {
        http:WebSocketCaller serverEp = getAssociatedListener(wsEp);
        var returnVal = serverEp->pushText(text);
        if (returnVal is http:WebSocketError) {
            panic <error> returnVal;
        }
    }

    resource function onPing(http:WebSocketClient wsEp, byte[] localData) {
        http:WebSocketCaller serverEp = getAssociatedListener(wsEp);
        var returnVal = serverEp->pushText("ping-from-remote-server-received");
        if (returnVal is http:WebSocketError) {
            panic <error> returnVal;
        }
    }

    resource function onPong(http:WebSocketClient wsEp, byte[] localData) {
        http:WebSocketCaller serverEp = getAssociatedListener(wsEp);
        var returnVal = serverEp->pushText("pong-from-remote-server-received");
        if (returnVal is http:WebSocketError) {
            panic <error> returnVal;
        }
    }
};

@http:WebSocketServiceConfig {
    path: "/pingpong/ws"
}
service PingPongTestService2 on new http:Listener(21014) {

    resource function onOpen(http:WebSocketCaller wsEp) {
        http:WebSocketClient wsClientEp = new("ws://localhost:15200/websocket", { callbackService:
            clientCallbackService2, readyOnConnect: false });
        wsEp.setAttribute(ASSOCIATED_CONNECTION, wsClientEp);
        wsClientEp.setAttribute(ASSOCIATED_CONNECTION, wsEp);
        var returnVal = wsClientEp->ready();
        if (returnVal is http:WebSocketError) {
           panic <error> returnVal;
        }
    }

    resource function onText(http:WebSocketCaller wsEp, string text) {
        http:WebSocketClient clientEp;

        if (text == "ping-me") {
             var returnVal = wsEp->ping(APPLICATION_DATA3);
             if (returnVal is http:WebSocketError) {
                panic <error> returnVal;
             }
        }

        if (text == "ping-remote-server") {
            clientEp = getAssociatedClientEndpoint(wsEp);
            var returnVal = clientEp->ping(APPLICATION_DATA3);
            if (returnVal is http:WebSocketError) {
                panic <error> returnVal;
            }
        }

        if (text == "tell-remote-server-to-ping") {
            clientEp = getAssociatedClientEndpoint(wsEp);
            var resp = clientEp.getHttpResponse();
            if (resp is http:Response) {
                log:printInfo(resp.getHeader("upgrade"));
            }
            var returnVal = clientEp->pushText("ping");
            if (returnVal is http:WebSocketError) {
                panic <error> returnVal;
            }
        }
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

    resource function onPing(http:WebSocketCaller wsEp, byte[] localData) {
        var returnVal = wsEp->pong(localData);
        if (returnVal is http:WebSocketError) {
            panic <error> returnVal;
        }
    }

    resource function onPong(http:WebSocketCaller wsEp, byte[] localData) {
        var returnVal = wsEp->pushText("pong-from-you");
        if (returnVal is http:WebSocketError) {
            panic <error> returnVal;
        }
    }

}

