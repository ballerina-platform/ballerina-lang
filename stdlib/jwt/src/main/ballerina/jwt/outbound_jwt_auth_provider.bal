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

import ballerina/auth;
import ballerina/runtime;
import ballerina/system;
import ballerina/time;

# Represents outbound JWT authenticator.
#
# + outboundJwtAuthConfig - Outbound JWT auth provider configurations
public type OutboundJWTAuthProvider object {

    *auth:OutboundAuthProvider;

    public OutboundJWTAuthConfig outboundJwtAuthConfig;

    # Provides authentication based on the provided jwt configuration.
    #
    # + outboundJwtAuthConfig - Outbound JWT authentication configurations
    public function __init(OutboundJWTAuthConfig outboundJwtAuthConfig) {
        self.outboundJwtAuthConfig = outboundJwtAuthConfig;
    }

    # Generate token for JWT authentication.
    #
    # + return - Generated token or `error` if an error occurred during the JWT issuing or validation
    public function generateToken() returns string|error {
        string authToken = EMPTY_STRING;
        var jwtIssuerConfig = self.outboundJwtAuthConfig["inferredJwtIssuerConfig"];
        if (jwtIssuerConfig is InferredJWTIssuerConfig) {
            authToken = check getAuthTokenForJWTAuth(jwtIssuerConfig);
        } else {
            authToken = runtime:getInvocationContext().authenticationContext.authToken;
        }
        if (authToken == EMPTY_STRING) {
            return prepareError("JWT was not used during inbound authentication.
                                Provide InferredJwtIssuerConfig to issue new token.");
        }
        return authToken;
    }
};

# The `outboundJwtAuthConfig` record can be used to configure JWT based authentication used by the HTTP endpoint.
#
# + inferredJwtIssuerConfig - JWT issuer configuration used to issue JWT with specific configuration
public type OutboundJWTAuthConfig record {|
    InferredJWTIssuerConfig inferredJwtIssuerConfig?;
|};

# Process auth token for JWT auth.
#
# + jwtIssuerConfig - JWT issuer configurations
# + return - Auth token or `error` if an error occurred during the JWT issuing or validation
function getAuthTokenForJWTAuth(InferredJWTIssuerConfig jwtIssuerConfig) returns string|error {
    JwtHeader header = { alg: jwtIssuerConfig.signingAlg, typ: "JWT" };
    JwtPayload payload = {
        sub: runtime:getInvocationContext().principal.username,
        iss: jwtIssuerConfig.issuer,
        exp: time:currentTime().time / 1000 + jwtIssuerConfig.expTime,
        iat: time:currentTime().time / 1000,
        nbf: time:currentTime().time / 1000,
        jti: system:uuid(),
        aud: jwtIssuerConfig.audience
    };
    JWTIssuerConfig issuerConfig = {
        keyStore: jwtIssuerConfig.keyStore,
        keyAlias: jwtIssuerConfig.keyAlias,
        keyPassword: jwtIssuerConfig.keyPassword
    };
    // TODO: cache the token per-user per-client and reuse it
    return issueJwt(header, payload, issuerConfig);
}