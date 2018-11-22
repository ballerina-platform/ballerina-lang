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

# Load balancing algorithm - Round Robin
@final public string ROUND_ROBIN = "round-robin";

# LoadBalancer caller actions which provides load balancing and failover capabilities to the
#load balance client endpoint.
#
# + serviceUri - The URL of the remote HTTP endpoint
# + config - The configurations of the client endpoint associated with this `LoadBalancer` instance
# + loadBalanceClientsArray - Array of HTTP clients for load balancing
# + lbRule - Load balancing rule
# + failover - Whether to fail over in case of a failure
public type LoadBalancerActions object {

   public string serviceUri;
   public ClientEndpointConfig config;
   public CallerActions[] loadBalanceClientsArray;
   public LoadBalancerRule lbRule;
   public boolean failover;

    # Load Balancer adds an additional layer to the HTTP client to make network interactions more resilient.
    #
    # + serviceUri - The URL of the remote HTTP endpoint
    # + config - The configurations of the client endpoint associated with this `LoadBalancer` instance
    # + loadBalanceClientsArray - Array of HTTP clients for load balancing
    # + lbRule - Load balancing rule
    # + failover - Whether to fail over in case of a failure
    public new(serviceUri, config, loadBalanceClientsArray, lbRule, failover) {}

    # The POST action implementation of the LoadBalancer Connector.
    #
    # + path - Resource path
    # + message - An HTTP request or any payload of type `string`, `xml`, `json`, `byte[]`, `io:ReadableByteChannel`
    #             or `mime:Entity[]`
    # + return - The response or an `error` if failed to fulfill the request
    public function post(string path, Request|string|xml|json|byte[]|io:ReadableByteChannel|mime:Entity[]|()
                                        message) returns Response|error;

    # The HEAD action implementation of the LoadBalancer Connector.
    #
    # + path - Resource path
    # + message - An optional HTTP request or any payload of type `string`, `xml`, `json`, `byte[]`, `io:ReadableByteChannel`
    #             or `mime:Entity[]`
    # + return - The response or an `error` if failed to fulfill the request
    public function head(string path, Request|string|xml|json|byte[]|io:ReadableByteChannel|mime:Entity[]|()
                                        message = ()) returns Response|error;

    # The PATCH action implementation of the LoadBalancer Connector.
    #
    # + path - Resource path
    # + message - An HTTP request or any payload of type `string`, `xml`, `json`, `byte[]`, `io:ReadableByteChannel`
    #             or `mime:Entity[]`
    # + return - The response or an `error` if failed to fulfill the request
    public function patch(string path, Request|string|xml|json|byte[]|io:ReadableByteChannel|mime:Entity[]|()
                                            message) returns Response|error;

    # The PUT action implementation of the Load Balance Connector.
    #
    # + path - Resource path
    # + message - An HTTP request or any payload of type `string`, `xml`, `json`, `byte[]`, `io:ReadableByteChannel`
    #             or `mime:Entity[]`
    # + return - The response or an `error` if failed to fulfill the request
    public function put(string path, Request|string|xml|json|byte[]|io:ReadableByteChannel|mime:Entity[]|()
                                        message) returns Response|error;

    # The OPTIONS action implementation of the LoadBalancer Connector.
    #
    # + path - Resource path
    # + message - An optional HTTP request or any payload of type `string`, `xml`, `json`, `byte[]`, `io:ReadableByteChannel`
    #             or `mime:Entity[]`
    # + return - The response or an `error` if failed to fulfill the request
    public function options(string path, Request|string|xml|json|byte[]|io:ReadableByteChannel|mime:Entity[]|()
                                            message = ()) returns Response|error;

    # The FORWARD action implementation of the LoadBalancer Connector.
    #
    # + path - Resource path
    # + request - An optional HTTP request
    # + return - The response or an `error` if failed to fulfill the request
    public function forward(string path, Request request) returns Response|error;

    # The EXECUTE action implementation of the LoadBalancer Connector.
    # The Execute action can be used to invoke an HTTP call with the given HTTP verb.
    #
    # + httpVerb - HTTP method to be used for the request
    # + path - Resource path
    # + message - An HTTP request or any payload of type `string`, `xml`, `json`, `byte[]`, `io:ReadableByteChannel`
    #             or `mime:Entity[]`
    # + return - The response or an `error` if failed to fulfill the request
    public function execute(string httpVerb, string path, Request|string|xml|json|byte[]|io:ReadableByteChannel|mime:Entity[]|()
                                                            message) returns Response|error;

    # The DELETE action implementation of the LoadBalancer Connector.
    #
    # + path - Resource path
    # + message - An HTTP request or any payload of type `string`, `xml`, `json`, `byte[]`, `io:ReadableByteChannel`
    #             or `mime:Entity[]`
    # + return - The response or an `error` if failed to fulfill the request
    public function delete(string path, Request|string|xml|json|byte[]|io:ReadableByteChannel|mime:Entity[]|()
                                            message) returns Response|error;

    # The GET action implementation of the LoadBalancer Connector.

    # + path - Resource path
    # + message - An optional HTTP request or any payload of type `string`, `xml`, `json`, `byte[]`, `io:ReadableByteChannel`
    #             or `mime:Entity[]`
    # + return - The response or an `error` if failed to fulfill the request
    public function get(string path, Request|string|xml|json|byte[]|io:ReadableByteChannel|mime:Entity[]|()
                                        message = ()) returns Response|error;

    # The submit implementation of the LoadBalancer Connector.
    #
    # + httpVerb - The HTTP verb value
    # + path - The resource path
    # + message - An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - An `HttpFuture` that represents an asynchronous service invocation, or an `error` if the submission fails
    public function submit(string httpVerb, string path, Request|string|xml|json|byte[]|io:ReadableByteChannel|mime:Entity[]|()
                                                            message) returns HttpFuture|error;

    # The getResponse implementation of the LoadBalancer Connector.
    #
    # + httpFuture - The `HttpFuture` related to a previous asynchronous invocation
    # + return - An HTTP response message, or an `error` if the invocation fails
    public function getResponse(HttpFuture httpFuture) returns Response|error;

    # The hasPromise implementation of the LoadBalancer Connector.
    #
    # + httpFuture - The `HttpFuture` relates to a previous asynchronous invocation
    # + return - A `boolean` that represents whether a `PushPromise` exists
    public function hasPromise(HttpFuture httpFuture) returns boolean;

    # The getNextPromise implementation of the LoadBalancer Connector.
    #
    # + httpFuture - The `HttpFuture` relates to a previous asynchronous invocation
    # + return - An HTTP Push Promise message, or an `error` if the invocation fails
    public function getNextPromise(HttpFuture httpFuture) returns PushPromise|error;

    # The getPromisedResponse implementation of the LoadBalancer Connector.
    #
    # + promise - The related `PushPromise`
    # + return - A promised HTTP `Response` message, or an `error` if the invocation fails
    public function getPromisedResponse(PushPromise promise) returns Response|error;

    # The rejectPromise implementation of the LoadBalancer Connector.
    #
    # + promise - The Push Promise to be rejected
    public function rejectPromise(PushPromise promise);

};

