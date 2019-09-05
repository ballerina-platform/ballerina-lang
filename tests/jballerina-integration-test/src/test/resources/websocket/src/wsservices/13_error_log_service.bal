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
import ballerina/io;
import ballerina/http;

@http:WebSocketServiceConfig {
    path: "/error/ws"
}
service errorService on new http:Listener(21013) {
    resource function onOpen(http:WebSocketCaller ep) {
        log:printInfo("connection open");
    }

    resource function onText(http:WebSocketCaller ep, string text) {
        log:printError(string `text received: ${text}`);
        var returnVal = ep->pushText(text);
        if (returnVal is http:WebSocketError) {
             panic <error> returnVal;
        }
    }

    resource function onError(http:WebSocketCaller ep, error err) {
        io:println(err.detail()["message"]);
    }

    resource function onClose(http:WebSocketCaller ep, int statusCode, string reason) {
        log:printError(string `Connection closed with ${statusCode}, ${reason}`);
    }
}
