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

import ballerina/io;
import ballerina/math;
import ballerina/mime;
import ballerina/runtime;

# Derived set of configurations from the `RetryConfig`.
#
# + count - Number of retry attempts before giving up
# + interval - Retry interval in milliseconds
# + backOffFactor - Multiplier of the retry interval to exponentailly increase retry interval
# + maxWaitInterval - Maximum time of the retry interval in milliseconds
# + statusCodes - HTTP response status codes which are considered as failures
public type RetryInferredConfig record {
    int count = 0;
    int interval = 0;
    float backOffFactor = 0.0;
    int maxWaitInterval = 0;
    boolean[] statusCodes = [];
    !...;
};

# Provides the HTTP remote functions for interacting with an HTTP endpoint. This is created by wrapping the HTTP client
# to provide retrying over HTTP requests.
#
# + url - Target service url
# + config - HTTP ClientEndpointConfig to be used for HTTP client invocation
# + retryInferredConfig - Derived set of configurations associated with retry
# + httpClient - Chain of different HTTP clients which provides the capability for initiating contact with a remote
#                HTTP service in resilient manner.
public type RetryClient client object {

    public string url;
    public ClientEndpointConfig config;
    public RetryInferredConfig retryInferredConfig;
    public Client httpClient;

    # Provides the HTTP remote functions for interacting with an HTTP endpoint. This is created by wrapping the HTTP client
    # to provide retrying over HTTP requests.
    #
    # + url - Target service url
    # + config - HTTP ClientEndpointConfig to be used for HTTP client invocation
    # + retryInferredConfig - Derived set of configurations associated with retry
    # + httpClient - HTTP client for outbound HTTP requests
    public function __init(string url, ClientEndpointConfig config, RetryInferredConfig retryInferredConfig,
                                        Client httpClient) {
        self.url = url;
        self.config = config;
        self.retryInferredConfig = retryInferredConfig;
        self.httpClient = httpClient;
    }

    # The `post()` function wraps the underlying HTTP remote functions in a way to provide
    # retrying functionality for a given endpoint to recover from network level failures.
    #
    # + path - Resource path
    # + message - An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - The HTTP `Response` message, or an error if the invocation fails
    public remote function post(string path, RequestMessage message) returns Response|error;

    # The `head()` function wraps the underlying HTTP remote functions in a way to provide
    # retrying functionality for a given endpoint to recover from network level failures.
    #
    # + path - Resource path
    # + message - An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - The HTTP `Response` message, or an error if the invocation fails
    public remote function head(string path, RequestMessage message = ()) returns Response|error;

    # The `put()` function wraps the underlying HTTP remote function in a way to provide
    # retrying functionality for a given endpoint to recover from network level failures.
    #
    # + path - Resource path
    # + message - An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - The HTTP `Response` message, or an error if the invocation fails
    public remote function put(string path, RequestMessage message) returns Response|error;

    # The `forward()` function wraps the underlying HTTP remote function in a way to provide retrying functionality
    # for a given endpoint with inbound request's HTTP verb to recover from network level failures.
    #
    # + path - Resource path
    # + request - An HTTP inbound request message
    # + return - The HTTP `Response` message, or an error if the invocation fails
    public remote function forward(string path, Request request) returns Response|error;

    # The `execute()` sends an HTTP request to a service with the specified HTTP verb. The function wraps the
    # underlying HTTP remote function in a way to provide retrying functionality for a given endpoint to recover
    # from network level failures.
    #
    # + path - Resource path
    # + message - An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - The HTTP `Response` message, or an error if the invocation fails
    public remote function execute(string httpVerb, string path, RequestMessage message) returns Response|error;

    # The `patch()` function wraps the undeline underlying HTTP remote function in a way to provide
    # retrying functionality for a given endpoint to recover from network level failures.
    #
    # + path - Resource path
    # + message - An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - The HTTP `Response` message, or an error if the invocation fails
    public remote function patch(string path, RequestMessage message) returns Response|error;

    # The `delete()` function wraps the underlying HTTP remote function in a way to provide
    # retrying functionality for a given endpoint to recover from network level failures.
    #
    # + path - Resource path
    # + message - An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - The HTTP `Response` message, or an error if the invocation fails
    public remote function delete(string path, RequestMessage message) returns Response|error;

    # The `get()` function wraps the underlying HTTP remote function in a way to provide
    # retrying functionality for a given endpoint to recover from network level failures.
    #
    # + path - Resource path
    # + message - An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - The HTTP `Response` message, or an error if the invocation fails
    public remote function get(string path, RequestMessage message = ()) returns Response|error;

    # The `options()` function wraps the underlying HTTP remote function in a way to provide
    # retrying functionality for a given endpoint to recover from network level failures.
    #
    # + path - Resource path
    # + message - An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - The HTTP `Response` message, or an error if the invocation fails
    public remote function options(string path, RequestMessage message = ()) returns Response|error;

    # Submits an HTTP request to a service with the specified HTTP verb.
	#cThe `submit()` function does not give out a `Response` as the result,
	#crather it returns an `HttpFuture` which can be used to do further interactions with the endpoint.
    #
    # + httpVerb - The HTTP verb value
    # + path - The resource path
    # + message - An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - An `HttpFuture` that represents an asynchronous service invocation, or an error if the submission fails
    public remote function submit(string httpVerb, string path, RequestMessage message) returns HttpFuture|error;

    # Retrieves the `Response` for a previously submitted request.
    #
    # + httpFuture - The `HttpFuture` relates to a previous asynchronous invocation
    # + return - An HTTP response message, or an error if the invocation fails
    public remote function getResponse(HttpFuture httpFuture) returns Response|error;

    # Checks whether a `PushPromise` exists for a previously submitted request.
    #
    # + httpFuture - The `HttpFuture` relates to a previous asynchronous invocation
    # + return - A `boolean` that represents whether a `PushPromise` exists
    public remote function hasPromise(HttpFuture httpFuture) returns (boolean);

    # Retrieves the next available `PushPromise` for a previously submitted request.
    #
    # + httpFuture - The `HttpFuture` relates to a previous asynchronous invocation
    # + return - An HTTP Push Promise message, or an error if the invocation fails
    public remote function getNextPromise(HttpFuture httpFuture) returns PushPromise|error;

    # Retrieves the promised server push `Response` message.
    #
    # + promise - The related `PushPromise`
    # + return - A promised HTTP `Response` message, or an error if the invocation fails
    public remote function getPromisedResponse(PushPromise promise) returns Response|error;

    # Rejects a `PushPromise`.
	# When a `PushPromise` is rejected, there is no chance of fetching a promised response using the rejected promise.
    #
    # + promise - The Push Promise to be rejected
    public remote function rejectPromise(PushPromise promise);
};

