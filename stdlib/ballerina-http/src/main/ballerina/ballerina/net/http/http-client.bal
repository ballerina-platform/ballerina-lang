package ballerina.net.http;

import ballerina.runtime;
import ballerina.time;
import ballerina.io;
import ballerina.log;

@Description { value:"HTTP client connector for outbound HTTP requests"}
@Param { value:"serviceUri: URI of the service" }
@Param { value:"connectorOptions: connector options" }
public connector HttpClient (string serviceUri, Options connectorOptions) {

    @Description { value:"The POST action implementation of the HTTP Connector."}
    @Param { value:"path: Resource path " }
    @Param { value:"req: An HTTP outbound request message" }
    @Return { value:"The inbound response message" }
    @Return { value:"Error occured during HTTP client invocation" }
    native action post (string path, OutRequest req) (InResponse, HttpConnectorError);

    @Description { value:"The HEAD action implementation of the HTTP Connector."}
    @Param { value:"path: Resource path " }
    @Param { value:"req: An HTTP outbound request message" }
    @Return { value:"The inbound response message" }
    @Return { value:"Error occured during HTTP client invocation" }
    native action head (string path, OutRequest req) (InResponse, HttpConnectorError);

    @Description { value:"The PUT action implementation of the HTTP Connector."}
    @Param { value:"path: Resource path " }
    @Param { value:"req: An HTTP outbound request message" }
    @Return { value:"The inbound response message" }
    @Return { value:"Error occured during HTTP client invocation" }
    native action put (string path, OutRequest req) (InResponse, HttpConnectorError);

    @Description { value:"Invokes an HTTP call with the specified HTTP verb."}
    @Param { value:"httpVerb: HTTP verb value" }
    @Param { value:"path: Resource path " }
    @Param { value:"req: An HTTP outbound request message" }
    @Return { value:"The inbound response message" }
    @Return { value:"Error occured during HTTP client invocation" }
    native action execute (string httpVerb, string path, OutRequest req) (InResponse, HttpConnectorError);

    @Description { value:"The PATCH action implementation of the HTTP Connector."}
    @Param { value:"path: Resource path " }
    @Param { value:"req: An HTTP outbound request message" }
    @Return { value:"The inbound response message" }
    @Return { value:"Error occured during HTTP client invocation" }
    native action patch (string path, OutRequest req) (InResponse, HttpConnectorError);

    @Description { value:"The DELETE action implementation of the HTTP connector"}
    @Param { value:"path: Resource path " }
    @Param { value:"req: An HTTP outbound request message" }
    @Return { value:"The inbound response message" }
    @Return { value:"Error occured during HTTP client invocation" }
    native action delete (string path, OutRequest req) (InResponse, HttpConnectorError);

    @Description { value:"GET action implementation of the HTTP Connector"}
    @Param { value:"path: Request path" }
    @Param { value:"req: An HTTP outbound request message" }
    @Return { value:"The inbound response message" }
    @Return { value:"Error occured during HTTP client invocation" }
    native action get (string path, OutRequest req) (InResponse, HttpConnectorError);

    @Description { value:"OPTIONS action implementation of the HTTP Connector"}
    @Param { value:"path: Request path" }
    @Param { value:"req: An HTTP outbound request message" }
    @Return { value:"The inbound response message" }
    @Return { value:"Error occured during HTTP client invocation" }
    native action options (string path, OutRequest req) (InResponse, HttpConnectorError);

    @Description { value:"Forward action can be used to invoke an HTTP call with inbound request's HTTP verb"}
    @Param { value:"path: Request path" }
    @Param { value:"req: An HTTP inbound request message" }
    @Return { value:"The inbound response message" }
    @Return { value:"Error occured during HTTP client invocation" }
    native action forward (string path, InRequest req) (InResponse, HttpConnectorError);
}

// HTTP CACHING CLIENT IMPLEMENTATION.
// Adding this to natives.bal temporarily, until issue #4865 gets fixed
const string WARNING_AGENT = getWarningAgent();

public const string WARNING_110_RESPONSE_IS_STALE = "110 " + WARNING_AGENT + " \"Response is Stale\"";
public const string WARNING_111_REVALIDATION_FAILED = "111 " + WARNING_AGENT + " \"Revalidation Failed\"";

const string WEAK_VALIDATOR_TAG = "W/";
const int STALE = -1;

Regex WARN_1xx_REGEX = {pattern:"\\s*1\\d\\d.*"};
Regex WARN_2xx_REGEX = {pattern:"\\s*2\\d\\d.*"};

