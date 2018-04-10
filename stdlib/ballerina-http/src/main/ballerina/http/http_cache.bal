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

package ballerina.http;

import ballerina/caching;

type HttpCache object {
    private {
        caching:Cache httpCache;
        CachingPolicy policy;
        boolean isShared;
    }

    function isAllowedToCache (Response response) returns boolean {
        if (policy == CACHE_CONTROL_AND_VALIDATORS) {
            return response.hasHeader(CACHE_CONTROL) && (response.hasHeader(ETAG) || response.hasHeader(LAST_MODIFIED));
        }

        return true;
    }

    function put (string key, RequestCacheControl requestCacheControl, Response inboundResponse) {
        if (inboundResponse.cacheControl.noStore ||
            requestCacheControl.noStore ||
            (inboundResponse.cacheControl.isPrivate && isShared)) {
            // TODO: Need to consider https://tools.ietf.org/html/rfc7234#section-3.2 as well here
            return;
        }

        // Based on https://tools.ietf.org/html/rfc7234#page-6
        // TODO: Consider cache control extensions as well here
        if (inboundResponse.hasHeader(EXPIRES) ||
            inboundResponse.cacheControl.maxAge >= 0 ||
            (inboundResponse.cacheControl.sMaxAge >= 0 && isShared) ||
            isCacheableStatusCode(inboundResponse.statusCode) ||
            !inboundResponse.cacheControl.isPrivate) {

            // IMPT: The call to getBinaryPayload() builds the payload from the stream. If this is not done, the stream will
            // be read by the client and the response will be after the first cache hit.
            match inboundResponse.getBinaryPayload() {
            // TODO: remove these dummy assignments once empty blocks are supported
                blob => {int x = 0;}
                mime:EntityError => {int x = 0;}
            }
            addEntry(httpCache, key, inboundResponse);
        }
    }

    function hasKey (string key) returns boolean {
        return httpCache.hasKey(key);
    }

    function get (string key) returns Response {
        match <Response[]>httpCache.get(key) {
            Response[] cacheEntry => return cacheEntry[lengthof cacheEntry - 1];
            error err => throw err;
        }
    }

    function getAll (string key) returns Response[]|() {
        try {
            match <Response[]>httpCache.get(key) {
                Response[] cacheEntry => return cacheEntry;
                error err => return ();
            }
        } catch (error e) {
            return ();
        }
        return ();
    }

    function getAllByETag (string key, string etag) returns Response[] {
        Response[] cachedResponses = [];
        Response[] matchingResponses = [];
        int i = 0;

        match getAll(key) {
            Response[] responses => cachedResponses = responses;
            () => cachedResponses = [];
        }

        foreach cachedResp in cachedResponses {
            if (cachedResp.getHeader(ETAG) == etag && !etag.hasPrefix(WEAK_VALIDATOR_TAG)) {
                matchingResponses[i] = cachedResp;
                i = i + 1;
            }
        }

        return matchingResponses;
    }

    function getAllByWeakETag (string key, string etag) returns Response[] {
        Response[] cachedResponses = [];
        Response[] matchingResponses = [];
        int i = 0;

        match getAll(key) {
            Response[] responses => cachedResponses = responses;
            () => cachedResponses = [];
        }

        foreach cachedResp in cachedResponses {
            if (cachedResp.hasHeader(ETAG) && weakValidatorEquals(etag, cachedResp.getHeader(ETAG))) {
                matchingResponses[i] = cachedResp;
                i = i + 1;
            }
        }

        return matchingResponses;
    }

    function remove (string key) {
        httpCache.remove(key);
    }
};

function createHttpCache (string name, CacheConfig cache) returns HttpCache {
    HttpCache httpCache = new;
    caching:Cache backingCache = caching:createCache(name, cache.expiryTimeMillis, cache.capacity,
                                                     cache.evictionFactor);
    httpCache.httpCache = backingCache;
    httpCache.policy = cache.policy;
    httpCache.isShared = cache.isShared;
    return httpCache;
}


function isCacheableStatusCode (int statusCode) returns boolean {
    return statusCode == OK_200 || statusCode == NON_AUTHORITATIVE_INFORMATION_203 ||
           statusCode == NO_CONTENT_204 || statusCode == PARTIAL_CONTENT_206 ||
           statusCode == MULTIPLE_CHOICES_300 || statusCode == MOVED_PERMANENTLY_301 ||
           statusCode == NOT_FOUND_404 || statusCode == METHOD_NOT_ALLOWED_405 ||
           statusCode == GONE_410 || statusCode == URI_TOO_LONG_414 ||
           statusCode == NOT_IMPLEMENTED_501;
}

function addEntry (caching:Cache cache, string key, Response inboundResponse) {
    try {
        var existingResponses = cache.get(key);
        match <Response[]>existingResponses {
            Response[] cachedRespArray => cachedRespArray[lengthof cachedRespArray] = inboundResponse;
            error err => throw err;
        }
    } catch (error e) {
        Response[] cachedResponses = [inboundResponse];
        cache.put(key, cachedResponses);
    }
}

function weakValidatorEquals (string etag1, string etag2) returns boolean {
    string validatorPortion1 = etag1.hasPrefix(WEAK_VALIDATOR_TAG) ? etag1.subString(2, lengthof etag1) : etag1;
    string validatorPortion2 = etag2.hasPrefix(WEAK_VALIDATOR_TAG) ? etag2.subString(2, lengthof etag2) : etag2;

    return validatorPortion1 == validatorPortion2;
}

function getCacheKey (string httpMethod, string url) returns string {
    return httpMethod + " " + url;
}