# Represents an error occurred in an action of the Load Balance connector.
#
# + message - An error message explaining about the error
# + statusCode - HTTP status code of the LoadBalanceActionError
# + httpActionErr - Array of errors occurred at each endpoint
public type LoadBalanceActionErrorData record {
    string message = "";
    int statusCode = 0;
    error[] httpActionErr = [];
    !...
};

public type LoadBalanceActionError error<string, LoadBalanceActionErrorData>;

function LoadBalancerActions.post(string path, Request|string|xml|json|byte[]|io:ReadableByteChannel|mime:Entity[]|()
                                                        message) returns Response|error {
    Request req = buildRequest(message);
    return performLoadBalanceAction(self, path, req, HTTP_POST);
}

function LoadBalancerActions.head(string path, Request|string|xml|json|byte[]|io:ReadableByteChannel|mime:Entity[]|()
                                                            message = ()) returns Response|error {
    Request req = buildRequest(message);
    return performLoadBalanceAction(self, path, req, HTTP_HEAD);
}

function LoadBalancerActions.patch(string path, Request|string|xml|json|byte[]|io:ReadableByteChannel|mime:Entity[]|()
                                                            message) returns Response|error {
    Request req = buildRequest(message);
    return performLoadBalanceAction(self, path, req, HTTP_PATCH);
}

function LoadBalancerActions.put(string path, Request|string|xml|json|byte[]|io:ReadableByteChannel|mime:Entity[]|()
                                                        message) returns Response|error {
    Request req = buildRequest(message);
    return performLoadBalanceAction(self, path, req, HTTP_PUT);
}

function LoadBalancerActions.options(string path, Request|string|xml|json|byte[]|io:ReadableByteChannel|mime:Entity[]|()
                                                        message = ()) returns Response|error {
    Request req = buildRequest(message);
    return performLoadBalanceAction(self, path, req, HTTP_OPTIONS);
}

function LoadBalancerActions.forward(string path, Request request) returns Response|error {
    return performLoadBalanceAction(self, path, request, HTTP_FORWARD);
}

function LoadBalancerActions.execute(string httpVerb, string path, Request|string|xml|json|byte[]|
                                                        io:ReadableByteChannel|mime:Entity[]|() message) returns Response|error {
    Request req = buildRequest(message);
    return performLoadBalanceExecuteAction(self, path, req, httpVerb);
}

