// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

@http:WebSocketServiceConfig {
}
service on new http:Listener(21032) {

    resource function onOpen(http:WebSocketCaller wsEp) {
        http:WebSocketFailoverClient wsClientEp = new({ callbackService: failoverClientCallbackService,
            targetUrls: ["ws://localhost:15300/websocket", "ws://localhost:15200/websocket",
            "ws://localhost:15400/websocket"], failoverIntervalInMillis: 3000});

        wsEp.setAttribute(ASSOCIATED_CONNECTION, wsClientEp);
        wsClientEp.setAttribute(ASSOCIATED_CONNECTION, wsEp);
    }

    resource function onText(http:WebSocketCaller wsEp, string text) {
        http:WebSocketFailoverClient clientEp = getAssociatedFailoverClientEndpoint(wsEp);
        checkpanic clientEp->pushText(text);
    }

    resource function onBinary(http:WebSocketCaller wsEp, byte[] data) {
        http:WebSocketFailoverClient clientEp = getAssociatedFailoverClientEndpoint(wsEp);
        checkpanic clientEp->pushBinary(data);
    }

    resource function onClose(http:WebSocketCaller wsEp, int statusCode, string reason) {
        http:WebSocketFailoverClient clientEp = getAssociatedFailoverClientEndpoint(wsEp);
        checkpanic clientEp->close(statusCode = statusCode, reason = reason);
    }
}

service failoverClientCallbackService = @http:WebSocketServiceConfig {} service {

    resource function onText(http:WebSocketFailoverClient wsEp, string text) {
        http:WebSocketCaller serviceEp = getAssociatedFailoverListener(wsEp);
        checkpanic serviceEp->pushText(text);
    }

    resource function onBinary(http:WebSocketFailoverClient wsEp, byte[] data) {
        http:WebSocketCaller serviceEp = getAssociatedFailoverListener(wsEp);
        checkpanic serviceEp->pushBinary(data);
    }

    resource function onClose(http:WebSocketFailoverClient wsEp, int statusCode, string reason) {
        http:WebSocketCaller serviceEp = getAssociatedFailoverListener(wsEp);
        checkpanic serviceEp->close(statusCode = statusCode, reason = reason);
    }
 };

public function getAssociatedFailoverClientEndpoint(http:WebSocketCaller wsServiceEp)
returns (http:WebSocketFailoverClient) {
    var returnVal = <http:WebSocketFailoverClient>wsServiceEp.getAttribute(ASSOCIATED_CONNECTION);
    return returnVal;
}

public function getAssociatedFailoverListener(http:WebSocketFailoverClient wsClientEp) returns (http:WebSocketCaller) {
    var returnVal = <http:WebSocketCaller>wsClientEp.getAttribute(ASSOCIATED_CONNECTION);
    return returnVal;
}
