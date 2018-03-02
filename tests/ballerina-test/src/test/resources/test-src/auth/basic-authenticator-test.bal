import ballerina.auth.basic;
import ballerina.auth.userstore;
import ballerina.caching;
import ballerina.auth.utils;

function testBasicAuthenticatorCreationWithoutCache () (basic:BasicAuthenticator, userstore:CredentialsStore,
                                                        caching:Cache) {
    userstore:FilebasedUserstore fileBasedUserstore = {};
    basic:BasicAuthenticator authenticator = basic:createAuthenticator((userstore:CredentialsStore)fileBasedUserstore,
                                                                       null);
    return authenticator, authenticator.credentialsStore, authenticator.authCache;
}

function testBasicAuthenticatorCreationWithCache () (basic:BasicAuthenticator, userstore:CredentialsStore,
                                                     caching:Cache) {
    userstore:FilebasedUserstore fileBasedUserstore = {};
    basic:BasicAuthenticator authenticator = basic:createAuthenticator((userstore:CredentialsStore)fileBasedUserstore,
                                                                       utils:createCache("auth_cache"));
    return authenticator, authenticator.credentialsStore, authenticator.authCache;
}

function testCreateBasicAuthenticatorWithoutUserstore () {
    _ = basic:createAuthenticator(null, null);
}

function testAuthenticationForNonExistingUser () (boolean) {
    userstore:FilebasedUserstore fileBasedUserstore = {};
    basic:BasicAuthenticator authenticator = basic:createAuthenticator((userstore:CredentialsStore)fileBasedUserstore,
                                                                       null);
    return authenticator.authenticate("amila", "yyy");
}

function testAuthenticationWithWrongPassword () (boolean) {
    userstore:FilebasedUserstore fileBasedUserstore = {};
    basic:BasicAuthenticator authenticator = basic:createAuthenticator((userstore:CredentialsStore)fileBasedUserstore,
                                                                       null);
    return authenticator.authenticate("isuru", "pqr");
}

function testAuthenticationSuccess () (boolean) {
    userstore:FilebasedUserstore fileBasedUserstore = {};
    basic:BasicAuthenticator authenticator = basic:createAuthenticator((userstore:CredentialsStore)fileBasedUserstore,
                                                                       null);
    return authenticator.authenticate("isuru", "xxx");
}