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
import ballerina/io;
import ballerina/grpc;

endpoint grpc:Service ep {
    host:"localhost",
    port:8085,
    secureSocket: {
                      keyStore: {
                                    filePath: "${ballerina.home}/bre/security/ballerinaKeystore.p12",
                                    password: "ballerina"
                                },
                      trustStore: {
                                      filePath: "${ballerina.home}/bre/security/ballerinaTruststore.p12",
                                      password: "ballerina"
                                  },
                      protocol: {
                                    name: "TLSv1.2",
                                    versions: ["TLSv1.2","TLSv1.1"]
                                },
                      ciphers:["TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA"],
                      sslVerifyClient:"require"
                  }
};
@grpc:serviceConfig {generateClientConnector: false}
service<grpc:Listener> HelloWorld bind ep {
    hello (endpoint client, string name) {
        io:println("name: " + name);
        string message = "Hello " + name;
        grpc:ConnectorError err = client -> send(message);
        io:println("Server send response : " + message);
        if (err != ()) {
            io:println("Error at helloWorld : " + err.message);
        }
        _ = client -> complete();
    }
}
