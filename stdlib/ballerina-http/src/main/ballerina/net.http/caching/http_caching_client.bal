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

package ballerina.net.http;

import ballerina/log;
import ballerina/runtime;
import ballerina/time;
import ballerina/io;

// HTTP CACHING CLIENT IMPLEMENTATION.
// Adding this to http_client.bal temporarily, until issue #4865 gets fixed
const string WARNING_AGENT = getWarningAgent();

const string WARNING_110_RESPONSE_IS_STALE = "110 " + WARNING_AGENT + " \"Response is Stale\"";
const string WARNING_111_REVALIDATION_FAILED = "111 " + WARNING_AGENT + " \"Revalidation Failed\"";

const string WEAK_VALIDATOR_TAG = "W/";
const int STALE = 0;

@Description {value:"Used for configuring the caching behaviour. Setting the cacheLevel field in the CacheConfig struct allows the user to control the caching behaviour."}
@Field {value:"CACHE_CONTROL_AND_VALIDATORS: Restricts caching to instances where the Cache-Control header and either the ETag or Last-Modified header is present."}
@Field {value:"SPECIFICATION: Caching behaviour is as specified by the RFC7234 specification."}
public enum CachingLevel {
    CACHE_CONTROL_AND_VALIDATORS,
    SPECIFICATION
}

@Description {value:"CacheConfig struct is used for providing the caching configurations necessary for the HTTP caching client."}
@Field {value:"isShared: Specifies whether the HTTP caching client should behave as a public cache or a private cache"}
@Field {value:"expiryTimeMillis: The number of milliseconds to keep an entry in the cache"}
@Field {value:"capacity: The capacity of the cache"}
@Field {value:"evictionFactor: The fraction of entries to be removed when the cache is full. The value should be between 0 (exclusive) and 1 (inclusive)."}
@Field {value:"cachingLevel: Gives the user some control over the caching behaviour. By default, this is set to CACHE_CONTROL_AND_VALIDATORS."}
public struct CacheConfig {
    boolean isShared;
    int expiryTimeMillis;
    int capacity; // 8MB
    float evictionFactor;
    CachingLevel cachingLevel;
}

@Description {value:"Initializes the CacheConfig struct to its default values"}
@Param {value:"cacheConfig: The CacheConfig struct to be initialized"}
public function <CacheConfig cacheConfig> CacheConfig () {
    cacheConfig.isShared = false;
    cacheConfig.expiryTimeMillis = 86400;
    cacheConfig.capacity = 8388608; // 8MB
    cacheConfig.evictionFactor = 0.2;
    cacheConfig.cachingLevel = CachingLevel.CACHE_CONTROL_AND_VALIDATORS;
}

@Description {value:"An HTTP caching client implementation which takes an HttpClient and wraps it with a caching layer."}
@Field {value:"httpClient: The underlying HTTP client which will be making the actual network calls"}
@Field {value:"cacheConfig: Caching configurations for the HTTP cache"}
public struct HttpCachingClient {
    string serviceUri;
    ClientEndpointConfiguration config;
    HttpClient httpClient;
    HttpCache cache;
    CacheConfig cacheConfig;
}

public function createHttpCachingClient(string url, ClientEndpointConfiguration config, CacheConfig cacheConfig) returns HttpClient {
    HttpCachingClient httpCachingClient = {
                                       serviceUri: url,
                                       httpClient: createHttpClient(url, config),
                                       cache: createHttpCache("http-cache", cacheConfig),
                                       cacheConfig: cacheConfig
                                   };
    log:printTrace("Created HTTP caching client: " + io:sprintf("%r",[httpCachingClient]));
    return httpCachingClient;
}
//HttpCache cache = createHttpCache("http-cache", cacheConfig);

@Description {value:"Responses returned for POST requests are not cacheable. Therefore, the requests are simply directed to the origin server."}
@Param {value:"path: Resource path "}
@Param {value:"req: An HTTP outbound request message"}
@Return {value:"The inbound response message"}
@Return {value:"Error occured during HTTP client invocation"}
public function <HttpCachingClient client> post (string path, Request req) returns (Response|HttpConnectorError) {
    match client.post(path, req) {
        Response inboundResponse => {
            invalidateResponses(client.cache, inboundResponse, path);
            return inboundResponse;
        }

        HttpConnectorError err => return err;
    }
}

