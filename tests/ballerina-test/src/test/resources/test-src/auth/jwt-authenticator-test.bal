import ballerina/auth.jwtAuth;

function testJwtAuthenticatorCreationWithCache () returns (jwtAuth:JWTAuthenticator) {
    return jwtAuth:createAuthenticator();
}

function testAuthenticationSuccess (string jwtToken) returns (boolean|error) {
    jwtAuth:JWTAuthenticator authenticator = jwtAuth:createAuthenticator();
    return authenticator.authenticate(jwtToken);
}
