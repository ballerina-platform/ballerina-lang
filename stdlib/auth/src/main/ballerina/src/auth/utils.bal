// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import ballerina/lang.'array as arrays;
import ballerina/lang.'string as strings;
import ballerina/log;
import ballerina/runtime;
import ballerina/stringutils;

# Default charset to be used with password hashing.
public const string DEFAULT_CHARSET = "UTF-8";

# Prefix used to denote special configuration values.
public const string CONFIG_PREFIX = "@";

# Prefix used to denote that the config value is a SHA-256 hash.
public const string CONFIG_PREFIX_SHA256 = "@sha256:";

# Prefix used to denote that the config value is a SHA-384 hash.
public const string CONFIG_PREFIX_SHA384 = "@sha384:";

# Prefix used to denote that the config value is a SHA-512 hash.
public const string CONFIG_PREFIX_SHA512 = "@sha512:";

# Prefix used to denote Basic Authentication scheme.
public const string AUTH_SCHEME_BASIC = "Basic ";

# Prefix used to denote Bearer Authentication scheme.
public const string AUTH_SCHEME_BEARER = "Bearer ";

# The table name of the config user section of the TOML file.
const string CONFIG_USER_SECTION = "b7a.users";

# Extracts the username and password from the credential values.
#
# + credential - The credential values.
# + return - A `string` tuple with the extracted username and password or `Error` occurred while extracting credentials
public function extractUsernameAndPassword(string credential) returns [string, string]|Error {
    string decodedHeaderValue = "";

    byte[]|error result = arrays:fromBase64(credential);
    if (result is error) {
        return prepareError(result.reason(), result);
    } else {
        string|error fromBytesResults = strings:fromBytes(result);
        if (fromBytesResults is string) {
            decodedHeaderValue = fromBytesResults;
        } else {
            return prepareError(fromBytesResults.reason(), fromBytesResults);
        }
    }

    string[] decodedCredentials = stringutils:split(decodedHeaderValue, ":");
    if (decodedCredentials.length() != 2) {
        return prepareError("Incorrect credential format. Format should be username:password");
    } else {
        return [decodedCredentials[0], decodedCredentials[1]];
    }
}

# Log and prepare `error` as a `Error`.
#
# + message - Error message
# + err - `error` instance
# + return - Prepared `Error` instance
public function prepareError(string message, error? err = ()) returns Error {
    log:printError(message, err);
    Error authError;
    if (err is error) {
        authError = error(AUTH_ERROR, message = message, cause = err);
    } else {
        authError = error(AUTH_ERROR, message = message);
    }
    return authError;
}

# Set the authentication related values (scheme, auth token) to the authentication context of the invocation context.
#
# + scheme - Auth scheme (JWT, LDAP, OAuth2, Basic etc.)
# + authToken - Auth token (credential)
public function setAuthenticationContext(string scheme, string authToken) {
    runtime:InvocationContext invocationContext = runtime:getInvocationContext();
    invocationContext.authenticationContext = {
        scheme: scheme,
        authToken: authToken
    };
}

# Set the authentication related values (user id, username, scopes, claims) to the principal of the invocation context.
#
# + userId - User Id of the authenticated user.
# + username - Username of the authenticated user.
# + claims - Claims of the authenticated user.
# + scopes - Authenticated user scopes.
public function setPrincipal(public string? userId = (), public string? username = (), public string[]? scopes = (),
                             public map<any>? claims = ()) {
    runtime:InvocationContext invocationContext = runtime:getInvocationContext();
    if (!(userId is ())) {
        invocationContext.principal.userId = userId;
    }
    if (!(username is ())) {
        invocationContext.principal.username = username;
    }
    if (!(scopes is ())) {
        invocationContext.principal.scopes = scopes;
    }
    if (!(claims is ())) {
        invocationContext.principal.claims = claims;
    }
}

# Check whether the scopes of the user and scopes of resource matches.
#
# + resourceScopes - Scopes of resource
# + userScopes - Scopes of user
# + authzCacheKey - Authorization cache key
# + positiveAuthzCache - The cache for positive authorizations
# + negativeAuthzCache - The cache for negative authorizations
# + return - true if there is a match between resource and user scopes, else false
public function checkForScopeMatch(string[]|string[][] resourceScopes, string[] userScopes, string authzCacheKey,
                                   cache:Cache? positiveAuthzCache, cache:Cache? negativeAuthzCache) returns boolean {
    var authorizedFromCache = authorizeFromCache(authzCacheKey, positiveAuthzCache, negativeAuthzCache);
    if (authorizedFromCache is boolean) {
        return authorizedFromCache;
    } else {
        if (userScopes.length() > 0) {
            boolean authorized = true;
            if (resourceScopes is string[]) {
                authorized = matchScopes(resourceScopes, userScopes);
            } else {
                foreach string[] resourceScope in resourceScopes {
                    authorized = authorized && matchScopes(resourceScope, userScopes);
                }
            }
            cacheAuthzResult(authorized, authzCacheKey, positiveAuthzCache, negativeAuthzCache);
            return authorized;
        }
    }
    return false;
}

# Tries to retrieve authorization decision from the cached information, if any.
#
# + authzCacheKey - Cache key
# + positiveAuthzCache - The cache for positive authorizations
# + negativeAuthzCache - The cache for negative authorizations
# + return - `true` or `false` in case of a cache hit, `()` in case of a cache miss
function authorizeFromCache(string authzCacheKey, cache:Cache? positiveAuthzCache,
                            cache:Cache? negativeAuthzCache) returns boolean? {
    cache:Cache? pCache = positiveAuthzCache;
    if (pCache is cache:Cache) {
        var positiveCacheResponse = pCache.get(authzCacheKey);
        if (positiveCacheResponse is boolean) {
            return true;
        }
    }

    cache:Cache? nCache = negativeAuthzCache;
    if (nCache is cache:Cache) {
        var negativeCacheResponse = nCache.get(authzCacheKey);
        if (negativeCacheResponse is boolean) {
            return false;
        }
    }
    return ();
}

# Cached the authorization result.
#
# + authorized - boolean flag to indicate the authorization decision
# + authzCacheKey - Cache key
# + positiveAuthzCache - The cache for positive authorizations
# + negativeAuthzCache - The cache for negative authorizations
function cacheAuthzResult(boolean authorized, string authzCacheKey, cache:Cache? positiveAuthzCache,
                          cache:Cache? negativeAuthzCache) {
    if (authorized) {
        cache:Cache? pCache = positiveAuthzCache;
        if (pCache is cache:Cache) {
            pCache.put(authzCacheKey, authorized);
        }
    } else {
        cache:Cache? nCache = negativeAuthzCache;
         if (nCache is cache:Cache) {
            nCache.put(authzCacheKey, authorized);
         }
    }
}

# Tries to find a match between the two scope arrays.
#
# + resourceScopes - Scopes of resource
# + userScopes - Scopes of the user
# + return - true if one of the resourceScopes can be found at userScopes, else false
function matchScopes(string[] resourceScopes, string[] userScopes) returns boolean {
    foreach var resourceScope in resourceScopes {
        foreach var userScope in userScopes {
            if (resourceScope == userScope) {
                return true;
            }
        }
    }
    return false;
}
