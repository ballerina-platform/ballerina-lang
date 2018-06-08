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
    Provides the HTTP actions for interacting with an HTTP endpoint. This is created by wrapping the HTTP client
    to provide retrying over HTTP requests.

    F{{serviceUri}} Target service url
    F{{config}}  HTTP ClientEndpointConfig to be used for HTTP client invocation
    F{{retryConfig}} Configurations associated with retry
    F{{httpClient}}  HTTP client for outbound HTTP requests
}
public type RedirectClient object {
    public {
        string serviceUri;
        ClientEndpointConfig config;
        FollowRedirects redirectConfig;
        CallerActions httpClient;
        int currentRedirectCount = 0;
    }

    documentation {
        Provides the HTTP actions for interacting with an HTTP endpoint. This is created by wrapping the HTTP client
        to provide retrying over HTTP requests.

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
        The `get()` function wraps the underlying HTTP actions in a way to provide
        retrying functionality for a given endpoint to recover from network level failures.

        P{{path}} Resource path
        P{{message}} An optional HTTP outbound request message or any payload of type `string`, `xml`, `json`,
                     `blob`, `io:ByteChannel` or `mime:Entity[]`
        R{{}} The HTTP `Response` message, or an error if the invocation fails
    }
    public function get(string path, Request|string|xml|json|blob|io:ByteChannel|mime:Entity[]|()
        message = ()) returns Response|error {
        io:println("Redirect Get!");
        Request request = buildRequest(message);
        Response|error result = self.httpClient.get(path, message = request);
        return handleRedirect(result, path, "GET", request, self, self.httpClient);
    }

    documentation {
        The `post()` function wraps the underlying HTTP actions in a way to provide
        retrying functionality for a given endpoint to recover from network level failures.

        P{{path}} Resource path
        P{{message}} An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `blob`,
                     `io:ByteChannel` or `mime:Entity[]`
        R{{}} The HTTP `Response` message, or an error if the invocation fails
    }
    public function post(string path, Request|string|xml|json|blob|io:ByteChannel|mime:Entity[]|()
        message) returns Response|error {
        Request request = buildRequest(message);
        return self.httpClient.post(path, request);
    }

    documentation {
        The `head()` function wraps the underlying HTTP actions in a way to provide
        retrying functionality for a given endpoint to recover from network level failures.

        P{{path}} Resource path
        P{{message}} An optional HTTP outbound request message or or any payload of type `string`, `xml`, `json`,
                     `blob`, `io:ByteChannel` or `mime:Entity[]`
        R{{}} The HTTP `Response` message, or an error if the invocation fails
    }
    public function head(string path, Request|string|xml|json|blob|io:ByteChannel|mime:Entity[]|()
        message = ()) returns Response|error {
        Request request = buildRequest(message);
        return self.httpClient.head(path, message = request);
    }

    documentation {
        The `put()` function wraps the underlying HTTP actions in a way to provide
        retrying functionality for a given endpoint to recover from network level failures.

        P{{path}} Resource path
        P{{message}} An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `blob`,
                     `io:ByteChannel` or `mime:Entity[]`
        R{{}} The HTTP `Response` message, or an error if the invocation fails
    }
    public function put(string path, Request|string|xml|json|blob|io:ByteChannel|mime:Entity[]|()
        message) returns Response|error {
        Request request = buildRequest(message);
        return self.httpClient.put(path, request);
    }

    documentation {
        The `forward()` function wraps the underlying HTTP actions in a way to provide retrying functionality
        for a given endpoint with inbound request's HTTP verb to recover from network level failures.

        P{{path}} Resource path
        P{{request}} An HTTP inbound request message
        R{{}} The HTTP `Response` message, or an error if the invocation fails
    }
    public function forward(string path, Request request) returns Response|error {
        return self.httpClient.forward(path, request);
    }

    documentation {
        The `execute()` sends an HTTP request to a service with the specified HTTP verb. The function wraps the
        underlying HTTP actions in a way to provide retrying functionality for a given endpoint to recover
        from network level failures.

        P{{path}} Resource path
        P{{message}} An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `blob`,
                     `io:ByteChannel` or `mime:Entity[]`
        R{{}} The HTTP `Response` message, or an error if the invocation fails
    }
    public function execute(string httpVerb, string path, Request|string|xml|json|blob|io:ByteChannel|mime:Entity[]|()
        message) returns Response|error {
        Request request = buildRequest(message);
        return self.httpClient.execute(httpVerb, path, request);
    }

    documentation {
        The `patch()` function wraps the undeline underlying HTTP actions in a way to provide
        retrying functionality for a given endpoint to recover from network level failures.

        P{{path}} Resource path
        P{{message}} An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `blob`,
                     `io:ByteChannel` or `mime:Entity[]`
        R{{}} The HTTP `Response` message, or an error if the invocation fails
    }
    public function patch(string path, Request|string|xml|json|blob|io:ByteChannel|mime:Entity[]|()
        message) returns Response|error {
        Request request = buildRequest(message);
        return self.httpClient.patch(path, request);
    }

    documentation {
        The `delete()` function wraps the underlying HTTP actions in a way to provide
        retrying functionality for a given endpoint to recover from network level failures.

        P{{path}} Resource path
        P{{message}} An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `blob`,
                     `io:ByteChannel` or `mime:Entity[]`
        R{{}} The HTTP `Response` message, or an error if the invocation fails
    }
    public function delete(string path, Request|string|xml|json|blob|io:ByteChannel|mime:Entity[]|()
        message) returns Response|error {
        Request request = buildRequest(message);
        return self.httpClient.delete(path, request);
    }

    documentation {
        The `options()` function wraps the underlying HTTP actions in a way to provide
        retrying functionality for a given endpoint to recover from network level failures.

        P{{path}} Resource path
        P{{message}} An optional HTTP outbound request message or any payload of type `string`, `xml`, `json`,
                     `blob`, `io:ByteChannel` or `mime:Entity[]`
        R{{}} The HTTP `Response` message, or an error if the invocation fails
    }
    public function options(string path, Request|string|xml|json|blob|io:ByteChannel|mime:Entity[]|()
        message = ()) returns Response|error {
        Request request = buildRequest(message);
        return self.httpClient.options(path, message = request);
    }

    documentation {
    Submits an HTTP request to a service with the specified HTTP verb.
    The `submit()` function does not give out a `Response` as the result,
    rather it returns an `HttpFuture` which can be used to do further interactions with the endpoint.

        P{{httpVerb}} The HTTP verb value
        P{{path}} The resource path
        P{{message}} An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `blob`,
                     `io:ByteChannel` or `mime:Entity[]`
        R{{}} An `HttpFuture` that represents an asynchronous service invocation, or an error if the submission fails
    }
    public function submit(string httpVerb, string path, Request|string|xml|json|blob|io:ByteChannel|mime:Entity[]|()
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

function handleRedirect(Response|error result, string path, string httpVerb, Request request,
                        RedirectClient redirectClient, CallerActions callerAction)
             returns @untainted Response|error {
    io:println("Handle redirect >> checking redirect eligibility...");
    match result {
        Response response => {
            if (isRedirectResponse(response.statusCode)) {
                log:printInfo("Redirect status code recevied");
                return doRedirection(response, path, httpVerb, request, redirectClient, callerAction);
            } else {
                return response;
            }
        }
        error err => {
            return err;
        }
    }
}

function isRedirectResponse(int statusCode) returns boolean {
    return (statusCode >= 300 && statusCode < 400);
}

function doRedirection(Response response, string path, string httpVerb, Request request,
                       RedirectClient redirectClient, CallerActions callerAction)
             returns @untainted Response|error {
    int currentCount = redirectClient.currentRedirectCount;
    int maxCount = redirectClient.redirectConfig.maxCount;
    if (currentCount >= maxCount) {
        io:println("Max count exceeded... returning...");
        return response;
    } else {
        currentCount++;
        io:println(currentCount);
        redirectClient.currentRedirectCount = currentCount;
        match getRedirectMethod(httpVerb, response) {
            () => {
                log:printInfo("No redirect method");
                return response;
            }
            string redirectMethod => {
                if (response.hasHeader("location")) {
                    string location = response.getHeader("location");
                    log:printInfo("Location header value: " + location);
                    if (isCrossDomain(redirectClient.config.url, location)) {
                        log:printInfo("Cross Domain: " + location);
                        CallerActions newCallerAction = createRetryClient(location,
                            createNewEndpoint(location, redirectClient.config));
                        Response|error result = invokeEndpoint("", createRedirectRequest(request),
                            extractHttpOperation(redirectMethod), newCallerAction);
                        return handleRedirect(result, path, redirectMethod, request, redirectClient,
                            newCallerAction);
                    } else {
                        log:printInfo("Not Cross Domain: " + location);
                        match resolve(redirectClient.config.url, location) {
                            string resolvedURI => {
                                log:printInfo("Resolved URI: " + resolvedURI);
                                Response|error result = invokeEndpoint(getPath(redirectClient.config.url, resolvedURI),
                                    createRedirectRequest(request),
                                    extractHttpOperation(redirectMethod), redirectClient.httpClient);
                                return handleRedirect(result, path, redirectMethod, request, redirectClient,
                                    callerAction);
                            }
                            error err => return err;
                        }
                    }
                } else {
                    error err = { message: "Location header not available!" };
                    return err;
                }
            }
        }
    }
}

function getPath(string endpointURL, string resolvedURI) returns string {
    log:printInfo("Calculating path...");
    log:printInfo("Original endpoint value : " +endpointURL + " resolvedURL : " + resolvedURI);
    string clientPath =  resolvedURI.substring(endpointURL.length(), resolvedURI.length());
    log:printInfo("Calculated path: " + clientPath);
    return clientPath;
}

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

function getRedirectMethod(string httpVerb, Response response) returns string|() {
    int statusCode = response.statusCode;
    if ((statusCode == 300 || statusCode == 305 || statusCode == 307 || statusCode == 308) &&
        (httpVerb.equalsIgnoreCase("GET") || httpVerb.equalsIgnoreCase("HEAD"))) {
        log:printInfo("First 3XX" + httpVerb + ":" + statusCode);
        return httpVerb;
    } else if ((statusCode == 301 || statusCode == 302) &&
        (httpVerb.equalsIgnoreCase("GET") || httpVerb.equalsIgnoreCase("HEAD"))) {
        log:printInfo("Second 3XX" + httpVerb + ":" + statusCode);
        return "GET";
    } else if (statusCode == 303) {
        log:printInfo("Third 3XX" + httpVerb + ":" + statusCode);
        return "GET";
    } else {
        log:printInfo(httpVerb + ":" + statusCode);
        return ();
    }
}

function createRedirectRequest(Request request) returns Request {
    return request;
}

function isCrossDomain(string endPointURL, string locationUrl) returns boolean {
    io:println("Start checking cross domain...");
    if (locationUrl.length() > 6) {
        string httpScheme = locationUrl.substring(0, 7);
        string httpsScheme = locationUrl.substring(0, 8);
        log:printInfo("location scheme : http >> " + httpScheme + " https: >> " + httpsScheme);
        if (httpScheme.equalsIgnoreCase("http://") || httpsScheme.equalsIgnoreCase("https://")) {
            URI location = new URI(locationUrl);
            URI endPoint = new URI(endPointURL);
            if (location.scheme == endPoint.scheme && location.host == endPoint.host && location.port == endPoint.port) {
                io:println("This is not cross domain...");
                return false;
            } else {
                log:printInfo("port or host doesn't match so its cross domain");
                log:printInfo("endpoint host:port >> " + endPoint.scheme + ":" + endPoint.host + ":" + endPoint.port);
                log:printInfo("location host:port >> " + location.scheme + ":" + location.host + ":" + location.port);
                return true;
            }
        } else {
            io:println("location doesn't start with http or https...");
            return false;
        }
    } else {
        io:println("location doesn't start with http or https...");
        return false;
    }
}

type URI object {

    private {
        string scheme;
        string host;
        int port;
    }

    public new(string uri) {
        match self.parse(uri) {
            () => {}
            error err => throw err;
        }
    }
    native function parse(string url) returns ()|error;
};

native function resolve(string baseUrl, string path) returns string|error;