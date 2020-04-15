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
import ballerina/runtime;
import ballerina/time;
import ballerina/io;
import ballerina/lang.'int;

final string WARNING_AGENT = getWarningAgent();

final string WARNING_110_RESPONSE_IS_STALE = "110 " + WARNING_AGENT + " \"Response is Stale\"";
final string WARNING_111_REVALIDATION_FAILED = "111 " + WARNING_AGENT + " \"Revalidation Failed\"";

const string WEAK_VALIDATOR_TAG = "W/";
const int STALE = 0;

const string FORWARD = "FORWARD";
const string GET = "GET";
const string POST = "POST";
const string DELETE = "DELETE";
const string OPTIONS = "OPTIONS";
const string PUT = "PUT";
const string PATCH = "PATCH";
const string HEAD = "HEAD";

# Used for configuring the caching behaviour. Setting the `policy` field in the `CacheConfig` record allows
# the user to control the caching behaviour.
public type CachingPolicy CACHE_CONTROL_AND_VALIDATORS|RFC_7234;

# This is a more restricted mode of RFC 7234. Setting this as the caching policy restricts caching to instances
# where the `cache-control` header and either the `etag` or `last-modified` header are present.
public const CACHE_CONTROL_AND_VALIDATORS = "CACHE_CONTROL_AND_VALIDATORS";

# Caching behaviour is as specified by the RFC 7234 specification.
public const RFC_7234 = "RFC_7234";

# Provides a set of configurations for controlling the caching behaviour of the endpoint.
#
# + enabled - Specifies whether HTTP caching is enabled. Caching is disabled by default.
# + isShared - Specifies whether the HTTP caching layer should behave as a public cache or a private cache
# + capacity - The capacity of the cache
# + evictionFactor - The fraction of entries to be removed when the cache is full. The value should be
#                    between 0 (exclusive) and 1 (inclusive).
# + policy - Gives the user some control over the caching behaviour. By default, this is set to
#            `CACHE_CONTROL_AND_VALIDATORS`. The default behaviour is to allow caching only when the `cache-control`
#            header and either the `etag` or `last-modified` header are present.
public type CacheConfig record {|
    boolean enabled = false;
    boolean isShared = false;
    int capacity = 8388608; // 8MB
    float evictionFactor = 0.2;
    CachingPolicy policy = CACHE_CONTROL_AND_VALIDATORS;
|};

