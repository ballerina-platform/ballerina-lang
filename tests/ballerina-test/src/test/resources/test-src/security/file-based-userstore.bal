import ballerina.security.authentication.userstore;

function testCreateFileBasedUserstore () (userstore:FilebasedUserstore) {
    userstore:FilebasedUserstore fileBasedUserstore = {};
    return fileBasedUserstore;
}

function testReadPasswordHashForNonExistingUser () (string) {
    userstore:FilebasedUserstore fileBasedUserstore = {};
    return fileBasedUserstore.readPasswordHash("amila");
}

function testReadPasswordHash () (string) {
    userstore:FilebasedUserstore fileBasedUserstore = {};
    return fileBasedUserstore.readPasswordHash("isuru");
}
