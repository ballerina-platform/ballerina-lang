import ballerina/auth;
import ballerina/crypto;

function testJwtAuthenticatorCreationWithCache(string trustStorePath) returns (auth:JWTAuthProvider) {
    crypto:TrustStore trustStore = { path: trustStorePath, password: "ballerina" };
    auth:JWTAuthProviderConfig jwtConfig = {
        issuer: "wso2",
        audience: "ballerina",
        certificateAlias: "ballerina",
        trustStore: trustStore
    };
    auth:JWTAuthProvider jwtAuthProvider = new(jwtConfig);
    return jwtAuthProvider;
}

function testAuthenticationSuccess(string jwtToken, string trustStorePath) returns (boolean|error) {
    crypto:TrustStore trustStore = { path: trustStorePath, password: "ballerina" };
    auth:JWTAuthProviderConfig jwtConfig = {
        issuer: "wso2",
        audience: "ballerina",
        certificateAlias: "ballerina",
        trustStore: trustStore
    };
    auth:JWTAuthProvider jwtAuthProvider = new(jwtConfig);
    return jwtAuthProvider.authenticate(jwtToken);
}
