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

@Description {value:"Representation of the Authorization filter"}
@Field {value:"filterRequest: request filter method which attempts to authorize the request"}
@Field {value:"filterRequest: response filter method (not used this scenario)"}
public type AuthzFilter object {

    public {
        HttpAuthzHandler authzHandler;
    }

    new (authzHandler) {
    }
    
    @Description {value:"Filter function implementation which tries to authorize the request"}
	@Param {value:"request: Request instance"}
	@Param {value:"context: FilterContext instance"}
	@Return {value:"FilterResult: Authorization result to indicate if the request can proceed or not"}
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

@Description {value:"Creates an instance of FilterResult"}
@Param {value:"authorized: authorization status for the request"}
@Return {value:"FilterResult: Authorization result to indicate if the request can proceed or not"}
function createAuthzResult (boolean authorized) returns (FilterResult) {
    FilterResult requestFilterResult = {};
    if (authorized) {
        requestFilterResult = {canProceed:true, statusCode:200, message:"Successfully authorized"};
    } else {
        requestFilterResult = {canProceed:false, statusCode:403, message:"Authorization failure"};
    }
    return requestFilterResult;
}

@Description {value:"Retrieves the scope for the resource, if any"}
@Param {value:"resourceLevelAuthAnn: Resource level auth annotation"}
@Param {value:"serviceLevelAuthAnn: service level auth annotation"}
@Return {value:"string: Scope name if defined, else nil"}
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
