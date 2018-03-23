import ballerina/auth.basic;
import ballerina/auth.userstore;
import ballerina/caching;
import ballerina/auth.utils;

function testBasicAuthenticatorCreationWithoutCache () returns (basic:BasicAuthenticator, userstore:UserStore,
                                                               caching:Cache|null) {
    userstore:FilebasedUserstore fileBasedUserstore = {};
    basic:BasicAuthenticator authenticator = basic:createAuthenticator(fileBasedUserstore, null);
    return (authenticator, authenticator.userStore, authenticator.authCache);
}

function testBasicAuthenticatorCreationWithCache () returns (basic:BasicAuthenticator, userstore:UserStore,
                                                            caching:Cache|null) {
    userstore:FilebasedUserstore fileBasedUserstore = {};
    basic:BasicAuthenticator authenticator = basic:createAuthenticator(fileBasedUserstore,
                                                                       utils:createCache("auth_cache"));
    return (authenticator, authenticator.userStore, authenticator.authCache);
}

function testAuthenticationForNonExistingUser () returns (boolean) {
    userstore:FilebasedUserstore fileBasedUserstore = {};
    basic:BasicAuthenticator authenticator = basic:createAuthenticator(fileBasedUserstore, {});
    return authenticator.authenticate("amila", "yyy");
}

function testAuthenticationWithWrongPassword () returns (boolean) {
    userstore:FilebasedUserstore fileBasedUserstore = {};
    basic:BasicAuthenticator authenticator = basic:createAuthenticator(fileBasedUserstore, {});
    return authenticator.authenticate("isuru", "pqr");
}

function testAuthenticationSuccess () returns (boolean) {
    userstore:FilebasedUserstore fileBasedUserstore = {};
    basic:BasicAuthenticator authenticator = basic:createAuthenticator(fileBasedUserstore, {});
    return authenticator.authenticate("isuru", "xxx");
}


