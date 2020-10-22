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

# Representation of the Authorization filter.
#
# + authzHandler - `AuthzHandler` instance for handling authorization
# + scopes - An array of scopes or an array consisting of arrays of scopes
public class AuthzFilter {

    *RequestFilter;

    public AuthzHandler authzHandler;
    public Scopes? scopes;

    # Initializes the `AuthzFilter` object.
    #
    # + authzHandler - The `http:AuthzHandler` instance for handling authorization
    # + scopes - An array of scopes or an array consisting of arrays of scopes
    public function init(AuthzHandler authzHandler, Scopes? scopes) {
        self.authzHandler = authzHandler;
        self.scopes = scopes;
    }

    # Filter function implementation which tries to authorize the request.
    #
    # + caller - Caller for outbound HTTP responses
    # + request - An inbound HTTP request message
    # + context - The `http:FilterContext` instance
    # + return - A flag to indicate if the request flow should be continued(true) or aborted(false)
    public function filterRequest(Caller caller, Request request, FilterContext context) returns boolean {
        boolean|AuthorizationError authorized = true;
        Scopes|boolean scopes = getScopes(context);
        if (scopes is Scopes) {
            authorized = self.authzHandler.canProcess(request);
            if (authorized is boolean && authorized) {
                authorized = self.authzHandler.process(scopes);
            }
        } else {
            if (scopes) {
                var selfScopes = self.scopes;
                if (selfScopes is Scopes) {
                    authorized = self.authzHandler.canProcess(request);
                    if (authorized is boolean && authorized) {
                        authorized = self.authzHandler.process(selfScopes);
                    }
                }
            }
        }
        if (authorized is boolean && authorized) {
            return true;
        }
        send403(caller, context);
        return false;
    }
}

function send403(Caller caller, FilterContext context) {
    if (isWebSocketUpgradeRequest(context)) {
        error? err = caller->cancelWebSocketUpgrade(403, "Authorization failure.");
        if (err is error) {
            panic <error> err;
        }
    } else {
        Response response = new;
        response.statusCode = 403;
        response.setTextPayload("Authorization failure.");
        error? err = caller->respond(response);
        if (err is error) {
            panic <error> err;
        }
    }
}
