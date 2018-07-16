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


documentation {Load balancing algorithm - Round Robin}
@final public string ROUND_ROBIN = "round-robin";

documentation {
    LoadBalancer caller actions which provides load balancing and failover capabilities to the
    load balance client endpoint.

    F{{serviceUri}} The URL of the remote HTTP endpoint
    F{{config}} The configurations of the client endpoint associated with this `LoadBalancer` instance
    F{{loadBalanceClientsArray}} Array of HTTP clients for load balancing
    F{{algorithm}} Load balancing algorithm
    F{{nextIndex}} Index of the next load balancing client
    F{{failover}} Whether to fail over in case of a failure
}
public type LoadBalancerActions object {

   public string serviceUri;
   public ClientEndpointConfig config;
   public CallerActions[] loadBalanceClientsArray;
   public string algorithm;
   public int nextIndex;
   public boolean failover;

   documentation {
        Load Balancer adds an additional layer to the HTTP client to make network interactions more resilient.

        P{{serviceUri}} The URL of the remote HTTP endpoint
        P{{config}} The configurations of the client endpoint associated with this `LoadBalancer` instance
        P{{loadBalanceClientsArray}} Array of HTTP clients for load balancing
        P{{algorithm}} Load balancing algorithm
        P{{nextIndex}} Index of the next load balancing client
        P{{failover}} Whether to fail over in case of a failure
   }
   public new (serviceUri, config, loadBalanceClientsArray, algorithm, nextIndex, failover) {}

    documentation {
        The POST action implementation of the LoadBalancer Connector.

        P{{path}} Resource path
        P{{message}} An HTTP request or any payload of type `string`, `xml`, `json`, `byte[]`, `io:ByteChannel`
                     or `mime:Entity[]`
        R{{}} The response or an `error` if failed to fulfill the request
    }
    public function post(string path, Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|()
                                        message) returns Response|error;

    documentation {
        The HEAD action implementation of the LoadBalancer Connector.

        P{{path}} Resource path
        P{{message}} An optional HTTP request or any payload of type `string`, `xml`, `json`, `byte[]`, `io:ByteChannel`
                     or `mime:Entity[]`
        R{{}} The response or an `error` if failed to fulfill the request
    }
    public function head(string path, Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|()
                                        message = ()) returns Response|error;

    documentation {
        The PATCH action implementation of the LoadBalancer Connector.

        P{{path}} Resource path
        P{{message}} An HTTP request or any payload of type `string`, `xml`, `json`, `byte[]`, `io:ByteChannel`
                     or `mime:Entity[]`
        R{{}} The response or an `error` if failed to fulfill the request
    }
    public function patch(string path, Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|()
                                            message) returns Response|error;

    documentation {
        The PUT action implementation of the Load Balance Connector.

        P{{path}} Resource path
        P{{message}} An HTTP request or any payload of type `string`, `xml`, `json`, `byte[]`, `io:ByteChannel`
                     or `mime:Entity[]`
        R{{}} The response or an `error` if failed to fulfill the request
    }
    public function put(string path, Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|()
                                        message) returns Response|error;

    documentation {
        The OPTIONS action implementation of the LoadBalancer Connector.

        P{{path}} Resource path
        P{{message}} An optional HTTP request or any payload of type `string`, `xml`, `json`, `byte[]`, `io:ByteChannel`
                     or `mime:Entity[]`
        R{{}} The response or an `error` if failed to fulfill the request
    }
    public function options(string path, Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|()
                                            message = ()) returns Response|error;

    documentation {
        The FORWARD action implementation of the LoadBalancer Connector.

        P{{path}} Resource path
        P{{request}} An optional HTTP request
        R{{}} The response or an `error` if failed to fulfill the request
    }
    public function forward(string path, Request request) returns Response|error;

    documentation {
        The EXECUTE action implementation of the LoadBalancer Connector.
        The Execute action can be used to invoke an HTTP call with the given HTTP verb.

        P{{httpVerb}} HTTP method to be used for the request
        P{{path}} Resource path
        P{{message}} An HTTP request or any payload of type `string`, `xml`, `json`, `byte[]`, `io:ByteChannel`
                     or `mime:Entity[]`
        R{{}} The response or an `error` if failed to fulfill the request
    }
    public function execute(string httpVerb, string path, Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|()
                                                            message) returns Response|error;

    documentation {
        The DELETE action implementation of the LoadBalancer Connector.

        P{{path}} Resource path
        P{{message}} An HTTP request or any payload of type `string`, `xml`, `json`, `byte[]`, `io:ByteChannel`
                     or `mime:Entity[]`
        R{{}} The response or an `error` if failed to fulfill the request
    }
    public function delete(string path, Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|()
                                            message) returns Response|error;

    documentation {
        The GET action implementation of the LoadBalancer Connector.

        P{{path}} Resource path
        P{{message}} An optional HTTP request or any payload of type `string`, `xml`, `json`, `byte[]`, `io:ByteChannel`
                     or `mime:Entity[]`
        R{{}} The response or an `error` if failed to fulfill the request
    }
    public function get(string path, Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|()
                                        message = ()) returns Response|error;

    documentation {
        The submit implementation of the LoadBalancer Connector.

        P{{httpVerb}} The HTTP verb value
        P{{path}} The resource path
        P{{message}} An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
                     `io:ByteChannel` or `mime:Entity[]`
        R{{}} An `HttpFuture` that represents an asynchronous service invocation, or an `error` if the submission fails
    }
    public function submit(string httpVerb, string path, Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|()
                                                            message) returns HttpFuture|error;

    documentation {
        The getResponse implementation of the LoadBalancer Connector.

        P{{httpFuture}} The `HttpFuture` related to a previous asynchronous invocation
        R{{}} An HTTP response message, or an `error` if the invocation fails
    }
    public function getResponse(HttpFuture httpFuture) returns Response|error;

    documentation {
        The hasPromise implementation of the LoadBalancer Connector.

        P{{httpFuture}} The `HttpFuture` relates to a previous asynchronous invocation
        R{{}} A `boolean` that represents whether a `PushPromise` exists
    }
    public function hasPromise(HttpFuture httpFuture) returns boolean;

    documentation {
        The getNextPromise implementation of the LoadBalancer Connector.

        P{{httpFuture}} The `HttpFuture` relates to a previous asynchronous invocation
        R{{}} An HTTP Push Promise message, or an `error` if the invocation fails
    }
    public function getNextPromise(HttpFuture httpFuture) returns PushPromise|error;

    documentation {
        The getPromisedResponse implementation of the LoadBalancer Connector.

        P{{promise}} The related `PushPromise`
        R{{}} A promised HTTP `Response` message, or an `error` if the invocation fails
    }
    public function getPromisedResponse(PushPromise promise) returns Response|error;

    documentation {
        The rejectPromise implementation of the LoadBalancer Connector.

        P{{promise}} The Push Promise to be rejected
    }
    public function rejectPromise(PushPromise promise);

};

