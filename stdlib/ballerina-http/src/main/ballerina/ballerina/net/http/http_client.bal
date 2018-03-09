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

import ballerina.log;
import ballerina.runtime;
import ballerina.time;

@Description {value:"HTTP client connector for outbound HTTP requests"}
@Param {value:"serviceUri: URI of the service"}
@Param {value:"connectorOptions: Connector options"}
public connector HttpClient (string serviceUri, Options connectorOptions) {

    @Description {value:"The POST action implementation of the HTTP Connector."}
    @Param {value:"path: Resource path "}
    @Param {value:"req: An HTTP outbound request message"}
    @Return {value:"The inbound response message"}
    @Return {value:"Error occured during HTTP client invocation"}
    native action post (string path, OutRequest req) (InResponse, HttpConnectorError);

    @Description {value:"The HEAD action implementation of the HTTP Connector."}
    @Param {value:"path: Resource path "}
    @Param {value:"req: An HTTP outbound request message"}
    @Return {value:"The inbound response message"}
    @Return {value:"Error occured during HTTP client invocation"}
    native action head (string path, OutRequest req) (InResponse, HttpConnectorError);

    @Description {value:"The PUT action implementation of the HTTP Connector."}
    @Param {value:"path: Resource path "}
    @Param {value:"req: An HTTP outbound request message"}
    @Return {value:"The inbound response message"}
    @Return {value:"Error occured during HTTP client invocation"}
    native action put (string path, OutRequest req) (InResponse, HttpConnectorError);

    @Description {value:"Invokes an HTTP call with the specified HTTP verb."}
    @Param {value:"httpVerb: HTTP verb value"}
    @Param {value:"path: Resource path "}
    @Param {value:"req: An HTTP outbound request message"}
    @Return {value:"The inbound response message"}
    @Return {value:"Error occured during HTTP client invocation"}
    native action execute (string httpVerb, string path, OutRequest req) (InResponse, HttpConnectorError);

    @Description {value:"The PATCH action implementation of the HTTP Connector."}
    @Param {value:"path: Resource path "}
    @Param {value:"req: An HTTP outbound request message"}
    @Return {value:"The inbound response message"}
    @Return {value:"Error occured during HTTP client invocation"}
    native action patch (string path, OutRequest req) (InResponse, HttpConnectorError);

    @Description {value:"The DELETE action implementation of the HTTP connector"}
    @Param {value:"path: Resource path "}
    @Param {value:"req: An HTTP outbound request message"}
    @Return {value:"The inbound response message"}
    @Return {value:"Error occured during HTTP client invocation"}
    native action delete (string path, OutRequest req) (InResponse, HttpConnectorError);

    @Description {value:"GET action implementation of the HTTP Connector"}
    @Param {value:"path: Request path"}
    @Param {value:"req: An HTTP outbound request message"}
    @Return {value:"The inbound response message"}
    @Return {value:"Error occured during HTTP client invocation"}
    native action get (string path, OutRequest req) (InResponse, HttpConnectorError);

    @Description {value:"OPTIONS action implementation of the HTTP Connector"}
    @Param {value:"path: Request path"}
    @Param {value:"req: An HTTP outbound request message"}
    @Return {value:"The inbound response message"}
    @Return {value:"Error occured during HTTP client invocation"}
    native action options (string path, OutRequest req) (InResponse, HttpConnectorError);

    @Description {value:"Forward action can be used to invoke an HTTP call with inbound request's HTTP verb"}
    @Param {value:"path: Request path"}
    @Param {value:"req: An HTTP inbound request message"}
    @Return {value:"The inbound response message"}
    @Return {value:"Error occured during HTTP client invocation"}
    native action forward (string path, InRequest req) (InResponse, HttpConnectorError);
}

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
    boolean isShared = false;
    int expiryTimeMillis = 86400;
    int capacity = 8388608; // 8MB
    float evictionFactor = 0.2;
    CachingLevel cachingLevel;
}

