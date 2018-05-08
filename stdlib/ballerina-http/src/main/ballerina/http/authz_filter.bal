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

    public {
        HttpAuthzHandler authzHandler;
    }

    public new (authzHandler) {
    }

    documentation {
        Filter function implementation which tries to authorize the request

        P{{request}} `Request` instance
        P{{context}} `FilterContext` instance
        R{{}} `FilterResult` instance populated with a flag to indicate if the request flow should be continued or
        aborted, a code and a message
    }
    public function filterRequest (Request request, FilterContext context) returns FilterResult {
		// first check if the resource is marked to be authenticated. If not, no need to authorize.
        ListenerAuthConfig? resourceLevelAuthAnn = getAuthAnnotation(ANN_PACKAGE, RESOURCE_ANN_NAME,
            reflect:getResourceAnnotations(context.serviceType, context.resourceName));
        ListenerAuthConfig? serviceLevelAuthAnn = getAuthAnnotation(ANN_PACKAGE, SERVICE_ANN_NAME,
            reflect:getServiceAnnotations(context.serviceType));
        if (!isResourceSecured(resourceLevelAuthAnn, serviceLevelAuthAnn)) {
            // not secured, no need to authorize
            return createAuthzResult(true);
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
        return createAuthzResult(authorized);
    }
};

documentation {
        Creates an instance of `FilterResult`

        P{{authorized}} flag to indicate if authorization is successful or not
        R{{}} `FilterResult` instance populated with a flag to indicate if the request flow should be continued or
        aborted, a code and a message
}
function createAuthzResult (boolean authorized) returns (FilterResult) {
    FilterResult requestFilterResult = {};
    if (authorized) {
        requestFilterResult = {canProceed:true, statusCode:200, message:"Successfully authorized"};
    } else {
        requestFilterResult = {canProceed:false, statusCode:403, message:"Authorization failure"};
    }
    return requestFilterResult;
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
