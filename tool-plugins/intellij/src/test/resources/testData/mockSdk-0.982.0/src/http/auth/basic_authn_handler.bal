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

# Authentication cache name.
@final string AUTH_CACHE = "basic_auth_cache";

# Defines Basic Auth handler for HTTP traffic.
#
# + name - Authentication handler name
# + authStoreProvider - AuthStoreProvider instance
public type HttpBasicAuthnHandler object {
    public string name;
    public auth:AuthStoreProvider authStoreProvider;

    public new(authStoreProvider) {
        name = "basic";
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

function HttpBasicAuthnHandler::handle(Request req) returns (boolean) {

    // extract the header value
    var basicAuthHeader = extractBasicAuthHeaderValue(req);
    string basicAuthHeaderValue;
    match basicAuthHeader {
        string basicAuthHeaderStr => {
            basicAuthHeaderValue = basicAuthHeaderStr;
        }
        () => {
            log:printError("Error in extracting basic authentication header");
            return false;
        }
    }
    var credentials = extractBasicAuthCredentials(basicAuthHeaderValue);
    match credentials {
        (string, string) creds => {
            var (username, password) = creds;
            boolean isAuthenticated = self.authStoreProvider.authenticate(username, password);
            if (isAuthenticated) {
                // set username
                runtime:getInvocationContext().userPrincipal.username = username;
                // read scopes and set to the invocation context
                string[] scopes = self.authStoreProvider.getScopes(username);
                if (lengthof scopes > 0) {
                    runtime:getInvocationContext().userPrincipal.scopes = scopes;
                }
            }
            return isAuthenticated;
        }
        error err => {
            log:printError("Error in decoding basic authentication header", err = err);
            return false;
        }
    }
}

function HttpBasicAuthnHandler::canHandle(Request req) returns (boolean) {
    string basicAuthHeader;
    try {
        basicAuthHeader = req.getHeader(AUTH_HEADER);
    } catch (error e) {
        return false;
    }
    match basicAuthHeader {
        string basicAuthHeaderStr => {
            return basicAuthHeader.hasPrefix(AUTH_SCHEME_BASIC);
        }
    }
}

# Extracts the basic authentication credentials from the header value.
#
# + authHeader - Basic authentication header
# + return - A `string` tuple with the extracted username and password or `error` that occured while extracting credentials
function extractBasicAuthCredentials(string authHeader) returns (string, string)|error {
    // extract user credentials from basic auth header
    string decodedBasicAuthHeader;
    try {
        decodedBasicAuthHeader = check authHeader.substring(5, authHeader.length()).trim().base64Decode();
    } catch (error err) {
        return err;
    }
    string[] decodedCredentials = decodedBasicAuthHeader.split(":");
    if (lengthof decodedCredentials != 2) {
        return handleError("Incorrect basic authentication header format");
    } else {
        return (decodedCredentials[0], decodedCredentials[1]);
    }
}
