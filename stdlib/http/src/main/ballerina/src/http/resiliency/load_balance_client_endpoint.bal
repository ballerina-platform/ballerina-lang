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

import ballerina/mime;

# LoadBalanceClient endpoint provides load balancing functionality over multiple HTTP clients.
#
# + loadBalanceClientConfig - The configurations for the load balance client endpoint
# + loadBalanceClientsArray - Array of HTTP clients for load balancing
# + lbRule - Load balancing rule
# + failover - Whether to fail over in case of a failure
public client class LoadBalanceClient {

    public LoadBalanceClientConfiguration loadBalanceClientConfig;
    public Client?[] loadBalanceClientsArray;
    public LoadBalancerRule lbRule;
    public boolean failover;

    # Load Balancer adds an additional layer to the HTTP client to make network interactions more resilient.
    #
    # + loadBalanceClientConfig - The configurations for the load balance client endpoint
    public function init(LoadBalanceClientConfiguration loadBalanceClientConfig) {
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
                LoadBalancerRoundRobinRule loadBalancerRoundRobinRule = new;
                self.lbRule = loadBalancerRoundRobinRule;
            }
        }
    }

    # The POST remote function implementation of the LoadBalancer Connector.
    #
    # + path - Resource path
    # + message - An HTTP request or any payload of type `string`, `xml`, `json`, `byte[]`, `io:ReadableByteChannel`
    #             or `mime:Entity[]`
    # + return - The response or an `http:ClientError` if failed to fulfill the request
    public remote function post(string path, RequestMessage message) returns Response|ClientError {
        Request req = buildRequest(message);
        return performLoadBalanceAction(self, path, req, HTTP_POST);
    }

    # The HEAD remote function implementation of the LoadBalancer Connector.
    #
    # + path - Resource path
    # + message - An optional HTTP request or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - The response or an `http:ClientError` if failed to fulfill the request
    public remote function head(string path, RequestMessage message = ()) returns Response|ClientError {
        Request req = buildRequest(message);
        return performLoadBalanceAction(self, path, req, HTTP_HEAD);
    }

    # The PATCH remote function implementation of the LoadBalancer Connector.
    #
    # + path - Resource path
    # + message - An HTTP request or any payload of type `string`, `xml`, `json`, `byte[]`, `io:ReadableByteChannel`
    #             or `mime:Entity[]`
    # + return - The response or an `http:ClientError` if failed to fulfill the request
    public remote function patch(string path, RequestMessage message) returns Response|ClientError {
        Request req = buildRequest(message);
        return performLoadBalanceAction(self, path, req, HTTP_PATCH);
    }

    # The PUT remote function implementation of the Load Balance Connector.
    #
    # + path - Resource path
    # + message - An HTTP request or any payload of type `string`, `xml`, `json`, `byte[]`, `io:ReadableByteChannel`
    #             or `mime:Entity[]`
    # + return - The response or an `http:ClientError` if failed to fulfill the request
    public remote function put(string path, RequestMessage message) returns Response|ClientError {
        Request req = buildRequest(message);
        return performLoadBalanceAction(self, path, req, HTTP_PUT);
    }

    # The OPTIONS remote function implementation of the LoadBalancer Connector.
    #
    # + path - Resource path
    # + message - An optional HTTP request or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - The response or an `http:ClientError` if failed to fulfill the request
    public remote function options(string path, RequestMessage message = ()) returns Response|ClientError {
        Request req = buildRequest(message);
        return performLoadBalanceAction(self, path, req, HTTP_OPTIONS);
    }

    # The FORWARD remote function implementation of the LoadBalancer Connector.
    #
    # + path - Resource path
    # + request - An optional HTTP request
    # + return - The response or an `http:ClientError` if failed to fulfill the request
    public remote function forward(string path, Request request) returns Response|ClientError {
        return performLoadBalanceAction(self, path, request, HTTP_FORWARD);
    }

    # The EXECUTE remote function implementation of the LoadBalancer Connector.
    # The Execute remote function can be used to invoke an HTTP call with the given HTTP verb.
    #
    # + httpVerb - HTTP method to be used for the request
    # + path - Resource path
    # + message - An HTTP request or any payload of type `string`, `xml`, `json`, `byte[]`, `io:ReadableByteChannel`
    #             or `mime:Entity[]`
    # + return - The response or an `http:ClientError` if failed to fulfill the request
    public remote function execute(string httpVerb, string path, RequestMessage message) returns Response|ClientError {
        Request req = buildRequest(message);
        return performLoadBalanceExecuteAction(self, path, req, httpVerb);
    }

    # The DELETE remote function implementation of the LoadBalancer Connector.
    #
    # + path - Resource path
    # + message - An HTTP request or any payload of type `string`, `xml`, `json`, `byte[]`, `io:ReadableByteChannel`
    #             or `mime:Entity[]`
    # + return - The response or an `http:ClientError` if failed to fulfill the request
    public remote function delete(string path, RequestMessage message = ()) returns Response|ClientError {
        Request req = buildRequest(message);
        return performLoadBalanceAction(self, path, req, HTTP_DELETE);
    }

    # The GET remote function implementation of the LoadBalancer Connector.

    # + path - Resource path
    # + message - An optional HTTP request or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - The response or an `http:ClientError` if failed to fulfill the request
    public remote function get(string path, RequestMessage message = ()) returns Response|ClientError {
        Request req = buildRequest(message);
        return performLoadBalanceAction(self, path, req, HTTP_GET);
    }

    # The submit implementation of the LoadBalancer Connector.
    #
    # + httpVerb - The HTTP verb value
    # + path - The resource path
    # + message - An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel`, or `mime:Entity[]`
    # + return - An `http:HttpFuture` that represents an asynchronous service invocation or else an `http:ClientError` if the submission
    #            fails
    public remote function submit(string httpVerb, string path, RequestMessage message) returns HttpFuture|ClientError {
        return UnsupportedActionError("Load balancer client not supported for submit action");
    }

    # The getResponse implementation of the LoadBalancer Connector.
    #
    # + httpFuture - The `http:HttpFuture` related to a previous asynchronous invocation
    # + return - An `http:Response` message or else an `http:ClientError` if the invocation fails
    public remote function getResponse(HttpFuture httpFuture) returns Response|ClientError {
        return UnsupportedActionError("Load balancer client not supported for getResponse action");
    }

    # The hasPromise implementation of the LoadBalancer Connector.
    #
    # + httpFuture - The `http:HttpFuture` related to a previous asynchronous invocation
    # + return - A `boolean`, which represents whether an `http:PushPromise` exists
    public remote function hasPromise(HttpFuture httpFuture) returns boolean {
        return false;
    }

    # The getNextPromise implementation of the LoadBalancer Connector.
    #
    # + httpFuture - The `http:HttpFuture` related to a previous asynchronous invocation
    # + return - An `http:PushPromise` message or else an `http:ClientError` if the invocation fails
    public remote function getNextPromise(HttpFuture httpFuture) returns PushPromise|ClientError {
        return UnsupportedActionError("Load balancer client not supported for getNextPromise action");
    }

    # The getPromisedResponse implementation of the LoadBalancer Connector.
    #
    # + promise - The related `http:PushPromise`
    # + return - A promised `http:Response` message or else an `http:ClientError` if the invocation fails
    public remote function getPromisedResponse(PushPromise promise) returns Response|ClientError {
        return UnsupportedActionError("Load balancer client not supported for getPromisedResponse action");
    }

    # The rejectPromise implementation of the LoadBalancer Connector.
    #
    # + promise - The Push Promise to be rejected
    public remote function rejectPromise(PushPromise promise) {}
}

