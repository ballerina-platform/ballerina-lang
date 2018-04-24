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

documentation {
    Used for configuring the caching behaviour. Setting the `policy` field in the `CacheConfig` record allows
    the user to control the caching behaviour.

    F{{CACHE_CONTROL_AND_VALIDATORS}} This a more restricted mode of RFC 7234. This restricts caching to instances
                                      where the Cache-Control header and either the ETag or Last-Modified header
                                      are present.
    F{{RFC_7234}} Caching behaviour is as specified by the RFC 7234 specification.
}
public type CachingPolicy "CACHE_CONTROL_AND_VALIDATORS"|"RFC_7234";

public CachingPolicy CACHE_CONTROL_AND_VALIDATORS = "CACHE_CONTROL_AND_VALIDATORS";
public CachingPolicy RFC_7234 = "RFC_7234";

documentation {
    Provides a set of configurations for controlling the caching behaviour of the endpoint.

    F{{enabled}} Specifies whether HTTP caching is enabled. Caching is enabled by default.
    F{{isShared}} Specifies whether the HTTP caching layer should behave as a public cache or a private cache
    F{{expiryTimeMillis}} The number of milliseconds to keep an entry in the cache
    F{{capacity}} The capacity of the cache
    F{{evictionFactor}} The fraction of entries to be removed when the cache is full. The value should be
                        between 0 (exclusive) and 1 (inclusive).
    F{{policy}} Gives the user some control over the caching behaviour. By default, this is set to
                `CACHE_CONTROL_AND_VALIDATORS`. The default behaviour is to allow caching only when the `cache-control`
                header and either the `etag` or `last-modified` header are present.
}
public type CacheConfig {
    boolean enabled = true,
    boolean isShared = false,
    int expiryTimeMillis = 86400,
    int capacity = 8388608, // 8MB
    float evictionFactor = 0.2,
    CachingPolicy policy = CACHE_CONTROL_AND_VALIDATORS,
};

