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
service on new http:Listener(21034) {

    resource function onOpen(http:WebSocketCaller wsEp) {
         http:WebSocketFailoverClient wsClientEp = new({ callbackService:countDownService,
                readyOnConnect: false, targetUrls:["ws://localhost:15300/websocket",
                "ws://localhost:21033/basic/ws"], handShakeTimeoutInSeconds:7 });

        wsEp.setAttribute(ASSOCIATED_CONNECTION, wsClientEp);
        wsClientEp.setAttribute(ASSOCIATED_CONNECTION, wsEp);
        var returnVal = wsClientEp->ready();
        if (returnVal is http:WebSocketError) {
            panic returnVal;
        }
    }

    resource function onText(http:WebSocketCaller wsEp, string text) {
        http:WebSocketFailoverClient clientEp = getAssociatedFailoverClientEndpoint(wsEp);
        var returnVal = clientEp->pushText(text);
        if (returnVal is http:WebSocketError) {
            panic returnVal;
        }
    }
}

service countDownService = @http:WebSocketServiceConfig {} service {

    resource function onText(http:WebSocketFailoverClient wsEp, string text) {
        http:WebSocketCaller serviceEp = getAssociatedFailoverListener(wsEp);
        var returnVal = serviceEp->pushText(text);
        if (returnVal is http:WebSocketError) {
            panic returnVal;
        }
    }

    //This resource gets invoked when an error occurs in the connection.
    resource function onError(http:WebSocketFailoverClient wsEp, error err) {
        http:WebSocketCaller serviceEp = getAssociatedFailoverListener(wsEp);
        var returnVal = serviceEp->pushText(err.toString());
        if (returnVal is http:WebSocketError) {
            panic returnVal;
        }
    }
};
