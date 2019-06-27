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

service simple7 on new http:Listener(9097) {

    @http:ResourceConfig {
        webSocketUpgrade: {
            upgradeService: castErrror
        }
    }
    resource function websocketProxy(http:Caller httpEp, http:Request req, string path1, string path2) {
        http:WebSocketCaller wsServiceEp;
        wsServiceEp = httpEp->acceptWebSocketUpgrade({ "X-some-header": "some-header-value" });
        wsServiceEp.attributes["Query1"] = req.getQueryParams().q1;
    }
}

service castErrror = @http:WebSocketServiceConfig {idleTimeoutInSeconds: 10} service {

    resource function onText(http:WebSocketCaller wsEp, string text) {
        http:WebSocketClient val;
        var returnVal = <http:WebSocketClient>wsEp.attributes[ASSOCIATED_CONNECTION];
        val = returnVal;
    }
    resource function onBinary(http:WebSocketCaller wsEp, byte[] data) {
        http:WebSocketClient val;
        var returnVal = <http:WebSocketClient>wsEp.attributes[ASSOCIATED_CONNECTION];
        val = returnVal;
    }
    resource function onPing(http:WebSocketCaller wsEp, byte[] data) {
        http:WebSocketClient val;
        var returnVal = <http:WebSocketClient>wsEp.attributes[ASSOCIATED_CONNECTION];
        val = returnVal;
    }
    resource function onIdleTimeout(http:WebSocketCaller wsEp) {
        http:WebSocketClient val;
        var returnVal = <http:WebSocketClient>wsEp.attributes[ASSOCIATED_CONNECTION];
        val = returnVal;
    }
    resource function onClose(http:WebSocketCaller wsEp, int code, string reason) {
        http:WebSocketClient val;
        var returnVal = <http:WebSocketClient>wsEp.attributes[ASSOCIATED_CONNECTION];
        val = returnVal;
    }
};
