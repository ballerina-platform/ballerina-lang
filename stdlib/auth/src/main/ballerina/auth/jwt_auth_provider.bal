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

import ballerina/cache;
import ballerina/internal;
import ballerina/log;
import ballerina/runtime;
import ballerina/time;

# Represents a JWT Authenticator
#
public type JWTAuthProvider object {

    public JWTAuthProviderConfig jwtAuthProviderConfig;

    # Provides authentication based on the provided jwt token
    #
    # + jwtAuthProviderConfig - JWT authentication provider configurations
    public function __init(JWTAuthProviderConfig jwtAuthProviderConfig) {
        self.jwtAuthProviderConfig = jwtAuthProviderConfig;
    }

    # Authenticate with a jwt token
    #
    # + jwtToken - Jwt token extracted from the authentication header
    # + return - true if authentication is successful, false otherwise.
    #            If an error occur during authentication, the error will be returned.
    public function authenticate(string jwtToken) returns boolean|error {
        if (self.jwtAuthProviderConfig.jwtCache.hasKey(jwtToken)) {
            var payload = self.authenticateFromCache(jwtToken);
            if (payload is JwtPayload) {
                self.setAuthenticationContext(payload, jwtToken);
                return true;
            } else {
                return false;
            }
        }

        var payload = validateJwt(jwtToken, self.jwtAuthProviderConfig);
        if (payload is JwtPayload) {
            self.setAuthenticationContext(payload, jwtToken);
            self.addToAuthenticationCache(jwtToken, payload.exp, payload);
            return true;
        } else {
            return payload;
        }
    }

    function authenticateFromCache(string jwtToken) returns JwtPayload|() {
        var cachedJwt = trap <CachedJwt>self.jwtAuthProviderConfig.jwtCache.get(jwtToken);
        if (cachedJwt is CachedJwt) {
            // convert to current time and check the expiry time
            if (cachedJwt.expiryTime > (time:currentTime().time / 1000)) {
                JwtPayload payload = cachedJwt.jwtPayload;
                log:printDebug(function() returns string {
                    return "Authenticate user :" + payload.sub + " from cache";
                });
                return payload;
            }
        }
        return ();
    }

    function addToAuthenticationCache(string jwtToken, int exp, JwtPayload payload) {
        CachedJwt cachedJwt = {jwtPayload : payload, expiryTime : exp};
        self.jwtAuthProviderConfig.jwtCache.put(jwtToken, cachedJwt);
        log:printDebug(function() returns string {
            return "Add authenticated user :" + payload.sub + " to the cache";
        });
    }

    function setAuthenticationContext(JwtPayload jwtPayload, string jwtToken) {
        runtime:Principal principal = runtime:getInvocationContext().principal;
        principal.userId = jwtPayload.iss + ":" + jwtPayload.sub;
        // By default set sub as username.
        principal.username = jwtPayload.sub;
        principal.claims = jwtPayload.customClaims;
        if (jwtPayload.customClaims.hasKey(SCOPES)) {
            var scopeString = jwtPayload.customClaims[SCOPES];
            if (scopeString is string) {
                principal.scopes = scopeString.split(" ");
            }
        }
        if (jwtPayload.customClaims.hasKey(USERNAME)) {
            var name = jwtPayload.customClaims[USERNAME];
            if (name is string) {
                principal.username = name;
            }
        }
        runtime:AuthenticationContext authenticationContext = runtime:getInvocationContext().authenticationContext;
        authenticationContext.scheme = AUTH_TYPE_JWT;
        authenticationContext.authToken = jwtToken;
    }

};

const string SCOPES = "scope";
const string GROUPS = "groups";
const string USERNAME = "name";
const string AUTH_TYPE_JWT = "jwt";

# Represents JWT validator configurations
#
# + issuer - Identifier of the token issuer
# + audience - Identifier of the token recipients
# + clockSkew - Time in seconds to mitigate clock skew
# + trustStore - Trust store used for signature verification
# + certificateAlias - Token signed key alias
# + validateCertificate - Validate public key certificate notBefore and notAfter periods
# + jwtCache - Cache used to store parsed JWT information as CachedJwt
public type JWTAuthProviderConfig record {|
    string issuer?;
    string[] audience?;
    int clockSkew = 0;
    crypto:TrustStore trustStore?;
    string certificateAlias?;
    boolean validateCertificate?;
    cache:Cache jwtCache = new;
|};

public type CachedJwt record {|
    JwtPayload jwtPayload;
    int expiryTime;
|};
