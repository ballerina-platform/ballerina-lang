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
import ballerina/log;

service<http:WebSocketService> onTextString bind { port: 9090 } {

    onText(endpoint wsEp, string data, boolean final) {
        _ = wsEp->pushText(data);
    }
}

service<http:WebSocketService> onTextJSON bind { port: 9091 } {

    onText(endpoint wsEp, json data) {
        io:println(data);
        _ = wsEp->pushText(data);
    }
}

service<http:WebSocketService> onTextXML bind { port: 9092 } {

    onText(endpoint wsEp, xml data) {
        _ = wsEp->pushText(data);
    }
}
type Person record {
    int id,
    string name,
    !...
};
service<http:WebSocketService> onTextRecord bind { port: 9093 } {

    onText(endpoint wsEp, Person data) {
        _ = wsEp->pushText(check <json>data);
    }
}

service<http:WebSocketService> onTextByteArray bind { port: 9094 } {

    onText(endpoint wsEp, byte[] data) {
        _ = wsEp->pushText(data);
    }
}
