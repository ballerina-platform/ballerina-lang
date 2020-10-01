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

import ballerina/lang.'float;
import ballerina/runtime;

# Derived set of configurations from the `RetryConfig`.
#
# + count - Number of retry attempts before giving up
# + intervalInMillis - Retry interval in milliseconds
# + backOffFactor - Multiplier of the retry interval to exponentially increase retry interval
# + maxWaitIntervalInMillis - Maximum time of the retry interval in milliseconds
# + statusCodes - HTTP response status codes which are considered as failures
public type RetryInferredConfig record {|
    int count = 0;
    int intervalInMillis = 0;
    float backOffFactor = 0.0;
    int maxWaitIntervalInMillis = 0;
    boolean[] statusCodes = [];
|};

# Provides the HTTP remote functions for interacting with an HTTP endpoint. This is created by wrapping the HTTP client
# to provide retrying over HTTP requests.
#
# + url - Target service url
# + config - HTTP ClientConfiguration to be used for HTTP client invocation
# + retryInferredConfig - Derived set of configurations associated with retry
# + httpClient - Chain of different HTTP clients which provides the capability for initiating contact with a remote
#                HTTP service in resilient manner.
public client class RetryClient {

    public string url;
    public ClientConfiguration config;
    public RetryInferredConfig retryInferredConfig;
    public HttpClient httpClient;

    # Provides the HTTP remote functions for interacting with an HTTP endpoint. This is created by wrapping the HTTP
    # client to provide retrying over HTTP requests.
    #
    # + url - Target service url
    # + config - HTTP ClientConfiguration to be used for HTTP client invocation
    # + retryInferredConfig - Derived set of configurations associated with retry
    # + httpClient - HTTP client for outbound HTTP requests
    public function init(string url, ClientConfiguration config, RetryInferredConfig retryInferredConfig,
                                        HttpClient httpClient) {
        self.url = url;
        self.config = config;
        self.retryInferredConfig = retryInferredConfig;
        self.httpClient = httpClient;
    }

    # The `RetryClient.post()` function wraps the underlying HTTP remote functions in a way to provide
    # retrying functionality for a given endpoint to recover from network level failures.
    #
    # + path - Resource path
    # + message - An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - An `http:Response` message or else an `http:ClientError` if the invocation fails
    public remote function post(string path, RequestMessage message) returns @tainted Response|ClientError {
        var result = performRetryAction(path, <Request>message, HTTP_POST, self);
        if (result is HttpFuture) {
            return getInvalidTypeError();
        } else {
            return result;
        }
    }

    # The `RetryClient.head()` function wraps the underlying HTTP remote functions in a way to provide
    # retrying functionality for a given endpoint to recover from network level failures.
    #
    # + path - Resource path
    # + message - An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - An `http:Response` message or else an `http:ClientError` if the invocation fails
    public remote function head(string path, RequestMessage message = ()) returns @tainted Response|ClientError {
        var result = performRetryAction(path, <Request>message, HTTP_HEAD, self);
        if (result is HttpFuture) {
            return getInvalidTypeError();
        } else {
            return result;
        }
    }

    # The `RetryClient.put()` function wraps the underlying HTTP remote function in a way to provide
    # retrying functionality for a given endpoint to recover from network level failures.
    #
    # + path - Resource path
    # + message - An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - An `http:Response` message or else an `http:ClientError` if the invocation fails
    public remote function put(string path, RequestMessage message) returns @tainted Response|ClientError {
        var result = performRetryAction(path, <Request>message, HTTP_PUT, self);
        if (result is HttpFuture) {
            return getInvalidTypeError();
        } else {
            return result;
        }
    }

    # The `RetryClient.forward()` function wraps the underlying HTTP remote function in a way to provide retrying
    # functionality for a given endpoint with inbound request's HTTP verb to recover from network level failures.
    #
    # + path - Resource path
    # + request - An HTTP inbound request message
    # + return - An `http:Response` message or else an `http:ClientError` if the invocation fails
    public remote function forward(string path, Request request) returns @tainted Response|ClientError {
        var result = performRetryAction(path, request, HTTP_FORWARD, self);
        if (result is HttpFuture) {
            return getInvalidTypeError();
        } else {
            return result;
        }
    }

    # The `RetryClient.execute()` sends an HTTP request to a service with the specified HTTP verb. The function wraps
    # the underlying HTTP remote function in a way to provide retrying functionality for a given endpoint to recover
    # from network level failures.
    #
    # + httpVerb - The HTTP verb value
    # + path - Resource path
    # + message - An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel`, or `mime:Entity[]`
    # + return - An `http:Response` message or else an `http:ClientError` if the invocation fails
    public remote function execute(string httpVerb, string path, RequestMessage message) returns
            @tainted Response|ClientError {
        var result = performRetryClientExecuteAction(path, <Request>message, httpVerb, self);
        if (result is HttpFuture) {
            return getInvalidTypeError();
        } else {
            return result;
        }
    }

    # The `RetryClient.patch()` function wraps the underlying HTTP remote function in a way to provide
    # retrying functionality for a given endpoint to recover from network level failures.
    #
    # + path - Resource path
    # + message - An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel`, or `mime:Entity[]`
    # + return - An `http:Response` message or else an `http:ClientError` if the invocation fails
    public remote function patch(string path, RequestMessage message) returns @tainted Response|ClientError {
        var result = performRetryAction(path, <Request>message, HTTP_PATCH, self);
        if (result is HttpFuture) {
            return getInvalidTypeError();
        } else {
            return result;
        }
    }

    # The `RetryClient.delete()` function wraps the underlying HTTP remote function in a way to provide
    # retrying functionality for a given endpoint to recover from network level failures.
    #
    # + path - Resource path
    # + message - An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel`, or `mime:Entity[]`
    # + return - An `http:Response` message, or else an `http:ClientError` if the invocation fails
    public remote function delete(string path, RequestMessage message = ()) returns
            @tainted Response|ClientError {
        var result = performRetryAction(path, <Request>message, HTTP_DELETE, self);
        if (result is HttpFuture) {
            return getInvalidTypeError();
        } else {
            return result;
        }
    }

    # The `RetryClient.get()` function wraps the underlying HTTP remote function in a way to provide
    # retrying functionality for a given endpoint to recover from network level failures.
    #
    # + path - Resource path
    # + message - An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel`, or `mime:Entity[]`
    # + return - An `http:Response` message or else an `http:ClientError` if the invocation fails
    public remote function get(string path, RequestMessage message = ()) returns @tainted Response|ClientError {
        var result = performRetryAction(path, <Request>message, HTTP_GET, self);
        if (result is HttpFuture) {
            return getInvalidTypeError();
        } else {
            return result;
        }
    }

    # The `RetryClient.options()` function wraps the underlying HTTP remote function in a way to provide
    # retrying functionality for a given endpoint to recover from network level failures.
    #
    # + path - Resource path
    # + message - An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel`, or `mime:Entity[]`
    # + return - An `http:Response` message or else an `http:ClientError` if the invocation fails
    public remote function options(string path, RequestMessage message = ()) returns
            @tainted Response|ClientError {
        var result = performRetryAction(path, <Request>message, HTTP_OPTIONS, self);
        if (result is HttpFuture) {
            return getInvalidTypeError();
        } else {
            return result;
        }
    }

    # Submits an HTTP request to a service with the specified HTTP verb.
    # The `RetryClient.submit()` function does not give out a `http:Response` as the result.
    # Rather it returns an `http:HttpFuture`, which can be used to do further interactions with the endpoint.
    #
    # + httpVerb - The HTTP verb value
    # + path - The resource path
    # + message - An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel`, or `mime:Entity[]`
    # + return - An `http:HttpFuture` that represents an asynchronous service invocation or else an `http:ClientError` if the submission fails
    public remote function submit(string httpVerb, string path, RequestMessage message) returns
            @tainted HttpFuture|ClientError {
        var result = performRetryClientExecuteAction(path, <Request>message, HTTP_SUBMIT, self, verb = httpVerb);
        if (result is Response) {
            return getInvalidTypeError();
        } else {
            return result;
        }
    }

    # Retrieves the `http:Response` for a previously submitted request.
    #
    # + httpFuture - The `http:HttpFuture` related to a previous asynchronous invocation
    # + return - An `http:Response` message or else an `http:ClientError` if the invocation fails
    public remote function getResponse(HttpFuture httpFuture) returns Response|ClientError {
        // This need not be retried as the response is already checked when submit is called.
        return self.httpClient->getResponse(httpFuture);
    }

    # Checks whether an `http:PushPromise` exists for a previously-submitted request.
    #
    # + httpFuture - The `http:HttpFuture` related to a previous asynchronous invocation
    # + return - A `boolean`, which represents whether an `http:PushPromise` exists
    public remote function hasPromise(HttpFuture httpFuture) returns (boolean) {
        return self.httpClient->hasPromise(httpFuture);
    }

    # Retrieves the next available `http:PushPromise` for a previously-submitted request.
    #
    # + httpFuture - The `http:HttpFuture` related to a previous asynchronous invocation
    # + return - An `http:PushPromise` message or else an `http:ClientError` if the invocation fails
    public remote function getNextPromise(HttpFuture httpFuture) returns PushPromise|ClientError {
        return self.httpClient->getNextPromise(httpFuture);
    }

    # Retrieves the promised server push `http:Response` message.
    #
    # + promise - The related `http:PushPromise`
    # + return - A promised `http:Response` message or else an `http:ClientError` if the invocation fails
    public remote function getPromisedResponse(PushPromise promise) returns Response|ClientError {
        return self.httpClient->getPromisedResponse(promise);
    }

    # Rejects an `http:PushPromise`.
    # When an `http:PushPromise` is rejected, there is no chance of fetching a promised response using the rejected promise.
    #
    # + promise - The Push Promise to be rejected
    public remote function rejectPromise(PushPromise promise) {
        return self.httpClient->rejectPromise(promise);
    }
}


