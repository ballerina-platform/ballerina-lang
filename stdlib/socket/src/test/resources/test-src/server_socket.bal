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

int totalLength = 0;
string errorString = "";

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

service helloServer on new socket:Listener(59153) {

    resource function onConnect(socket:Caller caller) {
        io:println("Join: ", caller.remotePort);
    }

    resource function onReadReady(socket:Caller caller) {
        var result = caller->read(length = 5);
        process(result, caller);
        result = caller->read(length = 4);
        process(result, caller);
        result = caller->read(length = 6);
        process(result, caller);
        string msg = "Hello Client";
        byte[] msgByteArray = msg.toByteArray("utf-8");
        _ = caller->write(msgByteArray);
    }

    resource function onError(socket:Caller caller, error er) {
        io:println(er.detail().message);
    }
}

function getTotalLength() returns int {
    return totalLength;
}

function getString(byte[] content) returns string|error {
    io:ReadableByteChannel byteChannel = io:createReadableChannel(content);
    io:ReadableCharacterChannel characterChannel = new io:ReadableCharacterChannel(byteChannel, "UTF-8");
    return characterChannel.read(50);
}

function process(any result, socket:Caller caller) {
    if (result is (byte[], int)) {
        var (content, length) = result;
        if (length > 0) {
            totalLength = totalLength + untaint length;
        } else {
            io:println("Client close: ", caller.remotePort);
            return;
        }
    } else {
        io:println(result);
    }
}

service BlockingReadServer on new socket:Listener(59154) {

    resource function onConnect(socket:Caller caller) {
        io:println("Join: ", caller.remotePort);
    }

    resource function onReadReady(socket:Caller caller) {
        var result = caller->read(length = 18);
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

service errorServer on new socket:Listener(59155) {

    resource function onConnect(socket:Caller caller) {
        io:println("Join: ", caller.remotePort);
    }

    resource function onReadReady(socket:Caller caller) returns error? {
        var result = caller->read();
        if (result is (byte[], int)) {
            var (content, length) = result;
            if (length > 0) {
                error e = error("Error while on read");
                return e;
            } else {
                io:println("Client close: ", caller.remotePort);
            }
        } else {
            io:println(result);
        }
    }

    resource function onError(socket:Caller caller, error er) {
        errorString = untaint string.convert(er.reason());
    }
}

function getError() returns string {
    return errorString;
}
