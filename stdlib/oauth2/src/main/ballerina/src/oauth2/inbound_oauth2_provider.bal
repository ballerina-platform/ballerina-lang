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
import ballerina/http;
import ballerina/mime;
import ballerina/stringutils;

# Represents inbound OAuth2 provider, which calls the introspection server and validate the received credentials.
#
# + introspectionClient - Introspection client endpoint
# + tokenTypeHint - A hint about the type of the token submitted for introspection
public type InboundOAuth2Provider object {

    *auth:InboundAuthProvider;

    public http:Client introspectionClient;
    public string? tokenTypeHint;

    public function __init(IntrospectionServerConfig config) {
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
        boolean authenticated = false;
        string username = "";
        string scopes = "";

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

            json payload = <json> result;
            boolean active = <boolean>payload.active;
            if (active) {
                authenticated = true;
                if (payload.username is string) {
                    username = <string>payload.username;
                }
                if (payload.scope is string) {
                    scopes = <string>payload.scope;
                }
            }
        } else {
            return auth:prepareError("Failed to call the introspection endpoint.", response);
        }

        if (authenticated) {
            auth:setAuthenticationContext("oauth2", credential);
            auth:setPrincipal(username, username, getScopes(scopes));
        }
        return authenticated;
    }
};

# Reads the scope(s) for the user with the given username.
#
# + scopes - Set of scopes seperated with a space
# + return - Array of groups for the user denoted by the username
public function getScopes(string scopes) returns string[] {
    string scopeVal = scopes.trim();
    return stringutils:split(scopeVal, " ");
}

# Represents introspection server onfigurations.
#
# + url - URL of the introspection server
# + tokenTypeHint - A hint about the type of the token submitted for introspection
# + clientConfig - HTTP client configurations which calls the introspection server
public type IntrospectionServerConfig record {|
    string url;
    string tokenTypeHint?;
    http:ClientConfiguration clientConfig = {};
|};

