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

@http:WebSocketServiceConfig {
}
service on new http:Listener(21035) {

    resource function onOpen(http:WebSocketCaller wsEp) {
        http:WebSocketFailoverClient wsClientEp = new({ callbackService: failoverClientCallbackService,
            readyOnConnect: false, targetUrls: ["ws://localhost:15300/websocket", "ws://localhost:15200/websocket",
            "ws://localhost:15400/websocket"], failoverIntervalInMillis: 2000});
        checkpanic wsClientEp->ready();
    }
}
