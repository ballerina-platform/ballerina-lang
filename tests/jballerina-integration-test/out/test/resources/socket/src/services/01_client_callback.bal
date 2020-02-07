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
import ballerina/socket;

listener http:Listener echoEP = new(58291);

@http:ServiceConfig { basePath: "/echo" }
service echo on echoEP {

    @http:ResourceConfig {
        methods: ["POST"],
        path: "/"
    }
    resource function echo1(http:Caller caller, http:Request req) {
        socket:Client socketClient = new({ host: "localhost", port: 54387, callbackService: ClientService });
        var payload = req.getTextPayload();
        http:Response resp = new;
        if (payload is string) {
            byte[] payloadByte = payload.toBytes();
            var writeResult = socketClient->write(payloadByte);
            if (writeResult is int) {
                io:println("Number of bytes written: ", writeResult);
                checkpanic caller->accepted();
            } else {
                io:println("Write error!!!");
                error writeError = writeResult;
                string errMsg = <string>writeError.detail()["message"];
                resp.statusCode = 500;
                resp.setPayload(errMsg);
                var responseError = caller->respond(resp);
                if (responseError is error) {
                    error err = responseError;
                    io:println("Error sending response: ", err.detail()["message"]);
                }
            }
        } else {
            error err = payload;
            string errMsg = <string>err.detail()["message"];
            resp.statusCode = 500;
            resp.setPayload(<@untainted> errMsg);
            var responseError = caller->respond(resp);
            if (responseError is error) {
                error responseErr = responseError;
                io:println("Error sending response: ", responseErr.detail()["message"]);
            }
        }
    }
}

service ClientService = service {

    resource function onConnect(socket:Caller caller) {
        io:println("connect: ", caller.remotePort);
    }

    resource function onReadReady(socket:Caller caller) {
        io:println("New content received for callback");
        var result = caller->read();
        if (result is [byte[], int]) {
            var [content, length] = result;
            if (length > 0) {
                var str = <@untainted> getString(content);
                if (str is string) {
                    io:println(<@untainted>str);
                } else {
                    error e = str;
                    io:println(e.detail()["message"]);
                }
                var closeResult = caller->close();
                if (closeResult is error) {
                    error closeResultError = closeResult;
                    io:println(closeResultError.detail()["message"]);
                } else {
                    io:println("Client connection closed successfully.");
                }
            } else {
                io:println("Client close: ", caller.remotePort);
            }
        } else {
            io:println(<error> result);
        }
    }

    resource function onError(socket:Caller caller, error er) {
        error e = er;
        io:println(e.detail()["message"]);
    }
};

function getString(byte[] content) returns @tainted string|io:Error {
    io:ReadableByteChannel byteChannel = check io:createReadableChannel(content);
    io:ReadableCharacterChannel characterChannel = new io:ReadableCharacterChannel(byteChannel, "UTF-8");
    return check characterChannel.read(15);
}
