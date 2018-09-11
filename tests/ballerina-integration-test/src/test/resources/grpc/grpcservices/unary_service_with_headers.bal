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
// This is server implementation for unary blocking/unblocking scenario
import ballerina/io;
import ballerina/grpc;

// Server endpoint configuration
endpoint grpc:Listener ep101 {
    host:"localhost",
    port:9101
};

service HelloWorld101 bind ep101 {
    hello(endpoint caller, string name, grpc:Headers headers) {
        io:println("name: " + name);
        string message = "Hello " + name;
        if (!headers.exists("x-id")) {
            error? err = caller->sendError(grpc:ABORTED, "x-id header is missing");
        } else {
            io:println("Request Header: " + (headers.get("x-id") ?: ""));
            headers.removeAll();
            headers.addEntry("x-id", "1234567890");
            headers.addEntry("x-id", "2233445677");
            io:print("Response Headers: ");
            io:println(headers.getAll("x-id"));
        }
        error? err = caller->send(message, headers = headers);
        string msg = "Server send response : " + message;
        io:println(err.message but { () => ("Server send response : " + message) });
        _ = caller->complete();
    }
}
