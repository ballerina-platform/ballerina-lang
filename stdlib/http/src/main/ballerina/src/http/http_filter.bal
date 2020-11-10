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

# Abstract Representation of a HTTP Request Filter.
# This filter will be applied before the request is dispatched to the relevant resource.
# Any RequestFilter implementation should be structurally similar to or implement the RequestFilter object.
public type RequestFilter object {
    # Request filter function. If a false is returned the response should have been sent from this function as it will
    # not be dispatched to the next filter or the resource.
    #
    # + caller - The http caller
    # + request - An inbound HTTP request message
    # + context - A filter context
    # + return - True if the filter succeeds
    public function filterRequest(Caller caller, Request request, FilterContext context) returns boolean;
};

# Abstract Representation of a HTTP Response Filter.
# This filter will be applied in the response path.
# Any ResponseFilter implementation should be structurally similar to or implement the ResponseFilter object.
public type ResponseFilter object {
    # Response filter function. If a false is returned a 500 Internal Server Error would be sent to the client.
    #
    # + response - An outbound HTTP response message
    # + context - A filter context
    # + return - True if the filter succeeds
    public function filterResponse(Response response, FilterContext context) returns boolean;
};

# Representation of request filter Context.
#
# + serviceRef - The service
# + serviceName - Name of the service
# + resourceName - Name of the resource
# + attributes - Attributes to share between filters
public class FilterContext {

    private service serviceRef;
    private string serviceName = "";
    private string resourceName = "";
    public map<any> attributes = {};

    # Initializes the `http:FilterContext` object.
    #
    # + serviceRef - The service to which the context is applied
    # + serviceName - Name of the service
    # + resourceName - Name of the resource function
    public function init(service serviceRef, string serviceName, string resourceName) {
        self.serviceRef = serviceRef;
        self.serviceName = serviceName;
        self.resourceName = resourceName;
    }

    # Gets the service to which the `http:FilerContext` is applied.
    #
    # + return  - `service` of the context
    public function getService() returns service {
        return self.serviceRef;
    }

    # Gets the service name to which the `http:FilerContext` is applied.
    #
    # + return  - Name of the `service`
    public function getServiceName() returns string {
        return self.serviceName;
    }

    # Gets the resource function name to which the `http:FilerContext` is applied.
    #
    # + return  - Name of the resource function
    public function getResourceName() returns string {
        return self.resourceName;
    }
}
