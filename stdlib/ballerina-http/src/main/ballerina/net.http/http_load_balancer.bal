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

package ballerina.net.http;

@Description {value:"Stands for the round robin algorithm for load balancing."}
public const string ROUND_ROBIN = "round-robin";

@Description {value:"Load Balancer adds an additional layer to the HTTP client to make network interactions more resilient."}
@Field {value:"serviceUri: This is there just so that the struct is equivalent to HttpClient. This has no bearing on the functionality."}
@Field {value:"config: The client endpoint configurations for the load balancer"}
@Field {value:"loadBalanceClientsArray: The clients for the load balance endpoints"}
@Field {value:"algorithm: The load balance algorithm. Round robin algorithm is provided out of the box."}
@Field {value:"nextIndex: Indicates the next client to be used. Users should not edit this."}
public struct LoadBalancer {
    string serviceUri;
    ClientEndpointConfiguration config;
    HttpClient[] loadBalanceClientsArray;
    string algorithm;
    int nextIndex; // Keeps to index which needs to be take the next load balance endpoint.
}

@Description {value:"Represents an error occurred in an action of the Load Balance connector."}
@Field {value:"message: An error message explaining about the error."}
@Field {value:"cause: The error that caused HttpConnectorError to get thrown."}
@Field {value:"stackTrace: Represents the invocation stack when LoadBalanceConnectorError is thrown."}
@Field {value:"statusCode: HTTP status code of the LoadBalanceConnectorError."}
@Field {value:"httpConnectorError: Array of HttpConnectorError error occurred at each endpoint."}
public struct LoadBalanceConnectorError {
    string message;
    error[] cause;
    int statusCode;
    HttpConnectorError[] httpConnectorError;
}


@Description {value:"The POST action implementation of the LoadBalancer Connector."}
@Param {value:"path: Resource path"}
@Param {value:"request: A Request struct"}
@Return {value:"The Response struct"}
@Return {value:"Error occurred during the action invocation, if any"}
public function <LoadBalancer lb> post (string path, Request request) returns (Response | HttpConnectorError) {
    return performLoadBalanceAction(lb, path, request, HttpOperation.POST);
}

@Description {value:"The HEAD action implementation of the LoadBalancer Connector."}
@Param {value:"path: Resource path"}
@Param {value:"request: A Request struct"}
@Return {value:"The Response struct"}
@Return {value:"Error occurred during the action invocation, if any"}
public function <LoadBalancer lb> head (string path, Request request) returns (Response | HttpConnectorError) {
    return performLoadBalanceAction(lb, path, request, HttpOperation.HEAD);
}

@Description {value:"The PATCH action implementation of the LoadBalancer Connector."}
@Param {value:"path: Resource path"}
@Param {value:"request: A Request struct"}
@Return {value:"The Response struct"}
@Return {value:"Error occurred during the action invocation, if any"}
public function <LoadBalancer lb> patch (string path, Request request) returns (Response | HttpConnectorError) {
    return performLoadBalanceAction(lb, path, request, HttpOperation.PATCH);
}

@Description {value:"The PUT action implementation of the Load Balance Connector."}
@Param {value:"path: Resource path"}
@Param {value:"request: A Request struct"}
@Return {value:"The Response struct"}
@Return {value:"Error occurred during the action invocation, if any"}
public function <LoadBalancer lb> put (string path, Request request) returns (Response | HttpConnectorError) {
    return performLoadBalanceAction(lb, path, request, HttpOperation.PUT);
}

@Description {value:"The OPTIONS action implementation of the LoadBalancer Connector."}
@Param {value:"path: Resource path"}
@Param {value:"request: A Request struct"}
@Return {value:"The Response struct"}
@Return {value:"Error occurred during the action invocation, if any"}
public function <LoadBalancer lb> options (string path, Request request) returns (Response | HttpConnectorError) {
    return performLoadBalanceAction(lb, path, request, HttpOperation.OPTIONS);
}