@Description {value:"Responses for HEAD requests are cacheable and as such, will be routed through the HTTP cache. Only if a suitable response cannot be found will the request be directed to the origin server."}
@Param {value:"path: Resource path "}
@Param {value:"req: An HTTP outbound request message"}
@Return {value:"The inbound response message"}
@Return {value:"Error occured during HTTP client invocation"}
public function <HttpCachingClient client> head (string path, Request req) returns (Response|HttpConnectorError) {
    return getCachedResponse(client.cache, client.httpClient, req, HEAD, path, client.cacheConfig.isShared);
}

@Description {value:"Responses returned for PUT requests are not cacheable. Therefore, the requests are simply directed to the origin server. In addition, PUT requests invalidate the currently stored responses for the given path."}
@Param {value:"path: Resource path "}
@Param {value:"req: An HTTP outbound request message"}
@Return {value:"The inbound response message"}
@Return {value:"Error occured during HTTP client invocation"}
public function <HttpCachingClient client> put (string path, Request req) returns (Response|HttpConnectorError) {
    match client.put(path, req) {
        Response inboundResponse => {
            invalidateResponses(client.cache, inboundResponse, path);
            return inboundResponse;
        }

        HttpConnectorError err => return err;
    }
}

@Description {value:"Invokes an HTTP call with the specified HTTP verb. This is not a cacheable operation."}
@Param {value:"httpMethod: HTTP method to be used for the request"}
@Param {value:"path: Resource path "}
@Param {value:"req: An HTTP outbound request message"}
@Return {value:"The inbound response message"}
@Return {value:"Error occured during HTTP client invocation"}
public function <HttpCachingClient client> execute (string httpMethod, string path, Request req) returns (Response|HttpConnectorError) {
    if (httpMethod == GET || httpMethod == HEAD) {
        return getCachedResponse(client.cache, client.httpClient, req, httpMethod, path, client.cacheConfig.isShared);
    }

    match client.execute(httpMethod, path, req) {
        Response inboundResponse => {
            invalidateResponses(client.cache, inboundResponse, path);
            return inboundResponse;
        }

        HttpConnectorError err => return err;
    }
}

@Description {value:"Responses returned for PATCH requests are not cacheable. Therefore, the requests are simply directed to the origin server."}
@Param {value:"path: Resource path "}
@Param {value:"req: An HTTP outbound request message"}
@Return {value:"The inbound response message"}
@Return {value:"Error occured during HTTP client invocation"}
public function <HttpCachingClient client> patch (string path, Request req) returns (Response|HttpConnectorError) {
    match client.patch(path, req) {
        Response inboundResponse => {
            invalidateResponses(client.cache, inboundResponse, path);
            return inboundResponse;
        }

        HttpConnectorError err => return err;
    }
}

@Description {value:"Responses returned for DELETE requests are not cacheable. Therefore, the requests are simply directed to the origin server."}
@Param {value:"path: Resource path "}
@Param {value:"req: An HTTP outbound request message"}
@Return {value:"The inbound response message"}
@Return {value:"Error occured during HTTP client invocation"}
public function <HttpCachingClient client> delete (string path, Request req) returns (Response|HttpConnectorError) {
    match client.delete(path, req) {
        Response inboundResponse => {
            invalidateResponses(client.cache, inboundResponse, path);
            return inboundResponse;
        }

        HttpConnectorError err => return err;
    }
}

@Description {value:"Responses for GET requests are cacheable and as such, will be routed through the HTTP cache. Only if a suitable response cannot be found will the request be directed to the origin server."}
@Param {value:"path: Request path"}
@Param {value:"req: An HTTP outbound request message"}
@Return {value:"The inbound response message"}
@Return {value:"Error occured during HTTP client invocation"}
public function <HttpCachingClient client> get (string path, Request req) returns (Response|HttpConnectorError) {
    return getCachedResponse(client.cache, client.httpClient, req, GET, path, client.cacheConfig.isShared);
}

@Description {value:"Responses returned for OPTIONS requests are not cacheable. Therefore, the requests are simply directed to the origin server."}
@Param {value:"path: Request path"}
@Param {value:"req: An HTTP outbound request message"}
@Return {value:"The inbound response message"}
@Return {value:"Error occured during HTTP client invocation"}
public function <HttpCachingClient client> options (string path, Request req) returns (Response|HttpConnectorError) {
    match client.options(path, req) {
        Response inboundResponse => {
            invalidateResponses(client.cache, inboundResponse, path);
            return inboundResponse;
        }

        HttpConnectorError err => return err;
    }
}

