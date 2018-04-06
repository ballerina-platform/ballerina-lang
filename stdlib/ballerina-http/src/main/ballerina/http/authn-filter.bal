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

    new (function (Request, FilterContext) returns (FilterResult) requestFilter,
        function (Response, FilterContext) returns (FilterResult) responseFilter) {
        filterRequest = requestFilter;
        filterResponse = responseFilter;
    }

    public function init ();
    public function terminate ();
};

@Description {value:"Initializes the AuthnFilter"}
public function AuthnFilter::init () {
    authnHandlerChain = createAuthnHandlerChain();
}

@Description {value:"Stops the AuthnFilter"}
public function AuthnFilter::terminate () {
}

@Description {value:"Filter function implementation which tries to authenticate the request"}
@Param {value:"request: Request instance"}
@Param {value:"context: FilterContext instance"}
@Return {value:"FilterResult: Authentication result to indicate if the request can proceed or not"}
public function authnRequestFilterFunc (Request request, FilterContext context) returns (FilterResult) {
    // check if this resource is secured
    boolean authenticated;
    if (isResourceSecured(context)) {
        authenticated = authnHandlerChain.handle(request);
    } else {
        // let the request pass without authentication
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
@Return {value:"boolean: true if the resource is secured, else false"}
function isResourceSecured (FilterContext context) returns (boolean) {
    // get authn details from the resource level
    auth:Authentication|() authAnn = getAuthnAnnotation(
                                  internal:getResourceAnnotations(context.serviceType, context.resourceName));
    match authAnn {
        auth:Authentication authAnnotation => {
            return authAnnotation.enabled;
        }
        () => {
            // if not found at resource level, check in the service level
            auth:Authentication|() serviceLevelAuthAnn = getAuthnAnnotation(internal:getServiceAnnotations(context
                                                                                                   .serviceType));
            match serviceLevelAuthAnn {
                auth:Authentication authAnnotation => {
                    return authAnnotation.enabled;
                }
                () => {
                    // if still authentication annotation is nil, means the user has not specified that the service
                    // should be secured. However since the authn filter has been engaged, need to authenticate.
                    return true;
                }
            }
        }
    }
}

@Description {value:"Tries to retrieve the annotation value for authentication hierarchically - first from the resource
level
and then from the service level, if its not there in the resource level"}
@Param {value:"annData: array of annotationData instances"}
@Return {value:"Authentication: Authentication instance if its defined, else nil"}
function getAuthnAnnotation (internal:annotationData[] annData) returns (auth:Authentication|()) {
    if (lengthof annData == 0) {
        return ();
    }
    internal:annotationData|() authAnn;
    foreach ann in annData {
        if (ann.name == AUTH_ANN_NAME && ann.pkgName == AUTH_ANN_PACKAGE) {
            authAnn = ann;
            break;
        }
    }
    match authAnn {
        internal:annotationData annData1 => {
            var authConfig = check <auth:AuthConfig> annData1.value;
            return authConfig.authentication;
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
