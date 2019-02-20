import ballerina/auth;
import ballerina/crypto;
import ballerina/internal;
import ballerina/time;

function testIssueJwt(string keyStorePath) returns (string)|error {
    crypto:KeyStore keyStore = { path: keyStorePath, password: "ballerina" };
    auth:JWTIssuerConfig config = {
        keyStore: keyStore,
        keyAlias: "ballerina",
        keyPassword: "ballerina"
    };

    auth:JwtHeader header = {};
    header.alg = auth:RS256;
    header.typ = "JWT";

    auth:JwtPayload payload = {};
    payload.sub = "John";
    payload.iss = "wso2";
    payload.jti = "100078234ba23";
    payload.aud = ["ballerina", "ballerinaSamples"];
    payload.exp = time:currentTime().time/1000 + 600;

    return auth:issueJwt(header, payload, config);
}

function testIssueJwtWithSingleAud(string keyStorePath) returns (string)|error {
    crypto:KeyStore keyStore = { path: keyStorePath, password: "ballerina" };
    auth:JWTIssuerConfig config = {
        keyStore: keyStore,
        keyAlias: "ballerina",
        keyPassword: "ballerina"
    };

    auth:JwtHeader header = {};
    header.alg = auth:RS256;
    header.typ = "JWT";

    auth:JwtPayload payload = {};
    payload.sub = "John";
    payload.iss = "wso2";
    payload.jti = "100078234ba23";
    payload.aud = ["ballerina"];
    payload.exp = time:currentTime().time/1000 + 600;

    return auth:issueJwt(header, payload, config);
}

function testIssueJwtWithSingleAudAndAudAsArray(string keyStorePath) returns (string)|error {
    crypto:KeyStore keyStore = { path: keyStorePath, password: "ballerina" };
    auth:JWTIssuerConfig config = {
        keyStore: keyStore,
        keyAlias: "ballerina",
        keyPassword: "ballerina",
        audienceAsArray: true
    };

    auth:JwtHeader header = {};
    header.alg = auth:RS256;
    header.typ = "JWT";

    auth:JwtPayload payload = {};
    payload.sub = "John";
    payload.iss = "wso2";
    payload.jti = "100078234ba23";
    payload.aud = ["ballerina"];
    payload.exp = time:currentTime().time/1000 + 600;

    return auth:issueJwt(header, payload, config);
}

function testValidateJwt(string jwtToken, string trustStorePath) returns boolean|error {
    crypto:TrustStore trustStore = { path: trustStorePath, password: "ballerina" };
    auth:JWTValidatorConfig config = {
        issuer: "wso2",
        certificateAlias: "ballerina",
        audience: ["ballerinaSamples"],
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

function testIssueJwtWithNoIssOrSub(string keyStorePath) returns (string)|error {
    crypto:KeyStore keyStore = { path: keyStorePath, password: "ballerina" };
    auth:JWTIssuerConfig config = {
        keyStore: keyStore,
        keyAlias: "ballerina",
        keyPassword: "ballerina"
    };

    auth:JwtHeader header = {};
    header.alg = auth:RS256;
    header.typ = "JWT";

    auth:JwtPayload payload = {};
    payload.jti = "100078234ba23";
    payload.aud = ["ballerina", "ballerinaSamples"];
    payload.exp = time:currentTime().time/1000 + 600;

    return auth:issueJwt(header, payload, config);
}

function testIssueJwtWithNoAudOrSub(string keyStorePath) returns (string)|error {
    crypto:KeyStore keyStore = { path: keyStorePath, password: "ballerina" };
    auth:JWTIssuerConfig config = {
        keyStore: keyStore,
        keyAlias: "ballerina",
        keyPassword: "ballerina"
    };

    auth:JwtHeader header = {};
    header.alg = auth:RS256;
    header.typ = "JWT";

    auth:JwtPayload payload = {};
    payload.sub = "John";
    payload.iss = "wso2";
    payload.jti = "100078234ba23";
    payload.exp = time:currentTime().time/1000 + 600;

    return auth:issueJwt(header, payload, config);
}

function testValidateJwtWithNoIssOrSub(string jwtToken, string trustStorePath) returns boolean|error {
    crypto:TrustStore trustStore = { path: trustStorePath, password: "ballerina" };
    auth:JWTValidatorConfig config = {
        certificateAlias: "ballerina",
        audience: ["ballerinaSamples"],
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