@Description {value:"Forward action can be used to invoke an HTTP call with inbound request's HTTP method. Only inbound requests of GET and HEAD HTTP method types are cacheable."}
@Param {value:"path: Request path"}
@Param {value:"req: An HTTP inbound request message"}
@Return {value:"The inbound response message"}
@Return {value:"Error occured during HTTP client invocation"}
public function <HttpCachingClient client> forward (string path, Request req) returns (Response|HttpConnectorError) {
    // TODO: handle response caching for forwarded GET and HEAD requests.
    return client.forward(path, req);
}

@Description {value:"Submits an HTTP request to a service with the specified HTTP verb."}
@Param {value:"httpVerb: The HTTP verb value"}
@Param {value:"path: The Resource path "}
@Param {value:"req: An HTTP outbound request message"}
@Return {value:"The Handle for further interactions"}
@Return {value:"The Error occured during HTTP client invocation"}
public function <HttpCachingClient client> submit (string httpVerb, string path, Request req) returns (HttpHandle|HttpConnectorError) {
    HttpConnectorError httpConnectorError = {};
    httpConnectorError.message = "Unsupported action";
    return httpConnectorError;
}

@Description {value:"Retrieves response for a previously submitted request."}
@Param {value:"handle: The Handle which relates to previous async invocation"}
@Return {value:"The HTTP response message"}
@Return {value:"The Error occured during HTTP client invocation"}
public function <HttpCachingClient client> getResponse (HttpHandle handle) returns (Response|HttpConnectorError) {
    HttpConnectorError httpConnectorError = {};
    httpConnectorError.message = "Unsupported action";
    return httpConnectorError;
}

@Description {value:"Checks whether server push exists for a previously submitted request."}
@Param {value:"handle: The Handle which relates to previous async invocation"}
@Return {value:"Whether push promise exists"}
public function <HttpCachingClient client> hasPromise (HttpHandle handle) returns boolean {
    return false;
}

@Description {value:"Retrieves the next available push promise for a previously submitted request."}
@Param {value:"handle: The Handle which relates to previous async invocation"}
@Return {value:"The HTTP Push Promise message"}
@Return {value:"The Error occured during HTTP client invocation"}
public function <HttpCachingClient client> getNextPromise (HttpHandle handle) returns (PushPromise|HttpConnectorError) {
    HttpConnectorError httpConnectorError = {};
    httpConnectorError.message = "Unsupported action";
    return httpConnectorError;
}

@Description {value:"Retrieves the promised server push response."}
@Param {value:"promise: The related Push Promise message"}
@Return {value:"HTTP The Push Response message"}
@Return {value:"The Error occured during HTTP client invocation"}
public function <HttpCachingClient client> getPromisedResponse (PushPromise promise) returns (Response|HttpConnectorError) {
    HttpConnectorError httpConnectorError = {};
    httpConnectorError.message = "Unsupported action";
    return httpConnectorError;
}

@Description {value:"Rejects a push promise."}
@Param {value:"promise: The Push Promise need to be rejected"}
@Return {value:"Whether operation is successful"}
public function <HttpCachingClient client> rejectPromise (PushPromise promise) returns boolean {
    return false;
}

