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


import ballerina/log;
import ballerina/runtime;
import ballerina/time;
import ballerina/io;

@final string WARNING_AGENT = getWarningAgent();

@final string WARNING_110_RESPONSE_IS_STALE = "110 " + WARNING_AGENT + " \"Response is Stale\"";
@final string WARNING_111_REVALIDATION_FAILED = "111 " + WARNING_AGENT + " \"Revalidation Failed\"";

@final string WEAK_VALIDATOR_TAG = "W/";
@final int STALE = 0;

@final string FORWARD = "FORWARD";
@final string GET = "GET";
@final string POST = "POST";
@final string DELETE = "DELETE";
@final string OPTIONS = "OPTIONS";
@final string PUT = "PUT";
@final string PATCH = "PATCH";
@final string HEAD = "HEAD";

# Used for configuring the caching behaviour. Setting the `policy` field in the `CacheConfig` record allows
# the user to control the caching behaviour.
public type CachingPolicy "CACHE_CONTROL_AND_VALIDATORS"|"RFC_7234";

# This is a more restricted mode of RFC 7234. Setting this as the caching policy restricts caching to instances
# where the `cache-control` header and either the `etag` or `last-modified` header are present.
@final public CachingPolicy CACHE_CONTROL_AND_VALIDATORS = "CACHE_CONTROL_AND_VALIDATORS";

# Caching behaviour is as specified by the RFC 7234 specification.
@final public CachingPolicy RFC_7234 = "RFC_7234";

# Provides a set of configurations for controlling the caching behaviour of the endpoint.
#
# + enabled - Specifies whether HTTP caching is enabled. Caching is enabled by default.
# + isShared - Specifies whether the HTTP caching layer should behave as a public cache or a private cache
# + expiryTimeMillis - The number of milliseconds to keep an entry in the cache
# + capacity - The capacity of the cache
# + evictionFactor - The fraction of entries to be removed when the cache is full. The value should be
#                    between 0 (exclusive) and 1 (inclusive).
# + policy - Gives the user some control over the caching behaviour. By default, this is set to
#            `CACHE_CONTROL_AND_VALIDATORS`. The default behaviour is to allow caching only when the `cache-control`
#            header and either the `etag` or `last-modified` header are present.
public type CacheConfig record {
    boolean enabled = true;
    boolean isShared = false;
    int expiryTimeMillis = 86400;
    int capacity = 8388608; // 8MB
    float evictionFactor = 0.2;
    CachingPolicy policy = CACHE_CONTROL_AND_VALIDATORS;
    !...
};

