import ballerina/auth;
import ballerina/caching;
import ballerina/io;

function testJwtAuthenticatorCreationWithCache () returns (auth:JWTAuthProvider) {
    auth:JWTAuthProviderConfig jwtConfig = {};
    jwtConfig.issuer = "wso2";
    jwtConfig.audience = "ballerina";
    jwtConfig.certificateAlias = "ballerina";
    auth:JWTAuthProvider jwtAuthProvider = new (jwtConfig);
    return jwtAuthProvider;
}

function testAuthenticationSuccess (string jwtToken, string trustStorePath) returns (boolean|error) {
    auth:JWTAuthProviderConfig jwtConfig = {};
    jwtConfig.issuer = "wso2";
    jwtConfig.audience = "ballerina";
    jwtConfig.certificateAlias = "ballerina";
    jwtConfig.trustStoreFilePath = trustStorePath;
    jwtConfig.trustStorePassword = "ballerina";
    auth:JWTAuthProvider jwtAuthProvider = new (jwtConfig);
    return jwtAuthProvider.authenticate(jwtToken);
}
