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
import ballerina/runtime;
import ballerina/io;

# Provides a set of configurations for controlling the failover behaviour of the endpoint.
#
# + failoverCodes - Array of HTTP response status codes for which the failover mechanism triggers
# + interval - Failover delay interval in milliseconds
public type FailoverConfig record {|
    int[] failoverCodes = [];
    int interval = 0;
|};

// TODO: This can be made package private
// Represents inferred failover configurations passed to Failover connector.
# Inferred failover configurations passed into the failover client.
#
# + failoverClientsArray - Array of HTTP Clients that needs to be Failover
# + failoverCodesIndex - An indexed array of HTTP response status codes for which the failover mechanism triggers
# + failoverInterval - Failover delay interval in milliseconds
public type FailoverInferredConfig record {|
    Client?[] failoverClientsArray = [];
    boolean[] failoverCodesIndex = [];
    int failoverInterval = 0;
|};

# An HTTP client endpoint which provides failover support over multiple HTTP clients.
#
# + failoverClientConfig - The configurations for the failover client endpoint
# + failoverInferredConfig - Configurations derived from `FailoverConfig`
# + succeededEndpointIndex - Index of the `CallerActions[]` array which given a successful response
public type FailoverClient client object {

    public FailoverClientEndpointConfiguration failoverClientConfig;
    public FailoverInferredConfig failoverInferredConfig;
    public int succeededEndpointIndex;

    # Failover caller actions which provides failover capabilities to an HTTP client endpoint.
    #
    # + config - The configurations of the client endpoint associated with this `Failover` instance
    public function __init(FailoverClientEndpointConfiguration failoverClientConfig) {
        self.failoverClientConfig = failoverClientConfig;
        self.succeededEndpointIndex = 0;
        var failoverHttpClientArray = createFailoverHttpClientArray(failoverClientConfig);
        if (failoverHttpClientArray is error) {
            panic failoverHttpClientArray;
        } else {
            Client?[] clients = failoverHttpClientArray;
            boolean[] failoverCodes = populateErrorCodeIndex(failoverClientConfig.failoverCodes);
            FailoverInferredConfig failoverInferredConfig = {
                failoverClientsArray:clients,
                failoverCodesIndex:failoverCodes,
                failoverInterval:failoverClientConfig.intervalMillis
            };
            self.failoverInferredConfig = failoverInferredConfig;
        }
    }

    # The POST remote function implementation of the Failover Connector.
    #
    # + path - Resource path
    # + message - HTTP request or any payload of type `string`, `xml`, `json`, `byte[]`, `io:ReadableByteChannel`
    #             or `mime:Entity[]`
    # + return - The response or an `error` if failed to fulfill the request
    public remote function post(string path, RequestMessage message) returns Response|error;

    # The HEAD remote function implementation of the Failover Connector.
    #
    # + path - Resource path
    # + message - An optional HTTP request or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - The response or an `error` if failed to fulfill the request
    public remote function head(string path, RequestMessage message = ()) returns Response|error;

    # The PATCH remote function implementation of the Failover Connector.
    #
    # + path - Resource path
    # + message - An HTTP request or any payload of type `string`, `xml`, `json`, `byte[]`, `io:ReadableByteChannel`
    #             or `mime:Entity[]`
    # + return - The response or an `error` if failed to fulfill the request
    public remote function patch(string path, RequestMessage message) returns Response|error;

    # The PUT remote function  implementation of the Failover Connector.
    #
    # + path - Resource path
    # + message - An HTTP request or any payload of type `string`, `xml`, `json`, `byte[]`, `io:ReadableByteChannel`
    #             or `mime:Entity[]`
    # + return - The response or an `error` if failed to fulfill the request
    public remote function put(string path, RequestMessage message) returns Response|error;

    # The OPTIONS remote function implementation of the Failover Connector.
    #
    # + path - Resource path
    # + message - An optional HTTP request or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - The response or an `error` if failed to fulfill the request
    public remote function options(string path, RequestMessage message = ()) returns Response|error;

    # Invokes an HTTP call using the incoming request's HTTP method.
    #
    # + path - Resource path
    # + request - An HTTP request
    # + return - The response or an `error` if failed to fulfill the request
    public remote function forward(string path, Request request) returns Response|error;

    # Invokes an HTTP call with the specified HTTP method.
    #
    # + httpVerb - HTTP method to be used for the request
    # + path - Resource path
    # + message - An HTTP request or any payload of type `string`, `xml`, `json`, `byte[]`, `io:ReadableByteChannel`
    #             or `mime:Entity[]`
    # + return - The response or an `error` if failed to fulfill the request
    public remote function execute(string httpVerb, string path, RequestMessage message) returns Response|error;

    # The DELETE remote function implementation of the Failover Connector.
    #
    # + path - Resource path
    # + message - An HTTP request or any payload of type `string`, `xml`, `json`, `byte[]`, `io:ReadableByteChannel`
    #             or `mime:Entity[]`
    # + return - The response or an `error` if failed to fulfill the request
    public remote function delete(string path, RequestMessage message) returns Response|error;

    # The GET remote function implementation of the Failover Connector.
    #
    # + path - Resource path
    # + message - An optional HTTP request or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - The response or an `error` if failed to fulfill the request
    public remote function get(string path, RequestMessage message = ()) returns Response|error;

    # Submits an HTTP request to a service with the specified HTTP verb. The `submit()` function does not return
    # a `Response` as the result, rather it returns an `HttpFuture` which can be used for subsequent interactions
    # with the HTTP endpoint.
    #
    # + httpVerb - The HTTP verb value
    # + path - The resource path
    # + message - An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - An `HttpFuture` that represents an asynchronous service invocation, or an `error` if the submission
    #            fails
    public remote function submit(string httpVerb, string path, RequestMessage message) returns HttpFuture|error;

    # Retrieves the `Response` for a previously submitted request.
    #
    # + httpFuture - The `HttpFuture` related to a previous asynchronous invocation
    # + return - An HTTP response message, or an `error` if the invocation fails
    public remote function getResponse(HttpFuture httpFuture) returns Response|error;

    # Checks whether a `PushPromise` exists for a previously submitted request.
    #
    # + httpFuture - The `HttpFuture` relates to a previous asynchronous invocation
    # + return - A `boolean` that represents whether a `PushPromise` exists
    public remote function hasPromise(HttpFuture httpFuture) returns boolean;

    # Retrieves the next available `PushPromise` for a previously submitted request.
    #
    # + httpFuture - The `HttpFuture` relates to a previous asynchronous invocation
    # + return - An HTTP Push Promise message, or an `error` if the invocation fails
    public remote function getNextPromise(HttpFuture httpFuture) returns PushPromise|error;

    # Retrieves the promised server push `Response` message.
    #
    # + promise - The related `PushPromise`
    # + return - A promised HTTP `Response` message, or an `error` if the invocation fails
    public remote function getPromisedResponse(PushPromise promise) returns Response|error;

    # Rejects a `PushPromise`. When a `PushPromise` is rejected, there is no chance of fetching a promised
    # response using the rejected promise.
    #
    # + promise - The Push Promise to be rejected
    public remote function rejectPromise(PushPromise promise);
};

