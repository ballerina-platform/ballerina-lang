import ballerina/auth.userstore;

function testCreateFileBasedUserstore () returns (userstore:FilebasedUserstore) {
    userstore:FilebasedUserstore fileBasedUserstore = new;
    return fileBasedUserstore;
}

function testAuthenticationOfNonExistingUser () returns (boolean) {
    userstore:FilebasedUserstore fileBasedUserstore = new;
    return fileBasedUserstore.authenticate("amila", "abc");
}

function testAuthenticationOfNonExistingPassword () returns (boolean) {
    userstore:FilebasedUserstore fileBasedUserstore = new;
    return fileBasedUserstore.authenticate("isuru", "xxy");
}

function testAuthentication () returns (boolean) {
    userstore:FilebasedUserstore fileBasedUserstore = new;
    return fileBasedUserstore.authenticate("isuru", "xxx");
}

function testReadGroupsOfNonExistingUser () returns (string[]) {
    userstore:FilebasedUserstore fileBasedUserstore = new;
    return fileBasedUserstore.readGroupsOfUser("amila");
}

function testReadGroupsOfUser () returns (string[]) {
    userstore:FilebasedUserstore fileBasedUserstore = new;
    return fileBasedUserstore.readGroupsOfUser("ishara");
}
