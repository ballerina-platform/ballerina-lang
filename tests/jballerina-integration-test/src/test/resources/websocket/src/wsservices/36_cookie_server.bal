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
import ballerina/io;
import ballerina/log;

@http:ServiceConfig {
    basePath: "/cookie-demo"
}
service cookieServer on new http:Listener(21036) {
    @http:ResourceConfig {
        methods: ["POST"],
        path: "/login"
    }

    resource function login(http:Caller caller, http:Request req) {
        json|error details = req.getJsonPayload();
        if (details is json) {
            json|error name = details.name;
            json|error password = details.password;

            if (name is json && password is json) {
                if (password == "p@ssw0rd") {

                    http:Cookie cookie = new("username", name.toString());

                    cookie.path = "/";

                    http:Response response = new;
                    response.addCookie(cookie);

                    response.setTextPayload("Login succeeded");
                    var result = caller->respond(response);
                    if (result is error) {
                        log:printError("Failed to respond", result);
                    }
                }
            }
        }
    }

    @http:ResourceConfig {
        webSocketUpgrade: {
            upgradePath: "/ws",
            upgradeService: cookieService
        }
    }
    resource function upgrader(http:Caller caller, http:Request req) {
        http:Cookie[] cookies = req.getCookies();
        http:Cookie[] usernameCookie = cookies.filter(function
                                (http:Cookie cookie) returns boolean {
            return cookie.name == "username";
        });
        if (usernameCookie.length() > 0) {
            string? user = usernameCookie[0].value;
            if (user is string) {
                log:printInfo("WebSocket connection made successfully");
            } else {
                var result = caller->cancelWebSocketUpgrade(401, "Unauthorized request. Please login");
                if (result is error) {
                    log:printError("Failed to cancel the WebSocket upgrade", result);
                }
            }
        } else {
            var result = caller->cancelWebSocketUpgrade(401, "Unauthorized request. Please login");
            if (result is error) {
                log:printError("Failed to cancel the WebSocket upgrade", result);
            }
        }
    }
}
service cookieService = @http:WebSocketServiceConfig {subProtocols: ["xml", "json"]} service {

    resource function onOpen(http:WebSocketCaller caller) {
        io:println("New WebSocket connection: " + caller.getConnectionId());
    }

    resource function onText(http:WebSocketCaller caller, string text) {
        io:println(text);
    }

    resource function onError(http:WebSocketCaller caller, error err) {
        log:printError("Error occurred ", err);
    }
};