documentation {
    An HTTP caching client implementation which takes an `HttpActions` instance and wraps it with an HTTP caching layer.

    F{{serviceUri}} The URL of the remote HTTP endpoint
    F{{config}} The configurations of the client endpoint associated with this `CachingActions` instance
    F{{httpClient}} The underlying `HttpActions` instance which will be making the actual network calls
    F{{cache}} The cache storage for the HTTP responses
    F{{cacheConfig}} Caching configurations for the HTTP cache
}
public type HttpCachingClient object {

    public {
        string serviceUri;
        ClientEndpointConfig config;
        CallerActions httpClient;
        HttpCache cache;
        CacheConfig cacheConfig;
    }

    public new(serviceUri, config, cacheConfig) {
        self.httpClient = createHttpSecureClient(serviceUri, config);
        self.cache = createHttpCache("http-cache", cacheConfig);
    }

    documentation {
        Responses returned for POST requests are not cacheable. Therefore, the requests are simply directed to the
        origin server. Responses received for POST requests invalidate the cached responses for the same resource.

        P{{path}} Resource path
        P{{request}} An optional HTTP request
        R{{}} The inbound response message
        R{{}} The error occurred while attempting to fulfill the HTTP request (if any)
    }
    public function post(string path, Request? request = ()) returns Response|HttpConnectorError;

    documentation {
        Responses for HEAD requests are cacheable and as such, will be routed through the HTTP cache. Only if a
        suitable response cannot be found will the request be directed to the origin server.

        P{{path}} Resource path
        P{{request}} An optional HTTP request
        R{{}} The inbound response message
        R{{}} The error occurred while attempting to fulfill the HTTP request (if any)
    }
    public function head(string path, Request? request = ()) returns Response|HttpConnectorError;

    documentation {
        Responses returned for PUT requests are not cacheable. Therefore, the requests are simply directed to the
        origin server. In addition, PUT requests invalidate the currently stored responses for the given path.

        P{{path}} Resource path
        P{{request}} An optional HTTP request
        R{{}} The inbound response message
        R{{}} The error occurred while attempting to fulfill the HTTP request (if any)
    }
    public function put(string path, Request? request = ()) returns Response|HttpConnectorError;

    documentation {
        Invokes an HTTP call with the specified HTTP method. This is not a cacheable operation, unless the HTTP method
        used is GET or HEAD.

        P{{httpMethod}} HTTP method to be used for the request
        P{{path}} Resource path
        P{{request}} An HTTP request
        R{{}} The inbound response message
        R{{}} Error occurred during HTTP client invocation
    }
    public function execute(string httpMethod, string path, Request request) returns Response|HttpConnectorError;

    documentation {
        Responses returned for PATCH requests are not cacheable. Therefore, the requests are simply directed to
        the origin server. Responses received for PATCH requests invalidate the cached responses for the same resource.

        P{{path}} Resource path
        P{{request}} An optional HTTP request
        R{{}} The inbound response message
        R{{}} The error occurred while attempting to fulfill the HTTP request (if any)
    }
    public function patch(string path, Request? request = ()) returns Response|HttpConnectorError;

    documentation {
        Responses returned for DELETE requests are not cacheable. Therefore, the requests are simply directed to the
        origin server. Responses received for DELETE requests invalidate the cached responses for the same resource.

        P{{path}} Resource path
        P{{request}} An optional HTTP request
        R{{}} The inbound response message
        R{{}} The error occurred while attempting to fulfill the HTTP request (if any)
    }
    public function delete(string path, Request? request = ()) returns Response|HttpConnectorError;

    documentation {
        Responses for GET requests are cacheable and as such, will be routed through the HTTP cache. Only if a suitable
        response cannot be found will the request be directed to the origin server.

        P{{path}} Request path
        P{{request}} An optional HTTP request
        R{{}} The inbound response message
        R{{}} The error occurred while attempting to fulfill the HTTP request (if any)
    }
    public function get(string path, Request? request = ()) returns Response|HttpConnectorError;

    documentation {
        Responses returned for OPTIONS requests are not cacheable. Therefore, the requests are simply directed to the
        origin server. Responses received for OPTIONS requests invalidate the cached responses for the same resource.

        P{{path}} Request path
        P{{request}} An optional HTTP request
        R{{}} The inbound response message
        R{{}} The error occurred while attempting to fulfill the HTTP request (if any)
    }
    public function options(string path, Request? request = ()) returns Response|HttpConnectorError;

    documentation {
        Forward action can be used to invoke an HTTP call with inbound request's HTTP method. Only inbound requests of
        GET and HEAD HTTP method types are cacheable.

        P{{path}} Request path
        P{{request}} The HTTP request to be forwarded
        R{{}} The inbound response message
        R{{}} The error occurred while attempting to fulfill the HTTP request (if any)
    }
    public function forward(string path, Request request) returns Response|HttpConnectorError;

    documentation {
        Submits an HTTP request to a service with the specified HTTP verb.

        P{{httpVerb}} The HTTP verb value
        P{{path}} The resource path
        P{{request}} An HTTP request
        R{{}} The Future for further interactions
        R{{}} The error occurred while attempting to fulfill the HTTP request (if any)
    }
    public function submit(string httpVerb, string path, Request request) returns (HttpFuture|HttpConnectorError);

    documentation {
        Retrieves the response for a previously submitted request.

        P{{httpFuture}} The Future which relates to previous async invocation
        R{{}} The HTTP response message
        R{{}} The error occurred while attempting to fulfill the HTTP request (if any)
    }
    public function getResponse(HttpFuture httpFuture) returns Response|HttpConnectorError;

    documentation {
        Checks whether server push exists for a previously submitted request.

        P{{httpFuture}} The Future which relates to previous async invocation
        R{{}} Returns true if the push promise exists
    }
    public function hasPromise(HttpFuture httpFuture) returns boolean;

    documentation {
        Retrieves the next available push promise for a previously submitted request.

        P{{httpFuture}} The Future which relates to previous async invocation
        R{{}} The HTTP Push Promise message
        R{{}} The error occurred while attempting to fulfill the HTTP request (if any)
    }
    public function getNextPromise(HttpFuture httpFuture) returns (PushPromise|HttpConnectorError);

    documentation {
        Retrieves the promised server push response.

        P{{promise}} The related Push Promise message
        R{{}} HTTP The Push Response message
        R{{}} The error occurred while attempting to fulfill the HTTP request (if any)
    }
    public function getPromisedResponse(PushPromise promise) returns Response|HttpConnectorError;

    documentation {
        Rejects a push promise.

        P{{promise}} The Push Promise to be rejected
    }
    public function rejectPromise(PushPromise promise);
};

