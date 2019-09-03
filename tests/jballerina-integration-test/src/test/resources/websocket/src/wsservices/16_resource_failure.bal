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

service simple7 on new http:Listener(21016) {

    @http:ResourceConfig {
        webSocketUpgrade: {
            upgradeService: castErrror
        }
    }
    resource function websocketProxy(http:Caller httpEp, http:Request req) {
        http:WebSocketCaller|http:WebSocketError wsServiceEp =
        httpEp->acceptWebSocketUpgrade({ "X-some-header": "some-header-value" });
        if (wsServiceEp is http:WebSocketCaller) {
            var queryParam = req.getQueryParamValue("q1");
            if (queryParam == ()) {
                error err = error("Query param not set");
                panic err;
            }
            wsServiceEp.setAttribute("Query1", queryParam);
        } else {
            panic wsServiceEp;
        }
    }
}

service castErrror = @http:WebSocketServiceConfig {idleTimeoutInSeconds: 10} service {

    resource function onText(http:WebSocketCaller wsEp, string text) {
        http:WebSocketClient val;
        var returnVal = <http:WebSocketClient>wsEp.getAttribute(ASSOCIATED_CONNECTION);
        val = returnVal;
    }
    resource function onBinary(http:WebSocketCaller wsEp, byte[] data) {
        http:WebSocketClient val;
        var returnVal = <http:WebSocketClient>wsEp.getAttribute(ASSOCIATED_CONNECTION);
        val = returnVal;
    }
    resource function onPing(http:WebSocketCaller wsEp, byte[] data) {
        http:WebSocketClient val;
        var returnVal = <http:WebSocketClient>wsEp.getAttribute(ASSOCIATED_CONNECTION);
        val = returnVal;
    }
    resource function onIdleTimeout(http:WebSocketCaller wsEp) {
        http:WebSocketClient val;
        var returnVal = <http:WebSocketClient>wsEp.getAttribute(ASSOCIATED_CONNECTION);
        val = returnVal;
    }
    resource function onClose(http:WebSocketCaller wsEp, int code, string reason) {
        http:WebSocketClient val;
        var returnVal = <http:WebSocketClient>wsEp.getAttribute(ASSOCIATED_CONNECTION);
        val = returnVal;
    }
};
