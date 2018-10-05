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
import ballerina/io;
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
        wsClientEp->ready() but {
            error e => io:println(e.message)
        };
    }

    onText(endpoint wsEp, string text) {
        endpoint http:WebSocketClient clientEp;

        if (text == "ping-me") {
            wsEp->ping(APPLICATION_DATA3) but {
                error e => io:println("error sending server ping")
            };
        }

        if (text == "ping-remote-server") {
            clientEp = getAssociatedClientEndpoint2(wsEp);
            clientEp->ping(APPLICATION_DATA3) but {
                error e => io:println("error sending client ping")
            };
        }

        if (text == "tell-remote-server-to-ping") {
            clientEp = getAssociatedClientEndpoint2(wsEp);
            io:println(clientEp.response.getHeader("upgrade"));
            clientEp->pushText("ping") but {
                error e => io:println("error sending client ping")
            };
        }
        if (text == "custom-headers") {
            clientEp = getAssociatedClientEndpoint2(wsEp);
            clientEp->pushText(text + ":X-some-header") but {
                error e => log:printError("Error sending request headers", err = e)
            };
        }
        if (text == "server-headers") {
            clientEp = getAssociatedClientEndpoint2(wsEp);
            clientEp->pushText(clientEp.response.getHeader("X-server-header")) but {
                error e => log:printError("Error sending response headers", err = e)
            };
        }
    }

    onPing(endpoint wsEp, byte[] localData) {
        wsEp->pong(localData) but {
            error e => io:println("Error sending server pong")
        };
    }

    onPong(endpoint wsEp, byte[] localData) {
        wsEp->pushText("pong-from-you") but {
            error e => io:println("server text error")
        };
    }

}

service<http:WebSocketClientService> clientCallbackService2 {

    onText(endpoint wsEp, string text) {
        endpoint http:WebSocketListener serverEp = getAssociatedListener2(wsEp);
        serverEp->pushText(text) but {
            error e => io:println("error sending client text")
        };
    }

    onPing(endpoint wsEp, byte[] localData) {
        endpoint http:WebSocketListener serverEp = getAssociatedListener2(wsEp);
        serverEp->pushText("ping-from-remote-server-received") but {
            error e => io:println("error sending client text")
        };
    }

    onPong(endpoint wsEp, byte[] localData) {
        endpoint http:WebSocketListener serverEp = getAssociatedListener2(wsEp);
        serverEp->pushText("pong-from-remote-server-received") but {
            error e => io:println("error sending client text")
        };
    }
}

public function getAssociatedClientEndpoint2(http:WebSocketListener wsServiceEp) returns (http:WebSocketClient) {
    return check <http:WebSocketClient>wsServiceEp.attributes[ASSOCIATED_CONNECTION3];
}

public function getAssociatedListener2(http:WebSocketClient wsClientEp) returns (http:WebSocketListener) {
    return check <http:WebSocketListener>wsClientEp.attributes[ASSOCIATED_CONNECTION3];
}
