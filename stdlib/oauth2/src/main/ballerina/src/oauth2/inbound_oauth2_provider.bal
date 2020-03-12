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

# Represents inbound OAuth2 provider, which calls the introspection server and validate the received credentials.
#
# + introspectionClient - Introspection client endpoint
# + tokenTypeHint - A hint about the type of the token submitted for introspection
public type InboundOAuth2Provider object {

    *auth:InboundAuthProvider;

    public http:Client introspectionClient;
    public string? tokenTypeHint;
    cache:Cache? inboundOAuth2Cache;
    int defaultTokenExpTimeInSeconds;

    # Provides authentication based on the provided introspection configuration.
    #
    # + config - OAuth2 introspection configurations
    public function __init(IntrospectionServerConfig config) {
        self.tokenTypeHint = config?.tokenTypeHint;
        self.introspectionClient = new(config.url, config.clientConfig);
        self.inboundOAuth2Cache = config?.oauth2Cache;
        self.defaultTokenExpTimeInSeconds = config.defaultTokenExpTimeInSeconds;
    }

    # Attempts to authenticate with credential.
    #
    # + credential - Credential to be authenticated
    # + return - `true` if authentication is successful, otherwise `false` or `auth:Error` if an error occurred
    public function authenticate(string credential) returns boolean|auth:Error {
        if (credential == "") {
            return false;
        }

        cache:Cache? oauth2Cache = self.inboundOAuth2Cache;
        if (oauth2Cache is cache:Cache && oauth2Cache.hasKey(credential)) {
            InboundOAuth2CacheEntry? oauth2CacheEntry = authenticateFromCache(oauth2Cache, credential);
            if (oauth2CacheEntry is InboundOAuth2CacheEntry) {
                auth:setAuthenticationContext("oauth2", credential);
                auth:setPrincipal(oauth2CacheEntry.username, oauth2CacheEntry.username,
                                  getScopes(oauth2CacheEntry.scopes));
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
                string? username = ();
                string? scopes = ();
                int exp;

                if (payload.username is string) {
                    username = <@untainted> <string>payload.username;
                }
                if (payload.scope is string) {
                    scopes = <@untainted> <string>payload.scope;
                }
                if (payload.exp is int) {
                    exp = <@untainted> <int>payload.exp;
                } else {
                    exp = self.defaultTokenExpTimeInSeconds +  (time:currentTime().time / 1000);
                }

                if (oauth2Cache is cache:Cache) {
                    addToAuthenticationCache(oauth2Cache, credential, username, scopes, exp);
                }
                auth:setAuthenticationContext("oauth2", credential);
                auth:setPrincipal(username, username, getScopes(scopes ?: ""));
                return true;
            }
            return false;
        } else {
            return prepareAuthError("Failed to call the introspection endpoint.", response);
        }
    }
};

function addToAuthenticationCache(cache:Cache oauth2Cache, string token, string? username, string? scopes, int exp) {
    InboundOAuth2CacheEntry oauth2CacheEntry = {username: username ?: "", scopes: scopes ?: ""};
    cache:Error? result = oauth2Cache.put(token, oauth2CacheEntry, exp);
    if (result is cache:Error) {
        log:printError(function() returns string {
            return "Failed to add JWT to the cache";
        });
        return;
    }
    if (username is string) {
        string user = username;
        log:printDebug(function() returns string {
            return "Add authenticated user: " + user + " to the cache.";
        });
    }
}

function authenticateFromCache(cache:Cache oauth2Cache, string token) returns InboundOAuth2CacheEntry? {
    if (oauth2Cache.hasKey(token)) {
        InboundOAuth2CacheEntry oauth2CacheEntry = <InboundOAuth2CacheEntry>oauth2Cache.get(token);
        log:printDebug(function() returns string {
            return "Get authenticated user: " + oauth2CacheEntry.username + " from the cache.";
        });
        return oauth2CacheEntry;
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
public type InboundOAuth2CacheEntry record {|
    string username;
    string scopes;
|};
