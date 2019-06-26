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
import ballerina/encoding;
import ballerina/log;
import ballerina/runtime;

# Defines the Basic Auth header handler for inbound and outbound HTTP traffic.
#
# + authProvider - The AuthProvider instance.
public type BasicAuthHandler object {

    *InboundAuthHandler;
    *OutboundAuthHandler;

    public auth:InboundAuthProvider|auth:OutboundAuthProvider authProvider;

    public function __init(auth:InboundAuthProvider|auth:OutboundAuthProvider authProvider) {
        self.authProvider = authProvider;
    }

    # Checks if the provided request can be authenticated with the Basic Auth header.
    #
    # + req - The request object.
    # + return - Returns `true` if authentication is successful. Else, returns `false`.
    public function canHandle(Request req) returns boolean {
        if (req.hasHeader(AUTH_HEADER)) {
            string headerValue = extractAuthorizationHeaderValue(req);
            return headerValue.hasPrefix(auth:AUTH_SCHEME_BASIC);
        }
        return false;
    }

    # Authenticates the incoming request with the use of the credentials passed as the Basic Auth header.
    #
    # + req - The request object.
    # + return - Returns `true` if it is possible to authenticate with Basic Auth. Else, returns `false` or the `error` in case of an error.
    public function handle(Request req) returns boolean|error {
        string headerValue = extractAuthorizationHeaderValue(req);
        string credential = headerValue.substring(5, headerValue.length()).trim();
        var authProvider = self.authProvider;
        if (authProvider is auth:InboundAuthProvider) {
            return authProvider.authenticate(credential);
        } else {
            return prepareError("Outbound auth provider is configured for inbound authentication.");
        }
    }

    # Prepares the request with the Basic Auth header.
    #
    # + req - The`Request` instance.
    # + return - Returns the updated `Request` instance or the`error` in case of an error.
    public function prepare(Request req) returns Request|error {
        var authProvider = self.authProvider;
        if (authProvider is auth:OutboundAuthProvider) {
            string token = check authProvider.generateToken();
            req.setHeader(AUTH_HEADER, auth:AUTH_SCHEME_BASIC + token);
            return req;
        } else {
            return prepareError("Inbound auth provider is configured for outbound authentication.");
        }
    }

    # Inspects the request and response and calls the Auth provider for inspection.
    #
    # req - The `Request` instance.
    # resp - The `Response` instance.
    # + return - Returns the updated `Request` instance, the `error` in case of an error, or `()` if nothing is to be done.
    public function inspect(Request req, Response resp) returns Request|error? {
        var authProvider = self.authProvider;
        if (authProvider is auth:OutboundAuthProvider) {
            map<anydata> headerMap = createResponseHeaderMap(resp);
            string? token = check authProvider.inspect(headerMap);
            if (token is string) {
                req.setHeader(AUTH_HEADER, auth:AUTH_SCHEME_BASIC + token);
                return req;
            }
            return ();
        } else {
            return prepareError("Inbound auth provider is configured for outbound authentication.");
        }
    }
};
