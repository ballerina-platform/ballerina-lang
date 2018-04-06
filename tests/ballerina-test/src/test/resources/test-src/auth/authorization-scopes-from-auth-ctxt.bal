import ballerina/auth.authz;
import ballerina/auth.authz.permissionstore;
import ballerina/auth.utils;
import ballerina/runtime;

function testAuthorizationSuccessWithScopesFromAuthContext () returns (boolean) {
    permissionstore:FileBasedPermissionStore fileBasedPermissionstore = new;
    permissionstore:PermissionStore permissionStore = check <permissionstore:PermissionStore>fileBasedPermissionstore;
    authz:AuthzChecker checker = authz:createChecker(permissionStore, utils:createCache("authz_cache"));
    string[] scopes = ["scope2"];
    runtime:getInvocationContext().authenticationContext.scopes = ["scope2"];
    return checker.authorize("isuru",  "sayHello", "GET", scopes);
}

function testAuthorizationFailureWithScopesFromAuthContext () returns (boolean) {
    permissionstore:FileBasedPermissionStore fileBasedPermissionstore = new;
    permissionstore:PermissionStore permissionStore = check <permissionstore:PermissionStore>fileBasedPermissionstore;
    authz:AuthzChecker checker = authz:createChecker(permissionStore, utils:createCache("authz_cache"));
    string[] scopes = ["scope2"];
    runtime:getInvocationContext().authenticationContext.scopes = ["scope3"];
    return checker.authorize("isuru",  "sayHello", "GET", scopes);
}

