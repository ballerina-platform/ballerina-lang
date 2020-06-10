// Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import ballerina/http;
import ballerina/log;
import ballerina/mime;
import ballerina/stringutils;
import ballerina/time;

# Represents the inbound OAuth2 provider, which calls the introspection server, validates the received credentials,
# and performs authentication and authorization. The `oauth2:InboundOAuth2Provider` is an implementation of the
# `auth:InboundAuthProvider` interface.
# ```ballerina
# oauth2:IntrospectionServerConfig introspectionServerConfig = {
#     url: "https://localhost:9196/oauth2/token/introspect"
# };
# oauth2:InboundOAuth2Provider inboundOAuth2Provider = new(introspectionServerConfig);
# ```
#
public type InboundOAuth2Provider object {

    *auth:InboundAuthProvider;

    IntrospectionServerConfig introspectionServerConfig;

    # Provides authentication based on the provided introspection configurations.
    #
    # + introspectionServerConfig - OAuth2 introspection server configurations
    public function init(IntrospectionServerConfig introspectionServerConfig) {
        self.introspectionServerConfig = introspectionServerConfig;
    }

# Authenticates the provider OAuth2 tokens with an introspection endpoint.
# ```ballerina
# boolean|auth:Error result = inboundOAuth2Provider.authenticate("<credential>");
# ```
#
# + credential - OAuth2 token to be authenticated
# + return - `true` if authentication is successful, `false` otherwise, or else an `auth:Error` if an error occurred
    public function authenticate(string credential) returns @tainted (boolean|auth:Error) {
        if (credential == "") {
            return false;
        }

        IntrospectionResponse|Error validationResult = validateOAuth2Token(credential, self.introspectionServerConfig);
        if (validationResult is IntrospectionResponse) {
            if (validationResult.active) {
                auth:setAuthenticationContext("oauth2", credential);
                auth:setPrincipal(validationResult?.username, validationResult?.username,
                                  getScopes(validationResult?.scopes));
            }
            return validationResult.active;
        } else {
            return prepareAuthError("OAuth2 validation failed.", validationResult);
        }
    }
};

# Validates the given OAuth2 token by calling the OAuth2 introspection endpoint.
# ```ballerina
# oauth2:IntrospectionResponse|oauth2:Error result = oauth2:validateOAuth2Token(token, introspectionServerConfig);
# ```
#
# + token - OAuth2 token, which needs to be validated
# + config -  OAuth2 introspection server configurations
# + return - OAuth2 introspection server response or else an `oauth2:Error` if token validation fails
public function validateOAuth2Token(string token, IntrospectionServerConfig config)
                                    returns @tainted (IntrospectionResponse|Error) {
    cache:Cache? oauth2Cache = config?.oauth2Cache;
    if (oauth2Cache is cache:Cache && oauth2Cache.hasKey(token)) {
        IntrospectionResponse? response = validateFromCache(oauth2Cache, token);
        if (response is IntrospectionResponse) {
            return response;
        }
    }

    // Builds the request to be sent to the introspection endpoint.
    // For more information, see the
    // [OAuth 2.0 Token Introspection RFC](https://tools.ietf.org/html/rfc7662#section-2.1)
    http:Request req = new;
    string textPayload = "token=" + token;
    string? tokenTypeHint = config?.tokenTypeHint;
    if (tokenTypeHint is string) {
        textPayload += "&token_type_hint=" + tokenTypeHint;
    }
    req.setTextPayload(textPayload, mime:APPLICATION_FORM_URLENCODED);
    http:Client introspectionClient = new(config.url, config.clientConfig);
    http:Response|http:ClientError response = introspectionClient->post("", req);
    if (response is http:Response) {
        json|error result = response.getJsonPayload();
        if (result is error) {
            return <@untainted> prepareError(result.reason(), result);
        }
        IntrospectionResponse introspectionResponse = prepareIntrospectionResponse(<json>result);
        if (introspectionResponse.active) {
            if (oauth2Cache is cache:Cache) {
                addToCache(oauth2Cache, token, introspectionResponse, config.defaultTokenExpTimeInSeconds);
            }
        }
        return introspectionResponse;
    } else {
        return prepareError("Failed to call the introspection endpoint.", response);
    }
}

function prepareIntrospectionResponse(json payload) returns IntrospectionResponse {
    boolean active = <boolean>payload.active;
    IntrospectionResponse introspectionResponse = {
        active: active
    };
    if (active) {
        if (payload.scope is string) {
            introspectionResponse.scopes = <@untainted> <string>payload.scope;
        }
        if (payload.client_id is string) {
            introspectionResponse.clientId = <@untainted> <string>payload.client_id;
        }
        if (payload.username is string) {
            introspectionResponse.username = <@untainted> <string>payload.username;
        }
        if (payload.token_type is string) {
            introspectionResponse.tokenType = <@untainted> <string>payload.token_type;
        }
        if (payload.exp is int) {
            introspectionResponse.exp = <@untainted> <int>payload.exp;
        }
        if (payload.iat is int) {
            introspectionResponse.iat = <@untainted> <int>payload.iat;
        }
        if (payload.nbf is int) {
            introspectionResponse.nbf = <@untainted> <int>payload.nbf;
        }
        if (payload.sub is string) {
            introspectionResponse.sub = <@untainted> <string>payload.sub;
        }
        if (payload.aud is string) {
            introspectionResponse.aud = <@untainted> <string>payload.aud;
        }
        if (payload.iss is string) {
            introspectionResponse.iss = <@untainted> <string>payload.iss;
        }
        if (payload.jti is string) {
            introspectionResponse.jti = <@untainted> <string>payload.jti;
        }
    }
    return introspectionResponse;
}

