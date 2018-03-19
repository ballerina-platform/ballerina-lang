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
import ballerina.net.grpc;

endpoint grpc:Service ep {
  host:"localhost",
  port:9090
};

@grpc:serviceConfig {rpcEndpoint:"LotsOfGreetings",
                     clientStreaming:true,
		     generateClientConnector:true}
service<grpc:Endpoint> helloWorld bind ep {
    onOpen (endpoint client) {
        io:println("connected sucessfully.");
    }

    onMessage (endpoint client, string name) {
        io:println("greet received: " + name);
    }

    onError (endpoint client, grpc:ServerError err) {
        if (err != null) {
            io:println("Something unexpected happens at server : " + err.message);
        }
    }

    onComplete (endpoint client) {
        io:println("Server Response");
        grpc:ConnectorError err = client -> send("Ack");
        if (err != null) {
            io:println("Error at onComplete send message : " + err.message);
        }
    }
}
