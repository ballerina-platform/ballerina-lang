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

# LoadBalanceClient endpoint provides load balancing functionality over multiple HTTP clients.
#
# + loadBalanceClientConfig - The configurations for the load balance client endpoint
# + loadBalanceClientsArray - Array of HTTP clients for load balancing
# + lbRule - Load balancing rule
# + failover - Whether to fail over in case of a failure
public type LoadBalanceClient client object {

    public LoadBalanceClientEndpointConfiguration loadBalanceClientConfig;
    public Client?[] loadBalanceClientsArray;
    public LoadBalancerRule lbRule;
    public boolean failover;

    # Load Balancer adds an additional layer to the HTTP client to make network interactions more resilient.
    #
    # + loadBalanceClientConfig - The configurations for the load balance client endpoint
    public function __init(LoadBalanceClientEndpointConfiguration loadBalanceClientConfig) {
        self.loadBalanceClientConfig = loadBalanceClientConfig;
        self.failover = loadBalanceClientConfig.failover;
        var lbClients = createLoadBalanceHttpClientArray(loadBalanceClientConfig);
        if (lbClients is error) {
            panic lbClients;
        } else {
            self.loadBalanceClientsArray = lbClients;
            var lbRule = loadBalanceClientConfig.lbRule;
            if (lbRule is LoadBalancerRule) {
                self.lbRule = lbRule;
            } else {
                LoadBalancerRounRobinRule loadBalancerRounRobinRule = new;
                self.lbRule = loadBalancerRounRobinRule;
            }
        }
    }

    # The POST remote function implementation of the LoadBalancer Connector.
    #
    # + path - Resource path
    # + message - An HTTP request or any payload of type `string`, `xml`, `json`, `byte[]`, `io:ReadableByteChannel`
    #             or `mime:Entity[]`
    # + return - The response or an `error` if failed to fulfill the request
    public remote function post(string path, RequestMessage message) returns Response|error;

    # The HEAD remote function implementation of the LoadBalancer Connector.
    #
    # + path - Resource path
    # + message - An optional HTTP request or any payload of type `string`, `xml`, `json`, `byte[]`, `io:ReadableByteChannel`
    #             or `mime:Entity[]`
    # + return - The response or an `error` if failed to fulfill the request
    public remote function head(string path, RequestMessage message = ()) returns Response|error;

    # The PATCH remote function implementation of the LoadBalancer Connector.
    #
    # + path - Resource path
    # + message - An HTTP request or any payload of type `string`, `xml`, `json`, `byte[]`, `io:ReadableByteChannel`
    #             or `mime:Entity[]`
    # + return - The response or an `error` if failed to fulfill the request
    public remote function patch(string path, RequestMessage message) returns Response|error;

    # The PUT remote function implementation of the Load Balance Connector.
    #
    # + path - Resource path
    # + message - An HTTP request or any payload of type `string`, `xml`, `json`, `byte[]`, `io:ReadableByteChannel`
    #             or `mime:Entity[]`
    # + return - The response or an `error` if failed to fulfill the request
    public remote function put(string path, RequestMessage message) returns Response|error;

    # The OPTIONS remote function implementation of the LoadBalancer Connector.
    #
    # + path - Resource path
    # + message - An optional HTTP request or any payload of type `string`, `xml`, `json`, `byte[]`, `io:ReadableByteChannel`
    #             or `mime:Entity[]`
    # + return - The response or an `error` if failed to fulfill the request
    public remote function options(string path, RequestMessage message = ()) returns Response|error;

    # The FORWARD remote function implementation of the LoadBalancer Connector.
    #
    # + path - Resource path
    # + request - An optional HTTP request
    # + return - The response or an `error` if failed to fulfill the request
    public remote function forward(string path, Request request) returns Response|error;

    # The EXECUTE remote function implementation of the LoadBalancer Connector.
    # The Execute remote function can be used to invoke an HTTP call with the given HTTP verb.
    #
    # + httpVerb - HTTP method to be used for the request
    # + path - Resource path
    # + message - An HTTP request or any payload of type `string`, `xml`, `json`, `byte[]`, `io:ReadableByteChannel`
    #             or `mime:Entity[]`
    # + return - The response or an `error` if failed to fulfill the request
    public remote function execute(string httpVerb, string path, RequestMessage message) returns Response|error;

    # The DELETE remote function implementation of the LoadBalancer Connector.
    #
    # + path - Resource path
    # + message - An HTTP request or any payload of type `string`, `xml`, `json`, `byte[]`, `io:ReadableByteChannel`
    #             or `mime:Entity[]`
    # + return - The response or an `error` if failed to fulfill the request
    public remote function delete(string path, RequestMessage message) returns Response|error;

    # The GET remote function implementation of the LoadBalancer Connector.

    # + path - Resource path
    # + message - An optional HTTP request or any payload of type `string`, `xml`, `json`, `byte[]`, `io:ReadableByteChannel`
    #             or `mime:Entity[]`
    # + return - The response or an `error` if failed to fulfill the request
    public remote function get(string path, RequestMessage message = ()) returns Response|error;

    # The submit implementation of the LoadBalancer Connector.
    #
    # + httpVerb - The HTTP verb value
    # + path - The resource path
    # + message - An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - An `HttpFuture` that represents an asynchronous service invocation, or an `error` if the submission fails
    public remote function submit(string httpVerb, string path, RequestMessage message) returns HttpFuture|error;

    # The getResponse implementation of the LoadBalancer Connector.
    #
    # + httpFuture - The `HttpFuture` related to a previous asynchronous invocation
    # + return - An HTTP response message, or an `error` if the invocation fails
    public remote function getResponse(HttpFuture httpFuture) returns Response|error;

    # The hasPromise implementation of the LoadBalancer Connector.
    #
    # + httpFuture - The `HttpFuture` relates to a previous asynchronous invocation
    # + return - A `boolean` that represents whether a `PushPromise` exists
    public remote function hasPromise(HttpFuture httpFuture) returns boolean;

    # The getNextPromise implementation of the LoadBalancer Connector.
    #
    # + httpFuture - The `HttpFuture` relates to a previous asynchronous invocation
    # + return - An HTTP Push Promise message, or an `error` if the invocation fails
    public remote function getNextPromise(HttpFuture httpFuture) returns PushPromise|error;

    # The getPromisedResponse implementation of the LoadBalancer Connector.
    #
    # + promise - The related `PushPromise`
    # + return - A promised HTTP `Response` message, or an `error` if the invocation fails
    public remote function getPromisedResponse(PushPromise promise) returns Response|error;

    # The rejectPromise implementation of the LoadBalancer Connector.
    #
    # + promise - The Push Promise to be rejected
    public remote function rejectPromise(PushPromise promise);
};

