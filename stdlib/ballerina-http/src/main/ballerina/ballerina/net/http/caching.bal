package ballerina.net.http;

import ballerina.time;

// TODO: Move this to the time package once it is moved to its own package.
public const string RFC_1123_DATE_TIME_FORMAT = "EEE, dd MMM yyyy HH:mm:ss Z";

// Cache-control directives
public const string NO_CACHE = "no-cache";
public const string NO_STORE = "no-store";
public const string NO_TRANSFORM = "no-transform";
public const string MAX_AGE = "max-age";

// Request only cache-control directives
public const string MAX_STALE = "max-stale";
public const string MIN_FRESH = "min-fresh";
public const string ONLY_IF_CACHED = "only-if-cached";

// Response only cache-control directives
public const string MUST_REVALIDATE = "must-revalidate";
public const string PUBLIC = "public";
public const string PRIVATE = "private";
public const string PROXY_REVALIDATE = "proxy-revalidate";
public const string S_MAX_AGE = "s-maxage";

public const int MAX_STALE_ANY_AGE = 9223372036854775807;

public struct RequestCacheControl {
    boolean noCache = false;
    boolean noStore = false;
    boolean noTransform = true;
    boolean onlyIfCached = false;
    int maxAge = -1;
    int maxStale = -1;
    int minFresh = -1;
}

public struct ResponseCacheControl {
    boolean mustRevalidate = false;
    boolean noCache = false;
    boolean noStore = false;
    boolean noTransform = true;
    boolean isPrivate = false;
    boolean proxyRevalidate = false;
    int maxAge = -1;
    int sMaxAge = -1;
    string[] noCacheFields;
    string[] privateFields;
}

public function <OutRequest request> setCacheControlHeader () {
    if (request.cacheControl == null) {
        request.cacheControl = {};
    }
    string directives = request.cacheControl.buildCacheControlDirectives();
    request.setHeader(CACHE_CONTROL, directives);
}

// TODO: add a function for parsing cache-control header in OutRequest

public function <OutResponse response> setCacheControlHeader () {
    if (response.cacheControl == null) {
        response.cacheControl = {};
    }
    string directives = response.cacheControl.buildCacheControlDirectives();
    response.setHeader(CACHE_CONTROL, directives);
}

function <OutRequest request> parseCacheControlHeader () {
    string cacheControl = request.getHeader(CACHE_CONTROL);
    request.cacheControl = {};

    // If the request doesn't contain a cache-control header, resort to default cache control settings
    if (cacheControl == null) {
        return;
    }

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
            request.cacheControl.maxAge = getDirectiveValue(directive);
        } else if (directive == MAX_STALE) {
            request.cacheControl.maxStale = MAX_STALE_ANY_AGE;
        } else if (directive.hasPrefix(MAX_STALE)) {
            request.cacheControl.maxStale = getDirectiveValue(directive);
        } else if (directive.hasPrefix(MIN_FRESH)) {
            request.cacheControl.minFresh = getDirectiveValue(directive);
        }
        // non-standard directives are ignored
    }
}

public function <RequestCacheControl cacheControl> buildCacheControlDirectives () (string) {
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

public function <ResponseCacheControl cacheControl> buildCacheControlDirectives () (string) {
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

public function <OutResponse response> setLastModifiedHeader () {
    time:Time currentT = time:currentTime();
    // TODO: Need to look at a better way of doing this. Ideally, needs to use the actual RFC1123 formatter in JDK
    string lastModifiedTime = currentT.format(RFC_1123_DATE_TIME_FORMAT);
    response.setHeader(LAST_MODIFIED, lastModifiedTime);
}

function appendFields (string[] fields) (string) {
    if (fields != null && lengthof fields > 0) {
        return "=\"" + buildCommaSeparatedString(fields) + "\"";
    }

    return "";
}

function buildCommaSeparatedString (string[] values) (string) {
    string delimitedValues = values[0];

    int i = 1;
    while (i < lengthof values) {
        delimitedValues = delimitedValues + ", " + values[i];
        i = i + 1;
    }

    return delimitedValues;
}

function getDirectiveValue (string directive) (int) {
    string[] directiveParts = directive.split("=");

    // Disregarding the directive if a value isn't provided
    if (lengthof directiveParts != 2) {
        return -1;
    }

    int age;
    error err;
    age, err = <int>directiveParts[1];

    // Disregarding the directive if the value cannot be parsed
    return err == null?age:-1;
}