// Performs execute remote function of the retry client. extract the corresponding http integer value representation
// of the http verb and invokes the perform action method.
// verb is used for submit methods only.
function performRetryClientExecuteAction(@untainted string path, Request request, @untainted string httpVerb,
                                         RetryClient retryClient, string verb = "") returns @tainted HttpResponse|ClientError {
    HttpOperation connectorAction = extractHttpOperation(httpVerb);
    return performRetryAction(path, request, connectorAction, retryClient, verb = verb);
}

// Handles all the actions exposed through the retry client.
function performRetryAction(@untainted string path, Request request, HttpOperation requestAction,
                            RetryClient retryClient, string verb = "") returns @tainted HttpResponse|ClientError {
    HttpClient httpClient = retryClient.httpClient;
    int currentRetryCount = 0;
    int retryCount = retryClient.retryInferredConfig.count;
    int intervalInMillis = retryClient.retryInferredConfig.intervalInMillis;
    boolean[] statusCodeIndex = retryClient.retryInferredConfig.statusCodes;
    initializeBackOffFactorAndMaxWaitInterval(retryClient);
    
    AllRetryAttemptsFailed retryFailedError = AllRetryAttemptsFailed("All the retry attempts failed.");
    ClientError httpConnectorErr = retryFailedError;
    Request inRequest = request;
    // When performing passthrough scenarios using retry client, message needs to be built before sending out the
    // to keep the request message to retry.
    var binaryPayload = check inRequest.getBinaryPayload();

    while (currentRetryCount < (retryCount + 1)) {
        inRequest = check populateMultipartRequest(inRequest);
        var backendResponse = invokeEndpoint(path, inRequest, requestAction, httpClient, verb = verb);
        if (backendResponse is Response) {
            int responseStatusCode = backendResponse.statusCode;
            if (statusCodeIndex.length() > responseStatusCode && (statusCodeIndex[responseStatusCode])
                                                              && currentRetryCount < (retryCount)) {
                [intervalInMillis, currentRetryCount] =
                                calculateEffectiveIntervalAndRetryCount(retryClient, currentRetryCount, intervalInMillis);
            } else {
                return backendResponse;
            }
        } else if (backendResponse is HttpFuture) {
            var response = httpClient->getResponse(backendResponse);
            if (response is Response) {
                int responseStatusCode = response.statusCode;
                if (statusCodeIndex.length() > responseStatusCode && (statusCodeIndex[responseStatusCode])
                                                                  && currentRetryCount < (retryCount)) {
                    [intervalInMillis, currentRetryCount] =
                                    calculateEffectiveIntervalAndRetryCount(retryClient, currentRetryCount, intervalInMillis);
                } else {
                    // We return the HttpFuture object as this is called by submit method.
                    return backendResponse;
                }
            } else {
                [intervalInMillis, currentRetryCount] =
                                calculateEffectiveIntervalAndRetryCount(retryClient, currentRetryCount, intervalInMillis);
                httpConnectorErr = response;
            }
        } else {
            [intervalInMillis, currentRetryCount] =
                            calculateEffectiveIntervalAndRetryCount(retryClient, currentRetryCount, intervalInMillis);
            httpConnectorErr = backendResponse;
        }
        runtime:sleep(intervalInMillis);
    }
    return httpConnectorErr;
}

function initializeBackOffFactorAndMaxWaitInterval(RetryClient retryClient) {
    if (retryClient.retryInferredConfig.backOffFactor <= 0.0) {
        retryClient.retryInferredConfig.backOffFactor = 1.0;
    }
    if (retryClient.retryInferredConfig.maxWaitIntervalInMillis == 0) {
        retryClient.retryInferredConfig.maxWaitIntervalInMillis = 60000;
    }
}

function getWaitTime(float backOffFactor, int maxWaitTime, int interval) returns int {
    int waitTime = <int>'float:round(interval * backOffFactor);
    waitTime = waitTime > maxWaitTime ? maxWaitTime : waitTime;
    return waitTime;
}

function calculateEffectiveIntervalAndRetryCount(RetryClient retryClient, int currentRetryCount, int currentDelay)
                                                 returns [int, int] {
    int interval = currentDelay;
    if (currentRetryCount != 0) {
        interval = getWaitTime(retryClient.retryInferredConfig.backOffFactor,
                    retryClient.retryInferredConfig.maxWaitIntervalInMillis, interval);
    }
    int retryCount = currentRetryCount + 1;
    return [interval, retryCount];
}