function addToCache(cache:Cache oauth2Cache, string token, IntrospectionResponse response,
                    int defaultTokenExpTimeInSeconds) {
    cache:Error? result;
    if (response?.exp is int) {
        result = oauth2Cache.put(token, response);
    } else {
        // If the `exp` parameter is not set by the introspection response, use the cache default expiry by
        // the `defaultTokenExpTimeInSeconds`. Then, the cached value will be removed when retrieving.
        result = oauth2Cache.put(token, response, defaultTokenExpTimeInSeconds);
    }
    if (result is cache:Error) {
        log:printDebug(function() returns string {
            return "Failed to add OAuth2 token to the cache. Introspection response: " + response.toString();
        });
        return;
    }
    log:printDebug(function() returns string {
        return "OAuth2 token added to the cache. Introspection response: " + response.toString();
    });
}

function validateFromCache(cache:Cache oauth2Cache, string token) returns IntrospectionResponse? {
    any|cache:Error cachedValue = oauth2Cache.get(token);
    if (cachedValue is ()) {
        // If the cached value is expired (defaultTokenExpTimeInSeconds is passed), it will return `()`.
        log:printDebug(function() returns string {
            return "Failed to validate the token from the cache since the token is expired.";
        });
        return;
    }
    if (cachedValue is cache:Error) {
        log:printDebug(function() returns string {
            return "Failed to validate the token from the cache. Cache error: " + cachedValue.toString();
        });
        return;
    }
    IntrospectionResponse response = <IntrospectionResponse>cachedValue;
    int? expTime = response?.exp;
    // The `expTime` can be `()`. This means that the `defaultTokenExpTimeInSeconds` is not exceeded yet.
    // Hence, the token is still valid. If the `expTime` is given in int, convert this to the current time and
    // check if the expiry time is exceeded.
    if (expTime is () || expTime > (time:currentTime().time / 1000)) {
        log:printDebug(function() returns string {
            return "OAuth2 token validated from the cache. Introspection response: " + response.toString();
        });
        return response;
    } else {
        cache:Error? result = oauth2Cache.invalidate(token);
        if (result is cache:Error) {
            log:printDebug(function() returns string {
                return "Failed to invalidate OAuth2 token from the cache. Introspection response: " + response.toString();
            });
        }
    }
}

# Reads the scope(s) of the user with the given username.
#
# + scopes - Set of scopes seperated with a space
# + return - Array of groups for the user who is denoted by the username
function getScopes(string? scopes) returns string[] {
    if (scopes is ()) {
        return [];
    } else {
        string scopeVal = scopes.trim();
        if (scopeVal == "") {
            return [];
        }
        return stringutils:split(scopeVal, " ");
    }
}

# Represents the introspection server configurations.
#
# + url - URL of the introspection server
# + tokenTypeHint - A hint about the type of the token submitted for introspection
# + oauth2Cache - Cache used to store the OAuth2 token and other related information
# + defaultTokenExpTimeInSeconds - Expiration time of the tokens if introspection response does not contain an `exp` field
# + clientConfig - HTTP client configurations which calls the introspection server
public type IntrospectionServerConfig record {|
    string url;
    string tokenTypeHint?;
    cache:Cache oauth2Cache?;
    int defaultTokenExpTimeInSeconds = 3600;
    http:ClientConfiguration clientConfig = {};
|};

# Represents the introspection server response.
#
# + active - Boolean indicator of whether or not the presented token is currently active
# + scopes - A JSON string containing a space-separated list of scopes associated with this token
# + clientId - Client identifier for the OAuth 2.0 client, which requested this token
# + username - Resource owner who authorized this token
# + tokenType - Type of the token
# + exp - Expiry time (seconds since the Epoch)
# + iat - Time when the token was issued originally (seconds since the Epoch)
# + nbf - Token is not to be used before this time (seconds since the Epoch)
# + sub - Subject of the token
# + aud - Intended audience of the token
# + iss - Issuer of the token
# + jti - String identifier for the token
public type IntrospectionResponse record {|
    boolean active;
    string scopes?;
    string clientId?;
    string username?;
    string tokenType?;
    int exp?;
    int iat?;
    int nbf?;
    string sub?;
    string aud?;
    string iss?;
    string jti?;
|};
