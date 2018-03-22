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

package ballerina.net.http.endpoints;

import ballerina/net.http;
import ballerina/net.http.authadaptor;

@Description {value:"Representation of an API Endpoint"}
@Field {value:"config: ServiceEndpointConfiguration instance"}
@Field {value:"httpEndpoint: ServiceEndpoint instance"}
public struct ApiEndpoint {
    http:ServiceEndpointConfiguration config;
    http:ServiceEndpoint httpEndpoint;
}

@Description {value:"Add authn and authz filters"}
@Param {value:"config: ServiceEndpointConfiguration instance"}
function addAuthFilters (http:ServiceEndpointConfiguration config) {
    // add authentication and authorization filters as the first two filters.
    // if there are any other filters specified, those should be added after the authn and authz filters.
    if (config.filters == null) {
        // can add authn and authz filters directly
        config.filters = createAuthFilters();
    } else {
        http:Filter[] newFilters = createAuthFilters();
        // add existing filters next
        int i = 0;
        while (i < lengthof config.filters) {
            newFilters[i + (lengthof newFilters)] = config.filters[i];
            i = i + 1;
        }
        config.filters = newFilters;
    }
}

@Description {value:"Create an array of auth and authz filters"}
@Return {value:"Array of Filters comprising of authn and authz Filters"}
function createAuthFilters () returns (http:Filter[]) {
    // TODO: currently hard coded. fix it.
    http:Filter[] authFilters = [];
    authadaptor:AuthnFilter authnFilter = {};
    authadaptor:AuthzFilter authzFilter = {};
    authFilters[0] = authnFilter;
    authFilters[1] = authzFilter;
    return authFilters;
}

public function <ApiEndpoint ep> ApiEndpoint () {
    ep.httpEndpoint = {};
}

public function <ApiEndpoint ep> init (http:ServiceEndpointConfiguration config) {
    addAuthFilters(config);
    ep.httpEndpoint.init(config);
}

public function <ApiEndpoint ep> register (typedesc serviceType) {
    ep.httpEndpoint.register(serviceType);
}

public function <ApiEndpoint ep> start () {
    ep.httpEndpoint.start();
}

public function <ApiEndpoint ep> getClient () returns (http:Connection) {
    return ep.httpEndpoint.getClient();
}

public function <ApiEndpoint ep> stop () {
    ep.httpEndpoint.stop();
}