public connector HttpCachingClient (HttpClient httpClient, boolean isShared) {

    endpoint<HttpClient> httpEP {
        httpClient;
    }

    HttpCache cache = createHttpCache("http-cache", 86400, 1024 * 1024, 0.5, isShared);

    @Description {value:"The POST action implementation of the HTTP Connector."}
    @Param {value:"path: Resource path "}
    @Param {value:"req: An HTTP outbound request message"}
    @Return {value:"The inbound response message"}
    @Return {value:"Error occured during HTTP client invocation"}
    action post (string path, OutRequest req) (InResponse, HttpConnectorError) {
        return null, null;
    }

    @Description {value:"The HEAD action implementation of the HTTP Connector."}
    @Param {value:"path: Resource path "}
    @Param {value:"req: An HTTP outbound request message"}
    @Return {value:"The inbound response message"}
    @Return {value:"Error occured during HTTP client invocation"}
    action head (string path, OutRequest req) (InResponse, HttpConnectorError) {
        return null, null;
    }

    @Description {value:"The PUT action implementation of the HTTP Connector."}
    @Param {value:"path: Resource path "}
    @Param {value:"req: An HTTP outbound request message"}
    @Return {value:"The inbound response message"}
    @Return {value:"Error occured during HTTP client invocation"}
    action put (string path, OutRequest req) (InResponse, HttpConnectorError) {
        return null, null;
    }

    @Description {value:"Invokes an HTTP call with the specified HTTP verb."}
    @Param {value:"httpVerb: HTTP verb value"}
    @Param {value:"path: Resource path "}
    @Param {value:"req: An HTTP outbound request message"}
    @Return {value:"The inbound response message"}
    @Return {value:"Error occured during HTTP client invocation"}
    action execute (string httpVerb, string path, OutRequest req) (InResponse, HttpConnectorError) {
        return null, null;
    }

    @Description {value:"The PATCH action implementation of the HTTP Connector."}
    @Param {value:"path: Resource path "}
    @Param {value:"req: An HTTP outbound request message"}
    @Return {value:"The inbound response message"}
    @Return {value:"Error occured during HTTP client invocation"}
    action patch (string path, OutRequest req) (InResponse, HttpConnectorError) {
        return null, null;
    }

    @Description {value:"The DELETE action implementation of the HTTP connector"}
    @Param {value:"path: Resource path "}
    @Param {value:"req: An HTTP outbound request message"}
    @Return {value:"The inbound response message"}
    @Return {value:"Error occured during HTTP client invocation"}
    action delete (string path, OutRequest req) (InResponse, HttpConnectorError) {
        return null, null;
    }

    @Description {value:"GET action implementation of the HTTP Connector"}
    @Param {value:"path: Request path"}
    @Param {value:"req: An HTTP outbound request message"}
    @Return {value:"The inbound response message"}
    @Return {value:"Error occured during HTTP client invocation"}
    action get (string path, OutRequest req) (InResponse, HttpConnectorError) {
        time:Time currentT = time:currentTime();
        InResponse cachedResponse = cache.get("GET", path, req);
        req.parseCacheControlHeader();

        // Based on https://tools.ietf.org/html/rfc7234#section-4
        if (cachedResponse != null) {
            log:printInfo("Cached response found");

            // If the no-cache directive is not set, responses can be served straight from the cache, without validating
            // with the origin server.
            if (!req.cacheControl.noCache && !cachedResponse.cacheControl.noCache && (req.getHeader(PRAGMA) == null)) {
                log:printInfo("Cached response can be served without validating with origin server");
                if (isFreshResponse(cachedResponse, isShared)) {
                    setAgeHeader(cachedResponse);
                    log:printInfo("Serving a fresh response");
                    return cachedResponse, null;
                }

                // If a fresh response is not available, serve a stale response, provided that it is not prohibited by
                // a directive and is explicitly allowed in the request.
                if (isAllowedToBeServedStale(req, cachedResponse, isShared)) {
                    setAgeHeader(cachedResponse);
                    cachedResponse.setHeader(WARNING, WARNING_110_RESPONSE_IS_STALE);
                    log:printInfo("Serving a stale response");
                    return cachedResponse, null;
                }
                log:printInfo("Failed to serve either a fresh or a stale response without validating with origin server");
            } else {
                // If the no-cache directive is set, always validate the response before serving
                InResponse validationResponse;
                HttpConnectorError validationErr;
                log:printInfo("Sending validation request");
                validationResponse, validationErr = sendValidationRequest(httpClient, path, cachedResponse);

                // Based on https://tools.ietf.org/html/rfc7234#section-4.2.4
                // This behaviour is based on the fact that currently HttpConnectorError structs are returned only
                // if the connection is refused or the connection times out.
                if (validationErr != null) {
                    // TODO: Verify that this behaviour is valid: returning a fresh response when 'no-cache' is present
                    // and origin server couldn't be reached.
                    log:printInfo("Cannot reach origin server");
                    if (isFreshResponse(cachedResponse, isShared)) {
                        setAgeHeader(cachedResponse);
                        log:printInfo("Cannot reach origin server. Sending fresh response");
                        return cachedResponse, null;
                    }

                    // If the origin server cannot be reached and a fresh response is unavailable, serve a stale
                    // response (unless it is prohibited through a directive).
                    if (!isServingStaleProhibited(req, cachedResponse)) {
                        setAgeHeader(cachedResponse);
                        cachedResponse.setHeader(WARNING, WARNING_111_REVALIDATION_FAILED);
                        log:printInfo("Cannot reach origin server. Sending stale response");
                        return cachedResponse, null;
                    }

                    return null, validationErr;
                }

                log:printInfo("Response for validation request received");
                // Based on https://tools.ietf.org/html/rfc7234#section-4.3.3
                if (validationResponse.statusCode == 304) {
                    // Use stored response
                    log:printInfo("304 response received");
                    string etag = validationResponse.getHeader(ETAG);
                    if (isAStrongValidator(etag)) {
                        // Assuming only ETags are strong validators
                        InResponse[] matchingCachedResponses = cache.getAllByETag(etag);

                        foreach resp in matchingCachedResponses {
                            updateResponse(resp, validationResponse);
                        }
                        log:printInfo("304 response received. Strong validator. Response(s) updated");
                        return cachedResponse, null;
                    } else if (hasAWeakValidator(validationResponse, etag)) {
                        // The weak validator should be either an ETag or a last modified date. Precedence given to ETag
                        if (etag != null) {
                            InResponse[] matchingCachedResponses = cache.getAllByWeakETag(etag);

                            foreach resp in matchingCachedResponses {
                                updateResponse(resp, validationResponse);
                            }
                            log:printInfo("304 response received. Weak validator. Response(s) updated");
                            return cachedResponse, null;
                        }

                        // TODO: check if last modified date can be used here
                    }

                    if ((cachedResponse.getHeader(ETAG) == null) && (cachedResponse.getHeader(LAST_MODIFIED) == null)) {
                        updateResponse(cachedResponse, validationResponse);
                    }
                    log:printInfo("304 response received. No validators. Returning cached response");
                    // TODO: Check if this behaviour is the expected one
                    return cachedResponse, null;
                } else if (validationResponse.statusCode >= 500 && validationResponse.statusCode < 600) {
                    // May forward the response or act as if the origin server failed to respond and serve a
                    // stored response
                    // TODO: Make the above mentioned behaviour user-configurable
                    return validationResponse, null;
                } else {
                    // Forward the received response and replace the stored responses
                    validationResponse.requestTime = currentT.time;
                    cache.put("GET", path, req, validationResponse);
                    log:printInfo("Received a full response. Storing it in cache and forwarding to the client");
                    return validationResponse, null;
                }
            }
        }

        HttpConnectorError err;
        log:printInfo("Sending new request");
        cachedResponse, err = httpEP.get(path, req);
        cachedResponse.requestTime = currentT.time;
        cachedResponse.receivedTime = time:currentTime().time;
        cache.put("GET", path, req, cachedResponse);
        return cachedResponse, err;
    }

    @Description {value:"OPTIONS action implementation of the HTTP Connector"}
    @Param {value:"path: Request path"}
    @Param {value:"req: An HTTP outbound request message"}
    @Return {value:"The inbound response message"}
    @Return {value:"Error occured during HTTP client invocation"}
    action options (string path, OutRequest req) (InResponse, HttpConnectorError) {
        return null, null;
    }

    @Description {value:"Forward action can be used to invoke an HTTP call with inbound request's HTTP verb"}
    @Param {value:"path: Request path"}
    @Param {value:"req: An HTTP inbound request message"}
    @Return {value:"The inbound response message"}
    @Return {value:"Error occured during HTTP client invocation"}
    action forward (string path, InRequest req) (InResponse, HttpConnectorError) {
        return null, null;
    }
}

