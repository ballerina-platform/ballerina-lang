import ballerina/auth.authz.permissionstore;

function testCreatePermissionstore () returns (permissionstore:FileBasedPermissionStore) {
    permissionstore:FileBasedPermissionStore permissionStore = {};
    return permissionStore;
}

function testReadGroupsForNonExistingScope () returns (string[]) {
    permissionstore:FileBasedPermissionStore permissionStore = {};
    return permissionStore.readGroupsOfScope("scope-x");
}

function testReadGroupsForScope () returns (string[]) {
    permissionstore:FileBasedPermissionStore permissionStore = {};
    return permissionStore.readGroupsOfScope("scope1");
}

function testReadGroupsForNonExistingUser () returns (string[]) {
    permissionstore:FileBasedPermissionStore permissionStore = {};
    return permissionStore.readGroupsOfUser("lahiru");
}

function testReadGroupsForUser () returns (string[]) {
    permissionstore:FileBasedPermissionStore permissionStore = {};
    return permissionStore.readGroupsOfUser("isuru");
}

function testAuthorizationFailure () returns (boolean) {
    permissionstore:FileBasedPermissionStore permissionStore = {};
    string[] scopes = ["scope1"];
    return permissionStore.isAuthorized("isuru", scopes);
}

function testAuthorizationSuccess () returns (boolean) {
    permissionstore:FileBasedPermissionStore permissionStore = {};
    string[] scopes = ["scope2"];
    return permissionStore.isAuthorized("isuru", scopes);
}

function testAuthorizationSuccessWithGroups () returns (boolean) {
    permissionstore:FileBasedPermissionStore permissionStore = {};
    string[] groups = ["lmn"];
    string[] scopes = ["scope2"];
    return permissionStore.isAuthorizedByGroups(groups, scopes);
}

function testAuthorizationFailureWithGroups () returns (boolean) {
    permissionstore:FileBasedPermissionStore permissionStore = {};
    string[] groups = ["lmn"];
    string[] scopes = ["scope1"];
    return permissionStore.isAuthorizedByGroups(groups, scopes);
}
