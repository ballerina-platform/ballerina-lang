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

import ballerina/cache;
import ballerina/log;
import ballerina/time;
import ballerina/io;

# An HTTP caching client implementation which takes an `HttpActions` instance and wraps it with an HTTP caching layer.
#
# + url - The URL of the remote HTTP endpoint
# + config - The configurations of the client endpoint associated with this `CachingActions` instance
# + httpClient - The underlying `HttpActions` instance which will be making the actual network calls
# + cache - The cache storage for the HTTP responses
# + cacheConfig - Configurations for the underlying cache storage and for controlling the HTTP caching behaviour
public client class HttpCachingClient {

    public string url = "";
    public ClientConfiguration config = {};
    public HttpClient httpClient;
    public HttpCache cache;
    public CacheConfig cacheConfig = {};

    # Takes a service URL, a `CliendEndpointConfig` and a `CacheConfig` and builds an HTTP client capable of
    # caching responses. The `CacheConfig` instance is used for initializing a new HTTP cache for the client and
    # the `ClientConfiguration` is used for creating the underlying HTTP client.
    #
    # + url - The URL of the HTTP endpoint to connect to
    # + config - The configurations for the client endpoint associated with the caching client
    # + cacheConfig - The configurations for the HTTP cache to be used with the caching client
    public function init(string url, ClientConfiguration config, CacheConfig cacheConfig) {
        var httpSecureClient = createHttpSecureClient(url, config);
        if (httpSecureClient is HttpClient) {
            self.httpClient = httpSecureClient;
        } else {
            error clientError = httpSecureClient;
            panic <error> httpSecureClient;
        }
        self.cache = new HttpCache(cacheConfig);
    }

    # Responses returned for POST requests are not cacheable. Therefore, the requests are simply directed to the
    # origin server. Responses received for POST requests invalidate the cached responses for the same resource.
    #
    # + path - Resource path
    # + message - HTTP request or any payload of type `string`, `xml`, `json`, `byte[]`, `io:ReadableByteChannel`
    #             or `mime:Entity[]`
    # + return - The response for the request or an `http:ClientError` if failed to establish communication with the upstream server
    public remote function post(string path, RequestMessage message) returns Response|ClientError {
        Request req = <Request>message;
        setRequestCacheControlHeader(req);

        var inboundResponse = self.httpClient->post(path, req);
        if (inboundResponse is Response) {
            invalidateResponses(self.cache, inboundResponse, path);
        }
        return inboundResponse;
    }

    # Responses for HEAD requests are cacheable and as such, will be routed through the HTTP cache. Only if a
    # suitable response cannot be found will the request be directed to the origin server.
    #
    # + path - Resource path
    # + message - An optional HTTP request or any payload of type `string`, `xml`, `json`, `byte[]`, `io:ReadableByteChannel`
    #             or `mime:Entity[]`
    # + return - The response for the request or an `http:ClientError` if failed to establish communication with the upstream server
    public remote function head(string path, RequestMessage message = ()) returns @tainted Response|ClientError {
        Request req = <Request>message;
        setRequestCacheControlHeader(req);
        return getCachedResponse(self.cache, self.httpClient, req, HTTP_HEAD, path, self.cacheConfig.isShared, false);
    }

    # Responses returned for PUT requests are not cacheable. Therefore, the requests are simply directed to the
    # origin server. In addition, PUT requests invalidate the currently stored responses for the given path.
    #
    # + path - Resource path
    # + message - An optional HTTP request or any payload of type `string`, `xml`, `json`, `byte[]`, `io:ReadableByteChannel`
    #             or `mime:Entity[]`
    # + return - The response for the request or an `http:ClientError` if failed to establish communication with the upstream server
    public remote function put(string path, RequestMessage message) returns Response|ClientError {
        Request req = <Request>message;
        setRequestCacheControlHeader(req);

        var inboundResponse = self.httpClient->put(path, req);
        if (inboundResponse is Response) {
            invalidateResponses(self.cache, inboundResponse, path);
        }
        return inboundResponse;
    }

    # Invokes an HTTP call with the specified HTTP method. This is not a cacheable operation, unless the HTTP method
    # used is GET or HEAD.
    #
    # + httpMethod - HTTP method to be used for the request
    # + path - Resource path
    # + message - An HTTP request or any payload of type `string`, `xml`, `json`, `byte[]`, `io:ReadableByteChannel`
    #             or `mime:Entity[]`
    # + return - The response for the request or an `http:ClientError` if failed to establish communication with the upstream server
    public remote function execute(string httpMethod, string path, RequestMessage message)
                                                            returns @tainted Response|ClientError {
        Request request = <Request>message;
        setRequestCacheControlHeader(request);

        if (httpMethod == HTTP_GET || httpMethod == HTTP_HEAD) {
            return getCachedResponse(self.cache, self.httpClient, request, httpMethod, path,
                                     self.cacheConfig.isShared, false);
        }

        var inboundResponse = self.httpClient->execute(httpMethod, path, request);
        if (inboundResponse is Response) {
            invalidateResponses(self.cache, inboundResponse, path);
        }
        return inboundResponse;
    }

    # Responses returned for PATCH requests are not cacheable. Therefore, the requests are simply directed to
    # the origin server. Responses received for PATCH requests invalidate the cached responses for the same resource.
    #
    # + path - Resource path
    # + message - An HTTP request or any payload of type `string`, `xml`, `json`, `byte[]`, `io:ReadableByteChannel`
    #             or `mime:Entity[]`
    # + return - The response for the request or an `http:ClientError` if failed to establish communication with the upstream server
    public remote function patch(string path, RequestMessage message) returns Response|ClientError {
        Request req = <Request>message;
        setRequestCacheControlHeader(req);

        var inboundResponse = self.httpClient->patch(path, req);
        if (inboundResponse is Response) {
            invalidateResponses(self.cache, inboundResponse, path);
        }
        return inboundResponse;
    }

    # Responses returned for DELETE requests are not cacheable. Therefore, the requests are simply directed to the
    # origin server. Responses received for DELETE requests invalidate the cached responses for the same resource.
    #
    # + path - Resource path
    # + message - An HTTP request or any payload of type `string`, `xml`, `json`, `byte[]`, `io:ReadableByteChannel`
    #             or `mime:Entity[]`
    # + return - The response for the request or an `http:ClientError` if failed to establish communication with the upstream server
    public remote function delete(string path, RequestMessage message = ()) returns Response|ClientError {
        Request req = <Request>message;
        setRequestCacheControlHeader(req);

        var inboundResponse = self.httpClient->delete(path, req);
        if (inboundResponse is Response) {
            invalidateResponses(self.cache, inboundResponse, path);
        }
        return inboundResponse;
    }

    # Responses for GET requests are cacheable and as such, will be routed through the HTTP cache. Only if a suitable
    # response cannot be found will the request be directed to the origin server.
    #
    # + path - Request path
    # + message - An optional HTTP request or any payload of type `string`, `xml`, `json`, `byte[]`, `io:ReadableByteChannel`
    #             or `mime:Entity[]`
    # + return - The response for the request or an `http:ClientError` if failed to establish communication with the upstream server
    public remote function get(string path, RequestMessage message = ()) returns @tainted Response|ClientError {
        Request req = <Request>message;
        setRequestCacheControlHeader(req);
        return getCachedResponse(self.cache, self.httpClient, req, HTTP_GET, path, self.cacheConfig.isShared, false);
    }

    # Responses returned for OPTIONS requests are not cacheable. Therefore, the requests are simply directed to the
    # origin server. Responses received for OPTIONS requests invalidate the cached responses for the same resource.
    #
    # + path - Request path
    # + message - An optional HTTP request or any payload of type `string`, `xml`, `json`, `byte[]`, `io:ReadableByteChannel`
    #             or `mime:Entity[]`
    # + return - The response for the request or an `http:ClientError` if failed to establish communication with the upstream server
    public remote function options(string path, RequestMessage message = ()) returns Response|ClientError {
        Request req = <Request>message;
        setRequestCacheControlHeader(req);

        var inboundResponse = self.httpClient->options(path, message = req);
        if (inboundResponse is Response) {
            invalidateResponses(self.cache, inboundResponse, path);
        }
        return inboundResponse;
    }

    # Forward remote function can be used to invoke an HTTP call with inbound request's HTTP method. Only inbound requests of
    # GET and HEAD HTTP method types are cacheable.
    #
    # + path - Request path
    # + request - The HTTP request to be forwarded
    # + return - The response for the request or an `http:ClientError` if failed to establish communication with the upstream server
    public remote function forward(string path, @tainted Request request) returns @tainted Response|ClientError {
        if (request.method == HTTP_GET || request.method == HTTP_HEAD) {
            return getCachedResponse(self.cache, self.httpClient, request, request.method, path,
                                     self.cacheConfig.isShared, true);
        }

        var inboundResponse = self.httpClient->forward(path, request);
        if (inboundResponse is Response) {
            invalidateResponses(self.cache, inboundResponse, path);
        }
        return inboundResponse;
    }

    # Submits an HTTP request to a service with the specified HTTP verb.
    #
    # + httpVerb - The HTTP verb value
    # + path - The resource path
    # + message - An HTTP request or any payload of type `string`, `xml`, `json`, `byte[]`, `io:ReadableByteChannel`
    #             or `mime:Entity[]`
    # + return - An `HttpFuture` that represents an asynchronous service invocation, or an error if the submission fails
    public remote function submit(string httpVerb, string path, RequestMessage message) returns HttpFuture|ClientError {
        return self.httpClient->submit(httpVerb, path, <Request>message);
    }

    # Retrieves the `http:Response` for a previously-submitted request.
    #
    # + httpFuture - The `http:HttpFuture` related to a previous asynchronous invocation
    # + return - A `http:Response` message, or else an `http:ClientError` if the invocation fails
    public remote function getResponse(HttpFuture httpFuture) returns Response|ClientError {
        return self.httpClient->getResponse(httpFuture);
    }

    # Checks whether an `http:PushPromise` exists for a previously-submitted request.
    #
    # + httpFuture - The `http:HttpFuture` relates to a previous asynchronous invocation
    # + return - A `boolean`, which represents whether an `http:PushPromise` exists
    public remote function hasPromise(HttpFuture httpFuture) returns boolean {
        return self.httpClient->hasPromise(httpFuture);
    }

    # Retrieves the next available `http:PushPromise` for a previously-submitted request.
    #
    # + httpFuture - The `http:HttpFuture` relates to a previous asynchronous invocation
    # + return - An `http:PushPromise` message or else an `http:ClientError` if the invocation fails
    public remote function getNextPromise(HttpFuture httpFuture) returns PushPromise|ClientError {
        return self.httpClient->getNextPromise(httpFuture);
    }

    # Retrieves the promised server push `http:Response` message.
    #
    # + promise - The related `http:PushPromise`
    # + return - A promised HTTP `http:Response` message or else an `http:ClientError` if the invocation fails
    public remote function getPromisedResponse(PushPromise promise) returns Response|ClientError {
        return self.httpClient->getPromisedResponse(promise);
    }

    # Rejects an `http:PushPromise`. When an `http:PushPromise` is rejected, there is no chance of fetching a promised
    # response using the rejected promise.
    #
    # + promise - The Push Promise to be rejected
    public remote function rejectPromise(PushPromise promise) {
        self.httpClient->rejectPromise(promise);
    }
}

