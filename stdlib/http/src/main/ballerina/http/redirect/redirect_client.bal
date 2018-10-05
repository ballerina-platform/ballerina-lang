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
import ballerina/runtime;
import ballerina/mime;
import ballerina/log;
import ballerina/math;
import ballerina/config;

# Provides redirect functionality for HTTP client actions.
#
# + serviceUri - Target service url
# + config - HTTP ClientEndpointConfig to be used for HTTP client invocation
# + redirectConfig - Configurations associated with redirect
# + httpClient - HTTP client for outbound HTTP requests
# + currentRedirectCount - Current redirect count of the HTTP client
public type RedirectClient object {

    public string serviceUri;
    public ClientEndpointConfig config;
    public FollowRedirects redirectConfig;
    public CallerActions httpClient;
    public int currentRedirectCount = 0;

    # Create a redirect client with the given configurations.
    #
    # + serviceUri - Target service url
    # + config - HTTP ClientEndpointConfig to be used for HTTP client invocation
    # + redirectConfig - Configurations associated with redirect
    # + httpClient - HTTP client for outbound HTTP requests
    public new(serviceUri, config, redirectConfig, httpClient) {
        self.serviceUri = serviceUri;
        self.config = config;
        self.redirectConfig = redirectConfig;
        self.httpClient = httpClient;
    }

    # If the received response for the `get()` action is redirect eligible, redirect will be performed automatically
    # by this `get()` function.
    #
    # + path - Resource path
    # + message - An optional HTTP outbound request message or any payload of type `string`, `xml`, `json`,
    #             `byte[]`, `io:ByteChannel` or `mime:Entity[]`
    # + return - The HTTP `Response` message, or an error if the invocation fails
    public function get(string path, Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|()
                                        message = ()) returns Response|error {
        Request request = buildRequest(message);
        return performRedirectIfEligible(self, path, request, HTTP_GET);
    }

    # If the received response for the `post()` action is redirect eligible, redirect will be performed automatically
    # by this `post()` function.
    #
    # + path - Resource path
    # + message - An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ByteChannel` or `mime:Entity[]`
    # + return - The HTTP `Response` message, or an error if the invocation fails
    public function post(string path, Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|()
                                        message) returns Response|error {
        Request request = buildRequest(message);
        return performRedirectIfEligible(self, path, request, HTTP_POST);
    }

    # If the received response for the `head()` action is redirect eligible, redirect will be performed automatically
    # by this `head()` function.
    #
    # + path - Resource path
    # + message - An optional HTTP outbound request message or or any payload of type `string`, `xml`, `json`,
    #             `byte[]`, `io:ByteChannel` or `mime:Entity[]`
    # + return - The HTTP `Response` message, or an error if the invocation fails
    public function head(string path, Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|()
                                        message = ()) returns Response|error {
        Request request = buildRequest(message);
        return performRedirectIfEligible(self, path, request, HTTP_HEAD);
    }

    # If the received response for the `put()` action is redirect eligible, redirect will be performed automatically
    # by this `put()` function.
    #
    # + path - Resource path
    # + message - An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ByteChannel` or `mime:Entity[]`
    # + return - The HTTP `Response` message, or an error if the invocation fails
    public function put(string path, Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|()
                                        message) returns Response|error {
        Request request = buildRequest(message);
        return performRedirectIfEligible(self, path, request, HTTP_PUT);
    }

    # The `forward()` function is used to invoke an HTTP call with inbound request's HTTP verb.
    #
    # + path - Resource path
    # + request - An HTTP inbound request message
    # + return - The HTTP `Response` message, or an error if the invocation fails
    public function forward(string path, Request request) returns Response|error {
        return self.httpClient.forward(path, request);
    }

    # The `execute()` sends an HTTP request to a service with the specified HTTP verb. Redirect will be performed
    # only for HTTP methods.
    #
    # + path - Resource path
    # + message - An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ByteChannel` or `mime:Entity[]`
    # + return - The HTTP `Response` message, or an error if the invocation fails
    public function execute(string httpVerb, string path, Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|()
                                                            message) returns Response|error {
        Request request = buildRequest(message);
        //Redirection is performed only for HTTP methods
        if (HTTP_NONE == extractHttpOperation(httpVerb)) {
            return self.httpClient.execute(httpVerb, path, request);
        } else {
            return performRedirectIfEligible(self, path, request, extractHttpOperation(httpVerb));
        }
    }

    # If the received response for the `patch()` action is redirect eligible, redirect will be performed automatically
    # by this `patch()` function.
    #
    # + path - Resource path
    # + message - An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ByteChannel` or `mime:Entity[]`
    # + return - The HTTP `Response` message, or an error if the invocation fails
    public function patch(string path, Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|()
                                            message) returns Response|error {
        Request request = buildRequest(message);
        return performRedirectIfEligible(self, path, request, HTTP_PATCH);
    }

    # If the received response for the `delete()` action is redirect eligible, redirect will be performed automatically
    # by this `delete()` function.
    #
    # + path - Resource path
    # + message - An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ByteChannel` or `mime:Entity[]`
    # + return - The HTTP `Response` message, or an error if the invocation fails
    public function delete(string path, Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|()
                                            message) returns Response|error {
        Request request = buildRequest(message);
        return performRedirectIfEligible(self, path, request, HTTP_DELETE);
    }

    # If the received response for the `options()` action is redirect eligible, redirect will be performed automatically
    # by this `options()` function.
    #
    # + path - Resource path
    # + message - An optional HTTP outbound request message or any payload of type `string`, `xml`, `json`,
    #             `byte[]`, `io:ByteChannel` or `mime:Entity[]`
    # + return - The HTTP `Response` message, or an error if the invocation fails
    public function options(string path, Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|()
                                            message = ()) returns Response|error {
        Request request = buildRequest(message);
        return performRedirectIfEligible(self, path, request, HTTP_OPTIONS);
    }

    # Submits an HTTP request to a service with the specified HTTP verb.
    # The `submit()` function does not give out a `Response` as the result,
    # rather it returns an `HttpFuture` which can be used to do further interactions with the endpoint.
    #
    # + httpVerb - The HTTP verb value
    # + path - The resource path
    # + message - An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ByteChannel` or `mime:Entity[]`
    # + return - An `HttpFuture` that represents an asynchronous service invocation, or an error if the submission fails
    public function submit(string httpVerb, string path, Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|()
                                                message) returns HttpFuture|error {
        Request request = buildRequest(message);
        return self.httpClient.submit(httpVerb, path, request);
    }

    # Retrieves the `Response` for a previously submitted request.
    #
    # + httpFuture - The `HttpFuture` relates to a previous asynchronous invocation
    # + return - An HTTP response message, or an error if the invocation fails
    public function getResponse(HttpFuture httpFuture) returns Response|error {
        return self.httpClient.getResponse(httpFuture);
    }

    # Checks whether a `PushPromise` exists for a previously submitted request.
    #
    # + httpFuture - The `HttpFuture` relates to a previous asynchronous invocation
    # + return - A `boolean` that represents whether a `PushPromise` exists
    public function hasPromise(HttpFuture httpFuture) returns (boolean) {
        return self.httpClient.hasPromise(httpFuture);
    }

    # Retrieves the next available `PushPromise` for a previously submitted request.
    #
    # + httpFuture - The `HttpFuture` relates to a previous asynchronous invocation
    # + return - An HTTP Push Promise message, or an error if the invocation fails
    public function getNextPromise(HttpFuture httpFuture) returns PushPromise|error {
        return self.httpClient.getNextPromise(httpFuture);
    }

    # Retrieves the promised server push `Response` message.
    #
    # + promise - The related `PushPromise`
    # + return - A promised HTTP `Response` message, or an error if the invocation fails
    public function getPromisedResponse(PushPromise promise) returns Response|error {
        return self.httpClient.getPromisedResponse(promise);
    }

    # Rejects a `PushPromise`.
    # When a `PushPromise` is rejected, there is no chance of fetching a promised response using the rejected promise.
    #
    # + promise - The Push Promise to be rejected
    public function rejectPromise(PushPromise promise) {
        self.httpClient.rejectPromise(promise);
    }
};

