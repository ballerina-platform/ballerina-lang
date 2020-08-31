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

@http:WebSocketServiceConfig {
}
service on new http:Listener(21039) {

    resource function onOpen(http:WebSocketCaller wsEp) {
        auth:OutboundBasicAuthProvider outboundBasicAuthProvider = new ({
                username: "isuru",
                password: "1234"
        });

        string token = checkpanic outboundBasicAuthProvider.generateToken();

        http:WebSocketClient wsClient = new ("ws://localhost:21040/auth/ws",
                                                config = {
                                                    callbackService: authenticationService,
                                                    customHeaders: {
                                                        [http:AUTH_HEADER]: string `Basic ${token}`
                                                    }
                                                }
                                            );
        checkpanic wsClient->pushText("Hello World!");
        checkpanic wsClient->close(statusCode = 1000, reason = "Close the connection");

    }
}

service authenticationService = @http:WebSocketServiceConfig {} service {

    resource function onText(http:WebSocketClient conn, string text, boolean finalFrame) {
        io:println(text);
    }

    resource function onError(http:WebSocketClient conn, error err) {
        io:println(err);
    }
};