# Creates an HTTP client capable of caching HTTP responses.
#
# + url - The URL of the HTTP endpoint to connect
# + config - The configurations for the client endpoint associated with the caching client
# + cacheConfig - The configurations for the HTTP cache to be used with the caching client
# + return - An `http:HttpCachingClient` instance, which wraps the base `http:Client` with a caching layer 
#            or else an `http:ClientError`
public function createHttpCachingClient(string url, ClientConfiguration config, CacheConfig cacheConfig)
                                                                                      returns HttpClient|ClientError {
    HttpCachingClient httpCachingClient = new(url, config, cacheConfig);
    log:printDebug(() => "Created HTTP caching client: " + io:sprintf("%s", httpCachingClient));
    return httpCachingClient;
}

function getCachedResponse(HttpCache cache, HttpClient httpClient, @tainted Request req, string httpMethod, string path,
                           boolean isShared, boolean forwardRequest) returns @tainted Response|ClientError {
    time:Time currentT = time:currentTime();
    req.parseCacheControlHeader();

    if (cache.hasKey(getCacheKey(httpMethod, path))) {
        Response cachedResponse = cache.get(getCacheKey(httpMethod, path));

        log:printDebug(() => "Cached response found for: '" + httpMethod + " " + path + "'");

        // Based on https://tools.ietf.org/html/rfc7234#section-4

        updateResponseTimestamps(cachedResponse, currentT.time, currentT.time);
        setAgeHeader(<@untainted> cachedResponse);

        RequestCacheControl? reqCache = req.cacheControl;
        ResponseCacheControl? resCache = cachedResponse.cacheControl;

        if (isFreshResponse(cachedResponse, isShared)) {
            // If the no-cache directive is not set, responses can be served straight from the cache, without
            // validating with the origin server.
            if (!isNoCacheSet(reqCache, resCache) && !req.hasHeader(PRAGMA)) {
                log:printDebug("Serving a cached fresh response without validating with the origin server");
                return cachedResponse;
            }

            log:printDebug("Serving a cached fresh response after validating with the origin server");
            return getValidationResponse(httpClient, req, cachedResponse, cache, currentT, path, httpMethod, true);
        }

        // If a fresh response is not available, serve a stale response, provided that it is not prohibited by
        // a directive and is explicitly allowed in the request.
        if (isAllowedToBeServedStale(req.cacheControl, cachedResponse, isShared) && !req.hasHeader(PRAGMA)) {
            // If the no-cache directive is not set, responses can be served straight from the cache, without
            // validating with the origin server.
            log:printDebug("Serving cached stale response without validating with the origin server");
            cachedResponse.setHeader(WARNING, WARNING_110_RESPONSE_IS_STALE);
            return cachedResponse;
        }

        log:printDebug(() => "Validating a stale response for '" + path + "' with the origin server.");

        var validatedResponse = getValidationResponse(httpClient, req, cachedResponse, cache, currentT, path,
                                                            httpMethod, false);
        if (validatedResponse is Response) {
            updateResponseTimestamps(validatedResponse, currentT.time, time:currentTime().time);
            setAgeHeader(validatedResponse);
        }
        return validatedResponse;
    }

    log:printDebug(() => "Cached response not found for: '" + httpMethod + " " + path + "'");
    log:printDebug(() => "Sending new request to: " + path);

    var response = sendNewRequest(httpClient, req, path, httpMethod, forwardRequest);
    if (response is Response) {
        if (cache.isAllowedToCache(response)) {
            response.requestTime = currentT.time;
            response.receivedTime = time:currentTime().time;
            cache.put(<@untainted> getCacheKey(httpMethod, path), <@untainted> req.cacheControl, <@untainted> response);
        }
    }
    return response;
}

