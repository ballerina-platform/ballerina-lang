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

import ballerina/internal;
import ballerina/auth;

@Description {value:"Authn handler chain instance"}
AuthnHandlerChain authnHandlerChain;

@Description {value:"Representation of the Authentication filter"}
@Field {value:"filterRequest: request filter method which attempts to authenticated the request"}
@Field {value:"filterRequest: response filter method (not used this scenario)"}
public type AuthnFilter object {
    public {
        function (Request request, FilterContext context) returns (FilterResult) filterRequest;
        function (Response response, FilterContext context) returns (FilterResult) filterResponse;
    }

    public new (filterRequest, filterResponse) {
    }

    public function init ();
    public function terminate ();
};

@Description {value:"Initializes the AuthnFilter"}
public function AuthnFilter::init () {
    authnHandlerChain = new;
}

@Description {value:"Stops the AuthnFilter"}
public function AuthnFilter::terminate () {
}

@Description {value:"Filter function implementation which tries to authenticate the request"}
@Param {value:"request: Request instance"}
@Param {value:"context: FilterContext instance"}
@Return {value:"FilterResult: Authentication result to indicate if the request can proceed or not"}
public function authnRequestFilterFunc (Request request, FilterContext context) returns (FilterResult) {
    // get auth config for this resource
    boolean authenticated;
    var (isSecured, authProviders) = getResourceAuthConfig(context);
    if (isSecured) {
        // if auth providers are there, use those to authenticate
        match authProviders {
            string[] providers => {
                if (lengthof providers > 0) {
                    authenticated = authnHandlerChain.handleWithSpecificAuthnHandlers(providers, request);
                }
                // if not, try to authenticate using any of available authn handlers
                authenticated = authnHandlerChain.handle(request);
            }
            () => {
                authenticated = authnHandlerChain.handle(request);
            }
        }
    } else {
        // not secured
        return createAuthnResult(true);
    }
    return createAuthnResult(authenticated);
}

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
@Return {value:"boolean, string[]?: tuple of whether the resource is secured and the "}
function getResourceAuthConfig (FilterContext context) returns (boolean, string[]?) {
    boolean isResourceSecured;
    string[]? authProviderIds;
    // get authn details from the resource level
    AuthConfig? resourceLevelAuthAnn = getAuthAnnotation(ANN_PACKAGE, RESOURCE_ANN_NAME,
                                    internal:getResourceAnnotations(context.serviceType, context.resourceName));
    AuthConfig? serviceLevelAuthAnn = getAuthAnnotation(ANN_PACKAGE, SERVICE_ANN_NAME,
                                    internal:getServiceAnnotations(context.serviceType));
    // check if authentication is enabled
    match resourceLevelAuthAnn.authn {
        Authentication authn => {
            isResourceSecured  = authn.enabled;
        }
        () => {
            // if not found at resource level, check in the service level
            match serviceLevelAuthAnn.authn {
                Authentication authn => {
                    isResourceSecured  = authn.enabled;
                }
                () => {
                    // if still authentication annotation is nil, means the user has not specified that the service
                    // should be secured. However since the authn filter has been engaged, need to authenticate.
                    isResourceSecured = true;
                }
            }
        }
    }
    // if resource is not secured, no need to check further
    if (!isResourceSecured) {
        return (isResourceSecured, authProviderIds);
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
    return (isResourceSecured, authProviderIds);
}

@Description {value:"Tries to retrieve the annotation value for authentication hierarchically - first from the resource
level
and then from the service level, if its not there in the resource level"}
@Param {value:"annotationPackage: annotation package name"}
@Param {value:"annotationName: annotation name"}
@Param {value:"annData: array of annotationData instances"}
@Return {value:"AuthConfig: AuthConfig instance if its defined, else nil"}
function getAuthAnnotation (string annotationPackage, string annotationName, internal:annotationData[] annData)
                                                                                            returns (AuthConfig?) {
    if (lengthof annData == 0) {
        return ();
    }
    internal:annotationData|() authAnn;
    foreach ann in annData {
        if (ann.name == annotationName && ann.pkgName == annotationPackage) {
            authAnn = ann;
            break;
        }
    }
    match authAnn {
        internal:annotationData annData1 => {
            if (annotationName == RESOURCE_ANN_NAME) {
                HttpResourceConfig resourceConfig = check <HttpResourceConfig> annData1.value;
                return resourceConfig.authConfig;
            } else if (annotationName == SERVICE_ANN_NAME) {
                HttpServiceConfig serviceConfig = check <HttpServiceConfig> annData1.value;
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

@Description {value:"Filter function implementation for the response flow"}
@Param {value:"request: Request instance"}
@Param {value:"context: FilterContext instance"}
@Return {value:"FilterResult: returns a FilterResult instance with a hard coded successful message"}
public function responseFilterFunc (Response response, FilterContext context) returns (FilterResult) {
    FilterResult responseFilterResult = {canProceed:true, statusCode:200, message:"Successful"};
    return responseFilterResult;
}