public remote function FailoverClient.post(string path, RequestMessage message) returns Response|error {
    Request req = buildRequest(message);
    var result = performFailoverAction(path, req, HTTP_POST, self);
    if (result is HttpFuture) {
        return getInvalidTypeError();
    } else {
        return result;
    }
}

public remote function FailoverClient.head(string path, RequestMessage message = ()) returns Response|error {
    Request req = buildRequest(message);
    var result = performFailoverAction(path, req, HTTP_HEAD, self);
    if (result is HttpFuture) {
        return getInvalidTypeError();
    } else {
        return result;
    }
}

public remote function FailoverClient.patch(string path, RequestMessage message) returns Response|error {
    Request req = buildRequest(message);
    var result = performFailoverAction(path, req, HTTP_PATCH, self);
    if (result is HttpFuture) {
        return getInvalidTypeError();
    } else {
        return result;
    }
}

public remote function FailoverClient.put(string path, RequestMessage message) returns Response|error {
    Request req = buildRequest(message);
    var result = performFailoverAction(path, req, HTTP_PUT, self);
    if (result is HttpFuture) {
        return getInvalidTypeError();
    } else {
        return result;
    }
}

public remote function FailoverClient.options(string path, RequestMessage message = ()) returns Response|error {
    Request req = buildRequest(message);
    var result = performFailoverAction(path, req, HTTP_OPTIONS, self);
    if (result is HttpFuture) {
        return getInvalidTypeError();
    } else {
        return result;
    }
}

