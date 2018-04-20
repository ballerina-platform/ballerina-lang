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

endpoint grpc:Listener ep {
    host:"localhost",
    port:8085,
    secureSocket: {
                      keyStore: {
                                    path: "${ballerina.home}/bre/security/ballerinaKeystore.p12",
                                    password: "ballerina"
                                },
                      trustStore: {
                                    path: "${ballerina.home}/bre/security/ballerinaTruststore.p12",
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
@grpc:serviceConfig
service HelloWorld bind ep {
    hello(endpoint caller, string name) {
        io:println("name: " + name);
        string message = "Hello " + name;
        error? err = caller->send(message);
        io:println(err.message but { () => ("Server send response : " + message) });
        _ = caller->complete();
    }
}
