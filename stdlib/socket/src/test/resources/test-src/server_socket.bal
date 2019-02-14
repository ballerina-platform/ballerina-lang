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
// KIND, either express or implied. See the License for the
// specific language governing permissions and limitations
// under the License.

import ballerina/io;
import ballerina/socket;

listener socket:Listener server = new(59152);

service echoServer on server {

    resource function onConnect(socket:Caller caller) {
        io:println("Join: ", caller.remotePort);
    }

    resource function onReadReady(socket:Caller caller) {
        var result = caller->read();
        if (result is (byte[], int)) {
            var (content, length) = result;
            if (length > 0) {
                _ = caller->write(content);
                io:println("Server write");
            } else {
                io:println("Client close: ", caller.remotePort);
            }
        } else {
            io:println(result);
        }
    }

    resource function onError(socket:Caller caller, error er) {
        io:println(er.detail().message);
    }
}
