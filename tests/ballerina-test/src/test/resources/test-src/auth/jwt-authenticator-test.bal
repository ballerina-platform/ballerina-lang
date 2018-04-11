import ballerina/auth;

function testJwtAuthenticatorCreationWithCache () returns (auth:JWTAuthProvider) {
    auth:JWTAuthProviderConfig jwtConfig = {};
    jwtConfig.issuer = "wso2";
    jwtConfig.audience = "ballerina";
    jwtConfig.certificateAlias = "ballerina";
    auth:JWTAuthProvider jwtAuthProvider = new (jwtConfig);
    return jwtAuthProvider;
}

function testAuthenticationSuccess (string jwtToken) returns (boolean|error) {
    auth:JWTAuthProviderConfig jwtConfig = {};
    jwtConfig.issuer = "wso2";
    jwtConfig.audience = "ballerina";
    jwtConfig.certificateAlias = "ballerina";
    auth:JWTAuthProvider jwtAuthProvider = new (jwtConfig);
    return jwtAuthProvider.authenticate(jwtToken);
}
