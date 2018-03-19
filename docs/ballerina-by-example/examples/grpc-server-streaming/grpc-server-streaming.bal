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
import ballerina.io;
import ballerina.log;
import ballerina.net.grpc;

endpoint grpc:Service ep {
  host:"localhost",
  port:9090
};

@grpc:serviceConfig {generateClientConnector:false}
service<grpc:Endpoint> helloWorld bind ep {

    @grpc:resourceConfig {streaming:true}
    lotsOfReplies (endpoint client, string name) {
        log:printInfo("Server received hello from " + name);
        string[] greets = ["Hi", "Hey", "GM"];
        foreach greet in greets {
            log:printInfo("send reply: " + greet + " " + name);
            grpc:ConnectorError err = client -> send(greet + " " + name);
            if (err != null) {
                io:println("Error at lotsOfReplies : " + err.message);
            }
        }
        // Once all messages are sent, server send complete message to notify the client, I’m done.
        _ = client -> complete();
        log:printInfo("send all responses sucessfully.");
    }

   @grpc:resourceConfig {streaming:true}
    lotsOfByes (endpoint client, string name) {
        log:printInfo("Server received hello from " + name);
        string[] greets = ["Hi", "Hey", "GM"];
        foreach greet in greets {
            log:printInfo("send reply: " + greet + " " + name);
            grpc:ConnectorError err = client -> send(greet + " " + name);
            if (err != null) {
                io:println("Error at lotsOfReplies : " + err.message);
            }
        }
        // Once all messages are sent, server send complete message to notify the client, I’m done.
        _ = client -> complete();
        log:printInfo("send all responses sucessfully.");
    }
}
