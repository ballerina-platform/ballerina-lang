import ballerina/internal;
import ballerina/time;

function testIssueJwt (string keyStorePath) returns (string)|error {
    internal:JwtHeader header = {};
    header.alg = "RS256";
    header.typ = "JWT";

    internal:JwtPayload payload = {};
    payload.sub = "John";
    payload.iss = "wso2";
    payload.jti = "100078234ba23";
    payload.aud = ["ballerina", "ballerinaSamples"];
    payload.exp = time:currentTime().time/1000 + 600;

    internal:JWTIssuerConfig config = {};
    config.keyAlias = "ballerina";
    config.keyPassword = "ballerina";
    config.keyStoreFilePath = keyStorePath;
    config.keyStorePassword = "ballerina";
    match internal:issue(header, payload, config) {
        string jwtString => return jwtString;
        error err => return err;
    }
}

function testValidateJwt (string jwtToken, string trustStorePath) returns boolean|error {
    internal:JWTValidatorConfig config = {};
    config.issuer = "wso2";
    config.certificateAlias = "ballerina";
    config.audience = "ballerinaSamples";
    config.clockSkew = 60;
    config.trustStoreFilePath = trustStorePath;
    config.trustStorePassword = "ballerina";

    var value = internal:validate(jwtToken, config);
    match value {
        internal:JwtPayload result => return true;
        error err => return err;
    }
}
