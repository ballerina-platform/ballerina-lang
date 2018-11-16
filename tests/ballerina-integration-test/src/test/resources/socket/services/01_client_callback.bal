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

endpoint http:Listener echoEP {
    port:9090
};

endpoint socket:Client client {
    host: "localhost",
    port: 47826,
    callbackService: ClientService
};

@http:ServiceConfig {
    basePath:"/echo"
}
service<http:Service> echo bind echoEP {

    @http:ResourceConfig {
        methods:["POST"],
        path:"/"
    }
    echo1 (endpoint caller, http:Request req) {
        var payload = req.getTextPayload();
        http:Response resp = new;
        if (payload is string) {
	        byte[] payloadByte = payload.toByteArray("UTF-8");
	        var writeResult = client->write(payloadByte);
            if (writeResult is int) {
                io:println("Number of byte written: ", writeResult);
                _ = caller -> accepted();
            } else if (writeResult is error) {
                string errMsg = <string> writeResult.reason();
                resp.statusCode = 500;
                resp.setPayload(errMsg);
                var responseError = caller->respond(resp);
                if (responseError is error) {
                    io:println("Error sending response: ", responseError.reason());
                }
            }
        } else if (payload is error) {
            string errMsg = <string> payload.detail().message;
            resp.statusCode = 500;
            resp.setPayload(errMsg);
            var responseError = caller->respond(resp);
            if (responseError is error) {
                io:println("Error sending response: ", responseError.reason());
            }
        }
    }
}

service<socket:ClientService> ClientService {

	onConnect(endpoint caller) {
		io:println("connect: ", caller.remotePort);
    }

	onReadReady (endpoint caller, byte[] content) {
        io:println("New content received for callback");
        var str = getString(content);
        if (str is string) {
            io:println(untaint str);
        } else if (str is error) {
            io:println(str.reason());
        }
        var closeResult =  client->close();
        if (closeResult is error) {
            io:println(closeResult.reason());
        } else {
            io:println("Client connection closed successfully.");
        }
    }

    onClose(endpoint caller) {
		io:println("Leave: ", caller.remotePort);
    }

    onError(endpoint caller, error er) {
        io:println(er.reason());
    }
}

function getString(byte[] content) returns string|error {
    io:ReadableByteChannel byteChannel = io:createReadableChannel(content);
    io:ReadableCharacterChannel characterChannel = new io:ReadableCharacterChannel(byteChannel, "UTF-8");
    return characterChannel.read(15);
}
