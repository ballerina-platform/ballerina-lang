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

//import ballerina/crypto;
//import ballerina/http;
//import ballerina/jwt;
//
//function testCanHandleHttpJwtAuthWithoutHeader() returns boolean {
//    http:BearerAuthHeaderAuthnHandler handler = new(createJwtAuthProvider("ballerina/security/ballerinaTruststore.p12"));
//    http:Request request = createRequest();
//    string authHeaderValue = "Basic xxxxxx";
//    request.setHeader("Authorization", authHeaderValue);
//    return handler.canHandle(request);
//}
//
//function testCanHandleHttpJwtAuth() returns boolean {
//    http:BearerAuthHeaderAuthnHandler handler = new(createJwtAuthProvider("ballerina/security/ballerinaTruststore.p12"));
//    http:Request request = createRequest();
//    string authHeaderValue = "Bearer xxx.yyy.zzz";
//    request.setHeader("Authorization", authHeaderValue);
//    return handler.canHandle(request);
//}
//
//function testHandleHttpJwtAuthFailure() returns boolean|error {
//    http:BearerAuthHeaderAuthnHandler handler = new(createJwtAuthProvider("ballerina/security/ballerinaTruststore.p12"));
//    http:Request request = createRequest();
//    string authHeaderValue = "Bearer xxx.yyy.zzz";
//    request.setHeader("Authorization", authHeaderValue);
//    return handler.handle(request);
//}
//
//function testHandleHttpJwtAuth(string token, string trustStorePath) returns boolean|error {
//    http:BearerAuthHeaderAuthnHandler handler = new(createJwtAuthProvider(trustStorePath));
//    http:Request request = createRequest();
//    string authHeaderValue = "Bearer " + token;
//    request.setHeader("Authorization", authHeaderValue);
//    return handler.handle(request);
//}
//
//function createRequest() returns http:Request {
//    http:Request inRequest = new;
//    inRequest.rawPath = "/helloWorld/sayHello";
//    inRequest.method = "GET";
//    inRequest.httpVersion = "1.1";
//    return inRequest;
//}
//
//function createJwtAuthProvider(string trustStorePath) returns jwt:JWTAuthProvider {
//    crypto:TrustStore trustStore = {
//        path: trustStorePath,
//        password: "ballerina"
//    };
//    jwt:JWTAuthProviderConfig jwtConfig = {
//        issuer: "wso2",
//        audience: ["ballerina"],
//        certificateAlias: "ballerina",
//        trustStore: trustStore
//    };
//    jwt:JWTAuthProvider jwtAuthProvider = new(jwtConfig);
//    return jwtAuthProvider;
//}
//
//function generateJwt(jwt:JwtHeader header, jwt:JwtPayload payload, string keyStorePath) returns string|error {
//    crypto:KeyStore keyStore = { path: keyStorePath, password: "ballerina" };
//    jwt:JWTIssuerConfig issuerConfig = {
//        keyStore: keyStore,
//        keyAlias: "ballerina",
//        keyPassword: "ballerina"
//    };
//    return jwt:issueJwt(header, payload, issuerConfig);
//}
//
//function verifyJwt(string jwt, jwt:JWTValidatorConfig config) returns jwt:JwtPayload|error {
//    return jwt:validateJwt(jwt, config);
//}
