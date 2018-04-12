import ballerina/jwt;
import ballerina/time;
import ballerina/io;

function testIssueJwt () returns (string)|error {
    jwt:Header header = {};
    header.alg = "RS256";
    header.typ = "JWT";

    jwt:Payload payload = {};
    payload.sub = "John";
    payload.iss = "wso2";
    payload.jti = "100078234ba23";
    payload.aud = ["ballerina", "ballerinaSamples"];
    payload.exp = time:currentTime().time + 600000;

    jwt:JWTIssuerConfig config = {};
    config.certificateAlias = "ballerina";
    config.keyPassword = "ballerina";
    match jwt:issue(header, payload, config) {
        string jwtString => return jwtString;
        error err => return err;
    }
}

function testValidateJwt (string jwtToken) returns (boolean)|error {
    io:println(jwtToken);
    jwt:JWTValidatorConfig config = {};
    config.issuer = "wso2";
    config.certificateAlias = "ballerina";
    config.audience = "ballerinaSamples";
    var value = jwt:validate(jwtToken, config);
    match value {
        (boolean, jwt:Payload) result => {
        var (isValid, payload) = result;
            return isValid;
        }
        error err => return err;
    }
}