@Description {value:"Initializes the CacheConfig struct to its default values"}
@Param {value:"cacheConfig: The CacheConfig struct to be initialized"}
public function <CacheConfig cacheConfig> CacheConfig () {
    cacheConfig.cachingLevel = CachingLevel.CACHE_CONTROL_AND_VALIDATORS;
}

@Description {value:"An HTTP caching client implementation which takes an HttpClient and wraps it with a caching layer."}
@Param {value:"httpClient: The underlying HTTP client which will be making the actual network calls"}
@Param {value:"cacheConfig: Caching configurations for the HTTP cache"}
public connector HttpCachingClient (HttpClient httpClient, CacheConfig cacheConfig) {

    endpoint<HttpClient> httpEP {
        httpClient;
    }

    HttpCache cache = createHttpCache("http-cache", cacheConfig);

    @Description {value:"Responses returned for POST requests are not cacheable. Therefore, the requests are simply directed to the origin server."}
    @Param {value:"path: Resource path "}
    @Param {value:"req: An HTTP outbound request message"}
    @Return {value:"The inbound response message"}
    @Return {value:"Error occured during HTTP client invocation"}
    action post (string path, OutRequest req) (InResponse, HttpConnectorError) {
        InResponse inboundResponse;
        HttpConnectorError err;
        inboundResponse, err = httpEP.post(path, req);
        invalidateResponses(cache, inboundResponse, path);
        return inboundResponse, err;
    }

    @Description {value:"Responses for HEAD requests are cacheable and as such, will be routed through the HTTP cache. Only if a suitable response cannot be found will the request be directed to the origin server."}
    @Param {value:"path: Resource path "}
    @Param {value:"req: An HTTP outbound request message"}
    @Return {value:"The inbound response message"}
    @Return {value:"Error occured during HTTP client invocation"}
    action head (string path, OutRequest req) (InResponse, HttpConnectorError) {
        return getCachedResponse(cache, httpClient, req, HTTP_HEAD, path, cacheConfig.isShared);
    }

    @Description {value:"Responses returned for PUT requests are not cacheable. Therefore, the requests are simply directed to the origin server. In addition, PUT requests invalidate the currently stored responses for the given path."}
    @Param {value:"path: Resource path "}
    @Param {value:"req: An HTTP outbound request message"}
    @Return {value:"The inbound response message"}
    @Return {value:"Error occured during HTTP client invocation"}
    action put (string path, OutRequest req) (InResponse, HttpConnectorError) {
        InResponse inboundResponse;
        HttpConnectorError err;
        inboundResponse, err = httpEP.put(path, req);
        invalidateResponses(cache, inboundResponse, path);
        return inboundResponse, err;
    }

    @Description {value:"Invokes an HTTP call with the specified HTTP verb. This is not a cacheable operation."}
    @Param {value:"httpMethod: HTTP method to be used for the request"}
    @Param {value:"path: Resource path "}
    @Param {value:"req: An HTTP outbound request message"}
    @Return {value:"The inbound response message"}
    @Return {value:"Error occured during HTTP client invocation"}
    action execute (string httpMethod, string path, OutRequest req) (InResponse, HttpConnectorError) {
        if (httpMethod == HTTP_GET || httpMethod == HTTP_HEAD) {
            return getCachedResponse(cache, httpClient, req, httpMethod, path, cacheConfig.isShared);
        }
        InResponse inboundResponse;
        HttpConnectorError err;
        inboundResponse, err = httpEP.execute(httpMethod, path, req);
        invalidateResponses(cache, inboundResponse, path);
        return inboundResponse, err;
    }

    @Description {value:"Responses returned for PATCH requests are not cacheable. Therefore, the requests are simply directed to the origin server."}
    @Param {value:"path: Resource path "}
    @Param {value:"req: An HTTP outbound request message"}
    @Return {value:"The inbound response message"}
    @Return {value:"Error occured during HTTP client invocation"}
    action patch (string path, OutRequest req) (InResponse, HttpConnectorError) {
        InResponse inboundResponse;
        HttpConnectorError err;
        inboundResponse, err = httpEP.patch(path, req);
        invalidateResponses(cache, inboundResponse, path);
        return inboundResponse, err;
    }

    @Description {value:"Responses returned for DELETE requests are not cacheable. Therefore, the requests are simply directed to the origin server."}
    @Param {value:"path: Resource path "}
    @Param {value:"req: An HTTP outbound request message"}
    @Return {value:"The inbound response message"}
    @Return {value:"Error occured during HTTP client invocation"}
    action delete (string path, OutRequest req) (InResponse, HttpConnectorError) {
        InResponse inboundResponse;
        HttpConnectorError err;
        inboundResponse, err = httpEP.delete(path, req);
        invalidateResponses(cache, inboundResponse, path);
        return inboundResponse, err;
    }

    @Description {value:"Responses for GET requests are cacheable and as such, will be routed through the HTTP cache. Only if a suitable response cannot be found will the request be directed to the origin server."}
    @Param {value:"path: Request path"}
    @Param {value:"req: An HTTP outbound request message"}
    @Return {value:"The inbound response message"}
    @Return {value:"Error occured during HTTP client invocation"}
    action get (string path, OutRequest req) (InResponse, HttpConnectorError) {
        return getCachedResponse(cache, httpClient, req, HTTP_GET, path, cacheConfig.isShared);
    }

    @Description {value:"Responses returned for OPTIONS requests are not cacheable. Therefore, the requests are simply directed to the origin server."}
    @Param {value:"path: Request path"}
    @Param {value:"req: An HTTP outbound request message"}
    @Return {value:"The inbound response message"}
    @Return {value:"Error occured during HTTP client invocation"}
    action options (string path, OutRequest req) (InResponse, HttpConnectorError) {
        InResponse inboundResponse;
        HttpConnectorError err;
        inboundResponse, err = httpEP.options(path, req);
        invalidateResponses(cache, inboundResponse, path);
        return inboundResponse, err;
    }

    @Description {value:"Forward action can be used to invoke an HTTP call with inbound request's HTTP method. Only inbound requests of GET and HEAD HTTP method types are cacheable."}
    @Param {value:"path: Request path"}
    @Param {value:"req: An HTTP inbound request message"}
    @Return {value:"The inbound response message"}
    @Return {value:"Error occured during HTTP client invocation"}
    action forward (string path, InRequest req) (InResponse, HttpConnectorError) {
        // TODO: handle response caching for forwarded GET and HEAD requests.
        return httpEP.forward(path, req);
    }
}

