import ballerina/auth.basic;
import ballerina/auth.userstore;
import ballerina/caching;
import ballerina/auth.utils;

function testBasicAuthenticatorCreationWithoutCache () returns (basic:BasicAuthenticator, userstore:UserStore,
                                                               caching:Cache) {
    basic:BasicAuthenticator authenticator = basic:createAuthenticator(getFileBasedUserstore(), {});
    return (authenticator, authenticator.userStore, authenticator.authCache);
}

function testBasicAuthenticatorCreationWithCache () returns (basic:BasicAuthenticator, userstore:UserStore,
                                                            caching:Cache) {
    basic:BasicAuthenticator authenticator = basic:createAuthenticator(getFileBasedUserstore(),
                                                                       utils:createCache("auth_cache"));
    return (authenticator, authenticator.userStore, authenticator.authCache);
}

function testCreateBasicAuthenticatorWithoutUserstore () {
    _ = basic:createAuthenticator({}, {});
}

function testAuthenticationForNonExistingUser () returns (boolean) {
    basic:BasicAuthenticator authenticator = basic:createAuthenticator(getFileBasedUserstore(), {});
    return authenticator.authenticate("amila", "yyy");
}

function testAuthenticationWithWrongPassword () returns (boolean) {
    basic:BasicAuthenticator authenticator = basic:createAuthenticator(getFileBasedUserstore(), {});
    return authenticator.authenticate("isuru", "pqr");
}

function testAuthenticationSuccess () returns (boolean) {
    basic:BasicAuthenticator authenticator = basic:createAuthenticator(getFileBasedUserstore(), {});
    return authenticator.authenticate("isuru", "xxx");
}

function getFileBasedUserstore () returns (userstore:UserStore) {
    userstore:FilebasedUserstore fileBasedUserstore = {};
    match <userstore:UserStore>fileBasedUserstore {
        userstore:UserStore userStore => return userStore;
        error err => return {};
    }
}