# Represents an error occurred in an remote function of the Load Balance connector.
#
# + message - An error message explaining about the error
# + statusCode - HTTP status code of the LoadBalanceActionError
# + httpActionErr - Array of errors occurred at each endpoint
public type LoadBalanceActionErrorData record {
    string message = "";
    int statusCode = 0;
    error?[] httpActionErr = [];
    !...;
};

public type LoadBalanceActionError error<string, LoadBalanceActionErrorData>;

public remote function LoadBalanceClient.post(string path, RequestMessage message) returns Response|error {
    Request req = buildRequest(message);
    return performLoadBalanceAction(self, path, req, HTTP_POST);
}

public remote function LoadBalanceClient.head(string path, RequestMessage message = ()) returns Response|error {
    Request req = buildRequest(message);
    return performLoadBalanceAction(self, path, req, HTTP_HEAD);
}

public remote function LoadBalanceClient.patch(string path, RequestMessage message) returns Response|error {
    Request req = buildRequest(message);
    return performLoadBalanceAction(self, path, req, HTTP_PATCH);
}

public remote function LoadBalanceClient.put(string path, RequestMessage message) returns Response|error {
    Request req = buildRequest(message);
    return performLoadBalanceAction(self, path, req, HTTP_PUT);
}

public remote function LoadBalanceClient.options(string path, RequestMessage message = ()) returns Response|error {
    Request req = buildRequest(message);
    return performLoadBalanceAction(self, path, req, HTTP_OPTIONS);
}

public remote function LoadBalanceClient.forward(string path, Request request) returns Response|error {
    return performLoadBalanceAction(self, path, request, HTTP_FORWARD);
}

public remote function LoadBalanceClient.execute(string httpVerb, string path, RequestMessage message) returns Response|error {
    Request req = buildRequest(message);
    return performLoadBalanceExecuteAction(self, path, req, httpVerb);
}

public remote function LoadBalanceClient.delete(string path, RequestMessage message) returns Response|error {
    Request req = buildRequest(message);
    return performLoadBalanceAction(self, path, req, HTTP_DELETE);
}

public remote function LoadBalanceClient.get(string path, RequestMessage message = ()) returns Response|error {
    Request req = buildRequest(message);
    return performLoadBalanceAction(self, path, req, HTTP_GET);
}

public remote function LoadBalanceClient.submit(string httpVerb, string path, RequestMessage message) returns HttpFuture|error {
    error err = error("Unsupported action for LoadBalancer client.");
    return err;
}

public remote function LoadBalanceClient.getResponse(HttpFuture httpFuture) returns Response|error {
    error err = error("Unsupported action for LoadBalancer client.");
    return err;
}

public remote function LoadBalanceClient.hasPromise(HttpFuture httpFuture) returns (boolean) {
    return false;
}

public remote function LoadBalanceClient.getNextPromise(HttpFuture httpFuture) returns PushPromise|error {
    error err = error("Unsupported action for LoadBalancer client.");
    return err;
}

public remote function LoadBalanceClient.getPromisedResponse(PushPromise promise) returns Response|error {
    error err = error("Unsupported action for LoadBalancer client.");
    return err;
}

public remote function LoadBalanceClient.rejectPromise(PushPromise promise) {
}