public remote function FailoverClient.forward(string path, Request request) returns Response|error {
    var result = performFailoverAction(path, request, HTTP_FORWARD, self);
    if (result is HttpFuture) {
        return getInvalidTypeError();
    } else {
        return result;
    }
}

public remote function FailoverClient.execute(string httpVerb, string path, RequestMessage message) returns
                                                                                                Response|error {

    Request req = buildRequest(message);
    var result = performExecuteAction(path, req, httpVerb, self);
    if (result is HttpFuture) {
        return getInvalidTypeError();
    } else {
        return result;
    }
}

public remote function FailoverClient.delete(string path, RequestMessage message) returns Response|error {
    Request req = buildRequest(message);
    var result = performFailoverAction(path, req, HTTP_DELETE, self);
    if (result is HttpFuture) {
        return getInvalidTypeError();
    } else {
        return result;
    }
}

public remote function FailoverClient.get(string path, RequestMessage message = ()) returns Response|error {
    Request req = buildRequest(message);
    var result = performFailoverAction(path, req, HTTP_GET, self);
    if (result is HttpFuture) {
        return getInvalidTypeError();
    } else {
        return result;
    }
}

public remote function FailoverClient.submit(string httpVerb, string path, RequestMessage message)
                                                                                returns HttpFuture|error {

    Request req = buildRequest(message);
    var result = performExecuteAction(path, req, "SUBMIT", self, verb = httpVerb);
    if (result is Response) {
        return getInvalidTypeError();
    } else {
        return result;
    }
}

public remote function FailoverClient.getResponse(HttpFuture httpFuture) returns Response|error {
    Client foClient = getLastSuceededClientEP(self);
    return foClient->getResponse(httpFuture);
}

public remote function FailoverClient.hasPromise(HttpFuture httpFuture) returns (boolean) {
    return false;
}

public remote function FailoverClient.getNextPromise(HttpFuture httpFuture) returns PushPromise|error {
    error err = error("Unsupported action for Failover client.");
    return err;
}

public remote function FailoverClient.getPromisedResponse(PushPromise promise) returns Response|error {
    error err = error("Unsupported action for Failover client.");
    return err;
}

public remote function FailoverClient.rejectPromise(PushPromise promise) {
}

// Performs execute action of the Failover connector. extract the corresponding http integer value representation
// of the http verb and invokes the perform action method.
// `verb` is used for HTTP `submit()` method.
function performExecuteAction (string path, Request request, string httpVerb,
                                    FailoverClient failoverClient, string verb = "") returns HttpResponse|error {
    HttpOperation connectorAction = extractHttpOperation(httpVerb);
    return performFailoverAction(path, request, connectorAction, failoverClient, verb = verb);
}

