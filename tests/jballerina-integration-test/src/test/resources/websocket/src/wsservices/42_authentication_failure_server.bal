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

http:BasicAuthHandler basicAuthHandler = new (new auth:InboundBasicAuthProvider());

listener http:Listener httpServ = new (21042, config = {
    auth: {
        authHandlers: [inboundBasicAuthHandler],
        mandateSecureSocket: false
    }
});

@http:ServiceConfig {
    basePath: "/auth"
}
service upgradeServ on httpServ {

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
service upgradedServ = @http:WebSocketServiceConfig {} service {

    resource function onOpen(http:WebSocketCaller caller) {
        io:println("onOpen: " + caller.getConnectionId());
    }
};
