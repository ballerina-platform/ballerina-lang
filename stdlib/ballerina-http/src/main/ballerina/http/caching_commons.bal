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

// Cache-control directives
@Description {value:"Forces the cache to validate a cached response with the origin server before serving."}
public const string NO_CACHE = "no-cache";
@Description {value:"Instructs the cache to not store a response in non-volatile storage."}
public const string NO_STORE = "no-store";
@Description {value:"Instructs intermediaries not to transform the payload."}
public const string NO_TRANSFORM = "no-transform";
@Description {value:"When used in requests, it implies that clients are not willing to accept responses whose age is greater than max-age. When used in responses, the response is to be considered stale after the specified number of seconds."}
public const string MAX_AGE = "max-age";

// Request only cache-control directives
@Description {value:"Indicates that the client is willing to accept responses which have exceeded their freshness lifetime by no more than the specified number of seconds."}
public const string MAX_STALE = "max-stale";
@Description {value:"Indicates that the client is only accepting responses whose freshness lifetime >= current age + min-fresh"}
public const string MIN_FRESH = "min-fresh";
@Description {value:"Indicates that the client is only willing to accept a cached response. A cached response is served subject to other constraints posed by the request."}
public const string ONLY_IF_CACHED = "only-if-cached";

// Response only cache-control directives
@Description {value:"Indicates that once the response has become stale, it should not be reused for subsequent requests without validating with the origin server."}
public const string MUST_REVALIDATE = "must-revalidate";
@Description {value:"Indicates that any cache may store the response."}
public const string PUBLIC = "public";
@Description {value:"Indicates that the response is intended for a single user and should not be stored by shared caches."}
public const string PRIVATE = "private";
@Description {value:"Has the same meaning as must-revalidate, except that this does not apply to private caches."}
public const string PROXY_REVALIDATE = "proxy-revalidate";
@Description {value:"In shared caches, this overrides the max-age or Expires header field"}
public const string S_MAX_AGE = "s-maxage";

@Description {value:"A constant for indicating that the max-stale directive does not specify a limit."}
public const int MAX_STALE_ANY_AGE = 9223372036854775807;

@Description {value:"Cache control directives configuration for requests"}
@Field {value:"noCache: Represents the no-cache directive"}
@Field {value:"noStore: Represents the no-store directive"}
@Field {value:"noTransform: Represents the no-transform directive"}
@Field {value:"onlyIfCached: Represents the only-if-cached directive"}
@Field {value:"maxAge: Represents the max-age directive"}
@Field {value:"maxStale: Represents the max-stale directive"}
@Field {value:"minFresh: Represents the min-fresh directive"}
public struct RequestCacheControl {
    boolean noCache;
    boolean noStore;
    boolean noTransform;
    boolean onlyIfCached;
    int maxAge;
    int maxStale;
    int minFresh;
}

@Description {value:"Initializes the fields of the RequestCacheControl struct to their default values."}
@Param {value:"cacheControl: The struct to be initialized"}
public function <RequestCacheControl cacheControl> RequestCacheControl () {
    cacheControl.noCache = false;
    cacheControl.noStore = false;
    cacheControl.noTransform = true;
    cacheControl.onlyIfCached = false;
    cacheControl.maxAge = -1;
    cacheControl.maxStale = -1;
    cacheControl.minFresh = -1;
}

@Description {value:"Cache control directives configuration for responses"}
@Field {value:"mustRevalidate: Represents the must-revalidate directive"}
@Field {value:"noCache: Represents the no-cache directive"}
@Field {value:"noStore: Represents the no-store directive"}
@Field {value:"noTransform: Represents the no-transform directive"}
@Field {value:"isPrivate: Represents the private and public directives"}
@Field {value:"proxyRevalidate: Represents the proxy-revalidate directive"}
@Field {value:"maxAge: Represents the max-age directive"}
@Field {value:"sMaxAge: Represents the s-maxage directive"}
@Field {value:"noCacheFields: Optional fields for no-cache directive. If sending any of the listed fields in a response, they must validated with the origin server."}
@Field {value:"privateFields: Optional fields for private directive. A cache can omit the fields specified and store the rest of the response."}
public struct ResponseCacheControl {
    boolean mustRevalidate;
    boolean noCache;
    boolean noStore;
    boolean noTransform;
    boolean isPrivate;
    boolean proxyRevalidate;
    int maxAge;
    int sMaxAge;
    string[] noCacheFields;
    string[] privateFields;
}

@Description {value:"Initializes the fields of the ResponseCacheControl struct to their default values."}
@Param {value:"cacheControl: The struct to be initialized"}
public function <ResponseCacheControl cacheControl> ResponseCacheControl () {
    cacheControl.mustRevalidate = false;
    cacheControl.noCache = false;
    cacheControl.noStore = false;
    cacheControl.noTransform = true;
    cacheControl.isPrivate = false;
    cacheControl.proxyRevalidate = false;
    cacheControl.maxAge = -1;
    cacheControl.sMaxAge = -1;
    cacheControl.noCacheFields = [];
    cacheControl.privateFields = [];
}