// Handles all the actions exposed through the Failover connector.
function performFailoverAction (string path, Request request, HttpOperation requestAction,
                                        FailoverClient failoverClient, string verb = "") returns HttpResponse|error {

    Client foClient = getLastSuceededClientEP(failoverClient);
    FailoverInferredConfig failoverInferredConfig = failoverClient.failoverInferredConfig;
    Client?[] failoverClients = failoverInferredConfig.failoverClientsArray;
    boolean[] failoverCodeIndex = failoverInferredConfig.failoverCodesIndex;
    int noOfEndpoints = (failoverInferredConfig.failoverClientsArray.length());
    // currentIndex and initialIndex are need to set to last succeeded endpoint index to start failover with
    // the endpoint which gave the expected results.
    int currentIndex = failoverClient.succeededEndpointIndex;
    int initialIndex = failoverClient.succeededEndpointIndex;
    int startIndex = -1;
    int failoverInterval = failoverInferredConfig.failoverInterval;

    Response inResponse = new;
    HttpFuture inFuture = new;
    Request failoverRequest = request;
    error?[] failoverActionErrData = [];
    mime:Entity requestEntity = new;

    if (isMultipartRequest(failoverRequest)) {
        failoverRequest = check populateMultipartRequest(failoverRequest);
    } else {
        // When performing passthrough scenarios using Failover connector, message needs to be built before trying
        // out the failover endpoints to keep the request message to failover the messages.
        var binaryPayload = failoverRequest.getBinaryPayload();
        requestEntity = check failoverRequest.getEntity();
    }
    while (startIndex != currentIndex) {
        startIndex = initialIndex;
        currentIndex = currentIndex + 1;
        var endpointResponse = invokeEndpoint(path, failoverRequest, requestAction, foClient, verb = verb);
        if (endpointResponse is Response) {
            inResponse = endpointResponse;
            int httpStatusCode = endpointResponse.statusCode;
            // Check whether HTTP status code of the response falls into configured `failoverCodes`
            if (failoverCodeIndex[httpStatusCode]) {
                error? result = ();
                [currentIndex, result] = handleResponseWithErrorCode(endpointResponse, initialIndex, noOfEndpoints,
                                                                                currentIndex, failoverActionErrData);
                if (result is error) {
                    return result;
                }
            } else {
                // If the execution reaches here, that means the first endpoint configured in the failover endpoints
                // gives the expected response.
                failoverClient.succeededEndpointIndex = currentIndex - 1;
                break;
            }
        } else if (endpointResponse is HttpFuture) {
            // Response came from the `submit()` method.
            inFuture = endpointResponse;
            var futureResponse = foClient->getResponse(endpointResponse);
            if (futureResponse is Response) {
                inResponse = futureResponse;
                int httpStatusCode = futureResponse.statusCode;
                // Check whether HTTP status code of the response falls into configured `failoverCodes`
                if (failoverCodeIndex[httpStatusCode]) {
                    error? result = ();
                    [currentIndex, result] = handleResponseWithErrorCode(futureResponse, initialIndex, noOfEndpoints,
                                                                                currentIndex, failoverActionErrData);
                    if (result is error) {
                        return result;
                    }
                } else {
                    // If the execution reaches here, that means the first endpoint configured in the failover endpoints
                    // gives the expected response.
                    failoverClient.succeededEndpointIndex = currentIndex - 1;
                    break;
                }
            } else {
                error? httpConnectorErr = ();
                [currentIndex, httpConnectorErr] = handleError(futureResponse, initialIndex, noOfEndpoints,
                                                                                currentIndex, failoverActionErrData);

                if (httpConnectorErr is error) {
                    return httpConnectorErr;
                }
            }
        } else {
            error? httpConnectorErr = ();
            [currentIndex, httpConnectorErr] = handleError(endpointResponse, initialIndex, noOfEndpoints,
                                                                                currentIndex, failoverActionErrData);

            if (httpConnectorErr is error) {
                return httpConnectorErr;
            }
        }
        failoverRequest = check createFailoverRequest(failoverRequest, requestEntity);
        runtime:sleep(failoverInterval);

        var tmpClnt = failoverClients[currentIndex];
        if (tmpClnt is Client) {
            foClient = tmpClnt;
        } else {
            error err = error("Unexpected type found for failover client.");
            panic err;
        }
    }
    if (HTTP_SUBMIT == requestAction) {
        return inFuture;
    }
    return inResponse;
}

// Populates generic error specific to Failover connector by including all the errors returned from endpoints.
function populateGenericFailoverActionError (error?[] failoverActionErr, error httpActionErr, int index)
           returns (error) {
    failoverActionErr[index] = httpActionErr;
    string lastErrorMsg = <string> httpActionErr.detail().message;
    string failoverMessage = "All the failover endpoints failed. Last error was " + lastErrorMsg;
    error actionError = error(HTTP_ERROR_CODE, message = failoverMessage, failoverErrors = failoverActionErr);
    return actionError;
}

// If leaf endpoint returns a response with status code configured to retry in the failover connector, failover error
// will be generated with last response status code and generic failover response.
function populateFailoverErrorHttpStatusCodes (Response inResponse, error?[] failoverActionErr, int index) {
    string failoverMessage = "Endpoint " + index + " returned response is: " + inResponse.statusCode + " " +
        inResponse.reasonPhrase;
    error httpActionErr = error(HTTP_ERROR_CODE, message = failoverMessage);
    failoverActionErr[index] = httpActionErr;
}