# An HTTP caching client implementation which takes an `HttpActions` instance and wraps it with an HTTP caching layer.
#
# + serviceUri - The URL of the remote HTTP endpoint
# + config - The configurations of the client endpoint associated with this `CachingActions` instance
# + httpClient - The underlying `HttpActions` instance which will be making the actual network calls
# + cache - The cache storage for the HTTP responses
# + cacheConfig - Configurations for the underlying cache storage and for controlling the HTTP caching behaviour
public type HttpCachingClient object {

    public string serviceUri;
    public ClientEndpointConfig config;
    public CallerActions httpClient;
    public HttpCache cache;
    public CacheConfig cacheConfig;

    # Takes a service URL, a `CliendEndpointConfig` and a `CacheConfig` and builds an HTTP client capable of
    # caching responses. The `CacheConfig` instance is used for initializing a new HTTP cache for the client and
    # the `ClientEndpointConfig` is used for creating the underlying HTTP client.
    #
    # + serviceUri - The URL of the HTTP endpoint to connect to
    # + config - The configurations for the client endpoint associated with the caching client
    # + cacheConfig - The configurations for the HTTP cache to be used with the caching client
    public new(serviceUri, config, cacheConfig) {
        self.httpClient = createHttpSecureClient(serviceUri, config);
        self.cache = createHttpCache("http-cache", cacheConfig);
    }

    # Responses returned for POST requests are not cacheable. Therefore, the requests are simply directed to the
    # origin server. Responses received for POST requests invalidate the cached responses for the same resource.
    #
    # + path - Resource path
    # + message - HTTP request or any payload of type `string`, `xml`, `json`, `byte[]`, `io:ByteChannel`
    #             or `mime:Entity[]`
    # + return - The response for the request or an `error` if failed to establish communication with the upstream server
    public function post(string path, Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|()
                                        message) returns Response|error;

    # Responses for HEAD requests are cacheable and as such, will be routed through the HTTP cache. Only if a
    # suitable response cannot be found will the request be directed to the origin server.
    #
    # + path - Resource path
    # + message - An optional HTTP request or any payload of type `string`, `xml`, `json`, `byte[]`, `io:ByteChannel`
    #             or `mime:Entity[]`
    # + return - The response for the request or an `error` if failed to establish communication with the upstream server
    public function head(string path, Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|()
                                        message = ()) returns Response|error;

    # Responses returned for PUT requests are not cacheable. Therefore, the requests are simply directed to the
    # origin server. In addition, PUT requests invalidate the currently stored responses for the given path.
    #
    # + path - Resource path
    # + message - An optional HTTP request or any payload of type `string`, `xml`, `json`, `byte[]`, `io:ByteChannel`
    #             or `mime:Entity[]`
    # + return - The response for the request or an `error` if failed to establish communication with the upstream server
    public function put(string path, Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|()
                                        message) returns Response|error;

    # Invokes an HTTP call with the specified HTTP method. This is not a cacheable operation, unless the HTTP method
    # used is GET or HEAD.
    #
    # + httpMethod - HTTP method to be used for the request
    # + path - Resource path
    # + message - An HTTP request or any payload of type `string`, `xml`, `json`, `byte[]`, `io:ByteChannel`
    #             or `mime:Entity[]`
    # + return - The response for the request or an `error` if failed to establish communication with the upstream server
    public function execute(string httpMethod, string path, Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|()
                                                                message) returns Response|error;

    # Responses returned for PATCH requests are not cacheable. Therefore, the requests are simply directed to
    # the origin server. Responses received for PATCH requests invalidate the cached responses for the same resource.
    #
    # + path - Resource path
    # + message - An HTTP request or any payload of type `string`, `xml`, `json`, `byte[]`, `io:ByteChannel`
    #             or `mime:Entity[]`
    # + return - The response for the request or an `error` if failed to establish communication with the upstream server
    public function patch(string path, Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|()
                                        message) returns Response|error;

    # Responses returned for DELETE requests are not cacheable. Therefore, the requests are simply directed to the
    # origin server. Responses received for DELETE requests invalidate the cached responses for the same resource.
    #
    # + path - Resource path
    # + message - An HTTP request or any payload of type `string`, `xml`, `json`, `byte[]`, `io:ByteChannel`
    #             or `mime:Entity[]`
    # + return - The response for the request or an `error` if failed to establish communication with the upstream server
    public function delete(string path, Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|()
                                            message) returns Response|error;

    # Responses for GET requests are cacheable and as such, will be routed through the HTTP cache. Only if a suitable
    # response cannot be found will the request be directed to the origin server.
    #
    # + path - Request path
    # + message - An optional HTTP request or any payload of type `string`, `xml`, `json`, `byte[]`, `io:ByteChannel`
    #             or `mime:Entity[]`
    # + return - The response for the request or an `error` if failed to establish communication with the upstream server
    public function get(string path, Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|()
                                        message = ()) returns Response|error;

    # Responses returned for OPTIONS requests are not cacheable. Therefore, the requests are simply directed to the
    # origin server. Responses received for OPTIONS requests invalidate the cached responses for the same resource.
    #
    # + path - Request path
    # + message - An optional HTTP request or any payload of type `string`, `xml`, `json`, `byte[]`, `io:ByteChannel`
    #             or `mime:Entity[]`
    # + return - The response for the request or an `error` if failed to establish communication with the upstream server
    public function options(string path, Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|()
                                            message = ()) returns Response|error;

    # Forward action can be used to invoke an HTTP call with inbound request's HTTP method. Only inbound requests of
    # GET and HEAD HTTP method types are cacheable.
    #
    # + path - Request path
    # + request - The HTTP request to be forwarded
    # + return - The response for the request or an `error` if failed to establish communication with the upstream server
    public function forward(string path, Request request) returns Response|error;

    # Submits an HTTP request to a service with the specified HTTP verb.
    #
    # + httpVerb - The HTTP verb value
    # + path - The resource path
    # + message - An HTTP request or any payload of type `string`, `xml`, `json`, `byte[]`, `io:ByteChannel`
    #             or `mime:Entity[]`
    # + return - An `HttpFuture` that represents an asynchronous service invocation, or an error if the submission fails
    public function submit(string httpVerb, string path, Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|()
                                                            message) returns HttpFuture|error;

    # Retrieves the `Response` for a previously submitted request.
    #
    # + httpFuture - The `HttpFuture` related to a previous asynchronous invocation
    # + return - An HTTP response message, or an `error` if the invocation fails
    public function getResponse(HttpFuture httpFuture) returns Response|error;

    # Checks whether a `PushPromise` exists for a previously submitted request.
    #
    # + httpFuture - The `HttpFuture` relates to a previous asynchronous invocation
    # + return - A `boolean` that represents whether a `PushPromise` exists
    public function hasPromise(HttpFuture httpFuture) returns boolean;

    # Retrieves the next available `PushPromise` for a previously submitted request.
    #
    # + httpFuture - The `HttpFuture` relates to a previous asynchronous invocation
    # + return - An HTTP Push Promise message, or an `error` if the invocation fails
    public function getNextPromise(HttpFuture httpFuture) returns PushPromise|error;

    # Retrieves the promised server push `Response` message.
    #
    # + promise - The related `PushPromise`
    # + return - A promised HTTP `Response` message, or an `error` if the invocation fails
    public function getPromisedResponse(PushPromise promise) returns Response|error;

    # Rejects a `PushPromise`. When a `PushPromise` is rejected, there is no chance of fetching a promised
    # response using the rejected promise.
    #
    # + promise - The Push Promise to be rejected
    public function rejectPromise(PushPromise promise);
};