documentation {
    Creates an HTTP client capable of caching HTTP responses.
}
public function createHttpCachingClient(string url, ClientEndpointConfig config, CacheConfig cacheConfig)
                                                                                                returns CallerActions {
    HttpCachingClient httpCachingClient = new(url, config, cacheConfig);
    log:printDebug("Created HTTP caching client: " + io:sprintf("%r", httpCachingClient));
    return httpCachingClient;
}

public function HttpCachingClient::post(string path, Request? request = ()) returns Response|HttpConnectorError {
    Request req = request ?: new;
    setRequestCacheControlHeader(req);

    match self.httpClient.post(path, request = req) {
        Response inboundResponse => {
            invalidateResponses(self.cache, inboundResponse, path);
            return inboundResponse;
        }

        HttpConnectorError err => return err;
    }
}

public function HttpCachingClient::head(string path, Request? request = ()) returns Response|HttpConnectorError {
    Request req = request ?: new;
    setRequestCacheControlHeader(req);
    return getCachedResponse(self.cache, self.httpClient, req, HEAD, path, self.cacheConfig.isShared);
}

public function HttpCachingClient::put(string path, Request? request = ()) returns Response|HttpConnectorError {
    Request req = request ?: new;
    setRequestCacheControlHeader(req);

    match self.httpClient.put(path, request = req) {
        Response inboundResponse => {
            invalidateResponses(self.cache, inboundResponse, path);
            return inboundResponse;
        }

        HttpConnectorError err => return err;
    }
}

public function HttpCachingClient::execute(string httpMethod, string path, Request request)
                                                                                returns Response|HttpConnectorError {
    setRequestCacheControlHeader(request);

    if (httpMethod == GET || httpMethod == HEAD) {
        return getCachedResponse(self.cache, self.httpClient, request, httpMethod, path, self.cacheConfig.isShared);
    }

    match self.httpClient.execute(httpMethod, path, request) {
        Response inboundResponse => {
            invalidateResponses(self.cache, inboundResponse, path);
            return inboundResponse;
        }

        HttpConnectorError err => return err;
    }
}

public function HttpCachingClient::patch(string path, Request? request = ()) returns Response|HttpConnectorError {
    Request req = request ?: new;
    setRequestCacheControlHeader(req);

    match self.httpClient.patch(path, request = req) {
        Response inboundResponse => {
            invalidateResponses(self.cache, inboundResponse, path);
            return inboundResponse;
        }

        HttpConnectorError err => return err;
    }
}

public function HttpCachingClient::delete(string path, Request? request = ()) returns Response|HttpConnectorError {
    Request req = request ?: new;
    setRequestCacheControlHeader(req);

    match self.httpClient.delete(path, request = req) {
        Response inboundResponse => {
            invalidateResponses(self.cache, inboundResponse, path);
            return inboundResponse;
        }

        HttpConnectorError err => return err;
    }
}

