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
import ballerina/cache;
import ballerina/log;
import ballerina/runtime;
import ballerina/time;

const string SCOPES = "scope";
const string GROUPS = "groups";
const string USERNAME = "name";
const string AUTH_TYPE_JWT = "jwt";

# Represents parsed and cached JWT.
#
# + jwtPayload - Parsed JWT payload
# + expiryTime - Expiry time of the JWT
public type CachedJwt record {|
    JwtPayload jwtPayload;
    int expiryTime;
|};

# Represents JWT validator configurations.
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
    cache:Cache jwtCache = new(capacity = 1000);
|};

# Represents a JWT Authenticator.
#
# + jwtAuthProviderConfig - JWT auth provider configurations
public type JWTAuthProvider object {

    *auth:AuthProvider;

    public JWTAuthProviderConfig jwtAuthProviderConfig;

    # Provides authentication based on the provided jwt token.
    #
    # + jwtAuthProviderConfig - JWT authentication provider configurations
    public function __init(JWTAuthProviderConfig jwtAuthProviderConfig) {
        self.jwtAuthProviderConfig = jwtAuthProviderConfig;
    }

    # Authenticate with a jwt token.
    #
    # + credential - Jwt token extracted from the authentication header
    # + return - `true` if authentication is successful, othewise `false` or `error` occurred during jwt validation
    public function authenticate(string credential) returns boolean|error {
        string[] jwtComponents = credential.split("\\.");
        if (jwtComponents.length() != 3) {
            return false;
        }

        if (self.jwtAuthProviderConfig.jwtCache.hasKey(credential)) {
            var payload = authenticateFromCache(self.jwtAuthProviderConfig, credential);
            if (payload is JwtPayload) {
                setAuthenticationContext(payload, credential);
                return true;
            } else {
                return false;
            }
        }

        JWTValidatorConfig jwtValidatorConfig = populateJWTValidatorConfig(self.jwtAuthProviderConfig);
        var payload = validateJwt(credential, jwtValidatorConfig);
        if (payload is JwtPayload) {
            setAuthenticationContext(payload, credential);
            addToAuthenticationCache(self.jwtAuthProviderConfig, credential, payload.exp, payload);
            return true;
        } else {
            return payload;
        }
    }
};

function populateJWTValidatorConfig(JWTAuthProviderConfig jwtAuthProviderConfig) returns JWTValidatorConfig {
    JWTValidatorConfig jwtValidatorConfig = { clockSkew: jwtAuthProviderConfig.clockSkew };
    var issuer = jwtAuthProviderConfig["issuer"];
    if (issuer is string) {
        jwtValidatorConfig.issuer = issuer;
    }
    var audience = jwtAuthProviderConfig["audience"];
    if (audience is string[]) {
        jwtValidatorConfig.audience = audience;
    }
    var trustStore = jwtAuthProviderConfig["trustStore"];
    if (trustStore is crypto:TrustStore) {
        jwtValidatorConfig.trustStore = trustStore;
    }
    var certificateAlias = jwtAuthProviderConfig["certificateAlias"];
    if (certificateAlias is string) {
        jwtValidatorConfig.certificateAlias = certificateAlias;
    }
    var validateCertificateConfig = jwtAuthProviderConfig["validateCertificate"];
    if (validateCertificateConfig is boolean) {
        jwtValidatorConfig.validateCertificate = validateCertificateConfig;
    }
    return jwtValidatorConfig;
}

function authenticateFromCache(JWTAuthProviderConfig jwtAuthProviderConfig, string jwtToken) returns JwtPayload? {
    var cachedJwt = trap <CachedJwt>jwtAuthProviderConfig.jwtCache.get(jwtToken);
    if (cachedJwt is CachedJwt) {
        // convert to current time and check the expiry time
        if (cachedJwt.expiryTime > (time:currentTime().time / 1000)) {
            JwtPayload payload = cachedJwt.jwtPayload;
            log:printDebug(function() returns string {
                return "Authenticate user :" + payload.sub + " from cache";
            });
            return payload;
        } else {
            jwtAuthProviderConfig.jwtCache.remove(jwtToken);
        }
    }
}

function addToAuthenticationCache(JWTAuthProviderConfig jwtAuthProviderConfig, string jwtToken, int exp, JwtPayload payload) {
    CachedJwt cachedJwt = {jwtPayload : payload, expiryTime : exp};
    jwtAuthProviderConfig.jwtCache.put(jwtToken, cachedJwt);
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
