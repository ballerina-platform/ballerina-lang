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

service simpleProxy8 = @http:WebSocketServiceConfig {} service {

    resource function onOpen(http:WebSocketCaller wsEp) {
    }
};

@http:ServiceConfig {
    basePath: "/proxy"
}
service simple8 on new http:Listener(21017) {

    @http:ResourceConfig {
        webSocketUpgrade: {
            upgradePath: "/cancel",
            upgradeService: simpleProxy8
        }
    }
    resource function websocketProxy(http:Caller httpEp, http:Request req) {

    }
}



