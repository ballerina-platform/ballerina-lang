import ballerina.auth.basic;
import ballerina.auth.userstore;
import ballerina.caching;
import ballerina.auth.utils;

function testBasicAuthenticatorCreationWithoutCache () (basic:BasicAuthenticator, userstore:UserStore,
                                                        caching:Cache) {
    userstore:FilebasedUserstore fileBasedUserstore = {};
    basic:BasicAuthenticator authenticator = basic:createAuthenticator((userstore:UserStore)fileBasedUserstore,
                                                                       null);
    return authenticator, authenticator.userStore, authenticator.authCache;
}

function testBasicAuthenticatorCreationWithCache () (basic:BasicAuthenticator, userstore:UserStore,
                                                     caching:Cache) {
    userstore:FilebasedUserstore fileBasedUserstore = {};
    basic:BasicAuthenticator authenticator = basic:createAuthenticator((userstore:UserStore)fileBasedUserstore,
                                                                       utils:createCache("auth_cache"));
    return authenticator, authenticator.userStore, authenticator.authCache;
}

function testCreateBasicAuthenticatorWithoutUserstore () {
    _ = basic:createAuthenticator(null, null);
}

function testAuthenticationForNonExistingUser () (boolean) {
    userstore:FilebasedUserstore fileBasedUserstore = {};
    basic:BasicAuthenticator authenticator = basic:createAuthenticator((userstore:UserStore)fileBasedUserstore,
                                                                       null);
    return authenticator.authenticate("amila", "yyy");
}

function testAuthenticationWithWrongPassword () (boolean) {
    userstore:FilebasedUserstore fileBasedUserstore = {};
    basic:BasicAuthenticator authenticator = basic:createAuthenticator((userstore:UserStore)fileBasedUserstore,
                                                                       null);
    return authenticator.authenticate("isuru", "pqr");
}

function testAuthenticationSuccess () (boolean) {
    userstore:FilebasedUserstore fileBasedUserstore = {};
    basic:BasicAuthenticator authenticator = basic:createAuthenticator((userstore:UserStore)fileBasedUserstore,
                                                                       null);
    return authenticator.authenticate("isuru", "xxx");
}