function getCachedResponse (HttpCache cache, HttpClient httpClient, OutRequest req, string httpMethod, string path,
                            boolean isShared) (InResponse, HttpConnectorError) {
    endpoint<HttpClient> httpEP {
        httpClient;
    }
    time:Time currentT = time:currentTime();
    InResponse cachedResponse = cache.get(getCacheKey(httpMethod, path));
    req.parseOutReqCacheControlHeader();

    // Based on https://tools.ietf.org/html/rfc7234#section-4
    if (cachedResponse != null) {
        log:printTrace("Cached response found for: '" + httpMethod + " " + path + "'");

        if (isFreshResponse(cachedResponse, isShared)) {
            // If the no-cache directive is not set, responses can be served straight from the cache, without
            // validating with the origin server.
            if (!req.cacheControl.noCache && !cachedResponse.cacheControl.noCache
                                              && (req.getHeader(PRAGMA) == null)) {
                setAgeHeader(cachedResponse);
                log:printTrace("Serving a cached fresh response without validating with the origin server");
                return cachedResponse, null;
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
                                              && (req.getHeader(PRAGMA) == null)) {
                log:printTrace("Serving cached stale response without validating with the origin server");
                setAgeHeader(cachedResponse);
                cachedResponse.setHeader(WARNING, WARNING_110_RESPONSE_IS_STALE);
                return cachedResponse, null;
            }
        }

        log:printTrace("Validating a stale response for '" + path + "' with the origin server.");
        return getValidationResponse(httpClient, req, cachedResponse, cache, currentT, path, httpMethod, false);
    }

    InResponse newResponse;
    HttpConnectorError err;
    log:printTrace("Sending new request to: " + path);
    newResponse, err = httpEP.get(path, req);
    if (newResponse != null && cache.isAllowedToCache(newResponse)) {
        newResponse.requestTime = currentT.time;
        newResponse.receivedTime = time:currentTime().time;
        cache.put(getCacheKey(httpMethod, path), req.cacheControl, newResponse);
    }
    return newResponse, err;
}

