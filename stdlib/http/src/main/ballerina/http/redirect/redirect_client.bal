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

# Provides redirect functionality for HTTP client remote functions.
#
# + url - Target service url
# + config - HTTP ClientEndpointConfig to be used for HTTP client invocation
# + redirectConfig - Configurations associated with redirect
# + httpClient - HTTP client for outbound HTTP requests
# + currentRedirectCount - Current redirect count of the HTTP client
public type RedirectClient client object {

    public string url;
    public ClientEndpointConfig config;
    public FollowRedirects redirectConfig;
    public Client httpClient;
    public int currentRedirectCount = 0;

    # Create a redirect client with the given configurations.
    #
    # + url - Target service url
    # + config - HTTP ClientEndpointConfig to be used for HTTP client invocation
    # + redirectConfig - Configurations associated with redirect
    # + httpClient - HTTP client for outbound HTTP requests
    public function __init(string url, ClientEndpointConfig config, FollowRedirects redirectConfig, Client httpClient) {
        self.url = url;
        self.config = config;
        self.redirectConfig = redirectConfig;
        self.httpClient = httpClient;
    }

    # If the received response for the `get()` remote function is redirect eligible, redirect will be performed automatically
    # by this `get()` function.
    #
    # + path - Resource path
    # + message - An optional HTTP outbound request message or any payload of type `string`, `xml`, `json`,
    #             `byte[]`, `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - The HTTP `Response` message, or an error if the invocation fails
    public function get(string path, RequestMessage message = ()) returns Response|error {
        return performRedirectIfEligible(self, path, <Request>message, HTTP_GET);
    }

    # If the received response for the `post()` remote function is redirect eligible, redirect will be performed automatically
    # by this `post()` function.
    #
    # + path - Resource path
    # + message - An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - The HTTP `Response` message, or an error if the invocation fails
    public function post(string path, RequestMessage message) returns Response|error {
        return performRedirectIfEligible(self, path, <Request>message, HTTP_POST);
    }

    # If the received response for the `head()` remote function is redirect eligible, redirect will be performed automatically
    # by this `head()` function.
    #
    # + path - Resource path
    # + message - An optional HTTP outbound request message or or any payload of type `string`, `xml`, `json`,
    #             `byte[]`, `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - The HTTP `Response` message, or an error if the invocation fails
    public function head(string path, RequestMessage message = ()) returns Response|error {
        return performRedirectIfEligible(self, path, <Request>message, HTTP_HEAD);
    }

    # If the received response for the `put()` remote function is redirect eligible, redirect will be performed automatically
    # by this `put()` function.
    #
    # + path - Resource path
    # + message - An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - The HTTP `Response` message, or an error if the invocation fails
    public function put(string path, RequestMessage message) returns Response|error {
        return performRedirectIfEligible(self, path, <Request>message, HTTP_PUT);
    }

    # The `forward()` function is used to invoke an HTTP call with inbound request's HTTP verb.
    #
    # + path - Resource path
    # + request - An HTTP inbound request message
    # + return - The HTTP `Response` message, or an error if the invocation fails
    public remote function forward(string path, Request request) returns Response|error {
        return self.httpClient->forward(path, request);
    }

    # The `execute()` sends an HTTP request to a service with the specified HTTP verb. Redirect will be performed
    # only for HTTP methods.
    #
    # + path - Resource path
    # + message - An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - The HTTP `Response` message, or an error if the invocation fails
    public remote function execute(string httpVerb, string path, RequestMessage message) returns Response|error {
        Request request = <Request>message;
        //Redirection is performed only for HTTP methods
        if (HTTP_NONE == extractHttpOperation(httpVerb)) {
            return self.httpClient->execute(httpVerb, path, request);
        } else {
            return performRedirectIfEligible(self, path, request, extractHttpOperation(httpVerb));
        }
    }

    # If the received response for the `patch()` remote function is redirect eligible, redirect will be performed automatically
    # by this `patch()` function.
    #
    # + path - Resource path
    # + message - An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - The HTTP `Response` message, or an error if the invocation fails
    public function patch(string path, RequestMessage message) returns Response|error {
        return performRedirectIfEligible(self, path, <Request>message, HTTP_PATCH);
    }

    # If the received response for the `delete()` remote function is redirect eligible, redirect will be performed automatically
    # by this `delete()` function.
    #
    # + path - Resource path
    # + message - An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - The HTTP `Response` message, or an error if the invocation fails
    public function delete(string path, RequestMessage message) returns Response|error {
        return performRedirectIfEligible(self, path, <Request>message, HTTP_DELETE);
    }

    # If the received response for the `options()` remote function is redirect eligible, redirect will be performed automatically
    # by this `options()` function.
    #
    # + path - Resource path
    # + message - An optional HTTP outbound request message or any payload of type `string`, `xml`, `json`,
    #             `byte[]`, `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - The HTTP `Response` message, or an error if the invocation fails
    public function options(string path, RequestMessage message = ()) returns Response|error {
        return performRedirectIfEligible(self, path, <Request>message, HTTP_OPTIONS);
    }

    # Submits an HTTP request to a service with the specified HTTP verb.
    # The `submit()` function does not give out a `Response` as the result,
    # rather it returns an `HttpFuture` which can be used to do further interactions with the endpoint.
    #
    # + httpVerb - The HTTP verb value
    # + path - The resource path
    # + message - An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - An `HttpFuture` that represents an asynchronous service invocation, or an error if the submission fails
    public remote function submit(string httpVerb, string path, RequestMessage message) returns HttpFuture|error {
        return self.httpClient->submit(httpVerb, path, <Request>message);
    }

    # Retrieves the `Response` for a previously submitted request.
    #
    # + httpFuture - The `HttpFuture` relates to a previous asynchronous invocation
    # + return - An HTTP response message, or an error if the invocation fails
    public function getResponse(HttpFuture httpFuture) returns Response|error {
        return self.httpClient->getResponse(httpFuture);
    }

    # Checks whether a `PushPromise` exists for a previously submitted request.
    #
    # + httpFuture - The `HttpFuture` relates to a previous asynchronous invocation
    # + return - A `boolean` that represents whether a `PushPromise` exists
    public function hasPromise(HttpFuture httpFuture) returns (boolean) {
        return self.httpClient->hasPromise(httpFuture);
    }

    # Retrieves the next available `PushPromise` for a previously submitted request.
    #
    # + httpFuture - The `HttpFuture` relates to a previous asynchronous invocation
    # + return - An HTTP Push Promise message, or an error if the invocation fails
    public function getNextPromise(HttpFuture httpFuture) returns PushPromise|error {
        return self.httpClient->getNextPromise(httpFuture);
    }

    # Retrieves the promised server push `Response` message.
    #
    # + promise - The related `PushPromise`
    # + return - A promised HTTP `Response` message, or an error if the invocation fails
    public function getPromisedResponse(PushPromise promise) returns Response|error {
        return self.httpClient->getPromisedResponse(promise);
    }

    # Rejects a `PushPromise`.
    # When a `PushPromise` is rejected, there is no chance of fetching a promised response using the rejected promise.
    #
    # + promise - The Push Promise to be rejected
    public function rejectPromise(PushPromise promise) {
        self.httpClient->rejectPromise(promise);
    }
};

