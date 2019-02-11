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

function oneWayWrite(string msg) {
    socket:Client socketClient = new({host: "localhost", port: 47826});
    byte[] msgByteArray = msg.toByteArray("utf-8");
    var writeResult = socketClient->write(msgByteArray);
    if (writeResult is int) {
        io:println("Number of bytes written: ", writeResult);
    } else {
        panic writeResult;
    }

    var closeResult =  socketClient->close();
    if (closeResult is error) {
        io:println(closeResult.detail().message);
    } else {
        io:println("Client connection closed successfully.");
    }
}

function shutdownWrite(string firstMsg, string secondMsg) returns error? {
    socket:Client socketClient = new({host: "localhost", port: 47826});
    byte[] msgByteArray = firstMsg.toByteArray("utf-8");
    var writeResult = socketClient->write(msgByteArray);
    if (writeResult is int) {
        io:println("Number of bytes written: ", writeResult);
    } else {
        panic writeResult;
    }

    var shutdownResult = socketClient->shutdownWrite();
    if (shutdownResult is error) {
        panic shutdownResult;
    }
    msgByteArray = secondMsg.toByteArray("utf-8");
    writeResult = socketClient->write(msgByteArray);
    if (writeResult is int) {
        io:println("Number of bytes written: ", writeResult);
    } else {
        var closeResult = socketClient->close();
        if (closeResult is error) {
            io:println(closeResult.detail().message);
        } else {
            io:println("Client connection closed successfully.");
        }
        return writeResult;
    }
    return;
}