function getValidationResponse (HttpClient httpClient, OutRequest req, InResponse cachedResponse, HttpCache cache,
                                time:Time currentT, string path, string httpMethod, boolean isFreshResponse)
(InResponse, HttpConnectorError) {
    // If the no-cache directive is set, always validate the response before serving
    InResponse validationResponse;
    HttpConnectorError validationErr;

    if (isFreshResponse) {
        log:printTrace("Sending validation request for a fresh response");
    } else {
        log:printTrace("Sending validation request for a stale response");
    }

    validationResponse, validationErr = sendValidationRequest(httpClient, path, cachedResponse);

    // Based on https://tools.ietf.org/html/rfc7234#section-4.2.4
    // This behaviour is based on the fact that currently HttpConnectorError structs are returned only
    // if the connection is refused or the connection times out.
    if (validationErr != null) {
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
        return cachedResponse, validationErr;
    }

    log:printTrace("Response for validation request received");
    // Based on https://tools.ietf.org/html/rfc7234#section-4.3.3
    if (validationResponse.statusCode == RESPONSE_304_NOT_MODIFIED) {
        return handle304Response(validationResponse, cachedResponse, cache, path, httpMethod);
    } else if (validationResponse.statusCode >= 500 && validationResponse.statusCode < 600) {
        // May forward the response or act as if the origin server failed to respond and serve a
        // stored response
        // TODO: Make the above mentioned behaviour user-configurable
        return validationResponse, null;
    } else {
        // Forward the received response and replace the stored responses
        validationResponse.requestTime = currentT.time;
        cache.put(getCacheKey(httpMethod, path), req.cacheControl, validationResponse);
        log:printTrace("Received a full response. Storing it in cache and forwarding to the client");
        return validationResponse, null;
    }
}

// Based on https://tools.ietf.org/html/rfc7234#section-4.3.4
function handle304Response (InResponse validationResponse, InResponse cachedResponse, HttpCache cache, string path,
                            string httpMethod) (InResponse, HttpConnectorError) {
    log:printTrace("304 response received");
    string etag = validationResponse.getHeader(ETAG);

    if (etag != null) {
        if (isAStrongValidator(etag)) {
            // Assuming ETags are the only strong validators
            InResponse[] matchingCachedResponses = cache.getAllByETag(getCacheKey(httpMethod, path), etag);

            foreach resp in matchingCachedResponses {
                updateResponse(resp, validationResponse);
            }
            log:printTrace("304 response received. Strong validator. Response(s) updated");
            return cachedResponse, null;
        } else if (hasAWeakValidator(validationResponse, etag)) {
            // The weak validator should be either an ETag or a last modified date. Precedence given to ETag
            if (etag != null) {
                InResponse[] matchingCachedResponses = cache.getAllByWeakETag(getCacheKey(httpMethod, path), etag);

                foreach resp in matchingCachedResponses {
                    updateResponse(resp, validationResponse);
                }
                log:printTrace("304 response received. Weak validator. Response(s) updated");
                return cachedResponse, null;
            }

            // TODO: check if last modified date can be used here as a weak validator
        }
    }

    if ((cachedResponse.getHeader(ETAG) == null) && (cachedResponse.getHeader(LAST_MODIFIED) == null)) {
        updateResponse(cachedResponse, validationResponse);
    }
    log:printTrace("304 response received. No validators. Returning cached response");
    // TODO: Check if this behaviour is the expected one
    return cachedResponse, null;
}

// Based on https://tools.ietf.org/html/rfc7234#section-4.4
function invalidateResponses (HttpCache httpCache, InResponse inboundResponse, string path) {
    // TODO: Improve this logic in accordance with the spec
    if (inboundResponse != null && (isCacheableStatusCode(inboundResponse.statusCode) &&
                                    inboundResponse.statusCode >= 200 && inboundResponse.statusCode < 400)) {
        httpCache.cache.remove(getCacheKey(HTTP_GET, path));
        httpCache.cache.remove(getCacheKey(HTTP_HEAD, path));
    }
}

