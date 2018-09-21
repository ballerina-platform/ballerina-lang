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

# Provides the HTTP actions for interacting with an HTTP endpoint. This is created by wrapping the HTTP client
# to provide retrying over HTTP requests.
#
# + serviceUri - Target service url
# + config - HTTP ClientEndpointConfig to be used for HTTP client invocation
# + retryConfig - Configurations associated with retry
# + httpClient - HTTP client for outbound HTTP requests
public type RetryClient object {

    public string serviceUri;
    public ClientEndpointConfig config;
    public RetryConfig retryConfig;
    public CallerActions httpClient;

    # Provides the HTTP actions for interacting with an HTTP endpoint. This is created by wrapping the HTTP client
    # to provide retrying over HTTP requests.
    #
    # + serviceUri - Target service url
    # + config - HTTP ClientEndpointConfig to be used for HTTP client invocation
    # + retryConfig - Configurations associated with retry
    # + httpClient - HTTP client for outbound HTTP requests
    public new(serviceUri, config, retryConfig, httpClient) {}

    # The `post()` function wraps the underlying HTTP actions in a way to provide
    # retrying functionality for a given endpoint to recover from network level failures.
    #
    # + path - Resource path
    # + message - An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`, `io:ByteChannel` or `mime:Entity[]`
    # + return - The HTTP `Response` message, or an error if the invocation fails
    public function post(string path, Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|()
                                        message) returns Response|error;

    # The `head()` function wraps the underlying HTTP actions in a way to provide
    # retrying functionality for a given endpoint to recover from network level failures.
    #
    # + path - Resource path
    # + message - An optional HTTP outbound request message or or any payload of type `string`, `xml`, `json`, `byte[]`, `io:ByteChannel` or `mime:Entity[]`
    # + return - The HTTP `Response` message, or an error if the invocation fails
    public function head(string path, Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|()
                                        message = ()) returns Response|error;

    # The `put()` function wraps the underlying HTTP actions in a way to provide
    # retrying functionality for a given endpoint to recover from network level failures.
    #
    # + path - Resource path
    # + message - An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`, `io:ByteChannel` or `mime:Entity[]`
    # + return - The HTTP `Response` message, or an error if the invocation fails
    public function put(string path, Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|()
                                        message) returns Response|error;

    # The `forward()` function wraps the underlying HTTP actions in a way to provide retrying functionality
    # for a given endpoint with inbound request's HTTP verb to recover from network level failures.
    #
    # + path - Resource path
    # + request - An HTTP inbound request message
    # + return - The HTTP `Response` message, or an error if the invocation fails
    public function forward(string path, Request request) returns Response|error;

    # The `execute()` sends an HTTP request to a service with the specified HTTP verb. The function wraps the
    # underlying HTTP actions in a way to provide retrying functionality for a given endpoint to recover
    # from network level failures.
    #
    # + path - Resource path
    # + message - An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`, `io:ByteChannel` or `mime:Entity[]`
    # + return - The HTTP `Response` message, or an error if the invocation fails
    public function execute(string httpVerb, string path, Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|()
                                                            message) returns Response|error;

    # The `patch()` function wraps the undeline underlying HTTP actions in a way to provide
    # retrying functionality for a given endpoint to recover from network level failures.
    #
    # + path - Resource path
    # + message - An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`, `io:ByteChannel` or `mime:Entity[]`
    # + return - The HTTP `Response` message, or an error if the invocation fails
    public function patch(string path, Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|()
                                        message) returns Response|error;

    # The `delete()` function wraps the underlying HTTP actions in a way to provide
    # retrying functionality for a given endpoint to recover from network level failures.
    #
    # + path - Resource path
    # + message - An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`, `io:ByteChannel` or `mime:Entity[]`
    # + return - The HTTP `Response` message, or an error if the invocation fails
    public function delete(string path, Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|()
                                            message) returns Response|error;

    # The `get()` function wraps the underlying HTTP actions in a way to provide
    # retrying functionality for a given endpoint to recover from network level failures.
    #
    # + path - Resource path
    # + message - An optional HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`, `io:ByteChannel` or `mime:Entity[]`
    # + return - The HTTP `Response` message, or an error if the invocation fails
    public function get(string path, Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|()
                                        message = ()) returns Response|error;

    # The `options()` function wraps the underlying HTTP actions in a way to provide
    # retrying functionality for a given endpoint to recover from network level failures.
    #
    # + path - Resource path
    # + message - An optional HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`, `io:ByteChannel` or `mime:Entity[]`
    # + return - The HTTP `Response` message, or an error if the invocation fails
    public function options(string path, Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|()
                                            message = ()) returns Response|error;

    # Submits an HTTP request to a service with the specified HTTP verb.
	#cThe `submit()` function does not give out a `Response` as the result,
	#crather it returns an `HttpFuture` which can be used to do further interactions with the endpoint.
    #
    # + httpVerb - The HTTP verb value
    # + path - The resource path
    # + message - An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`, `io:ByteChannel` or `mime:Entity[]`
    # + return - An `HttpFuture` that represents an asynchronous service invocation, or an error if the submission fails
    public function submit(string httpVerb, string path,  Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|()
                                                            message) returns HttpFuture|error;

    # Retrieves the `Response` for a previously submitted request.
    #
    # + httpFuture - The `HttpFuture` relates to a previous asynchronous invocation
    # + return - An HTTP response message, or an error if the invocation fails
    public function getResponse(HttpFuture httpFuture) returns Response|error;

    # Checks whether a `PushPromise` exists for a previously submitted request.
    #
    # + httpFuture - The `HttpFuture` relates to a previous asynchronous invocation
    # + return - A `boolean` that represents whether a `PushPromise` exists
    public function hasPromise(HttpFuture httpFuture) returns (boolean);

    # Retrieves the next available `PushPromise` for a previously submitted request.
    #
    # + httpFuture - The `HttpFuture` relates to a previous asynchronous invocation
    # + return - An HTTP Push Promise message, or an error if the invocation fails
    public function getNextPromise(HttpFuture httpFuture) returns PushPromise|error;

    # Retrieves the promised server push `Response` message.
    #
    # + promise - The related `PushPromise`
    # + return - A promised HTTP `Response` message, or an error if the invocation fails
    public function getPromisedResponse(PushPromise promise) returns Response|error;

    # Rejects a `PushPromise`.
	# When a `PushPromise` is rejected, there is no chance of fetching a promised response using the rejected promise.
    #
    # + promise - The Push Promise to be rejected
    public function rejectPromise(PushPromise promise);
};

