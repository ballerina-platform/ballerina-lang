package ballerina.security.authentication.basic;

import ballerina.security.authentication.userstore;
import ballerina.caching;
import ballerina.security.crypto;

@Description {value:"Represents a Basic Authenticator"}
@Field {value:"userstore: userstore object"}
@Field {value:"authCache: authentication cache object"}
public struct BasicAuthenticator {
    userstore:FilebasedUserstore userstore;
    caching:Cache authCache;
}

@Description {value:"Represents an authentication decision about a user"}
@Field {value:"username: user name"}
@Field {value:"isAuthenticated: authentication decision, true if authenticated, else false"}
public struct AuthenticationInfo {
    string username;
    boolean isAuthenticated;
}

@Description {value:"Creates a Basic Authenticator"}
@Return {value:"BasicAuthenticator instance"}
public function createAuthenticator (userstore:FilebasedUserstore userstore,
                                     caching:Cache cache) (BasicAuthenticator) {
    if (userstore == null) {
        // error, cannot proceed without userstore
        error e = {msg:"Userstore cannot be null for basic authenticator"};
        throw e;
    }

    BasicAuthenticator authenticator = {userstore:userstore, authCache:cache};
    return authenticator;
}

@Description {value:"Authenticates a request using basic auth"}
@Param {value:"username: username extracted from the basic authentication header"}
@Param {value:"password: password extracted from the basic authentication header"}
@Return {value:"boolean: true if authentication is a success, else false"}
public function <BasicAuthenticator authenticator> authenticate (string username, string password) (boolean) {
    //authenticate                 
    return authenticator.authenticateAgainstUserstore(username, password);
}

@Description {value:"Authenticates against the userstore"}
@Param {value:"username: user name"}
@Param {value:"password: password"}
@Return {value:"boolean: true if authentication is a success, else false"}
function <BasicAuthenticator authenticator> authenticateAgainstUserstore (string username, string password) (boolean) {
    string passwordHashReadFromUserstore = authenticator.userstore.readPasswordHash(username);
    if (passwordHashReadFromUserstore == null) {
        return false;
    }

    // compare the hashed password with then entry read from the userstore
    if (crypto:getHash(password, crypto:Algorithm.SHA256) == passwordHashReadFromUserstore) {
        return true;
    }
    return false;
}

@Description {value:"Retrieves the cached authentication result if any, for the given basic auth header value"}
@Param {value:"basicAuthCacheKey: basic authentication cache key - sha256(basic auth header)"}
@Return {value:"any: cached entry, or null in a cache miss"}
function <BasicAuthenticator authenticator> getCachedAuthResult (string basicAuthCacheKey) (any) {
    if (authenticator.authCache != null) {
        return authenticator.authCache.get(basicAuthCacheKey);
    }
    return null;
}

@Description {value:"Caches the authentication result"}
@Param {value:"basicAuthCacheKey: basic authentication cache key - sha256(basic auth header)"}
@Param {value:"authInfo: AuthenticationInfo instance containing authentication decision"}
function <BasicAuthenticator authenticator> cacheAuthResult (string basicAuthCacheKey, AuthenticationInfo authInfo) {
    if (authenticator.authCache != null) {
        authenticator.authCache.put(basicAuthCacheKey, authInfo);
    }
}

@Description {value:"Clears any cached authentication result"}
@Param {value:"basicAuthCacheKey: basic authentication cache key - sha256(basic auth header)"}
function <BasicAuthenticator authenticator> clearCachedAuthResult (string basicAuthCacheKey) {
    if (authenticator.authCache != null) {
        authenticator.authCache.remove(basicAuthCacheKey);
    }
}

@Description {value:"Creates AuthenticationInfo instance"}
@Param {value:"username: user name"}
@Param {value:"isAuthenticated: authentication decision"}
@Return {value:"AuthenticationInfo: Authentication decision instance, whether the user is authenticated or not"}
function createAuthenticationInfo (string username, boolean isAuthenticated) (AuthenticationInfo) {
    AuthenticationInfo authInfo = {username:username, isAuthenticated:isAuthenticated};
    return authInfo;
}