// Based on https://tools.ietf.org/html/rfc7234#section-4.2.1
function getFreshnessLifetime (InResponse cachedResponse, boolean isSharedCache) (int) {
    // TODO: Ensure that duplicate directives are not counted towards freshness lifetime.
    if (isSharedCache && cachedResponse.cacheControl.sMaxAge >= 0) {
        return cachedResponse.cacheControl.sMaxAge;
    }

    if (cachedResponse.cacheControl.maxAge >= 0) {
        return cachedResponse.cacheControl.maxAge;
    }

    string[] expiresHeader = cachedResponse.getHeaders(EXPIRES);

    // At this point, there should be exactly one Expires header to calculate the freshness lifetime.
    // When adding heuristic calculations, the condition would change to >1.
    if (expiresHeader != null && lengthof expiresHeader == 1 && expiresHeader[0] != null) {
        string[] dateHeader = cachedResponse.getHeaders(DATE);

        if (dateHeader != null && lengthof dateHeader == 1 && dateHeader[0] != null) {
            int freshnessLifetime = (time:parse(expiresHeader[0], RFC_1123_DATE_TIME_FORMAT).time
                                     - time:parse(dateHeader[0], RFC_1123_DATE_TIME_FORMAT).time) / 1000;
            return freshnessLifetime;
        }
    }

    // TODO: Add heuristic freshness lifetime calculation

    return STALE;
}

function isFreshResponse (InResponse cachedResponse, boolean isSharedCache) (boolean) {
    int currentAge = getAgeValue(cachedResponse.getHeader(AGE));
    int freshnessLifetime = getFreshnessLifetime(cachedResponse, isSharedCache);
    return freshnessLifetime >= currentAge;
}

// Based on https://tools.ietf.org/html/rfc7234#section-4.2.4
function isAllowedToBeServedStale (RequestCacheControl requestCacheControl, InResponse cachedResponse,
                                   boolean isSharedCache) (boolean) {
    // A cache MUST NOT generate a stale response if it is prohibited by an explicit in-protocol directive
    if (isServingStaleProhibited(requestCacheControl, cachedResponse.cacheControl)) {
        return false;
    }
    return isStaleResponseAccepted(requestCacheControl, cachedResponse, isSharedCache);
}

// Based on https://tools.ietf.org/html/rfc7234#section-4.2.4
function isServingStaleProhibited (RequestCacheControl requestCacheControl,
                                   ResponseCacheControl responseCacheControl) (boolean) {
    // A cache MUST NOT generate a stale response if it is prohibited by an explicit in-protocol directive
    return requestCacheControl.noStore ||
           requestCacheControl.noCache ||
           responseCacheControl.mustRevalidate ||
           responseCacheControl.proxyRevalidate ||
           (responseCacheControl.sMaxAge >= 0);
}

// Based on https://tools.ietf.org/html/rfc7234#section-4.2.4
function isStaleResponseAccepted (RequestCacheControl requestCacheControl, InResponse cachedResponse, boolean isSharedCache) (boolean) {
    if (requestCacheControl.maxStale == MAX_STALE_ANY_AGE) {
        return true;
    } else if (requestCacheControl.maxStale >=
               (getAgeValue(cachedResponse.getHeader(AGE)) - getFreshnessLifetime(cachedResponse, isSharedCache))) {
        return true;
    }
    return false;
}

// Based https://tools.ietf.org/html/rfc7234#section-4.3.1
function sendValidationRequest (HttpClient httpClient, string path, InResponse cachedResponse) (InResponse, HttpConnectorError) {
    endpoint<HttpClient> httpEP {
        httpClient;
    }
    OutRequest validationRequest = {};
    string etagHeader = cachedResponse.getHeader(ETAG);
    string lastModifiedHeader = cachedResponse.getHeader(LAST_MODIFIED);

    if (etagHeader != null) {
        validationRequest.setHeader(IF_NONE_MATCH, etagHeader);
    }

    if (lastModifiedHeader != null) {
        validationRequest.setHeader(IF_MODIFIED_SINCE, lastModifiedHeader);
    }

    // TODO: handle cases where neither of the above 2 headers are present

    InResponse validationResponse;
    HttpConnectorError err;

    validationResponse, err = httpEP.get(path, validationRequest);
    return validationResponse, err;
}

