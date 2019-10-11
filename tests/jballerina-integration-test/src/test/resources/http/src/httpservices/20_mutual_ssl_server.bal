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
import ballerina/http;
import ballerina/io;

http:ListenerConfiguration mutualSslServiceConf = {
    secureSocket: {
        keyStore: {
            path: config:getAsString("keystore"),
            password: "ballerina"
        },
        trustStore: {
            path: config:getAsString("truststore"),
            password: "ballerina"
        },
        protocol: {
            name: "TLS",
            versions: ["TLSv1.1"]
        },
        ciphers:["TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA"],
        sslVerifyClient: "require",
        certValidation: {
            enable: false
        },
        ocspStapling: {
            enable: false
        },
        handshakeTimeoutInSeconds: 20,
        sessionTimeoutInSeconds: 30
    }
};

listener http:Listener echo15 = new(9116, mutualSslServiceConf);

@http:ServiceConfig {
     basePath: "/echo"
}
service helloWorld15 on echo15 {
    @http:ResourceConfig {
        methods: ["GET"],
        path: "/"
    }
    resource function sayHello(http:Caller caller, http:Request req) {

        http:Response res = new;
        if (req.mutualSslHandshake["status"] == "passed") {
            res.setTextPayload("hello world");
        }
        checkpanic caller->respond(res);
        io:println("successful");
    }
}

listener http:Listener echoDummy15 = new(9117);

@http:ServiceConfig {
    basePath: "/echoDummy"
}
service echoDummyService15 on echoDummy15 {
    @http:ResourceConfig {
        methods: ["POST"],
        path: "/"
    }
    resource function sayHello (http:Caller caller, http:Request req) {
        http:Response res = new;
        res.setTextPayload("hello world");
        checkpanic caller->respond(res);
    }
}
