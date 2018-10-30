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
// This is server implementation for client streaming scenario
import ballerina/grpc;
import ballerina/io;

// Server endpoint configuration
endpoint grpc:Listener ep7 {
    host:"localhost",
    port:9096
};

@grpc:ServiceConfig {name:"lotsOfGreetings",
    clientStreaming:true}
service HelloWorld7 bind ep7 {
    onOpen(endpoint caller) {
        io:println("connected sucessfully.");
    }

    onMessage(endpoint caller, string name) {
        io:println("greet received: " + name);
    }

    onError(endpoint caller, error err) {
        io:println("Something unexpected happens at server : " + err.reason());
    }

    onComplete(endpoint caller) {
        io:println("Server Response");
        error? err = caller->send("Ack");
        if (err is error) {
            io:println("Error from Connector: " + err.reason());
        } else {
            io:println("Server send response : Ack");
        }
    }
}