# Creates an HTTP client capable of caching HTTP responses.
#
# + url - The URL of the HTTP endpoint to connect to
# + config - The configurations for the client endpoint associated with the caching client
# + cacheConfig - The configurations for the HTTP cache to be used with the caching client
# + return - An `HttpCachingClient` instance which wraps the base `CallerActions` with a caching layer
public function createHttpCachingClient(string url, ClientEndpointConfig config, CacheConfig cacheConfig)
                                                                                                returns CallerActions {
    HttpCachingClient httpCachingClient = new(url, config, cacheConfig);
    log:printDebug("Created HTTP caching client: " + io:sprintf("%s", httpCachingClient));
    return httpCachingClient;
}

function HttpCachingClient::post(string path, Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|()
                                                       message) returns Response|error {
    Request req = buildRequest(message);
    setRequestCacheControlHeader(req);

    match self.httpClient.post(path, req) {
        Response inboundResponse => {
            invalidateResponses(self.cache, inboundResponse, path);
            return inboundResponse;
        }

        error err => return err;
    }
}

function HttpCachingClient::head(string path, Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|()
                                                        message = ()) returns Response|error {
    Request req = buildRequest(message);
    setRequestCacheControlHeader(req);
    return getCachedResponse(self.cache, self.httpClient, req, HEAD, path, self.cacheConfig.isShared);
}

function HttpCachingClient::put(string path, Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|()
                                                        message) returns Response|error {
    Request req = buildRequest(message);
    setRequestCacheControlHeader(req);

    match self.httpClient.put(path, req) {
        Response inboundResponse => {
            invalidateResponses(self.cache, inboundResponse, path);
            return inboundResponse;
        }

        error err => return err;
    }
}

