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

service wsClientService = @http:WebSocketServiceConfig {} service {

    resource function onText(http:WebSocketFailoverClient caller, string text) {
    }

    resource function onBinary(http:WebSocketFailoverClient caller, byte[] text, boolean isFinal) {
    }

    resource function onClose(http:WebSocketFailoverClient caller, int val, string text) {
    }

    resource function onIdleTimeout(http:WebSocketFailoverClient caller) {
    }

    resource function onPing(http:WebSocketFailoverClient caller, byte[] data) {
    }

    resource function onPong(http:WebSocketFailoverClient caller, byte[] data) {
    }
};

http:WebSocketFailoverClient wsClient = new({callbackService: wsClientService,
targetUrls: ["ws://echo.websocket.org"] });
