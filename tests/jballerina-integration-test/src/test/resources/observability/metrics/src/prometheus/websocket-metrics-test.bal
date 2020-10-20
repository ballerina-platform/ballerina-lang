// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import ballerina/http;string ping = "ping";
byte[] pingData = ping.toBytes();

@http:WebSocketServiceConfig {
    path: "/basic/ws",
    subProtocols: ["xml", "json"],
    idleTimeoutInSeconds: 120
}
service basic on new http:Listener(9898) {
    resource function onOpen(http:WebSocketCaller caller) {
    }
    resource function onText(http:WebSocketCaller caller, string text,
                                boolean finalFrame) {
        if (text == "ping") {
            var err = caller->ping(pingData);
            if (err is http:WebSocketError) {
                log:printError("Error sending ping", err);
            }
        } else if (text == "closeMe") {
            error? result = caller->close(statusCode = 1001,
                            reason = "You asked me to close the connection",
                            timeoutInSeconds = 0);
            if (result is http:WebSocketError) {
                log:printError("Error occurred when closing connection", result);
            }
        } else {
            var err = caller->pushText("You said: " + text);
            if (err is http:WebSocketError) {
                log:printError("Error occurred when sending text", err);
            }
        }
    }
    resource function onBinary(http:WebSocketCaller caller, byte[] b) {
        var err = caller->pushBinary(b);
        if (err is http:WebSocketError) {
            log:printError("Error occurred when sending binary", err);
        }
    }
    resource function onPing(http:WebSocketCaller caller, byte[] data) {
        var err = caller->pong(data);
        if (err is http:WebSocketError) {
            log:printError("Error occurred when closing the connection", err);
        }
    }
    resource function onPong(http:WebSocketCaller caller, byte[] data) {
    }
    resource function onIdleTimeout(http:WebSocketCaller caller) {
        var err = caller->close(statusCode = 1001, reason =
                                    "Connection timeout");
        if (err is http:WebSocketError) {
            log:printError("Error occurred when closing the connection", err);
        }
    }
    resource function onError(http:WebSocketCaller caller, error err) {
    }
    resource function onClose(http:WebSocketCaller caller, int statusCode,
                                string reason) {
    }
}