// Performs execute action of the Load Balance connector. extract the corresponding http integer value representation
// of the http verb and invokes the perform action method.
function performLoadBalanceExecuteAction(LoadBalanceClient lb, string path, Request request,
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
function performLoadBalanceAction(LoadBalanceClient lb, string path, Request request, HttpOperation requestAction)
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
        var loadBalanceClient = lb.lbRule.getNextClient(lb.loadBalanceClientsArray);
        if (loadBalanceClient is Client) {
            var serviceResponse = invokeEndpoint(path, request, requestAction, loadBalanceClient);
            if (serviceResponse is Response) {
                return serviceResponse;
            } else {
                if (lb.failover) {
                    loadBlancerInRequest = check createFailoverRequest(loadBlancerInRequest, requestEntity);
                    loadBalanceActionErrorData.httpActionErr[lbErrorIndex] = serviceResponse;
                    lbErrorIndex += 1;
                    loadBalanceTermination = loadBalanceTermination + 1;
                } else {
                    return serviceResponse;
                }
            }
        } else {
            return loadBalanceClient;
        }
    }
    return populateGenericLoadBalanceActionError(loadBalanceActionErrorData);
}

// Populates generic error specific to Load Balance connector by including all the errors returned from endpoints.
function populateGenericLoadBalanceActionError(LoadBalanceActionErrorData loadBalanceActionErrorData)
                                                    returns error {
    int nErrs = loadBalanceActionErrorData.httpActionErr.length();
    error? er = loadBalanceActionErrorData.httpActionErr[nErrs - 1];
    error actError;
    if (er is error) {
        actError = er;
    } else {
        error err = error("Unexpected nil");
        panic err;
    }
    string lastErrorMessage = <string> actError.detail().message;
    loadBalanceActionErrorData.statusCode = INTERNAL_SERVER_ERROR_500;
    loadBalanceActionErrorData.message = "All the load balance endpoints failed. Last error was: " + lastErrorMessage;
    LoadBalanceActionError err = error(HTTP_ERROR_CODE, loadBalanceActionErrorData);
    return err;
}


# The configurations related to the load balance client endpoint.
#
# + circuitBreaker - Circuit Breaker configuration
# + timeoutMillis - The maximum time to wait (in milli seconds) for a response before closing the connection
# + httpVersion - The HTTP version to be used to communicate with the endpoint
# + forwarded - The choice of setting forwarded/x-forwarded header
# + keepAlive - Specifies whether to keep the connection alive (or not) for multiple request/response pairs
# + chunking - The chunking behaviour of the request
# + followRedirects - Redirect related options
# + retryConfig - Retry related options
# + proxy - Proxy related options
# + connectionThrottling - The configurations for controlling the number of connections allowed concurrently
# + targets - The upstream HTTP endpoints among which the incoming HTTP traffic load should be distributed
# + cache - The configurations for controlling the caching behaviour
# + compression - Specifies the way of handling compression (`accept-encoding`) header
# + auth - HTTP authentication releated configurations
# + lbRule - LoadBalancing rule
# + failover - Configuration for load balancer whether to fail over in case of a failure
public type LoadBalanceClientEndpointConfiguration record {
    CircuitBreakerConfig? circuitBreaker = ();
    int timeoutMillis = 60000;
    string httpVersion = "1.1";
    string forwarded = "disable";
    KeepAlive keepAlive = KEEPALIVE_AUTO;
    Chunking chunking = "AUTO";
    FollowRedirects? followRedirects = ();
    RetryConfig? retryConfig = ();
    ProxyConfig? proxy = ();
    PoolConfiguration? poolConfig = ();
    TargetService[] targets = [];
    CacheConfig cache = {};
    Compression compression = COMPRESSION_AUTO;
    AuthConfig? auth = ();
    LoadBalancerRule? lbRule = ();
    boolean failover = true;
    !...;
};

function createClientEPConfigFromLoalBalanceEPConfig(LoadBalanceClientEndpointConfiguration lbConfig,
                                                     TargetService target) returns ClientEndpointConfig {
    ClientEndpointConfig clientEPConfig = {
        circuitBreaker:lbConfig.circuitBreaker,
        timeoutMillis:lbConfig.timeoutMillis,
        keepAlive:lbConfig.keepAlive,
        chunking:lbConfig.chunking,
        httpVersion:lbConfig.httpVersion,
        forwarded:lbConfig.forwarded,
        followRedirects:lbConfig.followRedirects,
        retryConfig:lbConfig.retryConfig,
        proxy:lbConfig.proxy,
        poolConfig:lbConfig.poolConfig,
        secureSocket:target.secureSocket,
        cache:lbConfig.cache,
        compression:lbConfig.compression,
        auth:lbConfig.auth
    };
    return clientEPConfig;
}

function createLoadBalanceHttpClientArray(LoadBalanceClientEndpointConfiguration loadBalanceClientConfig)
                                                                                    returns Client?[]|error {
    Client cl;
    Client?[] httpClients = [];
    int i = 0;
    foreach var target in loadBalanceClientConfig.targets {
        ClientEndpointConfig epConfig = createClientEPConfigFromLoalBalanceEPConfig(loadBalanceClientConfig, target);
        cl =  new(target.url , config = epConfig);
        httpClients[i] = cl;
        i += 1;
    }
    return httpClients;
}
