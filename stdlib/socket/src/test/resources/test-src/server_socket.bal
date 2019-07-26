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
import ballerina/log;
import ballerina/socket;

int totalLength = 0;
string errorString = "";

listener socket:Listener server = new(59152);

service echoServer on server {

    resource function onConnect(socket:Caller caller) {
        log:printInfo("Join: " + caller.remotePort.toString());
    }

    resource function onReadReady(socket:Caller caller) {
        var result = caller->read();
        if (result is [byte[], int]) {
            var [content, length] = result;
            if (length > 0) {
                _ = checkpanic caller->write(content);
                log:printInfo("Server write");
            } else {
                log:printInfo("Client close: " + caller.remotePort.toString());
            }
        } else {
            log:printError("Error on echo server read", <error> result);
        }
    }

    resource function onError(socket:Caller caller, error er) {
        log:printError("Error on echo service", <error> er);
    }
}

service helloServer on new socket:Listener(59153) {

    resource function onConnect(socket:Caller caller) {
        log:printInfo("Join: " + caller.remotePort.toString());
    }

    resource function onReadReady(socket:Caller caller) {
        var result = caller->read(5);
        process(result, caller);
        result = caller->read(4);
        process(result, caller);
        result = caller->read(6);
        process(result, caller);
        string msg = "Hello Client";
        byte[] msgByteArray = msg.toBytes();
        _ = checkpanic caller->write(msgByteArray);
    }

    resource function onError(socket:Caller caller, error er) {
        log:printError("Error on hello server", <error> er);
    }
}

function getTotalLength() returns int {
    return totalLength;
}

function getString(byte[] content) returns @tainted string|io:Error {
    io:ReadableByteChannel byteChannel = check io:createReadableChannel(content);
    io:ReadableCharacterChannel characterChannel = new io:ReadableCharacterChannel(byteChannel, "UTF-8");
    return check characterChannel.read(50);
}

function process(any|error result, socket:Caller caller) {
    if (result is [byte[], int]) {
        var [content, length] = result;
        if (length > 0) {
            totalLength = totalLength + <@untainted> length;
        } else {
            log:printInfo("Client close: " + caller.remotePort.toString());
            return;
        }
    } else if (result is error) {
        log:printError("Error while process data", <error> result);
    }
}

service BlockingReadServer on new socket:Listener(59154) {

    resource function onConnect(socket:Caller caller) {
        log:printInfo("Join: " + caller.remotePort.toString());
    }

    resource function onReadReady(socket:Caller caller) {
        var result = caller->read(length = 18);
        if (result is [byte[], int]) {
            var [content, length] = result;
            if (length > 0) {
                _ = checkpanic caller->write(content);
                log:printInfo("Server write");
            } else {
                log:printInfo("Client close: " + caller.remotePort.toString());
            }
        } else {
            log:printError("Error while read data", <error> result);
        }
    }

    resource function onError(socket:Caller caller, error er) {
        log:printError("Error on blocking read server", <error> er);
    }
}

service errorServer on new socket:Listener(59155) {

    resource function onConnect(socket:Caller caller) {
        log:printInfo("Join: " + caller.remotePort.toString());
    }

    resource function onReadReady(socket:Caller caller) returns error? {
        var result = caller->read();
        if (result is [byte[], int]) {
            var [content, length] = result;
            if (length > 0) {
                error e = error("Error while on read");
                panic e;
            } else {
                log:printInfo("Client close: " + caller.remotePort.toString());
            }
        } else {
            log:printError("Error on error server read", <error> result);
        }
    }

    resource function onError(socket:Caller caller, error er) {
        error e = er;
        errorString = <@untainted> e.reason();
    }
}

function getError() returns string {
    return errorString;
}
