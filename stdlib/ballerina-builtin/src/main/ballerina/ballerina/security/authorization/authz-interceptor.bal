package ballerina.security.authorization;

import ballerina.log;
import ballerina.net.http;
import ballerina.security.utils;

@Description {value:"Authorization cache name"}
const string AUTHZ_CACHE = "authz_cache";

@Description {value:"AuthorizationChecker instance"}
AuthorizationChecker authzChecker;

@Description {value:"Performs a authorization check, by comparing the groups of the user and the groups of the scope"}
@Param {value:"req: InRequest instance"}
@Param {value:"scopeName: name of the scope"}
@Param {value:"resourceName: name of the resource which is being accessed"}
@Return {value:"boolean: true if authorization check is a success, else false"}
public function handle (http:InRequest req, string scopeName, string resourceName) (boolean) {

    // TODO: extracting username and passwords are not required once the Ballerina SecurityContext is available
    // extract the header value
    string basicAuthHeaderValue;
    error err;
    basicAuthHeaderValue, err = utils:extractBasicAuthHeaderValue(req);
    if (err != null) {
        log:printErrorCause("Error in extracting basic authentication header", err);
        return false;
    }

    string username;
    username, _, err = utils:extractBasicAuthCredentials(basicAuthHeaderValue);
    if (err != null) {
        log:printErrorCause("Error in decoding basic authentication header", err);
        return false;
    }
    if (authzChecker == null) {
        authzChecker = createChecker({}, utils:createCache(AUTHZ_CACHE));
    }

    // check in the cache. cache key is <username>-<resource>,
    // since different resources can have different scopes
    string authzCacheKey = username + "-" + resourceName;
    boolean isAuthorized;
    any cachedAuthzResult = authzChecker.getCachedAuthzResult(authzCacheKey);
    if (cachedAuthzResult != null) {
        log:printDebug("Authz cache hit for user: " + username + ", request URL: " + resourceName);
        TypeCastError typeCastErr;
        isAuthorized, typeCastErr = (boolean)cachedAuthzResult;
        if (typeCastErr == null) {
            // no type cast error, return cached result.
            return isAuthorized;
        }
        // if a casting error occurs, clear the cache entry
        authzChecker.clearCachedAuthzResult(authzCacheKey);
    }
    log:printDebug("Authz cache miss for user: " + username + ", request URL: " + resourceName);

    isAuthorized = authzChecker.check(username, scopeName);
    if (isAuthorized) {
        log:printInfo("Successfully authorized user: " + username + " for resource: " + resourceName);
    } else {
        log:printInfo("Insufficient permission for user: " + username + " to access resource: " + resourceName);
    }
    authzChecker.cacheAuthzResult(authzCacheKey, isAuthorized, username, resourceName);
    return isAuthorized;
}
