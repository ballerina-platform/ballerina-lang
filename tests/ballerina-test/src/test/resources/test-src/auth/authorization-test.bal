import ballerina/auth.authz;
import ballerina/auth.authz.permissionstore;
import ballerina/caching;
import ballerina/auth.utils;

function testAuthzCheckerCreationWithoutCache () returns (authz:AuthzChecker,
                                                  permissionstore:PermissionStore,
                                                  caching:Cache|()) {
    permissionstore:FileBasedPermissionStore fileBasedPermissionstore = new;
    permissionstore:PermissionStore permissionStore = check <permissionstore:PermissionStore>fileBasedPermissionstore;
    authz:AuthzChecker checker = authz:createChecker(permissionStore, ());
    return (checker, checker.permissionstore, checker.authzCache);
}

function testAuthzCheckerCreationWithCache () returns (authz:AuthzChecker,
                                               permissionstore:PermissionStore,
                                               caching:Cache|()) {
    permissionstore:FileBasedPermissionStore fileBasedPermissionstore = new;
    permissionstore:PermissionStore permissionStore = check <permissionstore:PermissionStore>fileBasedPermissionstore;
    authz:AuthzChecker checker = authz:createChecker(permissionStore, utils:createCache("authz_cache"));
    return (checker, checker.permissionstore, checker.authzCache);
}

//function testAuthzCheckerWithoutPermissionstore () {
//    _ = authz:createChecker(null, null);
//}

function testAuthorizationForNonExistingUser () returns (boolean) {
    permissionstore:FileBasedPermissionStore fileBasedPermissionstore = new;
    permissionstore:PermissionStore permissionStore = check <permissionstore:PermissionStore>fileBasedPermissionstore;
    authz:AuthzChecker checker = authz:createChecker(permissionStore, utils:createCache("authz_cache"));
    string[] scopes = ["scope1"];
    return checker.authorize("ayoma", "sayHello", "GET", scopes);
}

function testAuthorizationForNonExistingScope () returns (boolean) {
    permissionstore:FileBasedPermissionStore fileBasedPermissionstore = new;
    permissionstore:PermissionStore permissionStore = check <permissionstore:PermissionStore>fileBasedPermissionstore;
    authz:AuthzChecker checker = authz:createChecker(permissionStore, utils:createCache("authz_cache"));
    string[] scopes = ["scope-y"];
    return checker.authorize("ishara", "sayHello", "GET", scopes);
}

function testAuthorizationSuccess () returns (boolean) {
    permissionstore:FileBasedPermissionStore fileBasedPermissionstore = new;
    permissionstore:PermissionStore permissionStore = check <permissionstore:PermissionStore>fileBasedPermissionstore;
    authz:AuthzChecker checker = authz:createChecker(permissionStore, utils:createCache("authz_cache"));
    string[] scopes = ["scope2"];
    return checker.authorize("isuru", "sayHello", "GET", scopes);
}

function testAuthorizationSuccessWithMultipleScopes () returns (boolean) {
    permissionstore:FileBasedPermissionStore fileBasedPermissionstore = new;
    permissionstore:PermissionStore permissionStore = check <permissionstore:PermissionStore>fileBasedPermissionstore;
    authz:AuthzChecker checker = authz:createChecker(permissionStore, utils:createCache("authz_cache"));
    string[] scopes = ["scope2", "scope1"];
    return checker.authorize("isuru", "sayHello", "GET", scopes);
}
