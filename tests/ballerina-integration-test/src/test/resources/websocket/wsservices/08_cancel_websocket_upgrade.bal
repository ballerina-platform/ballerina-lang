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

import ballerina/log;
import ballerina/http;

endpoint http:Listener ep1 {
    port: 9090
};

service<http:Service> simple bind ep1 {

    @http:ResourceConfig {
        webSocketUpgrade: {
            upgradePath: "/cancel",
            upgradeService: simpleProxy1
        }
    }
    websocketProxy(endpoint httpEp, http:Request req, string path1, string path2) {
        var returnVal = httpEp->cancelWebSocketUpgrade(404, "Cannot proceed");
        if (returnVal is error) {
             panic returnVal;
        }
    }
}

service<http:WebSocketService> simpleProxy1 {

    onOpen(endpoint wsEp) {
    }
}

service<http:Service> cannotcancel bind ep1 {

    @http:ResourceConfig {
        webSocketUpgrade: {
            upgradePath: "/cannot/cancel",
            upgradeService: simpleProxy1
        }
    }
    websocketProxy(endpoint httpEp, http:Request req, string path1, string path2) {
        var returnVal = httpEp->cancelWebSocketUpgrade(200, "Cannot proceed");
        if (returnVal is error) {
             panic returnVal;
        }
    }
}

