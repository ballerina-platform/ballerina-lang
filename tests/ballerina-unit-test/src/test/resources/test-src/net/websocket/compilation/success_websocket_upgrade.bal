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

import ballerina/io;
import ballerina/http;

@final string REMOTE_BACKEND_URL = "ws://localhost:15500/websocket";

endpoint http:Listener httpCaller {
    port: 9090
};

service<http:Service> proxy bind httpCaller {

    @http:ResourceConfig {
        webSocketUpgrade: {
            upgradePath: "/ws",
            upgradeService: wsService

        }
    }
    websocketProxy(endpoint caller, http:Request req) {
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/hello"
    }

    sayHello(endpoint caller, http:Request req) {
        http:Response res = new;
        res.setTextPayload("Successful");
        _ = caller->respond(res);
    }

}

service<http:WebSocketService> wsService {

    onOpen(endpoint caller) {
    }

}
