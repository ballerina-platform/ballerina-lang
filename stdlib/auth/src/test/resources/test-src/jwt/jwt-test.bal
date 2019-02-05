import ballerina/internal;
import ballerina/time;
import ballerina/auth;
import ballerina/crypto;

function testIssueJwt (string keyStorePath) returns (string)|error {
    auth:JwtHeader header = {};
    header.alg = auth:RS256;
    header.typ = "JWT";

    auth:JwtPayload payload = {};
    payload.sub = "John";
    payload.iss = "wso2";
    payload.jti = "100078234ba23";
    payload.aud = ["ballerina", "ballerinaSamples"];
    payload.exp = time:currentTime().time/1000 + 600;

    crypto:KeyStore keyStore = { path: keyStorePath, password: "ballerina" };
    return auth:issueJwt(header, payload, keyStore, "ballerina", "ballerina");
}

function testValidateJwt (string jwtToken, string trustStorePath) returns boolean|error {
    crypto:TrustStore trustStore = { path: trustStorePath, password: "ballerina" };
    auth:JWTValidatorConfig config = {
        issuer: "wso2",
        certificateAlias: "ballerina",
        audience: "ballerinaSamples",
        clockSkew: 60,
        trustStore: trustStore
    };

    var result = auth:validateJwt(jwtToken, config);
    if (result is auth:JwtPayload) {
        return true;
    } else {
        return result;
    }
}
