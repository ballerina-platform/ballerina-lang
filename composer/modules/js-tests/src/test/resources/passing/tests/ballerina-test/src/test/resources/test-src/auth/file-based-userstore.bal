import ballerina.auth.userstore;

function testCreateFileBasedUserstore () (userstore:FilebasedUserstore) {
    userstore:FilebasedUserstore fileBasedUserstore = {};
    return fileBasedUserstore;
}

function testAuthenticationOfNonExistingUser () (boolean) {
    userstore:FilebasedUserstore fileBasedUserstore = {};
    return fileBasedUserstore.authenticate("amila", "abc");
}

function testAuthenticationOfNonExistingPassword () (boolean) {
    userstore:FilebasedUserstore fileBasedUserstore = {};
    return fileBasedUserstore.authenticate("isuru", "xxy");
}

function testAuthentication () (boolean) {
    userstore:FilebasedUserstore fileBasedUserstore = {};
    return fileBasedUserstore.authenticate("isuru", "xxx");
}

function testReadGroupsOfNonExistingUser () (string) {
    userstore:FilebasedUserstore fileBasedUserstore = {};
    return fileBasedUserstore.readGroupsOfUser("amila");
}

function testReadGroupsOfUser () (string) {
    userstore:FilebasedUserstore fileBasedUserstore = {};
    return fileBasedUserstore.readGroupsOfUser("ishara");
}
