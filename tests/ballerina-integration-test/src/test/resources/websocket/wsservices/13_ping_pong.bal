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

@final string REMOTE_BACKEND_URL3 = "ws://localhost:15200/websocket";
@final string ASSOCIATED_CONNECTION3 = "ASSOCIATED_CONNECTION";
@final string strData2 = "data";
@final byte[] APPLICATION_DATA3 = strData2.toByteArray("UTF-8");

@http:WebSocketServiceConfig {
    path: "/pingpong/ws"
}
service<http:WebSocketService> PingPongTestService2 bind { port: 9095 } {

    onOpen(endpoint wsEp) {
        endpoint http:WebSocketClient wsClientEp {
            url: REMOTE_BACKEND_URL3,
            callbackService: clientCallbackService2,
            readyOnConnect: false
        };
        wsEp.attributes[ASSOCIATED_CONNECTION3] = wsClientEp;
        wsClientEp.attributes[ASSOCIATED_CONNECTION3] = wsEp;
        var returnVal = wsClientEp->ready();
        if (returnVal is error) {
             panic returnVal;
        }
    }

    onText(endpoint wsEp, string text) {
        endpoint http:WebSocketClient clientEp;

        if (text == "ping-me") {
             var returnVal = wsEp->ping(APPLICATION_DATA3);
             if (returnVal is error) {
                  panic returnVal;
             }
        }

        if (text == "ping-remote-server") {
            clientEp = getAssociatedClientEndpoint2(wsEp);
            var returnVal = clientEp->ping(APPLICATION_DATA3);
            if (returnVal is error) {
                 panic returnVal;
            }
        }

        if (text == "tell-remote-server-to-ping") {
            clientEp = getAssociatedClientEndpoint2(wsEp);
            log:printInfo(clientEp.response.getHeader("upgrade"));
            var returnVal = clientEp->pushText("ping");
            if (returnVal is error) {
                 panic returnVal;
            }
        }
        if (text == "custom-headers") {
            clientEp = getAssociatedClientEndpoint2(wsEp);
            var returnVal = clientEp->pushText(text + ":X-some-header");
            if (returnVal is error) {
                 panic returnVal;
            }
        }
        if (text == "server-headers") {
            clientEp = getAssociatedClientEndpoint2(wsEp);
            var returnVal = clientEp->pushText(clientEp.response.getHeader("X-server-header"));
            if (returnVal is error) {
                 panic returnVal;
            }
        }
    }

    onPing(endpoint wsEp, byte[] localData) {
        var returnVal = wsEp->pong(localData);
        if (returnVal is error) {
             panic returnVal;
        }
    }

    onPong(endpoint wsEp, byte[] localData) {
        var returnVal = wsEp->pushText("pong-from-you");
        if (returnVal is error) {
             panic returnVal;
        }
    }

}

service<http:WebSocketClientService> clientCallbackService2 {

    onText(endpoint wsEp, string text) {
        endpoint http:WebSocketListener serverEp = getAssociatedListener2(wsEp);
        var returnVal = serverEp->pushText(text);
        if (returnVal is error) {
             panic returnVal;
        }
    }

    onPing(endpoint wsEp, byte[] localData) {
        endpoint http:WebSocketListener serverEp = getAssociatedListener2(wsEp);
        var returnVal = serverEp->pushText("ping-from-remote-server-received");
        if (returnVal is error) {
             panic returnVal;
        }
    }

    onPong(endpoint wsEp, byte[] localData) {
        endpoint http:WebSocketListener serverEp = getAssociatedListener2(wsEp);
        var returnVal = serverEp->pushText("pong-from-remote-server-received");
        if (returnVal is error) {
             panic returnVal;
        }
    }
}

public function getAssociatedClientEndpoint2(http:WebSocketListener wsServiceEp) returns (http:WebSocketClient) {
    var returnVal = <http:WebSocketClient>wsServiceEp.attributes[ASSOCIATED_CONNECTION3];
    if (returnVal is error) {
         panic returnVal;
    } else {
         return returnVal;
    }
}

public function getAssociatedListener2(http:WebSocketClient wsClientEp) returns (http:WebSocketListener) {
    var returnVal = <http:WebSocketListener>wsClientEp.attributes[ASSOCIATED_CONNECTION3];
    if (returnVal is error) {
         panic returnVal;
    } else {
         return returnVal;
    }
}
