import ballerina/auth;
import ballerina/caching;

function testJwtAuthenticatorCreationWithCache () returns (auth:JWTAuthProvider) {
    auth:JWTAuthProviderConfig jwtConfig = {};
    //caching:Cache authCache;
    jwtConfig.issuer = "wso2";
    jwtConfig.audience = "ballerina";
    jwtConfig.certificateAlias = "ballerina";
    auth:JWTAuthProvider jwtAuthProvider = new (jwtConfig);
    return jwtAuthProvider;
}

function testAuthenticationSuccess (string jwtToken) returns (boolean|error) {
    auth:JWTAuthProviderConfig jwtConfig = {};
    caching:Cache authCache;
    jwtConfig.issuer = "wso2";
    jwtConfig.audience = "ballerina";
    jwtConfig.certificateAlias = "ballerina";
    auth:JWTAuthProvider jwtAuthProvider = new (jwtConfig);
    return jwtAuthProvider.authenticate(jwtToken);
}
