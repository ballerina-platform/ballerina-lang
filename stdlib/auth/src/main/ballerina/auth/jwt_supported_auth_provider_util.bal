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
import ballerina/crypto;
import ballerina/internal;
import ballerina/log;
import ballerina/runtime;
import ballerina/system;
import ballerina/time;

# Represents authentication provider configurations that supports generating JWT for client interactions.
#
# + issuer - Expected JWT token issuer
# + audience - Expected JWT token audience
# + expTime - Expiry time for newly issued JWT tokens
# + keyAlias - Key alias for signing newly issued JWT tokens
# + keyPassword - Key password for signing newly issued JWT tokens
# + keyStoreFilePath - Path to the key-store file containing signing key
# + keyStorePassword - Password of the key-store file containing signing key
# + signingAlg - Signing algorithm for signing newly issued JWT tokens
public type InferredJwtAuthProviderConfig record {
    string issuer;
    string audience;
    int expTime;
    string keyAlias;
    string keyPassword;
    string keyStoreFilePath;
    string keyStorePassword;
    string signingAlg;
    !...
};

# Sets the jwt access token to the AuthContext
#
# + username - user name
# + authConfig - authentication provider configurations that supports generating JWT for client interactions
function setAuthToken(string username, InferredJwtAuthProviderConfig authConfig) {
    internal:JwtHeader header = createHeader(authConfig);
    internal:JwtPayload payload = createPayload(username, authConfig);
    internal:JWTIssuerConfig config = createJWTIssueConfig(authConfig);
    match internal:issue(header, payload, config) {
        string token => {
            runtime:AuthContext authContext = runtime:getInvocationContext().authContext;
            authContext.scheme = "jwt";
            authContext.authToken = token;
        }
        error err => {
            // Error issuing token.
        }
    }
}

function createHeader(InferredJwtAuthProviderConfig authConfig) returns (internal:JwtHeader) {
    internal:JwtHeader header = { alg: authConfig.signingAlg, typ: "JWT" };
    return header;
}

function createPayload(string username, InferredJwtAuthProviderConfig authConfig) returns (internal:JwtPayload) {
    string audList = authConfig.audience;
    string[] audience = audList.split(" ");
    internal:JwtPayload payload = {
        sub: username,
        iss: authConfig.issuer,
        exp: time:currentTime().time / 1000 + authConfig.expTime,
        iat: time:currentTime().time / 1000,
        nbf: time:currentTime().time / 1000,
        jti: system:uuid(),
        aud: audience
    };
    return payload;
}

function createJWTIssueConfig(InferredJwtAuthProviderConfig authConfig) returns (internal:JWTIssuerConfig) {
    internal:JWTIssuerConfig config = {
        keyAlias: authConfig.keyAlias,
        keyPassword: authConfig.keyPassword,
        keyStoreFilePath: authConfig.keyStoreFilePath,
        keyStorePassword: authConfig.keyStorePassword
    };
    return config;
}
