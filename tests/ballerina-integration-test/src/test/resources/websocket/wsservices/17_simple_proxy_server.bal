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

final string REMOTE_BACKEND_URL = "ws://localhost:15300/websocket";
final string ASSOCIATED_CONNECTION5 = "ASSOCIATED_CONNECTION";

@http:WebSocketServiceConfig {
    path: "/"
}
service simpleProxy9 on new http:WebSocketListener(9099) {

    resource function onOpen(http:WebSocketCaller wsEp) {
        http:WebSocketClient wsClientEp = new(
            REMOTE_BACKEND_URL,
            config = {callbackService: clientCallbackService9,
            readyOnConnect: false});
        wsEp.attributes[ASSOCIATED_CONNECTION5] = wsClientEp;
        wsClientEp.attributes[ASSOCIATED_CONNECTION5] = wsEp;
        var returnVal = wsClientEp->ready();
        if (returnVal is error) {
             panic returnVal;
        }
    }

    resource function onText(http:WebSocketCaller wsEp, string text) {
        http:WebSocketClient clientEp = getAssociatedClientEndpoint3(wsEp);
        var returnVal = clientEp->pushText(text);
        if (returnVal is error) {
             panic returnVal;
        }
    }

    resource function onBinary(http:WebSocketCaller wsEp, byte[] data) {
        http:WebSocketClient clientEp = getAssociatedClientEndpoint3(wsEp);
        var returnVal = clientEp->pushBinary(data);
        if (returnVal is error) {
             panic returnVal;
        }
    }

    resource function onClose(http:WebSocketCaller wsEp, int statusCode, string reason) {
        http:WebSocketClient clientEp = getAssociatedClientEndpoint3(wsEp);
        var returnVal = clientEp->close(statusCode = statusCode, reason = reason);
        if (returnVal is error) {
             panic returnVal;
        }
    }

}

service clientCallbackService9 = @http:WebSocketServiceConfig {} service {
    resource function onText(http:WebSocketCaller wsEp, string text) {
        http:WebSocketClient serviceEp = getAssociatedListener3(wsEp);
        var returnVal = serviceEp->pushText(text);
        if (returnVal is error) {
             panic returnVal;
        }
    }

    resource function onBinary(http:WebSocketCaller wsEp, byte[] data) {
        http:WebSocketClient serviceEp = getAssociatedListener3(wsEp);
        var returnVal = serviceEp->pushBinary(data);
        if (returnVal is error) {
             panic returnVal;
        }
    }

    resource function onClose(http:WebSocketCaller wsEp, int statusCode, string reason) {
        http:WebSocketClient serviceEp = getAssociatedListener3(wsEp);
        var returnVal = serviceEp->close(statusCode = statusCode, reason = reason);
        if (returnVal is error) {
             panic returnVal;
        }
    }
};

public function getAssociatedClientEndpoint3(http:WebSocketCaller wsServiceEp) returns (http:WebSocketClient) {
    var returnVal = <http:WebSocketClient>wsServiceEp.attributes[ASSOCIATED_CONNECTION5];
    return returnVal;
}

public function getAssociatedListener3(http:WebSocketCaller wsClientEp) returns (http:WebSocketClient) {
    var returnVal = <http:WebSocketClient>wsClientEp.attributes[ASSOCIATED_CONNECTION5];
    return returnVal;
}
