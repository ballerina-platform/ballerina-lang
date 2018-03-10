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

import ballerina.caching;

struct HttpCache {
    caching:Cache cache;
    CachingLevel cachingLevel;
    boolean isShared;
}

function createHttpCache (string name, CacheConfig cacheConfig) (HttpCache) {
    HttpCache httpCache = {};
    caching:Cache backingCache = caching:createCache(name, cacheConfig.expiryTimeMillis, cacheConfig.capacity,
                                                     cacheConfig.evictionFactor);
    httpCache.cache = backingCache;
    httpCache.cachingLevel = cacheConfig.cachingLevel;
    httpCache.isShared = cacheConfig.isShared;
    return httpCache;
}

function <HttpCache httpCache> isAllowedToCache (InResponse response) (boolean) {
    if (httpCache.cachingLevel == CachingLevel.CACHE_CONTROL_AND_VALIDATORS) {
        map headers = response.getAllHeaders();
        return headers[CACHE_CONTROL] != null && (headers[ETAG] != null || headers[LAST_MODIFIED] != null);
    }

    return true;
}

function <HttpCache httpCache> put (string key, RequestCacheControl requestCacheControl, InResponse inboundResponse) {
    if (inboundResponse.cacheControl.noStore ||
        requestCacheControl.noStore ||
        (inboundResponse.cacheControl.isPrivate && httpCache.isShared)) {
        // TODO: Need to consider https://tools.ietf.org/html/rfc7234#section-3.2 as well here
        return;
    }

    // Based on https://tools.ietf.org/html/rfc7234#page-6
    // TODO: Consider cache control extensions as well here
    if (inboundResponse.getHeader(EXPIRES) != null ||
        inboundResponse.cacheControl.maxAge >= 0 ||
        (inboundResponse.cacheControl.sMaxAge >= 0 && httpCache.isShared) ||
        isCacheableStatusCode(inboundResponse.statusCode) ||
        !inboundResponse.cacheControl.isPrivate) {

        // IMPT: The call to getBinaryPayload() builds the payload from the stream. If this is not done, the stream will
        // be read by the client and the response will be after the first cache hit.
        _, _ = inboundResponse.getBinaryPayload();
        addEntry(httpCache.cache, key, inboundResponse);
    }
}

function <HttpCache httpCache> get (string key) (InResponse) {
    var cacheEntry, _ = (InResponse[])httpCache.cache.get(key);
    return (cacheEntry == null) ? null : cacheEntry[lengthof cacheEntry - 1];
}

function <HttpCache httpCache> getAll (string key) (InResponse[]) {
    var cacheEntry, _ = (InResponse[])httpCache.cache.get(key);
    return cacheEntry;
}

function <HttpCache httpCache> getAllByETag (string key, string etag) (InResponse[]) {
    InResponse[] cachedResponses = httpCache.getAll(key);
    InResponse[] matchingResponses = [];
    int i = 0;

    foreach cachedResp in cachedResponses {
        if (cachedResp.getHeader(ETAG) == etag && !etag.hasPrefix(WEAK_VALIDATOR_TAG)) {
            matchingResponses[i] = cachedResp;
            i = i + 1;
        }
    }

    return matchingResponses;
}

function <HttpCache httpCache> getAllByWeakETag (string key, string etag) (InResponse[]) {
    InResponse[] cachedResponses = httpCache.getAll(key);
    InResponse[] matchingResponses = [];
    int i = 0;

    foreach cachedResp in cachedResponses {
        if (weakValidatorEquals(etag, cachedResp.getHeader(ETAG))) {
            matchingResponses[i] = cachedResp;
            i = i + 1;
        }
    }

    return matchingResponses;
}

function <HttpCache httpCache> remove (string key) {
    httpCache.cache.remove(key);
}

function isCacheableStatusCode (int statusCode) (boolean) {
    return statusCode == RESPONSE_200_OK || statusCode == RESPONSE_203_NON_AUTHORITATIVE_INFORMATION ||
           statusCode == RESPONSE_204_NO_CONTENT || statusCode == RESPONSE_206_PARTIAL_CONTENT ||
           statusCode == RESPONSE_300_MULTIPLE_CHOICES || statusCode == RESPONSE_301_MOVED_PERMANENTLY ||
           statusCode == RESPONSE_404_NOT_FOUND || statusCode == RESPONSE_405_METHOD_NOT_ALLOWED ||
           statusCode == RESPONSE_410_GONE || statusCode == RESPONSE_414_URI_TOO_LONG ||
           statusCode == RESPONSE_501_NOT_IMPLEMENTED;
}

function addEntry (caching:Cache cache, string key, InResponse inboundResponse) {
    var existingResponses = cache.get(key);
    if (existingResponses == null) {
        InResponse[] cachedResponses = [inboundResponse];
        cache.put(key, cachedResponses);
    } else {
        var cachedRespArray, _ = (InResponse[])existingResponses;
        cachedRespArray[lengthof cachedRespArray] = inboundResponse;
    }
}

function weakValidatorEquals (string etag1, string etag2) (boolean) {
    if (etag1 == null || etag2 == null) {
        return false;
    }

    string validatorPortion1 = etag1.hasPrefix(WEAK_VALIDATOR_TAG) ? etag1.subString(2, lengthof etag1) : etag1;
    string validatorPortion2 = etag2.hasPrefix(WEAK_VALIDATOR_TAG) ? etag2.subString(2, lengthof etag2) : etag2;

    return validatorPortion1 == validatorPortion2;
}

function getCacheKey (string httpMethod, string url) (string) {
    return httpMethod + " " + url;
}
