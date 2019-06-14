// TODO: Resolve with https://github.com/ballerina-platform/ballerina-lang/issues/15487
//// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
////
//// WSO2 Inc. licenses this file to you under the Apache License,
//// Version 2.0 (the "License"); you may not use this file except
//// in compliance with the License.
//// You may obtain a copy of the License at
////
//// http://www.apache.org/licenses/LICENSE-2.0
////
//// Unless required by applicable law or agreed to in writing,
//// software distributed under the License is distributed on an
//// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
//// KIND, either express or implied.  See the License for the
//// specific language governing permissions and limitations
//// under the License.
//
//import ballerina/http;
//import ballerina/jwt;
//
//jwt:JWTAuthProvider jwtAuthProvider12_1 = new({
//    issuer: "example1",
//    audience: ["ballerina"],
//    certificateAlias: "ballerina",
//    trustStore: {
//        path: "${ballerina.home}/bre/security/ballerinaTruststore.p12",
//        password: "ballerina"
//    }
//});
//
//http:BearerAuthHeaderAuthnHandler jwtAuthnHandler12_1 = new(jwtAuthProvider12_1);
//
//listener http:Listener listener12_1 = new(9105, config = {
//    auth: {
//        authnHandlers: [jwtAuthnHandler12_1]
//    },
//    secureSocket: {
//        keyStore: {
//            path: "${ballerina.home}/bre/security/ballerinaKeystore.p12",
//            password: "ballerina"
//        }
//    }
//});
//
//http:Client nyseEP12 = new("https://localhost:9106", config = {
//    auth: {
//        scheme: http:JWT_AUTH
//    }
//});
//
//@http:ServiceConfig { basePath: "/passthrough" }
//service passthroughService12 on listener12_1 {
//
//    @http:ResourceConfig {
//        methods: ["GET"],
//        path: "/"
//    }
//    resource function passthrough(http:Caller caller, http:Request clientRequest) {
//        var response = nyseEP12->get("/nyseStock/stocks", message = untaint clientRequest);
//        if (response is http:Response) {
//            checkpanic caller->respond(response);
//        } else {
//            http:Response resp = new;
//            json errMsg = { "error": "error occurred while invoking the service: " + response.reason() };
//            resp.statusCode = 500;
//            resp.setPayload(errMsg);
//            checkpanic caller->respond(resp);
//        }
//    }
//}
//
//jwt:JWTAuthProvider jwtAuthProvider12_2 = new({
//    issuer: "example1",
//    audience: ["ballerina"],
//    certificateAlias: "ballerina",
//    trustStore: {
//        path: "${ballerina.home}/bre/security/ballerinaTruststore.p12",
//        password: "ballerina"
//    }
//});
//
//http:BearerAuthHeaderAuthnHandler jwtAuthnHandler12_2 = new(jwtAuthProvider12_2);
//
//listener http:Listener listener12_2 = new(9106, config = {
//    auth: {
//        authnHandlers: [jwtAuthnHandler12_2]
//    },
//    secureSocket: {
//        keyStore: {
//            path: "${ballerina.home}/bre/security/ballerinaKeystore.p12",
//            password: "ballerina"
//        }
//    }
//});
//
//@http:ServiceConfig { basePath: "/nyseStock" }
//service nyseStockQuote12 on listener12_2 {
//
//    @http:ResourceConfig {
//        methods: ["GET"],
//        path: "/stocks"
//    }
//    resource function stocks(http:Caller caller, http:Request clientRequest) {
//        json payload = { "exchange": "nyse", "name": "IBM", "value": "127.50" };
//        checkpanic caller->respond(payload);
//    }
//}