# An HTTP caching client implementation which takes an `HttpActions` instance and wraps it with an HTTP caching layer.
#
# + url - The URL of the remote HTTP endpoint
# + config - The configurations of the client endpoint associated with this `CachingActions` instance
# + httpClient - The underlying `HttpActions` instance which will be making the actual network calls
# + cache - The cache storage for the HTTP responses
# + cacheConfig - Configurations for the underlying cache storage and for controlling the HTTP caching behaviour
public type HttpCachingClient client object {

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
    public function __init(string url, ClientConfiguration config, CacheConfig cacheConfig) {
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
    public remote function head(string path, public RequestMessage message = ()) returns @tainted Response|ClientError {
        Request req = <Request>message;
        setRequestCacheControlHeader(req);
        return getCachedResponse(self.cache, self.httpClient, req, HEAD, path, self.cacheConfig.isShared, false);
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
    public remote function execute(string httpMethod, string path, RequestMessage message) returns @tainted Response|ClientError {

        Request request = <Request>message;
        setRequestCacheControlHeader(request);

        if (httpMethod == GET || httpMethod == HEAD) {
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
    public remote function delete(string path, public RequestMessage message = ()) returns Response|ClientError {
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
    public remote function get(string path, public RequestMessage message = ()) returns @tainted Response|ClientError {
        Request req = <Request>message;
        setRequestCacheControlHeader(req);
        return getCachedResponse(self.cache, self.httpClient, req, GET, path, self.cacheConfig.isShared, false);
    }

    # Responses returned for OPTIONS requests are not cacheable. Therefore, the requests are simply directed to the
    # origin server. Responses received for OPTIONS requests invalidate the cached responses for the same resource.
    #
    # + path - Request path
    # + message - An optional HTTP request or any payload of type `string`, `xml`, `json`, `byte[]`, `io:ReadableByteChannel`
    #             or `mime:Entity[]`
    # + return - The response for the request or an `http:ClientError` if failed to establish communication with the upstream server
    public remote function options(string path, public RequestMessage message = ()) returns Response|ClientError {
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
        if (request.method == GET || request.method == HEAD) {
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
};

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
    log:printDebug(function() returns string {
        return "Created HTTP caching client: " + io:sprintf("%s", httpCachingClient);
    });
    return httpCachingClient;
}

function getCachedResponse(HttpCache cache, HttpClient httpClient, @tainted Request req, string httpMethod, string path,
                           boolean isShared, boolean forwardRequest) returns @tainted Response|ClientError {
    time:Time currentT = time:currentTime();
    req.parseCacheControlHeader();

    if (cache.hasKey(getCacheKey(httpMethod, path))) {
        Response cachedResponse = cache.get(getCacheKey(httpMethod, path));
        // Based on https://tools.ietf.org/html/rfc7234#section-4
        log:printDebug(function() returns string {
            return "Cached response found for: '" + httpMethod + " " + path + "'";
        });

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
        if (isAllowedToBeServedStale(req.cacheControl, cachedResponse, isShared)) {

            // If the no-cache directive is not set, responses can be served straight from the cache, without
            // validating with the origin server.
            if (!isNoCacheSet(reqCache, resCache) && !req.hasHeader(PRAGMA)) {
                log:printDebug("Serving cached stale response without validating with the origin server");
                cachedResponse.setHeader(WARNING, WARNING_110_RESPONSE_IS_STALE);
                return cachedResponse;
            }
        }

        log:printDebug(function() returns string {
            return "Validating a stale response for '" + path + "' with the origin server.";
        });
        var validatedResponse = getValidationResponse(httpClient, req, cachedResponse, cache, currentT, path,
                                                            httpMethod, false);
        if (validatedResponse is Response) {
            updateResponseTimestamps(validatedResponse, currentT.time, time:currentTime().time);
            setAgeHeader(validatedResponse);
        }
        return validatedResponse;
    } else {
        log:printDebug(function() returns string {
            return "Cached response not found for: '" + httpMethod + " " + path + "'";
        });
    }

    log:printDebug(function() returns string {
        return "Sending new request to: " + path;
    });
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

function isNoCacheSet(RequestCacheControl? reqCC, ResponseCacheControl? resCC) returns boolean {
    if (reqCC is RequestCacheControl && reqCC.noCache) {
        return true;
    }

    if (resCC is ResponseCacheControl && resCC.noCache) {
        return true;
    }

    return false;
}

function getValidationResponse(HttpClient httpClient, Request req, Response cachedResponse, HttpCache cache,
                               time:Time currentT, string path, string httpMethod, boolean isFreshResponse)
                                                                                returns @tainted Response|ClientError {
    // If the no-cache directive is set, always validate the response before serving
    Response validationResponse = new; // TODO: May have to make this Response?

    if (isFreshResponse) {
        log:printDebug("Sending validation request for a fresh response");
    } else {
        log:printDebug("Sending validation request for a stale response");
    }

    var response = sendValidationRequest(httpClient, path, cachedResponse);
    if (response is Response) {
        validationResponse = response;
    } else {
    // Based on https://tools.ietf.org/html/rfc7234#section-4.2.4
    // This behaviour is based on the fact that currently error structs are returned only
    // if the connection is refused or the connection times out.
    // TODO: Verify that this behaviour is valid: returning a fresh response when 'no-cache' is present and
    // origin server couldn't be reached.
    updateResponseTimestamps(cachedResponse, currentT.time, time:currentTime().time);
    setAgeHeader(<@untainted> cachedResponse);
    if (!isFreshResponse) {
        // If the origin server cannot be reached and a fresh response is unavailable, serve a stale
        // response (unless it is prohibited through a directive).
        cachedResponse.setHeader(WARNING, WARNING_111_REVALIDATION_FAILED);
        log:printDebug("Cannot reach origin server. Serving a stale response");
    } else {
        log:printDebug("Cannot reach origin server. Serving a fresh response");
    }
    return response;
    }

    log:printDebug("Response for validation request received");
    // Based on https://tools.ietf.org/html/rfc7234#section-4.3.3
    if (validationResponse.statusCode == STATUS_NOT_MODIFIED) {
        return handle304Response(validationResponse, cachedResponse, cache, path, httpMethod);
    } else if (validationResponse.statusCode >= 500 && validationResponse.statusCode < 600) {
        // May forward the response or act as if the origin server failed to respond and serve a
        // stored response
        // TODO: Make the above mentioned behaviour user-configurable
        return validationResponse;
    } else {
        // Forward the received response and replace the stored responses
        validationResponse.requestTime = currentT.time;
        if (req.cacheControl is RequestCacheControl) {
            cache.put(getCacheKey(httpMethod, path), req.cacheControl, validationResponse);
        }
        log:printDebug("Received a full response. Storing it in cache and forwarding to the client");
        return validationResponse;
    }
}

// Based on https://tools.ietf.org/html/rfc7234#section-4.3.4
function handle304Response(Response validationResponse, Response cachedResponse, HttpCache cache, string path,
                           string httpMethod) returns @tainted Response|ClientError {
    if (validationResponse.hasHeader(ETAG)) {
        string etag = validationResponse.getHeader(ETAG);

        if (isAStrongValidator(etag)) {
            // Assuming ETags are the only strong validators
            Response[] matchingCachedResponses = cache.getAllByETag(getCacheKey(httpMethod, path), etag);

            foreach var resp in matchingCachedResponses {
                updateResponse(resp, <@untainted> validationResponse);
            }
            log:printDebug("304 response received, with a strong validator. Response(s) updated");
            return cachedResponse;
        } else if (hasAWeakValidator(validationResponse, etag)) {
            // The weak validator should be either an ETag or a last modified date. Precedence given to ETag
            Response[] matchingCachedResponses = cache.getAllByWeakETag(getCacheKey(httpMethod, path), etag);

            foreach var resp in matchingCachedResponses {
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
        updateResponse(<@untainted> cachedResponse, validationResponse);
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
        cache:Error? result = httpCache.cache.invalidate(getCacheKey(GET, path));
        if (result is cache:Error) {
            log:printDebug(function() returns string {
                return "Failed to remove the key: " + getCacheKey(GET, path) + " from the cache.";
            });
        }
        result = httpCache.cache.invalidate(getCacheKey(HEAD, path));
        if (result is cache:Error) {
            log:printDebug(function() returns string {
                return "Failed to remove the key: " + getCacheKey(GET, path) + " from the cache.";
            });
        }
    }
}

// Based on https://tools.ietf.org/html/rfc7234#section-4.2.1
function getFreshnessLifetime(Response cachedResponse, boolean isSharedCache) returns int {
    // TODO: Ensure that duplicate directives are not counted towards freshness lifetime.
    var responseCacheControl = cachedResponse.cacheControl;
    if (responseCacheControl is ResponseCacheControl) {
        if (isSharedCache && responseCacheControl.sMaxAge >= 0) {
            return responseCacheControl.sMaxAge;
        }

        if (responseCacheControl.maxAge >= 0) {
            return responseCacheControl.maxAge;
        }
    }

    // At this point, there should be exactly one Expires header to calculate the freshness lifetime.
    // When adding heuristic calculations, the condition would change to >1.
    if (cachedResponse.hasHeader(EXPIRES)) {
        string[] expiresHeader = cachedResponse.getHeaders(EXPIRES);

        if (expiresHeader.length() == 1) {
            if (cachedResponse.hasHeader(DATE)) {
                string[] dateHeader = cachedResponse.getHeaders(DATE);

                if (dateHeader.length() == 1) {
                    var tExpiresHeader = time:parse(expiresHeader[0], time:TIME_FORMAT_RFC_1123);
                    var tDateHeader = time:parse(dateHeader[0], time:TIME_FORMAT_RFC_1123);
                    if (tExpiresHeader is time:Time && tDateHeader is time:Time) {
                        int freshnessLifetime = (tExpiresHeader.time - tDateHeader.time) /1000;
                        return freshnessLifetime;
                    }
                }
            }
        }
    }

    // TODO: Add heuristic freshness lifetime calculation

    return STALE;
}

function isFreshResponse(Response cachedResponse, boolean isSharedCache) returns @tainted boolean {
    int currentAge = getResponseAge(cachedResponse);
    int freshnessLifetime = getFreshnessLifetime(cachedResponse, isSharedCache);
    return freshnessLifetime > currentAge;
}

// Based on https://tools.ietf.org/html/rfc7234#section-4.2.4
function isAllowedToBeServedStale(RequestCacheControl? requestCacheControl, Response cachedResponse,
                                  boolean isSharedCache) returns boolean {
    // A cache MUST NOT generate a stale response if it is prohibited by an explicit in-protocol directive
    var responseCacheControl = cachedResponse.cacheControl;
    if (responseCacheControl is ResponseCacheControl) {
        if (isServingStaleProhibited(requestCacheControl, responseCacheControl)) {
            return false;
        }
    } else {
        return false;
    }
    return isStaleResponseAccepted(requestCacheControl, cachedResponse, isSharedCache);
}

// Based on https://tools.ietf.org/html/rfc7234#section-4.2.4
function isServingStaleProhibited(RequestCacheControl? reqCC, ResponseCacheControl? resCC) returns boolean {
    // A cache MUST NOT generate a stale response if it is prohibited by an explicit in-protocol directive
    if (reqCC is RequestCacheControl) {
        if (reqCC.noCache || reqCC.noStore) {
            return true;
        }
    }

    if (resCC is ResponseCacheControl) {
        if (resCC.mustRevalidate || resCC.proxyRevalidate || (resCC.sMaxAge >= 0)) {
            return true;
        }
    }

    return false;
}

// Based on https://tools.ietf.org/html/rfc7234#section-4.2.4
function isStaleResponseAccepted(RequestCacheControl? requestCacheControl, Response cachedResponse,
                                 boolean isSharedCache) returns boolean {
    if (requestCacheControl is RequestCacheControl) {
        if (requestCacheControl.maxStale == MAX_STALE_ANY_AGE) {
            return true;
        } else if (requestCacheControl.maxStale >=
                                (getResponseAge(cachedResponse) - getFreshnessLifetime(cachedResponse, isSharedCache))) {
            return true;
        }
    }
    return false;
}

// Based https://tools.ietf.org/html/rfc7234#section-4.3.1
function sendValidationRequest(HttpClient httpClient, string path, Response cachedResponse) returns Response|ClientError {
    Request validationRequest = new;

    if (cachedResponse.hasHeader(ETAG)) {
        validationRequest.setHeader(IF_NONE_MATCH, cachedResponse.getHeader(ETAG));
    }

    if (cachedResponse.hasHeader(LAST_MODIFIED)) {
        validationRequest.setHeader(IF_MODIFIED_SINCE, cachedResponse.getHeader(LAST_MODIFIED));
    }

    // TODO: handle cases where neither of the above 2 headers are present

    return httpClient->get(path, message = validationRequest);
}

function sendNewRequest(HttpClient httpClient, Request request, string path, string httpMethod, boolean forwardRequest)
                                                                                returns Response|ClientError {
    if (forwardRequest) {
        return httpClient->forward(path, request);
    }
    if (httpMethod == GET) {
        return httpClient->get(path, message = request);
    } else if (httpMethod == HEAD) {
        return httpClient->head(path, message = request);
    } else {
        string message = "HTTP method not supported in caching client: " + httpMethod;
        UnsupportedActionError err = error(UNSUPPORTED_ACTION, message = message);
        return err;
    }
}

function setAgeHeader(Response cachedResponse) {
    cachedResponse.setHeader(<@untainted> AGE, <@untainted> calculateCurrentResponseAge(cachedResponse).toString());
}

// Based on https://tools.ietf.org/html/rfc7234#section-4.2.3
function calculateCurrentResponseAge(Response cachedResponse) returns @tainted int {
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
    if (!etag.startsWith(WEAK_VALIDATOR_TAG)) {
        return true;
    }

    return false;
}

// Based on https://tools.ietf.org/html/rfc7234#section-4.3.4
function replaceHeaders(Response cachedResponse, Response validationResponse) {
    string[] headerNames = <@untainted> validationResponse.getHeaderNames();

    log:printDebug("Updating response headers using validation response.");

    foreach var headerName in headerNames {
        cachedResponse.removeHeader(headerName);
        string[] headerValues = <@untainted> validationResponse.getHeaders(headerName);
        foreach var value in headerValues {
            cachedResponse.addHeader(headerName, value);
        }
    }
}

function retain2xxWarnings(Response cachedResponse) {
    if (cachedResponse.hasHeader(WARNING)) {
        string[] warningHeaders = <@untainted> cachedResponse.getHeaders(WARNING);
        cachedResponse.removeHeader(WARNING);
        // TODO: Need to handle this in a better way using regex when the required regex APIs are there
        foreach var warningHeader in warningHeaders {
            if (warningHeader.indexOf("214") is int || warningHeader.indexOf("299") is int) {
                log:printDebug(function() returns string {
                    return "Adding warning header: " + warningHeader;
                });
                cachedResponse.addHeader(WARNING, warningHeader);
                continue;
            }
        }
    }
}

function getResponseAge(Response cachedResponse) returns @tainted int {
    if (!cachedResponse.hasHeader(AGE)) {
        return 0;
    }

    string ageHeaderString = cachedResponse.getHeader(AGE);
    var ageValue = 'int:fromString(ageHeaderString);
    if (ageValue is int) {
        return ageValue;
    }
    return 0;
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
        var timeStr = time:format(currentT, time:TIME_FORMAT_RFC_1123);
        if (timeStr is string) {
            inboundResponse.setHeader(DATE, timeStr);
        }
        return currentT.time;
    }

    string dateHeader = inboundResponse.getHeader(DATE);
    // TODO: May need to handle invalid date headers
    var dateHeaderTime = time:parse(dateHeader, time:TIME_FORMAT_RFC_1123);
    if (dateHeaderTime is time:Time) {
        return dateHeaderTime.time;
    } else {
        return 0;
    }
}

function getWarningAgent() returns string {
    string ballerinaVersion = runtime:getProperty("ballerina.version");
    return "ballerina-http-caching-client/" + ballerinaVersion;
}