function HttpCachingClient::execute(string httpMethod, string path, Request|string|xml|json|byte[]|
                                                    io:ByteChannel|mime:Entity[]|() message) returns Response|error {
    Request request = buildRequest(message);
    setRequestCacheControlHeader(request);

    if (httpMethod == GET || httpMethod == HEAD) {
        return getCachedResponse(self.cache, self.httpClient, request, httpMethod, path, self.cacheConfig.isShared);
    }

    match self.httpClient.execute(httpMethod, path, request) {
        Response inboundResponse => {
            invalidateResponses(self.cache, inboundResponse, path);
            return inboundResponse;
        }

        error err => return err;
    }
}

function HttpCachingClient::patch(string path, Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|()
                                                        message) returns Response|error {
    Request req = buildRequest(message);
    setRequestCacheControlHeader(req);

    match self.httpClient.patch(path, req) {
        Response inboundResponse => {
            invalidateResponses(self.cache, inboundResponse, path);
            return inboundResponse;
        }

        error err => return err;
    }
}

function HttpCachingClient::delete(string path, Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|()
                                                        message) returns Response|error {
    Request req = buildRequest(message);
    setRequestCacheControlHeader(req);

    match self.httpClient.delete(path, req) {
        Response inboundResponse => {
            invalidateResponses(self.cache, inboundResponse, path);
            return inboundResponse;
        }

        error err => return err;
    }
}

function HttpCachingClient::get(string path, Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|()
                                                        message = ()) returns Response|error {
    Request req = buildRequest(message);
    setRequestCacheControlHeader(req);
    return getCachedResponse(self.cache, self.httpClient, req, GET, path, self.cacheConfig.isShared);
}

function HttpCachingClient::options(string path, Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|()
                                                            message = ()) returns Response|error {
    Request req = buildRequest(message);
    setRequestCacheControlHeader(req);

    match self.httpClient.options(path, message = req) {
        Response inboundResponse => {
            invalidateResponses(self.cache, inboundResponse, path);
            return inboundResponse;
        }

        error err => return err;
    }
}

function HttpCachingClient::forward(string path, Request request) returns Response|error {
    if (request.method == GET || request.method == HEAD) {
        return getCachedResponse(self.cache, self.httpClient, request, request.method, path, self.cacheConfig.isShared);
    }

    match self.httpClient.forward(path, request) {
        Response inboundResponse => {
            invalidateResponses(self.cache, inboundResponse, path);
            return inboundResponse;
        }

        error err => return err;
    }
}

function HttpCachingClient::submit(string httpVerb, string path, Request|string|xml|json|byte[]|io:ByteChannel
                                                                            |mime:Entity[]|() message) returns HttpFuture|error {
    Request req = buildRequest(message);
    return self.httpClient.submit(httpVerb, path, req);
}

function HttpCachingClient::getResponse(HttpFuture httpFuture) returns Response|error {
    return self.httpClient.getResponse(httpFuture);
}

function HttpCachingClient::hasPromise(HttpFuture httpFuture) returns boolean {
    return self.httpClient.hasPromise(httpFuture);
}

function HttpCachingClient::getNextPromise(HttpFuture httpFuture) returns (PushPromise|error) {
    return self.httpClient.getNextPromise(httpFuture);
}

function HttpCachingClient::getPromisedResponse(PushPromise promise) returns Response|error {
    return self.httpClient.getPromisedResponse(promise);
}

function HttpCachingClient::rejectPromise(PushPromise promise) {
    self.httpClient.rejectPromise(promise);
}

