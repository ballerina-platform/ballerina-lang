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

@Description {value:"Authentication cache name"}
@final string AUTH_CACHE = "basic_auth_cache";

@Description {value:"Representation of Basic Auth handler for HTTP traffic"}
@Field {value:"authProvider: AuthProvider instance"}
@Field {value:"name: Authentication handler name"}
public type HttpBasicAuthnHandler object {
    public {
        string name;
        auth:AuthProvider authProvider;
    }
    new (authProvider) {
        name = "basic";
    }

    public function canHandle (Request req) returns (boolean);
    public function handle (Request req) returns (boolean);
};

@Description {value:"Intercepts a request for authentication"}
@Param {value:"req: Request object"}
@Return {value:"boolean: true if authentication is a success, else false"}
public function HttpBasicAuthnHandler::handle (Request req) returns (boolean) {
    
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
            boolean isAuthenticated = self.authProvider.authenticate(username, password);
            if (isAuthenticated) {
                // set username
                runtime:getInvocationContext().userPrincipal.username = username;
                // read scopes and set to the invocation context
                string[] scopes = self.authProvider.getScopes(username);
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

@Description {value:"Checks if the provided request can be authenticated with basic auth"}
@Param {value:"req: Request object"}
@Return {value:"boolean: true if its possible authenticate with basic auth, else false"}
public function HttpBasicAuthnHandler::canHandle (Request req) returns (boolean) {
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

@Description {value:"Extracts the basic authentication credentials from the header value"}
@Param {value:"authHeader: basic authentication header"}
@Return {value:"string: username extracted"}
@Return {value:"string: password extracted"}
@Return {value:"error: any error occurred while extracting creadentials"}
function extractBasicAuthCredentials (string authHeader) returns (string, string)|error {
    // extract user credentials from basic auth header
    string decodedBasicAuthHeader;
    try {
        decodedBasicAuthHeader = check authHeader.subString(5, authHeader.length()).trim().base64Decode();
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
