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

import ballerina/auth;
import ballerina/http;
import ballerina/io;
import ballerina/log;

http:BasicAuthHandler inboundBasicAuthHandler = new (new auth:InboundBasicAuthProvider());

listener http:Listener httpServi = new (21040, config = {
    auth: {
        authHandlers: [inboundBasicAuthHandler],
        mandateSecureSocket: false
    }
});

@http:ServiceConfig {
    basePath: "/auth"
}
service httpUpgradeServi on httpServi {
    @http:ResourceConfig {
        webSocketUpgrade: {
            upgradePath: "/ws",
            upgradeService: upgradedService
        }
    }
    resource function upgrader(http:Caller caller, http:Request req) {
        log:printInfo("WS upgrade resource");
    }
}
service upgradedService = @http:WebSocketServiceConfig {} service {

    resource function onOpen(http:WebSocketCaller caller) {
        io:println("onOpen: " + caller.getConnectionId());
    }

    resource function onText(http:WebSocketCaller caller, string text, boolean finalFrame) {
        checkpanic caller->pushText(text);
    }

    resource function onClose(http:WebSocketCaller caller,int statusCode,string reason) {
        io:println("onClose: " + caller.getConnectionId());
    }
};
