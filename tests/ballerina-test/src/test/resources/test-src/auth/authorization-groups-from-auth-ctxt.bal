import ballerina/auth.authz;
import ballerina/auth.authz.permissionstore;
import ballerina/auth.utils;
import ballerina/runtime;

function testAuthorizationSuccessWithGroupsFromAuthContext () returns (boolean) {
    permissionstore:FileBasedPermissionStore fileBasedPermissionstore = new;
    permissionstore:PermissionStore permissionStore = check <permissionstore:PermissionStore>fileBasedPermissionstore;
    authz:AuthzChecker checker = authz:createChecker(permissionStore, utils:createCache("authz_cache"));
    string[] scopes = ["scope2"];
    runtime:getInvocationContext().authenticationContext.groups = ["lmn"];
    return checker.authorize("isuru", "sayHello", "GET", scopes);
}

function testAuthorizationFailureWithGroupsFromAuthContext () returns (boolean) {
    permissionstore:FileBasedPermissionStore fileBasedPermissionstore = new;
    permissionstore:PermissionStore permissionStore = check <permissionstore:PermissionStore>fileBasedPermissionstore;
    authz:AuthzChecker checker = authz:createChecker(permissionStore, utils:createCache("authz_cache"));
    string[] scopes = ["scope1"];
    runtime:getInvocationContext().authenticationContext.groups = ["abc"];
    return checker.authorize("ishara", "sayHello", "GET", scopes);
}