function getCachedResponse(HttpCache cache, CallerActions httpClient, Request req, string httpMethod, string path,
                           boolean isShared) returns Response|error {
    time:Time currentT = time:currentTime();
    req.parseCacheControlHeader();

    if (cache.hasKey(getCacheKey(httpMethod, path))) {
        Response cachedResponse = cache.get(getCacheKey(httpMethod, path));
        // Based on https://tools.ietf.org/html/rfc7234#section-4
        log:printDebug("Cached response found for: '" + httpMethod + " " + path + "'");

        updateResponseTimestamps(cachedResponse, currentT.time, currentT.time);
        setAgeHeader(cachedResponse);

        if (isFreshResponse(cachedResponse, isShared)) {
            // If the no-cache directive is not set, responses can be served straight from the cache, without
            // validating with the origin server.
            if (!(req.cacheControl.noCache ?: false) && !(cachedResponse.cacheControl.noCache ?: false)
                                                                                        && !req.hasHeader(PRAGMA)) {
                log:printDebug("Serving a cached fresh response without validating with the origin server");
                return cachedResponse;
            } else {
                log:printDebug("Serving a cached fresh response after validating with the origin server");
                return getValidationResponse(httpClient, req, cachedResponse, cache, currentT, path, httpMethod, true);
            }
        }

        // If a fresh response is not available, serve a stale response, provided that it is not prohibited by
        // a directive and is explicitly allowed in the request.
        if (isAllowedToBeServedStale(req.cacheControl, cachedResponse, isShared)) {

            // If the no-cache directive is not set, responses can be served straight from the cache, without
            // validating with the origin server.
            if (!(req.cacheControl.noCache ?: false) && ! (cachedResponse.cacheControl.noCache ?: false)
                                                                                            && !req.hasHeader(PRAGMA)) {
                log:printDebug("Serving cached stale response without validating with the origin server");
                cachedResponse.setHeader(WARNING, WARNING_110_RESPONSE_IS_STALE);
                return cachedResponse;
            }
        }

        log:printDebug("Validating a stale response for '" + path + "' with the origin server.");
        match getValidationResponse(httpClient, req, cachedResponse, cache, currentT, path, httpMethod, false) {
            Response validatedResponse => {
                updateResponseTimestamps(validatedResponse, currentT.time, time:currentTime().time);
                setAgeHeader(validatedResponse);
                return validatedResponse;
            }

            error err => return err;
        }
    } else {
        log:printDebug("Cached response not found for: '" + httpMethod + " " + path + "'");
    }

    log:printDebug("Sending new request to: " + path);
    var response = sendNewRequest(httpClient, req, path, httpMethod);
    match response {
        Response newResponse => {
            if (cache.isAllowedToCache(newResponse)) {
                newResponse.requestTime = currentT.time;
                newResponse.receivedTime = time:currentTime().time;
                cache.put(getCacheKey(httpMethod, path), req.cacheControl, newResponse);
            }

            return newResponse;
        }

        error err => return err;
    }
}

function getValidationResponse(CallerActions httpClient, Request req, Response cachedResponse, HttpCache cache,
                               time:Time currentT, string path, string httpMethod, boolean isFreshResponse)
                                                                                returns Response|error {
    // If the no-cache directive is set, always validate the response before serving
    Response validationResponse = new; // TODO: May have to make this Response?

    if (isFreshResponse) {
        log:printDebug("Sending validation request for a fresh response");
    } else {
        log:printDebug("Sending validation request for a stale response");
    }

    match sendValidationRequest(httpClient, path, cachedResponse) {
        Response resp => validationResponse = resp;
        error validationErr => {
            // Based on https://tools.ietf.org/html/rfc7234#section-4.2.4
            // This behaviour is based on the fact that currently error structs are returned only
            // if the connection is refused or the connection times out.
            // TODO: Verify that this behaviour is valid: returning a fresh response when 'no-cache' is present and
            // origin server couldn't be reached.
            updateResponseTimestamps(cachedResponse, currentT.time, time:currentTime().time);
            setAgeHeader(cachedResponse);
            if (!isFreshResponse) {
                // If the origin server cannot be reached and a fresh response is unavailable, serve a stale
                // response (unless it is prohibited through a directive).
                cachedResponse.setHeader(WARNING, WARNING_111_REVALIDATION_FAILED);
                log:printDebug("Cannot reach origin server. Serving a stale response");
            } else {
                log:printDebug("Cannot reach origin server. Serving a fresh response");
            }
            return validationErr;
        }
    }

    log:printDebug("Response for validation request received");
    // Based on https://tools.ietf.org/html/rfc7234#section-4.3.3
    if (validationResponse.statusCode == NOT_MODIFIED_304) {
        return handle304Response(validationResponse, cachedResponse, cache, path, httpMethod);
    } else if (validationResponse.statusCode >= 500 && validationResponse.statusCode < 600) {
        // May forward the response or act as if the origin server failed to respond and serve a
        // stored response
        // TODO: Make the above mentioned behaviour user-configurable
        return validationResponse;
    } else {
        // Forward the received response and replace the stored responses
        validationResponse.requestTime = currentT.time;
        match req.cacheControl {
            RequestCacheControl reqCC => cache.put(getCacheKey(httpMethod, path), reqCC, validationResponse);
            () => {}
        }
        log:printDebug("Received a full response. Storing it in cache and forwarding to the client");
        return validationResponse;
    }
}

