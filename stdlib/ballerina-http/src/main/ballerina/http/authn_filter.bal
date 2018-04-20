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

import ballerina/auth;
import ballerina/reflect;

@Description {value:"Representation of the Authentication filter"}
@Field {value:"filterRequest: request filter method which attempts to authenticated the request"}
@Field {value:"filterRequest: response filter method (not used this scenario)"}
public type AuthnFilter object {

    public {
        AuthnHandlerChain authnHandlerChain;
    }

    new (authnHandlerChain) {}
    
    @Description {value:"filterRequest: Request filter function"}
    public function filterRequest (Request request, FilterContext context) returns FilterResult {
	    // get auth config for this resource
    	boolean authenticated;
    	var (isSecured, authProviders) = getResourceAuthConfig(context);
    	if (isSecured) {
        	// if auth providers are there, use those to authenticate
            if (lengthof authProviders > 0) {
                authenticated = self.authnHandlerChain.handleWithSpecificAuthnHandlers(authProviders, request);
            } else {
                // if not, try to authenticate using any of available authn handlers
                authenticated = self.authnHandlerChain.handle(request);
            }
    	} else {
            // not secured, no need to authenticate
            return createAuthnResult(true);
        }
        return createAuthnResult(authenticated);
    }
};

@Description {value:"Creates an instance of FilterResult"}
@Param {value:"authorized: authorization status for the request"}
@Return {value:"FilterResult: Authorization result to indicate if the request can proceed or not"}
function createAuthnResult (boolean authenticated) returns (FilterResult) {
    FilterResult requestFilterResult = {};
    if (authenticated) {
        requestFilterResult = {canProceed:true, statusCode:200, message:"Successfully authenticated"};
    } else {
        requestFilterResult = {canProceed:false, statusCode:401, message:"Authentication failure"};
    }
    return requestFilterResult;
}

@Description {value:"Checks if the resource is secured"}
@Param {value:"context: FilterContext object"}
@Return {value:"boolean, string[]: tuple of whether the resource is secured and the list of auth provider ids "}
function getResourceAuthConfig (FilterContext context) returns (boolean, string[]) {
    boolean resourceSecured;
    string[] authProviderIds = [];
    // get authn details from the resource level
    ListenerAuthConfig? resourceLevelAuthAnn = getAuthAnnotation(ANN_PACKAGE, RESOURCE_ANN_NAME,
                                    reflect:getResourceAnnotations(context.serviceType, context.resourceName));
    ListenerAuthConfig? serviceLevelAuthAnn = getAuthAnnotation(ANN_PACKAGE, SERVICE_ANN_NAME,
                                    reflect:getServiceAnnotations(context.serviceType));
    // check if authentication is enabled
    resourceSecured = isResourceSecured(resourceLevelAuthAnn, serviceLevelAuthAnn);
    // if resource is not secured, no need to check further
    if (!resourceSecured) {
        return (resourceSecured, authProviderIds);
    }
    // check if auth providers are given at resource level
    match resourceLevelAuthAnn.authProviders {
        string[] providers => {
            authProviderIds = providers;
        }
        () => {
            // no auth providers found in resource level, try in service level
            match serviceLevelAuthAnn.authProviders {
                string[] providers => {
                    authProviderIds = providers;
                }
                () => {
                    // no auth providers found
                }
            }
        }
    }
    return (resourceSecured, authProviderIds);
}

function isResourceSecured (ListenerAuthConfig? resourceLevelAuthAnn, ListenerAuthConfig? serviceLevelAuthAnn)
                                                                                                    returns boolean {
    boolean isSecured;
    match resourceLevelAuthAnn.authentication {
        Authentication authn => {
            isSecured = authn.enabled;
        }
        () => {
            // if not found at resource level, check in the service level
            match serviceLevelAuthAnn.authentication {
                Authentication authn => {
                    isSecured = authn.enabled;
                }
                () => {
                    // if still authentication annotation is nil, means the user has not specified that the service
                    // should be secured. However since the authn filter has been engaged, need to authenticate.
                    isSecured = true;
                }
            }
        }
    }
    return isSecured;
}

@Description {value:"Tries to retrieve the annotation value for authentication hierarchically - first from the resource
level
and then from the service level, if its not there in the resource level"}
@Param {value:"annotationPackage: annotation package name"}
@Param {value:"annotationName: annotation name"}
@Param {value:"annData: array of annotationData instances"}
@Return {value:"ListenerAuthConfig: ListenerAuthConfig instance if its defined, else nil"}
function getAuthAnnotation (string annotationPackage, string annotationName, reflect:annotationData[] annData) returns (ListenerAuthConfig?) {
    if (lengthof annData == 0) {
        return ();
    }
    reflect:annotationData|() authAnn;
    foreach ann in annData {
        if (ann.name == annotationName && ann.pkgName == annotationPackage) {
            authAnn = ann;
            break;
        }
    }
    match authAnn {
        reflect:annotationData annData1 => {
            if (annotationName == RESOURCE_ANN_NAME) {
                HttpResourceConfig resourceConfig = check <HttpResourceConfig>annData1.value;
                return resourceConfig.authConfig;
            } else if (annotationName == SERVICE_ANN_NAME) {
                HttpServiceConfig serviceConfig = check <HttpServiceConfig>annData1.value;
                return serviceConfig.authConfig;
            } else {
                return ();
            }
        }
        () => {
            return ();
        }
    }
}
