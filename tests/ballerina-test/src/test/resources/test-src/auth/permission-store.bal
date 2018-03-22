import ballerina/auth.authz.permissionstore;

function testCreatePermissionstore () returns (permissionstore:FileBasedPermissionStore) {
    permissionstore:FileBasedPermissionStore permissionStore = {};
    return permissionStore;
}

function testReadGroupsForNonExistingScope () returns (string) {
    permissionstore:FileBasedPermissionStore permissionStore = {};
    return permissionStore.readGroupsOfScope("scope-x");
}

function testReadGroupsForScope () returns (string) {
    permissionstore:FileBasedPermissionStore permissionStore = {};
    return permissionStore.readGroupsOfScope("scope1");
}

function testReadGroupsForNonExistingUser () returns (string) {
    permissionstore:FileBasedPermissionStore permissionStore = {};
    return permissionStore.readGroupsOfUser("lahiru");
}

function testReadGroupsForUser () returns (string) {
    permissionstore:FileBasedPermissionStore permissionStore = {};
    return permissionStore.readGroupsOfUser("isuru");
}