function setAgeHeader (InResponse cachedResponse) {
    cachedResponse.setHeader(AGE, "" + calculateCurrentResponseAge(cachedResponse));
}

// Based on https://tools.ietf.org/html/rfc7234#section-4.2.3
function calculateCurrentResponseAge (InResponse cachedResponse) (int) {
    int ageValue = getAgeValue(cachedResponse.getHeader(AGE));
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
function updateResponse (InResponse cachedResponse, InResponse validationResponse) {
    // 1 - delete warning headers with warn codes 1xx
    // 2 - retain warning headers with warn codes 2xx
    // 3 - use other headers in validation response to replace corresponding headers in cached response
    retain2xxWarnings(cachedResponse);
    replaceHeaders(cachedResponse, validationResponse);
}

// Based on https://tools.ietf.org/html/rfc7234#section-4.3.4
function hasAWeakValidator (InResponse validationResponse, string etag) (boolean) {
    return (validationResponse.getHeader(LAST_MODIFIED) != null || (etag != null && !isAStrongValidator(etag)));
}

// Based on https://tools.ietf.org/html/rfc7234#section-4.3.4
function isAStrongValidator (string etag) (boolean) {
    // TODO: Consider cases where Last-Modified can also be treated as a strong validator as per
    // https://tools.ietf.org/html/rfc7232#section-2.2.2
    if (!etag.hasPrefix(WEAK_VALIDATOR_TAG)) {
        return true;
    }

    return false;
}

// Based on https://tools.ietf.org/html/rfc7234#section-4.3.4
function replaceHeaders (InResponse cachedResponse, InResponse validationResponse) {
    map uptodateHeaders = validationResponse.getAllHeaders();

    foreach headerName, headerValues in uptodateHeaders {
        error err;
        var valueArray, err = (string[])headerValues;

        if (err != null) {
            next; // Skip the current header if there was an error in retrieving the header values
        }

        cachedResponse.removeHeader(headerName); // Remove existing headers before adding the up-to-date headers
        foreach value in valueArray {
            cachedResponse.addHeader(headerName, value);
        }
    }
}

function retain2xxWarnings (InResponse cachedResponse) {
    string[] warningHeaders = cachedResponse.getHeaders(WARNING);

    if (warningHeaders != null) {
        cachedResponse.removeHeader(WARNING);
        // TODO: Need to handle this in a better way using regex when the required regex APIs are there
        foreach warningHeader in warningHeaders {
            if (warningHeader.contains("214") || warningHeader.contains("299")) {
                log:printTrace("Adding header");
                cachedResponse.addHeader(WARNING, warningHeader);
                next;
            }
        }
    }
}

function getAgeValue (string ageHeader) (int) {
    if (ageHeader == null) {
        return 0;
    }
    int ageValue;
    error err;
    ageValue, err = <int>ageHeader;

    return err == null ? ageValue : 0;
}

function getDateValue (InResponse inboundResponse) (int) {
    string dateHeader = inboundResponse.getHeader(DATE);

    // Based on https://tools.ietf.org/html/rfc7231#section-7.1.1.2
    if (dateHeader == null) {
        time:Time currentT = time:currentTime();
        inboundResponse.setHeader(DATE, currentT.format(RFC_1123_DATE_TIME_FORMAT));
        return currentT.time;
    }

    // TODO: May need to handle invalid date headers
    time:Time dateHeaderTime = time:parse(dateHeader, RFC_1123_DATE_TIME_FORMAT);
    return dateHeaderTime.time;
}

function getWarningAgent () (string) {
    string ballerinaVersion = runtime:getProperty("ballerina.version");
    return "ballerina-http-caching-client/" + ballerinaVersion;
}
