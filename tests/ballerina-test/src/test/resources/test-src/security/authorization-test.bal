import ballerina.security.authorization;
import ballerina.security.authorization.permissionstore;
import ballerina.caching;
import ballerina.security.utils;

function testAuthzCheckerCreationWithoutCache () (authorization:AuthorizationChecker,
                                                  permissionstore:PermissionStore,
                                                  caching:Cache) {
    authorization:AuthorizationChecker checker = authorization:createChecker({}, null);
    return checker, checker.permissionstore, checker.authzCache;
}

function testAuthzCheckerCreationWithCache () (authorization:AuthorizationChecker,
                                                  permissionstore:PermissionStore,
                                                  caching:Cache) {
    authorization:AuthorizationChecker checker = authorization:createChecker({},  utils:createCache("authz_cache"));
    return checker, checker.permissionstore, checker.authzCache;
}

function testCreateBasicAuthenticatorWithoutPermissionstore () {
    _ = authorization:createChecker(null, null);
}

function testAuthorizationForNonExistingUser () (boolean) {
    authorization:AuthorizationChecker checker = authorization:createChecker({}, null);
    return checker.check("ayoma", "scope-x");
}

function testAuthorizationForNonExistingScope () (boolean) {
    authorization:AuthorizationChecker checker = authorization:createChecker({}, null);
    return checker.check("ishara", "scope-y");
}

function testAuthorizationSuccess () (boolean) {
    authorization:AuthorizationChecker checker = authorization:createChecker({}, null);
    return checker.check("isuru", "scope2");
}