public remote function RetryClient.post(string path, RequestMessage message) returns Response|error {
    return performRetryAction(path, <Request>message, HTTP_POST, self);
}

public remote function RetryClient.head(string path, RequestMessage message = ()) returns Response|error {
    return performRetryAction(path, <Request>message, HTTP_HEAD, self);
}

public remote function RetryClient.put(string path, RequestMessage message) returns Response|error {
    return performRetryAction(path, <Request>message, HTTP_PUT, self);
}

public remote function RetryClient.forward(string path, Request request) returns Response|error {
    return performRetryAction(path, request, HTTP_FORWARD, self);
}

public remote function RetryClient.execute(string httpVerb, string path, RequestMessage message) returns Response|error {
    return performRetryClientExecuteAction(path, <Request>message, httpVerb, self);
}

public remote function RetryClient.patch(string path, RequestMessage message) returns Response|error {
    return performRetryAction(path, <Request>message, HTTP_PATCH, self);
}

public remote function RetryClient.delete(string path, RequestMessage message) returns Response|error {
    return performRetryAction(path, <Request>message, HTTP_DELETE, self);
}

public remote function RetryClient.get(string path, RequestMessage message = ()) returns Response|error {
    return performRetryAction(path, <Request>message, HTTP_GET, self);
}

public remote function RetryClient.options(string path, RequestMessage message = ()) returns Response|error {
    return performRetryAction(path, <Request>message, HTTP_OPTIONS, self);
}

