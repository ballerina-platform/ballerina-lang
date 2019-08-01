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
// under the License.

import ballerina/http;
import ballerina/log;

final string failoverstrData = "data";
final byte[] FAILOVER_APPLICATION_DATA = failoverstrData.toBytes();

service failoverclientCallbackService = @http:WebSocketServiceConfig {} service {

    resource function onText(http:WebSocketFailoverClient wsEp, string text) {
        http:WebSocketCaller serviceEp = getAssociatedListener1(wsEp);
        var returnVal = serviceEp->pushText(text);
        if (returnVal is http:WebSocketError) {
            panic <error> returnVal;
        }
    }

    resource function onBinary(http:WebSocketFailoverClient wsEp, byte[] data) {
        http:WebSocketCaller serviceEp = getAssociatedListener1(wsEp);
        var returnVal = serviceEp->pushBinary(data);
        if (returnVal is http:WebSocketError) {
            panic <error> returnVal;
        }
    }
};

@http:WebSocketServiceConfig {
    path: "/failover/ws"
}
service failoverTestService on new http:WebSocketListener(21029) {

    resource function onOpen(http:WebSocketCaller wsEp) {
        http:WebSocketFailoverClient wsClientEp = new({ callbackService:
            failoverclientCallbackService, readyOnConnect: false, targetUrls: ["ws://localhost:8080/websocket",
            "ws://localhost:15100/websocket", "ws://localhost:15200/websocket"]});
        wsEp.attributes[ASSOCIATED_CONNECTION] = wsClientEp;
        wsClientEp.attributes[ASSOCIATED_CONNECTION] = wsEp;
        var returnVal = wsClientEp->ready();
        if (returnVal is http:WebSocketError) {
           panic <error> returnVal;
        }
    }

    resource function onText(http:WebSocketCaller wsEp, string text) {
        http:WebSocketFailoverClient clientEp = getAssociatedClientEndpoint1(wsEp);
        var returnVal = clientEp->pushText(text);
        if (returnVal is http:WebSocketError) {
            panic <error> returnVal;
        }
    }

    resource function onBinary(http:WebSocketCaller wsEp, byte[] data) {
        http:WebSocketFailoverClient clientEp = getAssociatedClientEndpoint1(wsEp);
        var returnVal = clientEp->pushBinary(data);
        if (returnVal is http:WebSocketError) {
            panic <error> returnVal;
        }
    }
}
