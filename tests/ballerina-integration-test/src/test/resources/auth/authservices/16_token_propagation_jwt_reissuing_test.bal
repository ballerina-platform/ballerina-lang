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

import ballerina/http;

http:AuthProvider basicAuthProvider16 = {
    scheme: http:JWT_AUTH,
    config: {
        issuer: "example1",
        audience: ["ballerina"],
        certificateAlias: "ballerina",
        trustStore: {
            path: "${ballerina.home}/bre/security/ballerinaTruststore.p12",
            password: "ballerina"
        }
    }
};

listener http:Listener listener16_1 = new(9105, config = {
    authProviders: [basicAuthProvider16],
    secureSocket: {
        keyStore: {
            path: "${ballerina.home}/bre/security/ballerinaKeystore.p12",
            password: "ballerina"
        }
    }
});

http:Client nyseEP16 = new("https://localhost:9106", config = {
    auth: {
        scheme: http:JWT_AUTH,
        config: {
            inferredJwtIssuerConfig: {
                issuer: "example2",
                audience: ["ballerina"],
                keyAlias: "ballerina",
                keyPassword: "ballerina",
                keyStore: {
                    path: "${ballerina.home}/bre/security/ballerinaKeystore.p12",
                    password: "ballerina"
                }
            }
        }
    }
});

@http:ServiceConfig { basePath: "/passthrough" }
service passthroughService16 on listener16_1 {

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/"
    }
    resource function passthrough(http:Caller caller, http:Request clientRequest) {
        var response = nyseEP16->get("/nyseStock/stocks", message = untaint clientRequest);
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

http:AuthProvider jwtAuthProvider16 = {
    scheme: http:JWT_AUTH,
    config: {
        issuer: "example2",
        audience: ["ballerina"],
        certificateAlias: "ballerina",
        trustStore: {
            path: "${ballerina.home}/bre/security/ballerinaTruststore.p12",
            password: "ballerina"
        }
    }
};

listener http:Listener listener16_2 = new(9106, config = {
        authProviders: [jwtAuthProvider16],
        secureSocket: {
            keyStore: {
                path: "${ballerina.home}/bre/security/ballerinaKeystore.p12",
                password: "ballerina"
            }
        }
    });

@http:ServiceConfig { basePath: "/nyseStock" }
service nyseStockQuote16 on listener16_2 {

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/stocks"
    }
    resource function stocks(http:Caller caller, http:Request clientRequest) {
        json payload = { "exchange": "nyse", "name": "IBM", "value": "127.50" };
        checkpanic caller->respond(payload);
    }
}
