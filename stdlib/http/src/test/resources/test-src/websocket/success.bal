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

import ballerina/http;


listener http:Listener wsListener = new http:Listener(9090, { host: "0.0.0.0"});

@http:WebSocketServiceConfig {
    path: "/"
}
service wsService on wsListener {

    resource function onOpen(http:WebSocketCaller caller) {
    }

    resource function onText(http:WebSocketCaller caller, string text, boolean isFinal) {
    }

    resource function onBinary(http:WebSocketCaller caller, byte[] data, boolean isFinal) {
    }

    resource function onClose(http:WebSocketCaller caller, int val, string text) {
    }

    resource function onIdleTimeout(http:WebSocketCaller caller) {
    }

    resource function onPing(http:WebSocketCaller caller, byte[] data) {
    }

    resource function onPong(http:WebSocketCaller caller, byte[] data) {
    }

    resource function onError(http:WebSocketCaller caller, error err) {
    }
}


service onTextJSON on wsListener {

    resource function onText(http:WebSocketCaller caller, json data) {
    }
}

service onTextXML on wsListener {

    resource function onText(http:WebSocketCaller caller, xml data) {
    }
}

service onTextbyteArray on wsListener {

    resource function onText(http:WebSocketCaller caller, byte[] data) {
    }
}

type Person record {|
    int id;
    string name;
|};

service onTextRecord on wsListener {

    resource function onText(http:WebSocketCaller caller, Person data) {
    }
}
