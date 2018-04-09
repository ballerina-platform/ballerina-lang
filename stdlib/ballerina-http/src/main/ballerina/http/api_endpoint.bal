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

@Description {value:"Representation of an API Endpoint"}
@Field {value:"config: ServiceEndpointConfiguration instance"}
@Field {value:"httpEndpoint: HTTP Listener instance"}
public type ApiEndpoint object {
    public {
        ServiceEndpointConfiguration config;
        Listener httpEndpoint;
    }

    new () {
        httpEndpoint = new;
    }

    public function init (ServiceEndpointConfiguration config);
    public function register (typedesc serviceType);
    public function start ();
    public function getClient () returns (Connection);
    public function stop ();
};

@Description {value:"Add authn and authz filters"}
@Param {value:"config: ServiceEndpointConfiguration instance"}
function addAuthFilters (ServiceEndpointConfiguration config) {
    // add authentication and authorization filters as the first two filters.
    // if there are any other filters specified, those should be added after the authn and authz filters.
    if (config.filters == null) {
        // can add authn and authz filters directly
        config.filters = createAuthFilters();
    } else {
        Filter[] newFilters = createAuthFilters();
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
function createAuthFilters () returns (Filter[]) {
    // TODO: currently hard coded. fix it.
    Filter[] authFilters = [];
    //TODO fix this object instantiation properly
    AuthnFilter authnFilter = new (authnRequestFilterFunc, responseFilterFunc);
    AuthzFilter authzFilter = new (authzRequestFilterFunc, responseFilterFunc);
    authFilters[0] = authnFilter;
    authFilters[1] = authzFilter;
    return authFilters;
}

public function ApiEndpoint::init (ServiceEndpointConfiguration config) {
    addAuthFilters(config);
    self.httpEndpoint.init(config);
}

public function ApiEndpoint::register (typedesc serviceType) {
    self.httpEndpoint.register(serviceType);
}

public function ApiEndpoint::start () {
    self.httpEndpoint.start();
}

public function ApiEndpoint::getClient () returns (Connection) {
    return self.httpEndpoint.getClient();
}

public function ApiEndpoint::stop () {
    self.httpEndpoint.stop();
}
