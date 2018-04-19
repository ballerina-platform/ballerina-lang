import ballerina/jwt;
import ballerina/time;
import ballerina/io;

function testIssueJwt (string keyStorePath) returns (string)|error {
    jwt:Header header = {};
    header.alg = "RS256";
    header.typ = "JWT";

    jwt:Payload payload = {};
    payload.sub = "John";
    payload.iss = "wso2";
    payload.jti = "100078234ba23";
    payload.aud = ["ballerina", "ballerinaSamples"];
    payload.exp = time:currentTime().time/1000 + 600;

    jwt:JWTIssuerConfig config = {};
    config.keyAlias = "ballerina";
    config.keyPassword = "ballerina";
    config.keyStoreFilePath = keyStorePath;
    config.keyStorePassword = "ballerina";
    match jwt:issue(header, payload, config) {
        string jwtString => return jwtString;
        error err => return err;
    }
}

function testValidateJwt (string jwtToken, string trustStorePath) returns (boolean)|error {
    io:println(jwtToken);
    jwt:JWTValidatorConfig config = {};
    config.issuer = "wso2";
    config.certificateAlias = "ballerina";
    config.audience = "ballerinaSamples";
    config.clockSkew = 60;
    config.trustStoreFilePath = trustStorePath;
    config.trustStorePassword = "ballerina";

    var value = jwt:validate(jwtToken, config);
    match value {
        (boolean, jwt:Payload) result => {
        var (isValid, payload) = result;
            return isValid;
        }
        error err => return err;
    }
}