// Based on https://tools.ietf.org/html/rfc7234#section-4.4
function invalidateResponses(HttpCache httpCache, Response inboundResponse, string path) {
    // TODO: Improve this logic in accordance with the spec
    if (isCacheableStatusCode(inboundResponse.statusCode) &&
                    inboundResponse.statusCode >= 200 && inboundResponse.statusCode < 400) {
        string getMethodCacheKey = getCacheKey(HTTP_GET, path);
        if (httpCache.cache.hasKey(getMethodCacheKey)) {
            cache:Error? result = httpCache.cache.invalidate(getMethodCacheKey);
            if (result is cache:Error) {
                log:printDebug(() => "Failed to remove the key: " + getMethodCacheKey + " from the cache.");
            }
        }

        string headMethodCacheKey = getCacheKey(HTTP_HEAD, path);
        if (httpCache.cache.hasKey(headMethodCacheKey)) {
            cache:Error? result = httpCache.cache.invalidate(headMethodCacheKey);
            if (result is cache:Error) {
                log:printDebug(() => "Failed to remove the key: " + headMethodCacheKey + " from the cache.");
            }
        }
    }
}

function sendNewRequest(HttpClient httpClient, Request request, string path, string httpMethod, boolean forwardRequest)
                                                                                returns Response|ClientError {
    if (forwardRequest) {
        return httpClient->forward(path, request);
    }
    if (httpMethod == HTTP_GET) {
        return httpClient->get(path, message = request);
    } else if (httpMethod == HTTP_HEAD) {
        return httpClient->head(path, message = request);
    } else {
        string message = "HTTP method not supported in caching client: " + httpMethod;
        return UnsupportedActionError(message);
    }
}