function LoadBalancerActions.delete(string path, Request|string|xml|json|byte[]|io:ReadableByteChannel|mime:Entity[]|()
                                                            message) returns Response|error {
    Request req = buildRequest(message);
    return performLoadBalanceAction(self, path, req, HTTP_DELETE);
}

function LoadBalancerActions.get(string path, Request|string|xml|json|byte[]|io:ReadableByteChannel|mime:Entity[]|()
                                                            message = ()) returns Response|error {
    Request req = buildRequest(message);
    return performLoadBalanceAction(self, path, req, HTTP_GET);
}

function LoadBalancerActions.submit(string httpVerb, string path, Request|string|xml|json|byte[]|
    io:ReadableByteChannel|mime:Entity[]|() message) returns HttpFuture|error {
    error err = error("Unsupported action for LoadBalancer client.");
    return err;
}

function LoadBalancerActions.getResponse(HttpFuture httpFuture) returns Response|error {
    error err = error("Unsupported action for LoadBalancer client.");
    return err;
}

function LoadBalancerActions.hasPromise(HttpFuture httpFuture) returns (boolean) {
    return false;
}

function LoadBalancerActions.getNextPromise(HttpFuture httpFuture) returns PushPromise|error {
    error err = error("Unsupported action for LoadBalancer client.");
    return err;
}

function LoadBalancerActions.getPromisedResponse(PushPromise promise) returns Response|error {
    error err = error("Unsupported action for LoadBalancer client.");
    return err;
}

function LoadBalancerActions.rejectPromise(PushPromise promise) {
}

// Performs execute action of the Load Balance connector. extract the corresponding http integer value representation
// of the http verb and invokes the perform action method.
function performLoadBalanceExecuteAction(LoadBalancerActions lb, string path, Request request,
                                         string httpVerb) returns Response|error {
    HttpOperation connectorAction = extractHttpOperation(httpVerb);
    if (connectorAction != HTTP_NONE) {
        return performLoadBalanceAction(lb, path, request, connectorAction);
    } else {
        error httpActionErr = error("Unsupported connector action received.");
        return httpActionErr;
    }
}

// Handles all the actions exposed through the Load Balance connector.
function performLoadBalanceAction(LoadBalancerActions lb, string path, Request request, HttpOperation requestAction)
             returns Response|error {
    int loadBalanceTermination = 0; // Tracks at which point failover within the load balancing should be terminated.
    //TODO: workaround to initialize a type inside a function. Change this once fix is available.
    LoadBalanceActionErrorData loadBalanceActionErrorData = {statusCode: 500, message: "", httpActionErr:[]};
    int lbErrorIndex = 0;
    Request loadBlancerInRequest = request;
    mime:Entity requestEntity = new;

    if (lb.failover) {
        if (isMultipartRequest(loadBlancerInRequest)) {
            loadBlancerInRequest = check populateMultipartRequest(loadBlancerInRequest);
        } else {
            // When performing passthrough scenarios using Load Balance connector,
            // message needs to be built before trying out the load balance endpoints to keep the request message
            // to load balance the messages in case of failure.
            var binaryPayload = loadBlancerInRequest.getBinaryPayload();
            requestEntity = check loadBlancerInRequest.getEntity();
        }
    }

    while (loadBalanceTermination < lb.loadBalanceClientsArray.length()) {
        var loadBalanceClient = lb.lbRule.getNextCallerActions(lb.loadBalanceClientsArray);
        if (loadBalanceClient is CallerActions) {
            var serviceResponse = invokeEndpoint(path, request, requestAction, loadBalanceClient);
            if (serviceResponse is Response) {
                return serviceResponse;
            } else if (serviceResponse is error) {
                if (lb.failover) {
                    loadBlancerInRequest = check createFailoverRequest(loadBlancerInRequest, requestEntity);
                    loadBalanceActionErrorData.httpActionErr[lbErrorIndex] = serviceResponse;
                    lbErrorIndex += 1;
                    loadBalanceTermination = loadBalanceTermination + 1;
                } else {
                    return serviceResponse;
                }
            }
        } else if (loadBalanceClient is error) {
            return loadBalanceClient;
        }
    }
    return populateGenericLoadBalanceActionError(loadBalanceActionErrorData);
}

// Populates generic error specific to Load Balance connector by including all the errors returned from endpoints.
function populateGenericLoadBalanceActionError(LoadBalanceActionErrorData loadBalanceActionErrorData)
                                                    returns error {
    int nErrs = loadBalanceActionErrorData.httpActionErr.length();
    string lastErrorMessage = <string> loadBalanceActionErrorData.httpActionErr[nErrs - 1].detail().message;
    loadBalanceActionErrorData.statusCode = INTERNAL_SERVER_ERROR_500;
    loadBalanceActionErrorData.message = "All the load balance endpoints failed. Last error was: " + lastErrorMessage;
    LoadBalanceActionError err = error(HTTP_ERROR_CODE, loadBalanceActionErrorData);
    return err;
}
