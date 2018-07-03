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

import ballerina/log;
import ballerina/runtime;
import ballerina/time;
import ballerina/config;
import ballerina/crypto;
import ballerina/internal;
import ballerina/system;

public type ConfigJwtAuthProvider object {
    public ConfigJwtAuthProviderConfig configJwtAuthProviderConfig;
    public ConfigAuthStoreProvider configAuthProvider;

    public new(configJwtAuthProviderConfig) {
    }

    public function authenticate(string username, string password) returns boolean {
        boolean isAuthenticated = configAuthProvider.authenticate(username, password);
        if (isAuthenticated){
            setAuthToken(username, configJwtAuthProviderConfig);
        }
        return isAuthenticated;
    }

    public function getScopes(string username) returns string[] {
        return configAuthProvider.getScopes(username);
    }

};

function setAuthToken(string username, ConfigJwtAuthProviderConfig authConfig) {
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

function createHeader(ConfigJwtAuthProviderConfig authConfig) returns (internal:JwtHeader) {
    internal:JwtHeader header = {};
    header.alg = authConfig.signingAlg;
    header.typ = "JWT";
    return header;
}

function createPayload(string username, ConfigJwtAuthProviderConfig authConfig) returns (internal:JwtPayload) {
    internal:JwtPayload payload = {};
    payload.sub = username;
    payload.iss = authConfig.issuer;
    payload.exp = time:currentTime().time / 1000 + authConfig.expTime;
    payload.iat = time:currentTime().time / 1000;
    payload.nbf = time:currentTime().time / 1000;
    payload.jti = system:uuid();
    string audList = authConfig.audience;
    payload.aud = audList.split(" ");
    return payload;
}

function createJWTIssueConfig(ConfigJwtAuthProviderConfig authConfig) returns (internal:JWTIssuerConfig) {
    internal:JWTIssuerConfig config = {};
    config.keyAlias = authConfig.keyAlias;
    config.keyPassword = authConfig.keyPassword;
    config.keyStoreFilePath = authConfig.keyStoreFilePath;
    config.keyStorePassword = authConfig.keyStorePassword;
    return config;
}

public type ConfigJwtAuthProviderConfig record {
    string issuer,
    string audience,
    int expTime,
    string keyAlias,
    string keyPassword,
    string keyStoreFilePath,
    string keyStorePassword,
    string signingAlg,
};
