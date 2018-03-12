import ballerina.auth.authz.permissionstore;

function testCreatePermissionstore () (permissionstore:FileBasedPermissionStore) {
    permissionstore:FileBasedPermissionStore permissionStore = {};
    return permissionStore;
}

function testReadGroupsForNonExistingScope () (string) {
    permissionstore:FileBasedPermissionStore permissionStore = {};
    return permissionStore.readGroupsOfScope("scope-x");
}

function testReadGroupsForScope () (string) {
    permissionstore:FileBasedPermissionStore permissionStore = {};
    return permissionStore.readGroupsOfScope("scope1");
}

function testReadGroupsForNonExistingUser () (string) {
    permissionstore:FileBasedPermissionStore permissionStore = {};
    return permissionStore.readGroupsOfUser("lahiru");
}

function testReadGroupsForUser () (string) {
    permissionstore:FileBasedPermissionStore permissionStore = {};
    return permissionStore.readGroupsOfUser("isuru");
}
