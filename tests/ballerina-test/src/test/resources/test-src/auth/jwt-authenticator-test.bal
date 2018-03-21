import ballerina/auth.jwtAuth;

function testJwtAuthenticatorCreationWithCache () (jwtAuth:JWTAuthenticator) {
    return jwtAuth:createAuthenticator();
}

function testAuthenticationSuccess (string jwtToken) (boolean, error) {
    jwtAuth:JWTAuthenticator authenticator = jwtAuth:createAuthenticator();
    return authenticator.authenticate(jwtToken);
}
