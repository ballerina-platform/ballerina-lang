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
import ballerina/runtime;

@http:ServiceConfig {
    basePath: "/basic"
}
service httpService on new http:Listener(21033) {
    @http:ResourceConfig {
        path: "/world",
        methods: ["POST"]
    }
    resource function httpResource(http:Caller caller, http:Request req) {
        http:Response resp = new;
        var payload = req.getTextPayload();
        if (payload is error) {
            resp.setPayload("Error in payload");
            resp.statusCode = 500;
        } else {
            resp.setPayload(string `HTTP POST received: {payload}`);
        }

        var err = caller->respond(resp);
        if (err is error) {
            panic err;
        }
    }
    @http:ResourceConfig {
        webSocketUpgrade: {
                upgradePath: "/ws",
                upgradeService: wsService
        }
    }
    resource function upgrader(http:Caller caller, http:Request req) {
        runtime:sleep(30000);
    }
}
service wsService = @http:WebSocketServiceConfig {} service {
};