//Invoke relevant HTTP client action and check the response for redirect eligibility.
function performRedirectIfEligible(RedirectClient redirectClient, string path, Request request,
                                   HttpOperation httpOperation) returns Response|error {
    string originalUrl = redirectClient.url + path;
    log:printDebug(function() returns string {
        return "Checking redirect eligibility for original request " + originalUrl;
    });
    Response|error result = invokeEndpoint(path, request, httpOperation, redirectClient.httpClient);
    return checkRedirectEligibility(result, originalUrl, httpOperation, request, redirectClient);
}

//Inspect the response for redirect eligibility.
function checkRedirectEligibility(Response|error response, string resolvedRequestedURI,
                                  HttpOperation httpVerb, Request request, RedirectClient redirectClient)
                                    returns @untainted Response|error {
    if (response is Response) {
        if (isRedirectResponse(response.statusCode)) {
            return redirect(response, httpVerb, request, redirectClient, resolvedRequestedURI);
        } else {
            setCountAndResolvedURL(redirectClient, response, resolvedRequestedURI);
            return response;
        }
    } else {
        redirectClient.currentRedirectCount = 0;
        return response;
    }
}

//Check the response status for redirect eligibility.
function isRedirectResponse(int statusCode) returns boolean {
    log:printDebug(function() returns string {
        return "Response Code : " + statusCode;
    });
    return (statusCode == 300 || statusCode == 301 || statusCode == 302 || statusCode == 303 || statusCode == 305 ||
        statusCode == 307 || statusCode == 308);
}

