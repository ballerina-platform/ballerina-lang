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
# + introspectionClient - Introspection client endpoint
# + tokenTypeHint - A hint about the type of the token submitted for introspection
public type InboundOAuth2Provider object {

    *auth:InboundAuthProvider;

    public http:Client introspectionClient;
    public string? tokenTypeHint;
    cache:Cache? inboundOAuth2Cache;
    int defaultTokenExpTimeInSeconds;

    # Provides authentication based on the provided introspection configurations.
    #
    # + config - OAuth2 introspection server configurations
    public function __init(IntrospectionServerConfig config) {
        self.tokenTypeHint = config?.tokenTypeHint;
        self.introspectionClient = new(config.url, config.clientConfig);
        self.inboundOAuth2Cache = config?.oauth2Cache;
        self.defaultTokenExpTimeInSeconds = config.defaultTokenExpTimeInSeconds;
    }

# Authenticates the provider OAuth2 tokens with an introspection endpoint.
# ```ballerina
# boolean|auth:Error result = inboundOAuth2Provider.authenticate("<credential>");
# ```
#
# + credential - OAuth2 token to be authenticated
# + return - `true` if authentication is successful, `false` otherwise, or else an `auth:Error` if an error occurred
    public function authenticate(string credential) returns boolean|auth:Error {
        if (credential == "") {
            return false;
        }

        cache:Cache? oauth2Cache = self.inboundOAuth2Cache;
        if (oauth2Cache is cache:Cache && oauth2Cache.hasKey(credential)) {
            map<json>? claims = authenticateFromCache(oauth2Cache, credential);
            if (claims is map<json>) {
                auth:setAuthenticationContext("oauth2", credential);
                auth:setPrincipal(claims["username"] is string ? <string>claims["username"] : (),
                                  claims["username"] is string ? <string>claims["username"] : (),
                                  getScopes(claims["scope"] is string ? <string>claims["scope"] : ""), claims);
                return true;
            }
        }

        // Build the request to be send to the introspection endpoint.
        // Refer: https://tools.ietf.org/html/rfc7662#section-2.1
        http:Request req = new;
        string textPayload = "token=" + credential;
        string? tokenTypeHint = self.tokenTypeHint;
        if (tokenTypeHint is string) {
            textPayload += "&token_type_hint=" + tokenTypeHint;
        }
        req.setTextPayload(textPayload, mime:APPLICATION_FORM_URLENCODED);
        http:Response|http:ClientError response = self.introspectionClient->post("", req);
        if (response is http:Response) {
            json|error result = response.getJsonPayload();
            if (result is error) {
                return <@untainted> prepareAuthError(result.reason(), result);
            }

            json payload = <json>result;
            boolean active = <boolean>payload.active;
            if (active) {
                map<json>|error payloadMap = map<json>.constructFrom(payload);
                if (payloadMap is error) {
                    return <@untainted> prepareAuthError(payloadMap.reason(), payloadMap);
                }

                map<json> claims = <map<json>>payloadMap;
                if (oauth2Cache is cache:Cache) {
                    addToAuthenticationCache(oauth2Cache, credential, claims, self.defaultTokenExpTimeInSeconds);
                }
                auth:setAuthenticationContext("oauth2", credential);
                auth:setPrincipal(claims["username"] is string ? <string>claims["username"] : (),
                                  claims["username"] is string ? <string>claims["username"] : (),
                                  getScopes(claims["scope"] is string ? <string>claims["scope"] : ""), claims);
                return true;
            }
            return false;
        } else {
            return prepareAuthError("Failed to call the introspection endpoint.", response);
        }
    }
};

function addToAuthenticationCache(cache:Cache oauth2Cache, string token, map<json> claims,
                                  int defaultTokenExpTimeInSeconds) {
    cache:Error? result;
    if (claims["exp"] is int) {
        result = oauth2Cache.put(token, claims);
    } else {
        // If the `exp` parameter is not set by the introspection response, use the cache default expiry by
        // the `defaultTokenExpTimeInSeconds`. Then, the cached value will be removed when retrieving.
        result = oauth2Cache.put(token, claims, defaultTokenExpTimeInSeconds);
    }
    if (result is cache:Error) {
        log:printDebug(function() returns string {
            return "Failed to add JWT to the cache";
        });
        return;
    }
    log:printDebug(function() returns string {
        return "OAuth2 token added to the cache. Claims: " + claims.toString();
    });
}

function authenticateFromCache(cache:Cache oauth2Cache, string token) returns map<json>? {
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
    map<json> claims = <map<json>>cachedValue;
    int? expTime = claims["exp"] is int ? <int> claims["exp"] : ();
    // The `expTime` can be `()`. This means that the `defaultTokenExpTimeInSeconds` is not exceeded yet.
    // Hence, the token is still valid. If the `expTime` is given in int, convert this to the current time and
    // check if the expiry time is exceeded.
    if (expTime is () || expTime > (time:currentTime().time / 1000)) {
        log:printDebug(function() returns string {
            return "OAuth2 token validated from the cache. Claims: " + claims.toString();
        });
        return claims;
    } else {
        cache:Error? result = oauth2Cache.invalidate(token);
        if (result is cache:Error) {
            log:printDebug(function() returns string {
                return "Failed to invalidate OAuth2 token from the cache. Claims: " + claims.toString();
            });
        }
    }
}

# Reads the scope(s) for the user with the given username.
#
# + scopes - Set of scopes seperated with a space
# + return - Array of groups for the user denoted by the username
public function getScopes(string scopes) returns string[] {
    string scopeVal = scopes.trim();
    if (scopeVal == "") {
        return [];
    }
    return stringutils:split(scopeVal, " ");
}

# Represents introspection server onfigurations.
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

# Represents cached OAuth2 information.
#
# + username - Username of the OAuth2 validated user
# + scopes - Scopes of the OAuth2 validated user
# # Deprecated
# This record is deprecated and it was used for OAuth2 caching. With the new cache API v2.0.0 this record no longer
# used and will be removed in next major version.
@deprecated
public type InboundOAuth2CacheEntry record {|
    string username;
    string scopes;
|};
