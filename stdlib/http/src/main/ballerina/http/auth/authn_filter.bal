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

import ballerina/reflect;

# Representation of the Authentication filter.
#
# + authHandlers - An array of authentication handlers.
public type AuthnFilter object {

    public InboundAuthHandler?[]|InboundAuthHandler?[][] authHandlers;

    public function __init(InboundAuthHandler?[]|InboundAuthHandler?[][] authHandlers) {
        self.authHandlers = authHandlers;
    }

    # The request filter method, which attempts to authenticate the request.
    #
    # + caller - Caller for outbound HTTP responses
    # + request - An inboud HTTP request message
    # + context - A filter context
    # + return - Returns `true` if the filter succeeds. Else, returns `false`.
    public function filterRequest(Caller caller, Request request, FilterContext context) returns boolean {
        boolean|error authenticated;
        var authHandlers = getAuthHandlers(context);
        if (authHandlers is InboundAuthHandler?[]|InboundAuthHandler?[][]) {
            authenticated = handleAuthRequest(authHandlers, request);
        } else {
            if (authHandlers) {
                authenticated = handleAuthRequest(self.authHandlers, request);
            } else {
                authenticated = true;
            }
        }
        return isAuthnSuccessful(caller, authenticated);
    }

    public function filterResponse(Response response, FilterContext context) returns boolean {
        return true;
    }
};

function handleAuthRequest(InboundAuthHandler?[]|InboundAuthHandler?[][] authHandlers, Request request) returns boolean|error {
    if (authHandlers is InboundAuthHandler?[]) {
        return checkForAuthHandlers(authHandlers, request);
    } else {
        foreach InboundAuthHandler?[] authHandler in authHandlers {
            var response = checkForAuthHandlers(authHandler, request);
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

function checkForAuthHandlers(InboundAuthHandler?[] authHandlers, Request request) returns boolean|error {
    error? err = ();
    foreach InboundAuthHandler? authHandler in authHandlers {
        if (authHandler is InboundAuthHandler) {
            boolean canHandleResponse = authHandler.canHandle(request);
            if (canHandleResponse) {
                var handleResponse = authHandler.handle(request);
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
    }
    if (err is error) {
        return err;
    }
    return false;
}

# Verifies if the authentication is successful. If not responds to the user.
#
# + caller - Caller for outbound HTTP responses
# + authenticated - Authentication status for the request, or `error` if error occurred
# + return - Authentication result to indicate if the filter can proceed(true) or not(false)
function isAuthnSuccessful(Caller caller, boolean|error authenticated) returns boolean {
    Response response = new;
    response.statusCode = 401;
    if (authenticated is boolean) {
        if (!authenticated) {
            response.setTextPayload("Authentication failure");
            var err = caller->respond(response);
            if (err is error) {
                panic err;
            }
            return false;
        }
    } else {
        response.setTextPayload("Authentication failure. " + authenticated.reason());
        var err = caller->respond(response);
        if (err is error) {
            panic err;
        }
        return false;
    }
    return true;
}