public remote function RetryClient.submit(string httpVerb, string path, RequestMessage message) returns HttpFuture|error {
    return self.httpClient->submit(httpVerb, path, <Request>message);
}

public remote function RetryClient.getResponse(HttpFuture httpFuture) returns Response|error {
    return self.httpClient->getResponse(httpFuture);
}

public remote function RetryClient.hasPromise(HttpFuture httpFuture) returns boolean {
    return self.httpClient->hasPromise(httpFuture);
}

public remote function RetryClient.getNextPromise(HttpFuture httpFuture) returns PushPromise|error {
    return self.httpClient->getNextPromise(httpFuture);
}

public remote function RetryClient.getPromisedResponse(PushPromise promise) returns Response|error {
    return self.httpClient->getPromisedResponse(promise);
}

public remote function RetryClient.rejectPromise(PushPromise promise) {
    return self.httpClient->rejectPromise(promise);
}

// Performs execute remote function of the retry client. extract the corresponding http integer value representation
// of the http verb and invokes the perform action method.
function performRetryClientExecuteAction(@sensitive string path, Request request, @sensitive string httpVerb,
                                         RetryClient retryClient) returns Response|error {
    HttpOperation connectorAction = extractHttpOperation(httpVerb);
    return performRetryAction(path, request, connectorAction, retryClient);
}

// Handles all the actions exposed through the retry client.
function performRetryAction(@sensitive string path, Request request, HttpOperation requestAction,
                            RetryClient retryClient) returns Response|error {
    Client httpClient = retryClient.httpClient;
    int currentRetryCount = 0;
    int retryCount = retryClient.retryInferredConfig.count;
    int interval = retryClient.retryInferredConfig.interval;
    boolean[] statusCodeIndex = retryClient.retryInferredConfig.statusCodes;

    if (retryClient.retryInferredConfig.backOffFactor <= 0.0) {
        retryClient.retryInferredConfig.backOffFactor = 1.0;
    }
    if (retryClient.retryInferredConfig.maxWaitInterval == 0) {
        retryClient.retryInferredConfig.maxWaitInterval = 60000;
    }
    Response response = new;
    //TODO : Initialize the record type correctly once it is fixed.
    error httpConnectorErr = error("http connection err");
    Request inRequest = request;
    // When performing passthrough scenarios using retry client, message needs to be built before sending out the
    // to keep the request message to retry.
    var binaryPayload = check inRequest.getBinaryPayload();

    while (currentRetryCount < (retryCount + 1)) {
        inRequest = check populateMultipartRequest(inRequest);
        var backendResponse = invokeEndpoint(path, inRequest, requestAction, httpClient);
        if (backendResponse is Response) {
            int responseStatusCode = backendResponse.statusCode;
            if (statusCodeIndex.length() > responseStatusCode && (statusCodeIndex[responseStatusCode] == true)
                                                              && currentRetryCount < (retryCount)) {
                (interval, currentRetryCount) =
                                calculateEffectiveIntervalAndRetryCount(retryClient, currentRetryCount, interval);
            } else {
                return backendResponse;
            }
        } else {
            (interval, currentRetryCount) =
                            calculateEffectiveIntervalAndRetryCount(retryClient, currentRetryCount, interval);
            httpConnectorErr = backendResponse;
        }
        runtime:sleep(interval);
    }
    return httpConnectorErr;
}

function getWaitTime(float backOffFactor, int maxWaitTime, int interval) returns int {
    int waitTime = math:round(interval * backOffFactor);
    waitTime = waitTime > maxWaitTime ? maxWaitTime : waitTime;
    return waitTime;
}

function calculateEffectiveIntervalAndRetryCount(RetryClient retryClient, int currentRetryCount, int currentDelay)
                                                 returns (int, int) {
    int interval = currentDelay;
    if (currentRetryCount != 0) {
        interval = getWaitTime(retryClient.retryInferredConfig.backOffFactor,
                    retryClient.retryInferredConfig.maxWaitInterval, interval);
    }
    int retryCount = currentRetryCount + 1;
    return (interval, retryCount);
}
