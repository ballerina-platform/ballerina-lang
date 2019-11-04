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
# + introspectionServerConfig - Introspection server configuration
# + introspectionClient - Introspection client endpoint
# + tokenTypeHint - A hint about the type of the token submitted for introspection
public type InboundOAuth2Provider object {

    *auth:InboundAuthProvider;

    public IntrospectionServerConfig introspectionServerConfig;
    public http:Client introspectionClient;
    public string? tokenTypeHint;

    public function __init(IntrospectionServerConfig config) {
        self.introspectionServerConfig = config;
        self.tokenTypeHint = config?.tokenTypeHint;
        self.introspectionClient = new(config.url, config.clientConfig);
    }

    # Attempts to authenticate with credential.
    #
    # + credential - Credential to be authenticated
    # + return - `true` if authentication is successful, otherwise `false` or `auth:Error` if an error occurred
    public function authenticate(string credential) returns boolean|auth:Error {
        if (credential == "") {
            return false;
        }

        var oauth2Cache = self.introspectionServerConfig?.oauth2Cache;
        if (oauth2Cache is cache:Cache && oauth2Cache.hasKey(credential)) {
            var cachedOAuth2Info = authenticateFromCache(oauth2Cache, credential);
            if (cachedOAuth2Info is CachedOAuth2Info) {
                auth:setAuthenticationContext("oauth2", credential);
                auth:setPrincipal(cachedOAuth2Info.username, cachedOAuth2Info.username, getScopes(cachedOAuth2Info.scopes));
                return true;
            } else {
                return false;
            }
        }

        // Build the request to be send to the introspection endpoint.
        // Refer: https://tools.ietf.org/html/rfc7662#section-2.1
        http:Request req = new;
        string textPayload = "token=" + credential;
        var tokenTypeHint = self.tokenTypeHint;
        if (tokenTypeHint is string) {
            textPayload += "&token_type_hint=" + tokenTypeHint;
        }
        req.setTextPayload(textPayload, mime:APPLICATION_FORM_URLENCODED);
        var response = self.introspectionClient->post("", req);
        if (response is http:Response) {
            json|error result = response.getJsonPayload();
            if (result is error) {
                return <@untainted> auth:Error(message = result.reason(), cause = result);
            }
            json payload = <json>result;
            boolean active = <boolean>payload.active;
            if (active) {
                string username = "";
                string scopes = "";
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
                    exp = self.introspectionServerConfig.expTimeInSeconds +  (time:currentTime().time / 1000);
                }

                if (oauth2Cache is cache:Cache) {
                    addToAuthenticationCache(oauth2Cache, credential, username, scopes, exp);
                }
                auth:setAuthenticationContext("oauth2", credential);
                auth:setPrincipal(username, username, getScopes(scopes));
                return true;
            }
            return false;
        } else {
            return auth:prepareError("Failed to call the introspection endpoint.", response);
        }
    }
};

function addToAuthenticationCache(cache:Cache oauth2Cache, string token, string username, string scopes, int exp) {
    CachedOAuth2Info cachedOAuth2Info = {username: username, scopes: scopes, expiryTime: exp};
    oauth2Cache.put(token, cachedOAuth2Info);
    log:printDebug(function() returns string {
        return "Add authenticated user :" + username + " to the cache.";
    });
}

function authenticateFromCache(cache:Cache oauth2Cache, string token) returns CachedOAuth2Info? {
    var cachedOAuth2Info = trap <CachedOAuth2Info>oauth2Cache.get(token);
    if (cachedOAuth2Info is CachedOAuth2Info) {
        if (cachedOAuth2Info.expiryTime > (time:currentTime().time / 1000)) {
            return cachedOAuth2Info;
        } else {
            oauth2Cache.remove(token);
        }
    }
}

# Represents cached OAuth2 information.
#
# + username - Username of the OAuth2 validated user
# + scopes - Scopes of the OAuth2 validated user
# + expiryTime - Expiration time, identifies the expiration time on or after which the OAuth2 token must not be accepted
public type CachedOAuth2Info record {|
    string username;
    string scopes;
    int expiryTime;
|};

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
# + oauth2Cache - Cache used to store parsed OAuth2 token information as CachedOAuth2Token
# + expTimeInSeconds - Expiration time of the tokens if introspection response does not contain `exp` field
# + clientConfig - HTTP client configurations which calls the introspection server
public type IntrospectionServerConfig record {|
    string url;
    string tokenTypeHint?;
    cache:Cache oauth2Cache?;
    int expTimeInSeconds = 3600;
    http:ClientConfiguration clientConfig = {};
|};