// Based on https://tools.ietf.org/html/rfc7234#section-4.3.4
function handle304Response(Response validationResponse, Response cachedResponse, HttpCache cache, string path,
                           string httpMethod) returns Response|error {
    if (validationResponse.hasHeader(ETAG)) {
        string etag = validationResponse.getHeader(ETAG);

        if (isAStrongValidator(etag)) {
            // Assuming ETags are the only strong validators
            Response[] matchingCachedResponses = cache.getAllByETag(getCacheKey(httpMethod, path), etag);

            foreach resp in matchingCachedResponses {
                updateResponse(resp, validationResponse);
            }
            log:printDebug("304 response received, with a strong validator. Response(s) updated");
            return cachedResponse;
        } else if (hasAWeakValidator(validationResponse, etag)) {
            // The weak validator should be either an ETag or a last modified date. Precedence given to ETag
            Response[] matchingCachedResponses = cache.getAllByWeakETag(getCacheKey(httpMethod, path), etag);

            foreach resp in matchingCachedResponses {
                updateResponse(resp, validationResponse);
            }
            log:printDebug("304 response received, with a weak validator. Response(s) updated");
            return cachedResponse;
        }
    }

    // Not checking the ETag in validation since it's already checked above.
    // TODO: Need to check whether cachedResponse is the only matching response
    if (!cachedResponse.hasHeader(ETAG) && !cachedResponse.hasHeader(LAST_MODIFIED) &&
                                                        !validationResponse.hasHeader(LAST_MODIFIED)) {
        log:printDebug("304 response received and stored response do not have validators. Updating the stored response.");
        updateResponse(cachedResponse, validationResponse);
    }

    log:printDebug("304 response received, but stored responses were not updated.");
    // TODO: Check if this behaviour is the expected one
    return cachedResponse;
}

// Based on https://tools.ietf.org/html/rfc7234#section-4.4
function invalidateResponses(HttpCache httpCache, Response inboundResponse, string path) {
    // TODO: Improve this logic in accordance with the spec
    if (isCacheableStatusCode(inboundResponse.statusCode) &&
        inboundResponse.statusCode >= 200 && inboundResponse.statusCode < 400) {
        httpCache.cache.remove(getCacheKey(GET, path));
        httpCache.cache.remove(getCacheKey(HEAD, path));
    }
}

// Based on https://tools.ietf.org/html/rfc7234#section-4.2.1
function getFreshnessLifetime(Response cachedResponse, boolean isSharedCache) returns int {
    // TODO: Ensure that duplicate directives are not counted towards freshness lifetime.
    match cachedResponse.cacheControl {
        ResponseCacheControl respCC => {
            if (isSharedCache && respCC.sMaxAge >= 0) {
                return respCC.sMaxAge;
            }

            if (respCC.maxAge >= 0) {
                return respCC.maxAge;
            }
        }


        () => {}
    }

    // At this point, there should be exactly one Expires header to calculate the freshness lifetime.
    // When adding heuristic calculations, the condition would change to >1.
    if (cachedResponse.hasHeader(EXPIRES)) {
        string[] expiresHeader = cachedResponse.getHeaders(EXPIRES);

        if (lengthof expiresHeader == 1) {
            if (cachedResponse.hasHeader(DATE)) {
                string[] dateHeader = cachedResponse.getHeaders(DATE);

                if (lengthof dateHeader == 1) {
                    // TODO: See if time parsing errors need to be handled
                    int freshnessLifetime = (time:parse(expiresHeader[0], time:TIME_FORMAT_RFC_1123).time
                            - time:parse(dateHeader[0], time:TIME_FORMAT_RFC_1123).time) / 1000;
                    return freshnessLifetime;
                }
            }
        }
    }

    // TODO: Add heuristic freshness lifetime calculation

    return STALE;
}

