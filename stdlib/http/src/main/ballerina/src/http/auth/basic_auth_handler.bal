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

# Defines the Basic Auth header handler for inbound and outbound HTTP traffic.
public class BasicAuthHandler {

    *InboundAuthHandler;
    *OutboundAuthHandler;

    auth:InboundAuthProvider|auth:OutboundAuthProvider authProvider;

    # Initializes the `BasicAuthHandler` object.
    #
    # + authProvider - The `auth:InboundAuthProvider` instance or the `auth:OutboundAuthProvider` instance
    public function init(auth:InboundAuthProvider|auth:OutboundAuthProvider authProvider) {
        self.authProvider = authProvider;
    }

    # Checks if the provided request can be authenticated with the Basic Auth header.
    #
    # + req - The `http:Request` instance
    # + return - `true` if authentication is successful or else `false`
    public function canProcess(Request req) returns @tainted boolean {
        if (req.hasHeader(AUTH_HEADER)) {
            string headerValue = extractAuthorizationHeaderValue(req);
            return headerValue.startsWith(auth:AUTH_SCHEME_BASIC);
        }
        return false;
    }

    # Authenticates the incoming request with the use of the credentials passed as the Basic Auth header.
    #
    # + req - The `http:Request` instance
    # + return - `true` if it is possible to authenticate with Basic Auth, `false` otherwise, or else
    #                 an `http:AuthenticationError` in case of an error
    public function process(Request req) returns boolean|AuthenticationError {
        string headerValue = extractAuthorizationHeaderValue(req);
        string credential = headerValue.substring(5, headerValue.length());
        credential = credential.trim();
        auth:InboundAuthProvider|auth:OutboundAuthProvider authProvider = self.authProvider;
        if (authProvider is auth:InboundAuthProvider) {
            boolean|auth:Error authenticationResult = authProvider.authenticate(credential);
            if (authenticationResult is boolean) {
                return authenticationResult;
            } else {
                return prepareAuthenticationError("Failed to authenticate with basic auth hanndler.", authenticationResult);
            }
        } else {
            return prepareAuthenticationError("Outbound auth provider is configured for inbound authentication.");
        }
    }

    # Prepares the request with the Basic Auth header.
    #
    # + req - The `http:Request` instance
    # + return - The updated `http:Request` instance or else an `http:AuthenticationError` in case of an error
    public function prepare(Request req) returns Request|AuthenticationError {
        auth:InboundAuthProvider|auth:OutboundAuthProvider authProvider = self.authProvider;
        if (authProvider is auth:OutboundAuthProvider) {
            string|auth:Error token = authProvider.generateToken();
            if (token is string) {
                req.setHeader(AUTH_HEADER, auth:AUTH_SCHEME_BASIC + token);
                return req;
            } else {
                return prepareAuthenticationError("Failed to prepare request at basic auth handler.", token);
            }
        } else {
            return prepareAuthenticationError("Inbound auth provider is configured for outbound authentication.");
        }
    }

    # Inspects the request and response and calls the Auth provider for inspection.
    #
    # + req - The `http:Request` instance
    # + resp - The `http:Response` instance
    # + return - The updated `http:Request` instance or the `http:AuthenticationError` in case of an error
    #                 or else `()` if nothing is to be returned
    public function inspect(Request req, Response resp) returns Request|AuthenticationError? {
        auth:InboundAuthProvider|auth:OutboundAuthProvider authProvider = self.authProvider;
        if (authProvider is auth:OutboundAuthProvider) {
            map<anydata> headerMap = createResponseHeaderMap(resp);
            string|auth:Error? token = authProvider.inspect(headerMap);
            if (token is string) {
                req.setHeader(AUTH_HEADER, auth:AUTH_SCHEME_BASIC + token);
                return req;
            } else if (token is auth:Error) {
                return prepareAuthenticationError("Failed to inspect at basic auth handler.", token);
            }
            return;
        } else {
            return prepareAuthenticationError("Inbound auth provider is configured for outbound authentication.");
        }
    }
}
