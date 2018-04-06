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

package ballerina.http;

import ballerina/auth.basic;
import ballerina/auth.utils;
import ballerina/auth.userstore;
import ballerina/log;

@Description {value:"Authentication cache name"}
@final string AUTH_CACHE = "basic_auth_cache";

userstore:FilebasedUserstore fileBasedUserstore = new;
userstore:UserStore userstore = <userstore:UserStore> fileBasedUserstore;
@Description {value:"Basic authenticator instance"}
basic:BasicAuthenticator basicAuthenticator = basic:createAuthenticator(userstore, utils:createCache(AUTH_CACHE));

@Description {value:"Representation of Basic Auth handler for HTTP traffic"}
@Field {value:"name: Authentication handler name"}
public type HttpBasicAuthnHandler object {
    public {
        string name;
    }
    new () {
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
    var credentials = utils:extractBasicAuthCredentials(basicAuthHeaderValue);
    match credentials {
        (string, string) creds => {
            var (username, password) = creds;
            return basicAuthenticator.authenticate(username, password);
        }
        error err => {
            log:printErrorCause("Error in decoding basic authentication header", err);
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
