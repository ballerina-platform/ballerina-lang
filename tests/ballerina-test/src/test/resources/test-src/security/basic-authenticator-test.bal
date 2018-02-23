import ballerina.security.authentication.basic;
import ballerina.security.authentication.userstore;
import ballerina.caching;
import ballerina.security.utils;

function testBasicAuthenticatorCreationWithoutCache () (basic:BasicAuthenticator, userstore:FilebasedUserstore,
                                                        caching:Cache) {
    basic:BasicAuthenticator authenticator = basic:createAuthenticator({}, null);
    return authenticator, authenticator.userstore, authenticator.authCache;
}

function testBasicAuthenticatorCreationWithCache () (basic:BasicAuthenticator, userstore:FilebasedUserstore,
                                                     caching:Cache) {
    basic:BasicAuthenticator authenticator = basic:createAuthenticator({}, utils:createCache("auth_cache"));
    return authenticator, authenticator.userstore, authenticator.authCache;
}

function testCreateBasicAuthenticatorWithoutUserstore () {
    _ = basic:createAuthenticator(null, null);
}

function testAuthenticationForNonExistingUser () (boolean) {
    basic:BasicAuthenticator authenticator = basic:createAuthenticator({}, null);
    return authenticator.authenticate("amila", "yyy");
}

function testAuthenticationWithWrongPassword () (boolean) {
    basic:BasicAuthenticator authenticator = basic:createAuthenticator({}, null);
    return authenticator.authenticate("isuru", "pqr");
}

function testAuthenticationSuccess () (boolean) {
    basic:BasicAuthenticator authenticator = basic:createAuthenticator({}, null);
    return authenticator.authenticate("isuru", "xxx");
}