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

package ballerina.net.http.authadaptor;

import ballerina/net.http;
import ballerina/auth.basic;
import ballerina/auth.utils;
import ballerina/auth.userstore;
import ballerina/security.crypto;
import ballerina/log;

@Description {value:"Authentication cache name"}
const string AUTH_CACHE = "basic_auth_cache";

userstore:FilebasedUserstore fileBasedUserstore = {};
userstore:UserStore userstore =? <userstore:UserStore>fileBasedUserstore;
@Description {value:"Basic authenticator instance"}
basic:BasicAuthenticator basicAuthenticator = basic:createAuthenticator(userstore, utils:createCache(AUTH_CACHE));

@Description {value:"Representation of Basic Auth handler for HTTP traffic"}
@Field {value:"name: Authentication handler name"}
public struct HttpBasicAuthnHandler {
    string name = "basic";
}

@Description {value:"Intercepts a request for authentication"}
@Param {value:"req: Request object"}
@Return {value:"boolean: true if authentication is a success, else false"}
public function <HttpBasicAuthnHandler basicAuthnHandler> handle (http:Request req) returns (boolean) {

    // extract the header value
    string basicAuthHeaderValue = extractBasicAuthHeaderValue(req);
    if (basicAuthHeaderValue.length() == 0) {
        log:printError("Error in extracting basic authentication header");
        return false;
    }
    
    // check in the cache - cache key is the sha256 hash of the basic auth header value
    string basicAuthCacheKey = crypto:getHash(basicAuthHeaderValue, crypto:Algorithm.SHA256);
    var cacheResult = getCachedValue(basicAuthCacheKey);
    match cacheResult {
        boolean authenticationResult => {
            return authenticationResult;
        }
        any|null => {
            var credentials = utils:extractBasicAuthCredentials(basicAuthHeaderValue);
            match credentials {
                (string, string) creds => {
                    var (username, password) = creds;
                    basic:AuthenticationInfo authInfo = basic:createAuthenticationInfo
                                                        (username, basicAuthenticator.authenticate(username, password));
                    // cache result
                    basicAuthenticator.cacheAuthResult(basicAuthCacheKey, authInfo);
                    if (authInfo.isAuthenticated) {
                        log:printDebug("Successfully authenticated against the userstore");
                    } else {
                        log:printDebug("Authentication failure");
                    }
                    return authInfo.isAuthenticated;
                }
                error err => {
                    log:printErrorCause("Error in decoding basic authentication header", err);
                    return false;
                }
            }
        }
    }
}

function getCachedValue (string cacheKey) returns (boolean | null) {
    match basicAuthenticator.getCachedAuthResult(cacheKey) {
        basic:AuthenticationInfo authnInfo => {
            log:printDebug("Auth cache hit");
            return authnInfo.isAuthenticated;
        }
        any|null => {
            log:printDebug("Auth cache miss");
            return null;
        }
    }
}

@Description {value:"Checks if the provided request can be authenticated with basic auth"}
@Param {value:"req: Request object"}
@Return {value:"boolean: true if its possible authenticate with basic auth, else false"}
public function <HttpBasicAuthnHandler basicAuthnHandler> canHandle (http:Request req) returns (boolean) {
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