@Description {value:"The FORWARD action implementation of the LoadBalancer Connector."}
@Param {value:"path: Resource path"}
@Param {value:"request: A Request struct"}
@Return {value:"The Response struct"}
@Return {value:"Error occurred during the action invocation, if any"}
public function <LoadBalancer lb> forward (string path, Request request) returns (Response | HttpConnectorError) {
    return performLoadBalanceAction(lb, path, request, HttpOperation.FORWARD);
}

@Description {value:"The EXECUTE action implementation of the LoadBalancer Connector. The Execute action can be used to invoke an HTTP call with the given HTTP verb."}
@Param {value:"httpVerb: HTTP verb to be used for the request"}
@Param {value:"path: Resource path"}
@Param {value:"request: A Request struct"}
@Return {value:"The Response struct"}
@Return {value:"Error occurred during the action invocation, if any"}
public function <LoadBalancer lb> execute (string httpVerb, string path, Request request) returns (Response | HttpConnectorError) {
    return performLoadBalanceExecuteAction(lb, path, request, httpVerb);
}

@Description {value:"The DELETE action implementation of the LoadBalancer Connector."}
@Param {value:"path: Resource path"}
@Param {value:"request: A Request struct"}
@Return {value:"The Response struct"}
@Return {value:"Error occurred during the action invocation, if any"}
public function <LoadBalancer lb> delete (string path, Request request) returns (Response | HttpConnectorError) {
    return performLoadBalanceAction(lb, path, request, HttpOperation.DELETE);
}

@Description {value:"The GET action implementation of the LoadBalancer Connector."}
@Param {value:"path: Resource path"}
@Param {value:"request: A Request struct"}
@Return {value:"The Response struct"}
@Return {value:"Error occurred during the action invocation, if any"}
public function <LoadBalancer lb> get (string path, Request request) returns (Response | HttpConnectorError) {
    return performLoadBalanceAction(lb, path, request, HttpOperation.GET);
}

@Description { value:"The submit implementation of the LoadBalancer Connector."}
@Param { value:"httpVerb: The HTTP verb value" }
@Param { value:"path: The Resource path " }
@Param { value:"req: An HTTP outbound request message" }
@Return { value:"The Handle for further interactions" }
@Return { value:"The Error occured during HTTP client invocation" }
public function <LoadBalancer lb> submit (string httpVerb, string path, Request req) returns (HttpHandle | HttpConnectorError) {
    HttpConnectorError httpConnectorError = {};
    httpConnectorError.message = "Unsupported action for LoadBalancer Connector";
    return httpConnectorError;
}

@Description { value:"The getResponse implementation of the LoadBalancer Connector."}
@Param { value:"handle: The Handle which relates to previous async invocation" }
@Return { value:"The HTTP response message" }
@Return { value:"The Error occured during HTTP client invocation" }
public function <LoadBalancer lb> getResponse (HttpHandle handle) returns (Response | HttpConnectorError) {
    HttpConnectorError httpConnectorError = {};
    httpConnectorError.message = "Unsupported action for LoadBalancer Connector";
    return httpConnectorError;
}

@Description { value:"The hasPromise implementation of the LoadBalancer Connector."}
@Param { value:"handle: The Handle which relates to previous async invocation" }
@Return { value:"Whether push promise exists" }
public function <LoadBalancer lb> hasPromise (HttpHandle handle) returns (boolean) {
    return false;
}

@Description { value:"The getNextPromise implementation of the LoadBalancer Connector."}
@Param { value:"handle: The Handle which relates to previous async invocation" }
@Return { value:"The HTTP Push Promise message" }
@Return { value:"The Error occured during HTTP client invocation" }
public function <LoadBalancer lb> getNextPromise (HttpHandle handle) returns (PushPromise | HttpConnectorError) {
    HttpConnectorError httpConnectorError = {};
    httpConnectorError.message = "Unsupported action for LoadBalancer Connector";
    return httpConnectorError;
}

@Description { value:"The getPromisedResponse implementation of the LoadBalancer Connector."}
@Param { value:"promise: The related Push Promise message" }
@Return { value:"HTTP The Push Response message" }
@Return { value:"The Error occured during HTTP client invocation" }
public function <LoadBalancer lb> getPromisedResponse (PushPromise promise) returns (Response | HttpConnectorError) {
    HttpConnectorError httpConnectorError = {};
    httpConnectorError.message = "Unsupported action for LoadBalancer Connector";
    return httpConnectorError;
}

