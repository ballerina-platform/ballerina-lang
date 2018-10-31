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
import ballerina/io;

endpoint http:WebSocketListener wsCaller {
    host: "0.0.0.0",
    port: 9090
};

@http:WebSocketServiceConfig {
    path: "/"
}
service<http:WebSocketService> wsService bind wsCaller {
    onOpen(endpoint caller) {
    }

    onText(endpoint caller, string text, boolean final) {
    }

    onBinary(endpoint caller, byte[] data, boolean final) {
    }

    onClose(endpoint caller, int val, string text) {
    }

    onIdleTimeout(endpoint caller) {
    }

    onPing(endpoint caller, byte[] data) {
    }

    onPong(endpoint caller, byte[] data) {
    }

    onError(endpoint caller, error err) {
    }
}


service<http:WebSocketService> onTextJSON bind wsCaller {

    onText(endpoint caller, json data) {
    }
}

service<http:WebSocketService> onTextXML bind wsCaller {

    onText(endpoint caller, xml data) {
    }
}

service<http:WebSocketService> onTextbyteArray bind wsCaller {

    onText(endpoint caller, byte[] data) {
    }
}

type Person record {
    int id;
    string name;
    !...
};

service<http:WebSocketService> onTextRecord bind wsCaller {

    onText(endpoint caller, Person data) {
    }
}
