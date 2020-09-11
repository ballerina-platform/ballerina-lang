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

import ballerina/time;

function isFreshResponse(Response cachedResponse, boolean isSharedCache) returns @tainted boolean {
    int currentAge = getResponseAge(cachedResponse);
    int freshnessLifetime = getFreshnessLifetime(cachedResponse, isSharedCache);
    return freshnessLifetime > currentAge;
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
    if (!cachedResponse.hasHeader(EXPIRES)) {
        return STALE;
    }

    string[] expiresHeader = cachedResponse.getHeaders(EXPIRES);

    if (expiresHeader.length() != 1) {
        return STALE;
    }

    if (!cachedResponse.hasHeader(DATE)) {
        return STALE;
    }

    string[] dateHeader = cachedResponse.getHeaders(DATE);

    if (dateHeader.length() != 1) {
        return STALE;
    }

    var tExpiresHeader = time:parse(expiresHeader[0], time:TIME_FORMAT_RFC_1123);
    var tDateHeader = time:parse(dateHeader[0], time:TIME_FORMAT_RFC_1123);
    if (tExpiresHeader is time:Time && tDateHeader is time:Time) {
        int freshnessLifetime = (tExpiresHeader.time - tDateHeader.time) /1000;
        return freshnessLifetime;
    }

    // TODO: Add heuristic freshness lifetime calculation

    return STALE;
}