//If max redirect count is not reached, perform redirection.
function redirect(Response response, HttpOperation httpVerb, Request request,
                  RedirectClient redirectClient, string resolvedRequestedURI) returns @untainted Response|error {
    int currentCount = redirectClient.currentRedirectCount;
    int maxCount = redirectClient.redirectConfig.maxCount;
    if (currentCount >= maxCount) {
        log:printDebug("Maximum redirect count reached!");
        setCountAndResolvedURL(redirectClient, response, resolvedRequestedURI);
    } else {
        currentCount += 1;
        log:printDebug(function() returns string {
            return "Redirect count : " + currentCount;
        });
        redirectClient.currentRedirectCount = currentCount;
        var redirectMethod = getRedirectMethod(httpVerb, response);
        if (redirectMethod is HttpOperation) {
            if (response.hasHeader(LOCATION)) {
                string location = response.getHeader(LOCATION);
                log:printDebug(function() returns string {
                    return "Location header value: " + location;
                });
                if (!isAbsolute(location)) {
                    var resolvedURI = resolve(resolvedRequestedURI, location);
                    if (resolvedURI is string) {
                        return performRedirection(resolvedURI, redirectClient, redirectMethod, request,
                            response);
                    } else {
                        redirectClient.currentRedirectCount = 0;
                        return resolvedURI;
                    }
                } else {
                    return performRedirection(location, redirectClient, redirectMethod, request, response);
                }
            } else {
                redirectClient.currentRedirectCount = 0;
                error err = error("Location header not available!");
                return err;
            }
        } else {
            setCountAndResolvedURL(redirectClient, response, resolvedRequestedURI);
        }
    }
    return response;
}

function performRedirection(string location, RedirectClient redirectClient, HttpOperation redirectMethod,
                            Request request, Response response) returns @untainted Response|error {
    var retryClient = createRetryClient(location, createNewEndpointConfig(redirectClient.config));
    if (retryClient is Client) {
        log:printDebug(function() returns string {
                return "Redirect using new clientEP : " + location;
            });
        Response|error result = invokeEndpoint("", createRedirectRequest(response.statusCode, request),
            redirectMethod, retryClient.httpClient);
        return checkRedirectEligibility(result, location, redirectMethod, request, redirectClient);
    } else {
        return retryClient;
    }
}

//Create a new HTTP client endpoint configuration with a given location as the url.
function createNewEndpointConfig(ClientEndpointConfig config) returns ClientEndpointConfig {
    ClientEndpointConfig newEpConfig = {
        circuitBreaker: config.circuitBreaker,
        timeoutMillis: config.timeoutMillis,
        keepAlive: config.keepAlive,
        chunking: config.chunking,
        httpVersion: config.httpVersion,
        forwarded: config.forwarded,
        followRedirects: config.followRedirects,
        retryConfig: config.retryConfig,
        proxy: config.proxy,
        poolConfig: config.poolConfig,
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
    foreach var headerName in headerNames {
        string[] headerValues = request.getHeaders(headerName);
        foreach var headerValue in headerValues {
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
    log:printDebug(function() returns string {
        return "Ultimate response coming from the request: " + resolvedRequestedURI;
    });
    redirectClient.currentRedirectCount = 0;
    response.resolvedRequestedURI = resolvedRequestedURI;
}
