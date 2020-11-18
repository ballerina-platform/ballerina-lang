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

# Representation of the Authentication filter.
#
# + authHandlers - An array of authentication handlers or an array consisting of arrays of authentication handlers
public class AuthnFilter {

    *RequestFilter;

    public InboundAuthHandlers authHandlers;

    # Initializes the `AuthnFilter` object.
    #
    # + authHandlers - An array of authentication handlers or an array consisting of arrays of authentication handlers
    public function init(InboundAuthHandlers authHandlers) {
        self.authHandlers = authHandlers;
    }

    # The request filter method, which attempts to authenticate the request.
    #
    # + caller - Caller for outbound HTTP responses
    # + request - An inbound HTTP request message
    # + context - The `http:FilterContext` instance
    # + return - A flag to indicate if the request flow should be continued(true) or aborted(false)
    public function filterRequest(Caller caller, Request request, FilterContext context) returns boolean {
        boolean|AuthenticationError authenticated = true;
        InboundAuthHandlers|boolean authHandlers = getAuthHandlers(context);
        if (authHandlers is InboundAuthHandlers) {
            authenticated = handleAuthRequest(authHandlers, request);
        } else {
            if (authHandlers) {
                authenticated = handleAuthRequest(self.authHandlers, request);
            }
        }
        if (authenticated is boolean && authenticated) {
            return true;
        }
        send401(caller, context);
        return false;
    }
}

function handleAuthRequest(InboundAuthHandlers authHandlers, Request request) returns boolean|AuthenticationError {
    if (authHandlers is InboundAuthHandler[]) {
        return checkForAuthHandlers(authHandlers, request);
    } else {
        foreach InboundAuthHandler[] authHandler in authHandlers {
            boolean|AuthenticationError response = checkForAuthHandlers(authHandler, request);
            if (response is boolean) {
                if (!response) {
                    return response;
                }
            } else {
                return response;
            }
        }
        return true;
    }
}

function checkForAuthHandlers(InboundAuthHandler[] authHandlers, Request request) returns boolean|AuthenticationError {
    AuthenticationError? err = ();
    foreach InboundAuthHandler authHandler in authHandlers {
        boolean canProcessResponse = authHandler.canProcess(request);
        if (canProcessResponse) {
            boolean|AuthenticationError handleResponse = authHandler.process(request);
            if (handleResponse is boolean) {
                if (handleResponse) {
                    // If one of the authenticators from the chain could successfully authenticate the user,
                    // it is not required to look through other providers. The authenticator chain is using "OR"
                    // combination of provider results.
                    return true;
                }
            } else {
                err = handleResponse;
            }
        }
    }
    if (err is AuthenticationError) {
        return err;
    }
    return false;
}

function send401(Caller caller, FilterContext context) {
    if (isWebSocketUpgradeRequest(context)) {
        error? err = caller->cancelWebSocketUpgrade(401, "Authentication failure.");
        if (err is error) {
            panic <error> err;
        }
    } else {
        Response response = new;
        response.statusCode = 401;
        response.setTextPayload("Authentication failure.");
        error? err = caller->respond(response);
        if (err is error) {
            panic <error> err;
        }
    }
}