documentation {
    Represents an error occurred in an action of the Load Balance connector.

    F{{message}} An error message explaining about the error
    F{{cause}} Cause of the error
    F{{statusCode}} HTTP status code of the LoadBalanceActionError
    F{{httpActionErr}} Array of errors occurred at each endpoint
}
public type LoadBalanceActionError record {
    string message,
    error? cause,
    int statusCode,
    error[] httpActionErr,
};

function LoadBalancerActions::post(string path, Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|()
                                                        message) returns Response|error {
    Request req = buildRequest(message);
    return performLoadBalanceAction(self, path, req, HTTP_POST);
}

function LoadBalancerActions::head(string path, Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|()
                                                            message = ()) returns Response|error {
    Request req = buildRequest(message);
    return performLoadBalanceAction(self, path, req, HTTP_HEAD);
}

function LoadBalancerActions::patch(string path, Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|()
                                                            message) returns Response|error {
    Request req = buildRequest(message);
    return performLoadBalanceAction(self, path, req, HTTP_PATCH);
}

function LoadBalancerActions::put(string path, Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|()
                                                        message) returns Response|error {
    Request req = buildRequest(message);
    return performLoadBalanceAction(self, path, req, HTTP_PUT);
}

function LoadBalancerActions::options(string path, Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|()
                                                        message = ()) returns Response|error {
    Request req = buildRequest(message);
    return performLoadBalanceAction(self, path, req, HTTP_OPTIONS);
}

function LoadBalancerActions::forward(string path, Request request) returns Response|error {
    return performLoadBalanceAction(self, path, request, HTTP_FORWARD);
}

function LoadBalancerActions::execute(string httpVerb, string path, Request|string|xml|json|byte[]|
                                                        io:ByteChannel|mime:Entity[]|() message) returns Response|error {
    Request req = buildRequest(message);
    return performLoadBalanceExecuteAction(self, path, req, httpVerb);
}

function LoadBalancerActions::delete(string path, Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|()
                                                            message) returns Response|error {
    Request req = buildRequest(message);
    return performLoadBalanceAction(self, path, req, HTTP_DELETE);
}

