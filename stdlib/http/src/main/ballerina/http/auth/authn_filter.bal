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
import ballerina/reflect;

# Representation of the Authentication filter.
#
# + authnHandlerChain - The Authentication handler chain
public type AuthnFilter object {

    public AuthnHandlerChain authnHandlerChain;

    public new(authnHandlerChain) {}

    # Request filter method which attempts to authenticated the request.
    #
    # + listener - The http endpoint
    # + request - An inboud HTTP request message
    # + context - A filter context
    # + return - True if the filter succeeds
    public function filterRequest(Listener listener, Request request, FilterContext context) returns boolean {
        // get auth config for this resource
        boolean authenticated = false;
        var (isSecured, authProviders) = getResourceAuthConfig(context);
        if (isSecured) {
            // if auth providers are there, use those to authenticate
            if (authProviders.length() > 0) {
                authenticated = self.authnHandlerChain.handleWithSpecificAuthnHandlers(authProviders, request);
            } else {
                // if not, try to authenticate using any of available authn handlers
                authenticated = self.authnHandlerChain.handle(request);
            }
        } else {
            // not secured, no need to authenticate
            return isAuthnSuccesfull(listener, true);
        }
        return isAuthnSuccesfull(listener, authenticated);
    }

    public function filterResponse(Response response, FilterContext context) returns boolean {
        return true;
    }
};

# Verifies if the authentication is successful. If not responds to the user.
#
# + listener - The http endpoint
# + authenticated - Authorization status for the request
# + return - Authorization result to indicate if the filter can proceed(true) or not(false)
function isAuthnSuccesfull(Listener listener, boolean authenticated) returns boolean {
    endpoint Listener caller = listener;
    Response response = new;
    if (!authenticated) {
        response.statusCode = 401;
        response.setTextPayload("Authentication failure");
        var err = caller->respond(response);
        if (err is error) {
            panic err;
        }
        return false;
    }
    return true;
}

# Checks if the resource is secured.
#
# + context - A filter context
# + return - A tuple of whether the resource is secured and the list of auth provider ids
function getResourceAuthConfig(FilterContext context) returns (boolean, string[]) {
    boolean resourceSecured;
    string[] authProviderIds = [];
    // get authn details from the resource level
    ListenerAuthConfig? resourceLevelAuthAnn = getAuthAnnotation(ANN_MODULE, RESOURCE_ANN_NAME,
        reflect:getResourceAnnotations(context.serviceType, context.resourceName));
    ListenerAuthConfig? serviceLevelAuthAnn = getAuthAnnotation(ANN_MODULE, SERVICE_ANN_NAME,
        reflect:getServiceAnnotations(context.serviceType));
    // check if authentication is enabled
    resourceSecured = isResourceSecured(resourceLevelAuthAnn, serviceLevelAuthAnn);
    // if resource is not secured, no need to check further
    if (!resourceSecured) {
        return (resourceSecured, authProviderIds);
    }
    // check if auth providers are given at resource level
    var resourceProviders = resourceLevelAuthAnn.authProviders;
    if (resourceProviders is string[]) {
        authProviderIds = resourceProviders;
    } else {
        // no auth providers found in resource level, try in service level
        var serviceProviders = serviceLevelAuthAnn.authProviders;
        if (serviceProviders is string[]) {
            authProviderIds = serviceProviders;
        }
    }
    return (resourceSecured, authProviderIds);
}

function isResourceSecured(ListenerAuthConfig? resourceLevelAuthAnn, ListenerAuthConfig? serviceLevelAuthAnn)
             returns boolean {
    boolean isSecured;
    var resourceAuthn = resourceLevelAuthAnn.authentication;
    if (resourceAuthn is Authentication) {
        isSecured = resourceAuthn.enabled;
    } else {
        // if not found at resource level, check in the service level
        var serviceAuthn = serviceLevelAuthAnn.authentication;
        if (serviceAuthn is Authentication) {
            isSecured = serviceAuthn.enabled;
        } else {
            // if still authentication annotation is nil, means the user has not specified that the service
            // should be secured. However since the authn filter has been engaged, need to authenticate.
            isSecured = true;
        }
    }
    return isSecured;
}

# Tries to retrieve the annotation value for authentication hierarchically - first from the resource level and then
# from the service level, if it  is not there in the resource level.
#
# + annotationModule - Annotation module name
# + annotationName - Annotation name
# + annData - Array of annotationData instances
# + return - ListenerAuthConfig instance if its defined, else nil
function getAuthAnnotation(string annotationModule, string annotationName, reflect:annotationData[] annData) returns (
            ListenerAuthConfig?) {
    if (annData.length() == 0) {
        return ();
    }
    reflect:annotationData|() authAnn = ();
    foreach ann in annData {
        if (ann.name == annotationName && ann.moduleName == annotationModule) {
            authAnn = ann;
            break;
        }
    }
    if (authAnn is reflect:annotationData) {
        if (annotationName == RESOURCE_ANN_NAME) {
            var resourceConfig = <HttpResourceConfig>authAnn.value;
            if (resourceConfig is HttpResourceConfig) {
                return resourceConfig.authConfig;
            } else if (resourceConfig is error) {
                panic resourceConfig;
            }
        } else if (annotationName == SERVICE_ANN_NAME) {
            var serviceConfig = <HttpServiceConfig>authAnn.value;
            if (serviceConfig is HttpServiceConfig) {
                return serviceConfig.authConfig;
            } else if (serviceConfig is error) {
                panic serviceConfig;
            }
        }
    }
    return ();
}
