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

import ballerina/config;
import ballerina/http;

service sslEcho on new http:Listener(21022, { secureSocket: {
        keyStore: {
            path: config:getAsString("keystore"),
            password: "ballerina"
        } } }) {

    resource function onText(http:WebSocketCaller caller, string data, boolean finalFrame) {
        var returnVal = caller->pushText(data, finalFrame);
        if (returnVal is http:WebSocketError) {
            panic <error> returnVal;
        }
    }

    resource function onBinary(http:WebSocketCaller caller, byte[] data, boolean finalFrame) {
        var returnVal = caller->pushBinary(data, finalFrame);
        if (returnVal is http:WebSocketError) {
            panic <error> returnVal;
        }
    }
}