@Description { value:"The rejectPromise implementation of the LoadBalancer Connector."}
@Param { value:"promise: The Push Promise need to be rejected" }
@Return { value:"Whether operation is successful" }
public function <LoadBalancer lb> rejectPromise (PushPromise promise) returns (boolean) {
    return false;
}

// Performs execute action of the Load Balance connector. extract the corresponding http integer value representation
// of the http verb and invokes the perform action method.
function performLoadBalanceExecuteAction (LoadBalancer lb, string path, Request outRequest,
                                          string httpVerb) returns (Response | HttpConnectorError) {
    HttpOperation connectorAction = extractHttpOperation(httpVerb);
    if (connectorAction != null) {
        return performLoadBalanceAction(lb, path, outRequest, connectorAction);
    } else {
        HttpConnectorError httpConnectorError = {};
        httpConnectorError.statusCode = 400;
        httpConnectorError.message = "Unsupported connector action received.";
        return httpConnectorError;
    }
}

// Handles all the actions exposed through the Load Balance connector.
function performLoadBalanceAction (LoadBalancer lb, string path, Request outRequest, HttpOperation requestAction)
                                    returns (Response | HttpConnectorError) {
    // When performing passthrough scenarios using Load Balance connector, message needs to be built before trying out the
    // load balance endpoints to keep the request message to load balance the messages in case of failure.
    if (HttpOperation.FORWARD == requestAction) {
        match outRequest.getBinaryPayload() {
            // TODO: remove these dummy assignments once empty blocks are supported
            blob => {int x = 0;}
            mime:EntityError => {int x = 0;}
        }
    }

    int loadBalanceTermination = 0; // Tracks at which point failover within the load balancing should be terminated.
    LoadBalanceConnectorError loadBalanceConnectorError = {};
    loadBalanceConnectorError.httpConnectorError = [];

    while (loadBalanceTermination < lengthof lb.loadBalanceClientsArray) {
        HttpClient loadBalanceClient = roundRobin(lb, lb.loadBalanceClientsArray);

        match invokeEndpoint(path, outRequest, requestAction, loadBalanceClient) {
            Response inResponse => return inResponse;

            HttpConnectorError httpConnectorError => {
                loadBalanceConnectorError.httpConnectorError[lb.nextIndex] = httpConnectorError;
                loadBalanceClient = roundRobin(lb, lb.loadBalanceClientsArray);
                loadBalanceTermination = loadBalanceTermination + 1;
            }
        }
    }

    return populateGenericLoadBalanceConnectorError(loadBalanceConnectorError);
}

// Round Robin Algorithm implementation with respect to load balancing endpoints.
public function roundRobin(LoadBalancer lb, HttpClient[] loadBalanceConfigArray) returns (HttpClient) {
    HttpClient httpClient = {};

    lock {
        if (lb.nextIndex == ((lengthof (loadBalanceConfigArray)) - 1)) {
            httpClient = loadBalanceConfigArray[lb.nextIndex];
            lb.nextIndex = 0;
        } else {
            httpClient = loadBalanceConfigArray[lb.nextIndex];
            lb.nextIndex = lb.nextIndex + 1;
        }
    }

    return httpClient;
}

// Populates generic error specific to Load Balance connector by including all the errors returned from endpoints.
function populateGenericLoadBalanceConnectorError (LoadBalanceConnectorError loadBalanceConnectorError)
                                                    returns (HttpConnectorError) {
    int nErrs = lengthof loadBalanceConnectorError.httpConnectorError;
    loadBalanceConnectorError.statusCode = 500;
    loadBalanceConnectorError.message = "All the load balance endpoints failed. Last error was: "
                                        + loadBalanceConnectorError.httpConnectorError[nErrs - 1].message;
    HttpConnectorError httpConnectorError = loadBalanceConnectorError;
    return httpConnectorError;
}

