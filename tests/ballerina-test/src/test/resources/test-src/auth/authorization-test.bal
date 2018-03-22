import ballerina/auth.authz;
import ballerina/auth.authz.permissionstore;
import ballerina/caching;
import ballerina/auth.utils;

function testAuthzCheckerCreationWithoutCache () returns (authz:AuthzChecker,
                                                  permissionstore:PermissionStore,
                                                  caching:Cache|null) {
    permissionstore:FileBasedPermissionStore fileBasedPermissionstore = {};
    permissionstore:PermissionStore permissionStore = <permissionstore:PermissionStore>fileBasedPermissionstore;
    authz:AuthzChecker checker = authz:createChecker(permissionStore, null);
    return (checker, checker.permissionstore, checker.authzCache);
}

function testAuthzCheckerCreationWithCache () returns (authz:AuthzChecker,
                                               permissionstore:PermissionStore,
                                               caching:Cache|null) {
    permissionstore:FileBasedPermissionStore fileBasedPermissionstore = {};
    permissionstore:PermissionStore permissionStore = <permissionstore:PermissionStore>fileBasedPermissionstore;
    authz:AuthzChecker checker = authz:createChecker(permissionStore, utils:createCache("authz_cache"));
    return (checker, checker.permissionstore, checker.authzCache);
}

//function testAuthzCheckerWithoutPermissionstore () {
//    _ = authz:createChecker(null, null);
//}

function testAuthorizationForNonExistingUser () returns (boolean) {
    permissionstore:FileBasedPermissionStore fileBasedPermissionstore = {};
    permissionstore:PermissionStore permissionStore = <permissionstore:PermissionStore>fileBasedPermissionstore;
    authz:AuthzChecker checker = authz:createChecker(permissionStore, utils:createCache("authz_cache"));
    return checker.check("ayoma", "scope1");
}

function testAuthorizationForNonExistingScope () returns (boolean) {
    permissionstore:FileBasedPermissionStore fileBasedPermissionstore = {};
    permissionstore:PermissionStore permissionStore = <permissionstore:PermissionStore>fileBasedPermissionstore;
    authz:AuthzChecker checker = authz:createChecker(permissionStore, utils:createCache("authz_cache"));
    return checker.check("ishara", "scope-y");
}

function testAuthorizationSuccess () returns (boolean) {
    permissionstore:FileBasedPermissionStore fileBasedPermissionstore = {};
    permissionstore:PermissionStore permissionStore = <permissionstore:PermissionStore>fileBasedPermissionstore;
    authz:AuthzChecker checker = authz:createChecker(permissionStore, utils:createCache("authz_cache"));
    return checker.check("isuru", "scope2");
}
