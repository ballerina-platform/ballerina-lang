package ballerina.net.http;

import ballerina.caching;

struct HttpCache {
    caching:Cache cache;
    boolean isShared;
}

function createHttpCache (string name, int expiryTimeMillis, int capacity, float evictionFactor, boolean isShared) (HttpCache) {
    HttpCache httpCache = {};
    caching:Cache backingCache = caching:createCache(name, expiryTimeMillis, capacity, evictionFactor);
    httpCache.cache = backingCache;
    httpCache.isShared = isShared;
    return httpCache;
}

function <HttpCache httpCache> put (string httpMethod, string url, OutRequest outboundRequest, InResponse inboundResponse) {
    if (inboundResponse.cacheControl.noStore || outboundRequest.cacheControl.noStore) {
        return;
    }

    // Based on https://tools.ietf.org/html/rfc7234#page-6
    if (inboundResponse.getHeader("Expires") != null ||
        inboundResponse.cacheControl.maxAge >= 0 ||
        (inboundResponse.cacheControl.sMaxAge >= 0 && httpCache.isShared) ||
        isCacheableStatusCode(inboundResponse.statusCode) ||
        !inboundResponse.cacheControl.isPrivate) {

        if (outboundRequest.getHeader("Authorization") != null) {
            // Not caching responses for requests with an 'Authorization' header
            return;
        }

        string key = httpMethod + " " + url;
        httpCache.cache.put(key, inboundResponse);
    }
}

function <HttpCache httpCache> get (string httpMethod, string url, OutRequest outboundRequest) (InResponse) {
    string key = httpMethod + " " + url;
    InResponse cachedResponse;
    cachedResponse, _ = (InResponse)httpCache.cache.get(key);
    return cachedResponse;
}

function <HttpCache httpCache> getAllByETag (string etag) (InResponse[]) {
    return null;
}

function <HttpCache httpCache> getAllByWeakETag (string etag) (InResponse[]) {
    return null;
}

function isCacheableStatusCode (int statusCode) (boolean) {
    return true;
}