function LoadBalancerActions::get(string path, Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|()
                                                            message = ()) returns Response|error {
    Request req = buildRequest(message);
    return performLoadBalanceAction(self, path, req, HTTP_GET);
}

function LoadBalancerActions::submit(string httpVerb, string path, Request|string|xml|json|byte[]|
    io:ByteChannel|mime:Entity[]|() message) returns HttpFuture|error {
    error err = {message:"Unsupported action for LoadBalancer client."};
    return err;
}

function LoadBalancerActions::getResponse(HttpFuture httpFuture) returns Response|error {
    error err = {message:"Unsupported action for LoadBalancer client."};
    return err;
}

function LoadBalancerActions::hasPromise(HttpFuture httpFuture) returns (boolean) {
    return false;
}

function LoadBalancerActions::getNextPromise(HttpFuture httpFuture) returns PushPromise|error {
    error err = {message:"Unsupported action for LoadBalancer client."};
    return err;
}

function LoadBalancerActions::getPromisedResponse(PushPromise promise) returns Response|error {
    error err = {message:"Unsupported action for LoadBalancer client."};
    return err;
}

function LoadBalancerActions::rejectPromise(PushPromise promise) {
}

// Performs execute action of the Load Balance connector. extract the corresponding http integer value representation
// of the http verb and invokes the perform action method.
function performLoadBalanceExecuteAction(LoadBalancerActions lb, string path, Request request,
                                         string httpVerb) returns Response|error {
    HttpOperation connectorAction = extractHttpOperation(httpVerb);
    if (connectorAction != HTTP_NONE) {
        return performLoadBalanceAction(lb, path, request, connectorAction);
    } else {
        error httpActionErr = {message:"Unsupported connector action received."};
        return httpActionErr;
    }
}

// Handles all the actions exposed through the Load Balance connector.
function performLoadBalanceAction(LoadBalancerActions lb, string path, Request request, HttpOperation requestAction)
                                    returns Response|error {
    int loadBalanceTermination = 0; // Tracks at which point failover within the load balancing should be terminated.
    //TODO: workaround to initialize a type inside a function. Change this once fix is aailable.
    LoadBalanceActionError loadBalanceActionError = {statusCode:500};
    loadBalanceActionError.httpActionErr = [];
    Request loadBlancerInRequest = request;
    mime:Entity requestEntity = new;

    if (lb.failover) {
        if (isMultipartRequest(loadBlancerInRequest)) {
            loadBlancerInRequest = populateMultipartRequest(loadBlancerInRequest);
        } else {
            // When performing passthrough scenarios using Load Balance connector,
            // message needs to be built before trying out the load balance endpoints to keep the request message
            // to load balance the messages in case of failure.
            var binaryPayload = loadBlancerInRequest.getBinaryPayload();
            requestEntity = check loadBlancerInRequest.getEntity();
        }
    }

    while (loadBalanceTermination < lengthof lb.loadBalanceClientsArray) {
        CallerActions loadBalanceClient = roundRobin(lb, lb.loadBalanceClientsArray);

        match invokeEndpoint(path, request, requestAction, loadBalanceClient) {
            Response inResponse => return inResponse;

            error httpActionErr => {
                if (!lb.failover) {
                    return httpActionErr;
                } else {
                    loadBlancerInRequest = createFailoverRequest(loadBlancerInRequest, requestEntity);
                    loadBalanceActionError.httpActionErr[lb.nextIndex] = httpActionErr;
                    loadBalanceTermination = loadBalanceTermination + 1;
                }
            }
        }
    }
    return populateGenericLoadBalanceActionError(loadBalanceActionError);
}

// Round Robin Algorithm implementation with respect to load balancing endpoints.
documentation {
    Round Robin Algorithm implementation with respect to load balancing endpoints.

    P{{lb}} `LoadBalancer` object
    P{{loadBalanceConfigArray}} Array of HTTP Clients that needs to be load balanced
    R{{}} HttpClient elected from the algorithm
}
public function roundRobin(LoadBalancerActions lb, CallerActions[] loadBalanceConfigArray) returns CallerActions {
    CallerActions httpClient = new;

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
function populateGenericLoadBalanceActionError(LoadBalanceActionError loadBalanceActionError)
                                                    returns error {
    int nErrs = lengthof loadBalanceActionError.httpActionErr;
    loadBalanceActionError.statusCode = INTERNAL_SERVER_ERROR_500;
    loadBalanceActionError.message = "All the load balance endpoints failed. Last error was: "
                                        + loadBalanceActionError.httpActionErr[nErrs - 1].message;
    error err = loadBalanceActionError;
    return err;
}
