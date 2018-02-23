import ballerina.security.authorization.permissionstore;

function testCreatePermissionstore () (permissionstore:PermissionStore) {
    permissionstore:PermissionStore permissionStore = {};
    return permissionStore;
}

function testReadGroupsForNonExistingScope () (string) {
    permissionstore:PermissionStore permissionStore = {};
    return permissionStore.readGroupsOfScope("scope-x");
}

function testReadGroupsForScope () (string) {
    permissionstore:PermissionStore permissionStore = {};
    return permissionStore.readGroupsOfScope("scope1");
}

function testReadGroupsForNonExistingUser () (string) {
    permissionstore:PermissionStore permissionStore = {};
    return permissionStore.readGroupsOfUser("lahiru");
}

function testReadGroupsForUser () (string) {
    permissionstore:PermissionStore permissionStore = {};
    return permissionStore.readGroupsOfUser("isuru");
}
