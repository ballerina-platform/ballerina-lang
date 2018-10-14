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
// This is server implementation for server streaming scenario
import ballerina/io;
import ballerina/grpc;

// Server endpoint configuration
endpoint grpc:Listener ep99 {
    host:"localhost",
    port:9099
};

service HelloWorld45 bind ep99 {

    @grpc:ResourceConfig {streaming:true}
    lotsOfReplies(endpoint caller, string name) {
        io:println("Server received hello from " + name);
        string[] greets = ["Hi", "Hey", "GM"];
        foreach greet in greets {
            error? err = caller->send(greet + " " + name);
            io:println(err.message but { () => ("send reply: " + greet + " " + name) });
        }
        // Once all messages are sent, server send complete message to notify the client, I’m done.
        _ = caller->complete();
        io:println("send all responses sucessfully.");
    }

}
