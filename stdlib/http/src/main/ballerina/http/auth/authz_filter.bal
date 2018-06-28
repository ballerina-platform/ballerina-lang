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
import ballerina/cache;
import ballerina/reflect;

documentation {
    Representation of the Authorization filter

    F{{authzHandler}} `HttpAuthzHandler` instance for handling authorization
}
public type AuthzFilter object {

    public HttpAuthzHandler authzHandler;

    public new (authzHandler) {
    }

    documentation {
        Filter function implementation which tries to authorize the request

        P{{listener}} `Listener` instance that is the http endpoint
        P{{request}} `Request` instance
        P{{context}} `FilterContext` instance
        R{{}} A flag to indicate if the request flow should be continued(true) or aborted(false), a code and a message
    }
    public function filterRequest (Listener listener, Request request, FilterContext context) returns boolean {
		// first check if the resource is marked to be authenticated. If not, no need to authorize.
        ListenerAuthConfig? resourceLevelAuthAnn = getAuthAnnotation(ANN_PACKAGE, RESOURCE_ANN_NAME,
            reflect:getResourceAnnotations(context.serviceType, context.resourceName));
        ListenerAuthConfig? serviceLevelAuthAnn = getAuthAnnotation(ANN_PACKAGE, SERVICE_ANN_NAME,
            reflect:getServiceAnnotations(context.serviceType));
        if (!isResourceSecured(resourceLevelAuthAnn, serviceLevelAuthAnn)) {
            // not secured, no need to authorize
            return isAuthzSuccessfull(listener, true);
        }

        string[]? scopes = getScopesForResource(resourceLevelAuthAnn, serviceLevelAuthAnn);
        boolean authorized;
        match scopes {
            string[] scopeNames => {
                if (authzHandler.canHandle(request)) {
                    authorized = authzHandler.handle(runtime:getInvocationContext().userPrincipal.username,
                        context.serviceName, context.resourceName, request.method, scopeNames);
                } else {
                    authorized = false;
                }
            }
            () => {
                // scopes are not defined, no need to authorize
                authorized = true;
            }
        }
        return isAuthzSuccessfull(listener, authorized);
    }

    public function filterResponse(Response response, FilterContext context) returns boolean {
        return true;
    }
};

documentation {
        Verifies if the authorization is successful. If not responds to the user.

        P{{authorized}} flag to indicate if authorization is successful or not
        R{{}} A boolean flag to indicate if the request flow should be continued(true) or
        aborted(false)
}
function isAuthzSuccessfull(Listener listener, boolean authorized) returns boolean {
    endpoint Listener caller = listener;
    Response response;
    if (!authorized) {
        response.statusCode = 403;
        response.setTextPayload("Authorization failure");
        var value = caller->respond(response);
        match value {
            error err => throw err;
            () => {}
        }
        return false;
    }
    return true;
}

documentation {
        Retrieves the scope for the resource, if any

        P{{resourceLevelAuthAnn}} `ListenerAuthConfig` instance denoting resource level auth annotation details
        P{{serviceLevelAuthAnn}} `ListenerAuthConfig` instance denoting service level auth annotation details
        R{{}} Array of scopes for the given resource or nil of no scopes are defined
}
function getScopesForResource (ListenerAuthConfig? resourceLevelAuthAnn, ListenerAuthConfig? serviceLevelAuthAnn)
                                                                                            returns (string[]|()) {
    match resourceLevelAuthAnn.scopes {
        string[] scopes => {
            return scopes;
        }
        () => {
            match serviceLevelAuthAnn.scopes {
                string[] scopes => {
                    return scopes;
                }
                () => {
                    return ();
                }
            }
        }
    }
}