@Description {value:"Build the cache control directives string from the current request cache control configurations"}
@Param {value:"cacheControl: The request cache control"}
public function <RequestCacheControl cacheControl> buildCacheControlDirectives () returns string {
    string[] directives = [];
    int i = 0;

    if (cacheControl.noCache) {
        directives[i] = NO_CACHE;
        i = i + 1;
    }

    if (cacheControl.noStore) {
        directives[i] = NO_STORE;
        i = i + 1;
    }

    if (cacheControl.noTransform) {
        directives[i] = NO_TRANSFORM;
        i = i + 1;
    }

    if (cacheControl.onlyIfCached) {
        directives[i] = ONLY_IF_CACHED;
        i = i + 1;
    }

    if (cacheControl.maxAge >= 0) {
        directives[i] = MAX_AGE + "=" + cacheControl.maxAge;
        i = i + 1;
    }

    if (cacheControl.maxStale == MAX_STALE_ANY_AGE) {
        directives[i] = MAX_STALE;
        i = i + 1;
    } else if (cacheControl.maxStale >= 0) {
        directives[i] = MAX_STALE + "=" + cacheControl.maxStale;
        i = i + 1;
    }

    if (cacheControl.minFresh >= 0) {
        directives[i] = MIN_FRESH + "=" + cacheControl.minFresh;
        i = i + 1;
    }

    return buildCommaSeparatedString(directives);
}

@Description {value:"Build the cache control directives string from the current response cache control configurations"}
@Param {value:"cacheControl: The response cache control"}
public function <ResponseCacheControl cacheControl> buildCacheControlDirectives () returns string {
    string[] directives = [];
    int i = 0;

    if (cacheControl.mustRevalidate) {
        directives[i] = MUST_REVALIDATE;
        i = i + 1;
    }

    if (cacheControl.noCache) {
        directives[i] = NO_CACHE + appendFields(cacheControl.noCacheFields);
        i = i + 1;
    }

    if (cacheControl.noStore) {
        directives[i] = NO_STORE;
        i = i + 1;
    }

    if (cacheControl.noTransform) {
        directives[i] = NO_TRANSFORM;
        i = i + 1;
    }

    if (cacheControl.isPrivate) {
        directives[i] = PRIVATE + appendFields(cacheControl.privateFields);
    } else {
        directives[i] = PUBLIC;
    }
    i = i + 1;

    if (cacheControl.proxyRevalidate) {
        directives[i] = PROXY_REVALIDATE;
        i = i + 1;
    }

    if (cacheControl.maxAge >= 0) {
        directives[i] = MAX_AGE + "=" + cacheControl.maxAge;
        i = i + 1;
    }

    if (cacheControl.sMaxAge >= 0) {
        directives[i] = S_MAX_AGE + "=" + cacheControl.sMaxAge;
        i = i + 1;
    }

    return buildCommaSeparatedString(directives);
}

function <Request request> parseCacheControlHeader () {
    request.cacheControl = {};

    // If the request doesn't contain a cache-control header, resort to default cache control settings
    if (!request.hasHeader(CACHE_CONTROL)) {
        return;
    }

    string cacheControl = request.getHeader(CACHE_CONTROL);
    string[] directives = cacheControl.split(",");

    foreach directive in directives {
        directive = directive.trim();
        if (directive == NO_CACHE) {
            request.cacheControl.noCache = true;
        } else if (directive == NO_STORE) {
            request.cacheControl.noStore = true;
        } else if (directive == NO_TRANSFORM) {
            request.cacheControl.noTransform = true;
        } else if (directive == ONLY_IF_CACHED) {
            request.cacheControl.onlyIfCached = true;
        } else if (directive.hasPrefix(MAX_AGE)) {
            request.cacheControl.maxAge = getExpirationDirectiveValue(directive);
        } else if (directive == MAX_STALE) {
            request.cacheControl.maxStale = MAX_STALE_ANY_AGE;
        } else if (directive.hasPrefix(MAX_STALE)) {
            request.cacheControl.maxStale = getExpirationDirectiveValue(directive);
        } else if (directive.hasPrefix(MIN_FRESH)) {
            request.cacheControl.minFresh = getExpirationDirectiveValue(directive);
        }
        // non-standard directives are ignored
    }
}

function appendFields (string[] fields) returns string {
    if (lengthof fields > 0) {
        return "=\"" + buildCommaSeparatedString(fields) + "\"";
    }
    return "";
}

function buildCommaSeparatedString (string[] values) returns string {
    string delimitedValues = values[0];

    int i = 1;
    while (i < lengthof values) {
        delimitedValues = delimitedValues + ", " + values[i];
        i = i + 1;
    }

    return delimitedValues;
}

function getExpirationDirectiveValue (string directive) returns int {
    string[] directiveParts = directive.split("=");

    // Disregarding the directive if a value isn't provided
    if (lengthof directiveParts != 2) {
        return -1;
    }

    match <int>directiveParts[1] {
        int age => return age;
        error => return -1; // Disregarding the directive if the value cannot be parsed
    }
}