function getCachedResponse (HttpCache cache, HttpClient httpClient, Request req, string httpMethod, string path,
                            boolean isShared) returns (Response|HttpConnectorError) {
    time:Time currentT = time:currentTime();
    //Response cachedResponse = {};
    req.parseCacheControlHeader();

    match cache.get(getCacheKey(httpMethod, path)) {
        Response cachedResponse => {
            // Based on https://tools.ietf.org/html/rfc7234#section-4
            log:printTrace("Cached response found for: '" + httpMethod + " " + path + "'");

            if (isFreshResponse(cachedResponse, isShared)) {
                // If the no-cache directive is not set, responses can be served straight from the cache, without
                // validating with the origin server.
                if (!req.cacheControl.noCache && !cachedResponse.cacheControl.noCache && !req.hasHeader(PRAGMA)) {
                    setAgeHeader(cachedResponse);
                    log:printTrace("Serving a cached fresh response without validating with the origin server: " + io:sprintf("%r", [cachedResponse]));
                    return cachedResponse;
                } else {
                    log:printTrace("Serving a cached fresh response after validating with the origin server");
                    return getValidationResponse(httpClient, req, cachedResponse, cache, currentT, path, httpMethod, true);
                }
            }

            // If a fresh response is not available, serve a stale response, provided that it is not prohibited by
            // a directive and is explicitly allowed in the request.
            if (isAllowedToBeServedStale(req.cacheControl, cachedResponse, isShared)) {

                // If the no-cache directive is not set, responses can be served straight from the cache, without
                // validating with the origin server.
                if (!req.cacheControl.noCache && !cachedResponse.cacheControl.noCache
                                                  && !req.hasHeader(PRAGMA)) {
                    log:printTrace("Serving cached stale response without validating with the origin server");
                    setAgeHeader(cachedResponse);
                    cachedResponse.setHeader(WARNING, WARNING_110_RESPONSE_IS_STALE);
                    return cachedResponse;
                }
            }

            log:printTrace("Validating a stale response for '" + path + "' with the origin server.");
            return getValidationResponse(httpClient, req, cachedResponse, cache, currentT, path, httpMethod, false);
        }

        null => {log:printTrace("Cached response not found for: '" + httpMethod + " " + path + "'");}
    }

    log:printTrace("Sending new request to: " + path);
    match httpClient.get(path, req) {
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

function getValidationResponse (HttpClient httpClient, Request req, Response cachedResponse, HttpCache cache,
                                time:Time currentT, string path, string httpMethod, boolean isFreshResponse)
returns (Response|HttpConnectorError) {
    // If the no-cache directive is set, always validate the response before serving
    Response validationResponse = {};

    if (isFreshResponse) {
        log:printTrace("Sending validation request for a fresh response");
    } else {
        log:printTrace("Sending validation request for a stale response");
    }

    //validationResponse, validationErr =
    match sendValidationRequest(httpClient, path, cachedResponse) {
        Response resp => validationResponse = resp;
        HttpConnectorError validationErr => {
            // Based on https://tools.ietf.org/html/rfc7234#section-4.2.4
            // This behaviour is based on the fact that currently HttpConnectorError structs are returned only
            // if the connection is refused or the connection times out.
            // TODO: Verify that this behaviour is valid: returning a fresh response when 'no-cache' is present
            // and origin server couldn't be reached.
            setAgeHeader(cachedResponse);
            if (!isFreshResponse) {
                // If the origin server cannot be reached and a fresh response is unavailable, serve a stale
                // response (unless it is prohibited through a directive).
                cachedResponse.setHeader(WARNING, WARNING_111_REVALIDATION_FAILED);
                log:printTrace("Cannot reach origin server. Serving a stale response");
            } else {
                log:printTrace("Cannot reach origin server. Serving a fresh response");
            }
            return validationErr;
        }
    }

    log:printTrace("Response for validation request received");
    // Based on https://tools.ietf.org/html/rfc7234#section-4.3.3
    if (validationResponse.statusCode == RESPONSE_304_NOT_MODIFIED) {
        return handle304Response(validationResponse, cachedResponse, cache, path, httpMethod);
    } else if (validationResponse.statusCode >= 500 && validationResponse.statusCode < 600) {
        // May forward the response or act as if the origin server failed to respond and serve a
        // stored response
        // TODO: Make the above mentioned behaviour user-configurable
        return validationResponse;
    } else {
        // Forward the received response and replace the stored responses
        validationResponse.requestTime = currentT.time;
        cache.put(getCacheKey(httpMethod, path), req.cacheControl, validationResponse);
        log:printTrace("Received a full response. Storing it in cache and forwarding to the client");
        return validationResponse;
    }
}

// Based on https://tools.ietf.org/html/rfc7234#section-4.3.4
function handle304Response (Response validationResponse, Response cachedResponse, HttpCache cache, string path,
                            string httpMethod) returns (Response|HttpConnectorError) {
    log:printTrace("304 response received");

    if (validationResponse.hasHeader(ETAG)) {
        string etag = validationResponse.getHeader(ETAG);

        if (isAStrongValidator(etag)) {
            // Assuming ETags are the only strong validators
            Response[] matchingCachedResponses = cache.getAllByETag(getCacheKey(httpMethod, path), etag);

            foreach resp in matchingCachedResponses {
                updateResponse(resp, validationResponse);
            }
            log:printTrace("304 response received. Strong validator. Response(s) updated");
            return cachedResponse;
        } else if (hasAWeakValidator(validationResponse, etag)) {
            // The weak validator should be either an ETag or a last modified date. Precedence given to ETag
            Response[] matchingCachedResponses = cache.getAllByWeakETag(getCacheKey(httpMethod, path), etag);

            foreach resp in matchingCachedResponses {
                updateResponse(resp, validationResponse);
            }
            log:printTrace("304 response received. Weak validator. Response(s) updated");
            return cachedResponse;

            // TODO: check if last modified date can be used here as a weak validator
        }
    }

    if (!cachedResponse.hasHeader(ETAG) && !cachedResponse.hasHeader(LAST_MODIFIED)) {
        updateResponse(cachedResponse, validationResponse);
    }
    log:printTrace("304 response received. No validators. Returning cached response");
    // TODO: Check if this behaviour is the expected one
    return cachedResponse;
}

// Based on https://tools.ietf.org/html/rfc7234#section-4.4
function invalidateResponses (HttpCache httpCache, Response inboundResponse, string path) {
    // TODO: Improve this logic in accordance with the spec
    if (isCacheableStatusCode(inboundResponse.statusCode) &&
        inboundResponse.statusCode >= 200 && inboundResponse.statusCode < 400) {
        httpCache.cache.remove(getCacheKey(GET, path));
        httpCache.cache.remove(getCacheKey(HEAD, path));
    }
}

// Based on https://tools.ietf.org/html/rfc7234#section-4.2.1
function getFreshnessLifetime (Response cachedResponse, boolean isSharedCache) returns int {
    // TODO: Ensure that duplicate directives are not counted towards freshness lifetime.
    if (isSharedCache && cachedResponse.cacheControl.sMaxAge >= 0) {
        return cachedResponse.cacheControl.sMaxAge;
    }

    if (cachedResponse.cacheControl.maxAge >= 0) {
        return cachedResponse.cacheControl.maxAge;
    }

    // At this point, there should be exactly one Expires header to calculate the freshness lifetime.
    // When adding heuristic calculations, the condition would change to >1.
    if (cachedResponse.hasHeader(EXPIRES)) {
        string[] expiresHeader = cachedResponse.getHeaders(EXPIRES);

        if (lengthof expiresHeader == 1 && expiresHeader[0] != null) {
            if (cachedResponse.hasHeader(DATE)) {
                string[] dateHeader = cachedResponse.getHeaders(DATE);

                if (lengthof dateHeader == 1 && dateHeader[0] != null) {
                    int freshnessLifetime = (time:parse(expiresHeader[0], RFC_1123_DATE_TIME_FORMAT).time
                                             - time:parse(dateHeader[0], RFC_1123_DATE_TIME_FORMAT).time) / 1000;
                    return freshnessLifetime;
                }
            }
        }
    }

    // TODO: Add heuristic freshness lifetime calculation

    return STALE;
}

function isFreshResponse (Response cachedResponse, boolean isSharedCache) returns boolean {
    int currentAge = getResponseAge(cachedResponse);
    int freshnessLifetime = getFreshnessLifetime(cachedResponse, isSharedCache);
    return freshnessLifetime >= currentAge;
}

// Based on https://tools.ietf.org/html/rfc7234#section-4.2.4
function isAllowedToBeServedStale (RequestCacheControl requestCacheControl, Response cachedResponse,
                                   boolean isSharedCache) returns boolean {
    // A cache MUST NOT generate a stale response if it is prohibited by an explicit in-protocol directive
    if (isServingStaleProhibited(requestCacheControl, cachedResponse.cacheControl)) {
        return false;
    }
    return isStaleResponseAccepted(requestCacheControl, cachedResponse, isSharedCache);
}

// Based on https://tools.ietf.org/html/rfc7234#section-4.2.4
function isServingStaleProhibited (RequestCacheControl requestCacheControl,
                                   ResponseCacheControl responseCacheControl) returns boolean {
    // A cache MUST NOT generate a stale response if it is prohibited by an explicit in-protocol directive
    return requestCacheControl.noStore ||
           requestCacheControl.noCache ||
           responseCacheControl.mustRevalidate ||
           responseCacheControl.proxyRevalidate ||
           (responseCacheControl.sMaxAge >= 0);
}

// Based on https://tools.ietf.org/html/rfc7234#section-4.2.4
function isStaleResponseAccepted (RequestCacheControl requestCacheControl, Response cachedResponse, boolean isSharedCache) returns boolean {
    if (requestCacheControl.maxStale == MAX_STALE_ANY_AGE) {
        return true;
    } else if (requestCacheControl.maxStale >=
               (getResponseAge(cachedResponse) - getFreshnessLifetime(cachedResponse, isSharedCache))) {
        return true;
    }
    return false;
}

// Based https://tools.ietf.org/html/rfc7234#section-4.3.1
function sendValidationRequest (HttpClient httpClient, string path, Response cachedResponse) returns (Response|HttpConnectorError) {
    Request validationRequest = {};
    string etagHeader = cachedResponse.getHeader(ETAG);
    string lastModifiedHeader = cachedResponse.getHeader(LAST_MODIFIED);

    if (etagHeader != null) {
        validationRequest.setHeader(IF_NONE_MATCH, etagHeader);
    }

    if (lastModifiedHeader != null) {
        validationRequest.setHeader(IF_MODIFIED_SINCE, lastModifiedHeader);
    }

    // TODO: handle cases where neither of the above 2 headers are present

    match httpClient.get(path, validationRequest) {
        Response validationResponse => return validationResponse;

        HttpConnectorError err => return err;
    }
}

function setAgeHeader (Response cachedResponse) {
    cachedResponse.setHeader(AGE, "" + calculateCurrentResponseAge(cachedResponse));
}

// Based on https://tools.ietf.org/html/rfc7234#section-4.2.3
function calculateCurrentResponseAge (Response cachedResponse) returns int {
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
function updateResponse (Response cachedResponse, Response validationResponse) {
    // 1 - delete warning headers with warn codes 1xx
    // 2 - retain warning headers with warn codes 2xx
    // 3 - use other headers in validation response to replace corresponding headers in cached response
    retain2xxWarnings(cachedResponse);
    replaceHeaders(cachedResponse, validationResponse);
}

// Based on https://tools.ietf.org/html/rfc7234#section-4.3.4
function hasAWeakValidator (Response validationResponse, string etag) returns boolean {
    return (validationResponse.hasHeader(LAST_MODIFIED) || !isAStrongValidator(etag));
}

// Based on https://tools.ietf.org/html/rfc7234#section-4.3.4
function isAStrongValidator (string etag) returns boolean {
    // TODO: Consider cases where Last-Modified can also be treated as a strong validator as per
    // https://tools.ietf.org/html/rfc7232#section-2.2.2
    if (!etag.hasPrefix(WEAK_VALIDATOR_TAG)) {
        return true;
    }

    return false;
}

// Based on https://tools.ietf.org/html/rfc7234#section-4.3.4
function replaceHeaders (Response cachedResponse, Response validationResponse) {
    map uptodateHeaders = validationResponse.getCopyOfAllHeaders();

    foreach headerName, headerValues in uptodateHeaders {
        string[] valueArray = [];
        match <string[]>headerValues {
            string[] arr => valueArray = arr;
            error => next; // Skip the current header if there was an error in retrieving the header values
        }

        cachedResponse.removeHeader(headerName); // Remove existing headers before adding the up-to-date headers
        foreach value in valueArray {
            cachedResponse.addHeader(headerName, value);
        }
    }
}

function retain2xxWarnings (Response cachedResponse) {
    if (cachedResponse.hasHeader(WARNING)) {
        string[] warningHeaders = cachedResponse.getHeaders(WARNING);
        cachedResponse.removeHeader(WARNING);
        // TODO: Need to handle this in a better way using regex when the required regex APIs are there
        foreach warningHeader in warningHeaders {
            if (warningHeader.contains("214") || warningHeader.contains("299")) {
                log:printTrace("Adding warning header: " + warningHeader);
                cachedResponse.addHeader(WARNING, warningHeader);
                next;
            }
        }
    }
}

function getResponseAge (Response cachedResponse) returns int {
    if (!cachedResponse.hasHeader(AGE)) {
        return 0;
    }

    match <int>cachedResponse.getHeader(AGE) {
        int ageValue => return ageValue;
        error => return 0;
    }
}

function getDateValue (Response inboundResponse) returns int {
    // Based on https://tools.ietf.org/html/rfc7231#section-7.1.1.2
    if (!inboundResponse.hasHeader(DATE)) {
        time:Time currentT = time:currentTime();
        inboundResponse.setHeader(DATE, currentT.format(RFC_1123_DATE_TIME_FORMAT));
        return currentT.time;
    }

    string dateHeader = inboundResponse.getHeader(DATE);
    // TODO: May need to handle invalid date headers
    time:Time dateHeaderTime = time:parse(dateHeader, RFC_1123_DATE_TIME_FORMAT);
    return dateHeaderTime.time;
}

function getWarningAgent () returns string {
    string ballerinaVersion = runtime:getProperty("ballerina.version");
    return "ballerina-http-caching-client/" + ballerinaVersion;
}
