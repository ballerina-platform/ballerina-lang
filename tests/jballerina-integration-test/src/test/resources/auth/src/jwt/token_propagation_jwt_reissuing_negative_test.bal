// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import ballerina/jwt;

jwt:InboundJwtAuthProvider jwtAuthProvider14_1 = new({
    issuer: "example1",
    audience: ["ballerina"],
    signatureConfig: {
        certificateAlias: "ballerina",
        trustStore: {
           path: config:getAsString("truststore"),
           password: "ballerina"
        }
    }
});
http:BearerAuthHandler jwtAuthHandler14_1 = new(jwtAuthProvider14_1);

listener http:Listener listener14_1 = new(20112, {
    auth: {
        authHandlers: [jwtAuthHandler14_1]
    },
    secureSocket: {
        keyStore: {
            path: config:getAsString("keystore"),
            password: "ballerina"
        }
    }
});

jwt:OutboundJwtAuthProvider jwtAuthProvider14_2 = new({
    issuer: "ballerina",
    audience: ["ballerina"],
    keyStoreConfig: {
        keyAlias: "ballerina",
        keyPassword: "ballerina",
        keyStore: {
            path: config:getAsString("keystore"),
            password: "ballerina"
        }
    }
});
http:BearerAuthHandler jwtAuthHandler14_2 = new(jwtAuthProvider14_2);

http:Client nyseEP14 = new("https://localhost:20113", {
    auth: {
        authHandler: jwtAuthHandler14_2
    },
    secureSocket: {
       trustStore: {
           path: config:getAsString("truststore"),
           password: "ballerina"
       }
    }
});

@http:ServiceConfig { basePath: "/passthrough" }
service passthroughService14 on listener14_1 {

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/"
    }
    resource function passthrough(http:Caller caller, http:Request clientRequest) {
        var response = nyseEP14->get("/nyseStock/stocks", <@untainted> clientRequest);
        if (response is http:Response) {
            checkpanic caller->respond(response);
        } else {
            http:Response resp = new;
            json errMsg = { "error": "error occurred while invoking the service: " + response.reason() };
            resp.statusCode = 500;
            resp.setPayload(errMsg);
            checkpanic caller->respond(resp);
        }
    }
}

jwt:InboundJwtAuthProvider jwtAuthProvider14_3 = new({
    issuer: "example2aaaaaaaaaaaaaa",
    audience: ["ballerina"],
    signatureConfig: {
        certificateAlias: "ballerina",
        trustStore: {
           path: config:getAsString("truststore"),
           password: "ballerina"
        }
    }
});
http:BearerAuthHandler jwtAuthHandler14_3 = new(jwtAuthProvider14_3);

listener http:Listener listener14_2 = new(20113, {
        auth: {
            authHandlers: [jwtAuthHandler14_3]
        },
        secureSocket: {
            keyStore: {
                path: config:getAsString("keystore"),
                password: "ballerina"
            }
        }
    });

@http:ServiceConfig { basePath: "/nyseStock" }
service nyseStockQuote14 on listener14_2 {

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/stocks"
    }
    resource function stocks(http:Caller caller, http:Request clientRequest) {
        json payload = { "exchange": "nyse", "name": "IBM", "value": "127.50" };
        checkpanic caller->respond(payload);
    }
}
