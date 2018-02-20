package ballerina.security.authentication.basic;

import ballerina.net.http;
import ballerina.log;
import ballerina.security.utils;
import ballerina.security.crypto;

@Description {value:"Authentication cache name"}
const string AUTH_CACHE = "basic_auth_cache";

@Description {value:"Basic authenticator instance"}
BasicAuthenticator authenticator;

@Description {value:"Intercepts a request for authentication"}
@Param {value:"req: InRequest object"}
@Return {value:"boolean: true if authentication is a success, else false"}
public function handle (http:InRequest req) (boolean) {
    // extract the header value
    string basicAuthHeaderValue;
    error err;
    basicAuthHeaderValue, err = utils:extractBasicAuthHeaderValue(req);
    if (err != null) {
        log:printErrorCause("Error in extracting basic authentication header", err);
        return false;
    }

    if (authenticator == null) {
        authenticator = createAuthenticator({}, utils:createCache(AUTH_CACHE));
    }
    AuthenticationInfo authInfo;
    // check in the cache - cache key is the sha256 hash of the basic auth header value
    string basicAuthCacheKey = crypto:getHash(basicAuthHeaderValue, crypto:Algorithm.SHA256);
    any cachedAuthResult = authenticator.getCachedAuthResult(basicAuthCacheKey);
    if (cachedAuthResult != null) {
        // cache hit
        TypeCastError typeCastErr;
        authInfo, typeCastErr = (AuthenticationInfo)cachedAuthResult;
        if (typeCastErr == null) {
            // no type cast error, return cached result.
            log:printDebug("Auth cache hit for user: " + authInfo.username);
            return authInfo.isAuthenticated;
        }
        // if a casting error occurs, clear the cache entry
        authenticator.clearCachedAuthResult(basicAuthCacheKey);
    }

    string username;
    string password;
    username, password, err = utils:extractBasicAuthCredentials(basicAuthHeaderValue);
    if (err != null) {
        log:printErrorCause("Error in decoding basic authentication header", err);
        return false;
    }
    log:printDebug("Auth cache miss for user: " + username);

    authInfo = createAuthenticationInfo(username, authenticator.authenticate(username, password));
    // cache result
    authenticator.cacheAuthResult(basicAuthHeaderValue, authInfo);
    if (authInfo.isAuthenticated) {
        log:printInfo("Successfully authenticated user " + username + " against the userstore");
    } else {
        log:printInfo("Authentication failure for user " + username);
    }

    return authInfo.isAuthenticated;
}