function populateErrorsFromLastResponse (Response inResponse, error?[] failoverActionErr, int index)
                                                                            returns (error) {
    string message = "Last endpoint returned response: " + inResponse.statusCode + " " + inResponse.reasonPhrase;
    error lastHttpConnectorErr = error(HTTP_ERROR_CODE, message = message);
    failoverActionErr[index] = lastHttpConnectorErr;
    string failoverMessage = "All the failover endpoints failed. Last endpoint returned response is: "
                                + inResponse.statusCode + " " + inResponse.reasonPhrase;
    error actionError = error(HTTP_ERROR_CODE, message = failoverMessage, failoverErrors = failoverActionErr);
    return actionError;
}

# Provides a set of HTTP related configurations and failover related configurations.
#
# + httpVersion - The HTTP version to be used to communicate with the endpoint
# + http1Settings - Configurations related to HTTP/1.x protocol
# + http2Settings - Configurations related to HTTP/2 protocol
# + timeoutMillis - The maximum time to wait (in milliseconds) for a response before closing the connection
# + forwarded - The choice of setting `forwarded`/`x-forwarded` header
# + followRedirects - Redirect related options
# + poolConfig - Configurations associated with request pooling
# + proxy - Proxy related options
# + targets - The upstream HTTP endpoints among which the incoming HTTP traffic load should be sent on failover
# + cache - The configurations for controlling the caching behaviour
# + compression - Specifies the way of handling compression (`accept-encoding`) header
# + auth - HTTP authentication releated configurations
# + circuitBreaker - Circuit Breaker behaviour configurations
# + retryConfig - Retry related options
# + failoverCodes - Array of HTTP response status codes for which the failover behaviour should be triggered
# + intervalMillis - Failover delay interval in milliseconds
public type FailoverClientEndpointConfiguration record {|
    string httpVersion = HTTP_1_1;
    Http1Settings http1Settings = {};
    Http2Settings http2Settings = {};
    int timeoutMillis = 60000;
    string forwarded = "disable";
    FollowRedirects? followRedirects = ();
    ProxyConfig? proxy = ();
    PoolConfiguration? poolConfig = ();
    TargetService[] targets = [];
    CacheConfig cache = {};
    Compression compression = COMPRESSION_AUTO;
    OutboundAuthConfig? auth = ();
    CircuitBreakerConfig? circuitBreaker = ();
    RetryConfig? retryConfig = ();
    int[] failoverCodes = [501, 502, 503, 504];
    int intervalMillis = 0;
|};

function createClientEPConfigFromFailoverEPConfig(FailoverClientEndpointConfiguration foConfig,
                                                  TargetService target) returns ClientEndpointConfig {
    ClientEndpointConfig clientEPConfig = {
        http1Settings: foConfig.http1Settings,
        http2Settings: foConfig.http2Settings,
        circuitBreaker:foConfig.circuitBreaker,
        timeoutMillis:foConfig.timeoutMillis,
        httpVersion:foConfig.httpVersion,
        forwarded:foConfig.forwarded,
        followRedirects:foConfig.followRedirects,
        retryConfig:foConfig.retryConfig,
        proxy:foConfig.proxy,
        poolConfig:foConfig.poolConfig,
        secureSocket:target.secureSocket,
        cache:foConfig.cache,
        compression:foConfig.compression,
        auth:foConfig.auth
    };
    return clientEPConfig;
}

function createFailoverHttpClientArray(FailoverClientEndpointConfiguration failoverClientConfig)
    returns Client?[]|error {
    Client clientEp;
    Client?[] httpClients = [];
    int i = 0;

    foreach var target in failoverClientConfig.targets {
        ClientEndpointConfig epConfig = createClientEPConfigFromFailoverEPConfig(failoverClientConfig, target);
        clientEp = new(target.url, config = epConfig);
        httpClients[i] = clientEp;
        i += 1;
    }
    return httpClients;
}

function getLastSuceededClientEP(FailoverClient failoverClient) returns Client {
    var lastSuccessClient = failoverClient.failoverInferredConfig
                                            .failoverClientsArray[failoverClient.succeededEndpointIndex];
    if (lastSuccessClient is Client) {
        // We dont have to check this again as we already check for the response when we get the future.
        return lastSuccessClient;
    } else {
        error err = error("Unexpected type found for failover client");
        panic err;
    }
}