# Represents the error attributes in addition to the message and the cause of the `LoadBalanceActionError`.
#
# + httpActionErr - Array of errors occurred at each endpoint
public type LoadBalanceActionErrorData record {|
    error?[] httpActionErr = [];
|};

# Represents an error occurred in an remote function of the Load Balance connector.
public type LoadBalanceActionError distinct error<LoadBalanceActionErrorData>;

// Performs execute action of the Load Balance connector. extract the corresponding http integer value representation
// of the http verb and invokes the perform action method.
function performLoadBalanceExecuteAction(LoadBalanceClient lb, string path, Request request,
                                         string httpVerb) returns Response|ClientError {
    HttpOperation connectorAction = extractHttpOperation(httpVerb);
    if (connectorAction != HTTP_NONE) {
        return performLoadBalanceAction(lb, path, request, connectorAction);
    } else {
        return UnsupportedActionError("Load balancer client not supported for http method: " + httpVerb);
    }
}

// Handles all the actions exposed through the Load Balance connector.
function performLoadBalanceAction(LoadBalanceClient lb, string path, Request request, HttpOperation requestAction)
             returns Response|ClientError {
    int loadBalanceTermination = 0; // Tracks at which point failover within the load balancing should be terminated.
    //TODO: workaround to initialize a type inside a function. Change this once fix is available.
    LoadBalanceActionErrorData loadBalanceActionErrorData = {httpActionErr:[]};
    int lbErrorIndex = 0;
    Request loadBalancerInRequest = request;
    mime:Entity requestEntity = new;

    if (lb.failover) {
        if (isMultipartRequest(loadBalancerInRequest)) {
            loadBalancerInRequest = check populateMultipartRequest(loadBalancerInRequest);
        } else {
            // When performing passthrough scenarios using Load Balance connector,
            // message needs to be built before trying out the load balance endpoints to keep the request message
            // to load balance the messages in case of failure.
            var binaryPayload = loadBalancerInRequest.getBinaryPayload();
            requestEntity = check loadBalancerInRequest.getEntity();
        }
    }

    while (loadBalanceTermination < lb.loadBalanceClientsArray.length()) {
        var loadBalanceClient = lb.lbRule.getNextClient(lb.loadBalanceClientsArray);
        if (loadBalanceClient is Client) {
            var serviceResponse = invokeEndpoint(path, request, requestAction, loadBalanceClient);
            if (serviceResponse is Response) {
                return serviceResponse;
            } else if (serviceResponse is HttpFuture) {
                return getInvalidTypeError();
            } else {
                if (lb.failover) {
                    loadBalancerInRequest = check createFailoverRequest(loadBalancerInRequest, requestEntity);
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
                                                    returns ClientError {
    int nErrs = loadBalanceActionErrorData.httpActionErr.length();
    error? lastError = loadBalanceActionErrorData.httpActionErr[nErrs - 1];
    if (lastError is ()) {
        panic error("Unexpected nil");
    }

    error actError = <error> lastError;
    string lastErrorMessage = actError.message();
    string message = "All the load balance endpoints failed. Last error was: " + lastErrorMessage;
    return AllLoadBalanceEndpointsFailedError(message, httpActionError = loadBalanceActionErrorData.httpActionErr);
}


# The configurations related to the load balance client endpoint. Following fields are inherited from the other
# configuration records in addition to the load balance client specific configs.
#
# |                                                         |
# |:------------------------------------------------------- |
# | httpVersion - Copied from CommonClientConfiguration     |
# | http1Settings - Copied from CommonClientConfiguration   |
# | http2Settings - Copied from CommonClientConfiguration   |
# | timeoutInMillis - Copied from CommonClientConfiguration |
# | forwarded - Copied from CommonClientConfiguration       |
# | followRedirects - Copied from CommonClientConfiguration |
# | poolConfig - Copied from CommonClientConfiguration      |
# | cache - Copied from CommonClientConfiguration           |
# | compression - Copied from CommonClientConfiguration     |
# | auth - Copied from CommonClientConfiguration            |
# | circuitBreaker - Copied from CommonClientConfiguration  |
# | retryConfig - Copied from CommonClientConfiguration     |
# | cookieConfig - Copied from CommonClientConfiguration    |
#
# + targets - The upstream HTTP endpoints among which the incoming HTTP traffic load should be distributed
# + lbRule - LoadBalancing rule
# + failover - Configuration for load balancer whether to fail over in case of a failure
public type LoadBalanceClientConfiguration record {|
    *CommonClientConfiguration;
    TargetService[] targets = [];
    LoadBalancerRule? lbRule = ();
    boolean failover = true;
|};

function createClientEPConfigFromLoalBalanceEPConfig(LoadBalanceClientConfiguration lbConfig,
                                                     TargetService target) returns ClientConfiguration {
    ClientConfiguration clientEPConfig = {
        http1Settings: lbConfig.http1Settings,
        http2Settings: lbConfig.http2Settings,
        circuitBreaker:lbConfig.circuitBreaker,
        timeoutInMillis:lbConfig.timeoutInMillis,
        httpVersion:lbConfig.httpVersion,
        forwarded:lbConfig.forwarded,
        followRedirects:lbConfig.followRedirects,
        retryConfig:lbConfig.retryConfig,
        poolConfig:lbConfig.poolConfig,
        secureSocket:target.secureSocket,
        cache:lbConfig.cache,
        compression:lbConfig.compression,
        auth:lbConfig.auth
    };
    return clientEPConfig;
}

function createLoadBalanceHttpClientArray(LoadBalanceClientConfiguration loadBalanceClientConfig)
                                                                                    returns Client?[]|error {
    Client cl;
    Client?[] httpClients = [];
    int i = 0;
    foreach var target in loadBalanceClientConfig.targets {
        ClientConfiguration epConfig = createClientEPConfigFromLoalBalanceEPConfig(loadBalanceClientConfig, target);
        cl =  new(target.url , epConfig);
        httpClients[i] = cl;
        i += 1;
    }
    return httpClients;
}