function RetryClient::post(string path, Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|()
                                                message) returns Response|error {
    Request req = buildRequest(message);
    return performRetryAction(path, req, HTTP_POST, self);
}

function RetryClient::head(string path, Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|()
                                                message = ()) returns Response|error {
    Request req = buildRequest(message);
    return performRetryAction(path, req, HTTP_HEAD, self);
}

function RetryClient::put(string path, Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|()
                                                message) returns Response|error {
    Request req = buildRequest(message);
    return performRetryAction(path, req, HTTP_PUT, self);
}

function RetryClient::forward(string path, Request request) returns Response|error {
    return performRetryAction(path, request, HTTP_FORWARD, self);
}

function RetryClient::execute(string httpVerb, string path, Request|string|xml|json|byte[]|io:ByteChannel
                                                                    |mime:Entity[]|() message) returns Response|error {
    Request req = buildRequest(message);
    return performRetryClientExecuteAction(path, req, httpVerb, self);
}

function RetryClient::patch(string path, Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|()
                                                    message) returns Response|error {
    Request req = buildRequest(message);
    return performRetryAction(path, req, HTTP_PATCH, self);
}

function RetryClient::delete(string path, Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|()
                                                    message) returns Response|error {
    Request req = buildRequest(message);
    return performRetryAction(path, req, HTTP_DELETE, self);
}

function RetryClient::get(string path, Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|()
                                                message = ()) returns Response|error {
    Request req = buildRequest(message);
    return performRetryAction(path, req, HTTP_GET, self);
}

function RetryClient::options(string path, Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|()
                                                    message = ()) returns Response|error {
    Request req = buildRequest(message);
    return performRetryAction(path, req, HTTP_OPTIONS, self);
}

function RetryClient::submit(string httpVerb, string path, Request|string|xml|json|byte[]|io:ByteChannel|
                                                                    mime:Entity[]|() message) returns HttpFuture|error {
    Request req = buildRequest(message);
    return self.httpClient.submit(httpVerb, path, req);
}

function RetryClient::getResponse(HttpFuture httpFuture) returns Response|error {
    return self.httpClient.getResponse(httpFuture);
}

function RetryClient::hasPromise(HttpFuture httpFuture) returns boolean {
    return self.httpClient.hasPromise(httpFuture);
}

function RetryClient::getNextPromise(HttpFuture httpFuture) returns PushPromise|error {
    return self.httpClient.getNextPromise(httpFuture);
}

function RetryClient::getPromisedResponse(PushPromise promise) returns Response|error {
    return self.httpClient.getPromisedResponse(promise);
}

function RetryClient::rejectPromise(PushPromise promise) {
    return self.httpClient.rejectPromise(promise);
}

// Performs execute action of the retry client. extract the corresponding http integer value representation
// of the http verb and invokes the perform action method.
function performRetryClientExecuteAction(@sensitive string path, Request request, @sensitive string httpVerb,
                                         RetryClient retryClient) returns Response|error {
    HttpOperation connectorAction = extractHttpOperation(httpVerb);
    return performRetryAction(path, request, connectorAction, retryClient);
}

// Handles all the actions exposed through the retry client.
function performRetryAction(@sensitive string path, Request request, HttpOperation requestAction,
                            RetryClient retryClient) returns Response|error {
    int currentRetryCount = 0;
    int retryCount = retryClient.retryConfig.count;
    int interval = retryClient.retryConfig.interval;
    float backOffFactor = retryClient.retryConfig.backOffFactor;
    int maxWaitInterval = retryClient.retryConfig.maxWaitInterval;
    if (backOffFactor <= 0.0) {
        backOffFactor = 1.0;
    }
    if (maxWaitInterval == 0) {
        maxWaitInterval = 60000;
    }
    CallerActions httpClient = retryClient.httpClient;
    Response response = new;
    //TODO : Initialize the record type correctly once it is fixed.
    error httpConnectorErr = {};
    Request inRequest = request;
    // When performing passthrough scenarios using retry client, message needs to be built before sending out the
    // to keep the request message to retry.
    var binaryPayload = check inRequest.getBinaryPayload();

    while (currentRetryCount < (retryCount + 1)) {
        inRequest = populateMultipartRequest(inRequest);
        var invokedEndpoint = invokeEndpoint(path, inRequest, requestAction, httpClient);
        match invokedEndpoint {
            Response backendResponse => {
                return backendResponse;
            }
            error errorResponse => {
                httpConnectorErr = errorResponse;
            }
        }
        if (currentRetryCount != 0) {
            interval = getWaitTime(backOffFactor, maxWaitInterval, interval);
        }
        runtime:sleep(interval);
        currentRetryCount = currentRetryCount + 1;
    }
    return httpConnectorErr;
}

function getWaitTime(float backOffFactor, int maxWaitTime, int interval) returns (int) {
    int waitTime = math:round(interval * backOffFactor);
    waitTime = waitTime > maxWaitTime ? maxWaitTime : waitTime;
    return waitTime;
}