function isFreshResponse(Response cachedResponse, boolean isSharedCache) returns boolean {
    int currentAge = getResponseAge(cachedResponse);
    int freshnessLifetime = getFreshnessLifetime(cachedResponse, isSharedCache);
    return freshnessLifetime > currentAge;
}

// Based on https://tools.ietf.org/html/rfc7234#section-4.2.4
function isAllowedToBeServedStale(RequestCacheControl? requestCacheControl, Response cachedResponse,
                                  boolean isSharedCache) returns boolean {
    // A cache MUST NOT generate a stale response if it is prohibited by an explicit in-protocol directive
    match cachedResponse.cacheControl {
        ResponseCacheControl respCC => {
            if (isServingStaleProhibited(requestCacheControl, respCC)) {
                return false;
            }
        }

        () => return false;
    }

    return isStaleResponseAccepted(requestCacheControl, cachedResponse, isSharedCache);
}

// Based on https://tools.ietf.org/html/rfc7234#section-4.2.4
function isServingStaleProhibited(RequestCacheControl? requestCacheControl,
                                  ResponseCacheControl? responseCacheControl) returns boolean {
    // A cache MUST NOT generate a stale response if it is prohibited by an explicit in-protocol directive
    return (requestCacheControl.noStore ?: false) ||
        (requestCacheControl.noCache ?: false) ||
        (responseCacheControl.mustRevalidate ?: false) ||
        (responseCacheControl.proxyRevalidate ?: false) ||
        ((responseCacheControl.sMaxAge ?: -1) >= 0);
}

// Based on https://tools.ietf.org/html/rfc7234#section-4.2.4
function isStaleResponseAccepted(RequestCacheControl? requestCacheControl, Response cachedResponse,
                                 boolean isSharedCache) returns boolean {
    if ((requestCacheControl.maxStale ?: -1) == MAX_STALE_ANY_AGE) {
        return true;
    } else if ((requestCacheControl.maxStale ?: -1) >=
                            (getResponseAge(cachedResponse) - getFreshnessLifetime(cachedResponse, isSharedCache))) {
        return true;
    }
    return false;
}

// Based https://tools.ietf.org/html/rfc7234#section-4.3.1
function sendValidationRequest(CallerActions httpClient, string path, Response cachedResponse) returns Response|error {
    Request validationRequest = new;

    if (cachedResponse.hasHeader(ETAG)) {
        validationRequest.setHeader(IF_NONE_MATCH, cachedResponse.getHeader(ETAG));
    }

    if (cachedResponse.hasHeader(LAST_MODIFIED)) {
        validationRequest.setHeader(IF_MODIFIED_SINCE, cachedResponse.getHeader(LAST_MODIFIED));
    }

    // TODO: handle cases where neither of the above 2 headers are present

    match httpClient.get(path, message = validationRequest) {
        Response validationResponse => return validationResponse;

        error err => return err;
    }
}

function sendNewRequest(CallerActions httpClient, Request request, string path, string httpMethod)
                                                                                returns Response|error {
    if (httpMethod == GET) {
        return httpClient.get(path, message = request);
    } else if (httpMethod == HEAD) {
        return httpClient.head(path, message = request);
    } else {
        error err = {message:"HTTP method not supported in caching client: " + httpMethod};
        return err;
    }
}

function setAgeHeader(Response cachedResponse) {
    cachedResponse.setHeader(AGE, "" + calculateCurrentResponseAge(cachedResponse));
}

