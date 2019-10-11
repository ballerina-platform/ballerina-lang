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

import ballerina/config;
import ballerina/io;
import ballerina/http;

http:ListenerConfiguration strongCipherConfig = {
    secureSocket: {
        keyStore: {
            path: config:getAsString("keystore"),
            password: "ballerina"
        },
        trustStore: {
            path: config:getAsString("truststore"),
            password: "ballerina"
        }
        // Service will start with the strong cipher suites. No need to specify.
    }
};

listener http:Listener strongCipher = new(9226, strongCipherConfig);

@http:ServiceConfig {
    basePath: "/echo"
}
service strongService on strongCipher {

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/"
    }
    resource function sayHello(http:Caller caller, http:Request req) {
        http:Response res = new;
        res.setTextPayload("hello world");
        checkpanic caller->respond(res);
        io:println("successful");
    }
}

http:ListenerConfiguration weakCipherConfig = {
    secureSocket: {
        keyStore: {
            path: "${ballerina.home}/bre/security/ballerinaKeystore.p12",
            password: "ballerina"
        },
        trustStore: {
            path: "${ballerina.home}/bre/security/ballerinaTruststore.p12",
            password: "ballerina"
        },
        ciphers: ["TLS_RSA_WITH_AES_128_CBC_SHA"]
    }
};

listener http:Listener weakCipher = new(9227, weakCipherConfig);

@http:ServiceConfig {
    basePath: "/echo"
}
service weakService on weakCipher {

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/"
    }
    resource function sayHello(http:Caller caller, http:Request req) {
        http:Response res = new;
        res.setTextPayload("hello world");
        checkpanic caller->respond(res);
        io:println("successful");
    }
}
