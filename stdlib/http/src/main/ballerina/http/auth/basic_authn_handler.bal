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
import ballerina/log;
import ballerina/runtime;
import ballerina/encoding;

# Authentication cache name.
const string AUTH_CACHE = "basic_auth_cache";

# Defines Basic Auth handler for HTTP traffic.
#
# + name - Authentication handler name
# + authStoreProvider - AuthStoreProvider instance
public type HttpBasicAuthnHandler object {
    public string name = "basic";
    public auth:AuthStoreProvider authStoreProvider = new;

    public function __init(auth:AuthStoreProvider authStoreProvider) {
        self.authStoreProvider = authStoreProvider;
    }

    # Checks if the provided request can be authenticated with basic auth.
    #
    # + req - Request object
    # + return - `true` if it is possible authenticate with basic auth, else `false`
    public function canHandle(Request req) returns (boolean);

    # Intercept requests for authentication.
    #
    # + req - Request object
    # + return - `true` if authentication is a success, else `false`
    public function handle(Request req) returns (boolean);
};

public function HttpBasicAuthnHandler.handle(Request req) returns (boolean) {
    // extract the header value
    var basicAuthHeader = extractBasicAuthHeaderValue(req);
    string basicAuthHeaderValue = "";
    if (basicAuthHeader is string) {
        basicAuthHeaderValue = basicAuthHeader;
    } else {
        log:printError("Error in extracting basic authentication header");
        return false;
    }
    var credentials = extractBasicAuthCredentials(basicAuthHeaderValue);
    if (credentials is (string, string)) {
        var (username, password) = credentials;
        boolean authenticated = self.authStoreProvider.authenticate(username, password);
        if (authenticated) {
            // set username
            runtime:getInvocationContext().userPrincipal.username = username;
            // read scopes and set to the invocation context
            string[] scopes = self.authStoreProvider.getScopes(username);
            if (scopes.length() > 0) {
                runtime:getInvocationContext().userPrincipal.scopes = scopes;
            }
        }
        return authenticated;
    } else if (credentials is error) {
        log:printError("Error in decoding basic authentication header", err = credentials);
    }
    return false;
}

public function HttpBasicAuthnHandler.canHandle(Request req) returns (boolean) {
    var basicAuthHeader = trap req.getHeader(AUTH_HEADER);
    if (basicAuthHeader is string) {
        return basicAuthHeader.hasPrefix(AUTH_SCHEME_BASIC);
    }
    return false;
}

# Extracts the basic authentication credentials from the header value.
#
# + authHeader - Basic authentication header
# + return - A `string` tuple with the extracted username and password or `error` that occured while extracting credentials
function extractBasicAuthCredentials(string authHeader) returns (string, string)|error {
    // extract user credentials from basic auth header
    string decodedBasicAuthHeader = encoding:byteArrayToString(check
        encoding:decodeBase64(authHeader.substring(5, authHeader.length()).trim()));
    string[] decodedCredentials = decodedBasicAuthHeader.split(":");
    if (decodedCredentials.length() != 2) {
        return handleError("Incorrect basic authentication header format");
    } else {
        return (decodedCredentials[0], decodedCredentials[1]);
    }
}
