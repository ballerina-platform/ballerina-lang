import ballerina/auth.authz;
import ballerina/auth.authz.permissionstore;
import ballerina/caching;
import ballerina/auth.utils;

function testAuthzCheckerCreationWithoutCache () (authz:AuthzChecker,
                                                  permissionstore:PermissionStore,
                                                  caching:Cache) {
    permissionstore:FileBasedPermissionStore fileBasedPermissionstore = {};
    authz:AuthzChecker checker = authz:createChecker((permissionstore:PermissionStore)fileBasedPermissionstore, null);
    return checker, checker.permissionstore, checker.authzCache;
}

function testAuthzCheckerCreationWithCache () (authz:AuthzChecker,
                                               permissionstore:PermissionStore,
                                               caching:Cache) {
    permissionstore:FileBasedPermissionStore fileBasedPermissionstore = {};
    authz:AuthzChecker checker = authz:createChecker((permissionstore:PermissionStore)fileBasedPermissionstore,
                                                     utils:createCache("authz_cache"));
    return checker, checker.permissionstore, checker.authzCache;
}

function testAuthzCheckerWithoutPermissionstore () {
    _ = authz:createChecker(null, null);
}

function testAuthorizationForNonExistingUser () (boolean) {
    permissionstore:FileBasedPermissionStore fileBasedPermissionstore = {};
    authz:AuthzChecker checker = authz:createChecker((permissionstore:PermissionStore)fileBasedPermissionstore, null);
    return checker.check("ayoma", "scope-x");
}

function testAuthorizationForNonExistingScope () (boolean) {
    permissionstore:FileBasedPermissionStore fileBasedPermissionstore = {};
    authz:AuthzChecker checker = authz:createChecker((permissionstore:PermissionStore)fileBasedPermissionstore, null);
    return checker.check("ishara", "scope-y");
}

function testAuthorizationSuccess () (boolean) {
    permissionstore:FileBasedPermissionStore fileBasedPermissionstore = {};
    authz:AuthzChecker checker = authz:createChecker((permissionstore:PermissionStore)fileBasedPermissionstore, null);
    return checker.check("isuru", "scope2");
}
