import ballerina/auth;

function testCreateConfigAuthProvider() returns (auth:ConfigAuthProvider) {
    auth:ConfigAuthProvider configAuthProvider = new;
    return configAuthProvider;
}

function testAuthenticationOfNonExistingUser () returns (boolean) {
    auth:ConfigAuthProvider configAuthProvider = new;
    return configAuthProvider.authenticate("amila", "abc");
}

function testAuthenticationOfNonExistingPassword () returns (boolean) {
    auth:ConfigAuthProvider configAuthProvider = new;
    return configAuthProvider.authenticate("isuru", "xxy");
}

function testAuthentication () returns (boolean) {
    auth:ConfigAuthProvider configAuthProvider = new;
    return configAuthProvider.authenticate("isuru", "xxx");
}

function testReadScopesOfNonExistingUser() returns (string[]) {
    auth:ConfigAuthProvider configAuthProvider = new;
    return configAuthProvider.getScopes("amila");
}

function testReadScopesOfUser() returns (string[]) {
    auth:ConfigAuthProvider configAuthProvider = new;
    return configAuthProvider.getScopes("ishara");
}