function handleResponseWithErrorCode(Response response, int initialIndex, int noOfEndpoints, int index,
                                                            error?[] failoverActionErrData) returns [int, error?] {

    error? resultError = ();
    int currentIndex = index;
    // If the initialIndex == DEFAULT_FAILOVER_EP_STARTING_INDEX check successful, that means the first
    // endpoint configured in the failover endpoints gave the response where its HTTP status code
    // falls into configured `failoverCodes`
    if (initialIndex == DEFAULT_FAILOVER_EP_STARTING_INDEX) {
        if (noOfEndpoints > currentIndex) {
            // If the execution lands here, that means there are endpoints that haven't tried out by
            // failover endpoint. Hence response will be collected to generate final response.
            populateFailoverErrorHttpStatusCodes(response, failoverActionErrData, currentIndex - 1);
        } else {
            // If the execution lands here, that means all the endpoints has been tried out and final
            // endpoint gave the response where its HTTP status code falls into configured
            // `failoverCodes`. Therefore appropriate error message needs to be generated and should
            // return it to the client.
            resultError = populateErrorsFromLastResponse(response, failoverActionErrData, currentIndex - 1);
        }
    } else {
        // If execution reaches here, that means failover has not started with the default starting index.
        // Failover resumed from the last succeeded endpoint.
        if (initialIndex == currentIndex) {
            // If the execution lands here, that means all the endpoints has been tried out and final
            // endpoint gives the response where its HTTP status code falls into configured
            // `failoverCodes`. Therefore appropriate error message needs to be generated and should
            // return it to the client.
            resultError = populateErrorsFromLastResponse(response, failoverActionErrData, currentIndex - 1);
        } else if (noOfEndpoints == currentIndex) {
            // If the execution lands here, that means the last endpoint has been tried out and
            // endpoint gave a response where its HTTP status code falls into configured
            // `failoverCodes`. Since failover resumed from the last succeeded endpoint we need try out
            // remaining endpoints. Therefore currentIndex need to be reset.
            populateFailoverErrorHttpStatusCodes(response, failoverActionErrData, currentIndex - 1);
            currentIndex = DEFAULT_FAILOVER_EP_STARTING_INDEX;
        } else if (noOfEndpoints > currentIndex) {
            // Collect the response to generate final response.
            populateFailoverErrorHttpStatusCodes(response, failoverActionErrData, currentIndex - 1);
        }
    }
    return [currentIndex, resultError];
}

function handleError(error response, int initialIndex, int noOfEndpoints, int index, error?[] failoverActionErrData)
                                                                                                returns [int, error?] {
    error? httpConnectorErr = ();
    int currentIndex = index;
    // If the initialIndex == DEFAULT_FAILOVER_EP_STARTING_INDEX check successful, that means the first
    // endpoint configured in the failover endpoints gave the erroneous response.
    if (initialIndex == DEFAULT_FAILOVER_EP_STARTING_INDEX) {
        if (noOfEndpoints > currentIndex) {
            // If the execution lands here, that means there are endpoints that haven't tried out by
            // failover endpoint. Hence error will be collected to generate final response.
            failoverActionErrData[currentIndex - 1] = response;
        } else {
            // If the execution lands here, that means all the endpoints has been tried out and final
            // endpoint gave an erroneous response. Therefore appropriate error message needs to be
            //  generated and should return it to the client.
            httpConnectorErr = populateGenericFailoverActionError(failoverActionErrData, response, currentIndex - 1);
        }
    } else {
        // If execution reaches here, that means failover has not started with the default starting index.
        // Failover resumed from the last succeeded endpoint.
        if (initialIndex == currentIndex) {
            // If the execution lands here, that means all the endpoints has been tried out and final
            // endpoint gave an erroneous response. Therefore appropriate error message needs to be
            //  generated and should return it to the client.
            httpConnectorErr = populateGenericFailoverActionError(failoverActionErrData, response, currentIndex - 1);
        } else if (noOfEndpoints == currentIndex) {
            // If the execution lands here, that means the last endpoint has been tried out and endpoint gave
            // a erroneous response. Since failover resumed from the last succeeded endpoint we need try out
            // remaining endpoints. Therefore currentIndex need to be reset.
            failoverActionErrData[currentIndex - 1] = response;
            currentIndex = DEFAULT_FAILOVER_EP_STARTING_INDEX;
        } else if (noOfEndpoints > currentIndex) {
            // Collect the error to generate final response.
            failoverActionErrData[currentIndex - 1] = response;
        }
    }
    return [currentIndex, httpConnectorErr];
}
