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

const string UPGRADE_PATH = "/ws";
const string RESOURCE_PATH = "/world";
const string BASE_PATH = "/hello";

@http:ServiceConfig {
    basePath: BASE_PATH
}
service httpService on new http:Listener(21030) {

    @http:ResourceConfig {
        webSocketUpgrade: {
            upgradePath: UPGRADE_PATH,
            upgradeService: wsService
        }
    }
    resource function upgrader(http:Caller caller, http:Request req) {
    }
}

service wsService = @http:WebSocketServiceConfig {subProtocols: ["xml, json"]} service {

    resource function onOpen(http:WebSocketCaller caller) {
        var returnVal = caller->pushText("Hello");
        if (returnVal is http:WebSocketError) {
            panic returnVal;
        }
    }
};
