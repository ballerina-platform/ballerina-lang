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

documentation {
    Provides redirect functionality for HTTP client actions.

    F{{serviceUri}} Target service url
    F{{config}}  HTTP ClientEndpointConfig to be used for HTTP client invocation
    F{{redirectConfig}} Configurations associated with redirect
    F{{httpClient}}  HTTP client for outbound HTTP requests
    F{{currentRedirectCount}}  Current redirect count of the HTTP client
}
public type RedirectClient object {

    public string serviceUri;
    public ClientEndpointConfig config;
    public FollowRedirects redirectConfig;
    public CallerActions httpClient;
    public int currentRedirectCount = 0;

    documentation {
        Create a redirect client with the given configurations.

        P{{serviceUri}} Target service url
        P{{config}}  HTTP ClientEndpointConfig to be used for HTTP client invocation
        P{{redirectConfig}} Configurations associated with redirect
        P{{httpClient}}  HTTP client for outbound HTTP requests
    }
    public new(serviceUri, config, redirectConfig, httpClient) {
        self.serviceUri = serviceUri;
        self.config = config;
        self.redirectConfig = redirectConfig;
        self.httpClient = httpClient;
    }

    documentation {
        If the received response for the `get()` action is redirect eligible, redirect will be performed automatically
        by this `get()` function.

        P{{path}} Resource path
        P{{message}} An optional HTTP outbound request message or any payload of type `string`, `xml`, `json`,
                     `byte[]`, `io:ByteChannel` or `mime:Entity[]`
        R{{}} The HTTP `Response` message, or an error if the invocation fails
    }
    public function get(string path, Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|()
                                        message = ()) returns Response|error {
        Request request = buildRequest(message);
        return performRedirectIfEligible(self, path, request, HTTP_GET);
    }

    documentation {
       If the received response for the `post()` action is redirect eligible, redirect will be performed automatically
       by this `post()` function.

        P{{path}} Resource path
        P{{message}} An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
                     `io:ByteChannel` or `mime:Entity[]`
        R{{}} The HTTP `Response` message, or an error if the invocation fails
    }
    public function post(string path, Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|()
                                        message) returns Response|error {
        Request request = buildRequest(message);
        return performRedirectIfEligible(self, path, request, HTTP_POST);
    }

    documentation {
        If the received response for the `head()` action is redirect eligible, redirect will be performed automatically
        by this `head()` function.

        P{{path}} Resource path
        P{{message}} An optional HTTP outbound request message or or any payload of type `string`, `xml`, `json`,
                     `byte[]`, `io:ByteChannel` or `mime:Entity[]`
        R{{}} The HTTP `Response` message, or an error if the invocation fails
    }
    public function head(string path, Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|()
                                        message = ()) returns Response|error {
        Request request = buildRequest(message);
        return performRedirectIfEligible(self, path, request, HTTP_HEAD);
    }

    documentation {
        If the received response for the `put()` action is redirect eligible, redirect will be performed automatically
        by this `put()` function.

        P{{path}} Resource path
        P{{message}} An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
                     `io:ByteChannel` or `mime:Entity[]`
        R{{}} The HTTP `Response` message, or an error if the invocation fails
    }
    public function put(string path, Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|()
                                        message) returns Response|error {
        Request request = buildRequest(message);
        return performRedirectIfEligible(self, path, request, HTTP_PUT);
    }

    documentation {
        The `forward()` function is used to invoke an HTTP call with inbound request's HTTP verb.

        P{{path}} Resource path
        P{{request}} An HTTP inbound request message
        R{{}} The HTTP `Response` message, or an error if the invocation fails
    }
    public function forward(string path, Request request) returns Response|error {
        return self.httpClient.forward(path, request);
    }

    documentation {
        The `execute()` sends an HTTP request to a service with the specified HTTP verb. Redirect will be performed
        only for HTTP methods.

        P{{path}} Resource path
        P{{message}} An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
                     `io:ByteChannel` or `mime:Entity[]`
        R{{}} The HTTP `Response` message, or an error if the invocation fails
    }
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

    documentation {
        If the received response for the `patch()` action is redirect eligible, redirect will be performed automatically
        by this `patch()` function.

        P{{path}} Resource path
        P{{message}} An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
                     `io:ByteChannel` or `mime:Entity[]`
        R{{}} The HTTP `Response` message, or an error if the invocation fails
    }
    public function patch(string path, Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|()
                                            message) returns Response|error {
        Request request = buildRequest(message);
        return performRedirectIfEligible(self, path, request, HTTP_PATCH);
    }

    documentation {
        If the received response for the `delete()` action is redirect eligible, redirect will be performed automatically
        by this `delete()` function.

        P{{path}} Resource path
        P{{message}} An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
                     `io:ByteChannel` or `mime:Entity[]`
        R{{}} The HTTP `Response` message, or an error if the invocation fails
    }
    public function delete(string path, Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|()
                                            message) returns Response|error {
        Request request = buildRequest(message);
        return performRedirectIfEligible(self, path, request, HTTP_DELETE);
    }

    documentation {
        If the received response for the `options()` action is redirect eligible, redirect will be performed automatically
        by this `options()` function.

        P{{path}} Resource path
        P{{message}} An optional HTTP outbound request message or any payload of type `string`, `xml`, `json`,
                     `byte[]`, `io:ByteChannel` or `mime:Entity[]`
        R{{}} The HTTP `Response` message, or an error if the invocation fails
    }
    public function options(string path, Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|()
                                            message = ()) returns Response|error {
        Request request = buildRequest(message);
        return performRedirectIfEligible(self, path, request, HTTP_OPTIONS);
    }

    documentation {
    Submits an HTTP request to a service with the specified HTTP verb.
    The `submit()` function does not give out a `Response` as the result,
    rather it returns an `HttpFuture` which can be used to do further interactions with the endpoint.

        P{{httpVerb}} The HTTP verb value
        P{{path}} The resource path
        P{{message}} An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
                     `io:ByteChannel` or `mime:Entity[]`
        R{{}} An `HttpFuture` that represents an asynchronous service invocation, or an error if the submission fails
    }
    public function submit(string httpVerb, string path, Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|()
                                                message) returns HttpFuture|error {
        Request request = buildRequest(message);
        return self.httpClient.submit(httpVerb, path, request);
    }

    documentation {
    Retrieves the `Response` for a previously submitted request.

        P{{httpFuture}} The `HttpFuture` relates to a previous asynchronous invocation
        R{{}} An HTTP response message, or an error if the invocation fails
    }
    public function getResponse(HttpFuture httpFuture) returns Response|error {
        return self.httpClient.getResponse(httpFuture);
    }

    documentation {
    Checks whether a `PushPromise` exists for a previously submitted request.

        P{{httpFuture}} The `HttpFuture` relates to a previous asynchronous invocation
        R{{}} A `boolean` that represents whether a `PushPromise` exists
    }
    public function hasPromise(HttpFuture httpFuture) returns (boolean) {
        return self.httpClient.hasPromise(httpFuture);
    }

    documentation {
    Retrieves the next available `PushPromise` for a previously submitted request.

        P{{httpFuture}} The `HttpFuture` relates to a previous asynchronous invocation
        R{{}} An HTTP Push Promise message, or an error if the invocation fails
    }
    public function getNextPromise(HttpFuture httpFuture) returns PushPromise|error {
        return self.httpClient.getNextPromise(httpFuture);
    }

    documentation {
    Retrieves the promised server push `Response` message.

        P{{promise}} The related `PushPromise`
        R{{}} A promised HTTP `Response` message, or an error if the invocation fails
    }
    public function getPromisedResponse(PushPromise promise) returns Response|error {
        return self.httpClient.getPromisedResponse(promise);
    }

    documentation {
    Rejects a `PushPromise`.
    When a `PushPromise` is rejected, there is no chance of fetching a promised response using the rejected promise.

        P{{promise}} The Push Promise to be rejected
    }
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