// Based on https://tools.ietf.org/html/rfc7234#section-4.2.1
function getFreshnessLifetime (InResponse cachedResponse, boolean isSharedCache) (int) {
    if (isSharedCache && cachedResponse.cacheControl.sMaxAge >= 0) {
        return cachedResponse.cacheControl.sMaxAge;
    }

    if (cachedResponse.cacheControl.maxAge >= 0) {
        return cachedResponse.cacheControl.maxAge;
    }

    string expiresHeader = cachedResponse.getHeader(EXPIRES);
    if (expiresHeader != null) {
        string dateHeader = cachedResponse.getHeader(DATE); // Not checking for null since this header should be there
        int freshnessLifetime = (time:parse(expiresHeader, RFC_1123_DATE_TIME_FORMAT).time
                                 - time:parse(dateHeader, RFC_1123_DATE_TIME_FORMAT).time) / 1000;
        return freshnessLifetime;
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
function isAllowedToBeServedStale (OutRequest clientRequest, InResponse cachedResponse,
                                   boolean isSharedCache) (boolean) {
    // A cache MUST NOT generate a stale response if it is prohibited by an explicit in-protocol directive
    if (isServingStaleProhibited(clientRequest, cachedResponse)) {
        return false;
    }

    return isStaleResponseAccepted(clientRequest, cachedResponse, isSharedCache);
}

// Based on https://tools.ietf.org/html/rfc7234#section-4.2.4
function isServingStaleProhibited (OutRequest clientRequest, InResponse cachedResponse) (boolean) {
    // A cache MUST NOT generate a stale response if it is prohibited by an explicit in-protocol directive
    return clientRequest.cacheControl.noStore ||
           clientRequest.cacheControl.noCache ||
           cachedResponse.cacheControl.mustRevalidate ||
           cachedResponse.cacheControl.proxyRevalidate ||
           (cachedResponse.cacheControl.sMaxAge >= 0);
}

// Based on https://tools.ietf.org/html/rfc7234#section-4.2.4
function isStaleResponseAccepted (OutRequest clientRequest, InResponse cachedResponse, boolean isSharedCache) (boolean) {
    if (clientRequest.cacheControl.maxStale == MAX_STALE_ANY_AGE) {
        return true;
    } else if (clientRequest.cacheControl.maxStale >
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

    int apparentAge = (responseTime - dateValue) >= 0?(responseTime - dateValue):0;

    int responseDelay = responseTime - requestTime;
    int correctedAgeValue = ageValue + responseDelay;

    int correctedInitialAge = apparentAge > correctedAgeValue?apparentAge:correctedAgeValue;
    int residentTime = now - responseTime;

    return (correctedInitialAge + residentTime) / 1000;
}

// Based on https://tools.ietf.org/html/rfc7234#section-4.3.4
function updateResponse (InResponse cachedResponse, InResponse validationResponse) {
    // 1 - delete warning headers with warn codes 1xx
    // 2 - retain warning headers with warn codes 2xx
    // 3 - use other headers in validation response to replace corresponding headers in cached response
    map cachedRespHeaders = cachedResponse.getAllHeaders();
    delete1xxWarnings(cachedRespHeaders);
    replaceHeaders(cachedResponse, validationResponse);
}

// Based on https://tools.ietf.org/html/rfc7234#section-4.3.4
function hasAWeakValidator (InResponse validationResponse, string etag) (boolean) {
    return !isAStrongValidator(etag) || (validationResponse.getHeader(LAST_MODIFIED) != null);
}

// Based on https://tools.ietf.org/html/rfc7234#section-4.3.4
function isAStrongValidator (string etag) (boolean) {
    // TODO: Consider cases where Last-Modified can also be treated as a strong validator as per
    // https://tools.ietf.org/html/rfc7232#section-2.2.2
    if (etag != null && !etag.hasPrefix(WEAK_VALIDATOR_TAG)) {
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

function delete1xxWarnings (map headers) {
    var warningHeaders, _ = (string[])headers[WARNING];

    foreach warningHeader in warningHeaders {
        // TODO: Implement this. There needs to be a refactoring in how headers are handled to do this.
    }
}

function getAgeValue (string ageHeader) (int) {
    if (ageHeader == null) {
        log:printInfo("Age header not set");
        return 0;
    }

    int ageValue;
    error err;
    ageValue, err = <int>ageHeader;

    return err == null?ageValue:0;
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