// Based on https://tools.ietf.org/html/rfc7234#section-4.2.3
function calculateCurrentResponseAge(Response cachedResponse) returns int {
    int ageValue = getResponseAge(cachedResponse);
    int dateValue = getDateValue(cachedResponse);
    int now = time:currentTime().time;
    int responseTime = cachedResponse.receivedTime;
    int requestTime = cachedResponse.requestTime;

    int apparentAge = (responseTime - dateValue) >= 0 ? (responseTime - dateValue) : 0;

    int responseDelay = responseTime - requestTime;
    int correctedAgeValue = ageValue + responseDelay;

    int correctedInitialAge = apparentAge > correctedAgeValue ? apparentAge : correctedAgeValue;
    int residentTime = now - responseTime;

    return (correctedInitialAge + residentTime) / 1000;
}

// Based on https://tools.ietf.org/html/rfc7234#section-4.3.4
function updateResponse(Response cachedResponse, Response validationResponse) {
    // 1 - delete warning headers with warn codes 1xx
    // 2 - retain warning headers with warn codes 2xx
    // 3 - use other headers in validation response to replace corresponding headers in cached response
    retain2xxWarnings(cachedResponse);
    replaceHeaders(cachedResponse, validationResponse);
}

// Based on https://tools.ietf.org/html/rfc7234#section-4.3.4
function hasAWeakValidator(Response validationResponse, string etag) returns boolean {
    return (validationResponse.hasHeader(LAST_MODIFIED) || !isAStrongValidator(etag));
}

// Based on https://tools.ietf.org/html/rfc7234#section-4.3.4
function isAStrongValidator(string etag) returns boolean {
    // TODO: Consider cases where Last-Modified can also be treated as a strong validator as per
    // https://tools.ietf.org/html/rfc7232#section-2.2.2
    if (!etag.hasPrefix(WEAK_VALIDATOR_TAG)) {
        return true;
    }

    return false;
}

// Based on https://tools.ietf.org/html/rfc7234#section-4.3.4
function replaceHeaders(Response cachedResponse, Response validationResponse) {
    string[] headerNames = untaint validationResponse.getHeaderNames();

    log:printDebug("Updating response headers using validation response.");
    
    foreach headerName in headerNames {
        cachedResponse.removeHeader(headerName);
        string[] headerValues = validationResponse.getHeaders(headerName);
        foreach value in headerValues {
            cachedResponse.addHeader(headerName, value);
        }
    }
}

function retain2xxWarnings(Response cachedResponse) {
    if (cachedResponse.hasHeader(WARNING)) {
        string[] warningHeaders = cachedResponse.getHeaders(WARNING);
        cachedResponse.removeHeader(WARNING);
        // TODO: Need to handle this in a better way using regex when the required regex APIs are there
        foreach warningHeader in warningHeaders {
            if (warningHeader.contains("214") || warningHeader.contains("299")) {
                log:printDebug("Adding warning header: " + warningHeader);
                cachedResponse.addHeader(WARNING, warningHeader);
                continue;
            }
        }
    }
}

function getResponseAge(Response cachedResponse) returns int {
    if (!cachedResponse.hasHeader(AGE)) {
        return 0;
    }

    match <int>cachedResponse.getHeader(AGE) {
        int ageValue => return ageValue;
        error => return 0;
    }
}

function updateResponseTimestamps(Response response, int requestedTime, int receivedTime) {
    response.requestTime = requestedTime;
    response.receivedTime = receivedTime;
}

function getDateValue(Response inboundResponse) returns int {
    // Based on https://tools.ietf.org/html/rfc7231#section-7.1.1.2
    if (!inboundResponse.hasHeader(DATE)) {
        log:printDebug("Date header not found. Using current time for the Date header.");
        time:Time currentT = time:currentTime();
        inboundResponse.setHeader(DATE, currentT.format(time:TIME_FORMAT_RFC_1123));
        return currentT.time;
    }

    string dateHeader = inboundResponse.getHeader(DATE);
    // TODO: May need to handle invalid date headers
    time:Time dateHeaderTime = time:parse(dateHeader, time:TIME_FORMAT_RFC_1123);
    return dateHeaderTime.time;
}

function getWarningAgent() returns string {
    string ballerinaVersion = runtime:getProperty("ballerina.version");
    return "ballerina-http-caching-client/" + ballerinaVersion;
}
