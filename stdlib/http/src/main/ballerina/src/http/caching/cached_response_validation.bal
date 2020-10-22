// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import ballerina/time;

function getValidationResponse(HttpClient httpClient, Request req, Response cachedResponse, HttpCache cache,
                               time:Time currentT, string path, string httpMethod, boolean isFreshResponse)
                                                                                returns @tainted Response|ClientError {
    // If the no-cache directive is set, always validate the response before serving
    if (isFreshResponse) {
        log:printDebug("Sending validation request for a fresh response");
    } else {
        log:printDebug("Sending validation request for a stale response");
    }

    var response = sendValidationRequest(httpClient, path, req, cachedResponse);

    if (response is ClientError) {
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

    Response validationResponse = <Response>response;

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

// Based https://tools.ietf.org/html/rfc7234#section-4.3.1
function sendValidationRequest(HttpClient httpClient, string path, Request originalRequest, Response cachedResponse)
                                returns Response|ClientError {
    // Set the precondition headers only if the user hasn't explicitly set them.
    boolean userProvidedINMHeader = originalRequest.hasHeader(IF_NONE_MATCH);
    if (!userProvidedINMHeader && cachedResponse.hasHeader(ETAG)) {
        originalRequest.setHeader(IF_NONE_MATCH, cachedResponse.getHeader(ETAG));
    }

    boolean userProvidedIMSHeader = originalRequest.hasHeader(IF_MODIFIED_SINCE);
    if (!userProvidedIMSHeader && cachedResponse.hasHeader(LAST_MODIFIED)) {
        originalRequest.setHeader(IF_MODIFIED_SINCE, cachedResponse.getHeader(LAST_MODIFIED));
    }

    // TODO: handle cases where neither of the above 2 headers are present

    Response|ClientError resp = httpClient->forward(path, originalRequest);

    // Have to remove the precondition headers from the request if they weren't user provided.
    if (!userProvidedINMHeader) {
        originalRequest.removeHeader(IF_NONE_MATCH);
    }

    if (!userProvidedIMSHeader) {
        originalRequest.removeHeader(IF_MODIFIED_SINCE);
    }

    return resp;
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
