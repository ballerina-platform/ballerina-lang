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

# Implements a cache for storing HTTP responses. This cache complies with the caching policy set when configuring
# HTTP caching in the HTTP client endpoint.
#
# + cache - The underlying cache used for storing HTTP responses
# + policy - Gives the user some control over the caching behaviour. By default, this is set to
#            `CACHE_CONTROL_AND_VALIDATORS`. The default behaviour is to allow caching only when the `cache-control`
#            header and either the `etag` or `last-modified` header are present.
# + isShared - Specifies whether the HTTP caching layer should behave as a public cache or a private cache
public type HttpCache object {

    public cache:Cache cache = new;
    public CachingPolicy policy = CACHE_CONTROL_AND_VALIDATORS;
    public boolean isShared = false;

    function isAllowedToCache (Response response) returns boolean {
        if (self.policy == CACHE_CONTROL_AND_VALIDATORS) {
            return response.hasHeader(CACHE_CONTROL) && (response.hasHeader(ETAG) || response.hasHeader(LAST_MODIFIED));
        }

        return true;
    }

    function put (string key, RequestCacheControl? requestCacheControl, Response inboundResponse) {
        ResponseCacheControl? respCacheControl = inboundResponse.cacheControl;

        if ((respCacheControl.noStore ?: false) ||
            (requestCacheControl.noStore ?: false) ||
            ((respCacheControl.isPrivate ?: false)  && self.isShared)) {
            // TODO: Need to consider https://tools.ietf.org/html/rfc7234#section-3.2 as well here
            return;
        }

        // Based on https://tools.ietf.org/html/rfc7234#page-6
        // TODO: Consider cache control extensions as well here
        if (inboundResponse.hasHeader(EXPIRES) ||
            (respCacheControl.maxAge ?: -1) >= 0 ||
            ((respCacheControl.sMaxAge ?: -1) >= 0 && self.isShared) ||
            isCacheableStatusCode(inboundResponse.statusCode) ||
            !(respCacheControl.isPrivate ?: false)) {

            // IMPT: The call to getBinaryPayload() builds the payload from the stream. If this is not done, the stream
            // will be read by the client and the response will be after the first cache hit.
            var binaryPayload = inboundResponse.getBinaryPayload();
            log:printDebug(function() returns string {
                return "Adding new cache entry for: " + key;
            });
            addEntry(self.cache, key, inboundResponse);
        }
    }

    function hasKey (string key) returns boolean {
        return self.cache.hasKey(key);
    }

    function get (string key) returns Response {
        Response response = new;
        var cacheEntry = <Response[]> self.cache.get(key);
        if (cacheEntry is Response[]) {
            response = cacheEntry[cacheEntry.length() - 1];
        } else if (cacheEntry is error) {
            panic cacheEntry;
        }
        return response;
    }

    function getAll (string key) returns Response[]|() {
        var cacheEntry = <Response[]> self.cache.get(key);
        if (cacheEntry is Response[]) {
            return cacheEntry;
        }
        return ();
    }

    function getAllByETag (string key, string etag) returns Response[] {
        Response[] cachedResponses = [];
        Response[] matchingResponses = [];
        int i = 0;

        var responses = self.getAll(key);
        if (responses is Response[]) {
            cachedResponses = responses;
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

        var responses = self.getAll(key);
        if (responses is Response[]) {
            cachedResponses = responses;
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
        self.cache.remove(key);
    }
};

function createHttpCache (string name, CacheConfig cacheConfig) returns HttpCache {
    HttpCache httpCache = new;
    cache:Cache backingCache = new(expiryTimeMillis = cacheConfig.expiryTimeMillis, capacity = cacheConfig.capacity,
                                     evictionFactor = cacheConfig.evictionFactor);
    httpCache.cache = backingCache;
    httpCache.policy = cacheConfig.policy;
    httpCache.isShared = cacheConfig.isShared;
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

function addEntry (cache:Cache cache, string key, Response inboundResponse) {
    var existingResponses = cache.get(key);
    if (existingResponses is Response[]) {
        existingResponses[existingResponses.length()] = inboundResponse;
    } else if (existingResponses is ()) {
        Response[] cachedResponses = [inboundResponse];
        cache.put(key, cachedResponses);
    }
}

function weakValidatorEquals (string etag1, string etag2) returns boolean {
    string validatorPortion1 = etag1.hasPrefix(WEAK_VALIDATOR_TAG) ? etag1.substring(2, etag1.length()) : etag1;
    string validatorPortion2 = etag2.hasPrefix(WEAK_VALIDATOR_TAG) ? etag2.substring(2, etag2.length()) : etag2;

    return validatorPortion1 == validatorPortion2;
}

function getCacheKey (string httpMethod, string url) returns string {
    return httpMethod + " " + url;
}
