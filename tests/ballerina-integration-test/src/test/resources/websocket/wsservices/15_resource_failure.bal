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

@final string ASSOCIATED_CONNECTION4 = "ASSOCIATED_CONNECTION";
service<http:Service> simple7 bind { port: 9097 } {

    @http:ResourceConfig {
        webSocketUpgrade: {
            upgradeService: castErrror
        }
    }
    websocketProxy(endpoint httpEp, http:Request req, string path1, string path2) {
        endpoint http:WebSocketListener wsServiceEp;
        wsServiceEp = httpEp->acceptWebSocketUpgrade({ "X-some-header": "some-header-value" });
        wsServiceEp.attributes["Query1"] = req.getQueryParams().q1;
    }
}
@http:WebSocketServiceConfig {
    idleTimeoutInSeconds: 10
}
service<http:WebSocketService> castErrror {

    onText(endpoint wsEp, string text) {
        endpoint http:WebSocketClient val;
        var returnVal = <http:WebSocketClient>wsEp.attributes[ASSOCIATED_CONNECTION4];
        if (returnVal is error) {
             panic returnVal;
        } else {
             val = returnVal;
        }
    }
    onBinary(endpoint wsEp, byte[] data) {
        endpoint http:WebSocketClient val;
        var returnVal = <http:WebSocketClient>wsEp.attributes[ASSOCIATED_CONNECTION4];
        if (returnVal is error) {
             panic returnVal;
        } else {
             val = returnVal;
        }
    }
    onPing(endpoint wsEp, byte[] data) {
        endpoint http:WebSocketClient val;
        var returnVal = <http:WebSocketClient>wsEp.attributes[ASSOCIATED_CONNECTION4];
        if (returnVal is error) {
             panic returnVal;
        } else {
             val = returnVal;
        }
    }
    onIdleTimeout(endpoint wsEp) {
        endpoint http:WebSocketClient val;
        var returnVal = <http:WebSocketClient>wsEp.attributes[ASSOCIATED_CONNECTION4];
        if (returnVal is error) {
             panic returnVal;
        } else {
             val = returnVal;
        }
    }
    onClose(endpoint wsEp, int code, string reason) {
        endpoint http:WebSocketClient val;
        var returnVal = <http:WebSocketClient>wsEp.attributes[ASSOCIATED_CONNECTION4];
        if (returnVal is error) {
             panic returnVal;
        } else {
             val = returnVal;
        }
    }
}
