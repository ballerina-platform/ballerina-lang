import ballerina.crypto;
import ballerina.net.http;

@http:configuration {basePath:"/cache"}
service<http> cachingProxy {

    // The HTTP Caching Client accepts an HTTP client connector as its first argument.
    // The second argument is a struct (CacheConfig) for configuring the underlying cache of the caching client.
    // Here, we have only set the 'isShared' field of the CacheConfig struct since in this particular scenario,
    // the cache will be a public one. By default, the caching client is rigid about what responses it caches. The
    // default behaviour is to cache a response only if it contains the Cache-Control header and either the ETag header
    // or the Last-Modified header. Currently, there is only one other configuration, which is to disable the
    // aforementioned behaviour. This can be done by setting the cachingLevel CacheConfig option to
    // CachingLevel.SPECIFICATION. 
    endpoint<http:HttpClient> cachingClientEP {
        create http:HttpCachingClient(create http:HttpClient("http://localhost:8080", {}), {isShared:true});
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/"
    }
    resource cacheableResource (http:Connection conn, http:InRequest req) {
        http:InResponse res;
        res, _ = cachingEP.get("/hello", {});

        _ = conn.forward(res);
    }
}

// Sample backend service which serves cacheable responses.
@http:configuration {basePath:"/hello", port:8080}
service<http> helloWorld {

    string payload = "Hello, World!";

    @http:resourceConfig {path:"/"}
    resource sayHello (http:Connection conn, http:InRequest req) {
        http:OutResponse res = {};

        // The ResponseCacheControl struct in the OutResponse struct can be used for setting the cache control
        // directives associated with the response. Here we have set the max-age to 15 seconds indicating that the
        // response will be fresh for 15 seconds. The must-revalidate directive instructs that the cache should not
        // serve a stale response without validating it with the origin server first. The public directive is set by
        // setting isPrivate=false. This indicates that the response can be cached by even intermediary caches which
        // serves multiple users.
        res.cacheControl.maxAge = 15;
        res.cacheControl.mustRevalidate = true;
        res.cacheControl.isPrivate = false;

        // Once the cache control directives has been set, calling the buildCacheControlDirectives() function
        // takes all those directives and build the directives string which can then be set as the Cache-Control header
        res.setHeader(http:CACHE_CONTROL, res.cacheControl.buildCacheControlDirectives());

        // The getCRC32() function from the ballerina.security.crypto library can be used for generating ETags for
        // strings and blobs.
        string etag = crypto:getCRC32(payload);
        res.setHeader(http:ETAG, etag);

        res.setStringPayload(payload);
        _ = conn.respond(res);
    }
}

