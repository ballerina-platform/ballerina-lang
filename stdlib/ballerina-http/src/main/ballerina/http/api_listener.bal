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

@Description {value:"Representation of an API Listener"}
@Field {value:"config: ServiceEndpointConfiguration instance"}
@Field {value:"httpListener: HTTP Listener instance"}
public type APIListener object {
    public {
        ServiceEndpointConfiguration config;
        Listener httpListener;
    }

    new () {
        httpListener = new;
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
    AuthHandlerRegistry registry = new;
    registry.add("basic", createBasicAuthHandler());
    // TODO: add JWT handler
    AuthnFilter authnFilter = new (registry, authnRequestFilterFunc, responseFilterFunc);
    AuthzFilter authzFilter = new (authzRequestFilterFunc, responseFilterFunc);
    authFilters[0] = <Filter> authnFilter;
    authFilters[1] = authzFilter;
    return authFilters;
}

public function APIListener::init (ServiceEndpointConfiguration config) {
    addAuthFilters(config);
    self.httpListener.init(config);
}

public function APIListener::register (typedesc serviceType) {
    self.httpListener.register(serviceType);
}

public function APIListener::start () {
    self.httpListener.start();
}

public function APIListener::getClient () returns (Connection) {
    return self.httpListener.getClient();
}

public function APIListener::stop () {
    self.httpListener.stop();
}
