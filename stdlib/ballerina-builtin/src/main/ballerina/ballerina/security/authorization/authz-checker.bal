package ballerina.security.authorization;

import ballerina.caching;
import ballerina.security.authorization.permissionstore;

@Description {value:"Representation of AuthorizationChecker"}
@Field {value:"authzCache: authorization cache instance"}
public struct AuthorizationChecker {
    permissionstore:PermissionStore permissionstore;
    caching:Cache authzCache;
}

@Description {value:"Creates a Basic Authenticator"}
@Return {value:"AuthorizationChecker instance"}
public function createChecker (permissionstore:PermissionStore permissionstore,
                               caching:Cache cache) (AuthorizationChecker) {
    if (permissionstore == null) {
        // error, cannot proceed without permissionstore
        error e = {message:"Permission store cannot be null for authz checker"};
        throw e;
    }

    AuthorizationChecker authzChecker = {permissionstore:permissionstore, authzCache:cache};
    return authzChecker;
}

@Description {value:"Performs a authorization check, by comparing the groups of the user and the groups of the scope"}
@Param {value:"username: user name"}
@Param {value:"scopeName: name of the scope"}
@Return {value:"boolean: true if authorization check is a success, else false"}
public function <AuthorizationChecker authzChecker> check (string username, string scopeName) (boolean) {
    // TODO: check if there are any groups set in the SecurityContext and if so, match against those.
    return authzChecker.checkAgainstPermissionStore(username, scopeName);
}

@Description {value:"Authenticates against permission store"}
@Param {value:"username: user name"}
@Param {value:"scopeName: name of the scope"}
@Return {value:"boolean: true if authorization check against the permission repo is successful, else false"}
function <AuthorizationChecker authzChecker> checkAgainstPermissionStore (string username, string scopeName) (boolean) {
    //authenticate
    string[] groupsForScope;
    error err;
    groupsForScope, err = getGroupsArray(authzChecker.permissionstore.readGroupsOfScope(scopeName));
    if (err != null) {
        // no groups found for this scope
        return false;
    }

    string[] groupsOfUser;
    groupsOfUser, err = getGroupsArray(authzChecker.permissionstore.readGroupsOfUser(username));
    if (err != null) {
        // no groups for user, authorization failure
        return false;
    }
    return matchGroups(groupsForScope, groupsOfUser);
}

@Description {value:"Matches the groups passed"}
@Param {value:"requiredGroupsForScope: array of groups for the scope"}
@Param {value:"groupsReadFromPermissionstore: array of groups for the user"}
@Return {value:"boolean: true if two arrays are equal in content, else false"}
function matchGroups (string[] requiredGroupsForScope, string[] groupsReadFromPermissionstore) (boolean) {
    int groupCountRequiredForResource = lengthof requiredGroupsForScope;
    int matchingRoleCount = 0;
    foreach groupReadFromPermissiontore in groupsReadFromPermissionstore {
        foreach groupRequiredForResource in requiredGroupsForScope {
            if (groupRequiredForResource == groupReadFromPermissiontore) {
                matchingRoleCount = matchingRoleCount + 1;
            }
        }
    }
    return matchingRoleCount == groupCountRequiredForResource;
}

@Description {value:"Construct an array of groups from the comma separed group string passed"}
@Param {value:"groupString: comma separated string of groups"}
@Return {value:"string[]: array of groups"}
@Return {value:"error: if the group string is nul or empty"}
function getGroupsArray (string groupString) (string[], error) {
    if (groupString == null || groupString.length() == 0) {
        return null, handleError("could not extract any groups from groupString: " + groupString);
    }
    return groupString.split(","), null;
}

@Description {value:"Retrieves the cached authorization result if any, for the given basic auth header value"}
@Param {value:"authzCacheKey: cache key - <username>-<resource>"}
@Return {value:"any: cached entry, or null in a cache miss"}
function <AuthorizationChecker authzChecker> getCachedAuthzResult (string authzCacheKey) (any) {
    if (authzChecker.authzCache != null) {
        return authzChecker.authzCache.get(authzCacheKey);
    }
    return null;
}

@Description {value:"Caches the authorization result"}
@Param {value:"authzCacheKey: cache key - <username>-<resource>"}
@Param {value:"isAuthorized: authorization decision"}
@Param {value:"username: user name"}
@Param {value:"requestUrl: request Url"}
function <AuthorizationChecker authzChecker> cacheAuthzResult (string authzCacheKey, boolean isAuthorized,
                                                               string username, string requestUrl) {
    if (authzChecker.authzCache != null) {
        authzChecker.authzCache.put(authzCacheKey, isAuthorized);
    }
}

@Description {value:"Clears any cached authorization result"}
@Param {value:"authzCacheKey: cache key - <username>-<resource>"}
function <AuthorizationChecker authzChecker> clearCachedAuthzResult (string authzCacheKey) {
    if (authzChecker.authzCache != null) {
        authzChecker.authzCache.remove(authzCacheKey);
    }
}

@Description {value:"Error handler"}
@Param {value:"message: error message"}
@Return {value:"error: error populated with the message"}
function handleError (string message) (error) {
    error e = {message:message};
    return e;
}