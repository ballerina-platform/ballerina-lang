package ballerina.net.http;

// TODO: Move this to the time package once it is moved to its own package.
public const string RFC_1123_DATE_TIME_FORMAT = "EEE, dd MMM yyyy HH:mm:ss Z";

const string CACHE_CONTROL_HEADER = "Cache-Control";

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

public function <OutResponse response> setCacheControlHeader () {
    if (response.cacheControl == null) {
        response.cacheControl = {};
    }
    string directives = response.cacheControl.buildCacheControlDirectives();
    response.setHeader(CACHE_CONTROL_HEADER, directives);
}

public function <ResponseCacheControl cacheControl> buildCacheControlDirectives () (string) {
    string directives = "";
    if (cacheControl.mustRevalidate) {
        directives = "must-revalidate";
    }

    if (cacheControl.noCache) {
        directives = directives + ", no-cache" + appendFields(cacheControl.noCacheFields);
    }

    if (cacheControl.noStore) {
        directives = directives + ", no-store";
    }

    if (cacheControl.noTransform) {
        directives = directives + ", no-transform";
    }

    if (cacheControl.isPrivate) {
        directives = directives + ", private" + appendFields(cacheControl.privateFields);
    } else {
        directives = directives + ", public";
    }

    if (cacheControl.proxyRevalidate) {
        directives = directives + ", proxy-revalidate";
    }

    if (cacheControl.maxAge >= 0) {
        directives = directives + ", max-age=" + cacheControl.maxAge;
    }

    if (cacheControl.sMaxAge >= 0) {
        directives = directives + ", s-maxage=" + cacheControl.sMaxAge;
    }

    return directives;
}

public function <OutResponse response> setLastModifiedHeader () {
    Time currentT = currentTime();
    // TODO: Need to look at a better way of doing this. Ideally, needs to use the actual RFC1123 formatter in JDK
    string lastModifiedTime = currentT.format(RFC_1123_DATE_TIME_FORMAT);
    response.setHeader("Last-Modified", lastModifiedTime);
}

function appendFields (string[] fields) (string) {
    if (fields != null && lengthof fields > 0) {
        string fieldString = "=\"" + fields[0];

        int i = 1;
        while (i < lengthof fields) {
            fieldString = fieldString + "," + fields[i];
            i = i + 1;
        }

        fieldString = fieldString + "\"";
        return fieldString;
    }

    return "";
}
