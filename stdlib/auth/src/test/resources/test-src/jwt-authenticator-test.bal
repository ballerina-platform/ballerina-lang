import ballerina/auth;
import ballerina/crypto;

function testJwtAuthenticatorCreationWithCache(string trustStorePath) returns (auth:JWTAuthProvider) {
    crypto:TrustStore trustStore = { path: trustStorePath, password: "ballerina" };
    auth:JWTAuthProviderConfig jwtConfig = {
        issuer: "wso2",
        audience: ["ballerina"],
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
        audience: ["ballerina"],
        certificateAlias: "ballerina",
        trustStore: trustStore
    };
    auth:JWTAuthProvider jwtAuthProvider = new(jwtConfig);
    return jwtAuthProvider.authenticate(jwtToken);
}

function generateJwt(auth:JwtHeader header, auth:JwtPayload payload, string keyStorePath) returns string|error {
    crypto:KeyStore keyStore = { path: keyStorePath, password: "ballerina" };
    auth:JWTIssuerConfig issuerConfig = {
        keyStore: keyStore,
        keyAlias: "ballerina",
        keyPassword: "ballerina"
    };
    return auth:issueJwt(header, payload, issuerConfig);
}

function verifyJwt(string jwt, auth:JWTValidatorConfig config) returns auth:JwtPayload|error {
    return auth:validateJwt(jwt, config);
}
