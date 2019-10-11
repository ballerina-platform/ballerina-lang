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

import ballerina/auth;
import ballerina/crypto;
import ballerina/jwt;

function testCreateJwtAuthProvider(string trustStorePath) returns jwt:InboundJwtAuthProvider {
    crypto:TrustStore trustStore = { path: trustStorePath, password: "ballerina" };
    jwt:JwtValidatorConfig jwtConfig = {
        issuer: "wso2",
        audience: "ballerina",
        trustStoreConfig: {
            trustStore: trustStore,
            certificateAlias: "ballerina"
        }
    };
    jwt:InboundJwtAuthProvider jwtAuthProvider = new(jwtConfig);
    return jwtAuthProvider;
}

function testJwtAuthProviderAuthenticationSuccess(string jwtToken, string trustStorePath)
                                                  returns @tainted (boolean|auth:Error) {
    crypto:TrustStore trustStore = { path: trustStorePath, password: "ballerina" };
    jwt:JwtValidatorConfig jwtConfig = {
        issuer: "wso2",
        audience: "ballerina",
        trustStoreConfig: {
            trustStore: trustStore,
            certificateAlias: "ballerina"
        }
    };
    jwt:InboundJwtAuthProvider jwtAuthProvider = new(jwtConfig);
    return jwtAuthProvider.authenticate(jwtToken);
}

function generateJwt(string keyStorePath) returns string|jwt:Error {
    jwt:JwtHeader header = {
        alg: "RS256",
        typ: "JWT"
    };
    jwt:JwtPayload payload = {
        iss: "wso2",
        sub: "John",
        aud: "ballerina",
        exp: 32475251189000
    };
    crypto:KeyStore keyStore = { path: keyStorePath, password: "ballerina" };
    jwt:JwtKeyStoreConfig keyStoreConfig = {
        keyStore: keyStore,
        keyAlias: "ballerina",
        keyPassword: "ballerina"
    };
    return jwt:issueJwt(header, payload, keyStoreConfig);
}

function verifyJwt(string jwt, string trustStorePath) returns @tainted (jwt:JwtPayload|jwt:Error) {
    crypto:TrustStore trustStore = { path: trustStorePath, password: "ballerina" };
    jwt:JwtValidatorConfig validatorConfig = {
        issuer: "wso2",
        audience: "ballerina",
        clockSkewInSeconds: 0,
        trustStoreConfig: {
            trustStore: trustStore,
            certificateAlias: "ballerina"
        }
    };
    return jwt:validateJwt(jwt, validatorConfig);
}