//Invoke relevant HTTP client action and check the response for redirect eligibility.
function performRedirectIfEligible(RedirectClient redirectClient, string path, Request request,
                                   HttpOperation httpOperation) returns Response|error {
    string originalUrl = redirectClient.serviceUri + path;
    log:printDebug("Checking redirect eligibility for original request " + originalUrl);
    Response|error result = invokeEndpoint(path, request, httpOperation, redirectClient.httpClient);
    return checkRedirectEligibility(result, originalUrl, httpOperation, request, redirectClient);
}

//Inspect the response for redirect eligibility.
function checkRedirectEligibility(Response|error result, string resolvedRequestedURI, HttpOperation httpVerb, Request
    request, RedirectClient redirectClient) returns @untainted Response|error {
    match result {
        Response response => {
            if (isRedirectResponse(response.statusCode)) {
                return redirect(response, httpVerb, request, redirectClient, resolvedRequestedURI);
            } else {
                setCountAndResolvedURL(redirectClient, response, resolvedRequestedURI);
                return response;
            }
        }
        error err => {
            redirectClient.currentRedirectCount = 0;
            return err;
        }
    }
}

//Check the response status for redirect eligibility.
function isRedirectResponse(int statusCode) returns boolean {
    log:printDebug("Response Code : " + statusCode);
    return (statusCode == 300 || statusCode == 301 || statusCode == 302 || statusCode == 303 || statusCode == 305 ||
        statusCode == 307 || statusCode == 308);
}

