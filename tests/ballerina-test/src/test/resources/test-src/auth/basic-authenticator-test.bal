import ballerina/auth.basic;
import ballerina/auth.userstore;
import ballerina/caching;
import ballerina/auth.utils;
import ballerina/runtime;

function testBasicAuthenticatorCreationWithoutCache () returns (basic:BasicAuthenticator, userstore:UserStore,
                                                               caching:Cache?) {
    userstore:FilebasedUserstore fileBasedUserstore = new;
    basic:BasicAuthenticator authenticator = basic:createAuthenticator(fileBasedUserstore, ());
    return (authenticator, authenticator.userStore, authenticator.authCache);
}

function testBasicAuthenticatorCreationWithCache () returns (basic:BasicAuthenticator, userstore:UserStore,
                                                            caching:Cache?) {
    userstore:FilebasedUserstore fileBasedUserstore = new;
    basic:BasicAuthenticator authenticator = basic:createAuthenticator(fileBasedUserstore,
                                                                       utils:createCache("auth_cache"));
    return (authenticator, authenticator.userStore, authenticator.authCache);
}

function testAuthenticationForNonExistingUser () returns (boolean) {
    userstore:FilebasedUserstore fileBasedUserstore = new;
    basic:BasicAuthenticator authenticator = basic:createAuthenticator(fileBasedUserstore, ());
    return authenticator.authenticate("amila", "yyy");
}

function testAuthenticationWithWrongPassword () returns (boolean) {
    userstore:FilebasedUserstore fileBasedUserstore = new;
    basic:BasicAuthenticator authenticator = basic:createAuthenticator(fileBasedUserstore, ());
    return authenticator.authenticate("isuru", "pqr");
}

function testUsernameInAuthContextWithAuthenticationFailure () returns (string) {
    userstore:FilebasedUserstore fileBasedUserstore = new;
    basic:BasicAuthenticator authenticator = basic:createAuthenticator(fileBasedUserstore, ());
    _ = authenticator.authenticate("isuru", "pqr");
    return runtime:getInvocationContext().authenticationContext.username;
}

function testAuthenticationSuccess () returns (boolean) {
    userstore:FilebasedUserstore fileBasedUserstore = new;
    basic:BasicAuthenticator authenticator = basic:createAuthenticator(fileBasedUserstore, ());
    return authenticator.authenticate("isuru", "xxx");
}

function testUsernameInAuthContextWithAuthenticationSuccess () returns (string) {
    userstore:FilebasedUserstore fileBasedUserstore = new;
    basic:BasicAuthenticator authenticator = basic:createAuthenticator(fileBasedUserstore, ());
    _ = authenticator.authenticate("isuru", "xxx");
    return runtime:getInvocationContext().authenticationContext.username;
}