public function HttpCachingClient::get(string path, Request? request = ()) returns Response|HttpConnectorError {
    Request req = request ?: new;
    setRequestCacheControlHeader(req);
    return getCachedResponse(self.cache, self.httpClient, req, GET, path, self.cacheConfig.isShared);
}

public function HttpCachingClient::options(string path, Request? request = ()) returns Response|HttpConnectorError {
    Request req = request ?: new;
    setRequestCacheControlHeader(req);

    match self.httpClient.options(path, request = req) {
        Response inboundResponse => {
            invalidateResponses(self.cache, inboundResponse, path);
            return inboundResponse;
        }

        HttpConnectorError err => return err;
    }
}

public function HttpCachingClient::forward(string path, Request request) returns Response|HttpConnectorError {
    if (request.method == GET || request.method == HEAD) {
        return getCachedResponse(self.cache, self.httpClient, request, request.method, path, self.cacheConfig.isShared);
    }

    match self.httpClient.forward(path, request) {
        Response inboundResponse => {
            invalidateResponses(self.cache, inboundResponse, path);
            return inboundResponse;
        }

        HttpConnectorError err => return err;
    }
}

public function HttpCachingClient::submit(string httpVerb, string path, Request request)
                                                                            returns HttpFuture|HttpConnectorError {
    return self.httpClient.submit(httpVerb, path, request);
}

public function HttpCachingClient::getResponse(HttpFuture httpFuture) returns Response|HttpConnectorError {
    return self.httpClient.getResponse(httpFuture);
}

public function HttpCachingClient::hasPromise(HttpFuture httpFuture) returns boolean {
    return self.httpClient.hasPromise(httpFuture);
}

public function HttpCachingClient::getNextPromise(HttpFuture httpFuture) returns (PushPromise|HttpConnectorError) {
    return self.httpClient.getNextPromise(httpFuture);
}

public function HttpCachingClient::getPromisedResponse(PushPromise promise) returns Response|HttpConnectorError {
    return self.httpClient.getPromisedResponse(promise);
}

public function HttpCachingClient::rejectPromise(PushPromise promise) {
    self.httpClient.rejectPromise(promise);
}

