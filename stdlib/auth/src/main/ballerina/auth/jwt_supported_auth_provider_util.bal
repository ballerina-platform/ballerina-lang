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

import ballerina/internal;
import ballerina/runtime;
import ballerina/system;
import ballerina/time;
import ballerina/crypto;

# Represents authentication provider configurations that supports generating JWT for client interactions.
#
# + issuer - Expected JWT token issuer
# + audience - Expected JWT token audience
# + expTime - Expiry time for newly issued JWT tokens
# + keyStore - Keystore containing the signing key
# + keyAlias - Key alias for signing newly issued JWT tokens
# + keyPassword - Key password for signing newly issued JWT tokens
# + signingAlg - Signing algorithm for signing newly issued JWT tokens
    public type InferredJwtAuthProviderConfig record {
    string issuer;
    string audience;
    int expTime = 0;
    crypto:KeyStore keyStore;
    string keyAlias;
    string keyPassword;
    JwtSigningAlgorithm signingAlg;
    !...;
};

# Sets the jwt access token to the AuthenticationContext
#
# + username - user name
# + authConfig - authentication provider configurations that supports generating JWT for client interactions
function setAuthToken(string username, InferredJwtAuthProviderConfig authConfig) {
    JwtHeader header = createHeader(authConfig);
    JwtPayload payload = createPayload(username, authConfig);
    var token = issueJwt(header, payload, authConfig.keyStore, authConfig.keyAlias, authConfig.keyPassword);
    if (token is string) {
        runtime:AuthenticationContext authenticationContext = runtime:getInvocationContext().authenticationContext;
        authenticationContext.scheme = "jwt";
        authenticationContext.authToken = token;
    }
}

function createHeader(InferredJwtAuthProviderConfig authConfig) returns (JwtHeader) {
    JwtHeader header = { alg: authConfig.signingAlg, typ: "JWT" };
    return header;
}

function createPayload(string username, InferredJwtAuthProviderConfig authConfig) returns (JwtPayload) {
    string audList = authConfig.audience;
    string[] audience = audList.split(" ");
    JwtPayload payload = {
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
