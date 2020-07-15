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

http:ClientConfiguration clientEPConfig = {
    cookieConfig: {
        enabled: true
    }
};

@http:WebSocketServiceConfig {
}
service on new http:Listener(21037) {

    resource function onOpen(http:WebSocketCaller wsEp) {
        http:Client httpClient = new("http://localhost:21036/cookie-demo", clientEPConfig);

        http:Request request = new;

        json jsonPart = {
            name: "John",
            password: "p@ssw0rd"
        };
        request.setJsonPayload(jsonPart);

        var loginResp = httpClient->post("/login", request);

        if (loginResp is http:Response) {
            string|error loginMessage = loginResp.getTextPayload();

            if (loginMessage is error) {
                io:println("Login failed", loginMessage);
            } else {
                http:Cookie[] cookies = loginResp.getCookies();
                http:WebSocketClient wsClientEp = new ("ws://localhost:21036/cookie-demo/ws",
                                config = {callbackService: CookieService, cookies: cookies});
                var err = wsClientEp->pushText("Hello World!");
                if (err is error) {
                    io:println(err);
                }
            }
        } else {
            log:printError(loginResp.message());
        }
    }
}

service CookieService = @http:WebSocketServiceConfig {} service {

    resource function onText(http:WebSocketClient conn, string text, boolean finalFrame) {
        io:println(text);
    }
};