function getCachedResponse(HttpCache cache, CallerActions httpClient, Request req, string httpMethod, string path,
                           boolean isShared) returns Response|HttpConnectorError {
    time:Time currentT = time:currentTime();
    req.parseCacheControlHeader();

    if (cache.hasKey(getCacheKey(httpMethod, path))) {
        Response cachedResponse = cache.get(getCacheKey(httpMethod, path));
        // Based on https://tools.ietf.org/html/rfc7234#section-4
        log:printDebug("Cached response found for: '" + httpMethod + " " + path + "'");

        if (isFreshResponse(cachedResponse, isShared)) {
            // If the no-cache directive is not set, responses can be served straight from the cache, without
            // validating with the origin server.
            if (!(req.cacheControl.noCache ?: true) && !(cachedResponse.cacheControl.noCache ?: true)
                                                                                        && !req.hasHeader(PRAGMA)) {
                setAgeHeader(cachedResponse);
                log:printDebug("Serving a cached fresh response without validating with the origin server: " + io:
                        sprintf("%r", cachedResponse));
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
            if (!(req.cacheControl.noCache ?: true) && ! (cachedResponse.cacheControl.noCache ?: true)
                                                                                            && !req.hasHeader(PRAGMA)) {
                log:printDebug("Serving cached stale response without validating with the origin server");
                setAgeHeader(cachedResponse);
                cachedResponse.setHeader(WARNING, WARNING_110_RESPONSE_IS_STALE);
                return cachedResponse;
            }
        }

        log:printDebug("Validating a stale response for '" + path + "' with the origin server.");
        return getValidationResponse(httpClient, req, cachedResponse, cache, currentT, path, httpMethod, false);
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

        HttpConnectorError err => return err;
    }
}

function getValidationResponse(CallerActions httpClient, Request req, Response cachedResponse, HttpCache cache,
                               time:Time currentT, string path, string httpMethod, boolean isFreshResponse)
                                                                                returns Response|HttpConnectorError {
    // If the no-cache directive is set, always validate the response before serving
    Response validationResponse = new; // TODO: May have to make this Response?

    if (isFreshResponse) {
        log:printDebug("Sending validation request for a fresh response");
    } else {
        log:printDebug("Sending validation request for a stale response");
    }

    match sendValidationRequest(httpClient, path, cachedResponse) {
        Response resp => validationResponse = resp;
        HttpConnectorError validationErr => {
            // Based on https://tools.ietf.org/html/rfc7234#section-4.2.4
            // This behaviour is based on the fact that currently HttpConnectorError structs are returned only
            // if the connection is refused or the connection times out.
            // TODO: Verify that this behaviour is valid: returning a fresh response when 'no-cache' is present and origin server couldn't be reached.
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
                           string httpMethod) returns Response|HttpConnectorError {
    log:printDebug("304 response received");

    if (validationResponse.hasHeader(ETAG)) {
        string etag = validationResponse.getHeader(ETAG);

        if (isAStrongValidator(etag)) {
            // Assuming ETags are the only strong validators
            Response[] matchingCachedResponses = cache.getAllByETag(getCacheKey(httpMethod, path), etag);

            foreach resp in matchingCachedResponses {
                updateResponse(resp, validationResponse);
            }
            log:printDebug("304 response received. Strong validator. Response(s) updated");
            return cachedResponse;
        } else if (hasAWeakValidator(validationResponse, etag)) {
            // The weak validator should be either an ETag or a last modified date. Precedence given to ETag
            Response[] matchingCachedResponses = cache.getAllByWeakETag(getCacheKey(httpMethod, path), etag);

            foreach resp in matchingCachedResponses {
                updateResponse(resp, validationResponse);
            }
            log:printDebug("304 response received. Weak validator. Response(s) updated");
            return cachedResponse;

            // TODO: check if last modified date can be used here as a weak validator
        }
    }

    if (!cachedResponse.hasHeader(ETAG) && !cachedResponse.hasHeader(LAST_MODIFIED)) {
        updateResponse(cachedResponse, validationResponse);
    }
    log:printDebug("304 response received. No validators. Returning cached response");
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
    return freshnessLifetime >= currentAge;
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
function sendValidationRequest(CallerActions httpClient, string path, Response cachedResponse)
                                                                                returns Response|HttpConnectorError {
    Request validationRequest = new;

    if (cachedResponse.hasHeader(ETAG)) {
        validationRequest.setHeader(IF_NONE_MATCH, cachedResponse.getHeader(ETAG));
    }

    if (cachedResponse.hasHeader(LAST_MODIFIED)) {
        validationRequest.setHeader(IF_MODIFIED_SINCE, cachedResponse.getHeader(LAST_MODIFIED));
    }

    // TODO: handle cases where neither of the above 2 headers are present

    match httpClient.get(path, request = validationRequest) {
        Response validationResponse => return validationResponse;

        HttpConnectorError err => return err;
    }
}

function sendNewRequest(CallerActions httpClient, Request request, string path, string httpMethod)
                                                                                returns Response|HttpConnectorError {
    if (httpMethod == GET) {
        return httpClient.get(path, request = request);
    } else if (httpMethod == HEAD) {
        return httpClient.head(path, request = request);
    } else {
        HttpConnectorError err = {message:"HTTP method not supported in caching client: " + httpMethod};
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
    string[] headerNames = validationResponse.getHeaderNames();
    foreach headerName in headerNames {
        cachedResponse.removeHeader(headerName);
        if (validationResponse.hasHeader(headerName)) {
            string[] headerValues = validationResponse.getHeaders(headerName);
            foreach value in headerValues {
                cachedResponse.addHeader(headerName, value);
            }
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
                next;
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
