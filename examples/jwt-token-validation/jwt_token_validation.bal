import ballerina/crypto;
import ballerina/config;
import ballerina/jwt;
import ballerina/time;
import ballerina/io;

public function main() {

    // Defines the keystore
    crypto:KeyStore keyStore = {
        path: config:getAsString("b7a.home") + "bre/security/ballerinaKeystore.p12",
        password: "ballerina"
    };

    // Defines the truststore
    crypto:TrustStore trustStore = {
        path: config:getAsString("b7a.home") + "bre/security/ballerinaTruststore.p12",
        password: "ballerina"
    };

    // Defines the JWT keystore configurations
    jwt:JwtKeyStoreConfig config = {
        keyStore: keyStore,
        keyAlias: "ballerina",
        keyPassword: "ballerina"
    };

    // Defines the JWT validator configurations
    jwt:JwtValidatorConfig validatorConfig = {
        issuer: "wso2",
        audience: "ballerina",
        clockSkewInSeconds: 60,
        trustStoreConfig: {
            certificateAlias: "ballerina",
            trustStore: trustStore
        }
    };

    // Defines the JWT header and set the values
    jwt:JwtHeader header = {};
    header.alg = jwt:RS256;
    header.typ = "JWT";

    // Defines the JWT payload and set the values
    jwt:JwtPayload payload = {};
    payload.sub = "John";
    payload.iss = "wso2";
    payload.jti = "100078234ba23";
    payload.aud = ["ballerina", "ballerinaSamples"];
    payload.exp = time:currentTime().time/1000 + 600;

    // Issues a JWT token based on provided header, payload and config.
    string|error jwt = jwt:issueJwt(header, payload, config);
    if (jwt is string) {
        // Validates the created JWT token
        jwt:JwtPayload|error result = jwt:validateJwt(jwt, validatorConfig);
        io:println(result);
    } else {
        io:println("Error: ", jwt.detail()?.message);
    }
}
