import ballerina/jwt;
import ballerina/time;

function testIssueJwt () (string, error) {
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
    var jwtString, e = jwt:issue(header, payload, config);
    return jwtString, e;
}

function testValidateJwt (string jwtToken) (boolean, error) {
    jwt:JWTValidatorConfig config = {};
    config.issuer = "wso2";
    config.certificateAlias = "ballerina";
    config.audience = "ballerinaSamples";
    var status, payload, e = jwt:validate(jwtToken, config);
    return status, e;
}