//If max redirect count is not reached, perform redirection.
function redirect(Response response, HttpOperation httpVerb, Request request, RedirectClient redirectClient,
                  string resolvedRequestedURI) returns @untainted Response|error {
    int currentCount = redirectClient.currentRedirectCount;
    int maxCount = redirectClient.redirectConfig.maxCount;
    if (currentCount >= maxCount) {
        log:printDebug("Maximum redirect count reached!");
        setCountAndResolvedURL(redirectClient, response, resolvedRequestedURI);
        return response;
    } else {
        currentCount++;
        log:printDebug("Redirect count : " + currentCount);
        redirectClient.currentRedirectCount = currentCount;
        match getRedirectMethod(httpVerb, response) {
            () => {
                setCountAndResolvedURL(redirectClient, response, resolvedRequestedURI);
                return response;
            }
            HttpOperation redirectMethod => {
                if (response.hasHeader(LOCATION)) {
                    string location = response.getHeader(LOCATION);
                    log:printDebug("Location header value: " + location);
                    if (!isAbsolute(location)) {
                        match resolve(resolvedRequestedURI, location) {
                            string resolvedURI => {
                                return performRedirection(resolvedURI, redirectClient, redirectMethod, request,
                                    response);
                            }
                            error err => {
                                redirectClient.currentRedirectCount = 0;
                                return err;
                            }
                        }
                    } else {
                        return performRedirection(location, redirectClient, redirectMethod, request, response);
                    }
                } else {
                    redirectClient.currentRedirectCount = 0;
                    error err = { message: "Location header not available!" };
                    return err;
                }
            }
        }
    }
}

function performRedirection(string location, RedirectClient redirectClient, HttpOperation redirectMethod,
                                       Request request, Response response) returns @untainted Response|error {
    log:printDebug("Redirect using new clientEP : " + location);
    CallerActions newCallerAction = createRetryClient(location,
        createNewEndpoint(location, redirectClient.config));
    Response|error result = invokeEndpoint("", createRedirectRequest(response.statusCode, request),
        redirectMethod, newCallerAction);
    return checkRedirectEligibility(result, location, redirectMethod, request, redirectClient);
}

//Create a new HTTP client endpoint configuration with a given location as the url.
function createNewEndpoint(string location, ClientEndpointConfig config) returns ClientEndpointConfig {
    ClientEndpointConfig newEpConfig = { url: location,
        circuitBreaker: config.circuitBreaker,
        timeoutMillis: config.timeoutMillis,
        keepAlive: config.keepAlive,
        chunking: config.chunking,
        httpVersion: config.httpVersion,
        forwarded: config.forwarded,
        followRedirects: config.followRedirects,
        retryConfig: config.retryConfig,
        proxy: config.proxy,
        connectionThrottling: config.connectionThrottling,
        secureSocket: config.secureSocket,
        cache: config.cache,
        compression: config.compression,
        auth: config.auth
    };
    return newEpConfig;
}

//Get the HTTP method that should be used for redirection based on the status code.
function getRedirectMethod(HttpOperation httpVerb, Response response) returns HttpOperation|() {
    int statusCode = response.statusCode;
    if ((statusCode == MULTIPLE_CHOICES_300 || statusCode == USE_PROXY_305 || statusCode == TEMPORARY_REDIRECT_307
            || statusCode == PERMANENT_REDIRECT_308) && (httpVerb == HTTP_GET || httpVerb == HTTP_HEAD)) {
        return httpVerb;
    } else if ((statusCode == MOVED_PERMANENTLY_301 || statusCode == FOUND_302) &&
        (httpVerb == HTTP_GET || httpVerb == HTTP_HEAD)) {
        return HTTP_GET;
    } else if (statusCode == SEE_OTHER_303) {
        return HTTP_GET;
    } else {
        return ();
    }
}

function createRedirectRequest(int statusCode, Request request) returns Request {
    Request redirectRequest = new;
    string[] headerNames = untaint request.getHeaderNames();
    foreach headerName in headerNames {
        string[] headerValues = request.getHeaders(headerName);
        foreach (headerValue in headerValues) {
            redirectRequest.addHeader(headerName, headerValue);
        }
    }
    if (statusCode == SEE_OTHER_303) {
        redirectRequest.removeHeader(TRANSFER_ENCODING);
        redirectRequest.removeHeader(CONTENT_LENGTH);
    }
    return redirectRequest;
}

function isAbsolute(string locationUrl) returns boolean {
    return (locationUrl.hasPrefix(HTTP_SCHEME) || locationUrl.hasPrefix(HTTPS_SCHEME));
}

//Reset the current redirect count to 0 and set the resolved requested URI.
function setCountAndResolvedURL(RedirectClient redirectClient, Response response, string resolvedRequestedURI) {
    log:printDebug("Ultimate response coming from the request: " + resolvedRequestedURI);
    redirectClient.currentRedirectCount = 0;
    response.resolvedRequestedURI = resolvedRequestedURI;
}
